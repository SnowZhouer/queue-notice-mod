package com.snowzhou.queuenotice.queue;

import com.snowzhou.queuenotice.util.NotificationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 队列管理器
 */
public class QueueManager {

    private static final String TARGET_SERVER = "3c3u.org";
    private static final boolean DEBUG_MODE = true;
    private static final Pattern QUEUE_PATTERN = Pattern.compile("正在游玩.*队列位置[：:]\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUEUE_PATTERN_2 = Pattern.compile("Queue.*position[：:]\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    private static boolean isConnectedToTargetServer = false;
    private static boolean hasNotifiedFormat = false;
    private static int lastQueuePosition = -1;
    private static int notifiedPosition10 = -1;
    private static int notifiedPosition3 = -1;
    private static int notifiedPosition1 = -1;

    /**
     * 检查当前服务器是否为目标服务器
     */
    public static void checkServerConnection() {
        if (DEBUG_MODE) {
            isConnectedToTargetServer = true;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            isConnectedToTargetServer = false;
            return;
        }

        String serverAddress = mc.getCurrentServer() != null ? mc.getCurrentServer().ip : "";

        if (serverAddress != null && serverAddress.toLowerCase().contains(TARGET_SERVER)) {
            isConnectedToTargetServer = true;
        } else {
            isConnectedToTargetServer = false;
            resetQueueState();
        }
    }

    /**
     * 处理聊天消息
     */
    public static void handleChatMessage(Component message) {
        if (!isConnectedToTargetServer) {
            return;
        }

        String text = message.getString();
        parseQueueInfo(text);
    }

    /**
     * 处理 Title 消息
     * @param title Title 组件
     * @param type 类型标识（"title" 或 "subtitle"）
     */
    public static void handleTitle(Component title, String type) {
        if (!isConnectedToTargetServer) {
            return;
        }

        String text = title.getString();
        boolean matched = QUEUE_PATTERN.matcher(text).find() || QUEUE_PATTERN_2.matcher(text).find();

        // 仅在首次匹配时发送一次提示
        if (matched && !hasNotifiedFormat) {
            hasNotifiedFormat = true;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.gui.getChat().addMessage(Component.literal("§a[队列通知] 已检测到指定格式，模组正常工作！"));
            }
        }

        parseQueueInfo(text);
    }

    /**
     * 解析队列信息
     */
    private static void parseQueueInfo(String text) {
        // 匹配两种模式
        Matcher matcher = QUEUE_PATTERN.matcher(text);
        boolean found = matcher.find();
        
        if (!found) {
            matcher = QUEUE_PATTERN_2.matcher(text);
            found = matcher.find();
        }

        if (found) {
            try {
                int     queuePosition = Integer.parseInt(matcher.group(1));

                // 如果队列位置改变了，更新并检查通知
                if (queuePosition != lastQueuePosition) {
                    lastQueuePosition = queuePosition;

                    // 显示 Title
                    //TitleManager.showQueueTitle(queuePosition);

                    // 检查是否需要发送桌面通知
                    checkAndNotify(queuePosition);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查并发送桌面通知
     */
    private static void checkAndNotify(int queuePosition) {
        // 队列位置为 10 时通知
        if (queuePosition == 10 && notifiedPosition10 != queuePosition) {
            NotificationHelper.notify("队列通知", "你的队列位置现在是 10！");
            notifiedPosition10 = queuePosition;
        }
        // 队列位置为 3 时通知
        if (queuePosition == 3 && notifiedPosition3 != queuePosition) {
            NotificationHelper.notify("队列通知", "你的队列位置现在是 3！");
            notifiedPosition3 = queuePosition;
        }

        // 队列位置为 1 时通知
        if (queuePosition == 1 && notifiedPosition1 != queuePosition) {
            NotificationHelper.notify("队列通知", "你的队列位置现在是 1！即将进入服务器！");
            notifiedPosition1 = queuePosition;
        }
    }

    /**
     * 重置队列状态
     */
    private static void resetQueueState() {
        lastQueuePosition = -1;
        notifiedPosition10 = -1;
        notifiedPosition3 = -1;
        notifiedPosition1 = -1;
    }

    /**
     * 断开连接时调用
     */
    public static void onDisconnect() {
        isConnectedToTargetServer = false;
        hasNotifiedFormat = false;
        resetQueueState();
    }

    /**
     * 图腾使用事件（爆图腾）桌面通知
     */
    public static void onTotemPop() {
        if (!isConnectedToTargetServer) {
            return;
        }
        NotificationHelper.notify("图腾提醒", "你刚刚使用了一个图腾！");
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.gui.getChat().addMessage(Component.literal("§d[图腾] 检测到图腾使用！已发送桌面通知"));
        }
    }
}