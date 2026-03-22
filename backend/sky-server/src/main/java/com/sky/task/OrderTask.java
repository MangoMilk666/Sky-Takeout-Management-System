package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，按时处理订单
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
//    @Scheduled(cron = "0 * * * * ? ") //每分钟
    public void processTimeoutOrders(){
        log.info("定时处理超时订单: {}", LocalDateTime.now());
        LocalDateTime ddl =  LocalDateTime.now().plusMinutes(-15);
        // 查询下单时间，处理下单时间 < 现在-15min的未支付订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, ddl);
        // 遍历修改
        if (ordersList!=null &&  ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理长期处于派送状态订单
     */
//    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨1点
    public void processDeliveringOrders() {
        log.info("处理长期处于派送中状态的订单: {}", LocalDateTime.now());
        LocalDateTime ddl =  LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList =  orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, ddl);
        if (ordersList!=null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orders.setDeliveryTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }
}
