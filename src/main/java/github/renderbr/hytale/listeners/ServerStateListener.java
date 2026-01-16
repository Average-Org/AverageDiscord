package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleLogManager;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;

public class ServerStateListener {
    public static void register(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(BootEvent.class, ServerStateListener::onServerStart);
        eventRegistry.registerGlobal(ShutdownEvent.class, ServerStateListener::onServerStop);
    }

    public static void onServerStart(BootEvent event) {
        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.SERVER_STATE, Message.translation("server.bot.averagediscord.serverstarted").getAnsiMessage());

    }

    public static void onServerStop(ShutdownEvent event) {
        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.SERVER_STATE, Message.translation("server.bot.averagediscord.serverstopped").getAnsiMessage());
    }
}
