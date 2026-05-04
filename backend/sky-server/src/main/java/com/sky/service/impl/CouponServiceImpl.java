package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CouponClaimDTO;
import com.sky.dto.CouponPublishDTO;
import com.sky.entity.Coupon;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.CouponMapper;
import com.sky.mapper.UserCouponMapper;
import com.sky.vo.UserCouponVO;
import com.sky.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class CouponServiceImpl implements CouponService {
    // redis优惠券，用户的key前缀
    private static final String STOCK_KEY_PREFIX = "sky:coupon:stock:";
    private static final String USER_SET_KEY_PREFIX = "sky:coupon:users:";
    // 异步领券消息队列 (Claim Queue)的key前缀
    private static final String CLAIM_QUEUE_KEY = "sky:coupon:claim:queue";
    /**
     * LUA脚本，处理用户的领券请求
     * 保证原子性
     * 减少网络IO
     * 无锁化设计
     * 由Redis单线程执行
     */
    private static final String CLAIM_LUA = "local stock_key = KEYS[1] "
            + "local user_set_key = KEYS[2] "
            + "local user_id = ARGV[1] "
            // 判断 user_id 是否在 user_set_key 对应的 Set 集合中(是否已经领取过)
            + "if redis.call('SISMEMBER', user_set_key, user_id) == 1 then return -1 end "
            // 获取当前奖池库存
            + "local stock = tonumber(redis.call('get', stock_key) or '0') "
            + "if stock <= 0 then return 0 end "
            // decr原子扣减库存，添加用户id到已领取用户的集合
            + "redis.call('DECR', stock_key) "
            + "redis.call('SADD', user_set_key, user_id) "
            // 抢券成功返回1
            + "return 1";

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> claimScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(CLAIM_LUA);
        return script;
    }

    /**
     * 发布优惠券
     * @param couponPublishDTO
     * @return
     */
    @Override
    @Transactional
    public Long publish(CouponPublishDTO couponPublishDTO) {
        Coupon coupon = Coupon.builder()
                .name(couponPublishDTO.getName())
                .discountType(couponPublishDTO.getDiscountType())
                .discount(couponPublishDTO.getDiscount())
                .totalCount(couponPublishDTO.getTotalCount())
                .remainedCount(couponPublishDTO.getTotalCount())
                .beginTime(couponPublishDTO.getBeginTime())
                .endTime(couponPublishDTO.getEndTime())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        // 先在db插入数据
        couponMapper.insert(coupon);

        String stockKey = STOCK_KEY_PREFIX + coupon.getId();
        String userSetKey = USER_SET_KEY_PREFIX + coupon.getId();
        // 写入库存
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(coupon.getTotalCount()));
        stringRedisTemplate.delete(userSetKey);
        return coupon.getId();
    }

    /**
     * 用户抢券
     */
    @Override
    public Long claim(CouponClaimDTO couponClaimDTO) {
        Long userId = BaseContext.getCurrentId();
        Long couponId = couponClaimDTO.getCouponId();
        Coupon coupon = couponMapper.getById(couponId);
        if (coupon == null) {
            throw new OrderBusinessException(MessageConstant.COUPON_NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getBeginTime() == null || coupon.getEndTime() == null || now.isBefore(coupon.getBeginTime()) || now.isAfter(coupon.getEndTime())) {
            throw new OrderBusinessException(MessageConstant.COUPON_NOT_AVAILABLE);
        }
        // 库存优惠券的redis key
        String stockKey = STOCK_KEY_PREFIX + couponId;
        // 标识所有已经抢到该券的用户ID集合的key
        String userSetKey = USER_SET_KEY_PREFIX + couponId;
        // 执行lua脚本，保证原子性
        Long result = stringRedisTemplate.execute(
                claimScript(),
                Arrays.asList(stockKey, userSetKey),
                String.valueOf(userId)
        );
        if (result == null) {
            throw new OrderBusinessException(MessageConstant.UNKNOWN_ERROR);
        }
        if (result == -1L) {
            throw new OrderBusinessException(MessageConstant.COUPON_DUPLICATE);
        }
        if (result == 0L) {
            throw new OrderBusinessException(MessageConstant.COUPON_OUT_OF_STOCK);
        }
        // requestId（请求唯一标识符）
        // 实际开发，最好由前端生成一个 UUID（或者根据 userId + couponId 拼接一个唯一的字符串）
        // 防止用户重复点击破坏幂等性
        String requestId = couponClaimDTO.getRequestId();
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // 异步领券消息队列 (Claim Queue)，存储序列化后的 JSON 字符串
        String msg = JSON.toJSONString(new ClaimMsg(couponId, userId, requestId, System.currentTimeMillis()));
        // 采用 生产者-消费者模型。后端只负责通过 LPUSH 把任务丢进队列，然后立即返回给前端“抢券成功”的结果。
        // 真正的数据库写入（持久化）由另一个异步监听器负责，实现流量削峰。
        // 有另外的监听器在不断 BRPOP 这个 List，然后执行 userCouponMapper.insert 将数据存入 MySQL。
        stringRedisTemplate.opsForList().leftPush(CLAIM_QUEUE_KEY, msg);
        return 1L;
    }

    @Override
    public List<UserCouponVO> listMine() {
        Long userId = BaseContext.getCurrentId();
        return userCouponMapper.listAvailableByUserId(userId);
    }

    @Override
    public List<Coupon> listAvailable() {
        return couponMapper.listAvailable();
    }

    private static class ClaimMsg {
        public Long couponId;
        public Long userId;
        public String requestId;
        public Long ts;

        public ClaimMsg(Long couponId, Long userId, String requestId, Long ts) {
            this.couponId = couponId;
            this.userId = userId;
            this.requestId = requestId;
            this.ts = ts;
        }
    }
}
