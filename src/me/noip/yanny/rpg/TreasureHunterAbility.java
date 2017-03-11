package me.noip.yanny.rpg;

import me.noip.yanny.utils.ItemInfo;
import me.noip.yanny.utils.Items;
import me.noip.yanny.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

import static me.noip.yanny.rpg.RpgTranslation.*;

class TreasureHunterAbility extends Ability {

    private SkillType skillType;
    private RpgConfiguration rpgConfiguration;
    private Random random = new Random();

    TreasureHunterAbility(SkillType skillType, String abilityName, int fromLevel, RpgConfiguration rpgConfiguration) {
        super(abilityName, fromLevel);

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
                                entity.getWorld().playEffect(entity.getLocation(), Effect.SMOKE, 4);
                                break;
                            case RARE:
                            case EXOTIC:
                            case HEROIC:
                                entity.getWorld().playEffect(entity.getLocation(), Effect.MOBSPAWNER_FLAMES, 4);
                                break;
                            case EPIC:
                            case LEGENDARY:
                            case MYTHIC:
                            case GODLIKE:
                                entity.getWorld().playEffect(entity.getLocation(), Effect.DRAGON_BREATH, 4);
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

                        rpgPlayer.getPlayer().sendMessage(TREASURE_FOUND.display().replace("{TREASURE}", next.getChatColor() + displayName + TREASURE_FOUND.getChatColor()));
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
                                block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4);
                                break;
                            case RARE:
                            case EXOTIC:
                            case HEROIC:
                                block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 4);
                                break;
                            case EPIC:
                            case LEGENDARY:
                            case MYTHIC:
                            case GODLIKE:
                                block.getWorld().playEffect(block.getLocation(), Effect.DRAGON_BREATH, 4);
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

                        rpgPlayer.getPlayer().sendMessage(TREASURE_FOUND.display().replace("{TREASURE}", next.getChatColor() + displayName + TREASURE_FOUND.getChatColor()));
                    }

                    return next;
                }
            }
        }

        return null;
    }
}
