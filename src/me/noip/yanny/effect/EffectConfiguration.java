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

class EffectConfiguration {

    private MainPlugin plugin;
    private LoggerHandler logger;

    private PreparedStatement addLightning;
    private PreparedStatement getLightnings;
    private PreparedStatement removeLightning;

    EffectConfiguration(MainPlugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLoggerHandler();

        Connection connection = plugin.getConnection();

        try {
            addLightning = connection.prepareStatement("INSERT INTO lightning (Location, World, Delay, Distance, Speed) VALUES (?, ?, ?, ?, ?)");
            getLightnings = connection.prepareStatement("SELECT Location, World, Delay, Distance, Speed FROM lightning");
            removeLightning = connection.prepareStatement("DELETE FROM lightning WHERE Location = ?");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addLightning(LightningInfo lightningInfo) {
        try {
            addLightning.setString(1, Utils.locationToString(lightningInfo.location));
            addLightning.setString(2, lightningInfo.world.getUID().toString());
            addLightning.setInt(3, lightningInfo.delay);
            addLightning.setDouble(4, lightningInfo.distance);
            addLightning.setDouble(5, lightningInfo.speed);
            addLightning.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Set<LightningInfo> getLightnings() {
        Set<LightningInfo> lightnings = new HashSet<>();

        try {
            ResultSet rs = getLightnings.executeQuery();

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
            removeLightning.setString(1, Utils.locationToString(location));
            removeLightning.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
