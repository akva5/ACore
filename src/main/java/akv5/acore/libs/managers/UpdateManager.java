package akv5.acore.libs.managers;

import akv5.acore.ACore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.io.InputStream;

public class UpdateManager {

    private final ACore plugin;
    private final String REPO = "https://api.github.com/repos/akva5/ACore/releases";
    private final String PLUGIN_NAME = "ACore.jar";
    private boolean updating = false;

    public UpdateManager(ACore plugin) {
        this.plugin = plugin;
    }

    private boolean isGattinoLandDev() {
        File configFile = new File(ACore.getInstance().getDataFolder(), "customs/placeholders.yml");
        if (!configFile.exists()) {
            return false;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String server = config.getString("placeholders.server");
        String serverName = config.getString("placeholders.server_name");

        if (server == null || !server.equalsIgnoreCase("GattinoLand")) {
            return false;
        }

        File infoFile = new File(ACore.getInstance().getDataFolder(), "info.yml");
        if (!infoFile.exists()) {
            return false;
        }

        YamlConfiguration infoConfig = YamlConfiguration.loadConfiguration(infoFile);
        String mode = infoConfig.getString("mode", "prod");

        return mode.equalsIgnoreCase("dev");
    }

    public void checkForUpdates() {
        if (updating) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                boolean isDev = isGattinoLandDev();

                // В dev режиме скачиваем черновики, в prod режиме - релизы
                String url = isDev ? REPO : REPO + "/latest";

                plugin.getLogger().info("Режим обновлений: " + (isDev ? "DEV (черновики)" : "PROD (релизы)"));

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .header("User-Agent", "Mozilla/5.0")
                        .header("Accept", "application/vnd.github.v3+json")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();

                String latestVersion = null;
                String downloadUrl = null;
                boolean isDraft = false;

                if (isDev) {
                    // DEV режим: скачиваем ТОЛЬКО черновики
                    JsonArray releases = JsonParser.parseString(body).getAsJsonArray();

                    for (int i = 0; i < releases.size(); i++) {
                        JsonObject release = releases.get(i).getAsJsonObject();
                        boolean draft = release.get("draft").getAsBoolean();
                        boolean prerelease = release.get("prerelease").getAsBoolean();

                        if (prerelease) continue;

                        // Ищем только черновики
                        if (draft) {
                            String tagName = release.get("tag_name").getAsString().replace("v", "");
                            latestVersion = tagName;
                            isDraft = true;

                            JsonArray assets = release.getAsJsonArray("assets");
                            if (assets.size() > 0) {
                                downloadUrl = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                            }
                            break;
                        }
                    }

                    // Если черновиков нет, выходим
                    if (latestVersion == null) {
                        plugin.getLogger().info("Нет доступных черновиков для скачивания.");
                        return;
                    }
                } else {
                    // PROD режим: скачиваем ТОЛЬКО релизы
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    latestVersion = json.get("tag_name").getAsString().replace("v", "");
                    isDraft = false;

                    JsonArray assets = json.getAsJsonArray("assets");
                    if (assets.size() > 0) {
                        downloadUrl = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                    }
                }

                if (latestVersion == null || downloadUrl == null) {
                    plugin.getLogger().info("Не найдено обновлений.");
                    return;
                }

                String currentVersion = plugin.getDescription().getVersion();
                plugin.getLogger().info("Текущая: " + currentVersion + " | Найдена: " + latestVersion + (isDraft ? " (ЧЕРНОВИК)" : ""));

                if (!currentVersion.equals(latestVersion)) {
                    plugin.getLogger().info("Доступно обновление! Версия " + latestVersion + (isDraft ? " (ЧЕРНОВИК)" : ""));
                    downloadUpdate(downloadUrl, latestVersion, isDraft);
                } else {
                    plugin.getLogger().info("У вас последняя версия!");
                }

            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка проверки обновлений: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void downloadUpdate(String downloadUrl, String newVersion, boolean isDraft) {
        if (updating) return;
        updating = true;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getLogger().info("Загрузка обновления" + (isDraft ? " (ЧЕРНОВИК)" : "") + "...");

                File pluginFile = new File(plugin.getDataFolder().getParentFile(), PLUGIN_NAME);
                File tempFile = new File(plugin.getDataFolder().getParentFile(), PLUGIN_NAME + ".tmp");

                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(downloadUrl))
                        .header("User-Agent", "Mozilla/5.0")
                        .GET()
                        .build();

                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == 200) {
                    Files.copy(response.body(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    plugin.getLogger().severe("Ошибка загрузки. Код: " + response.statusCode());
                    updating = false;
                    return;
                }

                if (!tempFile.exists() || tempFile.length() == 0) {
                    plugin.getLogger().severe("Файл не загрузился!");
                    updating = false;
                    return;
                }

                plugin.getLogger().info("Файл загружен: " + tempFile.length() + " байт");

                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        plugin.getLogger().info("Установка обновления...");
                        Bukkit.getPluginManager().disablePlugin(plugin);

                        if (pluginFile.exists()) {
                            pluginFile.delete();
                        }

                        Thread.sleep(1000);

                        tempFile.renameTo(pluginFile);

                        if (pluginFile.exists() && pluginFile.length() > 0) {
                            plugin.getLogger().info("Плагин обновлен до версии " + newVersion + (isDraft ? " (ЧЕРНОВИК)" : ""));
                            plugin.getLogger().info("Выключение сервера...");
                            updating = false;
                            Thread.sleep(2000);
                            Bukkit.shutdown();
                        } else {
                            plugin.getLogger().severe("Ошибка установки!");
                            updating = false;
                        }
                    } catch (Exception e) {
                        plugin.getLogger().severe("Ошибка установки: " + e.getMessage());
                        e.printStackTrace();
                        updating = false;
                    }
                });

            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка загрузки: " + e.getMessage());
                e.printStackTrace();
                updating = false;
            }
        });
    }
}