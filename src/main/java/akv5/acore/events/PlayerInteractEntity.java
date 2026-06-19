/*
package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlayerInteractEntity implements Listener, CommandExecutor {

    private final Map<Entity, Boolean> mobFollowMap = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (entity instanceof Fox fox && player.getInventory().getItemInMainHand().getType() == Material.SWEET_BERRIES) {
            toggleFollow(fox, player);
        } else if (entity instanceof Turtle turtle && player.getInventory().getItemInMainHand().getType() == Material.SEAGRASS) {
            toggleFollow(turtle, player);
        } else if (entity instanceof Bee bee && player.getInventory().getItemInMainHand().getType() == Material.POPPY) {
            toggleFollow(bee, player);
        }
    }

    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent event) {
        if (event.getAction().toString().contains("LEFT_CLICK") && event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
            Player player = event.getPlayer();

            for (Entity mob : mobFollowMap.keySet()) {
                if (mobFollowMap.get(mob)) {
                    changeMob(mob, player);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (command.getName().equalsIgnoreCase("mob")) {
                if (args.length == 1) {
                    return switch (args[0].toLowerCase()) {
                        case "fox" -> {
                            spawnMob(player, Fox.class);
                            yield true;
                        }
                        case "turtle" -> {
                            spawnMob(player, Turtle.class);
                            yield true;
                        }
                        case "bee" -> {
                            spawnMob(player, Bee.class);
                            yield true;
                        }
                        default -> {
                            Informer.send(player, ACore.getInstance().getConfig().getString("mobs.type"));
                            yield false;
                        }
                    };
                } else {
                    Informer.send(player, ACore.getInstance().getConfig().getString("mobs.type"));
                    return false;
                }
            }
        }
        return false;
    }

    public <T extends Entity> void spawnMob(Player player, Class<T> mobClass) {
        T mob = player.getWorld().spawn(player.getLocation(), mobClass);
        if (mob instanceof Fox) {
            ((Fox) mob).setBaby();
        }
        Informer.send(player, ACore.getInstance().getConfig().getString("mobs." + mob.getType().toString().toLowerCase() + ".spawn"));
        mobFollowMap.put(mob, false);
    }

    private void toggleFollow(Entity mob, Player player) {
        boolean isFollowing = mobFollowMap.getOrDefault(mob, false);

        if (isFollowing) {
            mobFollowMap.put(mob, false);
            Informer.send(player, ACore.getInstance().getConfig().getString("mobs." + mob.getType().toString().toLowerCase() + ".noFollow"));
        } else {
            mobFollowMap.put(mob, true);
            Informer.send(player, ACore.getInstance().getConfig().getString("mobs." + mob.getType().toString().toLowerCase() + ".followMe"));
            startFollowing(mob, player);
        }
    }

    private void startFollowing(Entity mob, Player player) {
        Bukkit.getScheduler().runTaskTimer(ACore.getInstance(), () -> {
            if (mob.isDead() || player.isDead() || !mobFollowMap.getOrDefault(mob, false)) {
                return;
            }
            mob.teleport(player.getLocation().add(1, 0, 1));
        }, 0L, 0L);
    }

    private void changeMob(Entity oldMob, Player player) {
        mobFollowMap.remove(oldMob);
        oldMob.remove();

        Entity newMob = null;

        if (oldMob instanceof Fox) {
            newMob = player.getWorld().spawn(player.getLocation(), Turtle.class);
            ((Turtle) newMob).setBaby();
        } else if (oldMob instanceof Turtle) {
            newMob = player.getWorld().spawn(player.getLocation(), Bee.class);
        } else if (oldMob instanceof Bee) {
            newMob = player.getWorld().spawn(player.getLocation(), Fox.class);
            ((Fox) newMob).setBaby();
        }

        if (newMob != null) {
            mobFollowMap.put(newMob, true);
            Informer.send(player, ACore.getInstance().getConfig().getString("mobs." + newMob.getType().toString().toLowerCase() + ".changeClick"));
            startFollowing(newMob, player);
        }
    }

}
*/
