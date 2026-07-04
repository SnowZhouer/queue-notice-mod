package com.snowzhou.queuenotice.mixin;

import com.snowzhou.queuenotice.queue.QueueManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 监听连接状态的 Mixin
 * 注意：onDisconnect 已移至 Fabric API 事件处理（1.21.5+ 网络栈重构）
 */
@Mixin(ClientPacketListener.class)
public class ConnectionMixin {

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void onConnect(net.minecraft.network.protocol.game.ClientboundLoginPacket packet, CallbackInfo ci) {
        // 延迟检查，确保服务器信息已加载
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 等待 1 秒
                Minecraft.getInstance().execute(() -> {
                    QueueManager.checkServerConnection();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}