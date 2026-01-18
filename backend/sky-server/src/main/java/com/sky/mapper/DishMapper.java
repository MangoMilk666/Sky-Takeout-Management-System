package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品数据
     */
    @AutoFill(value = OperationType.INSERT)
    void save(Dish dish);

    /**
     * 菜品分页查询
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     */
    void deleteById(Long id);

    /**
     * 根据id查询菜品+分类信息
     * @param id
     * @return
     */
    @Select("select d.*, c.name as categoryName from dish d left join category c on d.category_id = c.id where d.id = #{id}")
    DishVO getById(Long id);

    /**
     * 修改菜品信息
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);
}
