package com.emis.security.common.jwt;

import com.alibaba.fastjson.JSON;
import com.emis.security.model.UserModel;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @Description: JWT工具类
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Slf4j
public class JWTToken {

    /**
     * 生成token
     *
     * @param userModel 自定义的用户对象
     * @return String
     */
    public static String createAccessToken(UserModel userModel) {
        // 登陆成功生成JWT
        String token = Jwts.builder()
                // 放入用户名和用户ID
                .setId(userModel.getUserId() + "")
                // 主题
                .setSubject(userModel.getAccount())
                // 签发时间
                .setIssuedAt(new Date())
                // 签发者
                .setIssuer("LiuRunYong")
                // 自定义属性 放入用户拥有权限
                .claim("authorities", JSON.toJSONString(userModel.getAuthorities()))
                // 失效时间(一天)
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 3600000))
                // 签名算法和密钥
                .signWith(SignatureAlgorithm.HS512, "JWTSecret")
                .compact();
        return token;
    }
}
