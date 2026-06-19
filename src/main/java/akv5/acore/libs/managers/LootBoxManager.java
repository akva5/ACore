package akv5.acore.libs.managers;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Scheduler;
import akv5.acore.libs.hooks.CMIHologramHook;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LootBoxManager {

    private final ACore plugin;
    private File file;
    private FileConfiguration config;

    private final Map<String, LootBoxType> lootBoxTypes = new HashMap<>();
    private final Map<String, String> activeLootBoxes = new HashMap<>();
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private final Map<String, Inventory> lootBoxInventories = new HashMap<>();
    private final Map<String, Long> lastRefillTimes = new HashMap<>();

    public LootBoxManager(ACore plugin) {
        this.plugin = plugin;
        CMIHologramHook.init();
        loadConfig();
        startUpdateTask();
    }

    public void loadConfig() {
        file = new File(plugin.getDataFolder(), "customs/lootboxes.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("customs/lootboxes.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadTypes();
        loadLocations();
    }

    private void loadTypes() {
        lootBoxTypes.clear();
        ConfigurationSection section = config.getConfigurationSection("lootboxes");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection boxSection = section.getConfigurationSection(key);
            if (boxSection == null) continue;

            LootBoxType type = new LootBoxType();
            type.id = key;
            type.displayName = Colors.set(boxSection.getString("displayName", "LootBox"));
            type.blockMaterial = Material.valueOf(boxSection.getString("blockMaterial", "CHEST"));
            type.hologramHeight = boxSection.getDouble("hologramHeight", 1.5);
            type.hologramLines = Colors.set(boxSection.getStringList("hologramLines"));

            type.inventorySize = boxSection.getInt("inventorySize", 27);
            type.pickupDelay = boxSection.getLong("pickupDelay", 1000L);
            type.refillTime = boxSection.getLong("refillTime", 3600L);

            ConfigurationSection itemsSec = boxSection.getConfigurationSection("items");
            if (itemsSec != null) {
                for (String itemKey : itemsSec.getKeys(false)) {
                    ConfigurationSection is = itemsSec.getConfigurationSection(itemKey);
                    LootBoxItem item = new LootBoxItem();
                    item.material = Material.valueOf(is.getString("material", "STONE"));
                    item.amount = is.getInt("amount", 1);
                    item.name = is.getString("name");
                    item.lore = is.getStringList("lore");
                    item.chance = is.getDouble("chance", 50.0);
                    type.items.add(item);
                }
            }
            lootBoxTypes.put(key, type);
        }
    }

    private void loadLocations() {
        activeLootBoxes.clear();
        ConfigurationSection locSection = config.getConfigurationSection("locations");
        if (locSection == null) return;

        for (String locStr : locSection.getKeys(false)) {
            String typeId = locSection.getString(locStr);
            if (lootBoxTypes.containsKey(typeId)) {
                Location loc = stringToLoc(locStr);
                if (loc != null && loc.getWorld() != null) {
                    activeLootBoxes.put(locStr, typeId);
                    spawnVisuals(loc, typeId);
                }
            }
        }
    }

    private void startUpdateTask() {
        Scheduler.doAsyncRepeat(() -> {
            long now = System.currentTimeMillis();

            for (Map.Entry<String, String> entry : activeLootBoxes.entrySet()) {
                String locKey = entry.getKey();
                String typeId = entry.getValue();

                LootBoxType type = lootBoxTypes.get(typeId);
                if (type == null) continue;

                long lastRefill = lastRefillTimes.getOrDefault(locKey, 0L);
                long nextRefill = lastRefill + (type.refillTime * 1000);
                long diff = nextRefill - now;

                List<String> linesToSet = new ArrayList<>();

                if (diff <= 0) {
                    for (String line : type.hologramLines) {
                        if (line.contains("{time}")) {
                            linesToSet.add(line.replace("{time}", "&cЛУТАЕМ"));
                        } else {
                            linesToSet.add(line);
                        }
                    }
                } else {
                    String timeString = formatTime(diff / 1000);
                    for (String line : type.hologramLines) {
                        linesToSet.add(line.replace("{time}", timeString));
                    }
                }

                String holoId = "lb_" + locKey.hashCode();
                CMIHologramHook.updateHologramLines(holoId, Colors.set(linesToSet));
            }
        }, 20L, 20L);
    }

    public void createLootBox(Location loc, String typeId) {
        if (!lootBoxTypes.containsKey(typeId)) {
            plugin.getLogger().warning("Попытка создать лутбокс неизвестного типа: " + typeId);
            return;
        }

        Location blockLoc = loc.getBlock().getLocation();
        String key = locToString(blockLoc);

        activeLootBoxes.put(key, typeId);

        config.set("locations." + key, typeId);

        try {
            config.save(file);
            plugin.getLogger().info("Лутбокс сохранен в конфиг: " + key + " -> " + typeId);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить lootboxes.yml!");
            e.printStackTrace();
        }

        spawnVisuals(blockLoc, typeId);
    }

    public void removeLootBox(Location loc) {
        String key = locToString(loc.getBlock().getLocation());

        if (activeLootBoxes.containsKey(key)) {
            String holoId = "lb_" + key.hashCode();
            CMIHologramHook.removeHologram(holoId);

            activeLootBoxes.remove(key);
            config.set("locations." + key, null);

            lootBoxInventories.remove(key);
            lastRefillTimes.remove(key);
            saveConfig();

            loc.getBlock().setType(Material.AIR);
        }
    }

    private void spawnVisuals(Location loc, String typeId) {
        LootBoxType type = lootBoxTypes.get(typeId);
        if (type == null) return;

        loc.getBlock().setType(type.blockMaterial);

        Location holoLoc = loc.clone().add(0.5, type.hologramHeight, 0.5);
        String key = locToString(loc);
        CMIHologramHook.createHologram("lb_" + key.hashCode(), holoLoc, type.hologramLines);
    }

    public void openLootBox(Player player, Location loc) {
        String key = locToString(loc.getBlock().getLocation());
        String typeId = activeLootBoxes.get(key);
        if (typeId == null) return;

        LootBoxType type = lootBoxTypes.get(typeId);

        long now = System.currentTimeMillis();
        long lastRefill = lastRefillTimes.getOrDefault(key, 0L);

        Inventory inv = lootBoxInventories.get(key);

        if (inv == null || (now - lastRefill) >= (type.refillTime * 1000)) {
            if (inv == null) {
                inv = Bukkit.createInventory(null, type.inventorySize, type.displayName);
                lootBoxInventories.put(key, inv);
            }

            inv.clear();
            fillInventory(inv, type);

            lastRefillTimes.put(key, now);
        }

        player.openInventory(inv);
        player.playSound(loc, Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    private void fillInventory(Inventory inv, LootBoxType type) {
        List<ItemStack> rewards = new ArrayList<>();
        for (LootBoxItem item : type.items) {
            double roll = ThreadLocalRandom.current().nextDouble(100.0);
            if (roll <= item.chance) {
                rewards.add(item.build());
            }
        }

        if (!rewards.isEmpty()) {
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < type.inventorySize; i++) slots.add(i);
            Collections.shuffle(slots);

            int itemIndex = 0;
            for (Integer slot : slots) {
                if (itemIndex >= rewards.size()) break;
                inv.setItem(slot, rewards.get(itemIndex));
                itemIndex++;
            }
        }
    }

    public boolean checkCooldown(Player player, String typeId) {
        LootBoxType type = lootBoxTypes.get(typeId);
        if (type == null || type.pickupDelay <= 0) return true;

        long lastPickup = playerCooldowns.getOrDefault(player.getUniqueId(), 0L);
        long now = System.currentTimeMillis();

        if (now - lastPickup < type.pickupDelay) {
            return false;
        }

        playerCooldowns.put(player.getUniqueId(), now);
        return true;
    }

    public LootBoxType getTypeByTitle(String title) {
        for (LootBoxType type : lootBoxTypes.values()) {
            if (type.displayName.equals(title)) {
                return type;
            }
        }
        return null;
    }

    public ItemStack getPlacerItem(String typeId) {
        if (!lootBoxTypes.containsKey(typeId)) return null;
        ItemStack item = new ItemStack(Material.END_PORTAL_FRAME);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Colors.set("&fУстановщик Лутбокса: &a" + typeId));
        meta.setLore(Collections.singletonList(Colors.set("&7Поставь этот блок, чтобы создать лутбокс.")));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isLootBox(Location loc) {
        if (loc == null) return false;
        return activeLootBoxes.containsKey(locToString(loc.getBlock().getLocation()));
    }

    private void saveConfig() {
        try {
            if (file == null) {
                file = new File(plugin.getDataFolder(), "customs/lootboxes.yml");
            }
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка при сохранении lootboxes.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String locToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    private Location stringToLoc(String str) {
        try {
            String[] parts = str.split(";");
            if (parts.length != 4) return null;

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;

            return new Location(world,
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]));
        } catch (Exception e) {
            return null;
        }
    }

    public static class LootBoxType {
        public String id;
        String displayName;
        Material blockMaterial;
        double hologramHeight;
        List<String> hologramLines;
        List<LootBoxItem> items = new ArrayList<>();
        int inventorySize;
        long pickupDelay;
        public long refillTime;
    }

    private static class LootBoxItem {
        Material material;
        int amount;
        String name;
        List<String> lore;
        double chance;

        ItemStack build() {
            ItemStack is = new ItemStack(material, amount);
            ItemMeta meta = is.getItemMeta();
            if (name != null) meta.setDisplayName(Colors.set(name));
            if (lore != null) meta.setLore(Colors.set(lore));
            is.setItemMeta(meta);
            return is;
        }
    }

    public String getTypeIdByInventory(Inventory inventory) {
        if (inventory == null) return null;

        String locationKey = null;
        for (Map.Entry<String, Inventory> entry : lootBoxInventories.entrySet()) {
            if (entry.getValue().equals(inventory)) {
                locationKey = entry.getKey();
                break;
            }
        }

        if (locationKey == null) return null;

        return activeLootBoxes.get(locationKey);
    }

    public boolean isLootBoxInventory(Inventory inventory) {
        if (inventory == null) return false;
        return lootBoxInventories.containsValue(inventory);
    }

    private String formatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;

        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }
}