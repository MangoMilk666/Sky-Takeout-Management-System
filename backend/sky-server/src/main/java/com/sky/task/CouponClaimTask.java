package com.sky.task;

import com.sky.service.impl.CouponClaimConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CouponClaimTask {
    private static final String CLAIM_QUEUE_KEY = "sky:coupon:claim:queue";
    private static final String FAIL_QUEUE_KEY = "sky:coupon:claim:fail";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CouponClaimConsumer couponClaimConsumer;

    @Scheduled(fixedDelay = 200)
    public void consume() {
        for (int i = 0; i < 200; i++) {
            String msg = stringRedisTemplate.opsForList().rightPop(CLAIM_QUEUE_KEY);
            if (msg == null) {
                return;
            }
            try {
                couponClaimConsumer.consumeOne(msg);
            } catch (Exception e) {
                log.error("coupon claim async consume failed", e);
                stringRedisTemplate.opsForList().leftPush(FAIL_QUEUE_KEY, msg);
            }
        }
    }
}

