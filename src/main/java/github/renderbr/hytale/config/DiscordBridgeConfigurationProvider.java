package github.renderbr.hytale.config;

import com.google.gson.*;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;
import github.renderbr.hytale.config.obj.DiscordBridgeConfiguration;
import github.renderbr.hytale.registries.CommandRegistry;
import util.PathUtils;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class DiscordBridgeConfigurationProvider extends BlockingDiskFile {
    @Nonnull
    public DiscordBridgeConfiguration config = new DiscordBridgeConfiguration();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> broadcastTimer;

    Queue<String> messageQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();

    public DiscordBridgeConfigurationProvider() {
        var path = PathUtils.getPathForConfig("discord_bridge.json");
        super(path);

        PathUtils.initializeAndEnsurePathing(path, this);
    }

    @Override
    protected void read(BufferedReader bufferedReader) throws IOException {
        JsonObject root = JsonParser.parseReader(bufferedReader).getAsJsonObject();

        if (root.has("config")) {
            this.config = GSON.fromJson(root.get("config"), DiscordBridgeConfiguration.class);
        }
    }

    @Override
    protected void write(BufferedWriter bufferedWriter) throws IOException {
        JsonObject root = new JsonObject();

        root.add("config", GSON.toJsonTree(this.config));

        bufferedWriter.write(GSON.toJson(root));
    }

    @Override
    protected void create(@Nonnull BufferedWriter fileWriter) throws IOException {
        JsonObject root = new JsonObject();
        root.add("config", GSON.toJsonTree(this.config));
        fileWriter.write(GSON.toJson(root));
    }
}
