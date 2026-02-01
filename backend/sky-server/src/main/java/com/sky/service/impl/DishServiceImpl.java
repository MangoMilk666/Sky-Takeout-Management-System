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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteDishWithFlavors(List<Long> ids) {
        // 删除菜品
        // 起售中/关联套餐菜品不允许删除
        ids.forEach(id -> {
            Dish dish = dishMapper.getById(id);
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
        Dish dish = dishMapper.getById(id);
        // 查询口味信息
        List<DishFlavor> dishFlavorList = dishFlavorMapper.getByDishId(id);
        // 封装成为DishVO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavorList);
        return dishVO;
    }

    /**
     * 修改菜品信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 菜品起售/停售
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer status, Long id) {
        Dish dish = dishMapper.getById(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询（起售状态）菜品
     */
    @Override
    public List<Dish> getListByCategoryId(Long categoryId) {
        //List<Dish> dishList = dishMapper.getListByCategoryId(categoryId);

        // 根据1个Dish对象数据而不是单单categoryId来查询菜品列表
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        // 动态查询，这样可以确保只返回正处于起售的菜品
        List<Dish> dishList = dishMapper.getListByCategoryId(dish);
        return dishList;
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.getListByCategoryId(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
