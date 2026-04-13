package com.sky.controller.user;

import com.sky.dto.CouponClaimDTO;
import com.sky.entity.Coupon;
import com.sky.result.Result;
import com.sky.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userCouponController")
@RequestMapping("/user/coupon")
@Api(tags = "用户优惠券相关接口")
public class CouponController {
    @Autowired
    private CouponService couponService;

    @PostMapping("/claim")
    @ApiOperation("用户抢券")
    public Result<Long> claim(@RequestBody CouponClaimDTO couponClaimDTO) {
        return Result.success(couponService.claim(couponClaimDTO));
    }

    @GetMapping("/list")
    @ApiOperation("我的优惠券列表")
    public Result<Object> list() {
        return Result.success(couponService.listMine());
    }

    @GetMapping("/available")
    @ApiOperation("可领取优惠券列表")
    public Result<List<Coupon>> available() {
        return Result.success(couponService.listAvailable());
    }
}
