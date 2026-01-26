package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleLogManager;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.models.log.EventDrivenLogList;
import github.renderbr.hytale.services.DiscordBotService;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.LogRecord;

public class ServerStateListener {
    public static void register(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(BootEvent.class, ServerStateListener::onServerStart);
        eventRegistry.registerGlobal(ShutdownEvent.class, ServerStateListener::onServerStop);

        var logOutput = new EventDrivenLogList();
        HytaleLoggerBackend.subscribe(logOutput);
        logOutput.addListener(ServerStateListener::onLogReceived);
    }

    public static void onServerStart(BootEvent event) {

        try {
            AverageDiscord.instance = DiscordBotService.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.SERVER_STATE, Message.translation("server.bot.averagediscord.serverstarted").getAnsiMessage());
        AverageDiscord.instance.updateDiscordInformation();
    }

    public static void onServerStop(ShutdownEvent event) {
        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.SERVER_STATE, Message.translation("server.bot.averagediscord.serverstopped").getAnsiMessage());
    }

    public static void onLogReceived(LogRecord record) {
        if (AverageDiscord.instance == null) {
            return;
        }

        String logMessage = record.getMessage();

        // limit to 1500 characters to prevent crash
        if (logMessage.length() > 1500) {
            logMessage = logMessage.substring(0, 1500);
        }

        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.INTERNAL_LOG, logMessage);
    }
}
