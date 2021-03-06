package me.noip.yanny.rpg;

import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Random;

class DoubleDropAbility extends Ability {

    private SkillType skillType;
    private Random random = new Random();

    DoubleDropAbility(SkillType skillType, String abilityName, int fromLevel) {
        super(abilityName, fromLevel);

        this.skillType = skillType;
    }

    @Override
    public String toString(RpgPlayer rpgPlayer) {
        return String.format("%2.1f%%", (rpgPlayer.getStatsLevel(skillType) / 1000.0 * 0.5) * 100.0);
    }

    void execute(RpgPlayer rpgPlayer, Block block) {
        int level = rpgPlayer.getStatsLevel(skillType);

        if (level < fromLevel) {
            return;
        }

        if (random.nextDouble() <= (rpgPlayer.getStatsLevel(skillType) / 1000.0 * 0.5)) { // 0.05% per level
            Collection<ItemStack> drops = block.getDrops();

            for (ItemStack itemStack : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
            }

            block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4);
        }
    }
}
