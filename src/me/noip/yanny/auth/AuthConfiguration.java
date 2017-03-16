package me.noip.yanny.auth;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.LoggerHandler;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

class AuthConfiguration {

    private static final String CONFIGURATION_NAME = "auth.yml";
    private static final String TRANSLATION_SECTION = "translation";

    private PreparedStatement userCountStatement;
    private LoggerHandler logger;
    private ServerConfigurationWrapper serverConfigurationWrapper;

    AuthConfiguration(MainPlugin plugin) {
        logger = plugin.getLoggerHandler();
        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);

        Connection connection = plugin.getConnection();

        try {
            userCountStatement = connection.prepareStatement("SELECT COUNT(*) FROM users");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        for (AuthTranslation translation : AuthTranslation.values()) {
            translation.setDisplayName(translationSection.getString(translation.name(), translation.getDisplayName()));
        }

        save(); // save defaults
        logger.logInfo(Auth.class, String.format("Registered users: %d", getUserCount()));
    }

    private void save() {
        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        for (AuthTranslation translation : AuthTranslation.values()) {
            translationSection.set(translation.name(), translation.getDisplayName());
        }

        serverConfigurationWrapper.save();
    }

    private int getUserCount() {
        int count = 0;

        try {
            ResultSet rs = userCountStatement.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }
}
