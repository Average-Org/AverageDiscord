package github.renderbr.hytale.config;

import github.renderbr.hytale.config.obj.DiscordBridgeConfiguration;
import util.ConfigObjectProvider;

public final class DiscordBridgeConfigurationProvider
        extends ConfigObjectProvider<DiscordBridgeConfiguration> {

    public DiscordBridgeConfigurationProvider() {
        super("discord_bridge.json", DiscordBridgeConfiguration.class);
    }
}
