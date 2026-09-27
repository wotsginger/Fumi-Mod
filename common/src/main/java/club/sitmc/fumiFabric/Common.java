package club.sitmc.fumiFabric;

import io.nats.client.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;

public class Common {
    private Connection nc;
    private String subject;

    public record RemoteMessage(String source, String username, String message) {}

    public void init(String url, String token, String subject) throws Exception {
        this.subject = subject;

        Options.Builder builder = new Options.Builder()
                .server(url)
                .maxReconnects(-1)
                .reconnectWait(Duration.ofSeconds(2));

        if (token != null && !token.isEmpty()) {
            builder.token(token.toCharArray());
        }

        this.nc = Nats.connect(builder.build());
    }

    public void listenToRemote(String selfSource, Consumer<RemoteMessage> handler) {
        if (nc == null) return;

        Dispatcher dispatcher = nc.createDispatcher(msg -> {
            try {
                String json = new String(msg.getData(), StandardCharsets.UTF_8);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

                String source = obj.has("source") ? obj.get("source").getAsString() : "unknown";
                if (source.equals(selfSource)) return;

                String username = obj.has("username") ? obj.get("username").getAsString() : "Unknown";
                String message = obj.has("message") ? obj.get("message").getAsString() : "";

                handler.accept(new RemoteMessage(source, username, message));
            } catch (Exception ignored) {}
        });

        dispatcher.subscribe(this.subject);
    }

    public void broadcastToRemote(String source, String username, String message) {
        if (nc == null || nc.getStatus() != Connection.Status.CONNECTED) return;

        JsonObject payload = new JsonObject();
        payload.addProperty("source", source);
        payload.addProperty("username", username);
        payload.addProperty("message", message);

        nc.publish(this.subject, payload.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void stop() {
        try {
            if (nc != null) nc.close();
        } catch (Exception ignored) {}
    }

    public static String formatChat(String template, RemoteMessage message) {
        return template.replace("{source}", message.source())
                .replace("{username}", message.username())
                .replace("{message}", message.message())
                .replace('&', '\u00a7');
    }
}
