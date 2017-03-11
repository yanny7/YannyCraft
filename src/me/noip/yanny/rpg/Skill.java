package me.noip.yanny.rpg;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.LoggerHandler;

import java.util.*;

abstract class Skill {

    final MainPlugin plugin;
    final LoggerHandler logger;
    final Map<UUID, RpgPlayer> rpgPlayerMap;
    final RpgConfiguration rpgConfiguration;
    final Map<AbilityType, Ability> abilities = new LinkedHashMap<>();

    Skill(MainPlugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        this.plugin = plugin;
        this.rpgPlayerMap = rpgPlayerMap;
        this.rpgConfiguration = rpgConfiguration;
        logger = plugin.getLoggerHandler();
    }

    Collection<Ability> getAbilities() {
        return abilities.values();
    }

    abstract void onEnable();

}
