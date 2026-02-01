package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleLogFormatter;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.models.log.EventDrivenLogList;
import github.renderbr.hytale.services.DiscordBotService;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ServerStateListener {
    public static HytaleLogFormatter formatter;

    public static void register(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(BootEvent.class, ServerStateListener::onServerStart);
        eventRegistry.registerGlobal(ShutdownEvent.class, ServerStateListener::onServerStop);
    }

    public static void onServerStart(BootEvent event) {

        DiscordBotService.init().thenAccept(instance -> {
            var logOutput = new EventDrivenLogList();
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
        if (!DiscordBotService.isRunning()) {
            return;
        }

        DiscordBotService.getInstance().sendMessageAppropriately(ChannelOutputTypes.SERVER_STATE, Message.translation("server.bot.averagediscord.serverstopped").getAnsiMessage());
    }

    public static String repairAnsi(String brokenLog) {
        return brokenLog
                .replace("\u001b[m", "\u001b[0m")
                .replaceAll("(?<!\\u001b)\\[m", "\u001b[0m")
                .replaceAll("(?<!\\u001b)\\[(?=\\d{1,2}(;\\d{1,2})?m)", "\u001b[");
    }

    public static void onLogReceived(LogRecord record) {
        if (!DiscordBotService.isRunning()) {
            return;
        }

        String formattedMessage = formatter.format(record).trim();


        // limit to 1750 characters to prevent crash
        if (formattedMessage.length() > 1750) {
            formattedMessage = formattedMessage.substring(0, 1750) + "... (truncated)";
        }

        formattedMessage = repairAnsi(formattedMessage);

        DiscordBotService.getInstance().sendMessageAppropriately(ChannelOutputTypes.INTERNAL_LOG, "```ansi\n" + formattedMessage + "```");
    }
}
