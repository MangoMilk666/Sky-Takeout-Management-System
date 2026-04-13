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
    private static final String STOCK_KEY_PREFIX = "sky:coupon:stock:";
    private static final String USER_SET_KEY_PREFIX = "sky:coupon:users:";
    private static final String CLAIM_QUEUE_KEY = "sky:coupon:claim:queue";

    private static final String CLAIM_LUA = "local stock_key = KEYS[1] "
            + "local user_set_key = KEYS[2] "
            + "local user_id = ARGV[1] "
            + "if redis.call('SISMEMBER', user_set_key, user_id) == 1 then return -1 end "
            + "local stock = tonumber(redis.call('get', stock_key) or '0') "
            + "if stock <= 0 then return 0 end "
            + "redis.call('DECR', stock_key) "
            + "redis.call('SADD', user_set_key, user_id) "
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
        couponMapper.insert(coupon);

        String stockKey = STOCK_KEY_PREFIX + coupon.getId();
        String userSetKey = USER_SET_KEY_PREFIX + coupon.getId();
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(coupon.getTotalCount()));
        stringRedisTemplate.delete(userSetKey);
        return coupon.getId();
    }

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

        String stockKey = STOCK_KEY_PREFIX + couponId;
        String userSetKey = USER_SET_KEY_PREFIX + couponId;
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

        String requestId = couponClaimDTO.getRequestId();
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        String msg = JSON.toJSONString(new ClaimMsg(couponId, userId, requestId, System.currentTimeMillis()));
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
