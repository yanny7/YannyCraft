package me.noip.yanny.rpg;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

class ExcavationSkill extends Skill {

    ExcavationSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new ExcavationSkillListener(), plugin);
    }

    @Override
    Collection<Ability> getAbilities() {
        return new ArrayList<>();
    }

    private class ExcavationSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            ItemStack handMaterial = player.getInventory().getItemInMainHand();

            switch (handMaterial.getType()) {
                case WOOD_SPADE:
                case STONE_SPADE:
                case IRON_SPADE:
                case GOLD_SPADE:
                case DIAMOND_SPADE: {
                    RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                    if (rpgPlayer == null) {
                        plugin.getLogger().warning("RPG.onBlockBreak: Player not found!" + player.getDisplayName());
                        return;
                    }

                    Block destMaterial = event.getBlock();
                    int exp = rpgConfiguration.getExcavationExp(destMaterial.getType());
                    if (exp > 0) {
                        rpgPlayer.set(RpgPlayerStatsType.EXCAVATION, exp);
                        return;
                    }
                    break;
                }
            }
        }
    }

}
