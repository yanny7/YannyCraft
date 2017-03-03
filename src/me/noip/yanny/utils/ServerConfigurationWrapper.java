package me.noip.yanny.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ServerConfigurationWrapper extends YamlConfiguration {

    private Plugin plugin;
    private File file;

    public ServerConfigurationWrapper(Plugin plugin, String configName) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder() + "/" + configName + ".yml");
    }

    public void load() {
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

    public void save() {
        try {
            save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
