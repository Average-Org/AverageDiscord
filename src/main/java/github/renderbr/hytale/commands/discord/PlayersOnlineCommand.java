package github.renderbr.hytale.commands.discord;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PlayersOnlineCommand implements IDiscordCommand {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var players = Universe.get().getPlayers();

        if (players.isEmpty()) {
            event.reply(Message.translation("server.bot.averagediscord.commands.playersonline.noplayers").getAnsiMessage()).queue();
            return;
        }

        String msg;
        if (players.size() == 1) {
            msg = Message.translation("server.bot.averagediscord.commands.playersonline.oneplayer")
                    .param("player", players.getFirst().getUsername()).getAnsiMessage();
        } else {

            msg = Message.translation("server.bot.averagediscord.commands.playersonline.players")
                    .param("players", players.size())
                    .param("playerlist", players.stream()
                            .map(PlayerRef::getUsername)
                            .collect(java.util.stream.Collectors.joining(", "))).getAnsiMessage();
        }

        event.reply(msg).queue();
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("players", Message.translation("server.bot.averagediscord.commands.playersonline.desc").getAnsiMessage());
    }
}
