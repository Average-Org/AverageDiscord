package github.renderbr.hytale.registries;

import github.renderbr.hytale.config.DiscordBridgeConfigurationProvider;

public class ProviderRegistry {
    public static DiscordBridgeConfigurationProvider discordBridgeConfigProvider;

    public static void registerProviders(){
        discordBridgeConfigProvider = new DiscordBridgeConfigurationProvider();
    }
}
