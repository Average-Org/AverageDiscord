package github.renderbr.hytale.config.obj;

public class DiscordBridgeConfiguration {
    public String botToken = "bot_token_here";

    public DiscordChannelConfiguration[] channels = new DiscordChannelConfiguration[]{
            new DiscordChannelConfiguration("channel_id_here", new ChannelOutputTypes[]{ChannelOutputTypes.ALL})
    };
    public String discordIngamePrefix = "&9[Discord] ";

    public String botActivityMessage = "Playing Hytale!";
    public Boolean showActivePlayerCount = true;
    public Boolean stripLinksInChat = true;
    public Boolean stripPingsInChat = true;
}
