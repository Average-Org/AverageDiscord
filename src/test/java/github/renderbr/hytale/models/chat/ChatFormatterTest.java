package github.renderbr.hytale.models.chat;

import github.renderbr.hytale.config.obj.DiscordBridgeConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatFormatterTest {

    @Test
    void testStripTextForForbiddenContent_Links() {
        DiscordBridgeConfiguration config = new DiscordBridgeConfiguration();
        config.stripLinksInChat = true;
        
        String input = "Check this out: [Hytale](https://hytale.com)";
        String expected = "Check this out: (stripped url)";
        assertEquals(expected, ChatFormatter.stripTextForForbiddenContent(input, config));
    }

    @Test
    void testStripTextForForbiddenContent_Pings() {
        DiscordBridgeConfiguration config = new DiscordBridgeConfiguration();
        config.stripPingsInChat = true;

        String input = "Hey @everyone and <@123456789>!";
        String expected = "Hey (stripped ping) and (stripped ping)!";
        assertEquals(expected, ChatFormatter.stripTextForForbiddenContent(input, config));
    }
}
