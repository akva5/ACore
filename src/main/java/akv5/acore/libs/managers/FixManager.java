package akv5.acore.libs.managers;

import akv5.acore.ACore;
import akv5.acore.libs.ExploitsFix;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FixManager {

    private final Plugin plugin = ACore.getInstance();
    private FileConfiguration config;
    private final List<Fix> activeFixes = new ArrayList<>();
    private ExploitsFix exploitsFix;

    public FixManager() {
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "exploits.yml");
        if (!configFile.exists()) {
            plugin.saveResource("exploits.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void registerAllFixes() {
        exploitsFix = new ExploitsFix(config);
        Bukkit.getPluginManager().registerEvents(exploitsFix, plugin);

        if (config.getBoolean("fix-exploits.swap", true)) activeFixes.add(Fix.SWAP);
        if (config.getBoolean("fix-exploits.filled-map-on-banner", true)) activeFixes.add(Fix.FILLED_MAP_ON_BANNER);
        if (config.getBoolean("fix-exploits.shelf", true)) activeFixes.add(Fix.SHELF);
        if (config.getBoolean("fix-exploits.enderpearl-phase", true)) activeFixes.add(Fix.ENDERPEARL_PHASE);
        if (config.getBoolean("fix-exploits.cauldron-debug-stick", true)) activeFixes.add(Fix.CAULDRON_DEBUG_STICK);
        if (config.getBoolean("fix-exploits.horse", true)) activeFixes.add(Fix.HORSE);
        if (config.getBoolean("fix-exploits.sign-command", true)) activeFixes.add(Fix.SIGN_COMMAND);
        if (config.getBoolean("fix-exploits.cmi-crossbow-piercing", true)) activeFixes.add(Fix.CMI_CROSSBOW);
        if (config.getBoolean("fix-exploits.weaving", true)) activeFixes.add(Fix.WEAVING);
        if (config.getBoolean("fix-exploits.string-dupe", true)) activeFixes.add(Fix.STRING_DUPE);
        if (config.getBoolean("fix-exploits.log4j", true)) activeFixes.add(Fix.LOG4J);
        if (config.getBoolean("fix-exploits.pearl-phasing", true)) activeFixes.add(Fix.PEARL_PHASING);
        if (config.getBoolean("fix-exploits.portal-trap", false)) activeFixes.add(Fix.PORTAL_TRAP);
        if (config.getBoolean("fix-exploits.tab-complete", true)) activeFixes.add(Fix.TAB_COMPLETE);
        if (config.getBoolean("fix-exploits.worldedit", true)) activeFixes.add(Fix.WORLDEDIT);
        if (config.getBoolean("fix-exploits.custom-payload.enabled", true)) activeFixes.add(Fix.CUSTOM_PAYLOAD);
        if (config.getBoolean("fix-exploits.bed.disable-inventory", true) ||
                config.getBoolean("fix-exploits.bed.disable-commands", true)) {
            activeFixes.add(Fix.BED);
        }
        if (config.getBoolean("fix-exploits.emotes-spamming.enabled", true)) activeFixes.add(Fix.EMOTE_SPAM);
        if (config.getBoolean("fix-exploits.anti-redstone-clock.enabled", true)) activeFixes.add(Fix.REDSTONE_CLOCK);
        if (config.getBoolean("fix-exploits.fawe-patterns.enabled", true)) activeFixes.add(Fix.FAWE_PATTERNS);
        if (isBadItemsEnabled()) activeFixes.add(Fix.BAD_ITEMS);
    }

    public void unregisterProtocolListeners() {
        if (exploitsFix != null) {
            exploitsFix.unregisterProtocolListeners();
        }
    }

    private boolean isBadItemsEnabled() {
        return config.getBoolean("fix-exploits.bad-items.heads.enabled", true) ||
                config.getBoolean("fix-exploits.bad-items.limit-bytes.enabled", true) ||
                config.getBoolean("fix-exploits.bad-items.limit-namelore.enabled", true) ||
                config.getBoolean("fix-exploits.bad-items.shulker.block-books", true) ||
                config.getBoolean("fix-exploits.bad-items.shulker.block-nested", true) ||
                config.getBoolean("fix-exploits.bad-items.bundle.block-books", true) ||
                config.getBoolean("fix-exploits.bad-items.bundle.block-nested", true) ||
                config.getBoolean("fix-exploits.bad-items.no-placeholders", true) ||
                config.getBoolean("fix-exploits.bad-items.custom-model-data", true) ||
                config.getBoolean("fix-exploits.bad-items.attribute", true);
    }

    public int getActiveFixesCount() {
        return activeFixes.size();
    }

    public List<Fix> getActiveFixes() {
        return new ArrayList<>(activeFixes);
    }

    public enum Fix {
        SWAP("Swap"),
        FILLED_MAP_ON_BANNER("FilledMapOnBanner"),
        SHELF("Shelf"),
        ENDERPEARL_PHASE("EnderpearlPhase"),
        CAULDRON_DEBUG_STICK("CauldronDebugStick"),
        HORSE("Horse"),
        SIGN_COMMAND("SignCommand"),
        CMI_CROSSBOW("CMICrossbow"),
        WEAVING("Weaving"),
        STRING_DUPE("StringDupe"),
        LOG4J("Log4j"),
        PEARL_PHASING("PearlPhasing"),
        PORTAL_TRAP("PortalTrap"),
        TAB_COMPLETE("TabComplete"),
        WORLDEDIT("WorldEdit"),
        CUSTOM_PAYLOAD("CustomPayload"),
        BED("Bed"),
        EMOTE_SPAM("EmoteSpam"),
        REDSTONE_CLOCK("RedstoneClock"),
        FAWE_PATTERNS("FawePatterns"),
        BAD_ITEMS("BadItems");

        private final String name;

        Fix(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}