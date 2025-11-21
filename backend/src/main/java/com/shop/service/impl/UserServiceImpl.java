package com.shop.service.impl;

import com.shop.service.UserService;
import com.shop.util.PasswordHelper;
import org.springframework.stereotype.Service;
import com.shop.entity.User;          // User实体类
import com.shop.mapper.UserMapper;    // UserMapper接口
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;                // List接口

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordHelper passwordHelper;

    @Override
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public boolean addUser(User user) {
        // 检查用户名是否已存在
        User existing = userMapper.findByUsername(user.getUsername());
        if (existing != null) {
            return false;
        }
        // 生成盐值并加密密码
        String salt = passwordHelper.generateSalt();
        String encryptedPwd = passwordHelper.encryptPassword(user.getPassword(), salt);

        user.setPassword(encryptedPwd);
        user.setSalt(salt);
        user.setCreatedTime(LocalDateTime.now());

        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean updateUser(User user) {
        return userMapper.update(user) > 0;
    }

    @Override
    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public User authenticate(String username, String password) {
        User user = userMapper.findByUsername(username);

        if (user == null){
            throw new RuntimeException("用户不存在");
        }

        if(!passwordHelper.validatePassword(password, user.getPassword(), user.getSalt())){
            throw new RuntimeException("密码错误");
        }

        return user;
    }
}
