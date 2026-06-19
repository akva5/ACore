package akv5.acore.trolls;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import akv5.acore.placeholders.Placeholders;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static me.clip.placeholderapi.PlaceholderAPI.*;

public class BanTroll implements CommandExecutor {

    String perms = ACore.getInstance().getConfig().getString("messages.noPermissions");
    String playerNotFound = ACore.getInstance().getConfig().getString("messages.playerNotFound");

    String usage = ACore.getInstance().getConfig().getString("trolls.bantroll.usage");
    String reason = ACore.getInstance().getConfig().getString("trolls.bantroll.reason");
    String success = ACore.getInstance().getConfig().getString("trolls.bantroll.success");
    String protect = ACore.getInstance().getConfig().getString("trolls.protected");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (Methods.hasPermission(sender, "acore.trolls.bantroll")) {
            if (args.length >= 1) {
                String playerName = args[0];
                Player target = Bukkit.getPlayer(playerName);

                if (target != null && target.isOnline()) {
                    if (Methods.isProtected(target)) {
                        Informer.send(sender, protect);
                        return true;
                    }

                    String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date());

                    List<String> messageList = ACore.getInstance().getConfig().getStringList("trolls.bantroll.message");

                    StringBuilder messageBuilder = new StringBuilder();
                    for (String line : messageList) {
                        String formattedLine = line
                                .replace("{executor}", sender.getName())
                                .replace("{date}", date)
                                .replace("{reason}", reason);
                        messageBuilder.append(formattedLine).append("\n");
                    }

                    if (!messageBuilder.isEmpty()) {
                        messageBuilder.setLength(messageBuilder.length() - 1);
                    }

                    target.kickPlayer(Colors.set(messageBuilder.toString()));

                    Informer.send(sender, success.replace("{player}", target.getName()));
                } else {
                    Informer.send(sender, playerNotFound);
                }
            } else {
                Informer.send(sender, usage);
            }
        } else {
            Informer.send(sender, perms);
        }
        return false;
    }
}
