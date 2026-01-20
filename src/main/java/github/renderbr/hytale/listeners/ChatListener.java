package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;

public class ChatListener {

    public static void registerChatListeners(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(EventPriority.LATE, PlayerChatEvent.class, ChatListener::onPlayerChat);
    }

    public static void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) return;

        var formatter = event.getFormatter();

        FormattedMessage formattedMessage = formatter.format(event.getSender(), event.getContent()).getFormattedMessage();

        var builtMessage = recursivelyBuildFormattedMessage(formattedMessage);

        if (formattedMessage == null || builtMessage.isEmpty()) {
            // use fallback
            AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.CHAT, event.getSender().getUsername()
                    + ": " + event.getContent());
            return;
        }

        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.CHAT, builtMessage);
    }

    public static String recursivelyBuildFormattedMessage(FormattedMessage msg) {
        if (msg == null) return "";

        StringBuilder sb = new StringBuilder();

        // add this node's text if it exists
        if (msg.rawText != null) {
            sb.append(msg.rawText);
        }

        // recursively add all children
        if (msg.children != null) {
            for (FormattedMessage child : msg.children) {
                sb.append(recursivelyBuildFormattedMessage(child));
            }
        }

        return sb.toString();
    }
}
