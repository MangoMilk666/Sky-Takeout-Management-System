package com.sky.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCouponVO implements Serializable {
    private Long userCouponId;
    private Long couponId;
    private String name;
    private Integer discountType;
    private BigDecimal discount;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer status;
}

