package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    // 微信服务接口
    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {
        // httpClient调用微信接口服务，获得当前用户的openid
        // 参数map - appid, secret, js_code, grant_type
        String code = userLoginDTO.getCode();
        String openid = getOpenid(code);

        // 判断openid是否为空，if null则登录失败，抛出异常
        if (openid == null || openid.isEmpty()) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 查询db，判断当前用户是否是（外卖系统）的新用户
        User user = userMapper.getByOpenId(openid);
        // 若是新用户，自动完成注册
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.saveUser(user);
        }
        return user;
    }

    /**
     httpClient调用微信接口服务，
     获得当前用户的openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        // 参数map - appid, secret, js_code, grant_type
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", code);
        paramMap.put("grant_type", "authorization_code");
        String json =  HttpClientUtil.doGet(WX_LOGIN, paramMap);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }


}
