package me.noip.yanny;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

class ChestConfiguration {

    private static final String CONFIGURATION_NAME = "chests";

    private static final String TRANSLATION_SECTION = "translation";

    private static final String LOCKPICKING_CHANCE = "lockpicking_chance";

    private PreparedStatement getOwnerStatement;
    private PreparedStatement removeChestStatement;
    private PreparedStatement addChestStatement;

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private Map<String, String> translationMap = new HashMap<>();
    private double lockpickingChance = 0.05;

    ChestConfiguration(Plugin plugin, Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS chests ("
                    + "Location VARCHAR(64) PRIMARY KEY NOT NULL,"
                    + "Player VARCHAR(64) NOT NULL)");
            statement.execute("CREATE INDEX IF NOT EXISTS PlayerIndex ON chests( Player )");

            getOwnerStatement = connection.prepareStatement("SELECT Player FROM chests WHERE Location = ?");
            removeChestStatement = connection.prepareStatement("DELETE FROM chests WHERE Location = ?");
            addChestStatement = connection.prepareStatement("INSERT INTO chests (Location, Player) VALUES (?, ?)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        translationMap.put("msg_chest_lock", "Uzamkol si truhlicu");
        translationMap.put("msg_chest_locked", "Truhlica je uzamknuta");
        translationMap.put("msg_chest_unlocked", "Odomkol si truhlicu");
        translationMap.put("msg_chest_destroyed", "Znicil si uzamknutu truhlicu");
        translationMap.put("msg_chest_protected", "Truhlica je uzamknuta");
        translationMap.put("msg_chest_owned", "Truhlica je uz uzamknuta");
        translationMap.put("msg_chest_not_owned", "Nevlastnis tuto truhlicu");
        translationMap.put("msg_chest_lockpicking", "Nepodarilo sa ti odomknut truhlicu");
        translationMap.put("msg_chest_invalid", "Nemieris na truhlicu");
        translationMap.put("msg_chest_not_locked", "Truhlica je odomknuta");

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        translationMap.putAll(ServerConfigurationWrapper.convertMapString(translationSection.getValues(false)));

        lockpickingChance = serverConfigurationWrapper.getDouble(LOCKPICKING_CHANCE, lockpickingChance);

        save();
    }

    private void save() {
        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        for (HashMap.Entry<String, String> pair : translationMap.entrySet()) {
            translationSection.set(pair.getKey(), pair.getValue());
        }

        serverConfigurationWrapper.set(LOCKPICKING_CHANCE, lockpickingChance);

        serverConfigurationWrapper.save();
    }

    String getTranslation(String key) {
        return translationMap.get(key);
    }

    String getOwner(String location) {
        String owner = null;

        try {
            getOwnerStatement.setString(1, location);
            ResultSet rs = getOwnerStatement.executeQuery();

            if (rs.next()) {
                owner = rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return owner;
    }

    void removeChest(String location) {
        try {
            removeChestStatement.setString(1, location);
            removeChestStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addChest(String location, String player) {
        try {
            addChestStatement.setString(1, location);
            addChestStatement.setString(2, player);
            addChestStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    double getLockpickingChance() {
        return lockpickingChance;
    }

    static String locationToString(Location location) {
        return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + " " + location.getWorld().getName();
    }

/*
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
    }*/
}
