package com.shop.security.realm;

import com.shop.security.token.JwtToken;
import com.shop.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

/**
 * JWT Realm - 用于JWT令牌认证
 */
@Component
public class JwtRealm extends AuthorizingRealm {

    /**
     * 指定该Realm只支持JwtToken类型的认证
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 授权逻辑
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        
        // 根据用户名获取角色和权限（这里简单返回user角色）
        authorizationInfo.addRole("user");
        return authorizationInfo;
    }

    /**
     * 认证逻辑
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) token;
        String tokenString = (String) jwtToken.getCredentials();
        
        // 验证JWT令牌
        Claims claims = JwtUtil.parse(tokenString);
        if (claims == null) {
            throw new AuthenticationException("JWT令牌无效或已过期");
        }
        
        String username = claims.getSubject();
        if (username == null) {
            throw new AuthenticationException("JWT令牌中缺少用户名");
        }
        
        // 返回认证信息
        return new SimpleAuthenticationInfo(username, tokenString, getName());
    }
}