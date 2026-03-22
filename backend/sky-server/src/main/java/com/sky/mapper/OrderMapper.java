package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderHistoryVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * 根据订单状态统计个数，null表示所有状态
     * @return
     */
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

    /**
     * 查询时间范围内订单总数
     */
    @Select("select count(id) from orders where order_time>=#{initialTime} and order_time<=#{finalTime}")
    int countByRange(LocalDateTime initialTime, LocalDateTime finalTime);

    /**
     * 查询时间范围内指定状态订单数
     */
    @Select("select count(id) from orders where status=#{status} and order_time>=#{initialTime} and order_time<=#{finalTime}")
    int countByStatusAndRange(LocalDateTime initialTime, LocalDateTime finalTime, Integer status);

    /**
     * 统计销量top10(指定订单状态下)
     * 返回一个包含多个 Map 的列表，每个 Map 代表一行记录
     */
    // 不需要@MapKey()，iff一个字段映射大的map才需要
    List<GoodsSalesDTO> getTop10Sales(LocalDateTime begin, LocalDateTime end, Integer status);
}
