package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入菜品口味数据
     */

    public void insertBatch(List<DishFlavor> flavorList);

    /**
     * 删除菜品关联的口味
     */
    void deleteByDishId(Long dishId);
}
