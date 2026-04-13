package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CouponClaimDTO implements Serializable {
    private Long couponId;
    private String requestId;
}

