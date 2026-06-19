package akv5.acore.trolls;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class LiftTroll implements CommandExecutor {

    String perms = ACore.getInstance().getConfig().getString("messages.noPermissions");
    String playerNotFound = ACore.getInstance().getConfig().getString("messages.playerNotFound");
    String protect = ACore.getInstance().getConfig().getString("trolls.protected");
    String usage = ACore.getInstance().getConfig().getString("trolls.lifttroll.usage");
    String success = ACore.getInstance().getConfig().getString("trolls.lifttroll.success");
    String invalidNumber = ACore.getInstance().getConfig().getString("trolls.lifttroll.invalidNumber");
    String liftedSelf = ACore.getInstance().getConfig().getString("trolls.lifttroll.liftedSelf");
    String limitExceeded = ACore.getInstance().getConfig().getString("trolls.lifttroll.limitExceeded");
    private static final double MAX_POWER = ACore.getInstance().getConfig().getDouble("trolls.lifttroll.maxPower", 10.0);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!Methods.hasPermission(sender, "acore.trolls.lifttroll")) {
            Informer.send(sender, perms);
            return false;
        }

        if (args.length >= 1) {
            String playerName = args[0];
            Player target = Bukkit.getPlayer(playerName);

            if (target != null && target.isOnline()) {
                if (Methods.isProtected(target)) {
                    Informer.send(sender, protect);
                    return true;
                }

                if (sender instanceof Player && ((Player) sender).equals(target)) {
                    Informer.send(sender, liftedSelf);
                    return true;
                }

                double power = 2.5;

                if (args.length >= 2) {
                    try {
                        power = Double.parseDouble(args[1]);

                        if (power > MAX_POWER) {
                            Informer.send(sender, limitExceeded.replace("{max}", String.valueOf(MAX_POWER)));
                            return true;
                        }

                        if (power < 0) {
                            Informer.send(sender, invalidNumber);
                            return true;
                        }

                    } catch (NumberFormatException e) {
                        Informer.send(sender, invalidNumber);
                        return true;
                    }
                }

                liftPlayer(target, power);

                String successMsg = success
                        .replace("{player}", target.getName())
                        .replace("{power}", String.valueOf(power));
                Informer.send(sender, successMsg);

                return true;
            } else {
                Informer.send(sender, playerNotFound);
            }
        } else {
            Informer.send(sender, usage);
        }
        return false;
    }

    private void liftPlayer(Player player, double power) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setVelocity(player.getVelocity().setY(0));
                player.setVelocity(player.getVelocity().add(player.getLocation().getDirection().multiply(0).setY(power)));
            }
        }.runTaskLater(ACore.getInstance(), 0);
    }
}