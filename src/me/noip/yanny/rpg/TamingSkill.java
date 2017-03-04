package me.noip.yanny.rpg;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

class TamingSkill extends Skill {

    TamingSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new TamingSkillListener(), plugin);
    }

    static void loadDefaults(Map<EntityType, Integer> exp) {
        exp.put(EntityType.WOLF, 100);
        exp.put(EntityType.HORSE, 100);
        exp.put(EntityType.DONKEY, 200);
        exp.put(EntityType.LLAMA, 250);
        exp.put(EntityType.MULE, 300);
        exp.put(EntityType.OCELOT, 500);
    }

    private class TamingSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onEntityTame(EntityTameEvent event) {
            if (!(event.getOwner() instanceof Player)) {
                return;
            }

            Player player = (Player) event.getOwner();
            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                plugin.getLogger().warning("RPG.onEntityTame: Player not found!" + player.getDisplayName());
                return;
            }

            int exp = rpgConfiguration.getTameExp(event.getEntityType());
            if (exp > 0) {
                rpgPlayer.set(SkillType.TAMING, exp);
            }
        }
    }
}
