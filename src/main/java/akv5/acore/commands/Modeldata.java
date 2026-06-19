package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import akv5.acore.utils.customs.models.CustomModelDataUI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Modeldata implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!Methods.hasPermission(sender, "acore.command.modeldata")) {
            Informer.send(player, ACore.getInstance().getConfig().getString("messages.noPermissions"));
            return true;
        }

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            Informer.send(player,
                    ACore.getInstance().getConfig().getString("messages.iteminhand"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        CustomModelDataUI.open(player, item, 1);
        return false;
    }
}
