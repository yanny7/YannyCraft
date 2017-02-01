package me.noip.yanny;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RewardWrapper {

    private static final String COUNT = "count";
    private static final String MATERIAL = "reward";
    private static final String LORE = "lore";
    private static final String NAME = "name";
    private static final String ENCHANTMENT = "enchantment";

    private static final String RPG_PICKAXE = RewardWrapper.RewardType.PICKAXE.name().toLowerCase();
    private static final String RPG_SPADE = RewardWrapper.RewardType.SPADE.name().toLowerCase();
    private static final String RPG_AXE = RewardWrapper.RewardType.AXE.name().toLowerCase();
    private static final String RPG_SWORD = RewardWrapper.RewardType.SWORD.name().toLowerCase();

    private static final String REWARD_SECTION = "reward";

    private Plugin plugin;
    private Map<RewardType, Map<Integer, ItemStack>> rewards = new HashMap<>();
    private ServerConfigurationWrapper serverConfigurationWrapper;

    RewardWrapper(ServerConfigurationWrapper serverConfigurationWrapper, Plugin plugin) {
        this.serverConfigurationWrapper = serverConfigurationWrapper;
        this.plugin = plugin;
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

    }

    ItemStack getReward(RewardType type, int count) {
        //return rewards.get(type).get(count);
        return null;
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

    private void buildRewardSystem(ConfigurationSection section) {
        ConfigurationSection reward = section.getConfigurationSection("rpg_reward");
        if (reward == null) {
            reward = section.createSection("rpg_reward");
        }

        ConfigurationSection pickaxeReward = reward.getConfigurationSection(RPG_PICKAXE);
        if (pickaxeReward == null) {
            pickaxeReward = reward.createSection(RPG_PICKAXE);
        }
        Map<Enchantment, Integer> pickaxeEnchantments = new HashMap<>();
        pickaxeEnchantments.put(Enchantment.DIG_SPEED, 5);
        pickaxeEnchantments.put(Enchantment.DURABILITY, 10);
        addPickaxeReward(pickaxeReward, 100, Material.WOOD_PICKAXE, pickaxeEnchantments, new String[]{"Nastroj pravekeho cloveka"}, "Dreveny klin");
        addPickaxeReward(pickaxeReward, 500, Material.STONE_PICKAXE, pickaxeEnchantments, new String[]{"Vrcholne dielo davnoveku"}, "Kamenne dlato");
        addPickaxeReward(pickaxeReward, 1000, Material.GOLD_PICKAXE, pickaxeEnchantments, new String[]{"Nastroj bohov"}, "Zlate kladivko");
        addPickaxeReward(pickaxeReward, 5000, Material.IRON_PICKAXE, pickaxeEnchantments, new String[]{"Najnovsi model W4000"}, "Zelezna zbijacka");
        addPickaxeReward(pickaxeReward, 10000, Material.DIAMOND_PICKAXE, pickaxeEnchantments, new String[]{"THE DRILLER"}, "Diamantovy vrtak");

        ConfigurationSection spadeReward = reward.getConfigurationSection(RPG_SPADE);
        if (spadeReward == null) {
            spadeReward = reward.createSection(RPG_SPADE);
        }
        Map<Enchantment, Integer> spadeEnchantments = new HashMap<>();
        spadeEnchantments.put(Enchantment.DIG_SPEED, 5);
        spadeEnchantments.put(Enchantment.DURABILITY, 10);
        addPickaxeReward(spadeReward, 100, Material.WOOD_SPADE, spadeEnchantments, new String[]{"Nastroj prvych ludi"}, "Drevena lopatka");
        addPickaxeReward(spadeReward, 500, Material.STONE_SPADE, spadeEnchantments, new String[]{"Tymto sa kopali diery"}, "Kamenny ryl");
        addPickaxeReward(spadeReward, 1000, Material.GOLD_SPADE, spadeEnchantments, new String[]{"Bajna lopata"}, "Zlata lopata");
        addPickaxeReward(spadeReward, 5000, Material.IRON_SPADE, spadeEnchantments, new String[]{"S tymto vykopes najhlbsiu", "jamu na svete"}, "Zelezny bager");
        addPickaxeReward(spadeReward, 10000, Material.DIAMOND_SPADE, spadeEnchantments, new String[]{"VYKOPAVAC"}, "Diamantovy odstranovac zeme");

        ConfigurationSection axeReward = reward.getConfigurationSection(RPG_AXE);
        if (axeReward == null) {
            axeReward = reward.createSection(RPG_AXE);
        }
        Map<Enchantment, Integer> axeEnchantments = new HashMap<>();
        axeEnchantments.put(Enchantment.DIG_SPEED, 5);
        axeEnchantments.put(Enchantment.DURABILITY, 10);
        addPickaxeReward(axeReward, 100, Material.WOOD_AXE, axeEnchantments, new String[]{"S tymto toho moc neurobis"}, "Drevena rybicka");
        addPickaxeReward(axeReward, 500, Material.STONE_AXE, axeEnchantments, new String[]{"Odstranovac kory stromov"}, "Kamenna ziletka");
        addPickaxeReward(axeReward, 1000, Material.GOLD_AXE, axeEnchantments, new String[]{"Mimozemsky nastroj"}, "Zlata sekerka");
        addPickaxeReward(axeReward, 5000, Material.IRON_AXE, axeEnchantments, new String[]{"Pomocou tohto nastroja", "odstranis vsetky stromy", "zeme"}, "Motorova pila");
        addPickaxeReward(axeReward, 10000, Material.DIAMOND_AXE, axeEnchantments, new String[]{"HLAVNE SA NEPOREZ"}, "Diamantovy odstranovac stromov");

        ConfigurationSection swordReward = reward.getConfigurationSection(RPG_SWORD);
        if (swordReward == null) {
            swordReward = reward.createSection(RPG_SWORD);
        }
        Map<Enchantment, Integer> swordEnchantments = new HashMap<>();
        swordEnchantments.put(Enchantment.DAMAGE_ALL, 100);
        swordEnchantments.put(Enchantment.LOOT_BONUS_MOBS, 3);
        swordEnchantments.put(Enchantment.DURABILITY, 5);
        addPickaxeReward(swordReward, 100, Material.WOOD_SWORD, swordEnchantments, new String[]{"Dobre na napichovanie mravcov"}, "Dreveny ostep");
        addPickaxeReward(swordReward, 500, Material.STONE_SWORD, swordEnchantments, new String[]{"Zabijak domorodcov"}, "Kamenny kijak");
        addPickaxeReward(swordReward, 1000, Material.GOLD_SWORD, swordEnchantments, new String[]{"Zabijak bohov"}, "Zlaty mec nadvlady");
        addPickaxeReward(swordReward, 5000, Material.IRON_SWORD, swordEnchantments, new String[]{"Nastroj assassina"}, "Zelezny tichy zabijak");
        addPickaxeReward(swordReward, 10000, Material.DIAMOND_SWORD, swordEnchantments, new String[]{"POPRAVCA"}, "Diamantova rychla smrt");
    }

    private void addPickaxeReward(ConfigurationSection section, int count, Material material, Map<Enchantment, Integer> enchantments, String[] lore, String name) {
        ConfigurationSection stage = section.createSection("stage" + count);
        stage.set(RewardWrapper.COUNT, count);
        stage.set(RewardWrapper.MATERIAL, material.toString());
        stage.set(RewardWrapper.LORE, Arrays.asList(lore));
        stage.set(RewardWrapper.NAME, name);

        ConfigurationSection enchantment = stage.createSection(RewardWrapper.ENCHANTMENT);
        for (Map.Entry<Enchantment, Integer> pair : enchantments.entrySet()) {
            enchantment.set(pair.getKey().getName(), pair.getValue());
        }
    }

    enum RewardType {
        PICKAXE,
        SPADE,
        AXE,
        SWORD,
        ;
    }
}
