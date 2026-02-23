package github.renderbr.hytale.commands.discord;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.console.ConsoleModule;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.db.models.UserLink;
import github.renderbr.hytale.services.DiscordBotService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletionException;

public class ExecuteIngameCommand implements IDiscordCommand {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!DiscordBotService.getInstance().getAppropriateChannels(ChannelOutputTypes.ALL).contains(event.getChannel())) {
            return;
        }

        // get command from event
        var commandArg = event.getOption("command");

        if (commandArg == null) {
            return;
        }

        var command = commandArg.getAsString();

        event.deferReply().queue();

        var member = event.getMember();
        if (member == null) {
            event.getHook().sendMessage(Message.translation("server.bot.averagediscord.commands.execute.forbidden").getAnsiMessage()).queue();
            return;
        }

        var commandManager = HytaleServer.get().getCommandManager();

        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            commandManager.handleCommand(ConsoleSender.INSTANCE, command)
                    .whenComplete((result, error) -> handleCommandResult(event, error));
            return;
        }

        UserLink link;
        try {
            link = AverageDiscord.databaseService.getTable(UserLink.class)
                    .queryBuilder()
                    .where()
                    .eq("discordUserId", event.getUser().getId())
                    .queryForFirst();
        } catch (SQLException e) {
            event.getHook().sendMessage(
                    Message.translation("server.bot.averagediscord.commands.execute.error")
                            .param("error", e.getMessage())
                            .getAnsiMessage()
            ).queue();
            return;
        }

        if (link == null || link.hytaleUserId == null || link.hytaleUserId.isBlank()) {
            event.getHook().sendMessage(Message.translation("server.bot.averagediscord.commands.execute.linkrequired").getAnsiMessage()).queue();
            return;
        }

        UUID linkedUuid;
        try {
            linkedUuid = UUID.fromString(link.hytaleUserId);
        } catch (IllegalArgumentException ex) {
            event.getHook().sendMessage(Message.translation("server.bot.averagediscord.commands.execute.linkinvalid").getAnsiMessage()).queue();
            return;
        }

        var linkedPlayer = Universe.get().getPlayer(linkedUuid);
        if (linkedPlayer == null) {
            event.getHook().sendMessage(Message.translation("server.bot.averagediscord.commands.execute.playeroffline").getAnsiMessage()).queue();
            return;
        }
        
        commandManager.handleCommand(linkedPlayer, command)
                .whenComplete((result, error) -> handleCommandResult(event, error));

    }

    private void handleCommandResult(SlashCommandInteractionEvent event, Throwable error) {
        var hook = event.getHook();

        if (error != null) {
            Throwable actualError = (error instanceof CompletionException) ? error.getCause() : error;

            hook.sendMessage(Message.translation("server.bot.averagediscord.commands.execute.error")
                    .param("error", actualError.getMessage()).getAnsiMessage()).queue();
            return;
        }

        hook.sendMessage(Message.translation("server.bot.averagediscord.commands.execute.success")
                .getAnsiMessage()).queue();
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("execute", Message.translation("server.bot.averagediscord.commands.execute.desc").getAnsiMessage())
                .addOption(OptionType.STRING, "command", Message.translation("server.bot.averagediscord.commands.execute.command").getAnsiMessage(), true);
    }
}
