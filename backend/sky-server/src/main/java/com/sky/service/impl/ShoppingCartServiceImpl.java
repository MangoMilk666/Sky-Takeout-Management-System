package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void subItem(ShoppingCartDTO shoppingCartDTO) {
        System.out.println();
    }

    @Override
    public void addItem(ShoppingCartDTO shoppingCartDTO) {
        // 查询菜品或套餐信息
        Dish dish = null;
        Setmeal setmeal = null;
        if (shoppingCartDTO.getDishId() != null) {
            dish = dishMapper.getById(shoppingCartDTO.getDishId());
        } else if (shoppingCartDTO.getSetmealId() != null) {
            setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
        }

        // 封装一个查询用的ShoppingCart对象， 查询相同菜品/套餐是否已经存在购物车
        ShoppingCart queryCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, queryCart);
        ShoppingCart cart = shoppingCartMapper.getCartItem(queryCart);

        // 已经存在，update, 数量+1
        if (cart != null) {
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateById(cart);
        } else {
            // 若不存在，插入一条数据
            // 菜品
            if (dish != null) {
                cart = ShoppingCart.builder()
                        .name(dish.getName())
                        .userId(BaseContext.getCurrentId())
                        .dishId(shoppingCartDTO.getDishId())
                        .dishFlavor(shoppingCartDTO.getDishFlavor())
                        .setmealId(shoppingCartDTO.getSetmealId())
                        .number(1)
                        .amount(dish.getPrice())
                        .image(dish.getImage())
                        .createTime(LocalDateTime.now())
                        .build();
            } else if (setmeal != null) {
                cart = ShoppingCart.builder()
                        .name(setmeal.getName())
                        .userId(BaseContext.getCurrentId())
                        .dishId(shoppingCartDTO.getDishId())
                        .dishFlavor(shoppingCartDTO.getDishFlavor())
                        .setmealId(shoppingCartDTO.getSetmealId())
                        .number(1)
                        .amount(setmeal.getPrice())
                        .image(setmeal.getImage())
                        .createTime(LocalDateTime.now())
                        .build();
            }
            shoppingCartMapper.insert(cart);
        }
    }
}
