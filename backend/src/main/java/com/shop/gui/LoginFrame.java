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
 * 登录界面 - JFrame实现
 * 连接到现有的Spring Boot后端系统
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField captchaField;
    private JLabel captchaImageLabel;
    private JCheckBox rememberMeCheckBox;
    private JButton loginButton;
    private JButton refreshCaptchaButton;
    private JButton registerButton;
    
    private String sessionId = "";
    
    public LoginFrame() {
        initialize();
    }
    
    private void initialize() {
        // 设置窗口基本属性
        setTitle("智慧花店 - 登录");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
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
        
        // 初始化验证码
        refreshCaptcha();
        
        // 检查记住的用户名
        checkRememberedUsername();
    }
    
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("智慧花店");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(64, 158, 255));
        
        JLabel subtitleLabel = new JLabel("欢迎回来，请登录您的账号");
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
        
        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(passwordField, gbc);
        
        // 验证码
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel captchaLabel = new JLabel("验证码:");
        captchaLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(captchaLabel, gbc);
        
        // // 验证码输入和刷新按钮
        // gbc.gridx = 1;
        // gbc.weightx = 1.0;
        // JPanel captchaPanel = new JPanel(new BorderLayout());
        // captchaPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        // captchaField = new JTextField(10);
        // captchaField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        // captchaPanel.add(captchaField, BorderLayout.WEST);
        
        // 验证码输入框和图像在同一个面板中
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel captchaPanel = new JPanel(new BorderLayout());
        captchaPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    
        // 输入框和验证码图片的水平面板
        JPanel captchaInputPanel = new JPanel(new BorderLayout());
        captchaInputPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    
        captchaField = new JTextField(10);
        captchaField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        captchaInputPanel.add(captchaField, BorderLayout.CENTER);
    
        // 验证码图像设置固定大小并添加到输入框右侧
        captchaImageLabel = new JLabel();
        captchaImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        captchaImageLabel.setPreferredSize(new Dimension(100, 30)); // 设置固定大小
        captchaInputPanel.add(captchaImageLabel, BorderLayout.EAST);
    
        captchaPanel.add(captchaInputPanel, BorderLayout.CENTER);

        // 验证码图像设置固定大小并添加到输入框右侧
        captchaImageLabel = new JLabel();
        captchaImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        captchaImageLabel.setPreferredSize(new Dimension(100, 30)); // 设置固定大小
        captchaInputPanel.add(captchaImageLabel, BorderLayout.EAST);
    
        captchaPanel.add(captchaInputPanel, BorderLayout.CENTER);

        // 刷新验证码按钮
        refreshCaptchaButton = new JButton("刷新");
        refreshCaptchaButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        refreshCaptchaButton.setPreferredSize(new Dimension(60, 25));
        refreshCaptchaButton.addActionListener(e -> refreshCaptcha());
        captchaPanel.add(refreshCaptchaButton, BorderLayout.EAST);
        
        formPanel.add(captchaPanel, gbc);
        
        // // 验证码图片
        // gbc.gridx = 0;
        // gbc.gridy = 3;
        // gbc.gridwidth = 2;
        // gbc.fill = GridBagConstraints.CENTER;
        // captchaImageLabel = new JLabel();
        // captchaImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        // formPanel.add(captchaImageLabel, gbc);
        
        // 记住我选项
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.WEST;
        rememberMeCheckBox = new JCheckBox("记住我");
        rememberMeCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rememberMeCheckBox.setBackground(Color.WHITE);
        formPanel.add(rememberMeCheckBox, gbc);
        
        return formPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(150, 40));
        loginButton.setBackground(new Color(64, 158, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(true);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> handleLogin());
        
        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        registerButton.setBackground(Color.WHITE);
        registerButton.setForeground(new Color(64, 158, 255));
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createLineBorder(new Color(64, 158, 255)));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> openRegisterWindow());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(registerButton);
        
        return buttonPanel;
    }
    
    // 打开注册窗口
    public void openRegisterWindow() {
        this.setVisible(false); // 隐藏登录窗口
        RegisterFrame registerFrame = new RegisterFrame(this);
        registerFrame.setVisible(true);
    }
    
    // 设置用户名（供注册窗口调用）
    public void setUsername(String username) {
        usernameField.setText(username);
    }
    
    // 刷新验证码
    private void refreshCaptcha() {
        new Thread(() -> {
            try {
                String response = sendGetRequest("http://localhost:8080/api/captcha/image");
                JSONObject json = new JSONObject(response);
                
                // 处理验证码图片
                String base64Image = "";
                if (json.has("success") && json.getBoolean("success")) {
                    if (json.has("data")) {
                        JSONObject data = json.getJSONObject("data");
                        if (data.has("captchaImage")) {
                            base64Image = data.getString("captchaImage");
                        }
                    } else if (json.has("captchaImage")) {
                        base64Image = json.getString("captchaImage");
                    }
                }
                
                // 更新sessionId
                if (json.has("data")) {
                    JSONObject data = json.getJSONObject("data");
                    if (data.has("sessionId")) {
                        sessionId = data.getString("sessionId");
                    }
                } else if (json.has("sessionId")) {
                    sessionId = json.getString("sessionId");
                }
                
                if (!base64Image.isEmpty()) {
                    // 将base64转换为图片
                    ImageIcon icon = new ImageIcon(decodeBase64(base64Image));
                    
                    // 在EDT线程更新UI
                    SwingUtilities.invokeLater(() -> {
                        captchaImageLabel.setIcon(icon);
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "获取验证码失败: " + ex.getMessage(), 
                                              "错误", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    // 处理登录
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String captcha = captchaField.getText().trim();
        
        // 简单验证
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入密码", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (captcha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入验证码", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 禁用登录按钮，防止重复提交
        loginButton.setEnabled(false);
        loginButton.setText("登录中...");
        
        // 在新线程中执行登录请求
        new Thread(() -> {
            try {
                // 创建请求体
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", username);
                requestBody.put("password", password);
                requestBody.put("captcha", captcha);
                requestBody.put("sessionId", sessionId);
                
                // 发送登录请求
                String response = sendPostRequest("http://localhost:8080/api/users/login", 
                                                 requestBody.toString());
                JSONObject jsonResponse = new JSONObject(response);
                
                // 处理响应
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                            // 登录成功
                            if (rememberMeCheckBox.isSelected()) {
                                saveRememberedUsername(username);
                            } else {
                                clearRememberedUsername();
                            }
                            
                            JOptionPane.showMessageDialog(this, "登录成功！", 
                                                          "提示", JOptionPane.INFORMATION_MESSAGE);
                            
                            // 这里可以打开主窗口或关闭登录窗口
                            // new MainFrame().setVisible(true);
                            // dispose(); // 关闭登录窗口
                        } else {
                            // 登录失败
                            String message = "登录失败";
                            if (jsonResponse.has("message")) {
                                message = jsonResponse.getString("message");
                            }
                            
                            JOptionPane.showMessageDialog(this, message, 
                                                          "登录失败", JOptionPane.ERROR_MESSAGE);
                            refreshCaptcha();
                            captchaField.setText("");
                        }
                    } catch (JSONException ex) {
                        JOptionPane.showMessageDialog(this, "响应解析错误: " + ex.getMessage(), 
                                                      "错误", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        // 恢复登录按钮
                        loginButton.setEnabled(true);
                        loginButton.setText("登录");
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "登录请求失败: " + ex.getMessage(), 
                                                  "错误", JOptionPane.ERROR_MESSAGE);
                    refreshCaptcha();
                    captchaField.setText("");
                    loginButton.setEnabled(true);
                    loginButton.setText("登录");
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
    
    // Base64解码
    private byte[] decodeBase64(String base64String) {
        if (base64String.startsWith("data:image/")) {
            int commaIndex = base64String.indexOf(',');
            if (commaIndex != -1) {
                base64String = base64String.substring(commaIndex + 1);
            }
        }
        
        return java.util.Base64.getDecoder().decode(base64String);
    }
    
    // 保存记住的用户名
    private void saveRememberedUsername(String username) {
        try {
            // 在实际应用中，可以使用Properties或Preferences API
            // 这里简化处理
            java.io.FileWriter writer = new java.io.FileWriter("remembered_username.txt");
            writer.write(username);
            writer.close();
        } catch (IOException e) {
            System.err.println("保存记住的用户名失败: " + e.getMessage());
        }
    }
    
    // 清除记住的用户名
    private void clearRememberedUsername() {
        try {
            java.io.File file = new java.io.File("remembered_username.txt");
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            System.err.println("清除记住的用户名失败: " + e.getMessage());
        }
    }
    
    // 检查记住的用户名
    private void checkRememberedUsername() {
        try {
            java.io.File file = new java.io.File("remembered_username.txt");
            if (file.exists()) {
                java.io.FileReader reader = new java.io.FileReader(file);
                char[] buffer = new char[1024];
                int length = reader.read(buffer);
                reader.close();

                if (length > 0) {
                    String username = new String(buffer, 0, length).trim();
                    usernameField.setText(username);
                    rememberMeCheckBox.setSelected(true);
                }
            }
        } catch (IOException e) {
            System.err.println("读取记住的用户名失败: " + e.getMessage());
        }
    }
}
