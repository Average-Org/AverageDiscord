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
import github.renderbr.hytale.models.log.EventDrivenLogList;
import github.renderbr.hytale.services.DiscordBotService;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerStateListener {
    private static final Pattern BROKEN_ANSI_PATTERN = Pattern.compile("\u001b\\[m|(?<!\u001b)\\[(?:m|(?=\\d{1,2}(?:;\\d{1,2})?m))");
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

        if (!DiscordBotService.isRunning()) {
            return;
        }

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

    public static String repairAnsi(String brokenLog) {
        if (brokenLog == null || brokenLog.isEmpty() || brokenLog.indexOf('[') == -1) {
            return brokenLog;
        }

        Matcher m = BROKEN_ANSI_PATTERN.matcher(brokenLog);
        if (!m.find()) {
            return brokenLog;
        }

        StringBuilder sb = new StringBuilder(brokenLog.length() + 16);
        do {
            // m.end() - m.start() is 3 for \u001b[m, 2 for [m, and 1 for [
            if (m.end() - m.start() > 1) {
                m.appendReplacement(sb, "\u001b[0m");
            } else {
                m.appendReplacement(sb, "\u001b[");
            }
        } while (m.find());

        return m.appendTail(sb).toString();
    }

    public static void onLogReceived(LogRecord record) {
        if (isShuttingDown
                || formatter == null
                || !DiscordBotService.isRunning()
                || HytaleServer.get().isShuttingDown()) {
            return;
        }

        try {
            String formattedMessage = formatter.format(record).trim();
            if (formattedMessage.isEmpty()) return;

            // limit to 1750 characters to prevent crash
            if (formattedMessage.length() > MAX_LOG_MESSAGE_LENGTH) {
                formattedMessage = formattedMessage.substring(0, MAX_LOG_MESSAGE_LENGTH) + "... (truncated)";
            }

            formattedMessage = repairAnsi(formattedMessage);
            DiscordBotService.getInstance().sendMessageAppropriately(ChannelOutputTypes.INTERNAL_LOG, "```ansi\n" + formattedMessage + "```");
        } catch (Exception e) {
            // Log record processing failed
        }
    }
}
