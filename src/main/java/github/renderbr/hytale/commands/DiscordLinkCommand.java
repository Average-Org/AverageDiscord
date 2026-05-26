package github.renderbr.hytale.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.db.models.PendingLink;
import github.renderbr.hytale.db.models.UserLink;
import github.renderbr.hytale.services.DiscordBotService;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class DiscordLinkCommand extends AbstractCommandCollection {

    public DiscordLinkCommand() {
        super("discordlink", "server.commands.averagediscord.link.desc");
        this.addAliases("linkdiscord", "dlink");
        this.addSubCommand(new Username());
        this.addSubCommand(new Unlink());
    }

    protected static class Unlink extends AbstractPlayerCommand {

        public Unlink() {
            super("unlink", "server.commands.averagediscord.link.unlink.desc");
        }

        @Override
        protected void execute(@NotNull CommandContext commandContext, @NotNull Store<EntityStore> store, @NotNull Ref<EntityStore> ref, @NotNull PlayerRef playerRef, @NotNull World world) {
            var database = AverageDiscord.databaseService;
            var linkTable = database.getTable(UserLink.class);
            var playerUuid = commandContext.sender().getUuid().toString();

            // find player
            try {
                var link = linkTable.queryBuilder().where().eq("hytaleUserId", playerUuid).queryForFirst();
                if (link == null) {
                    commandContext.sendMessage(Message.translation("server.commands.averagediscord.link.unlink.nolink"));
                    return;
                }

                linkTable.delete(link);
                database.save();
                commandContext.sendMessage(Message.translation("server.commands.averagediscord.link.unlink.success"));
            } catch (SQLException e) {
                commandContext.sendMessage(Message.translation("server.commands.averagediscord.link.unlink.failed"));
                throw new RuntimeException(e);
            }
        }
    }

    protected static class Username extends AbstractPlayerCommand {
        public RequiredArg<String> username;

        public Username() {
            super("username", "server.commands.averagediscord.link.username.desc");
            this.username = this.withRequiredArg("username", "server.commands.averagediscord.link.username.argdesc", ArgTypes.STRING);
        }

        @Override
        protected void execute(@NotNull CommandContext commandContext, @NotNull Store<EntityStore> store, @NotNull Ref<EntityStore> ref, @NotNull PlayerRef playerRef, @NotNull World world) {
            var username = this.username.get(commandContext);
            if (username.startsWith("@")) {
                username = username.substring(1);
            }

            // try to find user on guild
            if (DiscordBotService.isRunning() && DiscordBotService.GUILD.get() != null) {
                var guild = DiscordBotService.GUILD.get();
                String finalUsername = username;

                var member = guild.findMembers(m -> m.getUser().getName().equalsIgnoreCase(finalUsername) ||
                        (m.getNickname() != null && m.getNickname().equalsIgnoreCase(finalUsername))).onSuccess(members -> {
                    if (members.isEmpty()) {
                        commandContext.sendMessage(Message.translation("server.commands.averagediscord.link.username.notfound").param("username", finalUsername));
                    }
                }).get().getFirst();

                if (member == null) {
                    commandContext.sendMessage(Message.translation("server.commands.averagediscord.link.username.notfound").param("username", username));
                    return;
                }

                String hytalePlayerName = commandContext.sender().getUsername();
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage(Message.translation("server.commands.averagediscord.link.username.confirm").param("username", hytalePlayerName).getAnsiMessage()).queue((message) -> {
                        message.addReaction(Emoji.fromUnicode("✅")).queue();
                        DiscordBotService.PENDING_LINKS.add(new PendingLink(commandContext.sender().getUuid(), member.getIdLong(), message.getIdLong()));
                        commandContext.sendMessage(Message.translation("server.commands.averagediscord.link.username.checkdm").param("username", finalUsername));
                    });
                });
            } else {
                commandContext.sendMessage(Message.translation("server.commands.averagediscord.link.username.discordnotrunning"));
            }
        }
    }
}
