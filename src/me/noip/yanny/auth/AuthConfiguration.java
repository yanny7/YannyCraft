package me.noip.yanny.auth;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import org.bukkit.configuration.ConfigurationSection;

class AuthConfiguration {

    private static final String CONFIGURATION_NAME = "auth";
    private static final String TRANSLATION_SECTION = "translation";

    private ServerConfigurationWrapper serverConfigurationWrapper;

    AuthConfiguration(MainPlugin plugin) {
        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
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
    }

    private void save() {
        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        for (AuthTranslation translation : AuthTranslation.values()) {
            translationSection.set(translation.name(), translation.getDisplayName());
        }

        serverConfigurationWrapper.save();
    }
}
