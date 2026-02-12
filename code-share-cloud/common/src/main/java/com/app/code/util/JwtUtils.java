package com.app.code.util;

import com.alibaba.fastjson.JSON;
import com.app.code.model.po.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.Duration;
import java.util.Date;
import java.util.List;

public class JwtUtils {
    private static final String SECRET_KEY = "codejwtsecretkeymustbeatleast32byteslongdsadasdashfhakjshfkashkjdha";
    private static final Duration EXPIRATION = Duration.ofHours(2);

    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(JSON.toJSONString(user))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION.toMillis()))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}