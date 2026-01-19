package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/setmeal")
public class SetmealController {
    // 套餐管理相关接口
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result<String> saveSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐:{}", setmealDTO);
        setmealService.saveSetmeal(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐分页查询
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> getPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询: {}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.getPage(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除套餐
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result<String> deleteBatch(@RequestParam List<Long> ids) {
        log.info("待删除套餐id: {}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改套餐信息
     */
    @PutMapping
    @ApiOperation("修改套餐信息")
    public Result<String> updateSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("套餐待修改为: {}", setmealDTO);
        setmealService.updateSetmeal(setmealDTO);
        return Result.success();
    }

    /**
     * 根据id查询套餐
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getSetmealById(@PathVariable Long id) {
        log.info("根据id{}查询套餐信息", id);
        SetmealVO setmealVO = setmealService.getSetmealById(id);
        return Result.success(setmealVO);
    }

    /**
     * 起售/停售套餐
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售/停售套餐")
    public Result<String> updateSetmealStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("将id为{}的套餐起售/停售，状态改为{}", id, status);
        setmealService.updateStatus(status, id);
        return Result.success();
    }



}
