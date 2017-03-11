package me.noip.yanny.rpg;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;

import java.util.Random;

import static me.noip.yanny.rpg.RpgTranslation.*;

class DoubleDamageAbility extends Ability {

    private SkillType skillType;
    private Random random = new Random();

    DoubleDamageAbility(SkillType skillType, String abilityName, int fromLevel) {
        super(abilityName, fromLevel);

        this.skillType = skillType;
    }

    @Override
    String toString(RpgPlayer rpgPlayer) {
        return String.format("%2.1f%%", (0.01 + rpgPlayer.getStatsLevel(skillType) / 1000.0 * 0.29) * 100.0);
    }

    void execute(RpgPlayer rpgPlayer, Entity entity, double damage) {
        int level = rpgPlayer.getStatsLevel(skillType);

        if (level < fromLevel) {
            return;
        }

        if (!(entity instanceof Damageable)) {
            return;
        }

        if (random.nextDouble() <= (0.01 + rpgPlayer.getStatsLevel(skillType) / 1000.0 * 0.29)) {
            int multiplier = 2;
            if (level >= 750) {
                multiplier = 5;
            } else if (level >= 500) {
                multiplier = 4;
            } else if (level >= 250) {
                multiplier = 3;
            }

            ((Damageable) entity).damage(damage * (multiplier - 1));

            entity.getWorld().playEffect(entity.getLocation(), Effect.SMOKE, 4);
            rpgPlayer.getPlayer().sendMessage(CRITICAL_DAMAGE.display().replace("{DMG_MULT}", ChatColor.GREEN + Integer.toString(multiplier) + "x" + ChatColor.GOLD));
        }
    }
}
