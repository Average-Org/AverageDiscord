
package github.renderbr.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import github.renderbr.hytale.commands.discord.CommandHandler;
import github.renderbr.hytale.registries.CommandRegistry;
import github.renderbr.hytale.registries.ListenerRegistry;
import github.renderbr.hytale.registries.ProviderRegistry;
import github.renderbr.hytale.services.DiscordBotService;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import util.PathUtils;

public class AverageDiscord extends JavaPlugin {

    public static DiscordBotService instance;
    public AverageDiscord(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        JDALogger.setFallbackLoggerEnabled(false);
        PathUtils.setModDirectoryName("AverageDiscord");

        CommandRegistry.registerCommands(this.getCommandRegistry());
        ProviderRegistry.registerProviders();
        ListenerRegistry.registerListeners(this.getEventRegistry());
    }
}