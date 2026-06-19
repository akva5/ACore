package akv5.alib.controller;

import java.util.Set;

public class DebugController {
    private static boolean enabled = false;
    private static Set<String> players = Set.of();
    private static String consoleName = "";

    public static boolean isEnabled() {
        return enabled;
    }

    public static Set<String> getPlayers() {
        return players;
    }

    public static String getConsoleName() {
        return consoleName;
    }

    public static void reload(boolean enabled, Set<String> players, String consoleName) {
        DebugController.enabled = enabled;
        DebugController.players = players;
        DebugController.consoleName = consoleName;
    }
}