package com.sky.service;

import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderHistoryVO;
import com.sky.vo.OrderPaymentVO;
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
     * 分页查询历史订单
     * @return
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

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
    void cancelOrder(Long id);

    /**
     * 再来一单
     * @param id
     */
    void placeSameNewOrder(Long id);
}
