package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import github.renderbr.hytale.AverageDiscord;

public class PlayerStateListener {
    public static void register(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(PlayerReadyEvent.class, PlayerStateListener::onPlayerJoin);
        eventRegistry.registerGlobal(PlayerDisconnectEvent.class, PlayerStateListener::onPlayerLeave);
    }

    public static void onPlayerJoin(PlayerReadyEvent event) {
        if(AverageDiscord.instance.getChatChannel() != null) {
            AverageDiscord.instance.getChatChannel().sendMessage(":arrow_right: " + event.getPlayer().getDisplayName() + " joined the server!").queue();
        }
    }

    public static void onPlayerLeave(PlayerDisconnectEvent event){
        if(AverageDiscord.instance.getChatChannel() != null) {
            AverageDiscord.instance.getChatChannel().sendMessage(":arrow_left: " + event.getPlayerRef().getUsername() + " left the server!").queue();
        }
    }
}
