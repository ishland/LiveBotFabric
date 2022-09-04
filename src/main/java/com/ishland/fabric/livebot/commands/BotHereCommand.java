package com.ishland.fabric.livebot.commands;

import com.ishland.fabric.livebot.LiveBotFabric;
import com.ishland.fabric.livebot.data.LiveBotConfig;
import com.ishland.fabric.livebot.data.LiveBotState;
import com.ishland.fabric.livebot.data.ServerInstance;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class BotHereCommand {

    private static final Logger logger = LogManager.getLogger("LiveBotFabric Command BotHere");

    public static void register(@NotNull CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register(
                CommandManager.literal("bothere")
                        .then(CommandManager.argument("target", EntityArgumentType.entity())
                                .executes(ctx -> {
                                    final Entity target =
                                            EntityArgumentType.getEntity(ctx, "target");
                                    logger.info(ctx.getSource().getDisplayName().asString() +
                                            " called with target: " + target.getUuid());

                                    if (!doTeleport(ctx, target)) return -1;

                                    return 1;
                                }))
                        .executes(ctx -> {
                            final Entity target = ctx.getSource().getEntity();
                            if (target == null) {
                                logger.info(ctx.getSource().getDisplayName().asString() +
                                        " called with no argument from non-entity");
                                ctx.getSource().sendFeedback(
                                        new LiteralText("This command can only be run by an entity!"),
                                        true);
                                return -1;
                            }
                            logger.info(ctx.getSource().getDisplayName().asString() +
                                    " called with no argument, using " +
                                    target.getUuid());

                            if (!doTeleport(ctx, target)) return -1;

                            return 1;
                        })
        );
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean doTeleport(CommandContext<ServerCommandSource> ctx, Entity target) {
        ServerPlayerEntity bot =
                ServerInstance.server.getPlayerManager()
                        .getPlayer(LiveBotConfig.INSTANCE.STREAM_BOT);
        if (bot == null) {
            ctx.getSource().sendFeedback(
                    new LiteralText("Bot is not online"), true);
            return false;
        }

        LiveBotFabric.teleport(bot, target, false);

        ctx.getSource().sendFeedback(
                new LiteralText("Successfully teleported Bot to " +
                        target.getDisplayName().asString()), true);
        return true;
    }

}
