package github.renderbr.hytale.config;

import github.renderbr.hytale.config.obj.DiscordBridgeConfiguration;
import util.ConfigObjectProvider;

public final class DiscordBridgeConfigurationProvider
        extends ConfigObjectProvider<DiscordBridgeConfiguration> {
    private static final String DISCORD_BRIDGE_FILE = "discord_bridge.json";

    public DiscordBridgeConfigurationProvider() {
        super(DISCORD_BRIDGE_FILE, DiscordBridgeConfiguration.class);
    }
}
