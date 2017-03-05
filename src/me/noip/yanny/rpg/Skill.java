package me.noip.yanny.rpg;

import org.bukkit.plugin.Plugin;

import java.util.*;

abstract class Skill {

    final Plugin plugin;
    final Map<UUID, RpgPlayer> rpgPlayerMap;
    final RpgConfiguration rpgConfiguration;
    final Map<AbilityType, Ability> abilities = new LinkedHashMap<>();

    Skill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        this.plugin = plugin;
        this.rpgPlayerMap = rpgPlayerMap;
        this.rpgConfiguration = rpgConfiguration;
    }

    Collection<Ability> getAbilities() {
        return abilities.values();
    }

    abstract void onEnable();

}
