package akv5.acore.libs.managers;

import akv5.acore.ACore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class PlayerDataManager {

    private final ACore plugin;
    private final FileConfiguration playerData;
    private final File playerDataFile;

    public PlayerDataManager(ACore plugin) {
        this.plugin = plugin;
        playerDataFile = new File(plugin.getDataFolder(), "chatcolor_data.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public String getPlayerColor(Player player) {
        String uuid = player.getUniqueId().toString();
        return playerData.getString(uuid + ".chatcolor", plugin.getConfig().getString("default_chatcolor", "&f"));
    }

    public void setPlayerColor(Player player, String color) {
        String uuid = player.getUniqueId().toString();
        playerData.set(uuid + ".chatcolor", color);
        save();
    }

    public void save() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
