package akv5.alib;

import akv5.acore.libs.Colors;
import akv5.acore.libs.Plugins;
import com.google.common.collect.Lists;
import akv5.alib.data.Table;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Variables {
    private static Boolean enabled = false;
    private static final Map<String, Map<String, String>> data = new ConcurrentHashMap<>();
    private static final Set<String> fields = ConcurrentHashMap.newKeySet();

    public static Boolean getEnabled() {
        return enabled;
    }

    public static void setEnabled(Boolean enabled) {
        Variables.enabled = enabled;
    }

    public static Map<String, Map<String, String>> getData() {
        return data;
    }

    public static Set<String> getFields() {
        return fields;
    }

    public static void update() {
        if (Tools.getMySQL() == null) throw new RuntimeException("MySQL has not been initialized");
        if (!getEnabled()) {
            Tools.getMySQL().query(Table.VARIABLE)
                    .execute();
            setEnabled(true);
        }
        Tools.getMySQL().query("SHOW columns FROM acore_variable")
                .setResult(resultSet -> {
                    getFields().clear();
                    while (resultSet.next()) {
                        String field = resultSet.getString("Field");
                        if (field.equalsIgnoreCase("id")) continue;
                        getFields().add(field);
                    }
                })
                .execute();
        Tools.getMySQL().query("SELECT * FROM acore_variable")
                .setResult(resultSet -> {
                    while (resultSet.next()) {
                        String id = resultSet.getString("id");
                        Map<String, String> params = getData().containsKey(id) ? getData().get(id) : new ConcurrentHashMap<>();
                        getFields().forEach(field -> {
                            try {
                                String value = resultSet.getString(field);
                                if (value == null) value = "";
                                params.put(field, value);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        });
                        getData().put(id, params);
                    }
                })
                .execute();
    }

    public static String get(String id, String key) {
        if (!getEnabled()) return null;
        if (id == null || key == null) return null;
        if (!getData().containsKey(id)) return null;
        if (!getFields().contains(key)) return null;
        String info = Colors.set(getData().get(id).get(key));
        if (Plugins.PlaceholderAPI.isEnabled() && info.contains("%")) return PlaceholderAPI.setPlaceholders(null, info);
        return info;
    }

    public static String groupToName(String input) {
        if (!getEnabled() || input == null) return input;
        if (getData().containsKey(input) && getData().get(input).get("name") != null) return getData().get(input).get("name");
        return input;
    }

    public static String nameToGroup(String input) {
        if (!getEnabled() || input == null) return input;
        AtomicReference<String> result = new AtomicReference<>(input);
        getData().forEach((id, map) -> {
            if (map.containsKey("name") && map.get("name").equalsIgnoreCase(input)) result.set(id);
        });
        return result.get();
    }

    public static String replace(String input) {
        if (!getEnabled() || input == null) return input;
        String prefix = "{acore_variable_";
        if (!input.contains(prefix)) return input;
        AtomicReference<String> result = new AtomicReference<>(input);
        getData().forEach((id, map) -> {
            String prefixWithId = prefix + id + "_";
            if (!input.contains(prefixWithId)) return;
            map.forEach((key, value) -> result.set(result.get().replace(prefixWithId + key + "}", value)));
        });
        return Colors.set(result.get());
    }

    public static String replaceCommandLuckPerms(String input) {
        if (!Plugins.LuckPerms.isEnabled()) return input;
        if (!getEnabled() || input == null) return input;
        AtomicReference<String> result = new AtomicReference<>(input);
        List.of("set", "unset", "settemp", "unsettemp", "add", "remove")
                .forEach(type -> result.set(formatCommandToLuckPerms(result.get(), " group " + type + " ")));
        return result.get();
    }

    private static String formatCommandToLuckPerms(String input, String param) {
        if (!input.contains(param)) return input;
        String[] parts = input.split(param);
        if (parts.length != 2) return input;
        String[] subparts = parts[1].split(" ");
        String group = nameToGroup(subparts[0]);
        if (group.equalsIgnoreCase(subparts[0])) return input;
        List<String> list = Lists.newArrayList(List.of(subparts));
        list.remove(0);
        list.add(0, group);
        return parts[0] + param + Tools.join(" ", list);
    }
}