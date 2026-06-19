package akv5.acore.placeholders;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

public class Placeholders extends PlaceholderExpansion {
    private final ACore plugin;

    public Placeholders(ACore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "acore";
    }

    @Override
    public @NotNull String getAuthor() {
        return "akv5";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Nullable
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        FileConfiguration placeholdersConfig = plugin.getPlaceholdersConfig();

        String customPlaceholder = placeholdersConfig.getString("placeholders." + identifier);
        if (customPlaceholder != null) {
            return Colors.set(customPlaceholder);
        }

        if (player == null) {
            return "";
        }

        String playerId = player.getUniqueId().toString();
        FileConfiguration playersConfig = plugin.getPlayersConfig();

        if ("chatcolor".equals(identifier)) {
            return Colors.set(playersConfig.getString(playerId + ".chatcolor",
                    plugin.getPlaceholdersConfig().getString("default_chatcolor", "&f")));
        }

        if ("first_join".equals(identifier)) {
            long firstJoin = playersConfig.getLong(playerId + ".firstJoin", 0L);
            if (firstJoin != 0L) {
                Date date = new Date(firstJoin);
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                return format.format(date);
            } else {
                return "Дата первого входа неизвестна";
            }
        }

        if ("last_join".equals(identifier)) {
            long lastJoin = playersConfig.getLong(playerId + ".lastJoin", 0L);
            if (lastJoin != 0L) {
                Date date = new Date(lastJoin);
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                return format.format(date);
            } else {
                return "Дата последнего входа неизвестна";
            }
        }

        switch (identifier) {
            case "health" -> {
                return String.format("%d", (int) player.getHealth());
            }

            case "group_name" -> {
                return getGroupPrefix(player);
            }

            case "luckperms_prefix" -> {
                return getGroupPrefixTab(player);
            }

            case "luckperms_suffix" -> {
                String suffix = PlaceholderAPI.setPlaceholders(player, "%luckperms_suffix%");
                if (suffix.trim().isEmpty()) {
                    return "";
                } else {
                    return " " + suffix.trim();
                }
            }

            case "luckperms_suffix_scoreboard" -> {
                String suffix = PlaceholderAPI.setPlaceholders(player, "%luckperms_suffix%");
                if (suffix.trim().isEmpty()) {
                    return Colors.set(plugin.getInfoConfig().getString("parameters.suffix"));
                } else {
                    return " " + suffix.trim();
                }
            }

            case "prefix" -> {
                return plugin.getInfoConfig().getString("prefix");
            }
        }

        return null;
    }

    public String getGroupPrefix(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();

        User user = luckPerms.getUserManager().getUser (player.getUniqueId());
        if (user == null) {
            return Colors.set(plugin.getInfoConfig().getString("parameters.group"));
        }

        String primaryGroupName = user.getPrimaryGroup();
        if (primaryGroupName.isEmpty()) {
            return Colors.set(plugin.getInfoConfig().getString("parameters.group"));
        }

        switch (primaryGroupName.toLowerCase()) {
            case "admin", "op", "gmod", "mod", "build", "default" -> {
                if (player.hasPermission("prefix.1000." + primaryGroupName)) {
                    return Colors.set("&f[{#42ffa1}Игрок&f] &f");
                } else {
                    return Colors.set(plugin.getInfoConfig().getString("parameters.group"));
                }
            }
            default -> {
                Group group = luckPerms.getGroupManager().getGroup(primaryGroupName);
                if (group == null) {
                    return Colors.set(plugin.getInfoConfig().getString("parameters.group"));
                }

                String prefix = getHighestPriorityPrefix(group);
                if (prefix == null || prefix.isEmpty()) {
                    return Colors.set(plugin.getInfoConfig().getString("parameters.group"));
                }
                return Colors.set(prefix);
            }
        }
    }

    public String getGroupPrefixTab(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();

        User user = luckPerms.getUserManager().getUser (player.getUniqueId());
        if (user == null) {
            return Colors.set(plugin.getInfoConfig().getString("parameters.group"));
        }

        String primaryGroupName = user.getPrimaryGroup();

        switch (primaryGroupName.toLowerCase()) {
            case "admin", "op", "build", "default" -> {
                return Colors.set("{#42ffa1}");
            }
        }
        return "%luckperms_prefix%";
    }

    private String getHighestPriorityPrefix(Group group) {
        return group.getNodes().stream()
                .filter(node -> node instanceof PrefixNode)
                .map(node -> (PrefixNode) node)
                .max((p1, p2) -> Integer.compare(p1.getPriority(), p2.getPriority()))
                .map(PrefixNode::getMetaValue)
                .orElse(null);
    }
}

