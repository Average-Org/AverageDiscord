package github.renderbr.hytale.listeners;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.backend.HytaleLogFormatter;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.models.chat.AnsiHandler;
import github.renderbr.hytale.models.log.EventDrivenLogList;
import github.renderbr.hytale.services.DiscordBotService;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Listener for server-wide state changes and log capture.
 */
public class ServerStateListener {
    private static final int MAX_LOG_MESSAGE_LENGTH = 1750;

    private static volatile boolean isShuttingDown = false;
    public static volatile HytaleLogFormatter formatter;
    private static volatile EventDrivenLogList logOutput;

    public static void register(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(BootEvent.class, ServerStateListener::onServerStart);
        eventRegistry.registerGlobal(ShutdownEvent.class, ServerStateListener::onServerStop);
    }

    public static void onServerStart(BootEvent event) {
        DiscordBotService.init().thenAccept(instance -> {
            logOutput = new EventDrivenLogList();
            formatter = new HytaleLogFormatter(() -> true);

            HytaleLoggerBackend.subscribe(logOutput);
            logOutput.addListener(ServerStateListener::onLogReceived);

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(Message.translation("server.bot.averagediscord.serverstarted").getAnsiMessage());
            eb.setColor(Color.green);

            eb.appendDescription(Message.translation("server.bot.averagediscord.serverstarted.desc1").getAnsiMessage());
            eb.appendDescription("\n");
            eb.appendDescription(Message.translation("server.bot.averagediscord.serverstarted.desc2").param("world", Objects.requireNonNull(Universe.get().getDefaultWorld()).getName()).getAnsiMessage());

            var version = ManifestUtil.getImplementationVersion();
            if (version != null) {
                eb.appendDescription("\n");
                eb.appendDescription(Message.translation("server.bot.averagediscord.serverstarted.desc3").param("version", ManifestUtil.getImplementationVersion()).getAnsiMessage());
            }

            eb.setTimestamp(java.time.Instant.now());

            instance.sendMessageAppropriately(ChannelOutputTypes.SERVER_STATE, eb.build());
            instance.updateDiscordInformation();
        }).exceptionally(ex -> {
            AverageDiscord.LOGGER.at(Level.SEVERE).log(Message.translation("server.error.averagediscord.failedtostart").param("ex", ex.getLocalizedMessage()).getAnsiMessage());
            return null;
        });
    }

    public static void onServerStop(ShutdownEvent event) {
        isShuttingDown = true;
        if (!DiscordBotService.isRunning()) return;

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(Message.translation("server.bot.averagediscord.serverstopped").getAnsiMessage());
        eb.setDescription(Message.translation("server.bot.averagediscord.serverstopped.desc1").getAnsiMessage());
        eb.setColor(Color.red);
        eb.setTimestamp(java.time.Instant.now());

        DiscordBotService.getInstance().sendMessageAppropriately(ChannelOutputTypes.SERVER_STATE, eb.build());

        logOutput.removeListener(ServerStateListener::onLogReceived);
        HytaleLoggerBackend.unsubscribe(logOutput);
        DiscordBotService.getInstance().stop();
    }

    public static void onLogReceived(LogRecord record) {
        if (isShuttingDown || formatter == null || !DiscordBotService.isRunning() || HytaleServer.get().isShuttingDown()) return;

        try {
            String formatted = formatter.format(record).trim();
            if (formatted.isEmpty()) return;

            if (formatted.length() > MAX_LOG_MESSAGE_LENGTH) {
                formatted = formatted.substring(0, MAX_LOG_MESSAGE_LENGTH) + "... (truncated)";
            }

            DiscordBotService.getInstance().sendMessageAppropriately(
                ChannelOutputTypes.INTERNAL_LOG, 
                "```ansi\n" + AnsiHandler.repair(formatted) + "```"
            );
        } catch (Exception ignored) {}
    }
}
