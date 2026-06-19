package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import akv5.acore.libs.configs.GrantConfig;
import akv5.acore.libs.managers.GrantManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Grant implements CommandExecutor, TabCompleter {

    private final ACore plugin;
    private final GrantManager grantManager;
    private final GrantConfig grantConfig;
    private final LuckPerms luckPerms;

    public Grant(ACore plugin, GrantManager grantManager, GrantConfig grantConfig) {
        this.plugin = plugin;
        this.grantManager = grantManager;
        this.grantConfig = grantConfig;

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        assert provider != null;
        this.luckPerms = provider.getProvider();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!Methods.hasPermission(sender, "acore.command.grant")) {
            Informer.send(player, plugin.getConfig().getString("messages.noPermissions"));
            return true;
        }

        if (args.length != 2) {
            Informer.send(player, grantConfig.getMessage("usage"));
            return true;
        }

        String targetName = args[0];
        String donationType = args[1].toLowerCase();

        String senderGroup = getPlayerPrimaryGroup(player);
        if (senderGroup == null) {
            Informer.send(player, grantConfig.getMessage("error"));
            return true;
        }

        if (donationType.equals("default")) {
            if (!grantManager.isAdmin(senderGroup)) {
                Informer.send(player, plugin.getConfig().getString("messages.noPermissions"));
                return true;
            }
        } else {
            if (!grantConfig.getAllAvailableDonations().contains(donationType)) {
                Map<String, String> placeholders = new HashMap<>();
                if (grantManager.isAdmin(senderGroup)) {
                    placeholders.put("available", String.join(", ", grantConfig.getAllAvailableDonations()));
                } else {
                    placeholders.put("available", grantManager.getAvailableDonationsForGroup(senderGroup));
                }
                Informer.send(player, grantConfig.getMessage("invalid-donation", placeholders));
                return true;
            }

            if (!grantManager.canGroupGrantDonationType(senderGroup, donationType)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("available", grantManager.getAvailableDonationsForGroup(senderGroup));
                Informer.send(player, grantConfig.getMessage("invalid-donation", placeholders));
                return true;
            }
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", targetName);
            Informer.send(player, plugin.getConfig().getString("messages.playerNotFound"));
            return true;
        }

        if (!donationType.equals("default") && !grantManager.isAdmin(senderGroup)) {
            if (!grantManager.canGrantDonation(player.getUniqueId(), senderGroup, donationType)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("donate", donationType);
                Informer.send(player, grantConfig.getMessage("limit-exceeded", placeholders));
                grantManager.showRemainingLimits(player, senderGroup);
                return true;
            }
        }

        if (grantDonationToPlayer(target, donationType)) {
            if (!donationType.equals("default") && !grantManager.isAdmin(senderGroup)) {
                grantManager.recordGrant(player.getUniqueId(), donationType);
            }

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("donate", donationType);
            placeholders.put("target", targetName);
            Informer.send(player, grantConfig.getMessage("grant-success", placeholders));

            Map<String, String> receivePlaceholders = new HashMap<>();
            receivePlaceholders.put("player", player.getName());
            receivePlaceholders.put("donate", donationType);

            if (donationType.equals("default")) {
                Informer.send(target, "&7\n{prefix}&cВаша привилегия была сброшена администратором&c.\n&7");
            } else {
                Informer.send(target, grantConfig.getMessage("grant-receive", receivePlaceholders));
            }
        } else {
            Informer.send(player, grantConfig.getMessage("error"));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player player)) {
            return completions;
        }

        if (!Methods.hasPermission(sender, "acore.command.grant")) {
            return completions;
        }

        String senderGroup = getPlayerPrimaryGroup(player);
        if (senderGroup == null) {
            return completions;
        }

        List<String> availableDonations = grantManager.getAvailableDonationsListForGroup(senderGroup);

        if (grantManager.isAdmin(senderGroup) && !availableDonations.contains("default")) {
            availableDonations.add("default");
            availableDonations.sort(String::compareToIgnoreCase);
        }

        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            List<String> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());

            completions.addAll(onlinePlayers.stream()
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList()));
        }
        else if (args.length == 2) {
            String partialDonation = args[1].toLowerCase();

            completions.addAll(availableDonations.stream()
                    .filter(donation -> donation.toLowerCase().startsWith(partialDonation))
                    .collect(Collectors.toList()));

            if (completions.isEmpty() && !partialDonation.isEmpty()) {
                completions.addAll(availableDonations.stream()
                        .filter(donation -> donation.toLowerCase().contains(partialDonation))
                        .collect(Collectors.toList()));
            }
        }

        return completions;
    }

    private String getPlayerPrimaryGroup(Player player) {
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return null;
            return user.getPrimaryGroup();
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при получении группы игрока: " + e.getMessage());
            return null;
        }
    }

    private boolean grantDonationToPlayer(Player player, String donationType) {
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return false;

            if (donationType.equals("default")) {
                return setDefaultGroup(player, user);
            } else {
                String permission = "group." + donationType;

                if (!isGroupExists(donationType)) {
                    plugin.getLogger().warning("Попытка выдать несуществующую группу: " + donationType);
                    return false;
                }

                Node node = Node.builder(permission).value(true).build();
                user.data().add(node);
                luckPerms.getUserManager().saveUser(user);
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при выдаче доната: " + e.getMessage());
            return false;
        }
    }

    private boolean setDefaultGroup(Player player, User user) {
        try {
            List<String> currentGroups = new ArrayList<>();

            for (Node node : user.data().toCollection()) {
                if (node.getKey().startsWith("group.")) {
                    String groupName = node.getKey().substring(6);
                    currentGroups.add(groupName);
                }
            }

            for (String group : currentGroups) {
                if (!group.equals("default")) {
                    String permission = "group." + group;
                    for (Node node : user.data().toCollection()) {
                        if (node.getKey().equals(permission) && node.getValue()) {
                            user.data().remove(node);
                            break;
                        }
                    }
                }
            }

            Node defaultNode = Node.builder("group.default").value(true).build();
            user.data().add(defaultNode);

            luckPerms.getUserManager().saveUser(user);

            plugin.getLogger().info("Администратор сбросил привилегии игрока " + player.getName() + " до default");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при установке группы default: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isGroupExists(String groupName) {
        try {
            return luckPerms.getGroupManager().getGroup(groupName) != null;
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при проверке существования группы: " + e.getMessage());
            return false;
        }
    }
}