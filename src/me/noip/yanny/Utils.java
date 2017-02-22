package me.noip.yanny;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

class Utils {

    static String locationToString(Location location) {
        return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + " " + location.getWorld().getName();
    }

    static Location parseLocation(String location, Plugin plugin) {
        String[] tokens = location.split(" ");

        if (tokens.length != 4) {
            plugin.getLogger().warning("ResidenceConfiguration.parseLocation: Invalid location data: " + location);
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
            plugin.getLogger().warning("ResidenceConfiguration.parseLocation: Invalid world name: " + tokens[3]);
            return null;
        }

        return new Location(world, x, y, z);
    }

}
