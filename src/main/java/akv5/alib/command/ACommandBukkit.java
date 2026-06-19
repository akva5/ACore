package akv5.alib.command;

import akv5.acore.libs.Cooldowner;
import akv5.acore.libs.Informer;
import akv5.alib.*;
import akv5.alib.config.Info;
import akv5.alib.controller.StorageController;
import akv5.alib.data.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class ACommandBukkit extends BukkitCommand {
    private String commandName;
    private String permission;
    private int cooldown;
    private int limit;
    private final Map<String, Integer> count = new ConcurrentHashMap<>();
    private static Set<ACommandBukkit> registeredCommands = ConcurrentHashMap.newKeySet();

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Map<String, Integer> getCount() {
        return count;
    }

    public static Set<ACommandBukkit> getRegisteredCommands() {
        return registeredCommands;
    }

    public static void setRegisteredCommands(Set<ACommandBukkit> registeredCommands) {
        ACommandBukkit.registeredCommands = registeredCommands;
    }

    protected ACommandBukkit(Object plugin, String name, List<String> aliases, int cooldown, int limit) {
        super(name);
        setCommandName(name);
        setPermission(Tools.join(".command.", plugin.getClass().getSimpleName().toLowerCase().replace("loader", ""), getCommandName().toLowerCase()));

        Set<String> rus = ConcurrentHashMap.newKeySet();
        if (!aliases.isEmpty()) rus = aliases.stream()
                .filter(alias -> !Tools.isCyrillic(alias))
                .map(Translator::toRussianKeymap)
                .collect(Collectors.toSet());
        rus.add(Translator.toRussianKeymap(name));

        aliases.addAll(rus);
        setAliases(aliases
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList()));

        setCooldown(cooldown);
        setLimit(limit);

        registeredCommands.add(this);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public final boolean execute(@NonNull CommandSender sender, @NonNull String alias, String[] arguments) {
        String key = getSenderName(sender);
        getCount().putIfAbsent(key, 0);
        int count = getCount().get(key);

        if (!Tools.isPerm(sender, Permission.getEnum(getPermission()))) {
            Informer.send(sender, StorageController.getNoPermMessage());
            return false;
        }

        if (isPlayer(sender)) {
            if (Cooldowner.inCooldown(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND) && !Tools.isPerm(sender, Permission.ACORE_BYPASS_COOLDOWN_COMMAND)) {
                long timeLeft = Cooldowner.getTimeLeft(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND);
                String timeString = formatTime(timeLeft);
                Informer.send(sender, StorageController.getCommandCooldownMessage()
                        .replace("{time}", timeString));
                return false;
            }

            if (getLimit() > 0 && count >= getLimit() && !Tools.isPerm(sender, Permission.ACORE_BYPASS_LIMIT_COMMAND)) {
                Informer.send(sender, StorageController.getCommandLimitMessage());
                return false;
            }
        }

        if (run(sender, alias, arguments)) {
            if (getLimit() > 0) getCount().merge(key, 1, Integer::sum);
            if (getCooldown() > 0 && !Tools.isPerm(sender, Permission.ACORE_BYPASS_COOLDOWN_COMMAND)) {
                Cooldowner.start(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND, getCooldown());
            }
        }

        return true;
    }

    // Простой метод форматирования времени
    private String formatTime(long seconds) {
        if (seconds <= 0) return "0s";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append("d ");
        if (hours > 0) result.append(hours).append("h ");
        if (minutes > 0) result.append(minutes).append("m ");
        if (secs > 0 || result.length() == 0) result.append(secs).append("s");

        return result.toString().trim();
    }

    protected abstract boolean run(CommandSender sender, String alias, String[] arguments);
    protected abstract List<String> tab(CommandSender sender, String alias, String[] arguments);

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandSender sender, @NonNull String alias, String[] arguments) throws IllegalArgumentException {
        return tab(sender, alias, arguments);
    }

    protected boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    protected String getSenderName(CommandSender sender) {
        return sender instanceof Player ? sender.getName() : Info.consoleName;
    }
}