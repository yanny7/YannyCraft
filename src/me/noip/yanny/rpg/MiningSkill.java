package me.noip.yanny.rpg;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

class MiningSkill extends Skill {

    private final Map<AbilityType, Ability> abilities = new HashMap<>();

    MiningSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DOUBLE_DROP, new DoubleDropAbility());
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new MiningSkillListener(), plugin);
    }

    @Override
    Collection<Ability> getAbilities() {
        return abilities.values();
    }

    private class MiningSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                plugin.getLogger().warning("RPG.onBlockBreak: Player not found!" + player.getDisplayName());
                return;
            }

            ItemStack handMaterial = player.getInventory().getItemInMainHand();

            switch (handMaterial.getType()) {
                case WOOD_PICKAXE:
                case STONE_PICKAXE:
                case IRON_PICKAXE:
                case GOLD_PICKAXE:
                case DIAMOND_PICKAXE: {
                    Block destMaterial = event.getBlock();
                    int exp = rpgConfiguration.getMiningExp(destMaterial.getType());

                    if (exp > 0) {
                        // double drops only for affected materials
                        ((DoubleDropAbility) abilities.get(AbilityType.DOUBLE_DROP)).execute(rpgPlayer, event.getBlock());

                        rpgPlayer.set(RpgPlayerStatsType.MINING, exp);
                        return;
                    }
                    break;
                }
            }
        }
    }

    private class DoubleDropAbility extends Ability {

        DoubleDropAbility() {
            super(AbilityType.DOUBLE_DROP.getDisplayName());
        }

        @Override
        public String toString(RpgPlayer rpgPlayer) {
            return String.format("%2.1f%%", rpgPlayer.getStatsLevel(RpgPlayerStatsType.MINING) / 10.0); // percentage
        }

        void execute(RpgPlayer rpgPlayer, Block block) {
            if (random.nextDouble() <= rpgPlayer.getStatsLevel(RpgPlayerStatsType.MINING) / 1000.0) {
                Collection<ItemStack> drops = block.getDrops();

                for (ItemStack itemStack : drops) {
                    block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4));
            }
        }
    }
}
