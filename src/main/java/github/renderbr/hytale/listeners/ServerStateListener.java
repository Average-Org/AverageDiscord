package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleLogFormatter;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.plugin.PluginState;
import com.hypixel.hytale.server.core.universe.Universe;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.models.log.EventDrivenLogList;
import github.renderbr.hytale.services.DiscordBotService;
import jdk.jfr.Event;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerStateListener {
    private static final Pattern BROKEN_ANSI_PATTERN = Pattern.compile("(?:\u001b\\[m|(?<!\u001b)\\[m|(?<!\u001b)\\[(?=\\d{1,2}(?:;\\d{1,2})?m))");

    private static volatile boolean isShuttingDown = false;
    public static HytaleLogFormatter formatter;
    private static EventDrivenLogList logOutput;

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

            instance.sendMessageAppropriately(ChannelOutputTypes.SERVER_STATE, Message.translation("server.bot.averagediscord.serverstarted").getAnsiMessage());
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

        DiscordBotService.getInstance().sendMessageAppropriately(ChannelOutputTypes.SERVER_STATE,
                Message.translation("server.bot.averagediscord.serverstopped").getAnsiMessage(), true);

        logOutput.removeListener(ServerStateListener::onLogReceived);
        HytaleLoggerBackend.unsubscribe(logOutput);
        DiscordBotService.getInstance().stop();
    }

    public static String repairAnsi(String brokenLog) {
        if (brokenLog == null || brokenLog.isEmpty()) {
            return brokenLog;
        }

        StringBuilder sb = new StringBuilder(brokenLog.length());
        Matcher m = BROKEN_ANSI_PATTERN.matcher(brokenLog);

        while (m.find()) {
            String match = m.group();
            // Map the found broken sequence to the fixed one
            if (match.equals("\u001b[m") || match.equals("[m")) {
                m.appendReplacement(sb, "\u001b[0m");
            } else {
                // This handles the cases like "[31m" -> "\u001b[31m"
                m.appendReplacement(sb, "\u001b[");
            }
        }
        m.appendTail(sb);

        return sb.toString();
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
            if (formattedMessage.length() > 1750) {
                formattedMessage = formattedMessage.substring(0, 1750) + "... (truncated)";
            }

            formattedMessage = repairAnsi(formattedMessage);
            DiscordBotService.getInstance().sendMessageAppropriately(ChannelOutputTypes.INTERNAL_LOG, "```ansi\n" + formattedMessage + "```");
        } catch (Exception e) {

        }
    }
}
