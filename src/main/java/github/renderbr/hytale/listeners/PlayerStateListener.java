package github.renderbr.hytale.listeners;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.models.playerdeath.ADPlayerDeathSystem;
import github.renderbr.hytale.services.DiscordBotService;

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
        if (!DiscordBotService.isRunning()) return;

        PlayerRef playerRef = event.getPlayerRef().getStore().getComponent(event.getPlayerRef(), PlayerRef.getComponentType());

        if (playerRef == null) {
            return;
        }

        if (onlinePlayers.contains(playerRef.getUuid())) return;
        onlinePlayers.add(playerRef.getUuid());

        var service = DiscordBotService.getInstance();

        service.updateDiscordInformation();
        service.sendMessageAppropriately(ChannelOutputTypes.JOIN_LEAVE, Message.translation("server.bot.averagediscord.playerjoined").param("player", event.getPlayer().getDisplayName()).getAnsiMessage());
    }

    public static void onPlayerLeave(PlayerDisconnectEvent event) {
        if (!DiscordBotService.isRunning()) return;

        onlinePlayers.remove(event.getPlayerRef().getUuid());
        var service = DiscordBotService.getInstance();

        service.updateDiscordInformation();
        service.sendMessageAppropriately(ChannelOutputTypes.JOIN_LEAVE, Message.translation("server.bot.averagediscord.playerleft").param("player", event.getPlayerRef().getUsername()).getAnsiMessage());
    }
}
