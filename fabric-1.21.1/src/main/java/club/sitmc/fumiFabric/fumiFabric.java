package club.sitmc.fumiFabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class fumiFabric implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fumi-fabric");
    private final Common commonLogic = new Common();
    private FumiConfig config;

    @Override
    public void onInitialize() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("fumi-fabric.json");
        this.config = FumiConfig.load(configPath);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CompletableFuture.runAsync(() -> {
                try {
                    commonLogic.init(config.url, config.token, config.subject);
                    commonLogic.listenToRemote(config.sourceName, (username, content) -> {
                        String colorized = config.chatFormat
                                .replace("{source}", config.sourceName)
                                .replace("{username}", username)
                                .replace("{message}", content)
                                .replace('&', '§');

                        server.execute(() -> {
                            server.getPlayerManager().broadcast(Text.literal(colorized), false);
                        });
                    });
                    LOGGER.info("Fumi-Fabric connected to NATS: {}", config.url);
                } catch (Exception e) {
                    LOGGER.error("Failed to connect to NATS", e);
                }
            });
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            String playerName = sender.getName().getString();
            String content = message.getContent().getString();
            CompletableFuture.runAsync(() -> {
                commonLogic.broadcastToRemote(config.sourceName, playerName, content);
            });
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            commonLogic.stop();
        });
    }
}