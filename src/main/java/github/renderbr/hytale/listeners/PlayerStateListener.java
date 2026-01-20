package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupDisconnectEvent;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.config.obj.DiscordBridgeConfiguration;
import github.renderbr.hytale.registries.ProviderRegistry;

public class PlayerStateListener {
    public static void register(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(PlayerReadyEvent.class, PlayerStateListener::onPlayerJoin);
        eventRegistry.registerGlobal(PlayerDisconnectEvent.class, PlayerStateListener::onPlayerLeave);
    }

    public static void onPlayerJoin(PlayerReadyEvent event) {
        AverageDiscord.instance.updateDiscordInformation();
        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.JOIN_LEAVE, Message.translation("server.bot.averagediscord.playerjoined").param("player", event.getPlayer().getDisplayName()).getAnsiMessage());
    }

    public static void onPlayerLeave(PlayerDisconnectEvent event) {
        AverageDiscord.instance.updateDiscordInformation();
        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.JOIN_LEAVE, Message.translation("server.bot.averagediscord.playerleft").param("player", event.getPlayerRef().getUsername()).getAnsiMessage());
    }
}
