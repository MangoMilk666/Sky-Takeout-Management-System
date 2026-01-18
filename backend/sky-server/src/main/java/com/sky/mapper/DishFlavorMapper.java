package com.sky.mapper;

import com.sky.dto.DishDTO;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 根据菜品id查询口味列表
     */
    @Select("select id, dish_id, name, value from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getByDishId(Long id);


}
