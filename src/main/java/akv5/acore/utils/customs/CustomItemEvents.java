package akv5.acore.utils.customs;

import akv5.acore.ACore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class CustomItemEvents implements Listener {

    private static CustomItemEvents instance;

    private final Map<String, BiConsumer<Player, ConfigurationSection>> actionRegistry = new HashMap<>();
    private final Map<String, BiPredicate<Object, String>> conditionRegistry = new HashMap<>();
    private final Map<String, List<EventRule>> itemRules = new ConcurrentHashMap<>();

    public static CustomItemEvents getInstance() {
        if (instance == null) {
            instance = new CustomItemEvents();
        }
        return instance;
    }

    public void initialize() {
        registerActions();
        registerConditions();
        loadEvents();
        ACore.getInstance().getLogger().info("CustomItemEvents engine loaded with " + itemRules.size() + " item configurations.");
    }

    public void reload() {
        itemRules.clear();
        loadEvents();
    }

    private void registerActions() {
        actionRegistry.put("execute_command", (p, cfg) -> {
            String cmd = cfg.getString("command", "").replace("{player}", p.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        });

        actionRegistry.put("teleport", (p, cfg) -> {
            String[] coords = cfg.getString("location", "0,0,0").split(",");
            if (coords.length == 3) {
                try {
                    p.teleport(new Location(p.getWorld(), Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2])));
                } catch (NumberFormatException ignored) {}
            }
        });

        actionRegistry.put("spawn_particles", (p, cfg) -> {
            try {
                Particle particle = Particle.valueOf(cfg.getString("type", "FLAME"));
                p.getWorld().spawnParticle(particle, p.getLocation().add(0, 1, 0), cfg.getInt("count", 10));
            } catch (IllegalArgumentException ignored) {}
        });

        actionRegistry.put("lightning_strike", (p, cfg) -> p.getWorld().strikeLightning(p.getLocation()));

        actionRegistry.put("apply_effects", (p, cfg) -> {
            ConfigurationSection effects = cfg.getConfigurationSection("effects");
            if (effects != null) {
                for (String key : effects.getKeys(false)) {
                    PotionEffectType type = PotionEffectType.getByName(key);
                    if (type != null) {
                        p.addPotionEffect(new PotionEffect(type, 200, effects.getInt(key)));
                    }
                }
            }
        });

        actionRegistry.put("explosion", (p, cfg) ->
                p.getWorld().createExplosion(p.getLocation(), (float) cfg.getDouble("power", 4.0), cfg.getBoolean("break_blocks", false))
        );

        actionRegistry.put("summon_mob", (p, cfg) -> {
            try {
                String typeStr = cfg.getString("mob_type", "ZOMBIE");
                EntityType type = EntityType.valueOf(typeStr);
                p.getWorld().spawnEntity(p.getLocation(), type);
            } catch (Exception ignored) {}
        });

        actionRegistry.put("moon_blessing", (p, cfg) -> {
            if (cfg == null) return;
            if (cfg.getBoolean("night_vision", false)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0));
            }
            int speed = cfg.getInt("speed", 0);
            if (speed > 0) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, speed - 1));
            }

            ConfigurationSection effects = cfg.getConfigurationSection("effects");
            if (effects != null) {
                for (String key : effects.getKeys(false)) {
                    PotionEffectType type = PotionEffectType.getByName(key);
                    if (type != null) {
                        p.addPotionEffect(new PotionEffect(type, 400, effects.getInt(key)));
                    }
                }
            }
        });

        actionRegistry.put("sun_power", (p, cfg) -> {
            if (cfg == null) return;
            int strength = cfg.getInt("strength_boost", 0);
            if (strength > 0) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.getByName("INCREASE_DAMAGE"), 400, strength - 1));
            }

            ConfigurationSection effects = cfg.getConfigurationSection("effects");
            if (effects != null) {
                for (String key : effects.getKeys(false)) {
                    PotionEffectType type = PotionEffectType.getByName(key);
                    if (type != null) {
                        p.addPotionEffect(new PotionEffect(type, 400, effects.getInt(key)));
                    }
                }
            }
        });
    }

    private void registerConditions() {
        conditionRegistry.put("action", (obj, val) -> {
            if (obj instanceof PlayerInteractEvent e) return e.getAction().name().equalsIgnoreCase(val);
            return false;
        });

        conditionRegistry.put("is_day", (obj, val) -> {
            if (obj instanceof Long time) {
                boolean isDay = time < 12300 || time > 23850;
                return isDay == Boolean.parseBoolean(val);
            }
            return false;
        });

        conditionRegistry.put("is_night", (obj, val) -> {
            if (obj instanceof Long time) {
                boolean isNight = time >= 12300 && time <= 23850;
                return isNight == Boolean.parseBoolean(val);
            }
            return false;
        });

        conditionRegistry.put("sneaking", (obj, val) -> {
            if (obj instanceof PlayerToggleSneakEvent e) return e.isSneaking() == Boolean.parseBoolean(val);
            return false;
        });

        conditionRegistry.put("player_health", (obj, val) -> {
            if (obj instanceof EntityDamageByEntityEvent e && e.getEntity() instanceof Player p) {
                return compareDouble(p.getHealth(), val);
            }
            return false;
        });

        conditionRegistry.put("is_critical", (obj, val) -> {
            if (obj instanceof EntityDamageByEntityEvent e && e.getDamager() instanceof Player p) {
                boolean isCrit = p.getFallDistance() > 0.0F && !p.isOnGround() && !p.isInsideVehicle();
                return isCrit == Boolean.parseBoolean(val);
            }
            return false;
        });
    }

    private boolean compareDouble(double actual, String criteria) {
        if (criteria.startsWith("<")) return actual < Double.parseDouble(criteria.substring(1));
        if (criteria.startsWith(">")) return actual > Double.parseDouble(criteria.substring(1));
        return actual == Double.parseDouble(criteria);
    }

    private void loadEvents() {
        FileConfiguration config = ACore.getInstance().getItemsConfig();
        ConfigurationSection itemsSection = config.getConfigurationSection("customItems");
        if (itemsSection == null) return;

        for (String itemName : itemsSection.getKeys(false)) {
            ConfigurationSection eventsSection = config.getConfigurationSection("customItems." + itemName + ".events");
            if (eventsSection == null) continue;

            List<EventRule> rules = new ArrayList<>();
            for (String eventKey : eventsSection.getKeys(false)) {
                ConfigurationSection sec = eventsSection.getConfigurationSection(eventKey);
                if (sec == null) continue;

                rules.add(new EventRule(
                        sec.getString("type"),
                        sec.getString("slot", "ANY"),
                        sec.getStringList("conditions"),
                        sec.getStringList("actions"),
                        sec.getConfigurationSection("actionParams")
                ));
            }
            itemRules.put(itemName, rules);
        }
    }

    private void trigger(Player player, String triggerType, Object eventObject) {
        if (player == null) return;

        checkItemAndExecute(player, player.getInventory().getItemInMainHand(), EquipmentSlot.HAND, triggerType, eventObject);
        checkItemAndExecute(player, player.getInventory().getItemInOffHand(), EquipmentSlot.OFF_HAND, triggerType, eventObject);
    }

    private void checkItemAndExecute(Player player, ItemStack item, EquipmentSlot slot, String triggerType, Object eventObject) {
        if (!CustomItems.getInstance().isCustomItem(item)) return;

        String itemName = CustomItems.getInstance().getCustomItemName(item);
        List<EventRule> rules = itemRules.get(itemName);
        if (rules == null) return;

        for (EventRule rule : rules) {
            if (!rule.triggerType().equalsIgnoreCase(triggerType)) continue;

            if (!rule.slot().equalsIgnoreCase("ANY")) {
                if (rule.slot().equalsIgnoreCase("HAND") && slot != EquipmentSlot.HAND) continue;
                if (rule.slot().equalsIgnoreCase("OFF_HAND") && slot != EquipmentSlot.OFF_HAND) continue;
            }

            boolean conditionsMet = true;
            for (String condStr : rule.conditions()) {
                String[] parts = condStr.split(":", 2);
                if (parts.length < 2) continue;

                BiPredicate<Object, String> predicate = conditionRegistry.get(parts[0]);
                if (predicate != null && !predicate.test(eventObject, parts[1])) {
                    conditionsMet = false;
                    break;
                }
            }
            if (!conditionsMet) continue;

            for (String actStr : rule.actions()) {
                String[] parts = actStr.split(":", 2);
                BiConsumer<Player, ConfigurationSection> action = actionRegistry.get(parts[0]);
                if (action != null) {
                    ConfigurationSection params = (parts.length > 1 && rule.actionParams() != null)
                            ? rule.actionParams().getConfigurationSection(parts[1])
                            : null;
                    action.accept(player, params);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            trigger(event.getPlayer(), "PlayerInteractEvent", event);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p) {
            trigger(p, "EntityDamageByEntityEvent", event);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            trigger(event.getEntity().getKiller(), "EntityDeathEvent", event);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        trigger(event.getPlayer(), "PlayerToggleSneakEvent", event);
    }

    @EventHandler
    public void onSprint(PlayerToggleSprintEvent event) {
        trigger(event.getPlayer(), "PlayerToggleSprintEvent", event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        trigger(event.getPlayer(), "BlockBreakEvent", event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        trigger(event.getPlayer(), "BlockPlaceEvent", event);
    }

    public void handleTimeChange(long time) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            trigger(p, "TimeChangeEvent", time);
        }
    }

    private record EventRule(String triggerType, String slot, List<String> conditions, List<String> actions, ConfigurationSection actionParams) {}
}