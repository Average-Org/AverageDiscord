package github.renderbr.hytale.services;

import com.hypixel.hytale.server.core.universe.Universe;
import github.renderbr.hytale.commands.discord.CommandHandler;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.config.obj.DiscordBridgeConfiguration;
import github.renderbr.hytale.listeners.ServerStateListener;
import github.renderbr.hytale.registries.ProviderRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import util.ColorUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordBotService extends ListenerAdapter implements EventListener {
    private static DiscordBotService instance;
    private JDA jdaInstance;

    public Map<ChannelOutputTypes, ArrayList<MessageChannel>> channels = new HashMap<>() {
        {
            put(ChannelOutputTypes.ALL, new ArrayList<>());
            put(ChannelOutputTypes.CHAT, new ArrayList<>());
            put(ChannelOutputTypes.JOIN_LEAVE, new ArrayList<>());
            put(ChannelOutputTypes.SERVER_STATE, new ArrayList<>());
            put(ChannelOutputTypes.INTERNAL_LOG, new ArrayList<>());
            put(ChannelOutputTypes.DESC_STATUS, new ArrayList<>());
        }
    };

    public ArrayList<MessageChannel> GetOfType(ChannelOutputTypes type) {
        ArrayList<MessageChannel> channelsOfType = new ArrayList<>();

        // if not internal log, add all channels
        if (type != ChannelOutputTypes.INTERNAL_LOG) {
            channelsOfType.addAll(this.channels.get(ChannelOutputTypes.ALL));
        }

        channelsOfType.addAll(this.channels.get(type));
        return channelsOfType;
    }

    public void SendMessageToType(@Nonnull ChannelOutputTypes type, @Nonnull String message) {
        for (var channel : GetOfType(type)) {
            channel.sendMessage(message).queue();
        }
    }

    private ScheduledExecutorService scheduler;

    EnumSet<GatewayIntent> intents = EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);

    private DiscordBotService() {

    }

    public static DiscordBotService getInstance() {
        return instance;
    }

    protected JDA buildNewInstance() {
        var configuration = ProviderRegistry.discordBridgeConfigProvider.config;

        return JDABuilder
                .createLight(ProviderRegistry.discordBridgeConfigProvider.config.botToken, intents)
                .setActivity(Activity.customStatus(configuration.botActivityMessage))
                .build();
    }

    public JDA getJdaInstance() {
        return this.jdaInstance;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        var configuration = ProviderRegistry.discordBridgeConfigProvider.config;
        User author = event.getAuthor();
        Message message = event.getMessage();

        if (author.isBot()) return;

        // if the id is not any of the "chat" or "all" channels, return
        if (!channels.get(ChannelOutputTypes.CHAT).contains(event.getChannel()) &&
                !channels.get(ChannelOutputTypes.ALL).contains(event.getChannel())) {
            return;
        }

        Universe.get().sendMessage(com.hypixel.hytale.server.core.Message.join(ColorUtils.parseColorCodes(configuration.discordIngamePrefix), com.hypixel.hytale.server.core.Message.raw(author.getName() + ": " + message.getContentDisplay())));
    }

    public void updateDiscordInformation() {
        DiscordBridgeConfiguration config = ProviderRegistry.discordBridgeConfigProvider.config;
        if (config.showActivePlayerCount) {
            updateActivityPlayerCount();
        } else {
            updateActivityDefault();
        }
        updateChannelDescriptionStatuses();
    }

    public void updateActivityPlayerCount() {
        int playerCount = Universe.get().getPlayerCount();

        var config = ProviderRegistry.discordBridgeConfigProvider.config;
        var playerCountMessage = com.hypixel.hytale.server.core.Message.translation("server.activity.averagediscord.playercount").param("players", playerCount).getAnsiMessage();
        var activityCount = Activity.customStatus(playerCountMessage);

        if (!config.botActivityMessage.isEmpty()) {
            activityCount = Activity.customStatus(config.botActivityMessage + " | " + playerCountMessage);
        }

        getInstance().getJdaInstance().getPresence().setActivity(activityCount);
    }

    private void updateActivityDefault() {
        var configuration = ProviderRegistry.discordBridgeConfigProvider.config;
        var defaultActivity = Activity.customStatus(configuration.botActivityMessage);
        getInstance().getJdaInstance().getPresence().setActivity(defaultActivity);
    }

    public void updateChannelDescriptionStatuses() {
        GetOfType(ChannelOutputTypes.DESC_STATUS).forEach(channel -> {
            if (channel.getType().isGuild()) {
                ((TextChannel) channel).getManager().setTopic(
                        com.hypixel.hytale.server.core.Message.translation("server.bot.averagediscord.descstatus")
                                .param("players", Universe.get().getPlayerCount())
                                .getAnsiMessage()
                ).queue();
            }
        });
    }

    public static DiscordBotService start() throws InterruptedException {
        instance = new DiscordBotService();
        var configuration = ProviderRegistry.discordBridgeConfigProvider.config;
        instance.scheduler = Executors.newSingleThreadScheduledExecutor();
        instance.jdaInstance = instance.buildNewInstance();
        instance.jdaInstance.awaitReady();

        Guild guild = null;
        for (var channel : configuration.channels) {
            for (var type : channel.type) {
                var textChannel = instance.jdaInstance.getTextChannelById(channel.channelId);

                instance.channels.get(type).add(textChannel);

                if (textChannel != null) {
                    guild = textChannel.getGuild();
                }
            }
        }

        instance.jdaInstance.addEventListener(instance);
        var cmdHandler = new CommandHandler();

        if (guild != null) {
            cmdHandler.registerCommands(guild);
        }

        instance.jdaInstance.addEventListener(cmdHandler);

        instance.scheduler.scheduleAtFixedRate(() -> {
            instance.updateDiscordInformation();
        }, 2, 10, TimeUnit.MINUTES);

        return instance;
    }

    public void stop() {
        this.jdaInstance.shutdown();
        this.scheduler.shutdown();
    }
}
