package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 减少购物车中一个商品数量
     * @param shoppingCartDTO
     */
    void subItem(ShoppingCartDTO shoppingCartDTO);

    /**
     * 添加购物车商品
     * @param shoppingCartDTO
     */
    void addItem(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车商品
     * @return
     */
    List<ShoppingCart> list();

    /**
     * 清空当前用户的购物车
     */
    void clearShoppingCart();

}
