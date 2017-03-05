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
    private int fromLevel;

    DoubleDamageAbility(Plugin plugin, SkillType skillType, int fromLevel, RpgConfiguration rpgConfiguration) {
        super(AbilityType.DOUBLE_DAMAGE.getDisplayName());

        this.plugin = plugin;
        this.skillType = skillType;
        this.fromLevel = fromLevel;
        this.rpgConfiguration = rpgConfiguration;
    }

    @Override
    String toString(RpgPlayer rpgPlayer) {
        return String.format("%2.1f%%", (0.01 + rpgPlayer.getStatsLevel(skillType) / 1000.0 * 0.29) * 100.0);
    }

    @Override
    int fromLevel() {
        return fromLevel;
    }

    void execute(RpgPlayer rpgPlayer, Entity entity, double damage) {
        int level = rpgPlayer.getStatsLevel(skillType);

        if (level < fromLevel()) {
            return;
        }

        if (!(entity instanceof Damageable)) {
            return;
        }

        if (random.nextDouble() <= (0.01 + rpgPlayer.getStatsLevel(skillType) / 1000.0 * 0.29)) {
            ((Damageable) entity).damage(damage);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> entity.getWorld().playEffect(entity.getLocation(), Effect.SMOKE, 4));
            rpgPlayer.getPlayer().sendMessage(ChatColor.GOLD + rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_CRITICAL_DAMAGE));
        }
    }
}
