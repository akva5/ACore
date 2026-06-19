package akv5.acore.commands;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public abstract class Animated implements CommandExecutor, TabCompleter {
    private final String name;
    private final List<String> aliases;
    private final int cooldown;
    private final int limit;
    private final JavaPlugin plugin;

    private final ConcurrentHashMap<Player, Integer> animatedPlayers;
    private final HashSet<Player> remotePlayers;
    private static final HashSet<Animated> commands = new HashSet<>();

    private CommandSender currentSender;
    private String[] currentArgs;

    public Animated(String name, List<String> aliases, int cooldown, int limit, JavaPlugin plugin) {
        this.name = name;
        this.aliases = aliases;
        this.cooldown = cooldown;
        this.limit = limit;
        this.plugin = plugin;
        this.animatedPlayers = new ConcurrentHashMap<>();
        this.remotePlayers = new HashSet<>();
        commands.add(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        this.currentSender = sender;
        this.currentArgs = args;
        return this.run();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        this.currentSender = sender;
        this.currentArgs = args;
        this.processTabComplete(sender, alias, args);
        return this.tabComplete(sender, alias, args);
    }

    public abstract boolean run();

    public abstract List<String> tabComplete(CommandSender sender, String alias, String[] args);

    public void processTabComplete(CommandSender sender, String alias, String[] args) {
    }

    public boolean isPlayer() {
        return this.currentSender instanceof Player;
    }

    public CommandSender getCommandSender() {
        return this.currentSender;
    }

    public String getCommandName() {
        return this.name;
    }

    public String[] getArgs() {
        return this.currentArgs != null ? this.currentArgs : new String[0];
    }

    public void startAnimation(Player player) {
    }

    public void startAnimation(Player player, int remove) {
        this.startAnimation(player);
        if (remove != 0) {
            this.remotePlayers.add(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    remotePlayers.remove(player);
                }
            }.runTaskLaterAsynchronously(plugin, 20L * (long) remove);
        }
    }

    public boolean inAnimation(Player player) {
        return this.animatedPlayers.containsKey(player);
    }

    public void stopAnimation(Player player) {
        Integer taskId = this.animatedPlayers.get(player);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
        this.animatedPlayers.remove(player);
    }

    public ConcurrentHashMap<Player, Integer> getAnimatedPlayers() {
        return this.animatedPlayers;
    }

    public HashSet<Player> getRemotePlayers() {
        return this.remotePlayers;
    }

    public static boolean inAnyAnimation(Player player) {
        return commands.stream().anyMatch(command -> command.inAnimation(player));
    }

    public static void stopAllAnimation(Player player) {
        commands.forEach(command -> command.stopAnimation(player));
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public int getLimit() {
        return this.limit;
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }
}
