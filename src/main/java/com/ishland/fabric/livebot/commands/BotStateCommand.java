package com.ishland.fabric.livebot.commands;

import com.ishland.fabric.livebot.LiveBotFabric;
import com.ishland.fabric.livebot.data.LiveBotConfig;
import com.ishland.fabric.livebot.data.LiveBotState;
import com.ishland.fabric.livebot.data.ServerInstance;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

public class BotStateCommand {

    public static void register(@NotNull CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register(
                CommandManager.literal("botstate")
                        .executes(ctx -> {
                            ServerPlayerEntity bot = ServerInstance.server.getPlayerManager()
                                    .getPlayer(LiveBotConfig.INSTANCE.STREAM_BOT);
                            ctx.getSource().sendFeedback(
                                    new LiteralText("Bot state: ")
                                            .append(
                                                    (
                                                            bot == null
                                                                    ? "Offline"
                                                                    : (
                                                                    bot.getCameraEntity() != bot
                                                                            ? "Spectating " + bot
                                                                            .getCameraEntity()
                                                                            .getDisplayName().asString()
                                                                            : "Normal"
                                                            )
                                                    )
                                            ),
                                    true);
                            if (bot != null) {
                                ctx.getSource().sendFeedback(
                                        new LiteralText("Bot current position: (")
                                                .append(String.valueOf(bot.getEntityWorld().getRegistryKey().getValue().toString()))
                                                .append(",")
                                                .append(String.valueOf(bot.getX()))
                                                .append(",")
                                                .append(String.valueOf(bot.getY()))
                                                .append(",")
                                                .append(String.valueOf(bot.getZ()))
                                                .append(")"),
                                        true);
                            }
                            ctx.getSource().sendFeedback(
                                    new LiteralText("Bot previous refresh position: (")
                                            .append(String.valueOf(LiveBotState.getInstance().x))
                                            .append(",")
                                            .append(String.valueOf(LiveBotState.getInstance().y))
                                            .append(",")
                                            .append(String.valueOf(LiveBotState.getInstance().z))
                                            .append(")"),
                                    true);
                            if (bot != null)
                                ctx.getSource().sendFeedback(
                                        new LiteralText("Distance: ")
                                                .append(String.valueOf(Math.sqrt(
                                                        Math.pow(LiveBotState.getInstance().x - bot.getX(), 2) +
                                                                Math.pow(LiveBotState.getInstance().y - bot.getY(), 2) +
                                                                Math.pow(LiveBotState.getInstance().z - bot.getZ(), 2)
                                                ))),
                                        true);
                            return 1;
                        })
        );
    }

}
