package club.sitmc.fumiFabric.neoforge;

import club.sitmc.fumiFabric.Common;
import club.sitmc.fumiFabric.FumiConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Mod("fumi_neoforge")
public class FumiNeoForge {
    public static final Logger LOGGER = LoggerFactory.getLogger("fumi-neoforge");

    private final Common commonLogic = new Common();
    private FumiConfig config;

    public FumiNeoForge() {
        System.setProperty("io.netty.noNative", "true");
        System.setProperty("io.netty.transport.noNative", "true");

        Path configPath = FMLPaths.CONFIGDIR.get()
                .resolve("fumi-neoforge.json");

        this.config = FumiConfig.load(configPath);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();

        CompletableFuture.runAsync(() -> {
            try {
                commonLogic.init(config.url, config.token, config.subject);

                commonLogic.listenToRemote(config.sourceName, msg -> {
                    String text = Common.formatChat(config.chatFormat, msg);

                    server.execute(() ->
                            server.getPlayerList()
                                    .broadcastSystemMessage(
                                            Component.literal(text),
                                            false
                                    )
                    );
                });

                LOGGER.info("Fumi-NeoForge connected to NATS: {}", config.url);
            } catch (Exception e) {
                LOGGER.error("Failed to connect to NATS", e);
            }
        });
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        String username = player.getScoreboardName();
        String content = event.getRawText();

        CompletableFuture.runAsync(() ->
                commonLogic.broadcastToRemote(
                        config.sourceName,
                        username,
                        content
                )
        );
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        commonLogic.stop();
    }
}
