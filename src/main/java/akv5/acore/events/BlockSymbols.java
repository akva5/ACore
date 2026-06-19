package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockSymbols implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (ACore.getInstance().getConfig().getBoolean("block.SymbolChat.enabled")) {
            Player player = event.getPlayer();
            String message = event.getMessage().toLowerCase();
            List<String> blockedSymbols = ACore.getInstance().getConfig().getStringList("block.SymbolChat.symbols");

            for (String blockedSymbol : blockedSymbols) {
                Pattern pattern = Pattern.compile(blockedSymbol, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(message);
                if (matcher.find()) {
                    event.setCancelled(true);
                    Informer.send(player, ACore.getInstance().getConfig().getString("block.SymbolChat.message"));

                    String notificationMessage = Objects.requireNonNull(ACore.getInstance().getConfig().getString("block.SymbolChat.notify"))
                            .replace("{player}", player.getName())
                            .replace("{message}", message);
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission("acore.notify.symbolchat")) {
                            Informer.send(onlinePlayer, notificationMessage);
                        }
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (ACore.getInstance().getConfig().getBoolean("block.SymbolCommands.enabled")) {
            Player player = event.getPlayer();
            String command = event.getMessage().toLowerCase();
            List<String> blockedCommands = ACore.getInstance().getConfig().getStringList("block.SymbolCommands.commands");
            List<String> blockedWords = ACore.getInstance().getConfig().getStringList("block.SymbolCommands.blockedWords");
            List<String> blockedRegex = ACore.getInstance().getConfig().getStringList("block.SymbolCommands.blockedRegex");
            List<String> blockedSymbols = ACore.getInstance().getConfig().getStringList("block.SymbolCommands.symbols");

            boolean isBlockedCommand = blockedCommands.stream().anyMatch(command::startsWith);

            if (isBlockedCommand) {
                for (String blockedWord : blockedWords) {
                    if (command.contains(blockedWord.toLowerCase())) {
                        event.setCancelled(true);
                        Informer.send(player, ACore.getInstance().getConfig().getString("block.SymbolCommands.message"));
                        return;
                    }
                }

                for (String regex : blockedRegex) {
                    if (Pattern.matches(regex, command)) {
                        event.setCancelled(true);
                        Informer.send(player, ACore.getInstance().getConfig().getString("block.SymbolCommands.message"));
                        String notificationMessage = Objects.requireNonNull(ACore.getInstance().getConfig().getString("block.SymbolCommands.notify"))
                                .replace("{player}", player.getName())
                                .replace("{message}", command);
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            if (onlinePlayer.hasPermission("acore.notify.symbolcommands")) {
                                Informer.send(onlinePlayer, notificationMessage);
                            }
                        }
                        return;
                    }
                }

                for (String blockedSymbol : blockedSymbols) {
                    if (command.contains(blockedSymbol)) {
                        event.setCancelled(true);
                        Informer.send(player, ACore.getInstance().getConfig().getString("block.SymbolCommands.message"));
                        return;
                    }
                }
            }
        }
    }
}