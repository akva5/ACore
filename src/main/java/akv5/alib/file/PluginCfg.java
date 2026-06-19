package akv5.alib.file;

public interface PluginCfg {
    PluginCfg[] getValues();
    String getName();
    String getVersion();
    String getFile();
    Configuration getConfig();
    void setConfig(Configuration config);
    void setVersion(String version);
    void setFile(String file);
    void reload(ConfigurationProvider version);
}