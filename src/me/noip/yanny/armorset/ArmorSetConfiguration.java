package me.noip.yanny.armorset;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.rpg.Rarity;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

class ArmorSetConfiguration {

    private static final String ARMOR_SET_SECTION = "armorset";
    private static final String CONFIGURATION_NAME = "armorset";

    private static final String ARMOR_RARITY = "rarity";
    private static final String ARMOR_ITEMS = "items";
    private static final String ARMOR_ENCHANTMENTS = "enchantments";
    private static final String ARMOR_LORE = "lore";
    private static final String ARMOR_EFFECTS = "set_effects";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private MainPlugin plugin;
    private Map<ItemStack, ItemSet> itemSets = new HashMap<>();

    ArmorSetConfiguration(MainPlugin plugin) {
        this.plugin = plugin;
        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);

        List<ItemStack> list = new ArrayList<>();
        {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_HELMET);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setLore(Arrays.asList("Helmet of fire"));
            itemMeta.addEnchant(Enchantment.DURABILITY, 2, false);
            itemMeta.addEnchant(Enchantment.PROTECTION_FIRE, 2, false);
            itemStack.setItemMeta(itemMeta);

            list.add(itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_CHESTPLATE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setLore(Arrays.asList("Chestplate of fire"));
            itemMeta.addEnchant(Enchantment.DURABILITY, 2, false);
            itemMeta.addEnchant(Enchantment.PROTECTION_FIRE, 2, false);
            itemStack.setItemMeta(itemMeta);

            list.add(itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_LEGGINGS);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setLore(Arrays.asList("Leggins of fire"));
            itemMeta.addEnchant(Enchantment.DURABILITY, 2, false);
            itemMeta.addEnchant(Enchantment.PROTECTION_FIRE, 2, false);
            itemStack.setItemMeta(itemMeta);

            list.add(itemStack);
        }
        {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_BOOTS);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setLore(Arrays.asList("Boots of fire"));
            itemMeta.addEnchant(Enchantment.DURABILITY, 2, false);
            itemMeta.addEnchant(Enchantment.PROTECTION_FIRE, 2, false);
            itemStack.setItemMeta(itemMeta);

            list.add(itemStack);
        }

        Map<Integer, Map<Enchantment, Integer>> setEffect = new HashMap<>();
        setEffect.put(2, new HashMap<Enchantment, Integer>(){{ put(Enchantment.FIRE_ASPECT, 1); }});
        setEffect.put(3, new HashMap<Enchantment, Integer>(){{ put(Enchantment.FIRE_ASPECT, 2); }});
        setEffect.put(4, new HashMap<Enchantment, Integer>(){{ put(Enchantment.FIRE_ASPECT, 2); put(Enchantment.KNOCKBACK, 1); }});

        for (ItemStack itemStack : list) {
            itemSets.put(itemStack, new ItemSet("Armor of fire", list, Rarity.HEROIC, setEffect));
        }
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection armorSetsSection = serverConfigurationWrapper.getConfigurationSection(ARMOR_SET_SECTION);
        if (armorSetsSection == null) {
            armorSetsSection = serverConfigurationWrapper.createSection(ARMOR_SET_SECTION);
        } else {
            itemSets.clear(); // do not use default set, if there is defined in configuration
        }

        for (String set : armorSetsSection.getKeys(false)) {
            ConfigurationSection armorSetSection = armorSetsSection.getConfigurationSection(set);
            if (armorSetSection == null) {
                debugMessage("Can`t get configuration section: " + set);
                continue;
            }

            String rarityString = armorSetSection.getString(ARMOR_RARITY);
            Rarity rarity = Rarity.getByName(rarityString);
            List<ItemStack> items = new ArrayList<>();

            ConfigurationSection itemsSection = armorSetSection.getConfigurationSection(ARMOR_ITEMS);
            if (itemsSection == null) {
                debugMessage("Can`t get configuration section: " + ARMOR_ITEMS);
                continue;
            }

            if (rarity == null) {
                debugMessage("Can`t decode Rarity type: " + rarityString);
                continue;
            }

            for (String item : itemsSection.getKeys(false)) {
                Material material = Material.getMaterial(item);

                if (material == null) {
                    debugMessage("Can`t decode Material type: " + item);
                    continue;
                }

                ItemStack itemStack = new ItemStack(material);
                ItemMeta itemMeta = itemStack.getItemMeta();

                ConfigurationSection itemSection = itemsSection.getConfigurationSection(item);
                if (itemSection == null) {
                    debugMessage("Can`t get configuration section: " + item);
                    continue;
                }

                itemMeta.setLore(itemSection.getStringList(ARMOR_LORE));

                for (String enchantment : itemSection.getStringList(ARMOR_ENCHANTMENTS)) {
                    String[] tokens = enchantment.split(":");

                    if (tokens.length != 2) {
                        debugMessage("Wrong Enchantment format: " + enchantment);
                        continue;
                    }

                    int level;
                    Enchantment ench = Enchantment.getByName(tokens[0]);

                    if (ench == null) {
                        debugMessage("Can`t decode Enchantment type: " + tokens[0]);
                        continue;
                    }

                    try {
                        level = Integer.valueOf(tokens[1]);
                    } catch (Exception e) {
                        debugMessage("Can`t decode Enchantment level: " + tokens[1] + " : " + e.getLocalizedMessage());
                        continue;
                    }

                    itemMeta.addEnchant(ench, level, false);
                }

                itemStack.setItemMeta(itemMeta);
                items.add(itemStack);
            }

            Map<Integer, Map<Enchantment, Integer>> setEffects = new HashMap<>();
            ConfigurationSection effectsSection = armorSetSection.getConfigurationSection(ARMOR_EFFECTS);
            if (effectsSection == null) {
                debugMessage("Can`t get configuration section: " + ARMOR_EFFECTS);
                continue;
            }

            for (String effects : effectsSection.getKeys(false)) {
                int level;
                Map<Enchantment, Integer> entry = new HashMap<>();

                try {
                    level = Integer.parseInt(effects);
                } catch (Exception e) {
                    debugMessage("Can`t decode Set level: " + effects + " : " + e.getLocalizedMessage());
                    continue;
                }

                for(String effect : effectsSection.getStringList(effects)) {
                    String[] tokens = effect.split(":");

                    if (tokens.length != 2) {
                        debugMessage("Wrong Effect format: " + effect);
                        continue;
                    }

                    int lvl;
                    Enchantment ench = Enchantment.getByName(tokens[0]);

                    if (ench == null) {
                        debugMessage("Can`t decode Effect type: " + tokens[0]);
                        continue;
                    }

                    try {
                        lvl = Integer.valueOf(tokens[1]);
                    } catch (Exception e) {
                        debugMessage("Can`t decode Enchantment level: " + tokens[1] + " : " + e.getLocalizedMessage());
                        continue;
                    }

                    entry.put(ench, lvl);
                }

                setEffects.put(level, entry);
            }

            ItemSet itemSet = new ItemSet(set, items, rarity, setEffects);

            for (ItemStack itemStack : items) {
                itemSets.put(itemStack, itemSet);
            }
        }

        save();
    }

    private void save() {
        serverConfigurationWrapper.set(ARMOR_SET_SECTION, null); // clear
        ConfigurationSection armorSetsSection = serverConfigurationWrapper.createSection(ARMOR_SET_SECTION);

        for (Map.Entry<ItemStack, ItemSet> set : itemSets.entrySet()) {
            ItemSet itemSet = set.getValue();
            ConfigurationSection armorSetSection = armorSetsSection.createSection(itemSet.getName());

            armorSetSection.set(ARMOR_RARITY, itemSet.getRarity().name());
            ConfigurationSection itemsSection = armorSetSection.createSection(ARMOR_ITEMS);
            List<ItemStack> items = itemSet.getItems();

            for (ItemStack itemStack : items) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                ConfigurationSection itemSection = itemsSection.createSection(itemStack.getType().name());
                itemSection.set(ARMOR_LORE, itemMeta.getLore());

                List<String> enchantments = new ArrayList<>();
                for (Map.Entry<Enchantment, Integer> enchantment : itemMeta.getEnchants().entrySet()) {
                    enchantments.add(enchantment.getKey().getName() + ":" + enchantment.getValue());
                }
                itemSection.set(ARMOR_ENCHANTMENTS, enchantments);
            }

            Map<Integer, Map<Enchantment, Integer>> effects = itemSet.getSetEffect();
            ConfigurationSection effectsSection = armorSetSection.createSection(ARMOR_EFFECTS);
            for (Map.Entry<Integer, Map<Enchantment, Integer>> effect : effects.entrySet()) {
                List<String> list = new ArrayList<>();
                for (Map.Entry<Enchantment, Integer> entry : effect.getValue().entrySet()) {
                    list.add(entry.getKey().getName() + ":" + entry.getValue().toString());
                }
                effectsSection.set(effect.getKey().toString(), list);
            }
        }

        serverConfigurationWrapper.save();
    }

    ItemSet getItemSet(ItemStack itemStack) {
        return itemSets.get(itemStack);
    }

    Map<ItemStack,ItemSet> getArmorSets() {
        return itemSets;
    }

    private void debugMessage(String msg) {
        plugin.getLogger().warning("[" + ArmorSet.class.getSimpleName() + "] " + msg);
    }
}
