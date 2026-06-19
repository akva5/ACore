package akv5.acore.libs.managers;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.configs.GrantConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class GrantManager {

    private final ACore plugin;
    private final GrantConfig donationConfig;
    private File dataFile;
    private FileConfiguration data;

    public GrantManager(ACore plugin, GrantConfig donationConfig) {
        this.plugin = plugin;
        this.donationConfig = donationConfig;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data/donations.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать donations.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить donations.yml: " + e.getMessage());
        }
    }

    public boolean canGrantDonation(UUID senderUUID, String senderGroup, String donationType) {
        if (isAdmin(senderGroup)) {
            return true;
        }

        Map<String, Map<String, Integer>> groups = donationConfig.getDonationGroups();
        Map<String, Integer> groupLimits = groups.get(senderGroup);

        if (groupLimits == null || !groupLimits.containsKey(donationType)) {
            return false;
        }

        int limit = groupLimits.get(donationType);

        if (limit == -1) {
            return true;
        }

        String monthKey = getCurrentMonthKey();
        String path = "players." + senderUUID.toString() + "." + monthKey + "." + donationType;
        int granted = data.getInt(path, 0);

        return granted < limit;
    }

    public void recordGrant(UUID senderUUID, String donationType) {
        String monthKey = getCurrentMonthKey();
        String path = "players." + senderUUID.toString() + "." + monthKey + "." + donationType;
        int current = data.getInt(path, 0);
        data.set(path, current + 1);
        saveData();
    }

    public boolean isAdmin(String group) {
        List<String> adminGroups = donationConfig.getAdminGroups();
        return adminGroups.contains(group);
    }

    public void showRemainingLimits(Player player, String playerGroup) {
        Informer.send(player, "&7\n&7╔═════════════════╗");
        Informer.send(player, "&7║     &eВаши лимиты на месяц");
        Informer.send(player, "&7║");

        UUID playerUUID = player.getUniqueId();
        String monthKey = getCurrentMonthKey();

        Map<String, Map<String, Integer>> groups = donationConfig.getDonationGroups();
        Map<String, Integer> groupLimits = groups.get(playerGroup);

        if (groupLimits == null) {
            Informer.send(player, "&cНедоступно!");
            Informer.send(player, "&7╚═════════════════╝\n&7");
            return;
        }

        for (Map.Entry<String, Integer> entry : groupLimits.entrySet()) {
            String donationType = entry.getKey();
            int limit = entry.getValue();

            if (limit == -1) {
                Informer.send(player, "&7║ &f" + donationType + ": &a∞ &7(без лимита)");
                continue;
            }

            String path = "players." + playerUUID.toString() + "." + monthKey + "." + donationType;
            int used = data.getInt(path, 0);
            int remaining = limit - used;

            String status = remaining > 0 ? "&a✓ " + remaining + " осталось" : "&c✗ исчерпан";
            Informer.send(player, "&7║ &f" + donationType + ": &e" + used + "/" + limit + " " + status);
        }

        Informer.send(player, "&7╚═════════════════╝\n&7");
    }

    public boolean canGroupGrantDonationType(String group, String donationType) {
        if (isAdmin(group)) {
            return true;
        }

        Map<String, Map<String, Integer>> groups = donationConfig.getDonationGroups();
        Map<String, Integer> groupLimits = groups.get(group);
        return groupLimits != null && groupLimits.containsKey(donationType);
    }

    public String getAvailableDonationsForGroup(String group) {
        return donationConfig.getAvailableDonationsStringForGroup(group);
    }

    public List<String> getAvailableDonationsListForGroup(String group) {
        return donationConfig.getAvailableDonationsListForGroup(group);
    }

    public int getRemainingLimit(UUID playerUUID, String playerGroup, String donationType) {
        if (isAdmin(playerGroup)) {
            return -1;
        }

        Map<String, Map<String, Integer>> groups = donationConfig.getDonationGroups();
        Map<String, Integer> groupLimits = groups.get(playerGroup);

        if (groupLimits == null || !groupLimits.containsKey(donationType)) {
            return 0;
        }

        int limit = groupLimits.get(donationType);
        if (limit == -1) {
            return -1;
        }

        String monthKey = getCurrentMonthKey();
        String path = "players." + playerUUID.toString() + "." + monthKey + "." + donationType;
        int used = data.getInt(path, 0);

        return Math.max(0, limit - used);
    }

    public String getCurrentMonthKey() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }

    public void cleanupOldData() {
        String currentMonth = getCurrentMonthKey();
        if (data.contains("players")) {
            for (String playerUUID : data.getConfigurationSection("players").getKeys(false)) {
                for (String monthKey : data.getConfigurationSection("players." + playerUUID).getKeys(false)) {
                    if (!monthKey.equals(currentMonth)) {
                        data.set("players." + playerUUID + "." + monthKey, null);
                    }
                }
            }
            saveData();
        }
    }
}