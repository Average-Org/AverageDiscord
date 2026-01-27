package github.renderbr.hytale.listeners;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.config.obj.DiscordBridgeConfiguration;
import github.renderbr.hytale.models.playerdeath.ADPlayerDeathSystem;
import github.renderbr.hytale.registries.ProviderRegistry;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerStateListener {
    public static ArrayList<UUID> onlinePlayers = new ArrayList<>();

    public static void register(EventRegistry eventRegistry, ComponentRegistryProxy<EntityStore> entityStoreRegistry) {
        eventRegistry.registerGlobal(PlayerReadyEvent.class, PlayerStateListener::onPlayerJoin);
        eventRegistry.registerGlobal(PlayerDisconnectEvent.class, PlayerStateListener::onPlayerLeave);
        entityStoreRegistry.registerSystem(new ADPlayerDeathSystem());
    }

    public static void onPlayerJoin(PlayerReadyEvent event) {
        PlayerRef playerRef = event.getPlayerRef().getStore().getComponent(event.getPlayerRef(), PlayerRef.getComponentType());

        if (playerRef == null) {
            return;
        }

        if (onlinePlayers.contains(playerRef.getUuid())) return;
        onlinePlayers.add(playerRef.getUuid());

        AverageDiscord.instance.updateDiscordInformation();
        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.JOIN_LEAVE, Message.translation("server.bot.averagediscord.playerjoined").param("player", event.getPlayer().getDisplayName()).getAnsiMessage());
    }

    public static void onPlayerLeave(PlayerDisconnectEvent event) {
        onlinePlayers.remove(event.getPlayerRef().getUuid());
        AverageDiscord.instance.updateDiscordInformation();
        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.JOIN_LEAVE, Message.translation("server.bot.averagediscord.playerleft").param("player", event.getPlayerRef().getUsername()).getAnsiMessage());
    }
}
