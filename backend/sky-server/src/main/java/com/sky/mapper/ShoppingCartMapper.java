package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 添加购物车数据
     */
    @Insert("insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "VALUES (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据菜品/套餐id等信息查询该用户购物车中的数据
     */
    ShoppingCart getCartItem(ShoppingCart queryCart);

    /**
     * 删除1条指定的购物车商品
     * @param cart
     */
    void delete(ShoppingCart cart);


    /**
     * 更新购物车商品（数量）,参数已经带id
     * @param cart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateById(ShoppingCart cart);


    /**
     * 查看该用户的购物车商品数据
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> getList(Long userId);

    /**
     * 根据用户Id清空购物车数据
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void clearCart(Long userId);
}
