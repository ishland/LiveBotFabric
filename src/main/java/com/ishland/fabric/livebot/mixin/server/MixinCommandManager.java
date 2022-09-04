package com.ishland.fabric.livebot.mixin.server;

import com.ishland.fabric.livebot.MixinLogger;
import com.ishland.fabric.livebot.commands.BotFollowCommand;
import com.ishland.fabric.livebot.commands.BotHereCommand;
import com.ishland.fabric.livebot.commands.BotStateCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class MixinCommandManager {

    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/MeCommand;register(Lcom/mojang/brigadier/CommandDispatcher;)V"))
    public void registerCommands(CommandManager.RegistrationEnvironment environment, CallbackInfo ci) {
        MixinLogger.logger.info("Registering commands...");
        BotHereCommand.register(this.dispatcher);
        BotFollowCommand.register(this.dispatcher);
        BotStateCommand.register(this.dispatcher);
    }

}
