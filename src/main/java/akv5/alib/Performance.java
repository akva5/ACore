package akv5.alib;

import akv5.acore.libs.Cooldowner;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Plugins;
import akv5.acore.libs.Scheduler;
import com.google.common.collect.Lists;
import akv5.alib.data.BukkitVersion;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import redis.clients.jedis.Jedis;
import akv5.alib.data.Platform;
import akv5.alib.database.Redis;
import java.io.File;
import java.text.DecimalFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.format.DateTimeFormatter;

public class Performance {
    private static final Map<String, Integer> online = new ConcurrentHashMap<>();
    private static final Map<String, Integer> maxOnline = new ConcurrentHashMap<>();
    private static final Map<String, Integer> CPUProcess = new ConcurrentHashMap<>();
    private static final Map<String, Integer> CPUSystem = new ConcurrentHashMap<>();

    private static final Map<String, Double> RAMTotal = new ConcurrentHashMap<>();
    private static final Map<String, Double> RAMMax = new ConcurrentHashMap<>();
    private static final Map<String, Double> disk = new ConcurrentHashMap<>();
    private static final Map<String, Double> TPS = new ConcurrentHashMap<>();
    private static final Map<String, Double> MSPT = new ConcurrentHashMap<>();
    private static final Set<String> servers = ConcurrentHashMap.newKeySet();

    private static final Calendar CALENDAR = Calendar.getInstance();
    private static final ArrayList<Integer> AVERAGE_ONLINE = Lists.newArrayList();
    private static Double THIS_DISK = 0.0;
    private static Integer MAX_CURRENT_ONLINE = 0;

    public static Map<String, Integer> getOnline() {
        return online;
    }

    public static Map<String, Integer> getMaxOnline() {
        return maxOnline;
    }

    public static Map<String, Integer> getCPUProcess() {
        return CPUProcess;
    }

    public static Map<String, Integer> getCPUSystem() {
        return CPUSystem;
    }

    public static Map<String, Double> getRAMTotal() {
        return RAMTotal;
    }

    public static Map<String, Double> getRAMMax() {
        return RAMMax;
    }

    public static Map<String, Double> getDisk() {
        return disk;
    }

    public static Map<String, Double> getTPS() {
        return TPS;
    }

    public static Map<String, Double> getMSPT() {
        return MSPT;
    }

    public static Set<String> getServers() {
        return servers;
    }

    public static void init() {
        Scheduler.doAsync(() -> {
            if (Tools.getRedis() == null) return;
            try (Jedis jedis = Tools.getRedis().getPool().getResource()) {
                jedis.subscribe(new Redis.PubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        try {
                            String name = message.split(":")[0];
                            if (Tools.getServerID().equalsIgnoreCase(name)) return;
                            put(message);
                            servers.add(name);
                            Cooldowner.start(name, Cooldowner.Type.MESSAGE, 3);
                        } catch (Throwable t) {
                            Informer.sendWarn("An error occurred while processing Redis data! message: " + message);
                            t.printStackTrace();
                        }
                    }
                }, Performance.class.getSimpleName());
            } catch (Throwable t) {
                t.printStackTrace();
                Tools.shutdown();
            }
        });

        Scheduler.doAsyncRepeat(() -> {
            File file = new File("/");
            int i = (int) (file.getTotalSpace() / 1024L / 1024L / 1024L);
            int j = (int) (file.getFreeSpace()  / 1024L / 1024L / 1024L);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            THIS_DISK = Tools.parseDouble(decimalFormat.format((i - j) * 100.0D / i).replace(",", "."));
        }, 0L, 60 * 20L);
    }

    public static void update() {
        try {
            int online      = calcOnline();
            int maxOnline   = calcMaxOnline(online);
            int cpuProcess  = calcCPUProcess();
            int cpuSystem   = calcCPUSystem();

            double ramTotal = calcRamTotal();
            double ramMax   = calcRamMax();
            double disk     = calcDisk();
            double tps      = calcTPS();
            double mspt     = calcMSPT();

            put(Tools.join(":", Tools.getServerID(), online, maxOnline, cpuProcess, cpuSystem, ramTotal, ramMax, disk, tps, mspt));

            if (Tools.getRedis() == null) return;

            servers.removeIf(name -> {
                if (!Cooldowner.inCooldown(name, Cooldowner.Type.MESSAGE)) {
                    put(Tools.join(":", name, -1, -1, -1, -1, -1, -1, -1, -1, -1));
                    return true;
                }
                return false;
            });

            try (Jedis jedis = Tools.getRedis().getPool().getResource()) {
                jedis.setex(Tools.getServerID(), 5, Tools.join(":", online, maxOnline, cpuProcess, cpuSystem, ramTotal, ramMax, disk, tps, mspt));
                if (Platform.get().isProxy()) {
                    LocalDateTime now = LocalDateTime.now();
                    CALENDAR.setTime(Date.from(now.toInstant(ZoneOffset.from(ZoneId.systemDefault().getRules().getOffset(Instant.now())))));
                    int minutes = CALENDAR.get(Calendar.MINUTE);
                    int hours   = CALENDAR.get(Calendar.HOUR);
                    if (minutes == 0 && hours == 0) MAX_CURRENT_ONLINE = online;
                    if (minutes % 10 == 0) jedis.setex(Tools.join("-", "HStats", "current", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))), 60 * 60 * 24, String.valueOf(online));
                    AVERAGE_ONLINE.add(online);
                    jedis.set(Tools.join("-", "HStats", "daily", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))), Tools.join(":", String.valueOf(getAverageOnline()), calcMaxOnline(-1)));
                }
            }

            Tools.getRedis().publish(Performance.class.getSimpleName(), Tools.join(":", Tools.getServerID(), online, maxOnline, cpuProcess, cpuSystem, ramTotal, ramMax, disk, tps, mspt));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void put(String info) {
        String[] data = info.split(":");
        if (data.length < 1) return;
        String name = data[0];
        if (data.length > 1) getOnline()    .put(name, Integer.valueOf(data[1]));
        if (data.length > 2) getMaxOnline() .put(name, Integer.valueOf(data[2]));
        if (data.length > 3) getCPUProcess().put(name, Integer.valueOf(data[3]));
        if (data.length > 4) getCPUSystem() .put(name, Integer.valueOf(data[4]));
        if (data.length > 5) getRAMTotal()  .put(name, Double .valueOf(data[5]));
        if (data.length > 6) getRAMMax()    .put(name, Double .valueOf(data[6]));
        if (data.length > 7) getDisk()      .put(name, Double .valueOf(data[7]));
        if (data.length > 8) getTPS()       .put(name, Double .valueOf(data[8]));
        if (data.length > 9) getMSPT()      .put(name, Double .valueOf(data[9]));
    }

    public static void gc() {
        if (!Tools.requireBukkitVersion(BukkitVersion.V1_18)) System.gc();
    }

    private static int calcOnline() {
        return Tools.getOnline();
    }

    private static int calcMaxOnline(int online) {
        if (online > MAX_CURRENT_ONLINE) MAX_CURRENT_ONLINE = online;
        return MAX_CURRENT_ONLINE;
    }

    public static int getAverageOnline() {
        int sum = 0;
        for (Integer integer : AVERAGE_ONLINE) sum += integer;
        return Math.round((float) sum / AVERAGE_ONLINE.size());
    }

    private static int calcCPUProcess() {
        if (!Plugins.Spark.isEnabled()) return -1;
        try {
            Spark spark = SparkProvider.get();
            // CPU Usage для процесса
            DoubleStatistic<StatisticWindow.CpuUsage> cpuUsage = spark.cpuProcess();
            double usage = cpuUsage.poll(StatisticWindow.CpuUsage.SECONDS_10);
            return (int) usage;
        } catch (Throwable t) {
            return -1;
        }
    }

    private static int calcCPUSystem() {
        if (!Plugins.Spark.isEnabled()) return -1;
        try {
            Spark spark = SparkProvider.get();
            // CPU Usage для системы
            DoubleStatistic<StatisticWindow.CpuUsage> cpuUsage = spark.cpuSystem();
            double usage = cpuUsage.poll(StatisticWindow.CpuUsage.SECONDS_10);
            return (int) usage;
        } catch (Throwable t) {
            return -1;
        }
    }

    private static double calcTPS() {
        if (!Plugins.Spark.isEnabled()) return -1;
        try {
            Spark spark = SparkProvider.get();
            DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
            return tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10);
        } catch (Throwable t) {
            return -1;
        }
    }

    private static double calcMSPT() {
        if (!Plugins.Spark.isEnabled()) return -1;
        try {
            Spark spark = SparkProvider.get();
            GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> mspt = spark.mspt();
            DoubleAverageInfo msptInfo = mspt.poll(StatisticWindow.MillisPerTick.SECONDS_10);
            return msptInfo != null ? msptInfo.mean() : -1.0;
        } catch (Throwable t) {
            return -1;
        }
    }

    private static double calcDisk() {
        return THIS_DISK;
    }

    private static double calcRamTotal() {
        return Tools.round((double) Runtime.getRuntime().totalMemory() / 1024L / 1024L / 1024L, 1);
    }

    private static double calcRamMax() {
        return Tools.round((double) Runtime.getRuntime().maxMemory() / 1024L / 1024L / 1024L, 1);
    }
}