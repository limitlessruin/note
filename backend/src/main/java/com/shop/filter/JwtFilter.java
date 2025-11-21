package com.shop.filter;

import com.shop.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT过滤器 - 用于Shiro框架的JWT认证
 */
public class JwtFilter extends AccessControlFilter {

    /**
     * 判断是否允许访问
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        // 如果是OPTIONS请求，直接放行（CORS预检请求）
        if (WebUtils.toHttp(request).getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        
        // 尝试进行JWT认证
        return executeLogin(request, response);
    }

    /**
     * 当访问被拒绝时调用
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        sendError(httpResponse, "请先登录", HttpStatus.UNAUTHORIZED.value());
        return false;
    }

    /**
     * 执行登录认证
     */
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String token = getTokenFromRequest(httpRequest);
        
        if (token == null) {
            return false;
        }

        // 验证JWT令牌
        Claims claims = JwtUtil.parse(token);
        if (claims == null) {
            return false;
        }

        // 创建JWT Token并提交给Shiro进行认证
        com.shop.security.token.JwtToken jwtToken = new com.shop.security.token.JwtToken(token, claims);
        getSubject(request, response).login(jwtToken);
        return true;
    }

    /**
     * 从请求头中获取JWT令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // 去掉"Bearer "前缀
        }
        return null;
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
        response.getWriter().flush();
    }
}