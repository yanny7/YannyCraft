package me.noip.yanny.residence;

import me.noip.yanny.utils.ServerConfigurationWrapper;
import me.noip.yanny.utils.Area;
import me.noip.yanny.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ResidenceConfiguration {

    private static final String CONFIGURATION_NAME = "residence";

    private static final String TRANSLATION_SECTION = "translation";

    private static final String RESIDENCE_MATERIAL = "res_material";

    private Plugin plugin;
    private ServerConfigurationWrapper serverConfigurationWrapper;
    private Map<String, String> translationMap = new HashMap<>();

    private PreparedStatement addResidenceStatement;
    private PreparedStatement getResidenceStatement;
    private PreparedStatement getAllResidenceStatement;
    private PreparedStatement removeResidenceStatement;

    private Material residenceMaterial;

    ResidenceConfiguration(Plugin plugin, Connection connection) {
        this.plugin = plugin;

        try {
            addResidenceStatement = connection.prepareStatement("INSERT INTO residence (Player, Location1, Location2) VALUES (?, ?, ?)");
            getResidenceStatement = connection.prepareStatement("SELECT Location1, Location2 FROM residence WHERE Player = ?");
            getAllResidenceStatement = connection.prepareStatement("SELECT Player, Location1, Location2 FROM residence");
            removeResidenceStatement = connection.prepareStatement("DELETE FROM residence WHERE Player = ? AND Location1 = ? AND Location2 = ?");
        } catch (Exception e) {
            e.printStackTrace();
        }

        translationMap.put("msg_res_created", "Vytvoril si rezidenciu");
        translationMap.put("msg_res_exists", "Uz tu je vytvorena rezidencia");
        translationMap.put("msg_res_wrong_place", "Nestojis na spravnom mieste pre vytvorenie rezidencie");
        translationMap.put("msg_res_removed", "Rezidencia bola zrusena");
        translationMap.put("msg_res_not_owned", "Nevlastnis tuto rezidenciu");
        translationMap.put("msg_res_not_exists", "Na tomto mieste nieje ziadna rezidencia");
        translationMap.put("msg_res_owner", "Majitel: {player}");
        translationMap.put("msg_res_foreign", "Nemas opravnenia v tejto rezidencii");

        residenceMaterial = Material.REDSTONE_BLOCK;

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        translationMap.putAll(ServerConfigurationWrapper.convertMapString(translationSection.getValues(false)));

        residenceMaterial = Material.getMaterial(serverConfigurationWrapper.getString(RESIDENCE_MATERIAL, residenceMaterial.name()));

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

        serverConfigurationWrapper.set(RESIDENCE_MATERIAL, residenceMaterial.name());

        serverConfigurationWrapper.save();
    }

    String getTranslation(String key) {
        return translationMap.get(key);
    }

    Material getResidenceMaterial() {
        return residenceMaterial;
    }

    void addResidence(Area area) {
        String loc1 = Utils.locationToString(area.first);
        String loc2 = Utils.locationToString(area.second);

        try {
            addResidenceStatement.setString(1, area.uuid);
            addResidenceStatement.setString(2, loc1);
            addResidenceStatement.setString(3, loc2);
            addResidenceStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    List<Area> getAllResidence() {
        List<Area> result = new ArrayList<>();

        try {
            ResultSet rs = getAllResidenceStatement.executeQuery();

            while (rs.next()) {
                Area area = new Area(Utils.parseLocation(rs.getString(2), plugin), Utils.parseLocation(rs.getString(3), plugin), rs.getString(1));
                result.add(area);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    List<Area> getResidence(Player player) {
        String uuid = player.getUniqueId().toString();
        List<Area> result = new ArrayList<>();

        try {
            getResidenceStatement.setString(1, uuid);
            ResultSet rs = getResidenceStatement.executeQuery();

            while (rs.next()) {
                Area area = new Area(Utils.parseLocation(rs.getString(1), plugin), Utils.parseLocation(rs.getString(2), plugin), uuid);
                result.add(area);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    void removeResidence(String uuid, Area area) {
        String loc1 = Utils.locationToString(area.first);
        String loc2 = Utils.locationToString(area.second);

        try {
            removeResidenceStatement.setString(1, uuid);
            removeResidenceStatement.setString(2, loc1);
            removeResidenceStatement.setString(3, loc2);
            removeResidenceStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
