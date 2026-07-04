package com.snowzhou.queuenotice;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueNoticeMod implements ModInitializer {
	public static final String MOD_ID = "queue-notice-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Queue Notice Mod initializing...");

		// 注册客户端停止事件，清理资源
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			LOGGER.info("Queue Notice Mod shutting down...");
		});

		LOGGER.info("Queue Notice Mod initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
