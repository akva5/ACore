package akv5.acore.libs.configs;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class GrantConfig {

    private final ACore plugin;
    private final File configFile;
    private FileConfiguration config;

    private final Map<String, Map<String, Integer>> donationGroups;
    private List<String> adminGroups;
    private final Map<String, String> messages;

    public GrantConfig(ACore plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "grant.yml");
        this.donationGroups = new HashMap<>();
        this.adminGroups = new ArrayList<>();
        this.messages = new HashMap<>();
    }

    public void load() {
        if (!configFile.exists()) {
            createDefaultConfig();
        }

        reload();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        loadGroups();
        loadAdminGroups();
        loadMessages();
    }

    private void createDefaultConfig() {
        plugin.getDataFolder().mkdirs();

        try (InputStream inputStream = plugin.getResource("grant.yml")) {
            if (inputStream != null) {
                Files.copy(inputStream, configFile.toPath());
            } else {
                config = new YamlConfiguration();

                Map<String, Object> groupsSection = new HashMap<>();

                Map<String, Object> grehGroup = new HashMap<>();
                grehGroup.put("chorus", 2);
                grehGroup.put("deluxe", 1);
                groupsSection.put("greh", grehGroup);

                config.set("groups", groupsSection);

                config.set("admin-groups", Arrays.asList("admin", "op"));

                Map<String, Object> messagesSection = new HashMap<>();
                messagesSection.put("usage", "&a/grant &7[&aник&7] &7[&aпривилегия&7] &f- Выдать игроку привилегию.");
                messagesSection.put("invalid-donation", "{prefix}&cНеверный тип привилегии! Доступные: {available}");
                messagesSection.put("limit-exceeded", "{prefix}&cВы исчерпали лимит на выдачу &a{donate} &cв этом месяце!");
                messagesSection.put("grant-success", "{prefix}Вы выдали привилегию &a{donate} &fигроку &e{target}&f.");
                messagesSection.put("grant-receive", "&7\n{prefix}&e{player} &fподарил вам привилегию &a{donate}&f.\n&7");
                messagesSection.put("error", "{prefix}&cОшибка при выдаче привилегии.");
                messagesSection.put("no-permission", "{prefix}&cУ вас нет прав на использование команды /grant!");
                messagesSection.put("no-group-permission", "{prefix}&cВаша группа не может выдавать этот тип привилегии!");

                config.set("messages", messagesSection);

                // Сохраняем конфиг
                config.save(configFile);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось создать конфиг grant.yml: " + e.getMessage());
        }
    }

    private void loadGroups() {
        donationGroups.clear();

        if (!config.contains("groups")) {
            plugin.getLogger().warning("Раздел 'groups' не найден в grant.yml!");
            return;
        }

        try {
            for (String groupName : config.getConfigurationSection("groups").getKeys(false)) {
                if (groupName == null) continue;

                Map<String, Integer> permissions = new HashMap<>();
                String path = "groups." + groupName;

                if (!config.isConfigurationSection(path)) {
                    plugin.getLogger().warning("Раздел 'groups." + groupName + "' не является корректной секцией!");
                    continue;
                }

                for (String donationType : config.getConfigurationSection(path).getKeys(false)) {
                    if (donationType == null) continue;

                    int limit = config.getInt(path + "." + donationType, 0);
                    permissions.put(donationType, limit);
                }

                donationGroups.put(groupName, permissions);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при загрузке групп из grant.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAdminGroups() {
        adminGroups.clear();
        adminGroups = config.getStringList("admin-groups");
    }

    private void loadMessages() {
        messages.clear();

        if (config.contains("messages")) {
            try {
                for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                    if (key == null) continue;

                    String message = config.getString("messages." + key, "");
                    if (message != null) {
                        messages.put(key, Colors.set(message));
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при загрузке сообщений из grant.yml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Map<String, Map<String, Integer>> getDonationGroups() {
        return new HashMap<>(donationGroups);
    }

    public List<String> getAdminGroups() {
        return new ArrayList<>(adminGroups);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "&cСообщение не найдено: " + key);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message;
    }

    public Set<String> getAllAvailableDonations() {
        Set<String> donations = new HashSet<>();

        for (Map<String, Integer> groupPermissions : donationGroups.values()) {
            donations.addAll(groupPermissions.keySet());
        }

        return donations;
    }

    public Set<String> getAvailableDonationsForGroup(String group) {
        Set<String> availableDonations = new HashSet<>();

        if (adminGroups.contains(group)) {
            return getAllAvailableDonations();
        }

        Map<String, Integer> groupPermissions = donationGroups.get(group);
        if (groupPermissions != null) {
            availableDonations.addAll(groupPermissions.keySet());
        }

        return availableDonations;
    }

    public List<String> getAvailableDonationsListForGroup(String group) {
        List<String> availableDonations = new ArrayList<>();

        if (adminGroups.contains(group)) {
            availableDonations.addAll(getAllAvailableDonations());
        } else {
            Map<String, Integer> groupPermissions = donationGroups.get(group);
            if (groupPermissions != null) {
                availableDonations.addAll(groupPermissions.keySet());
            }
        }

        availableDonations.sort(String::compareToIgnoreCase);
        return availableDonations;
    }

    public String getAvailableDonationsStringForGroup(String group) {
        Set<String> availableDonations = getAvailableDonationsForGroup(group);
        if (availableDonations.isEmpty()) {
            return "нет доступных привилегий";
        }
        return String.join(", ", availableDonations);
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить grant.yml: " + e.getMessage());
        }
    }
}