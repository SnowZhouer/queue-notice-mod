package com.snowzhou.queuenotice.mixin;

import com.snowzhou.queuenotice.queue.QueueManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截图腾使用事件的 Mixin
 * 实体事件 ID 35 表示图腾使用（Totem of Undying）
 */
@Mixin(ClientPacketListener.class)
public class TotemMixin {

    @Inject(method = "handleEntityEvent", at = @At("TAIL"))
    private void onEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        // 实体事件 ID 35 = 图腾使用
        if (packet.getEventId() == 35) {
            QueueManager.onTotemPop();
        }
    }
}
