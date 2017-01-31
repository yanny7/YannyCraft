package me.noip.yanny;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class ServerConfigurationWrapper extends YamlConfiguration {

    private Plugin plugin;
    private File file;

    ServerConfigurationWrapper(Plugin plugin, String configName) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder() + "/" + configName + ".yml");
    }

    void load() {
        if (!file.exists()) {
            File folder = file.getParentFile();

            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    plugin.getLogger().warning("Cant create config folder!");
                }
            }

            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Cant create config file " + file.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void save() {
        try {
            save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Map<String,String> convertMap(Map<String, Object> map) {
        Map<String,String> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if(entry.getValue() instanceof String){
                newMap.put(entry.getKey(), (String) entry.getValue());
            }
        }

        return newMap;
    }
}
