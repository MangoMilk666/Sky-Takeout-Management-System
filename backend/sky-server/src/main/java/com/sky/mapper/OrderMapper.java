package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderHistoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入1条订单数据
     * @param order
     */
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 用户(分页）查询历史订单信息
     */
    @Select("select * from orders where user_id = #{userId}")
    Page<OrderHistoryVO> pageQueryByUser(OrdersPageQueryDTO ordersPageQueryDTO);


    /**
     * 根据订单id查询订单
     */
    @Select("select * from orders where id = #{orderId}")
    Orders getById(Long orderId);

    /**
     * 管理端(分页）查询历史订单信息
     */
    Page<OrderHistoryVO> pageQueryByAdmin(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单状态统计个数
     * @return
     */
    @Select("select count(*) from orders where status = #{orderStatus}")
    Integer countByOrderStatus(Integer ordersStatus);

    /**
     * 根据订单状态和下单时间查询
     */
    @Select("select * from orders where status=#{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime time);

    /**
     * 查询时间范围内的营业额
     */
    @Select("select (case when sum(amount) is not null then sum(amount) else 0 end) from orders where status = 5 and order_time>=#{beginTime} and order_time <= #{endTime}")
    BigDecimal getTurnoverStatistics(LocalDateTime beginTime, LocalDateTime endTime);

}
