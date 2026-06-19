package akv5.acore.utils.versions;

import java.util.HashMap;
import java.util.Map;

public class VersionMapper {

    private static final Map<Integer, String> VERSION_MAP = new HashMap<>();

    static {
        VERSION_MAP.put(767, "1.21.1");
    }

    public static String getVersionFromProtocol(int protocolVersion) {
        return VERSION_MAP.getOrDefault(protocolVersion, "N/A");
    }
}