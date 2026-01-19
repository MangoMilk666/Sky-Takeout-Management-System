package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 批量新增套餐中的菜品
     */
    //@AutoFill(value = OperationType.INSERT)
    void saveSetmealDishes(List<SetmealDish> setmealDishes);

    /**
     * 删除指定id套餐包含的菜品
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteSetMealDishes(Long setmealId);

    /**
     * 根据套餐id查询包含的菜品
     */
    @Select("select id, setmeal_id, dish_id, name, price, copies from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getDishesBySetmealId(Long id);
}
