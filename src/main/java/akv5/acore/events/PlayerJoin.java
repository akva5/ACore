package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJoin implements Listener {

    private final Map<UUID, Integer> joinCountMap;
    private final FileConfiguration dataConfig;
    private final File dataFile;
    public final ACore plugin;

    public PlayerJoin(ACore plugin) {
        this.plugin = plugin;
        this.joinCountMap = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");

        if (!this.dataFile.exists()) {
            try {
                this.dataFile.getParentFile().mkdirs();
                this.dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml: " + e.getMessage());
                e.printStackTrace();
            }
        }

        this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
        this.loadJoinCounts();
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (ACore.getInstance().getConfig().getBoolean("events.PlayerJoin.title.enabled")) {
            int joinCount = (Integer) this.joinCountMap.getOrDefault(playerId, 0) + 1;
            this.joinCountMap.put(playerId, joinCount);

            if (joinCount >= 1 && joinCount <= 2) {
                String title = ACore.getInstance().getConfig().getString("events.PlayerJoin.title.firstJoin.line1");
                String subtitle = ACore.getInstance().getConfig().getString("events.PlayerJoin.title.firstJoin.line2");

                int fadeInTime = ACore.getInstance().getConfig().getInt("events.PlayerJoin.title.firstJoin.fade-in-time");
                int stayTime = ACore.getInstance().getConfig().getInt("events.PlayerJoin.title.firstJoin.stay-time");
                int fadeOutTime = ACore.getInstance().getConfig().getInt("events.PlayerJoin.title.firstJoin.fade-out-time");

                Informer.sendTitle(player, title, subtitle, fadeInTime, stayTime, fadeOutTime);

            } else if (joinCount >= 3 && joinCount <= 999999999) {
                String title = ACore.getInstance().getConfig().getString("events.PlayerJoin.title.join.line1");
                String subtitle = ACore.getInstance().getConfig().getString("events.PlayerJoin.title.join.line2");

                int fadeInTime = ACore.getInstance().getConfig().getInt("events.PlayerJoin.title.join.fade-in-time");
                int stayTime = ACore.getInstance().getConfig().getInt("events.PlayerJoin.title.join.stay-time");
                int fadeOutTime = ACore.getInstance().getConfig().getInt("events.PlayerJoin.title.join.fade-out-time");

                Informer.sendTitle(player, title, subtitle, fadeInTime, stayTime, fadeOutTime);
            }

            ConfigurationSection playerData = this.dataConfig.getConfigurationSection(playerId.toString());
            if (playerData == null) {
                playerData = this.dataConfig.createSection(playerId.toString());
            }

            playerData.set("joinCount", joinCount);
            this.saveDataConfig();
        }

        Methods.method(player, "grant");
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Methods.method(player, "revoke");
    }

    private void saveDataConfig() {
        try {
            this.dataConfig.save(this.dataFile);
        } catch (IOException var2) {
            plugin.getLogger().warning("An error occurred while saving data.yml");
            var2.printStackTrace();
        }
    }

    private void loadJoinCounts() {
        ConfigurationSection playersSection = this.dataConfig.getConfigurationSection("");
        if (playersSection != null) {
            for (String playerId : playersSection.getKeys(false)) {
                UUID uuid = UUID.fromString(playerId);
                int joinCount = playersSection.getInt(playerId + ".joinCount");
                this.joinCountMap.put(uuid, joinCount);
            }
        }
    }
}
