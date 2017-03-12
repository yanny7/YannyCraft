package me.noip.yanny.effect;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.LoggerHandler;
import me.noip.yanny.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class LightningConfiguration {

    private MainPlugin plugin;
    private LoggerHandler logger;

    private PreparedStatement addLightningStatement;
    private PreparedStatement getLightningsStatement;
    private PreparedStatement removeLightningStatement;
    private PreparedStatement lightningCountStatement;

    LightningConfiguration(MainPlugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLoggerHandler();

        Connection connection = plugin.getConnection();

        try {
            addLightningStatement = connection.prepareStatement("INSERT INTO lightning (Location, World, Delay, Distance, Speed) VALUES (?, ?, ?, ?, ?)");
            getLightningsStatement = connection.prepareStatement("SELECT Location, World, Delay, Distance, Speed FROM lightning");
            removeLightningStatement = connection.prepareStatement("DELETE FROM lightning WHERE Location = ?");
            lightningCountStatement = connection.prepareStatement("SELECT COUNT(*) FROM lightning");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void load() {
        logger.logInfo(Lightning.class, String.format("Lightning locations: %d", getLightningCount()));
    }

    void addLightning(LightningInfo lightningInfo) {
        try {
            addLightningStatement.setString(1, Utils.locationToString(lightningInfo.location));
            addLightningStatement.setString(2, lightningInfo.world.getUID().toString());
            addLightningStatement.setInt(3, lightningInfo.delay);
            addLightningStatement.setDouble(4, lightningInfo.distance);
            addLightningStatement.setDouble(5, lightningInfo.speed);
            addLightningStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Set<LightningInfo> getLightnings() {
        Set<LightningInfo> lightnings = new HashSet<>();

        try {
            ResultSet rs = getLightningsStatement.executeQuery();

            while (rs.next()) {
                Location location;
                World world;
                int delay;
                float distance;
                float speed;

                location = Utils.parseLocation(rs.getString(1), plugin);
                world = plugin.getServer().getWorld(UUID.fromString(rs.getString(2)));
                delay = rs.getInt(3);
                distance = (float) rs.getDouble(4);
                speed = (float) rs.getDouble(5);

                if ((location == null) || (world == null)) {
                    logger.logWarn(Lightning.class, "Cant load lightning info: location: " + location + " world: " + world);
                    continue;
                }

                lightnings.add(new LightningInfo(location, world, delay, distance, speed));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lightnings;
    }

    void removeLightning(Location location) {
        try {
            removeLightningStatement.setString(1, Utils.locationToString(location));
            removeLightningStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getLightningCount() {
        int count = 0;

        try {
            ResultSet rs = lightningCountStatement.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }
}
