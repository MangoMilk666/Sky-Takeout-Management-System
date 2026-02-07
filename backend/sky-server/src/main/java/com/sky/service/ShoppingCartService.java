package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

public interface ShoppingCartService {

    /**
     * 减少购物车商品数量
     * @param shoppingCartDTO
     */
    void subItem(ShoppingCartDTO shoppingCartDTO);

    /**
     * 添加购物车商品
     * @param shoppingCartDTO
     */
    void addItem(ShoppingCartDTO shoppingCartDTO);
}
