package akv5.acore.utils;

import akv5.acore.libs.Scheduler;
import org.bukkit.Bukkit;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Bot {

    private static final String WEBHOOK_URL = "https://gattinoland.fun/bot/gattinoland/sendMessage";

    public static void sendMessage(String message, List<String> chatIds) {
        Scheduler.doAsync(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(WEBHOOK_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                String jsonPayload = "{\"message\":\"" + escapeJson(message) + "\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Bukkit.getLogger().warning("Ошибка: " + responseCode);
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("Не удалось отправить сообщение: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    public static void sendMsg(String message) {
        sendMessage(message, null);
    }

    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '/': sb.append("\\/"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int)c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}