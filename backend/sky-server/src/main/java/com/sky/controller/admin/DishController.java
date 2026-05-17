package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate; //操作redis的对象

    /**
     * 新增菜品
     */
    @PostMapping
    public Result<String> addDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品: {}", dishDTO);
        dishService.saveWithFlavors(dishDTO);

        // 精确清理redis缓存的（旧）数据
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
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
        // 清除所有的菜品缓存数据，即所有以dish_开头的keys
        cleanCache("dish_*");
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
        // 因为修改的数据可能涉及多类数据（菜品/菜品分类）
        // update的开销远大于delete
        // 所以也清除所有的菜品缓存数据，即所有以dish_开头的keys
        cleanCache("dish_*");
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
        // 清除所有的菜品缓存数据，即所有以dish_开头的keys
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getListByCategoryId(@RequestParam Long categoryId) {
        log.info("根据分类id查询菜品列表: {}", categoryId);
        List<Dish> dishList = dishService.getListByCategoryId(categoryId);
        return Result.success(dishList);
    }

    /**
     * 当前类清理缓存的方法，可用SpringCache注解代替
     */
    private void cleanCache(String pattern){
        // 匹配redis待删除的keys
        Set keys =  redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}
