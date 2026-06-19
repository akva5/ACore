package akv5.acore.utils.textures;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TexturePack implements CommandExecutor, Listener {
    private String texturePackVersion = "";

    private final String texturePackURL = ACore.getInstance().getConfig().getString("textures.url");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (ACore.getInstance().getConfig().getBoolean("textures.enabled")) {
            Player player = event.getPlayer();
            String urlWithVersion = texturePackURL + "?v=" + texturePackVersion;
            player.setResourcePack(urlWithVersion);
        }
    }

    public void updateTexturePackVersion() {
        try {
            if (texturePackURL != null) {
                URL url = new URL(texturePackURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.connect();

                String lastModified = connection.getHeaderField("Last-Modified");
                if (lastModified != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                    Date date = sdf.parse(lastModified);
                    long timestamp = date.getTime();
                    texturePackVersion = String.valueOf(timestamp);
                } else {
                    String eTag = connection.getHeaderField("ETag");
                    if (eTag != null) {
                        texturePackVersion = eTag.replace("\"", "");
                    } else {
                        texturePackVersion = String.valueOf(System.currentTimeMillis());
                    }
                }

                connection.disconnect();
            }
        } catch(Exception e) {
            e.printStackTrace();
            texturePackVersion = String.valueOf(System.currentTimeMillis());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (ACore.getInstance().getConfig().getBoolean("textures.enabled")) {
            if (command.getName().equalsIgnoreCase("rp")) {
                updateTexturePackVersion();
                Informer.send(sender, ACore.getInstance().getConfig().getString("textures.message"));
                return true;
            }
        }
        return false;
    }
}
