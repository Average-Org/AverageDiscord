package github.renderbr.hytale.commands.discord;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.Color;
import java.util.Objects;
import java.util.stream.Collectors;

public class StatusCommand implements IDiscordCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // 1. Acknowledge immediately to stop the "Thinking" spinner
        event.deferReply().queue();

        try {
            var universe = Universe.get();
            var server = HytaleServer.get();

            if (universe == null || universe.getDefaultWorld() == null) {
                event.getHook().sendMessage("Server is still initializing...").queue();
                return;
            }

            // 2. Safe Metric Calculation (Manual average to avoid the IndexOutOfBounds bug)
            var metricSet = universe.getDefaultWorld().getBufferedTickLengthMetricSet();
            long[] values = metricSet.getAllValues();

            double avgTickNanos;
            if (values != null && values.length > 0) {
                long sum = 0;
                int count = Math.min(values.length, 100);
                for (int i = values.length - count; i < values.length; i++) {
                    sum += values[i];
                }
                avgTickNanos = (double) sum / count;
            } else {
                avgTickNanos = 50_000_000.0;
            }

            double tps = Math.min(20.0, 1_000_000_000.0 / avgTickNanos);
            long usedMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
            long maxMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(Message.translation("server.bot.averagediscord.commands.status.title")
                    .param("servername", server.getServerName()).getAnsiMessage());

            embed.setColor(tps > 19.0 ? Color.GREEN : (tps > 15.0 ? Color.ORANGE : Color.RED));

            String vitals = String.format("**TPS:** %.2f\n**MSPT:** %.1fms\n**Memory:** %dMB / %dMB",
                    tps, (avgTickNanos / 1_000_000.0), usedMem, maxMem);

            embed.addField("Server Vitals", vitals, false);

            // Player section
            var players = universe.getPlayers();
            if (players.isEmpty()) {
                embed.addField("Players", "No players online.", false);
            } else {
                String names = players.stream()
                        .map(PlayerRef::getUsername)
                        .limit(15)
                        .collect(Collectors.joining(", "));
                if (players.size() > 15) names += "...";
                embed.addField("Online (" + players.size() + ")", names, false);
            }

            embed.setFooter("Hytale Version: " + ServerManager.MANIFEST.getVersion().toString());

            // 4. Complete the interaction
            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            e.printStackTrace(); // Log it to your Hytale console
            event.getHook().sendMessage("Command failed: " + e.getMessage()).queue();
        }
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("status",
                Message.translation("server.bot.averagediscord.commands.status.desc").getAnsiMessage());
    }
}