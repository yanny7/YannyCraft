package me.noip.yanny.rpg;

import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;

abstract class Skill {

    Plugin plugin;
    Map<UUID, RpgPlayer> rpgPlayerMap;
    RpgConfiguration rpgConfiguration;

    Skill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        this.plugin = plugin;
        this.rpgPlayerMap = rpgPlayerMap;
        this.rpgConfiguration = rpgConfiguration;
    }

    abstract void onEnable();

}
