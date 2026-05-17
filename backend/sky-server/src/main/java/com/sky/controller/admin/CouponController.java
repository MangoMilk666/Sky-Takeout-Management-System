package com.sky.controller.admin;

import com.sky.dto.CouponPublishDTO;
import com.sky.mapper.CouponMapper;
import com.sky.result.Result;
import com.sky.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminCouponController")
@RequestMapping("/admin/coupon")
@Api(tags = "优惠券相关接口")
public class CouponController {
    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponMapper couponMapper;

    @PostMapping("/publish")
    @ApiOperation("发布优惠券并Redis预热")
    public Result<Long> publish(@RequestBody CouponPublishDTO couponPublishDTO) {
        return Result.success(couponService.publish(couponPublishDTO));
    }

    @GetMapping("/list")
    @ApiOperation("查看优惠券列表")
    public Result<Object> list() {
        return Result.success(couponMapper.listAll());
    }
}
