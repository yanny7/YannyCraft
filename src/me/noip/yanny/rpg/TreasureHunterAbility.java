package me.noip.yanny.rpg;

import me.noip.yanny.utils.ItemInfo;
import me.noip.yanny.utils.Items;
import me.noip.yanny.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;

class TreasureHunterAbility extends Ability {

    private Plugin plugin;
    private SkillType skillType;
    private RpgConfiguration rpgConfiguration;
    private Random random = new Random();
    private List<ItemInfo> itemInfos = Items.getItemList();

    TreasureHunterAbility(Plugin plugin, SkillType skillType, String abilityName, int fromLevel, RpgConfiguration rpgConfiguration) {
        super(abilityName, fromLevel);

        this.plugin = plugin;
        this.skillType = skillType;
        this.rpgConfiguration = rpgConfiguration;
    }

    @Override
    String toString(RpgPlayer rpgPlayer) {
        return String.format("%2.1f%%", (0.01 + rpgPlayer.getStatsLevel(skillType) / 1000.0 * 0.24) * 100.0);
    }

    Rarity execute(RpgPlayer rpgPlayer, Entity entity) {
        int level = rpgPlayer.getStatsLevel(skillType);

        if (level < fromLevel) {
            return null;
        }

        if (random.nextDouble() <= 0.01 + rpgPlayer.getStatsLevel(skillType) / 1000 * 0.24) {
            Rarity[] values = Rarity.values();

            for (int i = values.length - 1; i >= 0; i--) {
                Rarity next = values[i];
                double rand = random.nextDouble();

                if (rand <= next.getProbability()) {
                    List<ItemStack> treasure = rpgConfiguration.getTreasure(next);

                    if (treasure.size() > 0) {
                        ItemStack itemStack = treasure.get(random.nextInt(treasure.size()));
                        Entity newEntity = entity.getWorld().dropItemNaturally(entity.getLocation(), itemStack);
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
                                ": [" + next.getChatColor() + itemStack.getType().name() + ChatColor.GOLD + "]");
                    }

                    return next;
                }
            }
        }

        return null;
    }

    Rarity execute(RpgPlayer rpgPlayer, Block block) {
        int level = rpgPlayer.getStatsLevel(skillType);

        if (level < fromLevel) {
            return null;
        }

        if (random.nextDouble() <= 0.01 + level / 1000.0 * 0.24) {
            Rarity[] values = Rarity.values();

            for (int i = values.length - 1; i >= 0; i--) {
                Rarity next = values[i];
                double probability = Utils.sumProbabilities(next.getProbability(), level / 1000.0 * 0.005);
                double rand = random.nextDouble();

                if (rand <= probability) {
                    List<ItemStack> treasure = rpgConfiguration.getTreasure(next);

                    if (treasure.size() > 0) {
                        ItemStack itemStack = treasure.get(random.nextInt(treasure.size()));

                        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), itemStack);

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

                        ItemMeta itemMeta = itemStack.getItemMeta();
                        String displayName = itemMeta.getDisplayName();

                        if (displayName == null) {
                            ItemInfo itemInfo = Items.itemByType(itemStack.getType(), itemStack.getData().getData());

                            if (itemInfo != null) {
                                displayName = itemInfo.getName();
                            } else {
                                displayName = StringUtils.capitalize(itemStack.getType().name().toLowerCase().replace("_", " "));
                            }
                        }

                        rpgPlayer.getPlayer().sendMessage(ChatColor.GOLD + rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_TREASURE_FOUND) +
                                ": [" + next.getChatColor() + displayName + ChatColor.GOLD + "]");
                    }

                    return next;
                }
            }
        }

        return null;
    }
}
