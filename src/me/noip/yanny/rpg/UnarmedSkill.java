package me.noip.yanny.rpg;

import me.noip.yanny.MainPlugin;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.UUID;

class UnarmedSkill extends Skill {

    UnarmedSkill(MainPlugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DOUBLE_DAMAGE, new DoubleDamageAbility(SkillType.UNARMED, "Iron hand", 0));
        abilities.put(AbilityType.DAMAGE_REDUCED, new DamageReductionAbility(SkillType.UNARMED, "Stone skin", 100));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new UnarmedSkillListener(), plugin);
    }

    private class UnarmedSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onMobDamagedByEntity(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player) || (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                return;
            }

            Player player = (Player) event.getDamager();

            if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                return;
            }

            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                logger.logWarn(RPG.class, "UnarmedSkill.EntityDamageByEntityEvent: Player not found!" + player.getDisplayName());
                return;
            }

            int exp = rpgConfiguration.getDamageExp(event.getEntityType());
            if (exp > 0) {
                ((DoubleDamageAbility) abilities.get(AbilityType.DOUBLE_DAMAGE)).execute(rpgPlayer, event.getEntity(), event.getFinalDamage());

                rpgPlayer.set(SkillType.UNARMED, exp);
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onMobDamaged(EntityDamageEvent event) {
            if ((event.getEntityType() == EntityType.PLAYER) && (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                Player player = (Player) event.getEntity();
                RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                if (rpgPlayer == null) {
                    logger.logWarn(RPG.class, "UnarmedSkill.EntityDamageEvent: Player not found!" + player.getDisplayName());
                    return;
                }

                event.setDamage(((DamageReductionAbility) abilities.get(AbilityType.DAMAGE_REDUCED)).execute(rpgPlayer, event.getDamage()));
            }
        }
    }
}
