package me.noip.yanny.rpg;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;

class UnarmedSkill extends Skill {

    UnarmedSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DOUBLE_DAMAGE, new DoubleDamageAbility(plugin, SkillType.UNARMED, 0, rpgConfiguration));
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
                plugin.getLogger().warning("RPG.onMobDamagedByEntity: Player not found!" + player.getDisplayName());
                return;
            }

            int exp = rpgConfiguration.getDamageExp(event.getEntityType());
            if (exp > 0) {
                ((DoubleDamageAbility) abilities.get(AbilityType.DOUBLE_DAMAGE)).execute(rpgPlayer, event.getEntity(), event.getFinalDamage());

                rpgPlayer.set(SkillType.UNARMED, exp);
            }
        }
    }
}
