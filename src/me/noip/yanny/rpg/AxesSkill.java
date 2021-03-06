package me.noip.yanny.rpg;

import me.noip.yanny.MainPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

class AxesSkill extends Skill {

    AxesSkill(MainPlugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DOUBLE_DAMAGE, new DoubleDamageAbility(SkillType.AXES, "Berserk", 0));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new AxesSkillListener(), plugin);
    }

    private class AxesSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onMobDamagedByEntity(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player) || (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                return;
            }

            Player player = (Player) event.getDamager();

            switch (player.getInventory().getItemInMainHand().getType()) {
                case DIAMOND_AXE:
                case GOLD_AXE:
                case IRON_AXE:
                case STONE_AXE:
                case WOOD_AXE: {
                    int exp = rpgConfiguration.getDamageExp(event.getEntityType());

                    if (exp > 0) {
                        RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                        if (rpgPlayer == null) {
                            logger.logWarn(RPG.class, "AxesSkill.EntityDamageByEntityEvent: Player not found!" + player.getDisplayName());
                            return;
                        }

                        ((DoubleDamageAbility) abilities.get(AbilityType.DOUBLE_DAMAGE)).execute(rpgPlayer, event.getEntity(), event.getFinalDamage());
                        rpgPlayer.set(SkillType.AXES, exp);
                    }
                    break;
                }
            }

        }
    }
}
