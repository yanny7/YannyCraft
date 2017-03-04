package me.noip.yanny.rpg;

import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

abstract class Skill {

    final Plugin plugin;
    final Map<UUID, RpgPlayer> rpgPlayerMap;
    final RpgConfiguration rpgConfiguration;
    final Map<AbilityType, Ability> abilities = new HashMap<>();

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
