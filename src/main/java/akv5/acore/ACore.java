package akv5.acore;

import akv5.acore.commands.*;
import akv5.acore.events.*;
import akv5.acore.libs.PluginHider;
import akv5.acore.libs.Plugins;
import akv5.acore.libs.Scheduler;
import akv5.acore.libs.configs.GrantConfig;
import akv5.acore.libs.hooks.*;
import akv5.acore.libs.managers.*;
import akv5.acore.placeholders.*;
import akv5.acore.trolls.*;
import akv5.acore.utils.*;
import akv5.acore.utils.customs.CustomItemEvents;
import akv5.acore.utils.customs.CustomItems;
import akv5.acore.utils.events.*;
import akv5.acore.utils.textures.*;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class ACore extends JavaPlugin {

    private static ACore instance;
    private static ACore plugin;
    private FixManager fixManager;
    private FileConfiguration infoConfig;
    private File infoFile;

    private File placeholdersFile;
    private FileConfiguration placeholdersConfig;
    private File playersFile;
    private FileConfiguration playersConfig;
    public LootBoxManager lootBoxManager;

    private UpdateManager updateManager;

    public static ACore getInstance() {
        return instance;
    }

    public static ACore getPlugin() {
        return plugin;
    }

    public FixManager getFixManager() {
        return fixManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        plugin = this;
        Scheduler.init(this);

        this.loadConfig();
        this.loadInfoConfig();
        this.loadPlaceholdersConfig();
        this.loadPlayersConfig();
        this.loadDatabase();
        this.loadLimits();

        PluginHider.initialize();

        Scheduler.doSyncTimer(() -> new PlayerMove(this), 0L, 0L);

        CustomItems customItems = CustomItems.getInstance();
        customItems.initialize();
        Bukkit.getPluginManager().registerEvents(customItems, this);

        CustomItemEvents customItemEvents = CustomItemEvents.getInstance();
        customItemEvents.initialize();
        Bukkit.getPluginManager().registerEvents(customItemEvents, this);

        Scheduler.doSyncTimer(() -> {
            long currentTime = Bukkit.getWorlds().get(0).getTime();
            CustomItemEvents.getInstance().handleTimeChange(currentTime);
        }, 0L, 100L);

        new EventExamples(this);

        fixManager = new FixManager();
        fixManager.registerAllFixes();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JumpPlate(), this);
        pm.registerEvents(new PlayerJoin(this), this);
        pm.registerEvents(new MobSpawnLimit(), this);

        // Блокировка снарядов рядом с админом
        //pm.registerEvents(new BlockLaunch(this), this);

        pm.registerEvents(new BlockCmdsInWorld(this), this);
        pm.registerEvents(new BlockCmds(), this);
        pm.registerEvents(new VoidCmds(), this);
        pm.registerEvents(new BlockSymbols(), this);
        pm.registerEvents(new NotifyCommands(), this);
        pm.registerEvents(new Vanish(this), this);

        // Телепортирует игрока наверх если он зашел на сервер ниже чем с 1.18+
        //pm.registerEvents(new HeightAndVersion(), this);

        pm.registerEvents(new MagicBall(), this);

        // CustomModelData
        //pm.registerEvents(new Inventory(), this);

        if (Plugins.AntiRelog.isEnabled()) {
            pm.registerEvents(new PvPArena(this), this);
        }
        if (Plugins.WorldGuard.isEnabled()) {
            pm.registerEvents(new PlayerMove(this), this);
        }

        TexturePack texturePack = new TexturePack();
        pm.registerEvents(texturePack, this);
        Objects.requireNonNull(this.getCommand("rp")).setExecutor(new TexturePack());

        this.lootBoxManager = new LootBoxManager(this);
        pm.registerEvents(new LootBoxListener(this, this.lootBoxManager), this);
        Objects.requireNonNull(this.getCommand("lootbox")).setExecutor(new LootBox(this.lootBoxManager));

        Objects.requireNonNull(this.getCommand("areload")).setExecutor(new Areload());
        Objects.requireNonNull(this.getCommand("abroadcast")).setExecutor(new Abroadcast());
        Objects.requireNonNull(this.getCommand("citems")).setExecutor(new CItems());
        Objects.requireNonNull(this.getCommand("8ball")).setExecutor(new MagicBall());
        Objects.requireNonNull(this.getCommand("modeldata")).setExecutor(new Modeldata());

        Objects.requireNonNull(this.getCommand("achievementtroll")).setExecutor(new AchievementTroll());
        Objects.requireNonNull(this.getCommand("bantroll")).setExecutor(new BanTroll());
        Objects.requireNonNull(this.getCommand("lifttroll")).setExecutor(new LiftTroll());
        Objects.requireNonNull(this.getCommand("portaltroll")).setExecutor(new PortalTroll());
        Objects.requireNonNull(this.getCommand("sixseven")).setExecutor(new SixSeven());

        Objects.requireNonNull(this.getCommand("chatcolor")).setExecutor(new Chatcolor(this));
        Objects.requireNonNull(this.getCommand("chatcolor")).setTabCompleter(new Chatcolor(this));

        Objects.requireNonNull(this.getCommand("avanish")).setExecutor(new Vanish(this));

        GrantConfig grantConfig = new GrantConfig(this);
        grantConfig.load();

        GrantManager grantManager = new GrantManager(this, grantConfig);

        Grant grantCommand = new Grant(this, grantManager, grantConfig);
        Objects.requireNonNull(getCommand("grant")).setExecutor(grantCommand);
        Objects.requireNonNull(getCommand("grant")).setTabCompleter(grantCommand);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            (new Placeholders(this)).register();
        }

        createItemsFile();

        updateManager = new UpdateManager(this);
        updateManager.checkForUpdates();

        new Broadcast().autoMessages();

        spawnParticle();
    }

    @Override
    public void onDisable() {
        if (fixManager != null) {
            fixManager.unregisterProtocolListeners();
        }
    }

    private void loadDatabase() {
        if (Plugins.EmoteCraft.isEnabled()) {
            File base = this.getDataFolder().getParentFile().getParentFile();
            File emotes = new File(base, EmoteCraftHook.Emotes.class.getSimpleName().toLowerCase());

            if (!emotes.exists() && emotes.mkdir()) {
                getLogger().info("The emotes folder has been successfully created.");
            }

            Stream.of(EmoteCraftHook.Emotes.values()).forEach(emote -> {
                File file = new File(emotes, emote.name() + ".json");
                if (file.exists() && !file.delete()) {
                    getLogger().warning("An error occurred when deleting the previous copy of the emote: " + emote.name());
                    return;
                }

                try (InputStream inputStream = this.getResource(EmoteCraftHook.Emotes.class.getSimpleName().toLowerCase() + "/" + emote.name() + ".json")) {
                    if (inputStream != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                            String line;
                            StringBuilder stringBuilder = new StringBuilder();
                            while ((line = reader.readLine()) != null) {
                                stringBuilder.append(line);
                                stringBuilder.append(System.lineSeparator());
                            }
                            Files.writeString(file.toPath(), stringBuilder.toString());
                            getLogger().info("Emote " + emote.name() + " was successfully loaded!");
                        } catch (IOException e) {
                            getLogger().severe("An error occurred while creating the file: " + file.getAbsolutePath());
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    getLogger().severe("An error occurred while trying to access the file: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            });
            EmoteCraftHook.reload();
        }
    }

    public void loadPlaceholdersConfig() {
        if (placeholdersFile == null) placeholdersFile = new File(getDataFolder(), "customs/placeholders.yml");
        if (!placeholdersFile.exists()) {
            saveResource("customs/placeholders.yml", false);
        }
        placeholdersConfig = YamlConfiguration.loadConfiguration(placeholdersFile);
    }

    public FileConfiguration getPlaceholdersConfig() {
        return placeholdersConfig;
    }

    public void reloadPlaceholdersConfig() {
        if (placeholdersFile == null) placeholdersFile = new File(getDataFolder(), "customs/placeholders.yml");
        placeholdersConfig = YamlConfiguration.loadConfiguration(placeholdersFile);
    }

    public void loadPlayersConfig() {
        if (playersFile == null) {
            playersFile = new File(getDataFolder(), "data/players.yml");
        }
        if (!playersFile.exists()) {
            try {
                playersFile.getParentFile().mkdirs();
                playersFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create players.yml: " + e.getMessage());
            }
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);
    }

    public FileConfiguration getPlayersConfig() {
        return playersConfig;
    }

    public void savePlayersConfig() {
        if (playersConfig != null && playersFile != null) {
            try {
                playersConfig.save(playersFile);
            } catch (IOException e) {
                getLogger().severe("Could not save players.yml: " + e.getMessage());
            }
        }
    }

    public void loadInfoConfig() {
        if (infoFile == null) infoFile = new File(getDataFolder(), "info.yml");
        if (!infoFile.exists()) {
            saveResource("info.yml", false);
        }
        infoConfig = YamlConfiguration.loadConfiguration(infoFile);
    }

    public FileConfiguration getInfoConfig() {
        return infoConfig;
    }

    public void reloadInfoConfig() {
        if (infoFile == null) infoFile = new File(getDataFolder(), "info.yml");
        infoConfig = YamlConfiguration.loadConfiguration(infoFile);
    }

    public FileConfiguration getItemsConfig() {
        File file = new File(getDataFolder(), "customs/items.yml");
        if (!file.exists()) {
            createItemsFile();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void saveItemsConfig(FileConfiguration config) {
        try {
            config.save(new File(getDataFolder(), "customs/items.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createItemsFile() {
        File file = new File(getDataFolder(), "customs/items.yml");
        if (!file.exists()) {
            try {
                saveResource("customs/items.yml", false);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void loadLimits() {
        Limits limits = new Limits(this);
        limits.getCommandLimit("randomer");
        limits.getCommandLimit("pull");
    }

    private void loadConfig() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + getFile());
        }

        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }

        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    private void spawnParticle() {
        if (!isServerVersionAtLeast(1, 20)) {
            return;
        }

        String worldName = ACore.getInstance().getConfig().getString("settings.particles.cherry.world");
        assert worldName != null;
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            String particleName = ACore.getInstance().getConfig().getString("settings.particles.cherry.particle");
            Particle particleType = Particle.valueOf(particleName);

            if (ACore.getInstance().getConfig().getBoolean("settings.particles.cherry.enabled")) {
                double x = ACore.getInstance().getConfig().getDouble("settings.particles.cherry.coordinates.x");
                double y = ACore.getInstance().getConfig().getDouble("settings.particles.cherry.coordinates.y");
                double z = ACore.getInstance().getConfig().getDouble("settings.particles.cherry.coordinates.z");

                Location spawnLocation = new Location(world, x, y, z);

                int count = ACore.getInstance().getConfig().getInt("settings.particles.cherry.counts");
                double offsetX = ACore.getInstance().getConfig().getDouble("settings.particles.cherry.deltaX");
                double offsetY = ACore.getInstance().getConfig().getDouble("settings.particles.cherry.deltaY");
                double offsetZ = ACore.getInstance().getConfig().getDouble("settings.particles.cherry.deltaZ");
                double speed = ACore.getInstance().getConfig().getDouble("settings.particles.cherry.speed");

                Scheduler.doAsyncRepeat(() -> {
                    world.spawnParticle(particleType, spawnLocation, count, offsetX, offsetY, offsetZ, speed);
                }, 0L, 20L);
            }
        }
    }

    private boolean isServerVersionAtLeast(int major, int minor) {
        String version = Bukkit.getBukkitVersion();
        String[] parts = version.split("-")[0].split("\\.");
        try {
            int serverMajor = Integer.parseInt(parts[0]);
            int serverMinor = Integer.parseInt(parts[1]);

            if (serverMajor > major) {
                return true;
            } else if (serverMajor == major) {
                return serverMinor >= minor;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
