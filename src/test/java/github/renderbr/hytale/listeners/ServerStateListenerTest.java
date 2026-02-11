package github.renderbr.hytale.listeners;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServerStateListenerTest {

    @Test
    void testRepairAnsi_NullOrEmpty() {
        assertNull(ServerStateListener.repairAnsi(null));
        assertEquals("", ServerStateListener.repairAnsi(""));
    }

    @Test
    void testRepairAnsi_NoChangesNeeded() {
        String input = "\u001b[31mRed Text\u001b[0m";
        assertEquals(input, ServerStateListener.repairAnsi(input));
    }

    @Test
    void testRepairAnsi_BrokenReset() {
        // [m should become \u001b[0m
        assertEquals("\u001b[0m", ServerStateListener.repairAnsi("[m"));
        // \u001b[m should become \u001b[0m
        assertEquals("\u001b[0m", ServerStateListener.repairAnsi("\u001b[m"));
    }

    @Test
    void testRepairAnsi_BrokenColor() {
        // [31m should become \u001b[31m
        assertEquals("\u001b[31mRed", ServerStateListener.repairAnsi("[31mRed"));
        // [1;33m should become \u001b[1;33m
        assertEquals("\u001b[1;33mBold Yellow", ServerStateListener.repairAnsi("[1;33mBold Yellow"));
        // [38;5;46m should become \u001b[32m (mapped to basic Green for Discord)
        assertEquals("\u001b[32mGreen", ServerStateListener.repairAnsi("[38;5;46mGreen"));
        // RGB Red [38;2;255;0;0m should become \u001b[31m
        assertEquals("\u001b[31mRed", ServerStateListener.repairAnsi("[38;2;255;0;0mRed"));
    }

    @Test
    void testRepairAnsi_Mixed() {
        String input = "[31mError: [mSomething went wrong [33mWarning";
        String expected = "\u001b[31mError: \u001b[0mSomething went wrong \u001b[33mWarning";
        assertEquals(expected, ServerStateListener.repairAnsi(input));
    }

    @Test
    void testRepairAnsi_NoBracket() {
        String input = "Just some normal text";
        assertEquals(input, ServerStateListener.repairAnsi(input));
    }
}
