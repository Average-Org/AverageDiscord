
package github.renderbr.hytale;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import github.renderbr.hytale.registries.CommandRegistry;
import github.renderbr.hytale.registries.ListenerRegistry;
import github.renderbr.hytale.registries.ProviderRegistry;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import util.PathUtils;

import java.util.Optional;

public class AverageDiscord extends JavaPlugin {

    public static final String DISCORD_BOT_TOKEN_ENV = "HYTALE_DISCORD_TOKEN";
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public AverageDiscord(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        JDALogger.setFallbackLoggerEnabled(false);
        PathUtils.setModDirectoryName("AverageDiscord");

        CommandRegistry.registerCommands(this.getCommandRegistry());
        ProviderRegistry.registerProviders();
        ListenerRegistry.registerListeners(this);
    }

    public static String getDiscordBotToken() {
        var token = Optional.ofNullable(System.getenv(DISCORD_BOT_TOKEN_ENV));

        if (token.isEmpty()) {
            token = Optional.of(ProviderRegistry.discordBridgeConfigProvider.getConfig().botToken);
        }

        if (token.get().isBlank() || token.get().equals("bot_token_here")) {
            throw new IllegalStateException(Message.translation("server.error.averagediscord.tokennotfound").getAnsiMessage());
        }

        return token.orElse("");
    }
}