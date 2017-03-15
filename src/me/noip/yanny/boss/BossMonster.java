package me.noip.yanny.boss;

import me.noip.yanny.utils.Rarity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Monster;

import java.util.List;

class BossMonster {
    Monster monster;
    List<ArmorStand> armorStands;
    Rarity rarity;

    BossMonster(Monster monster, List<ArmorStand> armorStands, Rarity rarity) {
        this.monster = monster;
        this.armorStands = armorStands;
        this.rarity = rarity;
    }
}
