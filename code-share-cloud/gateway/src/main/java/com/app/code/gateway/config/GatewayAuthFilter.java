package com.app.code.gateway.config;

import com.app.code.model.po.User;
import com.app.code.util.UserUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lfj
 * @createDate 2025/4/22
 */
@Component
@Slf4j
@Data
@ConfigurationProperties(prefix = "spring.cloud.gateway")
public class GatewayAuthFilter implements GlobalFilter, Ordered {


    private List<String> whitelist = new ArrayList<>();

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求路径
        String path = exchange.getRequest().getURI().getPath();

        // 检查是否在白名单中
        boolean isWhitelisted = whitelist.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));

        if (isWhitelisted) {
            return chain.filter(exchange); // 直接放行
        }


        String token = extractToken(exchange.getRequest());
        try {
            // 验证并解析 JWT
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey("codejwtsecretkeymustbeatleast32byteslongdsadasdashfhakjshfkashkjdha") // 确保密钥格式正确
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            // 传递用户信息到下游服务
//            String subject = claims.getSubject();
//            User user = UserUtil.getUser(subject);
            addHeaders(exchange, claims);
        } catch (Exception ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            //return exchange.getResponse().setComplete();
            throw new RuntimeException("Invalid token");
        }

        return chain.filter(exchange);
    }

    private String extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst("Authorization");
        return header != null ? header.replace("Bearer ", "") : null;
    }

    private void addHeaders(ServerWebExchange exchange, Claims claims) {
        exchange.getRequest().mutate()
                .header("X-User-Info", claims.getSubject())
                .build();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

