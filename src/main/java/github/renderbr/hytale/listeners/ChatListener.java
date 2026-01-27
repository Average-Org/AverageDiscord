package github.renderbr.hytale.listeners;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.DiscordBridgeConfigurationProvider;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.registries.ProviderRegistry;

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
            var strippedText = stripTextForForbiddenContent(event.getContent());
            var message = "**" + event.getSender().getUsername() + "**: " + strippedText;

            AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.CHAT, message);
            return;
        }

        var strippedText = stripTextForForbiddenContent(builtMessage);
        AverageDiscord.instance.SendMessageToType(ChannelOutputTypes.CHAT, strippedText);
    }

    public static String stripTextForForbiddenContent(String text) {
        if (text == null) return null;

        String newText = text;

        // remove Markdown hyperlinks: [Text](Url)
        // Matches '[' then any non-bracket chars, then ']', then '(' any non-paren chars, then ')'
        if (ProviderRegistry.discordBridgeConfigProvider.config.stripLinksInChat) {
            newText = newText.replaceAll("\\[[^\\]]*\\]\\([^\\)]*\\)", "(stripped url)");
        }

        // remove Pings: <@...>, <@&...>, <@!...>, @everyone, @here
        // Matches the @everyone/@here literals OR the <@ID> format
        if(ProviderRegistry.discordBridgeConfigProvider.config.stripPingsInChat) {
            newText = newText.replaceAll("@everyone|@here|<@(!|&)?\\d+>", "(stripped ping)");
        }

        return newText;
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
