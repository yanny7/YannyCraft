package me.noip.yanny.rpg;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

class HerbalismSkill extends Skill {

    HerbalismSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new HerbalismSkillListener(), plugin);
    }

    @Override
    Collection<Ability> getAbilities() {
        return new ArrayList<>();
    }

    private void setExp(Player player, Material material) {
        RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

        if (rpgPlayer == null) {
            plugin.getLogger().warning("RPG.onBlockBreak: Player not found!" + player.getDisplayName());
            return;
        }

        int exp = rpgConfiguration.getHerbalismExp(material);
        if (exp > 0) {
            rpgPlayer.set(RpgPlayerStatsType.HERBALISM, exp);
        }
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
                    setExp(event.getPlayer(), destMaterial.getType());
                    break;
                }
                case POTATO:
                case CARROT:
                case BEETROOT_BLOCK:
                case CROPS:
                case NETHER_WARTS: {
                    if ((destMaterial.getData() == 7)) { // fullyGrown
                        setExp(event.getPlayer(), destMaterial.getType());
                    }
                    break;
                }
                case COCOA: {
                    if (((destMaterial.getData() & 0x8) == 8)) { // fullyGrown
                        setExp(event.getPlayer(), destMaterial.getType());
                    }
                    break;
                }
                case CHORUS_FLOWER: {
                    if ((destMaterial.getData() == 5)) { // fullyGrown
                        setExp(event.getPlayer(), destMaterial.getType());
                    }
                    break;

                }
            }
        }
    }
}
