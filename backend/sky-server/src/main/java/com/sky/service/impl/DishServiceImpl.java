package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增菜品
     */
    @Override
    public void saveWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 菜品表插入1条数据
        dishMapper.save(dish);
        // 插入后获得新增的菜品id
        Long dishId = dish.getId();

        // （批量）插入口味表dish_flavor数据
        List<DishFlavor> flavorList = dishDTO.getFlavors();
        // 补充菜品id
        if (flavorList!=null && !flavorList.isEmpty()){
            flavorList.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
        }
        dishFlavorMapper.insertBatch(flavorList);
    }

    /**
     * 菜品分页查询
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 分页参数
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        // 分页查询，返回Page<Dish>对象
        Page<DishVO> dishPage = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(dishPage.getTotal(), dishPage.getResult());
    }

    /**
     * 批量删除菜品
     */
    @Override
    public void deleteDishWithFlavors(List<Long> ids) {
        // 删除菜品
        // 起售中/关联套餐菜品不允许删除
        ids.forEach(id -> {
            DishVO dish = dishMapper.getById(id);
            if (dish!=null && dish.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

            // 判断菜品是否与至少1个套餐关联
            List<Long> setmealIds = setmealMapper.getIdsByDishId(id);
            if (setmealIds!=null && !setmealIds.isEmpty()){
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        });

        // 逐个删除菜品及其关联口味
        ids.forEach(id -> {
            dishMapper.deleteById(id);
            dishFlavorMapper.deleteByDishId(id);
        });

    }

    /**
     * 根据ID查询菜品(及口味数据)
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 查询菜品基本信息+分类信息
        DishVO dishVO = dishMapper.getById(id);
        // 查询口味信息
        List<DishFlavor> dishFlavorList = dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(dishFlavorList);
        return dishVO;
    }

    /**
     * 修改菜品信息
     */
    @Override
    public void updateDishWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 修改菜品基本信息
        dishMapper.update(dish);
        // 修改菜品口味 - 先删，再插入
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavorList = dishDTO.getFlavors();
        // 如果更新只删除
        if (flavorList!=null && !flavorList.isEmpty()){
            flavorList.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
        } else {
            return;
        }
        dishFlavorMapper.insertBatch(flavorList);
    }
}
