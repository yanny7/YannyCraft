package me.noip.yanny;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

class PlayerConfiguration {

    private JavaPlugin plugin;
    private Map<UUID, PlayerConfigurationData> playerConfiguration = new HashMap<>();
    private ConfigurationListener listener;

    PlayerConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
        listener = new ConfigurationListener();
    }

    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerConfigurationWrapper configuration = new PlayerConfigurationWrapper(plugin, player);
            PlayerConfigurationData data = new PlayerConfigurationData(configuration, player);

            data.load();
            playerConfiguration.put(player.getUniqueId(), data);
        }
    }

    void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerConfigurationData data = playerConfiguration.get(player.getUniqueId());
            data.save();
        }
    }

    Location getHomeLocation(Player player) {
        PlayerConfigurationData data = playerConfiguration.get(player.getUniqueId());

        if (data != null) {
            return data.getHomeLocation();
        } else {
            return null;
        }
    }

    void setHomeLocation(Player player, Location location) {
        PlayerConfigurationData data = playerConfiguration.get(player.getUniqueId());

        if (data != null) {
            data.setHomeLocation(location);
        }
    }

    Location getBackLocation(Player player) {
        PlayerConfigurationData data = playerConfiguration.get(player.getUniqueId());

        if (data != null) {
            return data.getBackLocation();
        } else {
            return null;
        }
    }

    void setBackLocation(Player player, Location location) {
        PlayerConfigurationData data = playerConfiguration.get(player.getUniqueId());

        if (data != null) {
            data.setBackLocation(location);
        }
    }

    void incrementStatistic(Player player, RewardWrapper.RewardType type) {
        PlayerConfigurationData data = playerConfiguration.get(player.getUniqueId());

        if (data != null) {
            data.incrementStatistic(type);
        }
    }

    int getStatistic(Player player, RewardWrapper.RewardType type) {
        PlayerConfigurationData data = playerConfiguration.get(player.getUniqueId());

        if (data != null) {
            return data.getStatistic(type);
        } else {
            plugin.getLogger().warning("PlayerConfiguration.getStatistic: Cant get correct statistic for player " + player.getDisplayName());
            return 0;
        }
    }

    List<String> getChestLocations(Player player) {
        PlayerConfigurationData data = playerConfiguration.get(player.getUniqueId());

        if (data != null) {
            return data.getChestLocations();
        } else {
            plugin.getLogger().warning("PlayerConfiguration.getChestLocations: Cant get correct statistic for player " + player.getDisplayName());
            return new ArrayList<>();
        }
    }

    private class PlayerConfigurationData {

        private static final String HOME_SECTION = "home";
        private static final String STATISTICS_SECTION = "statistics";
        private static final String CHEST_SECTION = "chests";

        private static final String HOME_LOCATION = "home_location";
        private static final String BACK_LOCATION = "back_location";

        private PlayerConfigurationWrapper playerConfigurationWrapper;
        private Player player;

        private Location homeLocation;
        private Location backLocation;
        private Map<String, Integer> statistics = new HashMap<>();
        private List<String> chestLocations = new ArrayList<>();

        PlayerConfigurationData(PlayerConfigurationWrapper playerConfigurationWrapper, Player player) {
            this.playerConfigurationWrapper = playerConfigurationWrapper;
            this.player = player;

            for (RewardWrapper.RewardType type : RewardWrapper.RewardType.values()) {
                statistics.put(type.name(), 0);
            }
        }

        void load() {
            playerConfigurationWrapper.load();

            ConfigurationSection homeSection = playerConfigurationWrapper.getConfigurationSection(HOME_SECTION);
            if (homeSection == null) {
                homeSection = playerConfigurationWrapper.createSection(HOME_SECTION);
            }
            homeLocation = (Location) homeSection.get(HOME_LOCATION, player.getWorld().getSpawnLocation());
            backLocation = (Location) homeSection.get(BACK_LOCATION, player.getWorld().getSpawnLocation());

            ConfigurationSection statisticsSection = playerConfigurationWrapper.getConfigurationSection(STATISTICS_SECTION);
            if (statisticsSection == null) {
                statisticsSection = playerConfigurationWrapper.createSection(STATISTICS_SECTION);
            }

            ConfigurationSection chestsSection = playerConfigurationWrapper.getConfigurationSection(CHEST_SECTION);
            if (chestsSection == null) {
                chestsSection = playerConfigurationWrapper.createSection(CHEST_SECTION);
            }
            chestLocations = chestsSection.getStringList(CHEST_SECTION);

            statistics.putAll(ServerConfigurationWrapper.convertMapInteger(statisticsSection.getValues(false)));
            save();
        }

        void save() {
            ConfigurationSection homeSection = playerConfigurationWrapper.getConfigurationSection(HOME_SECTION);
            homeSection.set(HOME_LOCATION, homeLocation);
            homeSection.set(BACK_LOCATION, backLocation);

            ConfigurationSection statisticsSection = playerConfigurationWrapper.getConfigurationSection(STATISTICS_SECTION);
            for (HashMap.Entry<String, Integer> pair : statistics.entrySet()) {
                statisticsSection.set(pair.getKey(), pair.getValue());
            }

            ConfigurationSection chestsSection = playerConfigurationWrapper.getConfigurationSection(CHEST_SECTION);
            chestsSection.set(CHEST_SECTION, chestLocations);

            playerConfigurationWrapper.save();
        }

        Location getHomeLocation() {
            return homeLocation;
        }

        void setHomeLocation(Location location) {
            homeLocation = location;
        }

        Location getBackLocation() {
            return backLocation;
        }

        void setBackLocation(Location location) {
            backLocation = location;
        }

        void incrementStatistic(RewardWrapper.RewardType type) {
            statistics.replace(type.name(), statistics.get(type.name()) + 1);
        }

        int getStatistic(RewardWrapper.RewardType type) {
            return statistics.get(type.name());
        }

        List<String> getChestLocations() {
            return chestLocations;
        }
    }

    class ConfigurationListener implements Listener {

        @EventHandler
        void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            PlayerConfigurationWrapper configuration = new PlayerConfigurationWrapper(plugin, player);
            PlayerConfigurationData data  = new PlayerConfigurationData(configuration, player);

            data.load();
            playerConfiguration.put(uuid, data);
        }

        @EventHandler
        void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            PlayerConfigurationData data = playerConfiguration.get(player.getUniqueId());

            if (data == null) {
                plugin.getLogger().warning("onPlayerQuit: Configuration not found!" + player.getDisplayName());
                return;
            }

            data.save();
            playerConfiguration.remove(player.getUniqueId());
        }
    }
}
