/*
package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import akv5.acore.libs.Informer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Exit implements CommandExecutor {

    private final List<String> commands = ACore.getInstance().getConfig().getStringList("commands.exit.commands");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (player.hasPermission("acore.command.exit")) {
                if (cmd.getName().equalsIgnoreCase("exit") && args.length == 1) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        if (!target.hasPermission("acore.command.exit.protected")) {
                            for (String cmds : commands) {
                                String sendCmds = cmds.replace("{target}", target.getName());
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), sendCmds);
                            }

                            Location playerLocation = target.getLocation();
                            new BukkitRunnable() {
                                int secondsPassed = 0;

                                @Override
                                public void run() {
                                    if (secondsPassed >= 3 || !target.isOnline()) {
                                        this.cancel();
                                        return;
                                    }

                                    spawnFireworks(playerLocation);
                                    sendTitle(target);
                                    secondsPassed++;
                                }
                            }.runTaskTimer(ACore.getInstance(), 0L, 20L);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (target.isOnline()) {
                                        target.kickPlayer("");
                                    }
                                }
                            }.runTaskLater(ACore.getInstance(), 60L);

                            return true;
                        } else {
                            Informer.send(sender,
                                    ACore.getInstance().getConfig().getString("messages.isAdmin"));
                            return true;
                        }
                    } else {
                        Informer.send(sender, 
                                ACore.getInstance().getConfig().getString("messages.playerNotFound"));
                        return true;
                    }
                } else {
                    Informer.send(sender, 
                            ACore.getInstance().getConfig().getString("commands.exit.usage"));
                    return true;
                }
            } else {
                Informer.send(sender, 
                        ACore.getInstance().getConfig().getString("messages.noPermissions"));
                return true;
            }
        } else {
            return true;
        }
    }

    private void spawnFireworks(Location location) {
        for (int i = 0; i < 100; i++) {
            Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            fireworkMeta.addEffect(FireworkEffect.builder().withColor(getRandomColor()).build());
            firework.setFireworkMeta(fireworkMeta);
        }
    }

    private Color getRandomColor() {
        Color[] colors = {Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN, Color.ORANGE, Color.PURPLE};
        int randomIndex = (int) (Math.random() * colors.length);
        return colors[randomIndex];
    }

    private void sendTitle(Player player) {
        String title = Colors.set(
                ACore.getInstance().getConfig().getString("commands.exit.title.line1"));
        String subtitle = Colors.set(
                ACore.getInstance().getConfig().getString("commands.exit.title.line2"));

        int fadeInTime = ACore.getInstance().getConfig().getInt("commands.exit.title.fade-in-time");
        int stayTime = ACore.getInstance().getConfig().getInt("commands.exit.title.stay-time");
        int fadeOutTime = ACore.getInstance().getConfig().getInt("commands.exit.title.fade-out-time");

        player.sendTitle(title, subtitle, fadeInTime, stayTime, fadeOutTime);
    }
}
*/
