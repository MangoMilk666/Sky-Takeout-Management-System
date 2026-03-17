package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderHistoryVO;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@Slf4j
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    /**
     * 订单搜索
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result searchOrdersPage(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("管理端分页搜索订单: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQueryByAdmin(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> getOrderStatistics(){
        log.info("管理端统计各个状态的订单数量");
        OrderStatisticsVO orderStatisticsVO = orderService.getOrderStatistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 管理端查询订单详情
     */
    @GetMapping("/details/{id}")
    @ApiOperation("管理端查询订单详情")
    public Result<OrderHistoryVO> getOrderDetailByAdmin(@PathVariable Long id){
        log.info("管理端查询id为{}的订单", id);
        OrderHistoryVO orderHistoryVO = orderService.getDetailsById(id);
        return Result.success(orderHistoryVO);
    }

    /**
     * 接单
     */
    @PutMapping("/confirm")
    @ApiOperation("管理端接单")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("管理端接单: {}", ordersConfirmDTO);
        orderService.confirmOrderByAdmin(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     */
    @PutMapping("/rejection")
    @ApiOperation("管理端拒单")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("管理端拒绝订单：{}", ordersRejectionDTO);
        orderService.rejectOrderByAdmin(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    @ApiOperation("管理端取消订单")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("管理端取消订单: {}", ordersCancelDTO);
        orderService.cancelOrderByAdmin(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 派送订单
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result deliverOrder(@PathVariable Long id){
        log.info("派送订单: {}", id);
        orderService.deliverOrder(id);
        return Result.success();
    }

    /**
     * 完成订单
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result completeOrder(@PathVariable Long id){
        log.info("完成id为{}的订单", id);
        orderService.completeOrder(id);
        return Result.success();
    }




}
