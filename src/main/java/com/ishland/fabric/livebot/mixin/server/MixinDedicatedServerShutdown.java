package com.ishland.fabric.livebot.mixin.server;

import com.ishland.fabric.livebot.LiveBotFabric;
import com.ishland.fabric.livebot.MixinLogger;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftDedicatedServer.class)
public class MixinDedicatedServerShutdown {

    @Inject(
            method = "shutdown",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onShutdown(CallbackInfo ci) {
        MixinLogger.logger.info("Shutting down LiveBotFabric");
        LiveBotFabric.getInstance().onDisable();
    }

}
