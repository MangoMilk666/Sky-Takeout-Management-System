package com.sky.service;

import com.sky.dto.CouponClaimDTO;
import com.sky.dto.CouponPublishDTO;
import com.sky.entity.Coupon;
import com.sky.vo.UserCouponVO;

import java.util.List;

public interface CouponService {
    Long publish(CouponPublishDTO couponPublishDTO);

    Long claim(CouponClaimDTO couponClaimDTO);

    List<UserCouponVO> listMine();

    List<Coupon> listAvailable();
}
