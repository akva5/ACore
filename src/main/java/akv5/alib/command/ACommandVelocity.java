package akv5.alib.command;

import akv5.acore.libs.Cooldowner;
import akv5.acore.libs.Informer;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import akv5.alib.Tools;
import akv5.alib.config.Info;
import akv5.alib.controller.StorageController;
import akv5.alib.data.Permission;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class ACommandVelocity implements SimpleCommand {
    private String commandName;
    private String permission;
    private int cooldown;
    private int limit;
    private final Map<String, Integer> count = new ConcurrentHashMap<>();
    private final List<String> aliases;
    private static Set<ACommandVelocity> registeredCommands = ConcurrentHashMap.newKeySet();

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

    public List<String> getAliases() {
        return aliases;
    }

    public static Set<ACommandVelocity> getRegisteredCommands() {
        return registeredCommands;
    }

    public static void setRegisteredCommands(Set<ACommandVelocity> registeredCommands) {
        ACommandVelocity.registeredCommands = registeredCommands;
    }

    protected ACommandVelocity(Object plugin, String name, List<String> aliases, int cooldown, int limit) {
        this.aliases = aliases
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        registeredCommands.add(this);

        setCommandName(name);
        setPermission(Tools.join(".command.", plugin.getClass().getSimpleName().toLowerCase().replace("loader", ""), getCommandName().toLowerCase()));
        setCooldown(cooldown);
        setLimit(limit);
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

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String alias = invocation.alias();
        String[] arguments = invocation.arguments();

        String key = getSourceName(source);
        getCount().putIfAbsent(key, 0);
        int count = getCount().get(key);

        if (!Tools.isPerm(source, Permission.getEnum(getPermission()))) {
            Informer.send(source, StorageController.getNoPermMessage());
            return;
        }

        if (isPlayer(source)) {
            if (Cooldowner.inCooldown(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND) && !Tools.isPerm(source, Permission.ACORE_BYPASS_COOLDOWN_COMMAND)) {
                long timeLeft = Cooldowner.getTimeLeft(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND);
                String timeString = formatTime(timeLeft);
                Informer.send(source, StorageController.getCommandCooldownMessage()
                        .replace("{time}", timeString));
                return;
            }

            if (getLimit() > 0 && count >= getLimit() && !Tools.isPerm(source, Permission.ACORE_BYPASS_LIMIT_COMMAND)) {
                Informer.send(source, StorageController.getCommandLimitMessage());
                return;
            }
        }

        if (run(source, alias, arguments)) {
            if (getLimit() > 0) getCount().merge(key, 1, Integer::sum);
            if (getCooldown() > 0 && !Tools.isPerm(invocation.source(), Permission.ACORE_BYPASS_COOLDOWN_COMMAND)) {
                Cooldowner.start(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND, getCooldown());
            }
        }
    }

    protected abstract boolean run(CommandSource source, String alias, String[] arguments);
    protected abstract List<String> tab(CommandSource source, String alias, String[] arguments);

    @Override
    public List<String> suggest(Invocation invocation) {
        return tab(invocation.source(), invocation.alias(), invocation.arguments());
    }

    protected boolean isPlayer(CommandSource source) {
        return source instanceof Player;
    }

    protected String getSourceName(CommandSource source) {
        return source instanceof Player player ? player.getUsername() : Info.consoleName;
    }
}