package github.renderbr.hytale.commands.discord;

import github.renderbr.hytale.AverageDiscord;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;

public class CommandHandler extends ListenerAdapter {

    public List<IDiscordCommand> commands = List.of(
            new PlayersOnlineCommand(),
            new StatusCommand(),
            new ExecuteIngameCommand()
    );

    public void registerCommands(Guild guild) {
        var action = guild.updateCommands().addCommands(commands
                .stream()
                .map(IDiscordCommand::buildCommand)
                .toList());

        action.queue(
                commandsRegistered -> AverageDiscord.LOGGER.at(Level.INFO).log("Successfully registered: " + commandsRegistered.size() + " commands."),
                error -> AverageDiscord.LOGGER.at(Level.SEVERE).log("Failed to register commands: " + error.getMessage())
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getMember() == null || event.getUser().isBot()){
            return;
        }

        for (var command : commands) {
            if (event.getName().equals(command.buildCommand().getName())) {
                command.execute(event);
                return;
            }
        }
    }
}
