package akv5.acore.libs.hooks;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import com.google.gson.JsonElement;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class EmoteCraftHook {
    private static final Map<String, KeyframeAnimation> CACHE = new ConcurrentHashMap<>();
    private static final Plugin plugin = ACore.getInstance();

    public static void playEmote(Player player, Emotes emote) {
        playEmote(player, getEmote(emote.name()));
    }

    public static void playEmote(Player player, Emotes emote, boolean force) {
        playEmote(player, getEmote(emote.name()), force);
    }

    public static void playEmote(Player player, KeyframeAnimation emote) {
        playEmote(player, emote, true);
    }

    public static void playEmote(Player player, KeyframeAnimation emote, boolean force) {
        UUID uuid = getUUIDFromPlayer(player);
        if (uuid != null) {
            ServerEmoteAPI.playEmote(uuid, emote, force);
        }
    }

    public static boolean isEmotePlayed(Player player) {
        UUID uuid = getUUIDFromPlayer(player);
        if (uuid == null) {
            return false;
        }
        return ServerEmoteAPI.getPlayedEmote(uuid) != null;
    }

    public static void stopEmote(Player player) {
        UUID uuid = getUUIDFromPlayer(player);
        if (uuid != null) {
            ServerEmoteAPI.playEmote(uuid, null, false);
        }
    }

    public static void reload() {
        reload(ACore.getInstance());
    }

    public static void reload(Plugin plugin) {
        File base = plugin.getDataFolder().getParentFile().getParentFile();
        File emotes = new File(base, "emotes");
        if (!emotes.exists() && emotes.mkdir()) {
            Informer.sendInfo("The emotes folder has been successfully created.");
        }

        List.of(Emotes.values()).forEach((emote) -> {
            File file = Paths.get(emotes.getAbsolutePath() + File.separator + emote.name() + ".json").toFile();
            if (file.exists() && !file.delete()) {
                Informer.sendWarn("An error occurred when deleting the previous copy of the emote: " + emote.name());
            } else {
                try {
                    InputStream inputStream = plugin.getResource("emotes/" + emote.name() + ".json");
                    if (inputStream == null) {
                        Informer.sendWarn("Resource not found: emotes/" + emote.name() + ".json");
                        return;
                    }

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                            stringBuilder.append(System.lineSeparator());
                        }
                        Files.writeString(file.toPath(), stringBuilder.toString());
                        Informer.sendInfo("Emote " + emote.name() + " was successfully loaded!");
                    } catch (Exception e) {
                        Informer.sendWarn("An error occurred while creating the file: " + file.getAbsolutePath());
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    Informer.sendWarn("An error occurred while trying to access the file: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        });
        UniversalEmoteSerializer.loadEmotes();
    }

    public static boolean isEmotecraftPlayer(Player player) {
        return player != null && player.isOnline() && player.getListeningPluginChannels().contains("emotecraft:emote");
    }

    public static KeyframeAnimation getEmote(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        if (CACHE.containsKey(id)) {
            return CACHE.get(id);
        }

        KeyframeAnimation found = ServerEmoteAPI.getLoadedEmotes().values().stream()
                .filter(emote -> emote.extraData.containsKey("name"))
                .filter(emote -> textToString(emote.extraData.get("name")).equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);

        if (found != null) {
            CACHE.put(id, found);
        }
        return found;
    }

    private static String fromJson(String str) {
        BaseComponent[] components = ComponentSerializer.parse(str);
        return Stream.of(components)
                .map(component -> component.toPlainText())
                .collect(Collectors.joining("-"));
    }

    private static String textToString(Object text) {
        if (text == null) {
            return "";
        }
        if (text instanceof JsonElement json) {
            return fromJson(json.toString());
        }
        if (text instanceof String) {
            try {
                return fromJson((String) text);
            } catch (Exception e) {
                return (String) text;
            }
        }
        return "";
    }

    private static UUID getUUIDFromPlayer(Player player) {
        if (player != null && player.isOnline()) {
            return player.getUniqueId();
        }
        return null;
    }

    public enum Emotes {
        MAGIC
    }
}