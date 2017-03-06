package me.noip.yanny.chestlocker;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

class ChestConfiguration {

    private static final String CONFIGURATION_NAME = "chests";
    private static final String TRANSLATION_SECTION = "translation";
    private static final String LOCKPICKING_CHANCE = "lockpicking_chance";

    private PreparedStatement getOwnerStatement;
    private PreparedStatement removeChestStatement;
    private PreparedStatement addChestStatement;

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private double lockpickingChance = 0.05;

    ChestConfiguration(MainPlugin plugin) {
        Connection connection = plugin.getConnection();

        try {
            getOwnerStatement = connection.prepareStatement("SELECT Player FROM chests WHERE Location = ?");
            removeChestStatement = connection.prepareStatement("DELETE FROM chests WHERE Location = ?");
            addChestStatement = connection.prepareStatement("INSERT INTO chests (Location, Player) VALUES (?, ?)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        for (ChestTranslation translation : ChestTranslation.values()) {
            translation.setDisplayName(translationSection.getString(translation.name(), translation.getDisplayName()));
        }

        lockpickingChance = serverConfigurationWrapper.getDouble(LOCKPICKING_CHANCE, lockpickingChance);

        save();
    }

    private void save() {
        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        for (ChestTranslation translation : ChestTranslation.values()) {
            translationSection.set(translation.name(), translation.getDisplayName());
        }

        serverConfigurationWrapper.set(LOCKPICKING_CHANCE, lockpickingChance);

        serverConfigurationWrapper.save();
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
}
