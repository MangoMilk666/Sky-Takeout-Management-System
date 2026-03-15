package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderHistoryVO;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@Slf4j
@RequestMapping("/user/order")
@Api(tags = "用户端订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单: {}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    // 正确逻辑（暂时跳过）
    /**
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }*/

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    // 测试使用逻辑
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<String> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        String estimatedDeliveryTime = orderService.getEstimatedTimeForTest(ordersPaymentDTO);
        log.info("支付完成!");
        return Result.success(estimatedDeliveryTime);
    }

    /**
     * 查询历史订单
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> getHistoryOrdersPage(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("用户端查询历史订单: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQueryByUser(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderHistoryVO> getOrderDetailsByUser(@PathVariable Long id){
        log.info("用户端查询id为{}的订单详情", id);
        OrderHistoryVO orderHistoryVO = orderService.getDetailsById(id);
        return Result.success(orderHistoryVO);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancelOrder(@PathVariable Long id){
        log.info("取消id为{}的订单", id);
        orderService.cancelOrderByUser(id);
        return Result.success();
    }

    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result placeSameNewOrder(@PathVariable Long id){
        log.info("再来一单，内容与id为{}订单内容相同", id);
        orderService.placeSameNewOrder(id);
        return Result.success();
    }
}
