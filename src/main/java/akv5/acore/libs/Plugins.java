package akv5.acore.libs;

import org.bukkit.Bukkit;

public enum Plugins {
    AntiRelog,
    EmoteCraft,
    WorldGuard,
    ProtocolLib,
    ViaVersion,
    LuckPerms,
    LiteBans,
    ItemJoin,
    PlaceholderAPI,
    ItemsAdder,
    Spark,
    TAB,
    Vault,
    CMI,
    CMILib;

    private Boolean status;

    private Plugins() {}

    public boolean isEnabled() {
        if (this.status != null) {
            return this.status;
        }
        this.status = Bukkit.getPluginManager().getPlugin(this.name().replace("_", "-")) != null;
        return this.status;
    }
}

