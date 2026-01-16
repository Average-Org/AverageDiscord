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
        if (!builtMessage.isEmpty()) {
            AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.CHAT, builtMessage);
        }

    }

    public static String recursivelyBuildFormattedMessage(FormattedMessage formattedMessage) {
        StringBuilder stringBuilder = new StringBuilder();
        if (formattedMessage.children == null) return formattedMessage.rawText;
        if (formattedMessage.children.length == 0) return "";

        for (FormattedMessage msgPart : formattedMessage.children) {
            if (msgPart.children != null) {
                if (msgPart.children.length > 0) {
                    stringBuilder.append(recursivelyBuildFormattedMessage(msgPart));
                    continue;
                }
            }

            if (msgPart.rawText == null) continue;
            stringBuilder.append(msgPart.rawText);
        }
        return stringBuilder.toString();
    }
}
