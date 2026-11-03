package uy.edu.isbo.control.agent;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Hace las dos solicitudes del protocolo: consultar y confirmar órdenes. */
final class AgentClient {
    record Offer(long id, long delayMillis) {}

    private final HttpClient http;
    private final AgentConfig config;

    AgentClient(HttpClient http, AgentConfig config) {
        this.http = http;
        this.config = config;
    }

    Offer poll(String name) throws Exception {
        String body = "id=" + encode(config.id()) + "&name=" + encode(name);
        return parse(post("/agent-api/poll", body));
    }

    static Offer parse(String response) {
        if (response.equals("none")) return null;

        var fields = response.split("\\|", -1);
        if (fields.length != 3 || !fields[0].equals("shutdown")) {
            throw new IllegalStateException("Orden desconocida");
        }
        long id = Long.parseLong(fields[1]);
        long delay = Long.parseLong(fields[2]);
        if (id <= 0 || delay < 0 || delay > 60_000) {
            throw new IllegalStateException("Orden fuera de plazo");
        }
        return new Offer(id, delay);
    }

    boolean acknowledge(long commandId) throws Exception {
        String body = "id=" + encode(config.id()) + "&commandId=" + commandId;
        return post("/agent-api/ack", body).equals("accepted");
    }

    private String post(String path, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(config.server() + path))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + config.token())
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        var response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Servidor respondió HTTP " + response.statusCode());
        }
        return response.body().trim();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
