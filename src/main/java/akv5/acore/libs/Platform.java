package akv5.acore.libs;

public enum Platform {
    BUKKIT("org.bukkit.Bukkit");

    public final boolean isAvailable;
    private static Platform platform = null;

    private Platform(String main) {
        this.isAvailable = checkClass(main);
    }

    private static boolean checkClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Platform get() {
        if (platform != null) {
            return platform;
        }
        for (Platform p : values()) {
            if (p.isAvailable) {
                platform = p;
                return platform;
            }
        }
        return null;
    }
}
