package github.renderbr.hytale.models.chat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnsiHandlerTest {

    @Test
    void testRepair_NullOrEmpty() {
        assertNull(AnsiHandler.repair(null));
        assertEquals("", AnsiHandler.repair(""));
    }

    @Test
    void testRepair_NoChangesNeeded() {
        String input = "\u001b[31mRed Text\u001b[0m";
        assertEquals(input, AnsiHandler.repair(input));
    }

    @Test
    void testRepair_BrokenReset() {
        // [m should become \u001b[0m
        assertEquals("\u001b[0m", AnsiHandler.repair("[m"));
        // \u001b[m should become \u001b[0m
        assertEquals("\u001b[0m", AnsiHandler.repair("\u001b[m"));
    }

    @Test
    void testRepair_BrokenColor() {
        // [31m should become \u001b[31m
        assertEquals("\u001b[31mRed", AnsiHandler.repair("[31mRed"));
        // [1;33m should become \u001b[1;33m
        assertEquals("\u001b[1;33mBold Yellow", AnsiHandler.repair("[1;33mBold Yellow"));
        // [38;5;46m should become \u001b[32m (mapped to basic Green for Discord)
        assertEquals("\u001b[32mGreen", AnsiHandler.repair("[38;5;46mGreen"));
        // RGB Red [38;2;255;0;0m should become \u001b[31m
        assertEquals("\u001b[31mRed", AnsiHandler.repair("[38;2;255;0;0mRed"));
    }

    @Test
    void testRepair_Mixed() {
        String input = "[31mError: [mSomething went wrong [33mWarning";
        String expected = "\u001b[31mError: \u001b[0mSomething went wrong \u001b[33mWarning";
        assertEquals(expected, AnsiHandler.repair(input));
    }

    @Test
    void testRepair_NoBracket() {
        String input = "Just some normal text";
        assertEquals(input, AnsiHandler.repair(input));
    }
}
