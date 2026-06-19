package akv5.alib.platform;

import com.velocitypowered.api.proxy.ProxyServer;

public class ProxyManager {
    private static ProxyServer proxy;

    public static void setProxy(ProxyServer proxy) {
        ProxyManager.proxy = proxy;
    }

    public static ProxyServer getProxy() {
        return proxy;
    }
}