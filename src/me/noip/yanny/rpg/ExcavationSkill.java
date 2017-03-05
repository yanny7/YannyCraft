package me.noip.yanny.rpg;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

class ExcavationSkill extends Skill {

    ExcavationSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.TREASURE_HUNTER, new TreasureHunterAbility(plugin, SkillType.EXCAVATION, "Lucky hand", 0, rpgConfiguration));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new ExcavationSkillListener(), plugin);
    }

    static void loadDefaults(Map<Material, Integer> exp) {
        exp.put(Material.SAND, 30);
        exp.put(Material.DIRT, 30);
        exp.put(Material.GRASS, 30);
        exp.put(Material.GRAVEL, 50);
        exp.put(Material.SOUL_SAND, 80);
        exp.put(Material.CLAY, 100);
        exp.put(Material.MYCEL, 200);
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
                        ((TreasureHunterAbility) abilities.get(AbilityType.TREASURE_HUNTER)).execute(rpgPlayer, destMaterial);

                        rpgPlayer.set(SkillType.EXCAVATION, exp);
                        return;
                    }
                    break;
                }
            }
        }
    }
}
