package me.noip.yanny.utils;

import me.noip.yanny.MainPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ServerConfigurationWrapper extends YamlConfiguration {

    private MainPlugin plugin;
    private File file;

    public ServerConfigurationWrapper(MainPlugin plugin, String configName) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder() + "/" + configName);
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
