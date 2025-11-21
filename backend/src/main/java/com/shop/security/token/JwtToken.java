package com.shop.security.token;

import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * JWT Token类 - 用于Shiro认证
 */
public class JwtToken implements AuthenticationToken {

    private final String token;
    private final Claims claims;

    public JwtToken(String token, Claims claims) {
        this.token = token;
        this.claims = claims;
    }

    @Override
    public Object getPrincipal() {
        return claims != null ? claims.getSubject() : null;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    public String getToken() {
        return token;
    }

    public Claims getClaims() {
        return claims;
    }
}