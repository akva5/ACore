package akv5.acore.libs;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Cooldowner {
    private static final HashMap<String, Long> cooldowns = new HashMap<>();

    public static void start(String name, Type type, long time) {
        Cooldowner.start(name, type, time, TimeUnit.SECONDS);
    }

    public static void start(String name, Type type, long time, TimeUnit timeUnit) {
        long endTime = System.currentTimeMillis() + timeUnit.toMillis(time);
        cooldowns.put(Cooldowner.key(name, type), endTime);
    }

    public static void remove(String name, Type type) {
        if (name == null || type == null) {
            return;
        }
        cooldowns.remove(Cooldowner.key(name, type));
    }

    public static boolean isInCooldown(String name, Type type) {
        return Cooldowner.getTimeLeft(name, type, TimeUnit.MILLISECONDS) > 0L;
    }

    public static boolean inCooldown(String name, Type type) {
        return isInCooldown(name, type);
    }

    public static long getTimeLeft(String name, Type type) {
        return Cooldowner.getTimeLeft(name, type, TimeUnit.SECONDS);
    }

    public static long getTimeLeft(String name, Type type, TimeUnit timeUnit) {
        Long endTime = cooldowns.get(Cooldowner.key(name, type));
        if (endTime == null) {
            return 0L;
        }
        long now = System.currentTimeMillis();
        long timeLeft = endTime - now;
        if (timeLeft > 0L) {
            return timeUnit.convert(timeLeft, TimeUnit.MILLISECONDS);
        }
        Cooldowner.remove(name, type);
        return 0L;
    }

    public static String key(String name, Type type) {
        return Cooldowner.key(name, type.name());
    }

    public static String key(String name, String type) {
        return Cooldowner.class.getName() + ":" + name + ":" + type;
    }

    public static enum Type {
        MESSAGE,
        COMMAND,
        RAPE_EFFECTS;
    }
}
