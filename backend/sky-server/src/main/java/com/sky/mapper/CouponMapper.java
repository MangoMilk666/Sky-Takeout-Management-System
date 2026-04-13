package com.sky.mapper;

import com.sky.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CouponMapper {
    void insert(Coupon coupon);

    Coupon getById(Long id);

    List<Coupon> listAll();

    List<Coupon> listAvailable();

    Integer decreaseRemainedCount(Long id);
}
