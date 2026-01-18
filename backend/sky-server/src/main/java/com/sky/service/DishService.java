package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品
     */
    void saveWithFlavors(DishDTO dishDTO);

    /**
     * 菜品分页查询
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     */
    void deleteDishWithFlavors(List<Long> ids);

    /**
     * 根据ID查询菜品(及口味数据)
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 修改菜品信息
     */
    void updateDishWithFlavors(DishDTO dishDTO);
}
