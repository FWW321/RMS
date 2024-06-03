package com.fww.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.sql.Date;
import java.util.Map;

public class JwtFun {
    private static final String key = "654321";

    public static String getToken(Map<String, Object> user) {
        return JWT.create()
                .withHeader(Map.of("alg", "HS256", "typ", "JWT"))
                .withClaim("sub", user)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .sign(Algorithm.HMAC256(key));
    }

    public static Map<String, Object> verifyToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(key)).build().verify(token).getClaim("sub").asMap();
        } catch (JWTVerificationException e) {
            // 处理令牌验证异常
            throw new RuntimeException("令牌验证失败", e);
        }
    }
}

