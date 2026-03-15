package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderHistoryVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 用户端分页查询历史订单
     * @return
     */
    PageResult pageQueryByUser(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 仅供测试使用，跳过支付逻辑，返回预计送达时间
     * @param ordersPaymentDTO
     * @return
     */
    String getEstimatedTimeForTest(OrdersPaymentDTO ordersPaymentDTO);

    /**
     * 根据订单id查询订单详情
     * @param orderId
     * @return
     */
    OrderHistoryVO getDetailsById(Long orderId);

    /**
     * 取消订单
     * @param id
     */
    void cancelOrderByUser(Long id);

    /**
     * 再来一单
     * @param id
     */
    void placeSameNewOrder(Long id);

    /**
     * 管理段分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQueryByAdmin(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO getOrderStatistics();

    /**
     * 管理端接单
     * @param id
     */
    void confirmOrderByAdmin(Long id);

    /**
     * 管理端拒单
     * @param ordersRejectionDTO
     */
    void rejectOrderByAdmin(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 管理端取消订单
     * @param ordersCancelDTO
     */
    void cancelOrderByAdmin(OrdersCancelDTO ordersCancelDTO);

    /**
     * 派送订单
     * @param id
     */
    void deliverOrder(Long id);

    /**
     * 完成订单
     * @param id
     */
    void completeOrder(Long id);
}
