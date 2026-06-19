package akv5.alib.controller;

public class DiscordController {
    private static boolean enabled = false;
    private static String token = "";
    private static String statusText = "";
    private static String statusType = "";
    private static String statusUrl = "";
    private static String autoRole = "";

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        DiscordController.enabled = enabled;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        DiscordController.token = token;
    }

    public static String getStatusText() {
        return statusText;
    }

    public static void setStatusText(String statusText) {
        DiscordController.statusText = statusText;
    }

    public static String getStatusType() {
        return statusType;
    }

    public static void setStatusType(String statusType) {
        DiscordController.statusType = statusType;
    }

    public static String getStatusUrl() {
        return statusUrl;
    }

    public static void setStatusUrl(String statusUrl) {
        DiscordController.statusUrl = statusUrl;
    }

    public static String getAutoRole() {
        return autoRole;
    }

    public static void setAutoRole(String autoRole) {
        DiscordController.autoRole = autoRole;
    }

    public static void reload(boolean enabled, String token, String statusText, String statusType, String statusUrl, String autoRole) {
        DiscordController.enabled = enabled;
        DiscordController.token = token;
        DiscordController.statusText = statusText;
        DiscordController.statusType = statusType;
        DiscordController.statusUrl = statusUrl;
        DiscordController.autoRole = autoRole;
    }
}