package github.renderbr.hytale.registries;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import github.renderbr.hytale.commands.DiscordBridgeCommand;

import java.util.List;

public class CommandRegistry {
    private static com.hypixel.hytale.server.core.command.system.CommandRegistry registry;

    public static final List<AbstractCommandCollection> REGISTERED_COMMANDS = List.of(
            new DiscordBridgeCommand()
    );


    public static void registerCommands(com.hypixel.hytale.server.core.command.system.CommandRegistry registry) {
        CommandRegistry.registry = registry;
        REGISTERED_COMMANDS.forEach(registry::registerCommand);
    }

    public static com.hypixel.hytale.server.core.command.system.CommandRegistry getHytaleCommandRegistry() {
        return CommandRegistry.registry;
    }
}
