package akv5.acore.utils;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class Broadcast {

    private final List<String> placeholders = ACore.getInstance().getConfig().getStringList("custom_placeholders.placeholders");

    private boolean enabled;
    private long delayInSeconds;
    private final Map<String, List<String>> messagesMap = new LinkedHashMap<>();

    public Broadcast() {
        loadAutoMessages();
    }

    private void loadAutoMessages() {
        try {
            File file = new File(ACore.getInstance().getDataFolder(), "announcer.yml");
            if (!file.exists()) {
                ACore.getInstance().saveResource("announcer.yml", false);
            }
            FileConfiguration announcerConfig = YamlConfiguration.loadConfiguration(file);

            enabled = announcerConfig.getBoolean("autoMessages.enabled", true);
            delayInSeconds = announcerConfig.getLong("autoMessages.delay", 30);

            messagesMap.clear();

            if (announcerConfig.isConfigurationSection("autoMessages.messages")) {
                for (String key : announcerConfig.getConfigurationSection("autoMessages.messages").getKeys(false)) {
                    List<String> lines = announcerConfig.getStringList("autoMessages.messages." + key + ".lines");
                    if (!lines.isEmpty()) {
                        messagesMap.put(key, lines);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            enabled = false;
        }
    }

    public void send(String playerName, String[] args) {
        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        String formattedMessage = Colors.set(message.toString().trim());
        FileConfiguration config = ACore.getInstance().getConfig();
        String configString = config.getString("trolls.achievementtroll.message");

        String finalMessage = configString
                .replace("{player}", playerName)
                .replace("{message}", formattedMessage);

        String[] lines = finalMessage.split("\\|");
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String line : lines) {
                Informer.send(player, line);
            }
        }

        Bukkit.getConsoleSender().sendMessage(finalMessage);
    }

    public void autoMessages() {
        if (!enabled) {
            return;
        }

        long delayInTicks = delayInSeconds * 20;

        new BukkitRunnable() {
            private final Random random = new Random();

            @Override
            public void run() {
                sendRandomMessage();
            }

            private void sendRandomMessage() {
                if (messagesMap.isEmpty()) {
                    return;
                }

                List<List<String>> allMessageLines = new ArrayList<>(messagesMap.values());
                List<String> lines = allMessageLines.get(random.nextInt(allMessageLines.size()));

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!Methods.hasPermission(player, "acore.bypass.broadcast")) {
                        for (String line : lines) {
                            String formattedLine = Colors.set(line);
                            formattedLine = PlaceholderAPI.setPlaceholders(player, formattedLine);
                            for (String placeholder : placeholders) {
                                formattedLine = formattedLine.replace(placeholder, player.getDisplayName());
                            }
                            Informer.send(player, formattedLine);
                        }
                    }
                }
            }
        }.runTaskTimer(ACore.getInstance(), 0L, delayInTicks);
    }
}
