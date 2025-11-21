package com.shop.controller;

import com.shop.entity.User;
import com.shop.service.UserService;
import com.shop.util.JwtUtil;
import com.shop.service.CaptchaService;
import com.shop.entity.LoginRequest;
import com.shop.entity.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CaptchaService captchaService;

    // 获取所有用户
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 创建新用户
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            // 简单的验证：只需要用户名和密码
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("用户名不能为空");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("密码不能为空");
            }

            if (userService.addUser(user)) {
                return ResponseEntity.ok("用户创建成功");
            } else {
                return ResponseEntity.badRequest().body("用户名已存在");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("服务器错误: " + e.getMessage());
        }
    }

    // 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            if (userService.updateUser(user)) {
                return ResponseEntity.ok("用户更新成功");
            } else {
                return ResponseEntity.badRequest().body("用户更新失败");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("服务器错误: " + e.getMessage());
        }
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            if (userService.deleteUser(id)) {
                return ResponseEntity.ok("用户删除成功");
            } else {
                return ResponseEntity.badRequest().body("用户删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("服务器错误: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 验证验证码
            if (!captchaService.validateCaptcha(loginRequest.getSessionId(), loginRequest.getCaptcha())) {
                Map<String, Object> response = new HashMap<>();
                response.put("登录状态：", false);
                response.put("message", "验证码错误");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

            // 生成JWT令牌等
            Map<String, Object> response = new HashMap<>();
            String token = JwtUtil.generate(loginRequest.getUsername());
            response.put("token", token);
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("登录状态", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest registerRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {   
            // 验证输入数据
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户名不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (registerRequest.getUsername().length() < 3) {
                response.put("success", false);
                response.put("message", "用户名长度不能少于3个字符");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "邮箱不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!registerRequest.getEmail().contains("@") || !registerRequest.getEmail().contains(".")) {
                response.put("success", false);
                response.put("message", "邮箱格式不正确");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (registerRequest.getPassword().length() < 6) {
                response.put("success", false);
                response.put("message", "密码长度不能少于6个字符");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建用户对象
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());
            
            // 调用服务层进行注册
            boolean success = userService.addUser(user);
            
            if (success) {
                response.put("success", true);
                response.put("message", "注册成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户名已存在");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "注册失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}