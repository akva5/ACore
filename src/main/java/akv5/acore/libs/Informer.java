package akv5.acore.libs;

import akv5.acore.ACore;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Informer {

    // Bukkit
    public static void send(Player player, String message) {
        String prefix = ACore.getInstance().getInfoConfig().getString("prefix");
        if (prefix != null && player != null) {
            message = message.replace("{prefix}", prefix);

            if (isPlaceholderAPIEnabled()) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }

            message = Colors.set(message);
            player.sendMessage(message);
        }
    }

    public static void send(CommandSender sender, String message) {
        String prefix = ACore.getInstance().getInfoConfig().getString("prefix");
        if (prefix != null) {
            message = message.replace("{prefix}", prefix);
        }

        if (isPlaceholderAPIEnabled() && sender instanceof Player) {
            message = PlaceholderAPI.setPlaceholders((Player) sender, message);
        }

        message = Colors.set(message);
        sender.sendMessage(message);
    }

    // Velocity
    public static void send(com.velocitypowered.api.proxy.Player player, String message) {
        String prefix = ACore.getInstance().getInfoConfig().getString("prefix");
        if (prefix != null && player != null) {
            message = message.replace("{prefix}", prefix);
        }
        message = Colors.set(message);
        Component component = LegacyComponentSerializer.legacySection().deserialize(message);
        player.sendMessage(component);
    }

    public static void send(CommandSource source, String message) {
        String prefix = ACore.getInstance().getInfoConfig().getString("prefix");
        if (prefix != null) {
            message = message.replace("{prefix}", prefix);
        }
        message = Colors.set(message);
        Component component = LegacyComponentSerializer.legacySection().deserialize(message);
        source.sendMessage(component);
    }

    // Title Bukkit
    public static void sendTitle(org.bukkit.entity.Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 70, 20);
    }

    public static void sendTitle(org.bukkit.entity.Player player, String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
        if (isPlaceholderAPIEnabled() && player != null) {
            title = PlaceholderAPI.setPlaceholders(player, title);
            subtitle = PlaceholderAPI.setPlaceholders(player, subtitle);

            title = Colors.set(title);
            subtitle = Colors.set(subtitle);
            player.sendTitle(title, subtitle, fadeInTime, stayTime, fadeOutTime);
        }
    }

    // Title Velocity
    public static void sendTitle(com.velocitypowered.api.proxy.Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 70, 20);
    }

    public static void sendTitle(com.velocitypowered.api.proxy.Player player, String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
        if (player != null) {
            title = Colors.set(title);
            subtitle = Colors.set(subtitle);

            Component titleComponent = LegacyComponentSerializer.legacySection().deserialize(title);
            Component subtitleComponent = LegacyComponentSerializer.legacySection().deserialize(subtitle);

            player.sendMessage(titleComponent.append(Component.newline()).append(subtitleComponent));
        }
    }

    // Logs
    public static void sendInfo(String message) {
        if (message != null && !message.isEmpty()) {
            if (isPlaceholderAPIEnabled()) {
                message = PlaceholderAPI.setPlaceholders(null, message);
            }
            Bukkit.getLogger().info(message);
        }
    }

    public static void sendWarn(String message) {
        if (message != null && !message.isEmpty()) {
            if (isPlaceholderAPIEnabled()) {
                message = PlaceholderAPI.setPlaceholders(null, message);
            }
            Bukkit.getLogger().warning(message);
        }
    }

    // HTTP
    public static String url(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "";
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            connection.disconnect();
            return response.toString();
        } catch (Exception e) {
            return "";
        }
    }

    // Parsing
    public static BaseComponent[] parseComponent(String message) {
        if (message == null || message.isEmpty()) {
            return new BaseComponent[]{new TextComponent("")};
        }
        try {
            return ComponentSerializer.parse(message);
        } catch (Exception e) {
            return new BaseComponent[]{new TextComponent(message)};
        }
    }

    public static List<BaseComponent[]> parseComponent(List<String> messages) {
        List<BaseComponent[]> result = new ArrayList<>();
        if (messages == null || messages.isEmpty()) {
            result.add(new BaseComponent[]{new TextComponent("")});
            return result;
        }
        for (String message : messages) {
            result.add(parseComponent(message));
        }
        return result;
    }

    // Component Velocity
    public static Component toAdventureComponent(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return LegacyComponentSerializer.legacySection().deserialize(Colors.set(message));
    }

    private static boolean isPlaceholderAPIEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }
}