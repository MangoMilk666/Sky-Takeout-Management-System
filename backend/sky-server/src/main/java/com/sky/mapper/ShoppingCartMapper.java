package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

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
     * 删除1条购物车商品
     * @param cart
     */
    void delete(ShoppingCart cart);


    /**
     * 更新购物车（商品）,参数已经带id
     * @param cart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateById(ShoppingCart cart);


}
