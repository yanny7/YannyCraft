package me.noip.yanny.effect;

import org.bukkit.Location;
import org.bukkit.World;

class LightningInfo {
    final Location location;
    final World world;
    final int delay;
    final float distance;
    final float speed;
    int id;

    LightningInfo(Location location, World world, int delay, float distance, float speed) {
        this.location = location;
        this.world = world;
        this.delay = delay;
        this.distance = distance;
        this.speed = speed;
    }
}
