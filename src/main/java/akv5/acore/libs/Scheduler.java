package akv5.acore.libs;

import akv5.acore.ACore;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class Scheduler {

    private static ACore plugin;

    public static void init(ACore init) {
        plugin = init;
    }

    public static BukkitTask doSync(Runnable task) {
        checkPlugin();
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    public static BukkitTask doAsync(Runnable task) {
        checkPlugin();
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static BukkitTask doSyncLater(Runnable task, long delayTicks) {
        checkPlugin();
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public static BukkitTask doAsyncLater(Runnable task, long delayTicks) {
        checkPlugin();
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    public static BukkitTask doSyncTimer(Runnable task, long delayTicks, long periodTicks) {
        checkPlugin();
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    public static BukkitTask doAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        checkPlugin();
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    public static BukkitTask doAsyncRepeat(Runnable task, long delayTicks, long periodTicks) {
        return doAsyncTimer(task, delayTicks, periodTicks);
    }

    public static void stop(int taskId) {
        if (taskId <= 0) return;
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public static void stop(BukkitTask task) {
        if (task == null) return;
        task.cancel();
    }

    public static void stopAll() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private static void checkPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("Scheduler not initialized!");
        }
    }
}
