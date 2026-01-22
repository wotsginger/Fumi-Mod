package club.sitmc.fumiFabric;

import io.nats.client.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

public class Common {
    private Connection nc;
    private String subject;

    public void init(String url, String token, String subject) throws Exception {
        this.subject = subject;
        Options.Builder builder = new Options.Builder()
                .server(url)
                .maxReconnects(-1)
                .reconnectWait(java.time.Duration.ofSeconds(2));

        if (token != null && !token.isEmpty()) {
            builder.token(token.toCharArray());
        }

        this.nc = Nats.connect(builder.build());
    }

    public void listenToRemote(String currentSource, BiConsumer<String, String> messageHandler) {
        if (nc == null) return;
        Dispatcher d = nc.createDispatcher((msg) -> {
            try {
                String jsonStr = new String(msg.getData(), StandardCharsets.UTF_8);
                JsonObject data = JsonParser.parseString(jsonStr).getAsJsonObject();

                if (!currentSource.equals(data.get("source").getAsString())) {
                    String user = data.has("username") ? data.get("username").getAsString() : "Unknown";
                    String content = data.has("message") ? data.get("message").getAsString() : "";
                    messageHandler.accept(user, content);
                }
            } catch (Exception ignored) {}
        });
        d.subscribe(this.subject);
    }

    public void broadcastToRemote(String source, String username, String message) {
        if (nc != null && nc.getStatus() == Connection.Status.CONNECTED) {
            JsonObject payload = new JsonObject();
            payload.addProperty("source", source);
            payload.addProperty("username", username);
            payload.addProperty("message", message);
            nc.publish(this.subject, payload.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    public void stop() {
        try {
            if (nc != null) nc.close();
        } catch (Exception ignored) {}
    }
}