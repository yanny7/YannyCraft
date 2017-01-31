package me.noip.yanny;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class PlayerConfiguration {

    private JavaPlugin plugin;
    private Map<UUID, PlayerConfigurationWrapper> playerConfiguration = new HashMap<>();
    private ConfigurationListener listener;

    PlayerConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
        listener = new ConfigurationListener();
    }

    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerConfigurationWrapper configuration = new PlayerConfigurationWrapper(plugin, player);

            configuration.load();
            playerConfiguration.put(player.getUniqueId(), configuration);
        }
    }

    void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerConfigurationWrapper configuration = playerConfiguration.get(player.getUniqueId());
            configuration.save();
        }
    }

    PlayerConfigurationWrapper getConfiguration(Player player) {
        return playerConfiguration.get(player.getUniqueId());
    }

    class ConfigurationListener implements Listener {

        @EventHandler
        void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            PlayerConfigurationWrapper configuration = playerConfiguration.get(player.getUniqueId());

            if (configuration == null) {
                configuration = new PlayerConfigurationWrapper(plugin, player);
                plugin.getLogger().warning("onPlayerAuth: Configuration not found! Creating new one");
            }

            configuration.load();
            playerConfiguration.put(uuid, configuration);
        }

        @EventHandler
        void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            PlayerConfigurationWrapper configuration = playerConfiguration.get(player.getUniqueId());

            if (configuration == null) {
                plugin.getLogger().warning("onPlayerQuit: Configuration not found!" + player.getDisplayName());
                return;
            }

            configuration.save();
        }
    }
}
