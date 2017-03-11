package me.noip.yanny.rpg;

import me.noip.yanny.MainPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.*;

class HerbalismSkill extends Skill {

    HerbalismSkill(MainPlugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DOUBLE_DROP, new DoubleDropAbility(SkillType.HERBALISM, "Farmer", 0));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new HerbalismSkillListener(), plugin);
    }

    private void setExp(Player player, Block block) {

        int exp = rpgConfiguration.getHerbalismExp(block.getType());
        if (exp > 0) {
            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                logger.logWarn(RPG.class, "HerbalismSkill.BlockBreakEvent: Player not found!" + player.getDisplayName());
                return;
            }

            ((DoubleDropAbility) abilities.get(AbilityType.DOUBLE_DROP)).execute(rpgPlayer, block);

            rpgPlayer.set(SkillType.HERBALISM, exp);
        }
    }

    static void loadDefaults(Map<Material, Integer> exp) {
        exp.put(Material.CROPS, 30);
        exp.put(Material.YELLOW_FLOWER, 30);
        exp.put(Material.RED_ROSE, 30);
        exp.put(Material.COCOA, 40);
        exp.put(Material.POTATO, 50);
        exp.put(Material.CARROT, 50);
        exp.put(Material.BROWN_MUSHROOM, 50);
        exp.put(Material.RED_MUSHROOM, 50);
        exp.put(Material.MELON_STEM, 60);
        exp.put(Material.BEETROOT_BLOCK, 60);
        exp.put(Material.PUMPKIN, 70);
        exp.put(Material.NETHER_WARTS, 100);
        exp.put(Material.CHORUS_FLOWER, 150);
    }

    private class HerbalismSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onBlockBreak(BlockBreakEvent event) {
            Block destMaterial = event.getBlock();

            switch (destMaterial.getType()) {
                case MELON_BLOCK:
                case YELLOW_FLOWER:
                case RED_ROSE:
                case BROWN_MUSHROOM:
                case RED_MUSHROOM:
                case PUMPKIN: {
                    setExp(event.getPlayer(), destMaterial);
                    break;
                }
                case POTATO:
                case CARROT:
                case BEETROOT_BLOCK:
                case CROPS:
                case NETHER_WARTS: {
                    if ((destMaterial.getData() == 7)) { // fullyGrown
                        setExp(event.getPlayer(), destMaterial);
                    }
                    break;
                }
                case COCOA: {
                    if (((destMaterial.getData() & 0x8) == 8)) { // fullyGrown
                        setExp(event.getPlayer(), destMaterial);
                    }
                    break;
                }
                case CHORUS_FLOWER: {
                    if ((destMaterial.getData() == 5)) { // fullyGrown
                        setExp(event.getPlayer(), destMaterial);
                    }
                    break;

                }
            }
        }
    }
}
