package me.noip.yanny.utils;

import org.bukkit.Location;

public class Area {

    public String uuid;
    public Location first;
    public Location second;

    public Area(Location first, Location second, String uuid) {
        this.first = first;
        this.second = second;
        this.uuid = uuid;
    }
}
