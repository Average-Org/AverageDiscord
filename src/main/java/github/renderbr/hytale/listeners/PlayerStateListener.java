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
import github.renderbr.hytale.db.models.UserLink;
import github.renderbr.hytale.models.playerdeath.ADPlayerDeathSystem;
import github.renderbr.hytale.services.DiscordBotService;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;
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
     *
     * @param eventRegistry       The event registry.
     * @param entityStoreRegistry The entity store registry.
     */
    public static void register(EventRegistry eventRegistry, ComponentRegistryProxy<EntityStore> entityStoreRegistry) {
        eventRegistry.registerGlobal(PlayerReadyEvent.class, PlayerStateListener::onPlayerJoin);
        eventRegistry.registerGlobal(PlayerDisconnectEvent.class, PlayerStateListener::onPlayerLeave);
        entityStoreRegistry.registerSystem(new ADPlayerDeathSystem());
    }

    /**
     * Handles player join events.
     *
     * @param event The player ready event.
     */
    public static void onPlayerJoin(PlayerReadyEvent event) {
        var playerRef = Objects.requireNonNull(event.getPlayerRef()
                .getStore()
                .getComponent(event.getPlayerRef(), PlayerRef.getComponentType()));

        var playerUuid = playerRef.getUuid();

        if (!DiscordBotService.isRunning() || !ONLINE_PLAYERS.add(playerUuid)) return;

        var service = DiscordBotService.getInstance();
        service.updateDiscordInformation();

        service.sendMessageAppropriately(ChannelOutputTypes.JOIN_LEAVE,
                buildPlayerStateEmbed(
                        playerRef.getUsername(),
                        Message.translation("server.bot.averagediscord.playerjoined")
                                .param("player", playerRef.getUsername()).getAnsiMessage(),
                        Color.GREEN,
                        playerUuid
                )
        );
    }

    /**
     * Handles player leave events.
     *
     * @param event The player disconnect event.
     */
    public static void onPlayerLeave(PlayerDisconnectEvent event) {
        if (!DiscordBotService.isRunning()) return;

        ONLINE_PLAYERS.remove(event.getPlayerRef().getUuid());

        var service = DiscordBotService.getInstance();
        service.updateDiscordInformation();
        service.sendMessageAppropriately(ChannelOutputTypes.JOIN_LEAVE,
                buildPlayerStateEmbed(
                        event.getPlayerRef().getUsername(),
                        Message.translation("server.bot.averagediscord.playerleft")
                                .param("player", event.getPlayerRef().getUsername()).getAnsiMessage(),
                        Color.RED,
                        event.getPlayerRef().getUuid()
                )
        );
    }

    private static net.dv8tion.jda.api.entities.MessageEmbed buildPlayerStateEmbed(String playerName, String description, Color color, UUID hytaleUserId) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(playerName)
                .setDescription(description)
                .setColor(color)
                .setTimestamp(Instant.now());

        String avatarUrl = getLinkedDiscordAvatarUrl(hytaleUserId);
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            embedBuilder.setThumbnail(avatarUrl);
        }

        return embedBuilder.build();
    }

    private static String getLinkedDiscordAvatarUrl(UUID hytaleUserId) {
        try {
            UserLink link = AverageDiscord.databaseService
                    .getTable(UserLink.class)
                    .queryBuilder()
                    .where()
                    .eq("hytaleUserId", hytaleUserId.toString())
                    .queryForFirst();

            if (link == null || link.discordUserId == null || link.discordUserId.isBlank()) {
                return null;
            }

            var discordUser = DiscordBotService.getInstance().getJdaInstance().retrieveUserById(link.discordUserId).complete();
            return discordUser != null ? discordUser.getEffectiveAvatarUrl() : null;
        } catch (SQLException | RuntimeException ignored) {
            return null;
        }
    }
}
