package github.renderbr.hytale.commands.discord;

import com.hypixel.hytale.logger.HytaleLogger;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CommandHandler extends ListenerAdapter {

    public List<IDiscordCommand> commands = new ArrayList<>();

    public void registerCommands(Guild guild) {
        commands.add(new PlayersOnlineCommand());
        commands.add(new StatusCommand());

        var action = guild.updateCommands();

        for (var command : commands) {
            action.addCommands(command.buildCommand());
        }

        action.queue(
                success -> HytaleLogger.getLogger().at(Level.INFO).log("Successfully registered " + commands.size() + " commands."),
                error -> HytaleLogger.getLogger().at(Level.SEVERE).log("Failed to register commands: " + error.getMessage())
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        for (var command : commands) {
            if (event.getName().equals(command.buildCommand().getName())) {
                command.execute(event);
                return;
            }
        }
    }
}
