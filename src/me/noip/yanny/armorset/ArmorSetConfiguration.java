package me.noip.yanny.armorset;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.Rarity;
import me.noip.yanny.utils.CustomItemStack;
import me.noip.yanny.utils.LoggerHandler;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

class ArmorSetConfiguration {

    private static final String ARMOR_SET_SECTION = "armorset";
    private static final String CONFIGURATION_NAME = "armorset.yml";

    private static final String ARMOR_RARITY = "rarity";
    private static final String ARMOR_ITEMS = "items";
    private static final String ARMOR_ENCHANTMENTS = "enchantments";
    private static final String ARMOR_LORE = "lore";
    private static final String ARMOR_NAME = "name";
    private static final String ARMOR_EFFECTS = "set_effects";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private LoggerHandler logger;
    private Map<CustomItemStack, ItemSet> itemSets = new LinkedHashMap<>();
    private Map<Rarity, List<ItemSet>> raritySets = new LinkedHashMap<>();

    ArmorSetConfiguration(MainPlugin plugin) {
        logger = plugin.getLoggerHandler();
        plugin.saveResource(CONFIGURATION_NAME, false);
        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection armorSetsSection = serverConfigurationWrapper.getConfigurationSection(ARMOR_SET_SECTION);

        if (armorSetsSection != null) {
            for (String set : armorSetsSection.getKeys(false)) {
                ConfigurationSection armorSetSection = armorSetsSection.getConfigurationSection(set);

                if (armorSetSection == null) {
                    warning("Can`t get configuration section: " + set);
                    continue;
                }

                String rarityString = armorSetSection.getString(ARMOR_RARITY);
                Rarity rarity = Rarity.getByName(rarityString);
                List<CustomItemStack> items = new ArrayList<>();

                ConfigurationSection itemsSection = armorSetSection.getConfigurationSection(ARMOR_ITEMS);
                if (itemsSection == null) {
                    warning("Can`t get configuration section: " + ARMOR_ITEMS);
                    continue;
                }

                if (rarity == null) {
                    warning("Can`t decode Rarity type: " + rarityString);
                    continue;
                }

                for (String item : itemsSection.getKeys(false)) {
                    Material material = Material.getMaterial(item);

                    if (material == null) {
                        warning("Can`t decode Material type: " + item);
                        continue;
                    }

                    CustomItemStack itemStack = new CustomItemStack(material);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(item);
                    if (itemSection == null) {
                        warning("Can`t get configuration section: " + item);
                        continue;
                    }

                    itemMeta.setLore(itemSection.getStringList(ARMOR_LORE));
                    itemMeta.setDisplayName(itemSection.getString(ARMOR_NAME));

                    for (String enchantment : itemSection.getStringList(ARMOR_ENCHANTMENTS)) {
                        String[] tokens = enchantment.split(":");

                        if (tokens.length != 2) {
                            warning("Wrong Enchantment format: " + enchantment);
                            continue;
                        }

                        int level;
                        Enchantment ench = Enchantment.getByName(tokens[0]);

                        if (ench == null) {
                            warning("Can`t decode Enchantment type: " + tokens[0]);
                            continue;
                        }

                        try {
                            level = Integer.valueOf(tokens[1]);
                        } catch (Exception e) {
                            warning("Can`t decode Enchantment level: " + tokens[1] + " : " + e.getLocalizedMessage());
                            continue;
                        }

                        itemMeta.addEnchant(ench, level, false);
                    }

                    itemStack.setItemMeta(itemMeta);
                    items.add(itemStack);
                }

                Map<Integer, Map<PotionEffectType, Integer>> setEffects = new LinkedHashMap<>();
                ConfigurationSection effectsSection = armorSetSection.getConfigurationSection(ARMOR_EFFECTS);
                if (effectsSection == null) {
                    warning("Can`t get configuration section: " + ARMOR_EFFECTS);
                    continue;
                }

                for (String effects : effectsSection.getKeys(false)) {
                    int level;
                    Map<PotionEffectType, Integer> entry = new LinkedHashMap<>();

                    try {
                        level = Integer.parseInt(effects);
                    } catch (Exception e) {
                        warning("Can`t decode Set level: " + effects + " : " + e.getLocalizedMessage());
                        continue;
                    }

                    for (String effect : effectsSection.getStringList(effects)) {
                        String[] tokens = effect.split(":");

                        if (tokens.length != 2) {
                            warning("Wrong Effect format: " + effect);
                            continue;
                        }

                        int lvl;
                        PotionEffectType eff = PotionEffectType.getByName(tokens[0]);

                        if (eff == null) {
                            warning("Can`t decode Effect type: " + tokens[0]);
                            continue;
                        }

                        try {
                            lvl = Integer.valueOf(tokens[1]);
                        } catch (Exception e) {
                            warning("Can`t decode Enchantment level: " + tokens[1] + " : " + e.getLocalizedMessage());
                            continue;
                        }

                        entry.put(eff, lvl);
                    }

                    setEffects.put(level, entry);
                }

                ItemSet itemSet = new ItemSet(set, items, rarity, setEffects);

                raritySets.computeIfAbsent(rarity, k -> new ArrayList<>());
                raritySets.get(rarity).add(itemSet);

                for (CustomItemStack itemStack : items) {
                    itemSets.put(itemStack, itemSet);
                }
            }
        } else {
            logger.logWarn(ArmorSet.class, "No armor sets in configuration file");
        }

        for (Map.Entry<Rarity, List<ItemSet>> rarityItems : raritySets.entrySet()) {
            logger.logInfo(ArmorSet.class, rarityItems.getKey().name() + ": " + rarityItems.getValue().size() + " armor sets");
        }

        save();
    }

    private void save() {
        serverConfigurationWrapper.set(ARMOR_SET_SECTION, null); // clear
        ConfigurationSection armorSetsSection = serverConfigurationWrapper.createSection(ARMOR_SET_SECTION);

        for (Map.Entry<CustomItemStack, ItemSet> set : itemSets.entrySet()) {
            ItemSet itemSet = set.getValue();
            ConfigurationSection armorSetSection = armorSetsSection.createSection(itemSet.getName());

            armorSetSection.set(ARMOR_RARITY, itemSet.getRarity().name());
            ConfigurationSection itemsSection = armorSetSection.createSection(ARMOR_ITEMS);
            List<CustomItemStack> items = itemSet.getItems();

            for (CustomItemStack itemStack : items) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                ConfigurationSection itemSection = itemsSection.createSection(itemStack.getType().name());
                itemSection.set(ARMOR_LORE, itemMeta.getLore());
                itemSection.set(ARMOR_NAME, itemMeta.getDisplayName());

                List<String> enchantments = new ArrayList<>();
                for (Map.Entry<Enchantment, Integer> enchantment : itemMeta.getEnchants().entrySet()) {
                    enchantments.add(enchantment.getKey().getName() + ":" + enchantment.getValue());
                }
                itemSection.set(ARMOR_ENCHANTMENTS, enchantments);
            }

            Map<Integer, Map<PotionEffectType, Integer>> effects = itemSet.getEffects();
            ConfigurationSection effectsSection = armorSetSection.createSection(ARMOR_EFFECTS);
            for (Map.Entry<Integer, Map<PotionEffectType, Integer>> effect : effects.entrySet()) {
                List<String> list = new ArrayList<>();
                for (Map.Entry<PotionEffectType, Integer> entry : effect.getValue().entrySet()) {
                    list.add(entry.getKey().getName() + ":" + entry.getValue().toString());
                }
                effectsSection.set(effect.getKey().toString(), list);
            }
        }

        serverConfigurationWrapper.save();
    }

    ItemSet getItemSet(CustomItemStack itemStack) {
        return itemSets.get(itemStack);
    }

    Map<CustomItemStack, ItemSet> getArmorSets() {
        return itemSets;
    }

    List<ItemSet> getSetByRarity(Rarity rarity) {
        return raritySets.get(rarity);
    }

    private void warning(String message) {
        logger.logWarn(ArmorSet.class, message);
    }
}
