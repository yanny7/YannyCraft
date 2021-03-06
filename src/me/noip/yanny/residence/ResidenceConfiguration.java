package me.noip.yanny.residence;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.LoggerHandler;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import me.noip.yanny.utils.Area;
import me.noip.yanny.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

class ResidenceConfiguration {

    private static final String CONFIGURATION_NAME = "residence.yml";
    private static final String TRANSLATION_SECTION = "translation";
    private static final String RESIDENCE_MATERIAL = "res_material";

    private MainPlugin plugin;
    private LoggerHandler logger;
    private ServerConfigurationWrapper serverConfigurationWrapper;

    private PreparedStatement addResidenceStatement;
    private PreparedStatement getResidenceStatement;
    private PreparedStatement getAllResidenceStatement;
    private PreparedStatement removeResidenceStatement;
    private PreparedStatement residenceCountStatement;

    private Material residenceMaterial;

    ResidenceConfiguration(MainPlugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLoggerHandler();

        Connection connection = plugin.getConnection();

        try {
            addResidenceStatement = connection.prepareStatement("INSERT INTO residence (Player, Location1, Location2) VALUES (?, ?, ?)");
            getResidenceStatement = connection.prepareStatement("SELECT Location1, Location2 FROM residence WHERE Player = ?");
            getAllResidenceStatement = connection.prepareStatement("SELECT Player, Location1, Location2 FROM residence");
            removeResidenceStatement = connection.prepareStatement("DELETE FROM residence WHERE Player = ? AND Location1 = ? AND Location2 = ?");
            residenceCountStatement = connection.prepareStatement("SELECT COUNT(*) FROM residence");
        } catch (Exception e) {
            e.printStackTrace();
        }

        residenceMaterial = Material.REDSTONE_BLOCK;

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection != null) {
            for (ResidenceTranslation translation : ResidenceTranslation.values()) {
                translation.setDisplayName(translationSection.getString(translation.name(), translation.getDisplayName()));
            }
        }

        residenceMaterial = Material.getMaterial(serverConfigurationWrapper.getString(RESIDENCE_MATERIAL, residenceMaterial.name()));

        save();

        logger.logInfo(Residence.class, String.format("Residence material: %s", residenceMaterial.name()));
        logger.logInfo(Residence.class, String.format("Stored residences: %d", getResidenceCount()));
    }

    private void save() {
        ConfigurationSection translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        for (ResidenceTranslation translation : ResidenceTranslation.values()) {
            translationSection.set(translation.name(), translation.getDisplayName());
        }

        serverConfigurationWrapper.set(RESIDENCE_MATERIAL, residenceMaterial.name());

        serverConfigurationWrapper.save();
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

    private int getResidenceCount() {
        int count = 0;

        try {
            ResultSet rs = residenceCountStatement.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }
}
