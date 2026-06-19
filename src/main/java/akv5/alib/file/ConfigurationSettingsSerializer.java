package akv5.alib.file;

import java.util.Map;

public interface ConfigurationSettingsSerializer {
    int getIndent();
    String serialize(String key, Object object);
    String getLineBreak();
    Map<String, Object> getValues(boolean deep);
    Map<String, Object> getValues(Object section, boolean deep);
    boolean isConfigurationSection(String path);
    boolean isConfigurationSection(Object object);
    @SuppressWarnings("rawtypes") void registerSerializable(Class clazz);
}