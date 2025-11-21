package com.shop.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * 注册界面 - JFrame实现
 * 连接到现有的Spring Boot后端系统
 */
public class RegisterFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JButton registerButton;
    private JButton backToLoginButton;
    
    private LoginFrame parentFrame;
    
    public RegisterFrame(LoginFrame parentFrame) {
        this.parentFrame = parentFrame;
        initialize();
    }
    
    private void initialize() {
        // 设置窗口基本属性
        setTitle("智慧花店 - 注册");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parentFrame); // 相对于登录窗口居中
        setResizable(false);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 添加标题
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // 创建表单面板
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 添加到窗口
        add(mainPanel);
        
        // 添加窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 关闭注册窗口时，重新显示登录窗口
                if (parentFrame != null) {
                    parentFrame.setVisible(true);
                }
            }
        });
    }
    
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("创建新账号");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(64, 158, 255));
        
        JLabel subtitleLabel = new JLabel("请填写以下信息完成注册");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(titleLabel, BorderLayout.CENTER);
        labelPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        titlePanel.add(labelPanel);
        return titlePanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(usernameField, gbc);
        
        // 邮箱
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel emailLabel = new JLabel("邮箱:");
        emailLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailField = new JTextField(20);
        emailField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(emailField, gbc);
        
        // 密码
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(passwordField, gbc);
        
        // 确认密码
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(confirmPasswordField, gbc);
        
        // 注册提示
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.WEST;
        JLabel tipLabel = new JLabel("<html><font color='gray' size=2>提示：密码至少6位，用户名至少3位</font></html>");
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(tipLabel, gbc);
        
        return formPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        registerButton.setPreferredSize(new Dimension(150, 40));
        registerButton.setBackground(new Color(64, 158, 255));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.setOpaque(true);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> handleRegister());
        
        backToLoginButton = new JButton("返回登录");
        backToLoginButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        backToLoginButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        backToLoginButton.setBackground(Color.WHITE);
        backToLoginButton.setForeground(new Color(64, 158, 255));
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.setBorder(BorderFactory.createLineBorder(new Color(64, 158, 255)));
        backToLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLoginButton.addActionListener(e -> {
            this.dispose(); // 关闭注册窗口
            if (parentFrame != null) {
                parentFrame.setVisible(true); // 显示登录窗口
            }
        });
        
        buttonPanel.add(registerButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(backToLoginButton);
        
        return buttonPanel;
    }
    

    
    // 处理注册
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        
        // 表单验证
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this, "用户名长度不能少于3个字符", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入邮箱", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 简单的邮箱格式验证
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "请输入有效的邮箱地址", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入密码", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "密码长度不能少于6个字符", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        

        
        // 禁用注册按钮，防止重复提交
        registerButton.setEnabled(false);
        registerButton.setText("注册中...");
        
        // 在新线程中执行注册请求
        new Thread(() -> {
            try {
                // 创建请求体
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", username);
                requestBody.put("email", email);
                requestBody.put("password", password);

                
                // 发送注册请求
                String response = sendPostRequest("http://localhost:8080/api/users/register", 
                                                 requestBody.toString());
                JSONObject jsonResponse = new JSONObject(response);
                
                // 处理响应
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                            // 注册成功
                            JOptionPane.showMessageDialog(this, "注册成功！请使用新账号登录", 
                                                          "提示", JOptionPane.INFORMATION_MESSAGE);
                            
                            // 关闭注册窗口，显示登录窗口
                            this.dispose();
                            if (parentFrame != null) {
                                parentFrame.setVisible(true);
                                // 可以预先填充用户名
                                parentFrame.setUsername(username);
                            }
                        } else {
                            // 注册失败
                            String message = "注册失败";
                            if (jsonResponse.has("message")) {
                                message = jsonResponse.getString("message");
                            }
                            
                            JOptionPane.showMessageDialog(this, message, 
                                                          "注册失败", JOptionPane.ERROR_MESSAGE);

                        }
                    } catch (JSONException ex) {
                        JOptionPane.showMessageDialog(this, "响应解析错误: " + ex.getMessage(), 
                                                      "错误", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        // 恢复注册按钮
                        registerButton.setEnabled(true);
                        registerButton.setText("注册");
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "注册请求失败: " + ex.getMessage(), 
                                                  "错误", JOptionPane.ERROR_MESSAGE);

                    registerButton.setEnabled(true);
                    registerButton.setText("注册");
                });
            }
        }).start();
    }
    
    // 发送GET请求
    private String sendGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            return response.toString();
        } else {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }
    
    // 发送POST请求
    private String sendPostRequest(String urlString, String body) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        
        // 发送请求体
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            return response.toString();
        } else {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }
    

}