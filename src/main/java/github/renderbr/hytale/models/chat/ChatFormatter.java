package github.renderbr.hytale.models.chat;

import com.hypixel.hytale.protocol.FormattedMessage;
import github.renderbr.hytale.config.obj.DiscordBridgeConfiguration;
import github.renderbr.hytale.registries.ProviderRegistry;

/**
 * Utility class for formatting and filtering chat messages.
 */
public final class ChatFormatter {
    private ChatFormatter() { }

    /**
     * Strips forbidden content (links, pings) from text based on current configuration.
     * @param text The text to process.
     * @return The sanitized text.
     */
    public static String stripTextForForbiddenContent(String text) {
        return stripTextForForbiddenContent(text, ProviderRegistry.discordBridgeConfigProvider.getConfig());
    }

    /**
     * Strips forbidden content using a specific configuration (useful for testing).
     * @param text The text to process.
     * @param config The configuration to use.
     * @return The sanitized text.
     */
    public static String stripTextForForbiddenContent(String text, DiscordBridgeConfiguration config) {
        if (text == null || config == null) return text;
        String result = text;

        if (Boolean.TRUE.equals(config.stripLinksInChat)) {
            result = result.replaceAll("\\[[^\\]]*\\]\\([^\\)]*\\)", "(stripped url)");
        }

        if (Boolean.TRUE.equals(config.stripPingsInChat)) {
            result = result.replaceAll("@everyone|@here|<@(!|&)?\\d+>", "(stripped ping)");
        }

        return result;
    }

    /**
     * Recursively builds a plain text string from a FormattedMessage.
     * @param msg The formatted message node.
     * @return The combined plain text.
     */
    public static String recursivelyBuildFormattedMessage(FormattedMessage msg) {
        if (msg == null) return "";
        StringBuilder sb = new StringBuilder();
        if (msg.rawText != null) sb.append(msg.rawText);
        if (msg.children != null) {
            for (FormattedMessage child : msg.children) {
                sb.append(recursivelyBuildFormattedMessage(child));
            }
        }
        return sb.toString();
    }
}
