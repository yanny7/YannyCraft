package me.noip.yanny.rpg;

import org.bukkit.ChatColor;

import java.util.Random;

import static me.noip.yanny.rpg.RpgTranslation.*;

class DamageReductionAbility extends Ability {

    private SkillType skillType;
    private Random random = new Random();

    DamageReductionAbility(SkillType skillType, String abilityName, int fromLevel) {
        super(abilityName, fromLevel);

        this.skillType = skillType;
    }

    @Override
    String toString(RpgPlayer rpgPlayer) {
        return String.format("%2.1f%%", (0.05 + rpgPlayer.getStatsLevel(skillType) / 1000.0 * 0.95) * 100.0);
    }

    double execute(RpgPlayer rpgPlayer, double damage) {
        int level = rpgPlayer.getStatsLevel(skillType);

        if (level < fromLevel) {
            return damage;
        }

        if (random.nextDouble() <= 0.05 + level / 1000.0 * 0.45) {
            double reduced = level / 1000.0 * 0.5;
            rpgPlayer.getPlayer().sendMessage(DAMAGE_REDUCED.display().replace("{DMG_PERC}", ChatColor.GREEN + String.format("%2.1f%%", reduced * 100) + DAMAGE_REDUCED.getChatColor()));
            return damage - damage * reduced;
        }

        return damage;
    }
}
