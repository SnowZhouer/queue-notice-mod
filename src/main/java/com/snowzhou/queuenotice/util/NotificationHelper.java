package com.snowzhou.queuenotice.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 桌面通知工具类
 * <p>
 * 通过启动独立 Java 子进程（NotificationServer）发送系统托盘通知。
 * 独立 JVM 中 AWT SystemTray 正常工作，不受 Minecraft headless 环境影响，
 * 且不会像 PowerShell 那样被杀毒软件标记。
 */
public class NotificationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger("QueueNotice-Notification");
    private static String javaPath;

    /**
     * 初始化通知系统，检测 Java 环境
     */
    public static void init() {
        LOGGER.info("[NotificationHelper] 通知系统初始化（子进程方案）...");

        // 查找 javaw.exe（无控制台窗口的 Java）
        String javaHome = System.getProperty("java.home");
        javaPath = javaHome + "/bin/javaw.exe";

        // 验证 javaw.exe 存在
        try {
            ProcessBuilder pb = new ProcessBuilder(javaPath, "-version");
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            proc.waitFor();
            LOGGER.info("[NotificationHelper] Java 子进程可用: {}", javaPath);
        } catch (Exception e) {
            // 回退到 java.exe
            javaPath = javaHome + "/bin/java.exe";
            try {
                ProcessBuilder pb = new ProcessBuilder(javaPath, "-version");
                pb.redirectErrorStream(true);
                Process proc = pb.start();
                proc.waitFor();
                LOGGER.info("[NotificationHelper] Java 子进程可用（回退）: {}", javaPath);
            } catch (Exception e2) {
                LOGGER.error("[NotificationHelper] 找不到 Java 可执行文件，通知功能不可用");
                javaPath = null;
            }
        }
    }

    /**
     * 显示桌面通知（异步，不阻塞游戏线程）
     *
     * @param title   标题
     * @param message 消息
     */
    public static void notify(String title, String message) {
        LOGGER.info("[NotificationHelper] notify() 被调用: title={}, message={}", title, message);

        if (javaPath == null) {
            LOGGER.warn("[NotificationHelper] Java 路径不可用，无法发送通知");
            return;
        }

        // 异步发送，不阻塞游戏线程
        Thread notifyThread = new Thread(() -> sendViaSubprocess(title, message), "QueueNotice-Send");
        notifyThread.setDaemon(true);
        notifyThread.start();
    }

    /**
     * 通过独立 Java 子进程发送通知
     */
    private static void sendViaSubprocess(String title, String message) {
        try {
            String modClasspath = getModClasspath();
            if (modClasspath == null || modClasspath.isEmpty()) {
                LOGGER.error("[NotificationHelper] 无法获取 mod 类路径，通知功能不可用");
                return;
            }
            String serverClass = "com.snowzhou.queuenotice.util.NotificationServer";

            List<String> cmd = new ArrayList<>();
            cmd.add(javaPath);
            cmd.add("-cp");
            cmd.add(modClasspath);
            cmd.add(serverClass);
            cmd.add(title);
            cmd.add(message);

            LOGGER.info("[NotificationHelper] 启动通知子进程: classpath长度={}, class={}", modClasspath.length(), serverClass);
            LOGGER.info("[NotificationHelper] 参数: title={}, message={}", title, message);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            // 异步读取子进程输出
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOGGER.info("[NotificationServer] {}", line);
                    }
                } catch (Exception e) {
                    LOGGER.debug("[NotificationHelper] 读取子进程输出结束");
                }
            }, "QueueNotice-Subprocess-Output");
            outputReader.setDaemon(true);
            outputReader.start();

            // 等待子进程完成（最多 15 秒）
            boolean finished = proc.waitFor(15, java.util.concurrent.TimeUnit.SECONDS);
            if (finished) {
                int exitCode = proc.exitValue();
                if (exitCode == 0) {
                    LOGGER.info("[NotificationHelper] 通知子进程正常退出");
                } else {
                    LOGGER.warn("[NotificationHelper] 通知子进程退出码: {}", exitCode);
                }
            } else {
                LOGGER.warn("[NotificationHelper] 通知子进程超时，强制终止");
                proc.destroyForcibly();
            }
        } catch (Exception e) {
            LOGGER.error("[NotificationHelper] 通知子进程启动失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取 mod 的实际类路径（兼容 Fabric Knot 类加载器和标准 classpath）
     * <p>
     * Fabric 使用自定义 Knot 类加载器，System.getProperty("java.class.path")
     * 不包含 mod jar 路径。需要通过 ProtectionDomain 获取实际位置。
     */
    private static String getModClasspath() {
        try {
            URI location = NotificationHelper.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI();
            File file = new File(location);
            String path = file.getAbsolutePath();
            LOGGER.info("[NotificationHelper] Mod 类路径: {}", path);
            return path;
        } catch (Exception e) {
            LOGGER.error("[NotificationHelper] 获取 mod 类路径失败: {}", e.getMessage());
            String fallback = System.getProperty("java.class.path");
            LOGGER.info("[NotificationHelper] 回退到 java.class.path: 长度={}", fallback != null ? fallback.length() : 0);
            return fallback;
        }
    }

    /**
     * 清理资源（子进程方案无需清理）
     */
    public static void cleanup() {
        // 每次通知启动独立子进程，自动退出，无需清理
    }
}
