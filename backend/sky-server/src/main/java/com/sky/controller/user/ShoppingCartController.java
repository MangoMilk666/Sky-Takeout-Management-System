package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation("删除购物车中一个商品")
    public Result subItem(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车减少商品数量， 待删除商品: {}", shoppingCartDTO);
        shoppingCartService.subItem(shoppingCartDTO);
        return Result.success();
    }

    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result addItem(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车添加商品: {}", shoppingCartDTO);
        shoppingCartService.addItem(shoppingCartDTO);
        return Result.success();
    }
}
