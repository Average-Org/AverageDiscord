package github.renderbr.hytale.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.registries.ProviderRegistry;
import github.renderbr.hytale.services.DiscordBotService;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class DiscordBridgeCommand extends AbstractCommandCollection {

    public DiscordBridgeCommand() {
        super("discordbridge", "server.commands.averagediscord.cmd.desc");
        this.addAliases("gm", "agm", "groupman");
        this.addSubCommand(new ReloadCommand());
    }

    protected static class ReloadCommand extends CommandBase {
        public ReloadCommand() {
            super("reload", "server.commands.averagediscord.reload.desc");
        }

        @Override
        protected void executeSync(@NotNull CommandContext commandContext) {
            ProviderRegistry.discordBridgeConfigProvider.syncLoad();

            if (AverageDiscord.instance.getJdaInstance() != null) {
                AverageDiscord.instance.stop();
            }

            try {
                DiscordBotService.start();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            AverageDiscord.instance = DiscordBotService.getInstance();
            commandContext.sendMessage(Message.translation("server.commands.averagediscord.reload.success").color(Color.GREEN.brighter()));
        }
    }
}
