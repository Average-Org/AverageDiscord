package github.renderbr.hytale.services;

import com.hypixel.hytale.server.core.universe.Universe;
import github.renderbr.hytale.config.obj.DiscordBridgeConfiguration;
import github.renderbr.hytale.registries.ProviderRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import util.ColorUtils;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordBotService extends ListenerAdapter implements EventListener {
    private static DiscordBotService instance;
    private JDA jdaInstance;
    private MessageChannel chatChannel;
    private ScheduledExecutorService scheduler;
    private boolean showPlayerCount = false;

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

    public MessageChannel getChatChannel() {
        return this.chatChannel;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        var configuration = ProviderRegistry.discordBridgeConfigProvider.config;
        User author = event.getAuthor();
        Message message = event.getMessage();

        if(author.isBot()) return;
        if(event.getChannel().getIdLong() != Long.parseLong(configuration.mainChatChannelId)) return;

        Universe.get().sendMessage(com.hypixel.hytale.server.core.Message.join(ColorUtils.parseColorCodes(configuration.discordIngamePrefix), com.hypixel.hytale.server.core.Message.raw(author.getName() + ": " + message.getContentDisplay())));
    }

    private void updateActivityPlayerCount() {
        var defaultWorld = Universe.get().getDefaultWorld();
        if (defaultWorld != null) {
            int playerCount = defaultWorld.getPlayerCount();
            var activityCount = Activity.customStatus("Player count: " + playerCount);
            getInstance().getJdaInstance().getPresence().setActivity(activityCount);
        }
    }

    private void updateActivityDefault() {
        var configuration = ProviderRegistry.discordBridgeConfigProvider.config;
        var defaultActivity = Activity.customStatus(configuration.botActivityMessage);
        getInstance().getJdaInstance().getPresence().setActivity(defaultActivity);
    }

    public static void start() throws InterruptedException {
        instance = new DiscordBotService();
        var configuration = ProviderRegistry.discordBridgeConfigProvider.config;
        instance.scheduler = Executors.newSingleThreadScheduledExecutor();
        instance.jdaInstance = instance.buildNewInstance();
        instance.jdaInstance.awaitReady();

        instance.chatChannel = instance.jdaInstance.getChannelById(MessageChannel.class, configuration.mainChatChannelId);
        if (instance.chatChannel == null) {
            throw new IllegalStateException("The provided main chat channel ID is invalid!");
        }

        instance.jdaInstance.addEventListener(instance);

        instance.scheduler.scheduleAtFixedRate(() -> {
            if (instance.showPlayerCount) {
                instance.updateActivityDefault();
            } else {
                instance.updateActivityPlayerCount();
            }
            instance.showPlayerCount = !instance.showPlayerCount;
        }, 10, 10, TimeUnit.MINUTES);
    }

    public void stop(){
        this.jdaInstance.shutdown();
        this.scheduler.shutdown();
    }
}
