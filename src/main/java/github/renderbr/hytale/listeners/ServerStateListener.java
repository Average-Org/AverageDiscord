package github.renderbr.hytale.listeners;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.backend.HytaleLogFormatter;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.models.log.EventDrivenLogList;
import github.renderbr.hytale.services.DiscordBotService;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener for server-wide state changes and log capture.
 */
public class ServerStateListener {
    private static final Pattern ANSI_PATTERN = Pattern.compile("(?:\u001b)?\\[([\\d;]*)m");
    private static final int MAX_LOG_MESSAGE_LENGTH = 1750;

    private static volatile boolean isShuttingDown = false;
    public static volatile HytaleLogFormatter formatter;
    private static volatile EventDrivenLogList logOutput;

    public static void register(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(BootEvent.class, ServerStateListener::onServerStart);
        eventRegistry.registerGlobal(ShutdownEvent.class, ServerStateListener::onServerStop);
    }

    public static void onServerStart(BootEvent event) {
        DiscordBotService.init().thenAccept(instance -> {
            logOutput = new EventDrivenLogList();
            formatter = new HytaleLogFormatter(() -> true);

            HytaleLoggerBackend.subscribe(logOutput);
            logOutput.addListener(ServerStateListener::onLogReceived);

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(Message.translation("server.bot.averagediscord.serverstarted").getAnsiMessage());
            eb.setColor(Color.green);

            eb.appendDescription(Message.translation("server.bot.averagediscord.serverstarted.desc1").getAnsiMessage());
            eb.appendDescription("\n");
            eb.appendDescription(Message.translation("server.bot.averagediscord.serverstarted.desc2").param("world", Objects.requireNonNull(Universe.get().getDefaultWorld()).getName()).getAnsiMessage());

            var version = ManifestUtil.getImplementationVersion();
            if (version != null) {
                eb.appendDescription("\n");
                eb.appendDescription(Message.translation("server.bot.averagediscord.serverstarted.desc3").param("version", ManifestUtil.getImplementationVersion()).getAnsiMessage());
            }

            eb.setTimestamp(java.time.Instant.now());

            instance.sendMessageAppropriately(ChannelOutputTypes.SERVER_STATE, eb.build());
            instance.updateDiscordInformation();
        }).exceptionally(ex -> {
            AverageDiscord.LOGGER.at(Level.SEVERE).log(Message.translation("server.error.averagediscord.failedtostart").param("ex", ex.getLocalizedMessage()).getAnsiMessage());
            return null;
        });
    }

    public static void onServerStop(ShutdownEvent event) {
        isShuttingDown = true;
        if (!DiscordBotService.isRunning()) return;

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(Message.translation("server.bot.averagediscord.serverstopped").getAnsiMessage());
        eb.setDescription(Message.translation("server.bot.averagediscord.serverstopped.desc1").getAnsiMessage());
        eb.setColor(Color.red);
        eb.setTimestamp(java.time.Instant.now());

        DiscordBotService.getInstance().sendMessageAppropriately(ChannelOutputTypes.SERVER_STATE, eb.build());

        logOutput.removeListener(ServerStateListener::onLogReceived);
        HytaleLoggerBackend.unsubscribe(logOutput);
        DiscordBotService.getInstance().stop();
    }

    /**
     * Repairs broken ANSI sequences and translates 256-color/RGB codes to basic ANSI for Discord.
     * @param brokenLog The log message containing potentially broken/unsupported ANSI.
     * @return The repaired and translated log message.
     */
    public static String repairAnsi(String brokenLog) {
        if (brokenLog == null || brokenLog.isEmpty() || brokenLog.indexOf('[') == -1) return brokenLog;

        StringBuilder sb = new StringBuilder(brokenLog.length());
        Matcher m = ANSI_PATTERN.matcher(brokenLog);

        while (m.find()) {
            String params = m.group(1);
            m.appendReplacement(sb, "\u001b[" + translateAnsi(params) + "m");
        }
        return m.appendTail(sb).toString();
    }

    private static String translateAnsi(String params) {
        if (params == null || params.isEmpty() || params.equals("0")) return "0";
        String[] parts = params.split(";");
        List<String> result = new ArrayList<>();

        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            try {
                // Handle 256-color (38;5;n or 48;5;n)
                if ((p.equals("38") || p.equals("48")) && i + 2 < parts.length && parts[i+1].equals("5")) {
                    int mapped = map256ToBasic(Integer.parseInt(parts[i+2]));
                    result.add(String.valueOf(p.equals("48") ? mapped + 10 : mapped));
                    i += 2;
                    continue;
                }
                // Handle RGB (38;2;r;g;b or 48;2;r;g;b)
                if ((p.equals("38") || p.equals("48")) && i + 4 < parts.length && parts[i+1].equals("2")) {
                    int r = Integer.parseInt(parts[i+2]), g = Integer.parseInt(parts[i+3]), b = Integer.parseInt(parts[i+4]);
                    int mapped = 30 + (r > 127 ? 1 : 0) + (g > 127 ? 2 : 0) + (b > 127 ? 4 : 0);
                    result.add(String.valueOf(p.equals("48") ? mapped + 10 : mapped));
                    i += 4;
                    continue;
                }
            } catch (Exception ignored) {}
            result.add(p);
        }
        return result.isEmpty() ? "0" : String.join(";", result);
    }

    private static int map256ToBasic(int colorId) {
        if (colorId < 8) return 30 + colorId;
        if (colorId < 16) return 30 + (colorId - 8);
        if (colorId >= 232) return (colorId < 244) ? 30 : 37;

        int res = colorId - 16;
        int r = (res / 36) > 2 ? 1 : 0;
        int g = ((res % 36) / 6) > 2 ? 1 : 0;
        int b = (res % 6) > 2 ? 1 : 0;
        return 30 + r + (g * 2) + (b * 4);
    }

    public static void onLogReceived(LogRecord record) {
        if (isShuttingDown || formatter == null || !DiscordBotService.isRunning() || HytaleServer.get().isShuttingDown()) return;

        try {
            String formatted = formatter.format(record).trim();
            if (formatted.isEmpty()) return;

            if (formatted.length() > MAX_LOG_MESSAGE_LENGTH) {
                formatted = formatted.substring(0, MAX_LOG_MESSAGE_LENGTH) + "... (truncated)";
            }

            DiscordBotService.getInstance().sendMessageAppropriately(ChannelOutputTypes.INTERNAL_LOG, "```ansi\n" + repairAnsi(formatted) + "```");
        } catch (Exception ignored) {}
    }
}
