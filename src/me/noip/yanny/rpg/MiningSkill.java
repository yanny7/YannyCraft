package me.noip.yanny.rpg;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

class MiningSkill extends Skill {

    MiningSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DOUBLE_DROP, new DoubleDropAbility(plugin, SkillType.MINING, "Double drop", 0));
        abilities.put(AbilityType.DOUBLE_DAMAGE, new DoubleDamageAbility(plugin, SkillType.MINING, "Protector", 100));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new MiningSkillListener(), plugin);
    }

    static void loadDefaults(Map<Material, Integer> exp) {
        exp.put(Material.SANDSTONE, 20);
        exp.put(Material.NETHERRACK, 20);
        exp.put(Material.STONE, 30);
        exp.put(Material.RED_SANDSTONE, 30);
        exp.put(Material.PRISMARINE, 30);
        exp.put(Material.HARD_CLAY, 40);
        exp.put(Material.STAINED_CLAY, 40);
        exp.put(Material.ENDER_STONE, 50);
        exp.put(Material.MOSSY_COBBLESTONE, 60);
        exp.put(Material.OBSIDIAN, 80);
        exp.put(Material.GLOWSTONE, 80);
        exp.put(Material.QUARTZ_ORE, 100);
        exp.put(Material.PURPUR_BLOCK, 100);
        exp.put(Material.COAL_ORE, 100);
        exp.put(Material.REDSTONE_ORE, 150);
        exp.put(Material.IRON_ORE, 200);
        exp.put(Material.LAPIS_ORE, 300);
        exp.put(Material.GOLD_ORE, 400);
        exp.put(Material.DIAMOND_ORE, 500);
        exp.put(Material.EMERALD_ORE, 1000);
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
                        ((DoubleDropAbility) abilities.get(AbilityType.DOUBLE_DROP)).execute(rpgPlayer, event.getBlock());

                        rpgPlayer.set(SkillType.MINING, exp);
                        return;
                    }
                    break;
                }
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onMobDamagedByEntity(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player) || (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                return;
            }

            Player player = (Player) event.getDamager();

            switch (player.getInventory().getItemInMainHand().getType()) {
                case WOOD_PICKAXE:
                case STONE_PICKAXE:
                case IRON_PICKAXE:
                case GOLD_PICKAXE:
                case DIAMOND_PICKAXE: {
                    RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                    if (rpgPlayer == null) {
                        plugin.getLogger().warning("RPG.onBlockBreak: Player not found!" + player.getDisplayName());
                        return;
                    }

                    ((DoubleDamageAbility) abilities.get(AbilityType.DOUBLE_DAMAGE)).execute(rpgPlayer, event.getEntity(), event.getFinalDamage());
                }
            }
        }
    }
}
