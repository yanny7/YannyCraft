package me.noip.yanny.rpg;

import me.noip.yanny.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

class TreasureHunterAbility extends Ability {

    private Plugin plugin;
    private SkillType skillType;
    private RpgConfiguration rpgConfiguration;
    private Random random = new Random();

    TreasureHunterAbility(Plugin plugin, SkillType skillType, RpgConfiguration rpgConfiguration) {
        super(AbilityType.TREASURE_HUNTER.getDisplayName());

        this.plugin = plugin;
        this.skillType = skillType;
        this.rpgConfiguration = rpgConfiguration;
    }

    @Override
    String toString(RpgPlayer rpgPlayer) {
        return String.format("%2.1f%%", (0.01 + rpgPlayer.getStatsLevel(skillType) / 1000.0 * 0.24) * 100.0);
    }

    @Override
    int fromLevel() {
        return 0;
    }

    Rarity execute(RpgPlayer rpgPlayer, Entity entity) {
        if (random.nextDouble() <= 0.01 + rpgPlayer.getStatsLevel(skillType) / 1000 * 0.24) {
            Rarity[] values = Rarity.values();

            for (int i = values.length - 1; i >= 0; i--) {
                Rarity next = values[i];
                double rand = random.nextDouble();

                if (rand <= next.getProbability()) {
                    List<Material> treasure = rpgConfiguration.getTreasure(next);

                    if (treasure.size() > 0) {
                        Material material = treasure.get(random.nextInt(treasure.size()));
                        Entity newEntity = entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(material));
                        newEntity.setVelocity(Utils.computeThrow(entity.getLocation(), rpgPlayer.getPlayer().getLocation()));

                        switch (next) {
                            case SCRAP:
                            case COMMON:
                            case UNCOMMON:
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> entity.getWorld().playEffect(entity.getLocation(), Effect.SMOKE, 4));
                                break;
                            case RARE:
                            case EXOTIC:
                            case HEROIC:
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> entity.getWorld().playEffect(entity.getLocation(), Effect.MOBSPAWNER_FLAMES, 4));
                                break;
                            case EPIC:
                            case LEGENDARY:
                            case MYTHIC:
                            case GODLIKE:
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> entity.getWorld().playEffect(entity.getLocation(), Effect.DRAGON_BREATH, 4));
                                break;
                        }

                        rpgPlayer.getPlayer().sendMessage(ChatColor.GOLD + rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_TREASURE_FOUND) +
                                ": [" + next.getChatColor() + material.name() + ChatColor.GOLD + "]");
                    }

                    return next;
                }
            }
        }

        return null;
    }

    Rarity execute(RpgPlayer rpgPlayer, Block block) {
        if (random.nextDouble() <= 0.01 + rpgPlayer.getStatsLevel(skillType) / 1000.0 * 0.24) {
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

                        rpgPlayer.getPlayer().sendMessage(ChatColor.GOLD + rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_TREASURE_FOUND) +
                                ": [" + next.getChatColor() + material.name() + ChatColor.GOLD + "]");
                    }

                    return next;
                }
            }
        }

        return null;
    }
}
