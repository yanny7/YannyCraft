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

class WoodcuttingSkill extends Skill {

    WoodcuttingSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DOUBLE_DROP, new DoubleDropAbility(plugin, SkillType.WOODCUTTING, "Double wood", 0));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new WoodcuttingSkillListener(), plugin);
    }

    static void loadDefaults(Map<Material, Integer> exp) {
        exp.put(Material.LOG, 30);
    }

    private class WoodcuttingSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            ItemStack handMaterial = player.getInventory().getItemInMainHand();

            switch (handMaterial.getType()) {
                case WOOD_AXE:
                case STONE_AXE:
                case IRON_AXE:
                case GOLD_AXE:
                case DIAMOND_AXE: {
                    RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                    if (rpgPlayer == null) {
                        plugin.getLogger().warning("RPG.onBlockBreak: Player not found!" + player.getDisplayName());
                        return;
                    }

                    Block destMaterial = event.getBlock();
                    int exp = rpgConfiguration.getWoodcuttingExp(destMaterial.getType());
                    if (exp > 0) {
                        ((DoubleDropAbility) abilities.get(AbilityType.DOUBLE_DROP)).execute(rpgPlayer, event.getBlock());

                        rpgPlayer.set(SkillType.WOODCUTTING, exp);
                        return;
                    }
                    break;
                }
            }
        }
    }
}
