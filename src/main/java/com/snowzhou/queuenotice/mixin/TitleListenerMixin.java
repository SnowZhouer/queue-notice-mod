package com.snowzhou.queuenotice.mixin;

import com.snowzhou.queuenotice.queue.QueueManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截 Title 消息的 Mixin
 */
@Mixin(Gui.class)
public class TitleListenerMixin {

    @Inject(method = "setTitle", at = @At("HEAD"))
    private void onSetTitle(Component title, CallbackInfo ci) {
        QueueManager.handleTitle(title, "title");
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"))
    private void onSetSubtitle(Component subtitle, CallbackInfo ci) {
        QueueManager.handleTitle(subtitle, "subtitle");
    }
}