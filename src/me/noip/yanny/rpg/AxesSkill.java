package me.noip.yanny.rpg;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

class AxesSkill extends Skill {
    AxesSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new AxesSkillListener(), plugin);
    }

    @Override
    Collection<Ability> getAbilities() {
        return new ArrayList<>();
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
                    RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                    if (rpgPlayer == null) {
                        plugin.getLogger().warning("RPG.onMobDamagedByEntity: Player not found!" + player.getDisplayName());
                        return;
                    }

                    int exp = rpgConfiguration.getDamageExp(event.getEntityType());
                    if (exp > 0) {
                        rpgPlayer.set(SkillType.SWORDS, exp);
                    }
                    break;
                }
            }

        }
    }
}
