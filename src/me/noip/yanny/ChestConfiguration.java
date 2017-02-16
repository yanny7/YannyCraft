package me.noip.yanny;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.*;

class ChestConfiguration {

    private static final String CONFIGURATION_NAME = "chests";

    private static final String TRANSLATION_SECTION = "translation";
    private static final String LOCATIONS_SECTION = "locations";

    private static final String LOCKPICKING_CHANCE = "lockpicking_chance";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private Map<String, String> chestLocations = new HashMap<>();
    private Map<String, String> translationMap = new HashMap<>();
    private Plugin plugin;
    private double lockpickingChance = 0.05;

    ChestConfiguration(Plugin plugin) {
        this.plugin = plugin;

        translationMap.put("msg_chest_lock", "Uzamkol si truhlicu");
        translationMap.put("msg_chest_locked", "Truhlica je uzamknuta");
        translationMap.put("msg_chest_unlocked", "Odomkol si truhlicu");
        translationMap.put("msg_chest_destroyed", "Znicil si uzamknutu truhlicu");
        translationMap.put("msg_chest_protected", "Truhlica je chranena");
        translationMap.put("msg_chest_not_owned", "Nevlastnis tuto truhlicu");
        translationMap.put("msg_chest_lockpicking", "Nepodarilo sa ti odomknut truhlicu");

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection locationSection = serverConfigurationWrapper.getConfigurationSection(LOCATIONS_SECTION);
        if (locationSection == null) {
            locationSection = serverConfigurationWrapper.createSection(LOCATIONS_SECTION);
        }
        chestLocations.putAll(ServerConfigurationWrapper.convertMapString(locationSection.getValues(false)));
        plugin.getLogger().info("ChestConfiguration.load: loaded " + chestLocations.size() + " entries");

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        translationMap.putAll(ServerConfigurationWrapper.convertMapString(translationSection.getValues(false)));

        lockpickingChance = serverConfigurationWrapper.getDouble(LOCKPICKING_CHANCE, lockpickingChance);

        save();
    }

    void save() {
        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        for (HashMap.Entry<String, String> pair : translationMap.entrySet()) {
            translationSection.set(pair.getKey(), pair.getValue());
        }

        serverConfigurationWrapper.set(LOCATIONS_SECTION, null);
        ConfigurationSection locationSection = serverConfigurationWrapper.getConfigurationSection(LOCATIONS_SECTION);
        if (locationSection == null) {
            locationSection = serverConfigurationWrapper.createSection(LOCATIONS_SECTION);
        }
        for (HashMap.Entry<String, String> pair : chestLocations.entrySet()) {
            locationSection.set(pair.getKey(), pair.getValue());
        }

        serverConfigurationWrapper.set(LOCKPICKING_CHANCE, lockpickingChance);

        serverConfigurationWrapper.save();
    }

    Map<String, String> getChestLocations() {
        return chestLocations;
    }

    String getTranslation(String key) {
        return translationMap.get(key);
    }

    double getLockpickingChance() {
        return lockpickingChance;
    }

    static String locationToString(Location location) {
        return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + " " + location.getWorld().getName();
    }

    static Location parseLocation(String location, Plugin plugin) {
        String[] tokens = location.split(" ");

        if (tokens.length != 4) {
            plugin.getLogger().warning("ChestConfiguration.parseLocation: Invalid location data: " + location);
            return null;
        }

        World world = plugin.getServer().getWorld(tokens[3]);
        double x, y, z;

        try {
            x = Double.parseDouble(tokens[0]);
            y = Double.parseDouble(tokens[1]);
            z = Double.parseDouble(tokens[2]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

        if (world == null) {
            plugin.getLogger().warning("ChestConfiguration.parseLocation: Invalid world name: " + tokens[3]);
            return null;
        }

        return new Location(world, x, y, z);
    }
}
