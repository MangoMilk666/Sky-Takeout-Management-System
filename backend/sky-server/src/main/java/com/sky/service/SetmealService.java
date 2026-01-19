package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /**
     * 新增套餐
     */
    void saveSetmeal(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     */
    PageResult getPage(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除套餐
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改套餐信息
     */
    void updateSetmeal(SetmealDTO setmealDTO);

    /**
     * 根据id查询套餐
     */
    SetmealVO getSetmealById(Long id);

    /**
     * 起售/停售套餐
     */
    void updateStatus(Integer status, Long id);
}
