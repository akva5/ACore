package akv5.alib;

import akv5.acore.libs.Colors;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Plugins;
import akv5.alib.hook.ItemsAdderHook;
import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import dev.lone.itemsadder.api.CustomStack;
import me.clip.placeholderapi.PlaceholderAPI;
import akv5.alib.controller.ItemsController;
import akv5.alib.controller.StorageController;
import akv5.alib.data.BukkitVersion;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemBuilder {
    private Material material;
    private final int damage;
    private String name;
    private final List<String> lore;
    private int amount;
    private final Map<Enchantment, Integer> enchantments;
    private boolean unbreakable;
    private boolean hideItemFlags;
    private boolean glowing;
    private final ItemStack referenceItem;
    private final PotionType potionType;
    private final Color potionColor;
    private boolean isPotion;
    private final Color armorColor;
    private boolean isLeatherArmor;
    private final List<Pattern> patterns;
    private boolean isBanner;
    private boolean isShield;
    private Integer customModelData;
    private boolean useCustomModelData;
    private String itemModel;
    private boolean useItemModel;
    private final List<ItemFlag> itemFlags;
    private final Map<String, String> nbt;

    public ItemBuilder() {
        this.material           = Material.STONE;
        this.damage             = 0;
        this.name               = "";
        this.lore               = Lists.newArrayList();
        this.amount             = 1;
        this.enchantments       = new ConcurrentHashMap<>();
        this.unbreakable        = false;
        this.hideItemFlags      = false;
        this.glowing            = false;
        this.referenceItem      = null;
        this.potionType         = null;
        this.potionColor        = null;
        this.isPotion           = false;
        this.armorColor         = null;
        this.isLeatherArmor     = false;
        this.patterns           = Lists.newArrayList();
        this.isBanner           = false;
        this.isShield           = false;
        this.customModelData    = 0;
        this.useCustomModelData = false;
        this.itemModel          = "";
        this.useItemModel       = false;
        this.itemFlags          = Lists.newArrayList();
        this.nbt                = new ConcurrentHashMap<>();
    }

    public ItemBuilder(ItemBuilder itemBuilder) {
        this.material = itemBuilder.material;
        this.damage = itemBuilder.damage;
        this.name = itemBuilder.name;
        this.lore = Lists.newArrayList(itemBuilder.lore);
        this.amount = itemBuilder.amount;
        this.enchantments = new ConcurrentHashMap<>(itemBuilder.enchantments);
        this.unbreakable = itemBuilder.unbreakable;
        this.hideItemFlags = itemBuilder.hideItemFlags;
        this.glowing = itemBuilder.glowing;
        this.referenceItem = itemBuilder.referenceItem;
        this.potionType = itemBuilder.potionType;
        this.potionColor = itemBuilder.potionColor;
        this.isPotion = itemBuilder.isPotion;
        this.armorColor = itemBuilder.armorColor;
        this.isLeatherArmor = itemBuilder.isLeatherArmor;
        this.patterns = Lists.newArrayList(itemBuilder.patterns);
        this.isBanner = itemBuilder.isBanner;
        this.isShield = itemBuilder.isShield;
        this.customModelData = itemBuilder.customModelData;
        this.useCustomModelData = itemBuilder.useCustomModelData;
        this.itemModel = itemBuilder.itemModel;
        this.useItemModel = itemBuilder.useItemModel;
        this.itemFlags = Lists.newArrayList(itemBuilder.itemFlags);
        this.nbt = new ConcurrentHashMap<>(itemBuilder.nbt);
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ItemBuilder setMaterial(Material material) {
        if (material != null) this.material = material;
        switch (this.material) {
            case POTION:
            case SPLASH_POTION:
            case LINGERING_POTION:
                this.isPotion = true;
                break;
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                this.isLeatherArmor = true;
                break;
            case SHIELD:
                this.isShield = true;
                break;
        }
        if (this.material.name().contains("BANNER")) this.isBanner = true;
        return this;
    }

    public ItemBuilder setMaterial(String material) {
        return setMaterial(material, null);
    }

    @SuppressWarnings("UnreachableCode")
    public ItemBuilder setMaterial(String material, Player player) {
        material = PlaceholderAPI.setPlaceholders(player, material);
        if (material.contains(":")) {
            if (Plugins.ItemsAdder.isEnabled()) {
                CustomStack customStack = ItemsAdderHook.getCustomItem(material);
                if (customStack != null) {
                    ItemStack itemStack = customStack.getItemStack();
                    material = itemStack.getType().name();
                    if (Tools.requireBukkitVersion(BukkitVersion.V1_21) && ItemsAdderHook.hasItemModel(customStack)) {
                        setItemModel(ItemsAdderHook.getItemModel(customStack));
                    } else {
                        setCustomModelData(ItemsAdderHook.getCustomModelData(customStack));
                    }
                }
            }
        }
        if (material.contains(":")) material = Material.STONE.name();
        setMaterial(Material.matchMaterial(material));
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ItemBuilder setCustomModelData(Integer integer) {
        this.useCustomModelData = integer != null && integer > 0;
        this.customModelData = integer;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ItemBuilder setItemModel(String modelPath) {
        if (!Tools.requireBukkitVersion(BukkitVersion.V1_21)) return this;
        this.useItemModel = modelPath != null && !modelPath.isEmpty();
        this.itemModel = modelPath;
        return this;
    }

    public ItemBuilder setName(String name) {
        if (name != null) this.name = name;
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.lore.clear();
        if (lore != null) this.lore.addAll(lore);
        return this;
    }

    public void addPattern(String stringPattern) {
        try {
            String[] split = stringPattern.split(":");
            for (PatternType pattern : PatternType.values()) {
                if (split[0].equalsIgnoreCase(pattern.name())) {
                    DyeColor color = getDyeColor(split[1]);
                    if (color != null) addPattern(new Pattern(color, pattern));
                    break;
                }
            }
        } catch (Throwable ignored) {}
    }

    public ItemBuilder addPatterns(List<String> stringList) {
        stringList.forEach(this::addPattern);
        return this;
    }

    public void addPattern(Pattern pattern) {
        patterns.add(pattern);
    }

    public ItemBuilder setAmount(Integer amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder addEnchantments(Enchantment enchantment, Integer level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemBuilder hideItemFlags(boolean hideItemFlags) {
        this.hideItemFlags = hideItemFlags;
        return this;
    }

    public ItemBuilder setGlowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }

    public ItemBuilder addItemFlags(List<String> flagStrings) {
        for (String flagString : flagStrings) {
            try {
                ItemFlag itemFlag = ItemFlag.valueOf(flagString.toUpperCase());
                addItemFlag(itemFlag);
            } catch (Throwable ignored) {}
        }
        return this;
    }

    public ItemBuilder addNBT(String key, boolean value) {
        this.nbt.put(key, String.valueOf(value));
        return this;
    }

    public ItemBuilder addNBT(String key, String value) {
        this.nbt.put(key, value);
        return this;
    }

    public void addItemFlag(ItemFlag itemFlag) {
        if (itemFlag == null) return;
        itemFlags.add(itemFlag);
    }

    public ItemStack build() {
        return build(null);
    }

    public ItemStack build(Player player) {
        ItemStack item = referenceItem != null ? referenceItem : new ItemStack(material);
        if (item.getType() == Material.AIR) return item;
        item.setAmount(amount);
        ItemMeta itemMeta = item.getItemMeta();

        String parsedName = PlaceholderAPI.setPlaceholders(player, name);
        if (parsedName != null && !parsedName.isEmpty()) {
            BaseComponent[] components = Informer.parseComponent(Colors.set(parsedName));
            if (components.length > 0) {
                itemMeta.setDisplayName(components[0].toPlainText());
            }
        }

        if (lore != null && !lore.isEmpty()) {
            List<String> parsedLore = new ArrayList<>();
            for (String line : lore) {
                String parsedLine = PlaceholderAPI.setPlaceholders(player, line);
                if (parsedLine != null) {
                    parsedLore.add(Colors.set(parsedLine));
                }
            }
            if (!parsedLore.isEmpty()) {
                itemMeta.setLore(parsedLore);
            }
        }

        itemMeta.setUnbreakable(unbreakable);
        if (itemMeta instanceof Damageable) ((Damageable) itemMeta).setDamage(damage);
        if (isPotion && (potionType != null || potionColor != null)) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            if (potionColor != null) {
                potionMeta.setColor(potionColor);
            }
        }
        if (isLeatherArmor && armorColor != null) {
            LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemMeta;
            leatherMeta.setColor(armorColor);
        }
        if (isBanner && !patterns.isEmpty()) {
            BannerMeta bannerMeta = (BannerMeta) itemMeta;
            bannerMeta.setPatterns(patterns);
        }
        if (isShield && !patterns.isEmpty()) {
            BlockStateMeta shieldMeta = (BlockStateMeta) itemMeta;
            Banner banner = (Banner) shieldMeta.getBlockState();
            banner.setPatterns(patterns);
            banner.update();
            shieldMeta.setBlockState(banner);
        }
        if (useCustomModelData) itemMeta.setCustomModelData(customModelData);
        itemFlags.forEach(itemMeta::addItemFlags);
        item.setItemMeta(itemMeta);
        hideFlags(item);
        item.addUnsafeEnchantments(enchantments);
        if (glowing) ItemsController.toggleGlowing(item, true);

        NBT.modify(item, nbt -> {
            ReadWriteNBT compound = nbt.getOrCreateCompound(StorageController.getGuiNBT());
            this.nbt.forEach((key, value) -> {
                if (value.equals("true") || value.equals("false")) {
                    compound.setBoolean(key, Tools.parseBoolean(value));
                } else compound.setString(key, value);
            });
        });

        if (Tools.requireBukkitVersion(BukkitVersion.V1_21) && useItemModel) {
            NBT.modifyComponents(item, nbt -> {
                nbt.setString("minecraft:item_model", itemModel);
            });
        }

        return item;
    }

    private void hideFlags(ItemStack item) {
        if (!hideItemFlags || item == null || !item.hasItemMeta()) return;

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.addItemFlags(ItemFlag.values());
        }
        item.setItemMeta(itemMeta);
    }

    private static DyeColor getDyeColor(String color) {
        if (color == null) return null;
        try {
            return DyeColor.valueOf(color.toUpperCase());
        } catch (Throwable e) {
            try {
                String[] rgb = color.split(",");
                return DyeColor.getByColor(Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
            } catch (Throwable ignore) {}
        }
        return null;
    }
}