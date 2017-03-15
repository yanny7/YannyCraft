package me.noip.yanny.boss;

import org.bukkit.Material;

class BossStats {
    int health;
    Material helmet;
    Material chestplate;
    Material leggings;
    Material boots;
    Material sword;

    BossStats(int health, Material helmet, Material chestplate, Material leggings, Material boots, Material sword) {
        this.health = health;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.sword = sword;
    }
}
