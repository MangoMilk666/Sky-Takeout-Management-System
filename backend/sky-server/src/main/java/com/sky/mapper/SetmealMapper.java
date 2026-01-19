package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 根据菜品id查询菜品关联的套餐，返回id列表
     * @param id
     */
    @Select("select sd.setmeal_id from setmeal_dish sd where sd.dish_id = #{id}")
    List<Long> getIdsByDishId(Long id);

    /**
     * 新增套餐
     */
    @AutoFill(value = OperationType.INSERT)
    void saveSetmeal(Setmeal setmeal);

    /**
     * 套餐分页查询
     */
    Page<SetmealVO> getPage(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id查询套餐
     * @param setmealId
     * @return
     */
    @Select("select id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user from setmeal where id = #{setmealId}")
    Setmeal getById(Long setmealId);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改套餐信息
     */
    @AutoFill(value = OperationType.UPDATE)
    void updateSetmeal(Setmeal setmeal);
}
