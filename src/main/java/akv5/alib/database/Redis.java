package akv5.alib.database;

import akv5.acore.libs.Informer;
import akv5.alib.Tools;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Redis {
    private boolean isEnabled = true;
    private final JedisPool poolDefault;
    private static final Set<JedisPubSub> pubSubs = ConcurrentHashMap.newKeySet();

    public Redis(String address, String password) {
        String ip = address.split(":")[0];
        int port  = address.contains(":") ? Tools.parseInt(address.split(":")[1]) : 6379;

        JedisPoolConfig config = new JedisPoolConfig();

        if (!password.isEmpty()) {
            poolDefault = new JedisPool(config, ip, port, 0, password);
        } else {
            poolDefault = new JedisPool(config, ip, port, 0);
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public JedisPool getPoolDefault() {
        return poolDefault;
    }

    public static Set<JedisPubSub> getPubSubs() {
        return pubSubs;
    }

    public JedisPool getPool() {
        try {
            return getPoolDefault();
        } catch (Throwable t) {
            shutdown();
            Informer.sendWarn("Failed to connect to the Redis! Check your configuration...");
            return null;
        }
    }

    public void publish(String channel, String message) {
        try (Jedis jedis = getPool().getResource()) {
            jedis.publish(channel, message);
        } catch (Throwable ignored) {
            shutdown();
        }
    }

    public void shutdown() {
        setEnabled(false);
        getPubSubs().stream()
                .filter(JedisPubSub::isSubscribed)
                .forEach(JedisPubSub::unsubscribe);
        getPoolDefault().close();
    }

    public static class PubSub extends JedisPubSub {
        public PubSub() {
            Redis.getPubSubs().add(this);
        }
    }
}