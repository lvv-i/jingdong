package com.example.shop.security;

import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具（T4 方案：24 小时有效期）
 * claims 约定：sub=用户ID、role=USER|MERCHANT|ADMIN、shopId=店铺ID（仅商家）、iat、exp
 * 密钥：application.yml 配置（可用环境变量 JWT_SECRET 覆盖），长度须 ≥32 字节（HS256）
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-hours:24}")
    private long expireHours;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发 token（sub/role/shopId/iat/exp） */
    public String generateToken(LoginUser user) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("role", user.getRole())
                .claim("shopId", user.getShopId())
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token 为当前登录人
     * token 无效/过期抛 1002 未登录（T4：过期返回 1002）
     */
    public LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Long id = Long.valueOf(claims.getSubject());
            String role = claims.get("role", String.class);
            Long shopId = claims.get("shopId", Long.class);
            return new LoginUser(id, null, role, shopId);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "登录已过期，请重新登录");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "登录凭证无效");
        }
    }
}
