package github.renderbr.hytale.services;

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

/**
 * Service responsible for managing the Discord bot connection and message routing.
 */
public class DiscordBotService extends ListenerAdapter implements EventListener {
    private static final Set<GatewayIntent> INTENTS =
            Collections.unmodifiableSet(EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT));

    private static final AtomicReference<DiscordBotService> instance = new AtomicReference<>();
    private static final Object INIT_LOCK = new Object();

    /**
     * Initializes the Discord bot service.
     * @return A future that completes with the bot service instance.
     */
    public static CompletableFuture<DiscordBotService> init() {
        if (instance.get() != null) return CompletableFuture.completedFuture(instance.get());

        return CompletableFuture.supplyAsync(() -> {
            synchronized (INIT_LOCK) {
                if (instance.get() != null) return instance.get();
                try {
                    DiscordBotService service = new DiscordBotService();
                    instance.set(service);
                    AverageDiscord.LOGGER.at(Level.INFO).log("Discord Bot started successfully.");
                    return service;
                } catch (Exception e) {
                    AverageDiscord.LOGGER.at(Level.SEVERE).log("Failed to start Discord bot", e);
                    throw new CompletionException(e);
                }
            }
        });
    }

    /**
     * Restarts the Discord bot service.
     * @return A future that completes when the restart is finished.
     */
    public static CompletableFuture<DiscordBotService> restart() {
        return CompletableFuture.runAsync(() -> {
            AverageDiscord.LOGGER.at(Level.INFO).log("Restarting Discord Bot...");
            DiscordBotService current = instance.getAndSet(null);
            if (current != null) current.stop();
        }).thenCompose(v -> init());
    }

    /**
     * Checks if the bot is currently running and connected to Discord.
     * @return true if connected.
     */
    public static boolean isRunning() {
        var service = instance.get();
        return service != null && service.jdaInstance != null && service.jdaInstance.getStatus() == JDA.Status.CONNECTED;
    }

    public static DiscordBotService getInstance() {
        return instance.get();
    }

    private final Map<ChannelOutputTypes, Set<MessageChannel>> channels = new EnumMap<>(ChannelOutputTypes.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final JDA jdaInstance;
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();

    private DiscordBotService() throws InterruptedException {
        for (var type : ChannelOutputTypes.values()) channels.put(type, ConcurrentHashMap.newKeySet());

        var config = ProviderRegistry.discordBridgeConfigProvider.getConfig();
        if (config.channels == null || config.channels.length == 0) {
            throw new IllegalStateException(Message.translation("server.error.averagediscord.nochannels").getAnsiMessage());
        }

        jdaInstance = buildNewInstance(AverageDiscord.getDiscordBotToken());
        if (!jdaInstance.awaitReady().getStatus().equals(JDA.Status.CONNECTED)) {
            throw new IllegalStateException("JDA failed to connect");
        }

        Guild guild = null;
        for (var channelConfig : config.channels) {
            var textChannel = jdaInstance.getTextChannelById(channelConfig.channelId);
            if (textChannel == null) continue;
            for (var type : channelConfig.type) channels.get(type).add(textChannel);
            if (guild == null) guild = textChannel.getGuild();
        }

        jdaInstance.addEventListener(this);
        CommandHandler cmdHandler = new CommandHandler();
        if (guild != null) cmdHandler.registerCommands(guild);
        jdaInstance.addEventListener(cmdHandler);

        scheduler.scheduleAtFixedRate(this::updateDiscordInformation, 2, 10, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::processLogQueue, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Retrieves channels mapped to a specific output type, including 'ALL' channels where applicable.
     * @param type The output type.
     * @return A collection of appropriate message channels.
     */
    public Collection<MessageChannel> getAppropriateChannels(ChannelOutputTypes type) {
        if (type == ChannelOutputTypes.INTERNAL_LOG) return channels.get(type);
        Set<MessageChannel> result = new HashSet<>(channels.get(ChannelOutputTypes.ALL));
        result.addAll(channels.get(type));
        return result;
    }

    /**
     * Routes a message to appropriate channels based on its type.
     * @param type The type of message.
     * @param message The raw message string.
     * @param block Whether to wait for the message to be sent.
     */
    public void sendMessageAppropriately(@Nonnull ChannelOutputTypes type, @Nonnull String message, boolean block) {
        if (type == ChannelOutputTypes.INTERNAL_LOG) {
            String clean = (message.startsWith("```ansi\n") && message.endsWith("```")) 
                ? message.substring(8, message.length() - 3) : message;
            logQueue.offer(clean);
            return;
        }

        getAppropriateChannels(type).forEach(channel -> {
            var action = channel.sendMessage(message);
            if (block) action.complete(); else action.queue(null, e -> logError(channel.getId(), e));
        });
    }

    public void sendMessageAppropriately(@Nonnull ChannelOutputTypes type, @Nonnull String message) {
        sendMessageAppropriately(type, message, false);
    }

    /**
     * Sends an embed message to appropriate channels.
     * @param type The type of message.
     * @param embed The message embed.
     */
    public void sendMessageAppropriately(@Nonnull ChannelOutputTypes type, @Nonnull MessageEmbed embed) {
        getAppropriateChannels(type).forEach(c -> c.sendMessageEmbeds(embed).queue(null, e -> logError(c.getId(), e)));
    }

    private void processLogQueue() {
        if (logQueue.isEmpty()) return;
        var logChannels = getAppropriateChannels(ChannelOutputTypes.INTERNAL_LOG);
        if (logChannels.isEmpty()) { logQueue.clear(); return; }

        List<String> batch = new ArrayList<>();
        int length = 0;
        while (!logQueue.isEmpty() && batch.size() < 20) {
            String msg = logQueue.poll();
            if (msg == null) break;
            if (length + msg.length() + 1 > 1900) { sendBatch(batch, logChannels); batch.clear(); length = 0; }
            batch.add(msg);
            length += msg.length() + 1;
        }
        if (!batch.isEmpty()) sendBatch(batch, logChannels);
    }

    private void sendBatch(List<String> batch, Collection<MessageChannel> logChannels) {
        String combined = "```ansi\n" + String.join("\n", batch) + "```";
        logChannels.forEach(c -> c.sendMessage(combined).queue(null, e -> logError(c.getId(), e)));
    }

    private void logError(String channelId, Throwable error) {
        AverageDiscord.LOGGER.at(Level.SEVERE).log("Failed to send to channel " + channelId, error);
    }

    protected JDA buildNewInstance(String token) {
        var config = ProviderRegistry.discordBridgeConfigProvider.getConfig();
        return JDABuilder.createLight(token, INTENTS)
                .setActivity(Activity.customStatus(config.botActivityMessage))
                .build();
    }

    public JDA getJdaInstance() {
        return jdaInstance;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getMessage().isWebhookMessage()) return;
        if (!channels.get(ChannelOutputTypes.CHAT).contains(event.getChannel()) &&
            !channels.get(ChannelOutputTypes.ALL).contains(event.getChannel())) return;

        var config = ProviderRegistry.discordBridgeConfigProvider.getConfig();
        Universe.get().sendMessage(Message.join(
                ColorUtils.parseColorCodes(config.discordIngamePrefix),
                Message.raw(event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay())));
    }

    /**
     * Updates bot activity and channel descriptions based on server state.
     */
    public void updateDiscordInformation() {
        if (!isRunning()) return;
        var config = ProviderRegistry.discordBridgeConfigProvider.getConfig();
        var status = config.showActivePlayerCount 
            ? Activity.customStatus((config.botActivityMessage.isEmpty() ? "" : config.botActivityMessage + " | ") + 
                Message.translation("server.activity.averagediscord.playercount")
                       .param("players", Universe.get().getPlayerCount()).getAnsiMessage())
            : Activity.customStatus(config.botActivityMessage);

        jdaInstance.getPresence().setActivity(status);
        updateChannelDescriptionStatuses();
    }

    private void updateChannelDescriptionStatuses() {
        String topic = Message.translation("server.bot.averagediscord.descstatus")
                .param("players", Universe.get().getPlayerCount()).getAnsiMessage();

        getAppropriateChannels(ChannelOutputTypes.DESC_STATUS).forEach(channel -> {
            if (channel instanceof TextChannel tc && !topic.equals(tc.getTopic())) {
                tc.getManager().setTopic(topic).queue(null, e -> 
                    AverageDiscord.LOGGER.at(Level.WARNING).log("Failed to update topic for " + tc.getId(), e));
            }
        });
    }

    /**
     * Shuts down the Discord bot and cleans up resources.
     */
    public void stop() {
        processLogQueue();
        scheduler.shutdown();
        try { if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) scheduler.shutdownNow(); } 
        catch (InterruptedException e) { scheduler.shutdownNow(); Thread.currentThread().interrupt(); }

        jdaInstance.shutdown();
        try { if (!jdaInstance.awaitShutdown(Duration.ofSeconds(5))) jdaInstance.shutdownNow(); } 
        catch (InterruptedException e) { jdaInstance.shutdownNow(); Thread.currentThread().interrupt(); }
        instance.set(null);
    }
}
