package com.shop.gui;

import javax.swing.SwingUtilities;

/**
 * GUI应用程序启动类
 * 这是您唯一需要运行的类
 */
public class GUIApplication {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                javax.swing.UIManager.setLookAndFeel(

                    javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // 创建并显示登录窗口作为入口
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}