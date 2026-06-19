package akv5.acore.commands;

import java.util.Iterator;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class Vanish implements CommandExecutor, Listener {
    private final ACore plugin;
    private BukkitTask task;

    public Vanish(ACore plugin) {
        this.plugin = plugin;
    }

    private void applyEffects(Player player) {
        PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1, false, false);
        player.addPotionEffect(nightVision);
    }

    private void removeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (Methods.hasPermission(sender, "acore.command.avanish")) {
                if (args.length == 0) {
                    this.toggleVanish(player, player);
                } else if (args.length == 1) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        this.toggleVanish(player, target);
                    } else {
                        Informer.send(sender, ACore.getInstance().getConfig().getString("messages.playerNotFound")
                                .replace("{player}", args[0]));
                    }
                } else {
                    Informer.send(sender, ACore.getInstance().getConfig().getString("commands.avanish.usage"));
                }
            } else {
                Informer.send(sender, ACore.getInstance().getConfig().getString("messages.noPermissions"));
            }
        } else {
            Informer.send(sender, ACore.getInstance().getConfig().getString("messages.playerNotFound")
                    .replace("{player}", args[0]));
        }
        return true;
    }

    private void toggleVanish(Player sender, final Player target) {
        Iterator var3;
        Player online;

        if (target.hasMetadata("vanish")) {
            target.removeMetadata("vanish", this.plugin);
            var3 = Bukkit.getOnlinePlayers().iterator();

            while (var3.hasNext()) {
                online = (Player) var3.next();
                online.showPlayer(this.plugin, target);
            }

            this.removeEffects(target);
            if (ACore.getInstance().getConfig().getBoolean("allow-flight", true) && target.getGameMode() != GameMode.CREATIVE && target.getGameMode() != GameMode.SPECTATOR) {
                target.setAllowFlight(false);
            }

            if (ACore.getInstance().getConfig().getBoolean("invulnerable", true)) {
                target.setInvulnerable(false);
            }

            Informer.send(target, ACore.getInstance().getConfig().getString("commands.avanish.off-self"));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + sender.getName() + " permission unset ajleaderboards.dontupdate.*");
            if (!sender.equals(target)) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + target.getName() + " permission unset ajleaderboards.dontupdate.*");
                Informer.send(sender, ACore.getInstance().getConfig().getString("commands.avanish.off-other")
                        .replace("{target}", target.getName()));
            }

            if (this.task != null) {
                this.task.cancel();
                this.task = null;
            }
        } else {
            target.setMetadata("vanish", new FixedMetadataValue(this.plugin, true));
            var3 = Bukkit.getOnlinePlayers().iterator();

            while (var3.hasNext()) {
                online = (Player) var3.next();
                if (!Methods.hasPermission(online, "acore.command.avanish.see")) {
                    online.hidePlayer(this.plugin, target);
                }
            }

            this.applyEffects(target);
            if (ACore.getInstance().getConfig().getBoolean("allow-flight", true)) {
                target.setAllowFlight(true);
            }

            if (ACore.getInstance().getConfig().getBoolean("invulnerable", true)) {
                target.setInvulnerable(true);
            }

            Informer.send(target, ACore.getInstance().getConfig().getString("commands.avanish.on-self"));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + sender.getName() + " permission set ajleaderboards.dontupdate.*");
            if (!sender.equals(target)) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + target.getName() + " permission set ajleaderboards.dontupdate.*");
                Informer.send(sender, ACore.getInstance().getConfig().getString("commands.avanish.on-other")
                        .replace("{target}", target.getName()));
            }
        }

        this.updateTabList();
    }

    private void updateTabList() {
        Player online;
        String name;
        for (Iterator var1 = Bukkit.getOnlinePlayers().iterator(); var1.hasNext(); online.setPlayerListName(name)) {
            online = (Player) var1.next();
            name = online.getName();
            if (online.hasMetadata("vanish")) {
                name = ChatColor.GRAY + "[V] " + name;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        Iterator var3 = Bukkit.getOnlinePlayers().iterator();

        while (var3.hasNext()) {
            Player online = (Player) var3.next();
            if (online.hasMetadata("vanish") && !Methods.hasPermission(joiner, "acore.command.avanish.see")) {
                joiner.hidePlayer(this.plugin, online);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quitter = event.getPlayer();
        if (quitter.hasMetadata("vanish")) {
            quitter.removeMetadata("vanish", this.plugin);
            this.removeEffects(quitter);
            if (ACore.getInstance().getConfig().getBoolean("allow-flight", true) && quitter.getGameMode() != GameMode.CREATIVE && quitter.getGameMode() != GameMode.SPECTATOR) {
                quitter.setAllowFlight(false);
            }

            if (ACore.getInstance().getConfig().getBoolean("invulnerable", true)) {
                quitter.setInvulnerable(false);
            }
        }
    }
}
