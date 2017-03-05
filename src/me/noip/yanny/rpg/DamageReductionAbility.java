package me.noip.yanny.rpg;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.Random;

class DamageReductionAbility extends Ability {

    private Plugin plugin;
    private SkillType skillType;
    private RpgConfiguration rpgConfiguration;
    private Random random = new Random();

    DamageReductionAbility(Plugin plugin, SkillType skillType, String abilityName, int fromLevel, RpgConfiguration rpgConfiguration) {
        super(abilityName, fromLevel);

        this.plugin = plugin;
        this.skillType = skillType;
        this.rpgConfiguration = rpgConfiguration;
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
            rpgPlayer.getPlayer().sendMessage(ChatColor.GOLD + rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_DAMAGE_REDUCED) +
                    " " + ChatColor.GREEN + String.format("%2.1f%%", reduced * 100));
            return damage - damage * reduced;
        }

        return damage;
    }
}
