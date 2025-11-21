package com.shop.config;

import com.shop.security.realm.UserRealm;
import com.shop.security.realm.JwtRealm;
import com.shop.filter.JwtFilter;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {
    // 1. 配置 Realm（核心安全数据源）
    
    public UserRealm userRealm(HashedCredentialsMatcher matcher) { // 注入Matcher
        UserRealm realm = new UserRealm();
        realm.setCredentialsMatcher(matcher);
        return realm;
    }

    // 2. 密码匹配器（加密规则）
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
        matcher.setHashAlgorithmName("SHA-256");
        matcher.setHashIterations(1000);
        matcher.setStoredCredentialsHexEncoded(true);
        return matcher;
    }

    // 3. SecurityManager（Shiro 核心）

    @Bean
    public DefaultWebSecurityManager securityManager(UserRealm userRealm, JwtRealm jwtRealm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        // 设置多个Realm，JwtRealm优先
        manager.setRealms(java.util.Arrays.asList(jwtRealm, userRealm));
        return manager;
    }

    // 4. Shiro 过滤器（Web 应用必须）
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean filterFactory = new ShiroFilterFactoryBean();
        filterFactory.setSecurityManager(securityManager);

        // 配置自定义过滤器
        Map<String, Filter> filters = new LinkedHashMap<>();
        filters.put("jwt", new JwtFilter());
        filterFactory.setFilters(filters);

        // 配置拦截规则
        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/api/captcha/**", "anon");  // 验证码接口匿名访问
        filterMap.put("/api/users/login", "anon"); // 登录接口匿名访问
        filterMap.put("/api/users/register", "anon");       // 用户注册接口匿名访问
        filterMap.put("/api/users", "anon"); // 用户接口基础路径匿名访问
        filterMap.put("/api/vision/**", "anon");   // 视觉AI接口匿名访问
        filterMap.put("/api/**", "jwt");           // 其他API接口使用JWT认证
        filterMap.put("/**", "authc");             // 其他请求需要认证

        filterFactory.setFilterChainDefinitionMap(filterMap);
        return filterFactory;
    }
}
