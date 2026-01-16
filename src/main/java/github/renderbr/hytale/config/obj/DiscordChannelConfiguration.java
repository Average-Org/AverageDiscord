package github.renderbr.hytale.config.obj;

public class DiscordChannelConfiguration {
    public String channelId;
    public ChannelOutputTypes[] type = new ChannelOutputTypes[]{};

    public DiscordChannelConfiguration(String channelId, ChannelOutputTypes[] type) {
        this.channelId = channelId;
        this.type = type;
    }
}

