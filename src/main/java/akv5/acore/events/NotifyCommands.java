package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import akv5.acore.libs.Informer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class NotifyCommands implements Listener {

    private static final Pattern COLOR_PATTERN = Pattern.compile(
            "(?i)" +
                    "&[0-9A-FK-ORX]|" +
                    "§[0-9A-FK-ORX]|" +
                    "&#[0-9A-F]{6}|" +
                    "&\\s?[0-9A-FK-OR]|" +
                    "#[0-9A-Fa-f]{6}|" +
                    "\\{#[A-Za-z]\\}" +
                    "\\{#[0-9A-Fa-f]{6}\\}"
    );

    @EventHandler
    public void onPlayerCommandAdmins(PlayerCommandPreprocessEvent event) {
        if (ACore.getInstance().getConfig().getBoolean("notify.enabled")) {
            Player player = event.getPlayer();
            String command = event.getMessage().toLowerCase();
            List<String> commands = ACore.getInstance().getConfig().getStringList("notify.commands");

            for (String cmds : commands) {
                if (command.startsWith(cmds)) {
                    notifyAdmins(player, command);
                }
            }
        }
    }

    private void notifyAdmins(Player sender, String command) {
        String notificationMessage = Objects.requireNonNull(ACore.getInstance().getConfig().getString("notify.message"))
                .replace("{player}", sender.getName())
                .replace("{message}", command);

        String notify = Colors.set("{player} &f» {message}.")
                .replace("{player}", sender.getName())
                .replace("{message}", command);

        String strippedMessage = ChatColor.stripColor(notify);
        strippedMessage = COLOR_PATTERN.matcher(strippedMessage).replaceAll("")
                .replace(">", "")
                .replace("<", "")
                .replace("{", "")
                .replace("}", "")
                .replace("name", "<name>");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("acore.notify.commands")) {
                Informer.send(player, notificationMessage);
            }
        }
    }
}
