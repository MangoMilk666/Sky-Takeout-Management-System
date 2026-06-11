package com.sky.task;

import com.alibaba.fastjson.JSON;
import com.sky.service.impl.CouponClaimConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 针对失败队列（claim:fail）的重试补偿任务。
 *
 * 策略：
 *   - 每 30 秒轮询一次失败队列
 *   - retryCount <= MAX_RETRY：重新调用 consumeOne（幂等安全）
 *   - 重试成功：done
 *   - 重试失败：retryCount + 1，回推失败队列
 *   - retryCount > MAX_RETRY：移入死信队列（claim:dead），告警并等待人工处理
 */
@Component
@Slf4j
public class CouponClaimRetryTask {

    /** 最大重试次数（首次失败计为第 1 次，超过此值进死信队列） */
    private static final int MAX_RETRY = 3;
    private static final String FAIL_QUEUE_KEY = "sky:coupon:claim:fail";
    private static final String DEAD_QUEUE_KEY  = "sky:coupon:claim:dead";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CouponClaimConsumer couponClaimConsumer;

    @Scheduled(fixedDelay = 30_000)
    public void retryFailed() {
        for (int i = 0; i < 50; i++) {
            String msg = stringRedisTemplate.opsForList().rightPop(FAIL_QUEUE_KEY);
            if (msg == null) {
                return;
            }

            CouponClaimConsumer.ClaimMsg claimMsg;
            try {
                claimMsg = JSON.parseObject(msg, CouponClaimConsumer.ClaimMsg.class);
            } catch (Exception e) {
                log.error("[重试任务] 消息解析异常，移入死信队列: {}", msg, e);
                stringRedisTemplate.opsForList().leftPush(DEAD_QUEUE_KEY, msg);
                continue;
            }

            if (claimMsg == null) {
                log.error("[重试任务] 空消息，移入死信队列: {}", msg);
                stringRedisTemplate.opsForList().leftPush(DEAD_QUEUE_KEY, msg);
                continue;
            }

            // 当前是第几次重试（retryCount 由 CouponClaimTask 首次设为 1）
            int retryCount = claimMsg.retryCount == null ? 1 : claimMsg.retryCount;

            if (retryCount > MAX_RETRY) {
                log.error("[重试任务] 已超过最大重试次数({})，移入死信队列: couponId={}, userId={}, requestId={}",
                        MAX_RETRY, claimMsg.couponId, claimMsg.userId, claimMsg.requestId);
                stringRedisTemplate.opsForList().leftPush(DEAD_QUEUE_KEY, msg);
                continue;
            }

            log.info("[重试任务] 第 {} 次重试: couponId={}, userId={}", retryCount, claimMsg.couponId, claimMsg.userId);
            try {
                couponClaimConsumer.consumeOne(msg);
                log.info("[重试任务] 重试成功: couponId={}, userId={}", claimMsg.couponId, claimMsg.userId);
            } catch (Exception e) {
                log.error("[重试任务] 第 {} 次重试仍失败: couponId={}, userId={}, error={}",
                        retryCount, claimMsg.couponId, claimMsg.userId, e.getMessage());
                // 递增重试次数，回推失败队列，等待下一轮
                claimMsg.retryCount = retryCount + 1;
                stringRedisTemplate.opsForList().leftPush(FAIL_QUEUE_KEY, JSON.toJSONString(claimMsg));
            }
        }
    }
}
