package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.services.DiscordBotService;

import static github.renderbr.hytale.models.chat.ChatFormatter.recursivelyBuildFormattedMessage;
import static github.renderbr.hytale.models.chat.ChatFormatter.stripTextForForbiddenContent;

public class ChatListener {

    public static void registerChatListeners(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(EventPriority.LATE, PlayerChatEvent.class, ChatListener::onPlayerChat);
    }

    public static void onPlayerChat(PlayerChatEvent event) {
        if (!DiscordBotService.isRunning()) return;
        if (event.isCancelled()) return;

        var service = DiscordBotService.getInstance();

        var formatter = event.getFormatter();

        FormattedMessage formattedMessage = formatter.format(event.getSender(), event.getContent()).getFormattedMessage();

        var builtMessage = recursivelyBuildFormattedMessage(formattedMessage);

        if (formattedMessage == null || builtMessage.isEmpty()) {
            // use fallback
            var strippedText = stripTextForForbiddenContent(event.getContent());
            var message = "**" + event.getSender().getUsername() + "**: " + strippedText;

            service.sendMessageAppropriately(ChannelOutputTypes.CHAT, message);
            return;
        }

        var strippedText = stripTextForForbiddenContent(builtMessage);
        service.sendMessageAppropriately(ChannelOutputTypes.CHAT, strippedText);
    }
}
