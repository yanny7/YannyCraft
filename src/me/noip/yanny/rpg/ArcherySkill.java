package me.noip.yanny.rpg;

import me.noip.yanny.MainPlugin;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

class ArcherySkill extends Skill {

    ArcherySkill(MainPlugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DOUBLE_DAMAGE, new DoubleDamageAbility(SkillType.ARCHERY, "Sharp eye", 0));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new ArcherySkillListener(), plugin);
    }

    private class ArcherySkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onMobDamagedByEntity(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Arrow) || (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE)) {
                return;
            }

            Arrow arrow = (Arrow) event.getDamager();

            if (!(arrow.getShooter() instanceof Player)) {
                return;
            }

            int exp = rpgConfiguration.getDamageExp(event.getEntityType());
            if (exp > 0) {
                Player player = (Player) arrow.getShooter();
                RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                if (rpgPlayer == null) {
                    logger.logWarn(RPG.class, "ArcherySkill.EntityDamageByEntityEvent: Player not found!" + player.getDisplayName());
                    return;
                }

                ((DoubleDamageAbility) abilities.get(AbilityType.DOUBLE_DAMAGE)).execute(rpgPlayer, event.getEntity(), event.getFinalDamage());
                rpgPlayer.set(SkillType.ARCHERY, exp);
            }
        }
    }
}
