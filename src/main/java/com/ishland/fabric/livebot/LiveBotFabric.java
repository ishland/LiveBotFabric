package com.ishland.fabric.livebot;

import com.ishland.fabric.livebot.data.LiveBotConfig;
import com.ishland.fabric.livebot.data.LiveBotState;
import com.ishland.fabric.livebot.data.ServerInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class LiveBotFabric {

    public static final Logger logger = LogManager.getLogger("LiveBotFabric Main");

    @SuppressWarnings("SpellCheckingInspection")
    private static final Identifier bossBarIdentifier = new Identifier("livebot", "bossbar");

    private static LiveBotFabric instance = null;

    private int nextRefresh = 0;
    private TimerTask savingTask = null;

    private LiveBotFabric() {
        instance = this;
    }

    public static LiveBotFabric getInstance() {
        return instance != null ? instance : new LiveBotFabric();
    }

    public static void teleport(ServerPlayerEntity bot, Entity target) {
        teleport(bot, target, false, true);
    }

    public static void teleport(ServerPlayerEntity bot, Entity target, boolean spectate) {
        teleport(bot, target, spectate, true);
    }

    public static void teleport(ServerPlayerEntity bot, Entity target,
                                boolean spectate, boolean updateTwice) {
        logger.info("Moved Bot to " + target.getDisplayName().asString() + " " + target.getUuid());
        if (!bot.isSpectator())
            bot.changeGameMode(GameMode.SPECTATOR);
        bot.teleport(
                (ServerWorld) target.world,
                target.getX(),
                target.getY(),
                target.getZ(),
                target.getYaw(),
                target.getPitch()
        );
        if (spectate) {
            LiveBotState.getInstance().followedEntity = target.getUuid();
            LiveBotState.getInstance().dim = bot.getEntityWorld().getRegistryKey().getValue().toString();
            LiveBotState.getInstance().x = target.getX();
            LiveBotState.getInstance().y = target.getY();
            LiveBotState.getInstance().z = target.getZ();
            LiveBotState.getInstance().yaw = target.getYaw();
            LiveBotState.getInstance().pitch = target.getPitch();
            bot.setCameraEntity(target);
        } else
            LiveBotState.getInstance().followedEntity = null;
        if (updateTwice) getInstance().nextRefresh = LiveBotConfig.INSTANCE.REFRESH_DELAY;
    }

    // Warning: The server instance is not available right here
    public void onLoad() {
        logger.info("Loading data...");
        LiveBotState.getInstance().load();
        LiveBotConfig.INSTANCE.hashCode();
        savingTask = new TimerTask() {
            @Override
            public void run() {
                LiveBotState.getInstance().save();
            }
        };
        new Timer(true).schedule(savingTask, 30 * 1000, 60 * 1000);
    }

    public void onDisable() {
        savingTask.cancel();
        logger.info("Saving data...");
        LiveBotState.getInstance().save();

    }

    public void onPreWorldTick() {
        ServerPlayerEntity bot = ServerInstance.server.getPlayerManager().getPlayer(LiveBotConfig.INSTANCE.STREAM_BOT);
        updateView(bot);
        updateBossBar(bot);
    }

    private void updateBossBar(ServerPlayerEntity bot) {
        CommandBossBar bossBar = ServerInstance.server.getBossBarManager().get(bossBarIdentifier);
        if (bossBar == null) bossBar = ServerInstance.server.getBossBarManager()
                .add(bossBarIdentifier, new LiteralText("LiveBotFabric BossBar"));
        bossBar.addPlayers(ServerInstance.server.getPlayerManager().getPlayerList());
        bossBar.setColor(BossBar.Color.BLUE);
        bossBar.setVisible(true);
        bossBar.setName(
                new LiteralText("Bot State: ")
                        .setStyle(Style.EMPTY.withBold(true))
                        .append(
                                (
                                        bot == null
                                                ? new LiteralText("Offline")
                                                .setStyle(Style.EMPTY.withBold(true)
                                                        .withColor(Formatting.RED))
                                                : (
                                                bot.getCameraEntity() != bot
                                                        ? new LiteralText(
                                                        "Spectating " + bot
                                                                .getCameraEntity()
                                                                .getDisplayName().asString())
                                                        .setStyle(Style.EMPTY.withBold(true)
                                                                .withColor(Formatting.GREEN))
                                                        : new LiteralText("Normal")
                                                        .setStyle(Style.EMPTY.withBold(true)
                                                                .withColor(Formatting.DARK_GREEN))
                                        )
                                )
                        )
        );
        if (bot != null) {
            if (bot.getCameraEntity() == bot)
                bossBar.setPercent(0);
            else bossBar.setPercent(
                    (float) (1F - (
                            Math.sqrt(
                                    Math.pow(LiveBotState.getInstance().x - bot.getX(), 2) +
                                            Math.pow(LiveBotState.getInstance().y - bot.getY(), 2) +
                                            Math.pow(LiveBotState.getInstance().z - bot.getZ(), 2)
                            ) / (LiveBotConfig.INSTANCE.REFRESH_DISTANCE + 4)
                    ))
            );
        } else bossBar.setPercent(0);

    }

    private void updateView(ServerPlayerEntity bot) {
        Entity target = getEntity(LiveBotState.getInstance().followedEntity);
        if (bot == null || target == null) return;
        if (nextRefresh >= 0) nextRefresh--;
        if (!bot.isSpectator())
            bot.changeGameMode(GameMode.SPECTATOR);
        if (LiveBotState.getInstance().followedEntity.equals(bot.getUuid()))
            LiveBotState.getInstance().followedEntity = null;
        if (bot.getCameraEntity().equals(bot) ||
                !bot.getEntityWorld().getRegistryKey().getValue().toString()
                        .equals(LiveBotState.getInstance().dim) ||
                (
                        Math.pow(LiveBotState.getInstance().x - target.getX(), 2) +
                                Math.pow(LiveBotState.getInstance().y - target.getY(), 2) +
                                Math.pow(LiveBotState.getInstance().z - target.getZ(), 2)
                ) > LiveBotConfig.INSTANCE.REFRESH_DISTANCE * LiveBotConfig.INSTANCE.REFRESH_DISTANCE)
            teleport(bot, target, true);
        if (nextRefresh == 0)
            teleport(bot, target, true, false);
    }

    private Entity getEntity(UUID uuid) {
        for (ServerWorld world : ServerInstance.server.getWorlds()) {
            final Entity entity = world.getEntity(uuid);
            if (entity != null) return entity;
        }
        return null;
    }
}
