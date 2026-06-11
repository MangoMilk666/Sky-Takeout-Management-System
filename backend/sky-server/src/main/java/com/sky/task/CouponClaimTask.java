package com.sky.task;

import com.alibaba.fastjson.JSON;
import com.sky.service.impl.CouponClaimConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 异步将成功抢券记录写入DB的任务类
 */
@Component
@Slf4j
public class CouponClaimTask {
    private static final String CLAIM_QUEUE_KEY = "sky:coupon:claim:queue";
    static final String FAIL_QUEUE_KEY = "sky:coupon:claim:fail";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    // 处理写入抢券成功数据的消费者
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
                log.error("优惠券异步落库失败，推入重试队列: {}", e.getMessage());
                // 解析消息，设 retryCount=1（首次失败），再推入失败队列
                pushToFailQueue(msg, 1);
            }
        }
    }

    /**
     * 将消息推入失败队列，并更新 retryCount
     */
    void pushToFailQueue(String msg, int retryCount) {
        try {
            CouponClaimConsumer.ClaimMsg claimMsg = JSON.parseObject(msg, CouponClaimConsumer.ClaimMsg.class);
            if (claimMsg == null) {
                log.error("消息解析失败，无法推入重试队列，消息丢失: {}", msg);
                return;
            }
            claimMsg.retryCount = retryCount;
            stringRedisTemplate.opsForList().leftPush(FAIL_QUEUE_KEY, JSON.toJSONString(claimMsg));
        } catch (Exception ex) {
            log.error("推入失败队列时异常，消息丢失: {}", msg, ex);
        }
    }
}

