package github.renderbr.hytale.listeners;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.models.playerdeath.ADPlayerDeathSystem;
import github.renderbr.hytale.services.DiscordBotService;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener for player-related state changes such as joining and leaving.
 */
public class PlayerStateListener {
    private static final Set<UUID> ONLINE_PLAYERS = ConcurrentHashMap.newKeySet();

    /**
     * Registers player state listeners and the death system.
     * @param eventRegistry The event registry.
     * @param entityStoreRegistry The entity store registry.
     */
    public static void register(EventRegistry eventRegistry, ComponentRegistryProxy<EntityStore> entityStoreRegistry) {
        eventRegistry.registerGlobal(PlayerReadyEvent.class, PlayerStateListener::onPlayerJoin);
        eventRegistry.registerGlobal(PlayerDisconnectEvent.class, PlayerStateListener::onPlayerLeave);
        entityStoreRegistry.registerSystem(new ADPlayerDeathSystem());
    }

    /**
     * Handles player join events.
     * @param event The player ready event.
     */
    public static void onPlayerJoin(PlayerReadyEvent event) {
        if (!DiscordBotService.isRunning() || !ONLINE_PLAYERS.add(Objects.requireNonNull(event.getPlayerRef()
            .getStore()
            .getComponent(event.getPlayerRef(), PlayerRef.getComponentType()))
            .getUuid())) return;

        var service = DiscordBotService.getInstance();
        service.updateDiscordInformation();
        service.sendMessageAppropriately(
            ChannelOutputTypes.JOIN_LEAVE,
            Message.translation("server.bot.averagediscord.playerjoined")
                   .param("player", event.getPlayer().getDisplayName()).getAnsiMessage()
        );
    }

    /**
     * Handles player leave events.
     * @param event The player disconnect event.
     */
    public static void onPlayerLeave(PlayerDisconnectEvent event) {
        if (!DiscordBotService.isRunning()) return;

        ONLINE_PLAYERS.remove(event.getPlayerRef().getUuid());

        var service = DiscordBotService.getInstance();
        service.updateDiscordInformation();
        service.sendMessageAppropriately(
            ChannelOutputTypes.JOIN_LEAVE,
            Message.translation("server.bot.averagediscord.playerleft")
                   .param("player", event.getPlayerRef().getUsername()).getAnsiMessage()
        );
    }
}
