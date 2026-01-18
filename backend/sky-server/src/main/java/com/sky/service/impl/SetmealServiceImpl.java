package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    /**
     * 新增套餐
     */
    @Override
    public void saveSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 保存新套餐
        setmealMapper.saveSetmeal(setmeal);
        // 获得插入后的id
        Long id = setmeal.getId();
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        if (setmealDishList!=null && !setmealDishList.isEmpty()){
            setmealDishList.forEach(setmealDish -> {
                setmealDish.setSetmealId(id);
            });
        } else {
            return;
        }
        // 批量保存新套餐中包含的菜品
        dishMapper.saveSetmealDishes(setmealDishList);
    }

    /**
     * 套餐分页查询
     */
    @Override
    public PageResult getPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 分页参数
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> setmealPage = setmealMapper.getPage(setmealPageQueryDTO);
        return new PageResult(setmealPage.getTotal(), setmealPage.getResult());
    }
}
