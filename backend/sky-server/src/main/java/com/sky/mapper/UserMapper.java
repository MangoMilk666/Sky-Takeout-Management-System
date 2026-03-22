package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    /**
     * 根据openid判断是否是新用户
     * @param openid
     * @return
     */
    @Select("select id, openid, name, phone, sex, id_number, avatar, create_time from user where openid = #{openid}")
    User getByOpenId(String openid);

    /**
     * 保存（新）用户信息（完成注册）
     * @param user
     */
    void saveUser(User user);


    /**
     * 根据userId查找User
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 查询时间之前注册的总用户数
     */
    @Select("select count(id) from user where create_time<=#{finalTime}")
    int getUserNum(LocalDateTime finalTime);
}
