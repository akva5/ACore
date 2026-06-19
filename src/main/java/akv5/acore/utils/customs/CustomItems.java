package akv5.acore.utils.customs;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import akv5.acore.libs.Methods;
import akv5.acore.utils.AttributeCompat;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomItems implements Listener {

    private static CustomItems instance;
    private final NamespacedKey customItemKey = new NamespacedKey(ACore.getInstance(), "customItem");

    public static CustomItems getInstance() {
        if (instance == null) {
            instance = new CustomItems();
        }
        return instance;
    }

    public void initialize() {
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (isCustomItem(item) && !canPlaceCustomItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {
        if (event.getPlayer() == null) return;
        Player player = event.getPlayer();

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isCustomItem(mainHand) && !canPlaceCustomItem(mainHand)) {
            event.setCancelled(true);
            return;
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (isCustomItem(offHand) && !canPlaceCustomItem(offHand)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (isCustomItem(item) && !canPlaceCustomItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && isCustomItem(item) && isPlaceableMaterial(item.getType()) && !canPlaceCustomItem(item)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean canPlaceCustomItem(ItemStack item) {
        if (!isCustomItem(item)) return true;
        String customItemName = getCustomItemName(item);
        if (customItemName == null) return true;
        FileConfiguration config = ACore.getInstance().getItemsConfig();
        return !config.contains("customItems." + customItemName + ".canBePlaced") || config.getBoolean("customItems." + customItemName + ".canBePlaced", false);
    }

    private boolean isPlaceableMaterial(Material material) {
        if (material == null) return false;
        if (material.isBlock()) return true;
        return switch (material) {
            case PLAYER_HEAD, PLAYER_WALL_HEAD, CREEPER_HEAD, CREEPER_WALL_HEAD, DRAGON_HEAD, DRAGON_WALL_HEAD,
                 ZOMBIE_HEAD, ZOMBIE_WALL_HEAD, SKELETON_SKULL, SKELETON_WALL_SKULL, WITHER_SKELETON_SKULL,
                 WITHER_SKELETON_WALL_SKULL -> true;
            default -> false;
        };
    }

    public ItemStack custom(String itemName, Player player) {
        FileConfiguration config = ACore.getInstance().getItemsConfig();
        if (config.contains("customItems." + itemName)) {
            String name = Colors.set(config.getString("customItems." + itemName + ".name"));
            ItemStack item = getItemStack(itemName, config);
            if (item == null) {
                ACore.getInstance().getLogger().warning("Не удалось получить предмет для " + itemName);
                return null;
            }

            List<String> lore = getProcessedLore(config, itemName, player);
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(name);

                ConfigurationSection hideFlags = config.getConfigurationSection("customItems." + itemName + ".hideFlags");
                boolean hideAttributes = hideFlags != null && hideFlags.getBoolean("hideAttributes", false);
                boolean hideEnchantments = hideFlags != null && hideFlags.getBoolean("hideEnchantments", false);

                itemMeta.setLore(lore);

                if (config.contains("customItems." + itemName + ".enchantments")) {
                    for (String enchantment : config.getConfigurationSection("customItems." + itemName + ".enchantments").getKeys(false)) {
                        Enchantment ench = Enchantment.getByName(enchantment);
                        if (ench != null) {
                            int level = config.getInt("customItems." + itemName + ".enchantments." + enchantment);
                            itemMeta.addEnchant(ench, level, true);
                        }
                    }
                    if (hideEnchantments) itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                if (config.contains("customItems." + itemName + ".attributes")) {
                    ConfigurationSection attributeSection = config.getConfigurationSection("customItems." + itemName + ".attributes");
                    if (attributeSection != null) {
                        for (String attributeName : attributeSection.getKeys(false)) {
                            double value = attributeSection.getDouble(attributeName);
                            EquipmentSlot slot = getEquipmentSlot(config.getString("customItems." + itemName + ".slots.attributes"));
                            if (slot != null) {
                                AttributeCompat.addAttributeModifierSafe(itemMeta, attributeName, itemName, value, slot);
                            }
                        }
                        if (hideAttributes) itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    }
                }

                itemMeta.getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, itemName);

                this.applyPotionEffects(itemName);
                this.applyAttributes(itemName);

                item.setItemMeta(itemMeta);
                return item;
            }
        }
        ACore.getInstance().saveItemsConfig(config);
        return null;
    }

    private List<String> getProcessedLore(FileConfiguration config, String itemName, Player player) {
        List<String> rawLore = config.getStringList("customItems." + itemName + ".lore");
        List<String> processedLore = new ArrayList<>();
        for (String line : rawLore) {
            String coloredLine = Colors.set(line);
            String processedLine = processPlaceholders(coloredLine, player);
            processedLore.add(processedLine);
        }
        return processedLore;
    }

    private String processPlaceholders(String text, Player player) {
        if (text == null || text.isEmpty()) return text;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && player != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    public ItemStack getItemStack(String itemName, FileConfiguration config) {
        if (config.contains("customItems." + itemName)) {
            Material material;
            String materialName = config.getString("customItems." + itemName + ".material");
            if (materialName != null) {
                if (materialName.startsWith("hdb-")) {
                    HeadDatabaseAPI headDatabase = new HeadDatabaseAPI();
                    int headId = Integer.parseInt(materialName.substring(4));
                    return headDatabase.getItemHead(String.valueOf(headId));
                } else {
                    material = Material.getMaterial(materialName);
                    if (material != null) return new ItemStack(material);
                }
            }
        }
        return null;
    }

    public void applyPotionEffects(String itemName) {
        FileConfiguration config = ACore.getInstance().getItemsConfig();
        long delay = config.getLong("customItems." + itemName + ".delay");
        if (delay == 0) return;

        EquipmentSlot requiredSlot = getEquipmentSlot(config.getString("customItems." + itemName + ".slots" + ".effects"));
        if (requiredSlot == null) return;

        Bukkit.getScheduler().runTaskTimer(ACore.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack heldItem = player.getInventory().getItem(requiredSlot);
                if (isCustomItem(heldItem, itemName)) {
                    if (Methods.hasPermission(player, "acore.customs.effects")) {
                        applyCustomItemPotionEffects(player, itemName);
                    }
                } else {
                    removePotionEffects(player, itemName);
                }
            }
        }, delay, delay);
    }

    private void applyCustomItemPotionEffects(Player player, String itemName) {
        FileConfiguration config = ACore.getInstance().getItemsConfig();
        if (config.contains("customItems." + itemName + ".potionEffects")) {
            ConfigurationSection potionEffectsSection = config.getConfigurationSection("customItems." + itemName + ".potionEffects");
            if (potionEffectsSection != null) {
                for (String effectName : potionEffectsSection.getKeys(false)) {
                    int potency = potionEffectsSection.getInt(effectName);
                    PotionEffectType type = PotionEffectType.getByName(effectName);
                    if (type != null) {
                        player.setMetadata("customEffect_" + effectName, new FixedMetadataValue(ACore.getInstance(), itemName));
                        player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, potency));
                    }
                }
            }
        }
    }

    private void removePotionEffects(Player player, String itemName) {
        FileConfiguration config = ACore.getInstance().getItemsConfig();
        if (config.contains("customItems." + itemName + ".potionEffects")) {
            ConfigurationSection potionEffectsSection = config.getConfigurationSection("customItems." + itemName + ".potionEffects");
            if (potionEffectsSection != null) {
                for (String effectName : potionEffectsSection.getKeys(false)) {
                    PotionEffectType type = PotionEffectType.getByName(effectName);
                    if (type != null && player.hasPotionEffect(type)) {
                        if (player.hasMetadata("customEffect_" + effectName)) {
                            String sourceItem = player.getMetadata("customEffect_" + effectName).get(0).asString();
                            if (sourceItem.equals(itemName)) {
                                player.removePotionEffect(type);
                                player.removeMetadata("customEffect_" + effectName, ACore.getInstance());
                            }
                        }
                    }
                }
            }
        }
    }

    public void applyAttributes(String itemName) {
        FileConfiguration config = ACore.getInstance().getItemsConfig();
        long delay = config.getLong("customItems." + itemName + ".delay");
        if (delay == 0) return;

        EquipmentSlot requiredSlot = getEquipmentSlot(config.getString("customItems." + itemName + ".slots.attributes"));
        if (requiredSlot == null) return;

        Bukkit.getScheduler().runTaskTimer(ACore.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack heldItem = player.getInventory().getItem(requiredSlot);
                if (isCustomItem(heldItem, itemName)) {
                    applyCustomItemAttributes(player, itemName);
                } else {
                    removeCustomItemAttributes(player, itemName);
                }
            }
        }, delay, delay);
    }

    private void applyCustomItemAttributes(Player player, String itemName) {
        FileConfiguration config = ACore.getInstance().getItemsConfig();
        if (config.contains("customItems." + itemName + ".attributes")) {
            ConfigurationSection attributeSection = config.getConfigurationSection("customItems." + itemName + ".attributes");
            if (attributeSection != null) {
                for (String attributeName : attributeSection.getKeys(false)) {
                    double value = attributeSection.getDouble(attributeName);
                    Attribute attribute = Attribute.valueOf(attributeName);
                    if (attribute != null) {
                        AttributeInstance attributeInstance = player.getAttribute(attribute);
                        if (attributeInstance != null) {
                            UUID modifierUUID = UUID.nameUUIDFromBytes((itemName + attributeName).getBytes());
                            for (AttributeModifier existingModifier : attributeInstance.getModifiers()) {
                                if (existingModifier.getUniqueId().equals(modifierUUID)) {
                                    attributeInstance.removeModifier(existingModifier);
                                    break;
                                }
                            }
                            AttributeModifier modifier = new AttributeModifier(modifierUUID, itemName + "_" + attributeName, value, AttributeModifier.Operation.ADD_NUMBER);
                            attributeInstance.addModifier(modifier);
                            player.setMetadata("customAttribute_" + attributeName, new FixedMetadataValue(ACore.getInstance(), itemName));
                        }
                    }
                }
            }
        }
    }

    private void removeCustomItemAttributes(Player player, String itemName) {
        FileConfiguration config = ACore.getInstance().getItemsConfig();
        if (config.contains("customItems." + itemName + ".attributes")) {
            ConfigurationSection attributeSection = config.getConfigurationSection("customItems." + itemName + ".attributes");
            if (attributeSection != null) {
                for (String attributeName : attributeSection.getKeys(false)) {
                    Attribute attribute = Attribute.valueOf(attributeName);
                    if (attribute != null) {
                        AttributeInstance attributeInstance = player.getAttribute(attribute);
                        if (attributeInstance != null && player.hasMetadata("customAttribute_" + attributeName)) {
                            String sourceItem = player.getMetadata("customAttribute_" + attributeName).get(0).asString();
                            if (sourceItem.equals(itemName)) {
                                UUID modifierUUID = UUID.nameUUIDFromBytes((itemName + attributeName).getBytes());
                                for (AttributeModifier existingModifier : attributeInstance.getModifiers()) {
                                    if (existingModifier.getUniqueId().equals(modifierUUID)) {
                                        attributeInstance.removeModifier(existingModifier);
                                        break;
                                    }
                                }
                                player.removeMetadata("customAttribute_" + attributeName, ACore.getInstance());
                            }
                        }
                    }
                }
            }
        }
    }

    private EquipmentSlot getEquipmentSlot(String slotName) {
        if (slotName != null && !slotName.isEmpty()) {
            try {
                return EquipmentSlot.valueOf(slotName.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    public boolean isCustomItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(customItemKey, PersistentDataType.STRING);
    }

    private boolean isCustomItem(ItemStack item, String expectedItemName) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        String actualItemName = meta.getPersistentDataContainer().get(customItemKey, PersistentDataType.STRING);
        return expectedItemName != null && expectedItemName.equals(actualItemName);
    }

    public String getCustomItemName(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(customItemKey, PersistentDataType.STRING);
    }
}