package me.noip.yanny.rpg;

import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

abstract class Skill {

    Plugin plugin;
    Map<UUID, RpgPlayer> rpgPlayerMap;
    RpgConfiguration rpgConfiguration;
    Random random;

    Skill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        this.plugin = plugin;
        this.rpgPlayerMap = rpgPlayerMap;
        this.rpgConfiguration = rpgConfiguration;
        random = new Random();
    }

    abstract void onEnable();

    abstract Collection<Ability> getAbilities();

}
