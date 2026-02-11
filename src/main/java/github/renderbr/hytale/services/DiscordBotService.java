package github.renderbr.hytale.services;

import com.hypixel.hytale.logger.backend.HytaleConsole;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import github.renderbr.hytale.AverageDiscord;
import github.renderbr.hytale.commands.discord.CommandHandler;
import github.renderbr.hytale.config.obj.ChannelOutputTypes;
import github.renderbr.hytale.registries.ProviderRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import util.ColorUtils;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class DiscordBotService extends ListenerAdapter implements EventListener {
    private static final Set<GatewayIntent> INTENTS =
            Collections.unmodifiableSet(EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT));

    private static final AtomicReference<DiscordBotService> instance = new AtomicReference<>();

    public static CompletableFuture<DiscordBotService> init() {
        if (instance.get() != null) {
            return CompletableFuture.completedFuture(instance.get());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                DiscordBotService service = new DiscordBotService();
                instance.set(service);
                AverageDiscord.LOGGER.at(Level.INFO).log("Discord Bot started successfully.");
                return service;
            } catch (Exception e) {
                AverageDiscord.LOGGER.at(Level.SEVERE).log("Failed to start Discord bot", e);
                throw new CompletionException(e);
            }
        });
    }

    public static CompletableFuture<DiscordBotService> restart() {
        return CompletableFuture.runAsync(() -> {
            AverageDiscord.LOGGER.at(Level.INFO).log("Stopping Discord Bot for restart...");

            DiscordBotService currentService = instance.get();
            if (currentService != null) {
                currentService.stop();
            }

            instance.set(null);

        }).thenCompose(v -> init());
    }

    public static boolean isRunning() {
        var service = instance.get();

        if (service == null) {
            return false;
        }

        var jda = service.getJdaInstance();
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    public static DiscordBotService getInstance() {
        return instance.get();
    }

    private final Map<ChannelOutputTypes, List<MessageChannel>> channels = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final JDA jdaInstance;

    private DiscordBotService() throws InterruptedException {
        for (var type : ChannelOutputTypes.values()) {
            channels.put(type, new CopyOnWriteArrayList<>());
        }

        var configuration = ProviderRegistry.discordBridgeConfigProvider.getConfig();

        if (configuration.channels.length == 0) {
            throw new IllegalStateException(Message.translation("server.error.averagediscord.nochannels").getAnsiMessage());
        }

        jdaInstance = buildNewInstance(AverageDiscord.getDiscordBotToken());
        scheduler = Executors.newSingleThreadScheduledExecutor();
        jdaInstance.awaitReady();

        Guild guild = null;
        for (var channel : configuration.channels) {
            var textChannel = jdaInstance.getTextChannelById(channel.channelId);
            if (textChannel == null) continue;

            for (var type : channel.type) {
                channels.get(type).add(textChannel);
            }

            guild = textChannel.getGuild();
        }

        jdaInstance.addEventListener(this);
        var cmdHandler = new CommandHandler();

        if (guild != null) {
            cmdHandler.registerCommands(guild);
        }

        jdaInstance.addEventListener(cmdHandler);
        scheduler.scheduleAtFixedRate(this::updateDiscordInformation, 2, 10, TimeUnit.MINUTES);
    }

    /**
     * @param type The type of message to send.
     * @return A list of channels where it is appropriate to send the given type of message.
     */
    public List<MessageChannel> getAppropriateChannels(ChannelOutputTypes type) {
        if (type != ChannelOutputTypes.INTERNAL_LOG) {
            return this.channels.get(ChannelOutputTypes.ALL);
        }

        return this.channels.get(type);
    }

    /**
     * Sends a message to all channels where it is appropriate to send the given type of message.
     *
     * @param type    The type of message to send.
     * @param message The message to send.
     */
    public void sendMessageAppropriately(@Nonnull ChannelOutputTypes type, @Nonnull String message, Boolean block) {
        getAppropriateChannels(type).forEach(channel -> {
                    var msg = channel.sendMessage(message);

                    if (block) {
                        msg.complete();
                    } else {
                        msg.queue();
                    }
                }
        );
    }

    public void sendMessageAppropriately(@Nonnull ChannelOutputTypes type, @Nonnull String message) {
        sendMessageAppropriately(type, message, false);
    }

    public void sendMessageAppropriately(@Nonnull ChannelOutputTypes type, @Nonnull MessageEmbed message) {
        getAppropriateChannels(type).forEach(channel ->
                channel.sendMessageEmbeds(message).queue(
                        _ -> {
                        },
                        error -> AverageDiscord.LOGGER.at(Level.SEVERE).log("Failed to send to channel " + channel.getId(), error)
                )
        );
    }

    /**
     * Builds a new instance of JDA.
     *
     * @return A new instance of JDA.
     */
    protected JDA buildNewInstance(String token) {
        var configuration = ProviderRegistry.discordBridgeConfigProvider.getConfig();

        return JDABuilder
                .createLight(token, INTENTS)
                .setActivity(Activity.customStatus(configuration.botActivityMessage))
                .build();
    }

    /**
     * Gets the current instance of JDA.
     *
     * @return The current instance of JDA.
     */
    public JDA getJdaInstance() {
        return this.jdaInstance;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getMessage().isWebhookMessage()) return;

        var configuration = ProviderRegistry.discordBridgeConfigProvider.getConfig();
        var author = event.getAuthor();
        var message = event.getMessage();

        // if the id is not any of the "chat" or "all" channels, return
        if (!channels.get(ChannelOutputTypes.CHAT).contains(event.getChannel()) &&
                !channels.get(ChannelOutputTypes.ALL).contains(event.getChannel())) {
            return;
        }

        Universe.get()
                .sendMessage(com.hypixel.hytale.server.core.Message.join(
                        ColorUtils.parseColorCodes(configuration.discordIngamePrefix),
                        com.hypixel.hytale.server.core.Message.raw(author.getName() + ": " + message.getContentDisplay())));
    }

    /**
     * Updates the Discord bot activity and channel descriptions if appropriate.
     */
    public void updateDiscordInformation() {
        var jda = getJdaInstance();
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) return;

        var config = ProviderRegistry.discordBridgeConfigProvider.getConfig();
        var presence = jda.getPresence();

        Activity status;

        if (config.showActivePlayerCount) {
            int playerCount = Universe.get().getPlayerCount();

            var playerCountMessage = com.hypixel.hytale.server.core.Message.translation("server.activity.averagediscord.playercount")
                    .param("players", playerCount).getAnsiMessage();

            status = config.botActivityMessage.isEmpty()
                    ? Activity.customStatus(playerCountMessage) : Activity.customStatus(config.botActivityMessage + " | " + playerCountMessage);
        } else {
            status = Activity.customStatus(config.botActivityMessage);
        }

        presence.setActivity(status);
        updateChannelDescriptionStatuses();
    }

    /**
     * Updates channel descriptions where appropritae
     */
    public void updateChannelDescriptionStatuses() {
        int playerCount = Universe.get().getPlayerCount();
        String topic = Message.translation("server.bot.averagediscord.descstatus")
                .param("players", playerCount)
                .getAnsiMessage();

        getAppropriateChannels(ChannelOutputTypes.DESC_STATUS).forEach(channel -> {
            if (channel instanceof TextChannel textChannel) {
                if (!topic.equals(textChannel.getTopic())) {
                    textChannel.getManager().setTopic(topic).queue(
                            null,
                            err -> AverageDiscord.LOGGER.at(Level.WARNING).log("Rate limited or failed to update topic", err)
                    );
                }
            }
        });
    }

    public void stop() {
        this.scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        this.jdaInstance.shutdown();

        try {
            if (!jdaInstance.awaitShutdown(Duration.ofSeconds(5))) {
                jdaInstance.shutdownNow();
            }
        } catch (InterruptedException e) {
            jdaInstance.shutdownNow();
            Thread.currentThread().interrupt();
        }

        instance.set(null);
    }
}
