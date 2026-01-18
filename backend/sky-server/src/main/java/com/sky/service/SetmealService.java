package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

public interface SetmealService {

    /**
     * 新增套餐
     */
    void saveSetmeal(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     */
    PageResult getPage(SetmealPageQueryDTO setmealPageQueryDTO);
}
