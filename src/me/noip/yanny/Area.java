package me.noip.yanny;

import org.bukkit.Location;
import org.bukkit.entity.Player;

class Area {

    String uuid;
    Location first;
    Location second;

    Area(Location first, Location second, String uuid) {
        this.first = first;
        this.second = second;
        this.uuid = uuid;
    }
}
