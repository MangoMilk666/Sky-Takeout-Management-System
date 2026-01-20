package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        setmealDishMapper.saveSetmealDishes(setmealDishList);
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

    /**
     * 批量删除套餐
     * 不允许删除起售套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        // 检查是否处于起售状态
        if (ids==null || ids.isEmpty()){
            return;
        }
        ids.forEach(setmealId -> {
            Setmeal setmeal = setmealMapper.getById(setmealId);
            if (Objects.equals(setmeal.getStatus(), StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        // 检查无误，批量删除
        setmealMapper.deleteBatch(ids);
    }

    /**
     * 修改套餐信息
     * 处于起售状态的套餐不允许修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = setmealMapper.getById(setmealDTO.getId());
        if (Objects.equals(setmeal.getStatus(), StatusConstant.ENABLE)){
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 修改套餐基本信息
        setmealMapper.updateSetmeal(setmeal);

        // 修改套餐包含菜品
        // 先删除
        setmealDishMapper.deleteSetMealDishes(setmealDTO.getId());
        // 再插入
        // setmealDTO传递的setmealDishes列表需要重新赋值套餐id值
        List<SetmealDish> newDishList = setmealDTO.getSetmealDishes();
        if (newDishList!=null && !newDishList.isEmpty()){
            newDishList.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
        } else {
            return;
        }

        setmealDishMapper.saveSetmealDishes(newDishList);
    }

    /**
     * 根据id查询套餐
     */
    @Override
    public SetmealVO getSetmealById(Long id) {
        // 查询基本信息
        Setmeal setmeal = setmealMapper.getById(id);
        // 查询包含的菜品
        List<SetmealDish> setmealDishList = setmealDishMapper.getDishesBySetmealId(id);

        // 封装成SetmealVO
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishList);
        return setmealVO;
    }

    /**
     * 起售/停售套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer status, Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        setmeal.setStatus(status);
        setmealMapper.updateSetmeal(setmeal);
    }
}
