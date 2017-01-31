package me.noip.yanny;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;

class PlayerConfigurationWrapper extends YamlConfiguration {

    private static final String FOLDER = "players";

    private Plugin plugin;
    private File file;

    PlayerConfigurationWrapper(Plugin plugin, Player player) {
        this.plugin = plugin;
        file = new File(getPath(plugin, player));
    }

    void load() {
        if (!file.exists()) {
            File folder = file.getParentFile();

            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    plugin.getLogger().warning("Cant create players folder!");
                }
            }

            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Cant create player file " + file.getName());
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

    private static String getPath(Plugin plugin, Player player) {
        return plugin.getDataFolder() + "/" + FOLDER + "/" + player.getUniqueId() + ".yml";
    }
}
