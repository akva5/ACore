package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import akv5.acore.utils.customs.CustomItemEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Areload implements CommandExecutor {

    String perms = ACore.getInstance().getConfig().getString("messages.noPermissions");
    String reloaded = ACore.getInstance().getConfig().getString("messages.reloaded");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!Methods.hasPermission(sender, "acore.reload")) {
            Informer.send(sender, perms);
            return false;
        }

        // убрать потом
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");

        ACore.getInstance().reloadConfig();
        ACore.getInstance().reloadInfoConfig();
        ACore.getInstance().getItemsConfig();
        CustomItemEvents.getInstance().reload();

        Informer.send(sender, reloaded);
        return true;
    }
}