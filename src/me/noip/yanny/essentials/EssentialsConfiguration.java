package me.noip.yanny.essentials;

import me.noip.yanny.utils.ServerConfigurationWrapper;
import me.noip.yanny.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

class EssentialsConfiguration {

    private static final String CONFIGURATION_NAME = "essentials";

    private static final String TRANSLATION_SECTION = "translation";
    private static final String SPAWN_SECTION = "spawn";
    private static final String CHAT_SECTION = "chat";

    private static final String CHAT_NORMAL = "normal";
    private static final String CHAT_OP = "op";

    private PreparedStatement setHomeStatement;
    private PreparedStatement getHomeStatement;
    private PreparedStatement setBackStatement;
    private PreparedStatement getBackStatement;

    private Plugin plugin;
    private ServerConfigurationWrapper serverConfigurationWrapper;
    private Map<String, String> translationMap = new HashMap<>();
    private Location spawnLocation = null;
    private String normalChatFormat = "<&a{PLAYER}&r> {MSG}";
    private String opChatFormat = "<&4{PLAYER}&r> {MSG}";

    EssentialsConfiguration(Plugin plugin, Connection connection) {
        this.plugin = plugin;

        try {
            setHomeStatement = connection.prepareStatement("UPDATE users SET HomeLocation = ? WHERE ID = ?");
            getHomeStatement = connection.prepareStatement("SELECT HomeLocation FROM users WHERE ID = ?");
            setBackStatement = connection.prepareStatement("UPDATE users SET BackLocation = ? WHERE ID = ?");
            getBackStatement = connection.prepareStatement("SELECT BackLocation FROM users WHERE ID = ?");
        } catch (Exception e) {
            e.printStackTrace();
        }

        translationMap.put("msg_spawn_set", "Nova spawn lokacia bola nastavena");
        translationMap.put("msg_tpa_sended", "Poziadavka na teleport bola odoslana hracovi");
        translationMap.put("msg_tpa_received", "Hrac {player} sa chce k tebe teleportovat /tpaccept prijmi, /tpdeny zamietni");
        translationMap.put("msg_tpdeny", "Poziadavka na teleport bola zamietnuta");
        translationMap.put("msg_teleported", "Bol si teleportovany k hracovi {player}");
        translationMap.put("msg_home_created", "Domov bol nastaveny");
        translationMap.put("msg_home", "Bol si teleportovany domov");
        translationMap.put("msg_back", "Bol si teleportovany na poslednu poziciu");

        translationMap.put("msg_err_invalid_user", "Hrac {player} neexistuje");
        translationMap.put("msg_err_permission", "Na tento prikaz nemas prava");

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection spawnSection = serverConfigurationWrapper.getConfigurationSection(SPAWN_SECTION);
        if (spawnSection == null) {
            serverConfigurationWrapper.createSection(SPAWN_SECTION);
        }

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        translationMap.putAll(ServerConfigurationWrapper.convertMapString(translationSection.getValues(false)));

        // try to load spawn location, after startup this fails, after reload succeed
        String spawnWorld = getSpawnWorld();
        if (spawnWorld != null) {
            for (World world : plugin.getServer().getWorlds()) {
                if (spawnWorld.equals(world.getName())) {
                    loadSpawnLocation(spawnWorld);
                }
            }
        }

        ConfigurationSection chatSection = serverConfigurationWrapper.getConfigurationSection(CHAT_SECTION);
        if (chatSection == null) {
            chatSection = serverConfigurationWrapper.createSection(CHAT_SECTION);
        }
        normalChatFormat = chatSection.getString(CHAT_NORMAL, normalChatFormat);
        opChatFormat = chatSection.getString(CHAT_OP, opChatFormat);

        save(); // save defaults
    }

    private void save() {
        if (spawnLocation != null) {
            ConfigurationSection spawnSection = serverConfigurationWrapper.getConfigurationSection(SPAWN_SECTION);
            spawnSection.set("world", spawnLocation.getWorld().getName());
            spawnSection.set("x", spawnLocation.getX());
            spawnSection.set("y", spawnLocation.getY());
            spawnSection.set("z", spawnLocation.getZ());
            spawnSection.set("yaw", spawnLocation.getYaw());
            spawnSection.set("pitch", spawnLocation.getPitch());
        }


        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        for (HashMap.Entry<String, String> pair : translationMap.entrySet()) {
            translationSection.set(pair.getKey(), pair.getValue());
        }

        ConfigurationSection chatSection = serverConfigurationWrapper.getConfigurationSection(CHAT_SECTION);
        chatSection.set(CHAT_NORMAL, normalChatFormat);
        chatSection.set(CHAT_OP, opChatFormat);

        serverConfigurationWrapper.save();
    }

    String getSpawnWorld() {
        ConfigurationSection spawnSection = serverConfigurationWrapper.getConfigurationSection(SPAWN_SECTION);
        return spawnSection.getString("world");
    }

    void loadSpawnLocation(String worldName) {
        ConfigurationSection spawnSection = serverConfigurationWrapper.getConfigurationSection(SPAWN_SECTION);
        double x = spawnSection.getDouble("x", 0);
        double y = spawnSection.getDouble("y", 100);
        double z = spawnSection.getDouble("z", 0);
        double yaw = spawnSection.getDouble("yaw", 0);
        double pitch = spawnSection.getDouble("pitch", 0);

        spawnLocation = new Location(plugin.getServer().getWorld(worldName), x, y, z, (float)yaw, (float)pitch);
    }

    void setSpawnLocation(Location location) {
        spawnLocation = location;

        save();
    }

    Location getSpawnLocation() {
        if (spawnLocation == null) {
            if (plugin.getServer().getWorlds().size() > 0) {
                World world = plugin.getServer().getWorlds().get(0);
                return world.getSpawnLocation();
            }
        }

        return spawnLocation;
    }

    String getChatNormal() {
        return normalChatFormat;
    }

    String getChatOp() {
        return opChatFormat;
    }

    String getTranslation(String key) {
        return translationMap.get(key);
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
