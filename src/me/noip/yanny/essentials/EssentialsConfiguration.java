package me.noip.yanny.essentials;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.LoggerHandler;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import me.noip.yanny.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

class EssentialsConfiguration {

    private static final String CONFIGURATION_NAME = "essentials.yml";

    private static final String TRANSLATION_SECTION = "translation";
    private static final String SPAWN_SECTION = "spawn";
    private static final String CHAT_SECTION = "chat";

    private static final String CHAT_NORMAL = "normal";
    private static final String CHAT_OP = "op";

    private PreparedStatement setHomeStatement;
    private PreparedStatement getHomeStatement;
    private PreparedStatement setBackStatement;
    private PreparedStatement getBackStatement;

    private MainPlugin plugin;
    private Server server;
    private LoggerHandler logger;
    private ServerConfigurationWrapper serverConfigurationWrapper;
    private Location spawnLocation = null;
    private String normalChatFormat = "<&a{PLAYER}&r> {MSG}";
    private String opChatFormat = "<&4{PLAYER}&r> {MSG}";

    EssentialsConfiguration(MainPlugin plugin) {
        this.plugin = plugin;
        server = plugin.getServer();
        logger = plugin.getLoggerHandler();

        Connection connection = plugin.getConnection();

        try {
            setHomeStatement = connection.prepareStatement("UPDATE users SET HomeLocation = ? WHERE ID = ?");
            getHomeStatement = connection.prepareStatement("SELECT HomeLocation FROM users WHERE ID = ?");
            setBackStatement = connection.prepareStatement("UPDATE users SET BackLocation = ? WHERE ID = ?");
            getBackStatement = connection.prepareStatement("SELECT BackLocation FROM users WHERE ID = ?");
        } catch (Exception e) {
            e.printStackTrace();
        }

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection spawnSection = serverConfigurationWrapper.getConfigurationSection(SPAWN_SECTION);
        if (spawnSection != null) {
            String worldName = spawnSection.getString("world");

            World world = server.getWorld(worldName);
            if (world == null) {
                if ((server.getWorlds() == null) || (server.getWorlds().isEmpty())) {
                    logger.logWarn(Essentials.class, "No worlds created");
                    return;
                }

                world = server.getWorlds().get(0);
                logger.logWarn(Essentials.class, "World '" + worldName + "' does not exists, using found world '" + world.getName() + "'");
            }

            Location defaultSpawn = world.getSpawnLocation();
            double x = spawnSection.getDouble("x", defaultSpawn.getX());
            double y = spawnSection.getDouble("y", defaultSpawn.getY());
            double z = spawnSection.getDouble("z", defaultSpawn.getZ());
            double yaw = spawnSection.getDouble("yaw", defaultSpawn.getYaw());
            double pitch = spawnSection.getDouble("pitch", defaultSpawn.getPitch());

            spawnLocation = new Location(world, x, y, z, (float)yaw, (float)pitch);
        } else {
            if ((server.getWorlds() == null) || (server.getWorlds().isEmpty())) {
                logger.logWarn(Essentials.class, "No worlds created");
                return;
            }

            spawnLocation = server.getWorlds().get(0).getSpawnLocation();
        }

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection != null) {
            for (EssentialsTranslation translation : EssentialsTranslation.values()) {
                translation.setDisplayName(translationSection.getString(translation.name(), translation.getDisplayName()));
            }
        }

        ConfigurationSection chatSection = serverConfigurationWrapper.getConfigurationSection(CHAT_SECTION);
        if (chatSection != null) {
            normalChatFormat = chatSection.getString(CHAT_NORMAL, normalChatFormat);
            opChatFormat = chatSection.getString(CHAT_OP, opChatFormat);
        }

        logger.logInfo(Essentials.class, String.format("Spawn location: %s [%d %d %d]", spawnLocation.getWorld().getName(), spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ()));

        save(); // save defaults
    }

    private void save() {
        if ((server.getWorlds() == null) || (server.getWorlds().isEmpty())) {
            logger.logWarn(Essentials.class, "No worlds created");
            return;
        }

        ConfigurationSection spawnSection = serverConfigurationWrapper.createSection(SPAWN_SECTION);
        spawnSection.set("world", spawnLocation.getWorld().getName());
        spawnSection.set("x", spawnLocation.getX());
        spawnSection.set("y", spawnLocation.getY());
        spawnSection.set("z", spawnLocation.getZ());
        spawnSection.set("yaw", spawnLocation.getYaw());
        spawnSection.set("pitch", spawnLocation.getPitch());

        ConfigurationSection translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        for (EssentialsTranslation translation : EssentialsTranslation.values()) {
            translationSection.set(translation.name(), translation.getDisplayName());
        }

        ConfigurationSection chatSection = serverConfigurationWrapper.createSection(CHAT_SECTION);
        chatSection.set(CHAT_NORMAL, normalChatFormat);
        chatSection.set(CHAT_OP, opChatFormat);

        serverConfigurationWrapper.save();
    }

    void setSpawnLocation(Location location) {
        spawnLocation = location;
        save();
    }

    Location getSpawnLocation() {
        return spawnLocation;
    }

    String getChatNormal() {
        return normalChatFormat;
    }

    String getChatOp() {
        return opChatFormat;
    }

    Location getHomeLocation(Player player) {
        Location location = null;

        try {
            getHomeStatement.setString(1, player.getUniqueId().toString());
            ResultSet rs = getHomeStatement.executeQuery();

            if (rs.next()) {
                location = Utils.parseLocation(rs.getString(1), plugin);
            }

            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    Location getBackLocation(Player player) {
        Location location = null;

        try {
            getBackStatement.setString(1, player.getUniqueId().toString());
            ResultSet rs = getBackStatement.executeQuery();

            if (rs.next()) {
                location = Utils.parseLocation(rs.getString(1), plugin);
            }

            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    void setHomeLocation(Player player, Location location) {
        try {
            String loc = Utils.locationToString(location);
            setHomeStatement.setString(1, loc);
            setHomeStatement.setString(2, player.getUniqueId().toString());
            setHomeStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setBackLocation(Player player, Location location) {
        try {
            String loc = Utils.locationToString(location);
            setBackStatement.setString(1, loc);
            setBackStatement.setString(2, player.getUniqueId().toString());
            setBackStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
