package me.noip.yanny;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

class AuthConfiguration {

    private static final String CONFIGURATION_NAME = "auth";
    private static final String TRANSLATION_SECTION = "translation";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private Map<String, String> translationMap = new HashMap<>();

    AuthConfiguration(Plugin plugin) {
        translationMap.put("msg_register", "Zaregistruj sa /register [heslo] [heslo]");
        translationMap.put("msg_login", "Prihlas sa /login [heslo]");
        translationMap.put("msg_registered", "Bol si uspesne zaregistrovany");
        translationMap.put("msg_logged", "Bol si uspesne prihlaseny");
        translationMap.put("msg_password_changed", "Heslo bolo uspesne zmenene");

        translationMap.put("msg_registered_all", "Hrac {player} sa registroval na server");
        translationMap.put("msg_logged_all", "Hrac {player} sa prihlasil na server");

        translationMap.put("msg_err_logged", "Uz si prihlaseny");
        translationMap.put("msg_err_registered", "Uz si zaregistrovany");
        translationMap.put("msg_err_wrong_password", "Zadal si zle heslo");
        translationMap.put("msg_err_not_registered", "Najprv sa zaregistruj");
        translationMap.put("msg_err_password_not_same", "Zadane hesla sa nezhoduju");
        translationMap.put("msg_err_characters", "Heslo moze obsahovat len znaky [a-z], [A-Z] a [0-9]");
        translationMap.put("msg_err_length", "Heslo musi mat 6 - 32 znakov");
        translationMap.put("msg_err_register", "Chyba pocas registracie, skus znova");
        translationMap.put("msg_err_password", "Chyba pocas zmeny hesla, skus znova");
        translationMap.put("msg_err_command", "Nemas opravnenie na prikaz ak nie si prihlaseny");
        translationMap.put("msg_err_chat", "Nemas opravnenie na prikaz ak nie si prihlaseny");

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        translationMap.putAll(ServerConfigurationWrapper.convertMap(translationSection.getValues(false)));

        save(); // save defaults
    }

    void save() {
        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        for (HashMap.Entry<String, String> pair : translationMap.entrySet()) {
            translationSection.set(pair.getKey(), pair.getValue());
        }
        serverConfigurationWrapper.save();
    }

    String getTranslation(String key) {
        return translationMap.get(key);
    }
}
