package com.shop.mapper;

import com.shop.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    // 只有方法声明，没有注解
    List<User> findAll();

    User findById(Long id);

    User findByUsername(String username);

    int insert(User user);

    int update(User user);

    int deleteById(Long id);
}