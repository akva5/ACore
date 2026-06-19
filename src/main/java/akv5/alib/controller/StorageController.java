package akv5.alib.controller;

import akv5.alib.data.HPlayer;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StorageController {
    public record ShortlySkinnedItem(String id, String name, String namespace, String category) {}

    private static final String guiNBT = "${lib_name_id}";
    private static final Map<HPlayer, Set<HPlayer>> kissPlayers = new ConcurrentHashMap<>();
    private static final Set<String> ragePlayers = ConcurrentHashMap.newKeySet();
    private static final Set<ShortlySkinnedItem> shortlySkinnedItems = ConcurrentHashMap.newKeySet();
    private static final Map<HPlayer, Set<String>> playerSkinnedItems = new ConcurrentHashMap<>();
    private static final Map<Player, Boolean> pvpArenaPlayers = new ConcurrentHashMap<>();
    private static String noPermMessage = "";
    private static String commandCooldownMessage = "";
    private static String commandLimitMessage = "";

    public static String getGuiNBT() {
        return guiNBT;
    }

    public static Map<HPlayer, Set<HPlayer>> getKissPlayers() {
        return kissPlayers;
    }

    public static Set<String> getRagePlayers() {
        return ragePlayers;
    }

    public static Set<ShortlySkinnedItem> getShortlySkinnedItems() {
        return shortlySkinnedItems;
    }

    public static Map<HPlayer, Set<String>> getPlayerSkinnedItems() {
        return playerSkinnedItems;
    }

    public static Map<Player, Boolean> getPvpArenaPlayers() {
        return pvpArenaPlayers;
    }

    public static String getNoPermMessage() {
        return noPermMessage;
    }

    public static void setNoPermMessage(String noPermMessage) {
        StorageController.noPermMessage = noPermMessage;
    }

    public static String getCommandCooldownMessage() {
        return commandCooldownMessage;
    }

    public static void setCommandCooldownMessage(String commandCooldownMessage) {
        StorageController.commandCooldownMessage = commandCooldownMessage;
    }

    public static String getCommandLimitMessage() {
        return commandLimitMessage;
    }

    public static void setCommandLimitMessage(String commandLimitMessage) {
        StorageController.commandLimitMessage = commandLimitMessage;
    }
}