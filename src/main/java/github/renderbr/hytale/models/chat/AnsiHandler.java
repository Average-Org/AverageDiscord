package github.renderbr.hytale.models.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for repairing and translating ANSI sequences for Discord compatibility.
 */
public final class AnsiHandler {
    private static final Pattern ANSI_PATTERN = Pattern.compile("(?:\u001b)?\\[([\\d;]*)m");

    private AnsiHandler() {}

    /**
     * Repairs broken ANSI sequences and translates 256-color/RGB codes to basic ANSI.
     * 
     * @param text The text to process.
     * @return Sanitized text compatible with Discord's ANSI blocks.
     */
    public static String repair(String text) {
        if (text == null || text.isEmpty() || !text.contains("[")) {
            return text;
        }

        StringBuilder sb = new StringBuilder(text.length());
        Matcher m = ANSI_PATTERN.matcher(text);

        while (m.find()) {
            String translated = translate(m.group(1));
            m.appendReplacement(sb, "\u001b[" + translated + "m");
        }
        return m.appendTail(sb).toString();
    }

    private static String translate(String params) {
        if (params == null || params.isEmpty() || "0".equals(params)) {
            return "0";
        }

        String[] parts = params.split(";");
        List<String> result = new ArrayList<>(parts.length);

        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            
            // 256-color (38;5;n or 48;5;n)
            if (isColorPrefix(p) && hasSequence(parts, i + 1, "5")) {
                result.add(translate256(p, parts[i + 2]));
                i += 2;
                continue;
            }

            // RGB (38;2;r;g;b or 48;2;r;g;b)
            if (isColorPrefix(p) && hasSequence(parts, i + 1, "2") && i + 4 < parts.length) {
                result.add(translateRgb(p, parts[i + 2], parts[i + 3], parts[i + 4]));
                i += 4;
                continue;
            }

            result.add(p);
        }

        return result.isEmpty() ? "0" : String.join(";", result);
    }

    private static boolean isColorPrefix(String p) {
        return "38".equals(p) || "48".equals(p);
    }

    private static boolean hasSequence(String[] parts, int index, String value) {
        return index + 1 < parts.length && value.equals(parts[index]);
    }

    private static String translate256(String prefix, String colorIdStr) {
        try {
            int colorId = Integer.parseInt(colorIdStr);
            int mapped = map256ToBasic(colorId);
            return String.valueOf("48".equals(prefix) ? mapped + 10 : mapped);
        } catch (NumberFormatException e) {
            return prefix + ";5;" + colorIdStr;
        }
    }

    private static String translateRgb(String prefix, String r, String g, String b) {
        try {
            int ri = Integer.parseInt(r), gi = Integer.parseInt(g), bi = Integer.parseInt(b);
            int mapped = 30 + (ri > 127 ? 1 : 0) + (gi > 127 ? 2 : 0) + (bi > 127 ? 4 : 0);
            return String.valueOf("48".equals(prefix) ? mapped + 10 : mapped);
        } catch (NumberFormatException e) {
            return prefix + ";2;" + r + ";" + g + ";" + b;
        }
    }

    private static int map256ToBasic(int colorId) {
        if (colorId < 8) return 30 + colorId;
        if (colorId < 16) return 30 + (colorId - 8);
        if (colorId >= 232) return colorId < 244 ? 30 : 37;

        int res = colorId - 16;
        int r = (res / 36) > 2 ? 1 : 0;
        int g = ((res % 36) / 6) > 2 ? 1 : 0;
        int b = (res % 6) > 2 ? 1 : 0;
        return 30 + r + (g * 2) + (b * 4);
    }
}
