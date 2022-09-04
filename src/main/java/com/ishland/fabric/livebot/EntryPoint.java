package com.ishland.fabric.livebot;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntryPoint implements ModInitializer {

    public static final Logger logger = LogManager.getLogger("LiveBotFabric EntryPoint");

    public EntryPoint() {
        logger.info("Loading LiveBotFabric");
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Server instance is not ready right here.
        logger.info("Loading LiveBotFabric...");
        try {
            LiveBotFabric.getInstance().onLoad();
        } catch (Throwable throwable) {
            logger.log(Level.FATAL,
                    "Error occurred while loading LiveBotFabric", throwable);
            throw new IllegalStateException("Error occurred while loading LiveBotFabric", throwable);
        }
        logger.info("Loaded LiveBotFabric.");

    }
}
