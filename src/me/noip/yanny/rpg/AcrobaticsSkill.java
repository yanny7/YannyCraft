package me.noip.yanny.rpg;

import me.noip.yanny.MainPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

class AcrobaticsSkill extends Skill {

    AcrobaticsSkill(MainPlugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DAMAGE_REDUCED, new DamageReductionAbility(SkillType.ACROBATICS, "Feather", 0));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new AcrobaticsSkillListener(), plugin);
    }

    private class AcrobaticsSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onMobDamaged(EntityDamageEvent event) {
            if ((event.getEntityType() == EntityType.PLAYER) && (event.getCause() == EntityDamageEvent.DamageCause.FALL)) {
                int exp = rpgConfiguration.getAcrobaticExp(event.getFinalDamage());
                if (exp > 0) {
                    Player player = (Player) event.getEntity();
                    RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                    if (rpgPlayer == null) {
                        logger.logWarn(RPG.class, "AcrobaticSkill.EntityDamageEvent: Player not found!" + player.getDisplayName());
                        return;
                    }

                    event.setDamage(((DamageReductionAbility) abilities.get(AbilityType.DAMAGE_REDUCED)).execute(rpgPlayer, event.getDamage()));
                    rpgPlayer.set(SkillType.ACROBATICS, exp);
                }
            }
        }
    }
}
