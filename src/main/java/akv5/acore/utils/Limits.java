package akv5.acore.utils;

import akv5.acore.ACore;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class Limits {

    private final ACore plugin;
    private final FileConfiguration config;
    private final Map<String, Integer> commandLimits;
    private final Map<String, Integer> commandUsageCounts;

    public Limits(ACore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.commandLimits = new HashMap<>();
        this.commandUsageCounts = new HashMap<>();
    }

    public int getCommandLimit(String command) {
        if (config.contains("admin." + command + ".limit")) {
            return config.getInt("admin." + command + ".limit");
        } else {
            return -1;
        }
    }

    public boolean canExecuteCommand(String command) {
        int limit = getCommandLimit(command);
        if (limit == -1) {
            return true;
        } else {
            int usageCount = commandUsageCounts.getOrDefault(command, 0);
            if (usageCount < limit) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void incrementCommandCount(String command) {
        int usageCount = commandUsageCounts.getOrDefault(command, 0);
        commandUsageCounts.put(command, usageCount + 1);
    }
}

