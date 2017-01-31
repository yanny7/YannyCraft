package me.noip.yanny;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RewardWrapper {

    static final String COUNT = "count";
    static final String MATERIAL = "reward";
    static final String LORE = "lore";
    static final String NAME = "name";
    static final String ENCHANTMENT = "enchantment";

    private Plugin plugin;
    private Map<RewardType, Map<Integer, ItemStack>> rewards = new HashMap<>();

    RewardWrapper(ConfigurationSection section, Plugin plugin) {
        this.plugin = plugin;

        for (String key : section.getKeys(false)) {
            buildReward(RewardType.valueOf(key.toUpperCase()), section.getConfigurationSection(key));
        }
    }

    ItemStack getReward(RewardType type, int count) {
        return rewards.get(type).get(count);
    }

    private void buildReward(RewardType type, ConfigurationSection section) {
        Map<Integer, ItemStack> rewardMap = new HashMap<>();

        for (String key : section.getKeys(false)) {
            ConfigurationSection rewardSection = section.getConfigurationSection(key);
            int count = rewardSection.getInt(COUNT);
            Material material = Material.getMaterial(rewardSection.getString(MATERIAL));
            String name = rewardSection.getString(NAME);
            List<String> lore = rewardSection.getStringList(LORE);
            ItemStack reward = new ItemStack(material, 1);
            ItemMeta meta = reward.getItemMeta();

            if (rewardSection.contains(ENCHANTMENT)) {
                ConfigurationSection enchantmentsSection = rewardSection.getConfigurationSection(ENCHANTMENT);
                for (Map.Entry<String, Object> pair : enchantmentsSection.getValues(false).entrySet()) {
                    Enchantment enchantment = Enchantment.getByName(pair.getKey());
                    int level = (Integer)pair.getValue();

                    if (enchantment != null) {
                        meta.addEnchant(enchantment, level, true);
                    } else {
                        plugin.getLogger().warning("Cant create enchantment " + pair.getKey());
                    }
                }
            }

            meta.setLore(lore);
            meta.setDisplayName(name);
            reward.setItemMeta(meta);
            rewardMap.put(count, reward);
        }

        rewards.put(type, rewardMap);
    }

    enum RewardType {
        PICKAXE,
        SPADE,
        AXE,
        SWORD,
        ;
    }
}
