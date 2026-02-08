package com.sky.service.impl;

import com.fasterxml.jackson.databind.ser.Serializers;
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
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 减少购物车中一个商品数量
     * @param shoppingCartDTO
     */
    @Override
    public void subItem(ShoppingCartDTO shoppingCartDTO) {
        // 封装一个查询购物车对象
        ShoppingCart queryCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, queryCart);
        queryCart.setUserId(BaseContext.getCurrentId());

        // 查询
        ShoppingCart cart = shoppingCartMapper.getCartItem(queryCart);

        // 未查询到
        if (cart == null) {
            return;
        }

        if (cart.getNumber() == 1){ //查询到一个，直接删除
            shoppingCartMapper.delete(cart);
        } else { //查询到多个，update
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.updateById(cart);
        }
    }

    /**
     * 添加购物车商品
     * @param shoppingCartDTO
     */
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

    /**
     * 查看购物车商品
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartMapper.getList(userId);
        return list;
    }

    /**
     * 清空当前用户的购物车
     */
    @Override
    public void clearShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clearCart(userId);
    }
}
