package com.shop.service;

import com.shop.entity.User;
import java.util.List;

// 将 class 改为 interface
public interface UserService {
    List<User> getAllUsers();

    User getUserById(Long id);

    User getUserByUsername(String username);

    boolean addUser(User user);

    boolean updateUser(User user);

    boolean deleteUser(Long id);

    User authenticate(String username, String password);
}