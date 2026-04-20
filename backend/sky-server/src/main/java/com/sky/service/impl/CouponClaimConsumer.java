package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.entity.UserCoupon;
import com.sky.mapper.CouponMapper;
import com.sky.mapper.UserCouponMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 异步处理抢券成功请求的消费者类
 */
@Service
public class CouponClaimConsumer {
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private CouponMapper couponMapper;

    /**
     * 将成功的抢券数据更新到db
     */
    @Transactional
    public void consumeOne(String msg) {
        ClaimMsg claimMsg = JSON.parseObject(msg, ClaimMsg.class);
        if (claimMsg == null || claimMsg.userId == null || claimMsg.couponId == null || claimMsg.requestId == null) {
            return;
        }
        // 检查是否已写入db，requestId的唯一性
        Long existed = userCouponMapper.getIdByRequestId(claimMsg.requestId);
        if (existed != null) {
            return;
        }
        try {
            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(claimMsg.userId)
                    .couponId(claimMsg.couponId)
                    .status(0)
                    .usedTime(null)
                    .requestId(claimMsg.requestId)
                    .createTime(LocalDateTime.now())
                    .build();
            userCouponMapper.insert(userCoupon);
        } catch (DataIntegrityViolationException e) {
            return;
        }
        // 扣减db库存
        Integer updated = couponMapper.decreaseRemainedCount(claimMsg.couponId);
        if (updated == null || updated != 1) {
            throw new IllegalStateException("coupon remained_count mismatch");
        }
    }

    public static class ClaimMsg {
        public Long couponId;
        public Long userId;
        public String requestId;
        public Long ts;
    }
}

