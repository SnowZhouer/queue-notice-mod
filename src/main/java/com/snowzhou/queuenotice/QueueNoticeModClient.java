package com.snowzhou.queuenotice;

import com.snowzhou.queuenotice.queue.QueueManager;
import com.snowzhou.queuenotice.util.NotificationHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;

/**
 * 客户端模组初始化
 */
public class QueueNoticeModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 初始化桌面通知（PowerShell 方案，无需 AWT）
        NotificationHelper.init();

        // 监听服务器游戏消息（系统消息）
        ClientReceiveMessageEvents.GAME.register((Component message, boolean overlay) -> {
            QueueManager.handleChatMessage(message);
        });

        // 监听玩家聊天消息
        ClientReceiveMessageEvents.CHAT.register((Component message, net.minecraft.network.chat.PlayerChatMessage signedMessage, com.mojang.authlib.GameProfile sender, net.minecraft.network.chat.ChatType.Bound params, java.time.Instant receptionTimestamp) -> {
            QueueManager.handleChatMessage(message);
        });

        // 监听断开连接事件（1.21.5+ 网络栈重构后使用 Fabric API 事件替代 Mixin）
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            QueueManager.onDisconnect();
        });

        QueueNoticeMod.LOGGER.info("Queue Notice Mod Client initialized!");
    }
}
