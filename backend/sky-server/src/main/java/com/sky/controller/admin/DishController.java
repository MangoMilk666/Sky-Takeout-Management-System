package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     */

    @PostMapping
    public Result<String> addDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品: {}", dishDTO);
        dishService.saveWithFlavors(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    public Result<PageResult> getPage(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询: {}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     */
    @DeleteMapping
    public Result<String> deleteBatch(@RequestParam List<Long> ids) {
        log.info("待删除菜品id: {}", ids);
        dishService.deleteDishWithFlavors(ids);
        return Result.success();
    }

    /**
     * 根据ID查询菜品(和关联的口味数据)
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品和关联的口味数据")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据ID查询菜品和口味: {}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        log.info("菜品待修改为:{}", dishDTO);
        dishService.updateDishWithFlavors(dishDTO);
        return Result.success();
    }

    /**
     * 菜品起售/停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售/停售菜品")
    public Result<String> updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("将id为{}的菜品起售/停售,状态改为{}", id,  status);
        dishService.updateStatus(status, id);
        return Result.success();
    }

}
