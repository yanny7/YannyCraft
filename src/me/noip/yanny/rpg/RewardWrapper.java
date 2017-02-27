package me.noip.yanny.rpg;

import me.noip.yanny.utils.ServerConfigurationWrapper;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class RewardWrapper {

    private static final String MATERIAL = "reward";
    private static final String LORE = "lore";
    private static final String NAME = "name";
    private static final String ENCHANTMENT = "enchantment";

    private static final String REWARD_SECTION = "reward";

    private Plugin plugin;
    private Map<RewardType, Map<Integer, ItemStack>> rewards = new HashMap<>();
    private ServerConfigurationWrapper serverConfigurationWrapper;

    RewardWrapper(ServerConfigurationWrapper serverConfigurationWrapper, Plugin plugin) {
        this.serverConfigurationWrapper = serverConfigurationWrapper;
        this.plugin = plugin;

        rewards.put(RewardType.AXE,
                new HashMap<Integer, ItemStack>(){{
                    put(Integer.MAX_VALUE, createReward(Material.WOOD_AXE, new HashMap<Enchantment, Integer>(){{
                        put(Enchantment.DIG_SPEED, 1);
                    }}, new String[]{
                            "Test lore"
                    }, "Test name"));
                }}
        );
        rewards.put(RewardType.PICKAXE,
                new HashMap<Integer, ItemStack>(){{
                    put(Integer.MAX_VALUE, createReward(Material.WOOD_PICKAXE, new HashMap<Enchantment, Integer>(){{
                        put(Enchantment.DIG_SPEED, 1);
                    }}, new String[]{
                            "Test lore"
                    }, "Test name"));
                }}
        );
        rewards.put(RewardType.SPADE,
                new HashMap<Integer, ItemStack>(){{
                    put(Integer.MAX_VALUE, createReward(Material.WOOD_SPADE, new HashMap<Enchantment, Integer>(){{
                        put(Enchantment.DIG_SPEED, 1);
                    }}, new String[]{
                            "Test lore"
                    }, "Test name"));
                }}
        );
        rewards.put(RewardType.SWORD,
                new HashMap<Integer, ItemStack>(){{
                    put(Integer.MAX_VALUE, createReward(Material.WOOD_SWORD, new HashMap<Enchantment, Integer>(){{
                        put(Enchantment.DAMAGE_ALL, 1);
                    }}, new String[]{
                            "Test lore"
                    }, "Test name"));
                }}
        );
    }

    void load() {
        ConfigurationSection rewardSection = serverConfigurationWrapper.getConfigurationSection(REWARD_SECTION);
        if (rewardSection == null) {
            rewardSection = serverConfigurationWrapper.createSection(REWARD_SECTION);
        }

        for (String key : rewardSection.getKeys(false)) {
            buildReward(RewardType.valueOf(key.toUpperCase()), rewardSection.getConfigurationSection(key));
        }

        save();
    }

    void save() {
        ConfigurationSection rewardSection = serverConfigurationWrapper.getConfigurationSection(REWARD_SECTION);

        storeReward(rewardSection);
    }

    ItemStack getReward(RewardType type, int count) {
        Map<Integer, ItemStack> reward = rewards.get(type);

        if (reward == null) {
            return null;
        }

        return reward.get(count);
    }

    private void buildReward(RewardType type, ConfigurationSection section) {
        Map<Integer, ItemStack> rewardMap = new HashMap<>();

        for (String key : section.getKeys(false)) {
            ConfigurationSection rewardSection = section.getConfigurationSection(key);
            int count = Integer.parseInt(key);
            Material material = Material.getMaterial(rewardSection.getString(MATERIAL));
            String name = rewardSection.getString(NAME);
            List<String> lore = rewardSection.getStringList(LORE);
            Map<Enchantment, Integer> enchantments = new HashMap<>();

            if (rewardSection.contains(ENCHANTMENT)) {
                ConfigurationSection enchantmentsSection = rewardSection.getConfigurationSection(ENCHANTMENT);
                for (Map.Entry<String, Object> pair : enchantmentsSection.getValues(false).entrySet()) {
                    Enchantment enchantment = Enchantment.getByName(pair.getKey());
                    int level = (Integer)pair.getValue();

                    if (enchantment != null) {
                        enchantments.put(enchantment, level);
                    } else {
                        plugin.getLogger().warning("Cant create enchantment " + pair.getKey());
                    }
                }
            }

            ItemStack reward = createReward(material, enchantments, lore.toArray(new String[lore.size()]), name);
            rewardMap.put(count, reward);
        }

        rewards.put(type, rewardMap);
    }

    private void storeReward(ConfigurationSection section) {
        for (RewardType rewardType : RewardType.values()) {
            Map<Integer, ItemStack> rewardMap = rewards.get(rewardType);

            ConfigurationSection rewardsSection = section.getConfigurationSection(rewardType.name());
            if (rewardsSection == null) {
                rewardsSection = section.createSection(rewardType.name());
            }

            for (Map.Entry<Integer, ItemStack> entry : rewardMap.entrySet()) {
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(entry.getKey().toString());
                if (rewardSection == null) {
                    rewardSection = rewardsSection.createSection(entry.getKey().toString());
                }

                ItemStack reward = entry.getValue();
                ItemMeta rewardMeta = reward.getItemMeta();

                rewardSection.set(MATERIAL, reward.getType().name());
                rewardSection.set(NAME, rewardMeta.getDisplayName());
                rewardSection.set(LORE, rewardMeta.getLore());

                ConfigurationSection enchantmentSection = rewardSection.getConfigurationSection(ENCHANTMENT);
                if (enchantmentSection == null) {
                    enchantmentSection = rewardSection.createSection(ENCHANTMENT);
                }

                for (Map.Entry<Enchantment, Integer> enchantment : rewardMeta.getEnchants().entrySet()) {
                    enchantmentSection.set(enchantment.getKey().getName(), enchantment.getValue());
                }
            }
        }
    }

    private ItemStack createReward(Material material, Map<Enchantment, Integer> enchantments, String[] lore, String name) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta meta = itemStack.getItemMeta();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            if (!meta.addEnchant(entry.getKey(), entry.getValue(), true)) {
                plugin.getLogger().warning("Cant create reward " + entry.getKey() + " level " + entry.getValue());
            }
        }

        meta.setLore(Arrays.asList(lore));
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public enum RewardType {
        PICKAXE("Vykopanych blokov:"),
        SPADE("Vytazenych blokov:"),
        AXE("Narubaneho dreva:"),
        SWORD("Zabitych mobov:"),
        ;

        private String displayName;

        RewardType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
