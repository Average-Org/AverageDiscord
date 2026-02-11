package github.renderbr.hytale.models.chat;

import com.hypixel.hytale.protocol.FormattedMessage;
import github.renderbr.hytale.registries.ProviderRegistry;

public final class ChatFormatter {
    private ChatFormatter() {
    }

    public static String stripTextForForbiddenContent(String text) {
        if (text == null) return null;

        String newText = text;

        var config = ProviderRegistry.discordBridgeConfigProvider.getConfig();

        // remove Markdown hyperlinks: [Text](Url)
        // Matches '[' then any non-bracket chars, then ']', then '(' any non-paren chars, then ')'
        if (config.stripLinksInChat) {
            newText = newText.replaceAll("\\[[^\\]]*\\]\\([^\\)]*\\)", "(stripped url)");
        }

        // remove Pings: <@...>, <@&...>, <@!...>, @everyone, @here
        // Matches the @everyone/@here literals OR the <@ID> format
        if (config.stripPingsInChat) {
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
