package com.sky.mapper;

import com.sky.entity.UserCoupon;
import com.sky.vo.UserCouponVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserCouponMapper {
    void insert(UserCoupon userCoupon);

    UserCoupon getById(Long id);

    Long getIdByRequestId(String requestId);

    List<UserCouponVO> listByUserId(Long userId);

    List<UserCouponVO> listAvailableByUserId(Long userId);

    Integer markUsed(Long id, Long userId, LocalDateTime usedTime);

    Integer revertToUnused(Long id);
}
