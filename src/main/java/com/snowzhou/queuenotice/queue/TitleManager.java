package com.snowzhou.queuenotice.queue;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Title 显示管理器
 */
public class TitleManager {

    /**
     * 显示队列位置 Title
     *
     * @param queuePosition 队列位置
     */
    public static void showQueueTitle(int queuePosition) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        // 使用 displayClientMessage 的第二个参数 true 来显示 Title
        Component title = Component.literal("正在游玩 | 队列位置：" + queuePosition);
        mc.gui.setTitle(title);
        mc.gui.setSubtitle(Component.empty());

        // 设置 Title 的显示时间（ticks）
        // 淡入 10 ticks，停留 70 ticks，淡出 20 ticks
        mc.gui.setTimes(10, 70, 20);
    }

    /**
     * 清除 Title
     */
    public static void clearTitle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        mc.gui.setTitle(Component.empty());
        mc.gui.setSubtitle(Component.empty());
    }
}