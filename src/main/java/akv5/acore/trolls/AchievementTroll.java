package akv5.acore.trolls;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import akv5.acore.utils.Broadcast;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class AchievementTroll implements CommandExecutor {

    String perms = ACore.getInstance().getConfig().getString("messages.noPermissions");
    String playerNotFound = ACore.getInstance().getConfig().getString("messages.playerNotFound");
    String usage = ACore.getInstance().getConfig().getString("trolls.achievementtroll.usage");
    String protect = ACore.getInstance().getConfig().getString("trolls.protected");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!Methods.hasPermission(sender, "acore.trolls.achievementtroll")) {
            Informer.send(sender, perms);
            return false;
        }

        if (args.length >= 2) {
            String playerName = args[0];
            Player target = Bukkit.getPlayer(playerName);

            if (target != null && target.isOnline()) {
                if (Methods.isProtected(target)) {
                    Informer.send(sender, protect);
                    return true;
                }

                String[] messageArgs = Arrays.copyOfRange(args, 1, args.length);
                Broadcast broadcast = new Broadcast();
                broadcast.send(playerName, messageArgs);
                return true;
            } else {
                Informer.send(sender, playerNotFound);
            }
        } else {
            Informer.send(sender, usage);
        }
        return false;
    }
}