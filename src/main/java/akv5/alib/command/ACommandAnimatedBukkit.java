package akv5.alib.command;

import akv5.acore.libs.Scheduler;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ACommandAnimatedBukkit extends ACommandBukkit {
    private final Map<Player, Integer> animatedPlayers = new ConcurrentHashMap<>();
    private final Set<Player> remotePlayers = ConcurrentHashMap.newKeySet();
    private static final Set<ACommandAnimatedBukkit> commands = ConcurrentHashMap.newKeySet();

    public ACommandAnimatedBukkit(Object plugin, String name, List<String> aliases, int cooldown, int limit) {
        super(plugin, name, aliases, cooldown, limit);
        commands.add(this);
    }

    public Map<Player, Integer> getAnimatedPlayers() {
        return animatedPlayers;
    }

    public Set<Player> getRemotePlayers() {
        return remotePlayers;
    }

    public static Set<ACommandAnimatedBukkit> getCommands() {
        return commands;
    }

    public void startAnimation(Player player) {}

    public void startAnimation(Player player, int remove) {
        startAnimation(player);
        if (remove == 0) return;
        getRemotePlayers().add(player);
        Scheduler.doAsyncLater(() -> getRemotePlayers().remove(player), 20L * remove);
    }

    public boolean inAnimation(Player player) {
        return getAnimatedPlayers().containsKey(player);
    }

    public void stopAnimation(Player player) {
        Scheduler.stop(getAnimatedPlayers().get(player));
        getAnimatedPlayers().remove(player);
    }

    public static boolean inAnyAnimation(Player player) {
        return commands.stream().anyMatch(command -> command.inAnimation(player));
    }

    public static void stopAllAnimation(Player player) {
        if (!inAnyAnimation(player)) return;
        commands.forEach(command -> command.stopAnimation(player));
    }
}