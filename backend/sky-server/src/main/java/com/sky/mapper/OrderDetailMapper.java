package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.ShoppingCart;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单明细数据
     */
    void insertBatchOrderDetail(List<OrderDetail> orderDetailList);

    /**
     * 查询某次订单的明细表
     */
    @Select("select id, name, image, order_id, dish_id, setmeal_id, dish_flavor, number, amount from order_detail where order_id = #{orderId}")
    List<OrderDetail> getDetailByOrderId(Long orderId);



}
