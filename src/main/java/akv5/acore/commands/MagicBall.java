package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import akv5.acore.libs.Cooldowner;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import akv5.acore.utils.Bot;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MagicBall implements CommandExecutor, Listener {

    private final String[] responses = {
            "Да.",
            "Нет.",
            "Не уверен.",
            "Определенно да.",
            "Определенно нет.",
            "Спроси позже.",
            "Может быть.",
            "Наверняка.",
            "Я бы не рассчитывал на это.",
            "Без сомнений."
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("8ball")) {
            if (sender instanceof Player player) {

                if (args.length == 0) {
                    Informer.send(player, "{prefix}Вы не задали вопрос!");
                    return true;
                }

                String question = String.join(" ", args);

                Random random = new Random();
                String response = responses[random.nextInt(responses.length)];

                Informer.send(player, "&7\n  &dМагический шар" + "\n" + "  &fВопрос &a" + question + " &fбыл задан!"
                        + "\n" + "  &fОтвет: &a" + response + "\n&7");

                Bot.sendMsg("Игрок " + player.getName() + " задал вопрос магическому шару!" +
                        "\n" + "Вопрос: " + question +
                        "\n" + "Ответ: " + response);

                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (Methods.hasPermission(admin, "acore.notify.8ball")) {
                        String message = "&7\n  &dМагический шар" + "\n" + "  &fИгрок &e" + player.getName() + " &fзадал вопрос!" +
                                "\n" + "  &fВопрос &a" + question + "\n" + "  &fОтвет: &a" + response + "\n&7";
                        Informer.send(admin, message);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        int chatCooldown = 10;

        if (command.startsWith("/8ball")) {
            if (Cooldowner.isInCooldown(player.getName(), Cooldowner.Type.MESSAGE)) {
                event.setCancelled(true);
                String msg = ACore.getInstance().getConfig().getString("messages.cooldown-cmds");
                Informer.send(player, msg);
                return;
            }

            Cooldowner.start(player.getName(), Cooldowner.Type.MESSAGE, chatCooldown);
        }
    }
}
