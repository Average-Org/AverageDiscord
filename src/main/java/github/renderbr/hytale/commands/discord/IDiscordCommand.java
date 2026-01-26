package github.renderbr.hytale.commands.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface IDiscordCommand {
    public void execute(SlashCommandInteractionEvent event);

    public SlashCommandData buildCommand();
}
