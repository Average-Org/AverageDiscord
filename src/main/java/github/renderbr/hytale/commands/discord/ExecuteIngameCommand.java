package github.renderbr.hytale.commands.discord;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.services.DiscordBotService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.concurrent.CompletionException;

public class ExecuteIngameCommand implements IDiscordCommand {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.isGuildCommand()) {
            return;
        }

        if (!DiscordBotService.getInstance().getAppropriateChannels(ChannelOutputTypes.ALL).contains(event.getChannel())) {
            return;
        }

        var user = event.getMember();

        // does user have administrator permission on Discord?
        assert user != null;
        if (!user.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        // get command from event
        var commandArg = event.getOption("command");

        if (commandArg == null) {
            return;
        }

        var command = commandArg.getAsString();

        // try execute as server
        HytaleServer.get().getCommandManager().handleCommand(ConsoleSender.INSTANCE, command)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        Throwable actualError = (error instanceof CompletionException) ? error.getCause() : error;

                        event.reply(Message.translation("server.bot.averagediscord.commands.execute.error")
                                .param("error", actualError.getMessage()).getAnsiMessage()).queue();
                        return;
                    }

                    event.reply(Message.translation("server.bot.averagediscord.commands.execute.success")
                            .getAnsiMessage()).queue();
                });

    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("execute", Message.translation("server.bot.averagediscord.commands.execute.desc").getAnsiMessage())
                .addOption(OptionType.STRING, "command", Message.translation("server.bot.averagediscord.commands.execute.command").getAnsiMessage(), true);
    }
}
