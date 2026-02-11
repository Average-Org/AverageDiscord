package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.services.DiscordBotService;

import static github.renderbr.hytale.models.chat.ChatFormatter.recursivelyBuildFormattedMessage;
import static github.renderbr.hytale.models.chat.ChatFormatter.stripTextForForbiddenContent;

/**
 * Listener for player chat events to bridge them to Discord.
 */
public class ChatListener {

    /**
     * Registers chat listeners to the event registry.
     * @param eventRegistry The event registry to register with.
     */
    public static void registerChatListeners(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(EventPriority.LATE, PlayerChatEvent.class, ChatListener::onPlayerChat);
    }

    /**
     * Handles player chat events and sends them to Discord.
     * @param event The player chat event.
     */
    public static void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled() || !DiscordBotService.isRunning()) return;

        var formatted = event.getFormatter().format(event.getSender(), event.getContent()).getFormattedMessage();
        var message = recursivelyBuildFormattedMessage(formatted);

        // Fallback if formatting fails or is empty
        if (message.isEmpty()) {
            message = "**" + event.getSender().getUsername() + "**: " + event.getContent();
        }

        DiscordBotService.getInstance().sendMessageAppropriately(
            ChannelOutputTypes.CHAT, 
            stripTextForForbiddenContent(message)
        );
    }
}
