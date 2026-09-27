package club.sitmc.fumiFabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
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
        config = FumiConfig.load(configPath);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> CompletableFuture.runAsync(() -> {
            try {
                commonLogic.init(config.url, config.token, config.subject);
                commonLogic.listenToRemote(config.sourceName, msg -> {
                    String formatted = Common.formatChat(config.chatFormat, msg);
                    server.execute(() -> server.getPlayerList().broadcastSystemMessage(Component.literal(formatted), false));
                });
                LOGGER.info("Fumi-Fabric connected to NATS: {}", config.url);
            } catch (Exception e) {
                LOGGER.error("Failed to connect to NATS", e);
            }
        }));

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) ->
                CompletableFuture.runAsync(() -> commonLogic.broadcastToRemote(
                        config.sourceName,
                        sender.getName().getString(),
                        message.signedContent()
                ))
        );

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> commonLogic.stop());
    }
}
