package me.noip.yanny.auth;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import me.noip.yanny.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

class AuthConfiguration {

    private static final String CONFIGURATION_NAME = "auth";
    private static final String TRANSLATION_SECTION = "translation";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private Map<String, String> translationMap = new HashMap<>();

    AuthConfiguration(MainPlugin plugin) {
        translationMap.put("msg_register", "Zaregistruj sa /register [heslo] [heslo]");
        translationMap.put("msg_login", "Prihlas sa /login [heslo]");
        translationMap.put("msg_registered", "Bol si uspesne zaregistrovany");
        translationMap.put("msg_logged", "Bol si uspesne prihlaseny");
        translationMap.put("msg_password_changed", "Heslo bolo uspesne zmenene");

        translationMap.put("msg_registered_all", "Hrac {player} sa registroval na server");
        translationMap.put("msg_logged_all", "Hrac {player} sa prihlasil na server");
        translationMap.put("msg_disconnect_all", "Hrac {player} sa odhlasil zo servera");

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
        translationMap.putAll(Utils.convertMapString(translationSection.getValues(false)));

        save(); // save defaults
    }

    private void save() {
        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        translationMap.forEach(translationSection::set);

        serverConfigurationWrapper.save();
    }

    String getTranslation(String key) {
        return translationMap.get(key);
    }
}
