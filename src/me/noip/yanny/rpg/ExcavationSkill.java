package me.noip.yanny.rpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
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

    private final Map<AbilityType, Ability> abilities = new HashMap<>();

    ExcavationSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.TREASURE_HUNTER, new TreasureHunterAbility());
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new ExcavationSkillListener(), plugin);
    }

    @Override
    Collection<Ability> getAbilities() {
        return abilities.values();
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

    private class TreasureHunterAbility extends Ability {

        TreasureHunterAbility() {
            super(AbilityType.TREASURE_HUNTER.getDisplayName());
        }

        @Override
        String toString(RpgPlayer rpgPlayer) {
            return String.format("%2.1f%%", (0.01 + rpgPlayer.getStatsLevel(SkillType.EXCAVATION) / 1000.0 * 0.24) * 100.0);
        }

        @Override
        int fromLevel() {
            return 0;
        }

        void execute(RpgPlayer rpgPlayer, Block block) {
            if (random.nextDouble() <= 0.01 + rpgPlayer.getStatsLevel(SkillType.EXCAVATION) / 1000.0 * 0.24) {
                Rarity[] values = Rarity.values();

                for (int i = values.length - 1; i >= 0; i--) {
                    Rarity next = values[i];
                    double rand = random.nextDouble();

                    if (rand <= next.getProbability()) {
                        List<Material> treasure = rpgConfiguration.getTreasure(next);

                        if (treasure.size() > 0) {
                            Material material = treasure.get(random.nextInt(treasure.size()));

                            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(material));

                            switch (next) {
                                case SCRAP:
                                case COMMON:
                                case UNCOMMON:
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4));
                                    break;
                                case RARE:
                                case EXOTIC:
                                case HEROIC:
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 4));
                                    break;
                                case EPIC:
                                case LEGENDARY:
                                case MYTHIC:
                                case GODLIKE:
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> block.getWorld().playEffect(block.getLocation(), Effect.DRAGON_BREATH, 4));
                                    break;
                            }

                            rpgPlayer.getPlayer().sendMessage(ChatColor.GOLD + "Nasiel si poklad: [" + next.getChatColor() + material.name() + ChatColor.GOLD + "]");
                        }

                        return;
                    }
                }
            }
        }
    }
}
