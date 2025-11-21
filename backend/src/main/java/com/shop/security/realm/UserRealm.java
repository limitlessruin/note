package com.shop.security.realm;

import com.shop.entity.User;
import com.shop.entity.LoginRequest;
import com.shop.mapper.UserMapper;

import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shop.entity.LoginRequest;

@Service
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private UserMapper userMapper; // 注入 MyBatis Mapper

    // 设置密码匹配器
    public UserRealm() {
        setCredentialsMatcher(new HashedCredentialsMatcher());
    }

    // 授权逻辑（权限相关）
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 添加角色和权限（根据业务需要）
        info.addRole("user");
        return info;
    }

    // 认证逻辑（登录验证）
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();

        // 从数据库查询用户（通过 MyBatis）
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }

        // 返回认证信息（Shiro 会自动进行密码匹配）
        return new SimpleAuthenticationInfo(
                user.getUsername(),      // 主体标识
                user.getPassword(),      // 数据库中的加密密码
                ByteSource.Util.bytes(user.getSalt()), // 盐值
                getName()                // Realm 名称
        );
    }
}