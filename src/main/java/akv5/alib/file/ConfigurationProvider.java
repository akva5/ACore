package akv5.alib.file;

import akv5.alib.file.configuration.file.YamlConfiguration;
import java.io.File;

public interface ConfigurationProvider {
    File getConfigFile();
    <T> T get(String path);
    void set(String path, Object value);
    void reloadFileFromDisk();
    YamlConfiguration getYamlConfiguration();
    void setFile(File file);
    boolean isFileSuccessfullyLoaded();
    ConfigurationSettingsSerializer getConfigurationSettingsSerializer();
}