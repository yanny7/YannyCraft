package me.noip.yanny.rpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Random;

class DoubleDamageAbility extends Ability {

    private Plugin plugin;
    private SkillType skillType;
    private RpgConfiguration rpgConfiguration;
    private Random random = new Random();

    DoubleDamageAbility(Plugin plugin, SkillType skillType, String abilityName, int fromLevel, RpgConfiguration rpgConfiguration) {
        super(abilityName, fromLevel);

        this.plugin = plugin;
        this.skillType = skillType;
        this.rpgConfiguration = rpgConfiguration;
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

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> entity.getWorld().playEffect(entity.getLocation(), Effect.SMOKE, 4));
            rpgPlayer.getPlayer().sendMessage(ChatColor.GOLD + rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_CRITICAL_DAMAGE) +
                    ": " + ChatColor.GREEN + Integer.toString(multiplier) + "x" + ChatColor.GOLD + " dmg");
        }
    }
}
