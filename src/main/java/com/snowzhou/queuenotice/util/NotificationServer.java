package com.snowzhou.queuenotice.util;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 独立的通知服务进程
 * <p>
 * 在独立的 JVM 中运行，避免 Minecraft 的 AWT headless 环境和 GLFW 窗口冲突。
 * 独立 JVM 中 SystemTray 可以正常工作，且不会被杀毒软件标记。
 * <p>
 * 用法: java NotificationServer "标题" "消息"
 */
public class NotificationServer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("用法: NotificationServer <标题> <消息>");
            System.exit(1);
            return;
        }

        String title = args[0];
        String message = args[1];
        System.out.println("[NotificationServer] 发送通知: title=" + title + ", message=" + message);

        // 检查 SystemTray 支持
        if (!SystemTray.isSupported()) {
            System.err.println("[NotificationServer] SystemTray 不支持");
            System.exit(1);
            return;
        }

        TrayIcon trayIcon = null;
        try {
            // 创建简单图标
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.GREEN);
            g2d.fillOval(0, 0, 16, 16);
            g2d.dispose();

            trayIcon = new TrayIcon(image, "Queue Notice Mod");
            trayIcon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(trayIcon);

            // 显示通知
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            System.out.println("[NotificationServer] 通知已发送");

            // 保持进程存活一段时间，确保通知显示完成
            Thread.sleep(6000);
        } catch (Exception e) {
            System.err.println("[NotificationServer] 错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (trayIcon != null) {
                try {
                    SystemTray.getSystemTray().remove(trayIcon);
                } catch (Exception ignored) {
                }
            }
        }
        System.out.println("[NotificationServer] 完成，退出");
        System.exit(0);
    }
}
