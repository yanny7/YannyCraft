package me.noip.yanny;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

class ChestConfiguration {

    private static final String CONFIGURATION_NAME = "chests";

    private static final String LOCATIONS_SECTION = "locations";

    private Plugin plugin;
    private ServerConfigurationWrapper serverConfigurationWrapper;
    private List<String> chestsLocation = new ArrayList<>();

    ChestConfiguration(Plugin plugin) {
        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        chestsLocation = serverConfigurationWrapper.getStringList(LOCATIONS_SECTION);

        save();
    }

    void save() {
        serverConfigurationWrapper.set(LOCATIONS_SECTION, chestsLocation);
        serverConfigurationWrapper.save();
    }

    List<String> getChestsLocation() {
        return chestsLocation;
    }

    static String locationToString(Location location) {
        return location.getX() + " " + location.getY() + " " + location.getZ() + " " + location.getWorld().getName();
    }

    static Location parseLocation(String location, Plugin plugin) {
        String[] tokens = location.split(" ");

        if (tokens.length != 4) {
            plugin.getLogger().warning("ChestConfiguration.parseLocation: Invalid location data: " + location);
            return null;
        }

        World world = plugin.getServer().getWorld(tokens[3]);
        double x, y, z;

        try {
            x = Double.parseDouble(tokens[0]);
            y = Double.parseDouble(tokens[1]);
            z = Double.parseDouble(tokens[2]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

        if (world == null) {
            plugin.getLogger().warning("ChestConfiguration.parseLocation: Invalid world name: " + tokens[3]);
            return null;
        }

        return new Location(world, x, y, z);
    }
}
