package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import github.renderbr.hytale.AverageDiscord;

public class ServerStateListener {
    public static void register(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(AllWorldsLoadedEvent.class, ServerStateListener::onServerStart);
        eventRegistry.registerGlobal(ShutdownEvent.class, ServerStateListener::onServerStop);
    }

    public static void onServerStart(AllWorldsLoadedEvent event) {
        if (AverageDiscord.instance.getChatChannel() != null) {
            AverageDiscord.instance.getChatChannel().sendMessage(":white_check_mark: Server started!").queue();
        }
    }

    public static void onServerStop(ShutdownEvent event) {
        if (AverageDiscord.instance.getChatChannel() != null) {
            AverageDiscord.instance.getChatChannel().sendMessage(":x: Server stopped!").queue();
        }
    }
}
