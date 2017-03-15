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
    private static final String CONFIGURATION_NAME = "armorset";

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
        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);

        buildDefaultSets();
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection armorSetsSection = serverConfigurationWrapper.getConfigurationSection(ARMOR_SET_SECTION);
        if (armorSetsSection == null) {
            armorSetsSection = serverConfigurationWrapper.createSection(ARMOR_SET_SECTION);
        } else {
            // do not use default set, if there is defined in configuration
            itemSets.clear();
            raritySets.clear();
        }

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

                for(String effect : effectsSection.getStringList(effects)) {
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

    private void buildDefaultSets() {
        List<ItemSet> sets = new LinkedList<>();

        /*
         * UNCOMMON
         */
        {
            ItemSet itemSet = new ItemSet("OldLeatherSet", Rarity.UNCOMMON);
            itemSet.addItem(Material.LEATHER_HELMET, "Old leather helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.LEATHER_CHESTPLATE, "Old leather chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.LEATHER_LEGGINGS, "Old leather leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.LEATHER_BOOTS, "Old leather boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Old leather shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("OldIronSet", Rarity.UNCOMMON);
            itemSet.addItem(Material.IRON_HELMET, "Old iron helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.IRON_CHESTPLATE, "Old iron chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.IRON_LEGGINGS, "Old iron leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.IRON_BOOTS, "Old iron boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Old iron shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("OldChainmailSet", Rarity.UNCOMMON);
            itemSet.addItem(Material.CHAINMAIL_HELMET, "Old chainmail helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.CHAINMAIL_CHESTPLATE, "Old chainmail chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.CHAINMAIL_LEGGINGS, "Old chainmail leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.CHAINMAIL_BOOTS, "Old chainmail boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Old chainmail shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        /*
         * RARE
         */
        {
            ItemSet itemSet = new ItemSet("LeatherSet", Rarity.RARE);
            itemSet.addItem(Material.LEATHER_HELMET, "Leather helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.LEATHER_CHESTPLATE, "Leather chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.LEATHER_LEGGINGS, "Leather leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.LEATHER_BOOTS, "Leather boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Leather shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.NIGHT_VISION, 1);
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("IronSet", Rarity.RARE);
            itemSet.addItem(Material.IRON_HELMET, "Iron helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.IRON_CHESTPLATE, "Iron chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.IRON_LEGGINGS, "Iron leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.IRON_BOOTS, "Iron boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Iron shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("ChainmailSet", Rarity.RARE);
            itemSet.addItem(Material.CHAINMAIL_HELMET, "Chainmail helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.CHAINMAIL_CHESTPLATE, "Chainmail chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.CHAINMAIL_LEGGINGS, "Chainmail leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.CHAINMAIL_BOOTS, "Chainmail boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Chainmail shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 1);
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("GoldSet", Rarity.EXOTIC);
            itemSet.addItem(Material.GOLD_HELMET, "Gold helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.GOLD_CHESTPLATE, "Gold chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.GOLD_LEGGINGS, "Gold leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.GOLD_BOOTS, "Gold boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Gold shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
                put(PotionEffectType.HEALTH_BOOST, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        /*
         * EXOTIC
         */
        {
            ItemSet itemSet = new ItemSet("NewLeatherSet", Rarity.EXOTIC);
            itemSet.addItem(Material.LEATHER_HELMET, "New leather helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 1);
            }});
            itemSet.addItem(Material.LEATHER_CHESTPLATE, "New leather chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.LEATHER_LEGGINGS, "New leather leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.LEATHER_BOOTS, "New leather boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.SHIELD, "New leather shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.SPEED, 2);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("NewIronSet", Rarity.EXOTIC);
            itemSet.addItem(Material.IRON_HELMET, "New iron helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.IRON_CHESTPLATE, "New iron chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.IRON_LEGGINGS, "New iron leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.IRON_BOOTS, "New iron boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_FIRE, 1);
            }});
            itemSet.addItem(Material.SHIELD, "New iron shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 2);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("NewChainmailSet", Rarity.EXOTIC);
            itemSet.addItem(Material.CHAINMAIL_HELMET, "New chainmail helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.CHAINMAIL_CHESTPLATE, "New chainmail chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.CHAINMAIL_LEGGINGS, "New chainmail leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            }});
            itemSet.addItem(Material.CHAINMAIL_BOOTS, "New chainmail boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.SHIELD, "New chainmail shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 2);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("NewGoldSet", Rarity.EXOTIC);
            itemSet.addItem(Material.GOLD_HELMET, "New gold helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.GOLD_CHESTPLATE, "New gold chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addItem(Material.GOLD_LEGGINGS, "New gold leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.GOLD_BOOTS, "New gold boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.SHIELD, "New gold shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        /*
         * HEROIC
         */
        {
            ItemSet itemSet = new ItemSet("HeroicLeatherSet", Rarity.HEROIC);
            itemSet.addItem(Material.LEATHER_HELMET, "Heroic leather helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 1);
            }});
            itemSet.addItem(Material.LEATHER_CHESTPLATE, "Heroic leather chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_FALL, 1);
            }});
            itemSet.addItem(Material.LEATHER_LEGGINGS, "Heroic leather leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_FIRE, 1);
            }});
            itemSet.addItem(Material.LEATHER_BOOTS, "Heroic leather boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Heroic leather shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.SPEED, 2);
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("HeroicIronSet", Rarity.HEROIC);
            itemSet.addItem(Material.IRON_HELMET, "Heroic iron helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_FIRE, 1);
            }});
            itemSet.addItem(Material.IRON_CHESTPLATE, "Heroic iron chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_FIRE, 1);
            }});
            itemSet.addItem(Material.IRON_LEGGINGS, "Heroic iron leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_FIRE, 1);
            }});
            itemSet.addItem(Material.IRON_BOOTS, "Heroic iron boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_FIRE, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Heroic iron shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_FIRE, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 2);
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("HeroicChainmailSet", Rarity.HEROIC);
            itemSet.addItem(Material.CHAINMAIL_HELMET, "Heroic chainmail helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addItem(Material.CHAINMAIL_CHESTPLATE, "Heroic chainmail chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addItem(Material.CHAINMAIL_LEGGINGS, "Heroic chainmail leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addItem(Material.CHAINMAIL_BOOTS, "Heroic chainmail boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Heroic chainmail shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 2);
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("HeroicGoldSet", Rarity.HEROIC);
            itemSet.addItem(Material.GOLD_HELMET, "Heroic gold helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.WATER_WORKER, 1);
            }});
            itemSet.addItem(Material.GOLD_CHESTPLATE, "Heroic gold chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.OXYGEN, 1);
            }});
            itemSet.addItem(Material.GOLD_LEGGINGS, "Heroic gold leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.OXYGEN, 1);
            }});
            itemSet.addItem(Material.GOLD_BOOTS, "Heroic gold boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.OXYGEN, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Heroic gold shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 2);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("HeroicDiamondSet", Rarity.HEROIC);
            itemSet.addItem(Material.DIAMOND_HELMET, "Heroic diamond helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.DIAMOND_CHESTPLATE, "Heroic diamond chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.DIAMOND_LEGGINGS, "Heroic diamond leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.DIAMOND_BOOTS, "Heroic diamond boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Heroic diamond shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 2);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        /*
         * EPIC
         */
        {
            ItemSet itemSet = new ItemSet("EpicIronSet", Rarity.EPIC);
            itemSet.addItem(Material.IRON_HELMET, "Epic iron helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
            }});
            itemSet.addItem(Material.IRON_CHESTPLATE, "Epic iron chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
            }});
            itemSet.addItem(Material.IRON_LEGGINGS, "Epic iron leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
            }});
            itemSet.addItem(Material.IRON_BOOTS, "Epic iron boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Epic iron shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 2);
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 2);
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("EpicChainmailSet", Rarity.EPIC);
            itemSet.addItem(Material.CHAINMAIL_HELMET, "Epic chainmail helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.THORNS, 2);
            }});
            itemSet.addItem(Material.CHAINMAIL_CHESTPLATE, "Epic chainmail chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.THORNS, 2);
            }});
            itemSet.addItem(Material.CHAINMAIL_LEGGINGS, "Epic chainmail leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.THORNS, 2);
            }});
            itemSet.addItem(Material.CHAINMAIL_BOOTS, "Epic chainmail boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.THORNS, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Epic chainmail shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.THORNS, 2);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 2);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 3);
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("EpicGoldSet", Rarity.EPIC);
            itemSet.addItem(Material.GOLD_HELMET, "Epic gold helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.WATER_WORKER, 2);
            }});
            itemSet.addItem(Material.GOLD_CHESTPLATE, "Epic gold chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
            }});
            itemSet.addItem(Material.GOLD_LEGGINGS, "Epic gold leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
            }});
            itemSet.addItem(Material.GOLD_BOOTS, "Epic gold boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Epic gold shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 1);
                put(PotionEffectType.HEALTH_BOOST, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("EpicDiamondSet", Rarity.EPIC);
            itemSet.addItem(Material.DIAMOND_HELMET, "Epic diamond helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }});
            itemSet.addItem(Material.DIAMOND_CHESTPLATE, "Epic diamond chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }});
            itemSet.addItem(Material.DIAMOND_LEGGINGS, "Epic diamond leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }});
            itemSet.addItem(Material.DIAMOND_BOOTS, "Epic diamond boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Epic diamond shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
                put(PotionEffectType.FAST_DIGGING, 2);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 3);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        /*
         * LEGENDARY
         */
        {
            ItemSet itemSet = new ItemSet("LegendaryIronSet", Rarity.LEGENDARY);
            itemSet.addItem(Material.IRON_HELMET, "Legendary iron helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.THORNS, 2);
            }});
            itemSet.addItem(Material.IRON_CHESTPLATE, "Legendary iron chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.THORNS, 2);
            }});
            itemSet.addItem(Material.IRON_LEGGINGS, "Legendary iron leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.THORNS, 2);
            }});
            itemSet.addItem(Material.IRON_BOOTS, "Legendary iron boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.THORNS, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Legendary iron shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.THORNS, 2);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 2);
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 3);
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("LegendaryGoldSet", Rarity.LEGENDARY);
            itemSet.addItem(Material.GOLD_HELMET, "Legendary gold helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.WATER_WORKER, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 2);
            }});
            itemSet.addItem(Material.GOLD_CHESTPLATE, "Legendary gold chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 2);
            }});
            itemSet.addItem(Material.GOLD_LEGGINGS, "Legendary gold leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 2);
            }});
            itemSet.addItem(Material.GOLD_BOOTS, "Legendary gold boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Legendary gold shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 2);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 2);
                put(PotionEffectType.HEALTH_BOOST, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("LegendaryDiamondSet", Rarity.LEGENDARY);
            itemSet.addItem(Material.DIAMOND_HELMET, "Legendary diamond helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 2);
            }});
            itemSet.addItem(Material.DIAMOND_CHESTPLATE, "Legendary diamond chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 2);
            }});
            itemSet.addItem(Material.DIAMOND_LEGGINGS, "Legendary diamond leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 2);
            }});
            itemSet.addItem(Material.DIAMOND_BOOTS, "Legendary diamond boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Legendary diamond shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 2);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
                put(PotionEffectType.FAST_DIGGING, 2);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 3);
                put(PotionEffectType.NIGHT_VISION, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        /*
         * MYTHIC
         */
        {
            ItemSet itemSet = new ItemSet("MythicIronSet", Rarity.MYTHIC);
            itemSet.addItem(Material.IRON_HELMET, "Mythic iron helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.THORNS, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }});
            itemSet.addItem(Material.IRON_CHESTPLATE, "Mythic iron chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.THORNS, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }});
            itemSet.addItem(Material.IRON_LEGGINGS, "Mythic iron leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.THORNS, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }});
            itemSet.addItem(Material.IRON_BOOTS, "Mythic iron boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.THORNS, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Mythic iron shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.THORNS, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 2);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 2);
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
                put(PotionEffectType.WATER_BREATHING, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.HEALTH_BOOST, 3);
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.SPEED, 1);
                put(PotionEffectType.WATER_BREATHING, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("MythicGoldSet", Rarity.MYTHIC);
            itemSet.addItem(Material.GOLD_HELMET, "Mythic gold helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.WATER_WORKER, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addItem(Material.GOLD_CHESTPLATE, "Mythic gold chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addItem(Material.GOLD_LEGGINGS, "Mythic gold leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addItem(Material.GOLD_BOOTS, "Mythic gold boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addItem(Material.SHIELD, "Mythic gold shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.OXYGEN, 2);
                put(Enchantment.PROTECTION_PROJECTILE, 2);
                put(Enchantment.THORNS, 1);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 1);
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 2);
                put(PotionEffectType.HEALTH_BOOST, 1);
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("MythicDiamondSet", Rarity.MYTHIC);
            itemSet.addItem(Material.DIAMOND_HELMET, "Mythic diamond helmet", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 2);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.PROTECTION_FALL, 2);
            }});
            itemSet.addItem(Material.DIAMOND_CHESTPLATE, "Mythic diamond chestplate", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 2);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.PROTECTION_FALL, 2);
            }});
            itemSet.addItem(Material.DIAMOND_LEGGINGS, "Mythic diamond leggings", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 2);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.PROTECTION_FALL, 2);
            }});
            itemSet.addItem(Material.DIAMOND_BOOTS, "Mythic diamond boots", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 2);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 2);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.PROTECTION_FALL, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Mythic diamond shield", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.DURABILITY, 3);
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                put(Enchantment.PROTECTION_EXPLOSIONS, 2);
                put(Enchantment.PROTECTION_FIRE, 2);
                put(Enchantment.PROTECTION_FALL, 2);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 2);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 1);
                put(PotionEffectType.FAST_DIGGING, 2);
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.DAMAGE_RESISTANCE, 2);
                put(PotionEffectType.FAST_DIGGING, 3);
                put(PotionEffectType.NIGHT_VISION, 1);
                put(PotionEffectType.SPEED, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        /*
         * GODLIKE
         */
        {
            ItemSet itemSet = new ItemSet("FireSet", Rarity.GODLIKE);
            itemSet.addItem(Material.DIAMOND_HELMET, "Helmet of fire", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.PROTECTION_FIRE, 4);
            }});
            itemSet.addItem(Material.DIAMOND_CHESTPLATE, "Chestplate of fire", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.PROTECTION_FIRE, 4);
            }});
            itemSet.addItem(Material.DIAMOND_LEGGINGS, "Leggings of fire", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.PROTECTION_FIRE, 4);
            }});
            itemSet.addItem(Material.DIAMOND_BOOTS, "Boots of fire", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.PROTECTION_FIRE, 4);
            }});
            itemSet.addItem(Material.SHIELD, "Shield of fire", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.PROTECTION_FIRE, 4);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FIRE_RESISTANCE, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("EarthSet", Rarity.GODLIKE);
            itemSet.addItem(Material.DIAMOND_HELMET, "Helmet of earth", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
            }});
            itemSet.addItem(Material.DIAMOND_CHESTPLATE, "Chestplate of earth", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
            }});
            itemSet.addItem(Material.DIAMOND_LEGGINGS, "Leggings of earth", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
            }});
            itemSet.addItem(Material.DIAMOND_BOOTS, "Boots of earth", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
            }});
            itemSet.addItem(Material.SHIELD, "Shield of earth", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.REGENERATION, 1);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }
        {
            ItemSet itemSet = new ItemSet("WindSet", Rarity.GODLIKE);
            itemSet.addItem(Material.DIAMOND_HELMET, "Helmet of wind", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.LOOT_BONUS_BLOCKS, 2);
            }});
            itemSet.addItem(Material.DIAMOND_CHESTPLATE, "Chestplate of wind", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.LOOT_BONUS_BLOCKS, 2);
            }});
            itemSet.addItem(Material.DIAMOND_LEGGINGS, "Leggings of wind", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.LOOT_BONUS_BLOCKS, 2);
            }});
            itemSet.addItem(Material.DIAMOND_BOOTS, "Boots of wind", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.LOOT_BONUS_BLOCKS, 2);
            }});
            itemSet.addItem(Material.SHIELD, "Shield of wind", new LinkedHashMap<Enchantment, Integer>(){{
                put(Enchantment.LOOT_BONUS_BLOCKS, 2);
            }});
            itemSet.addEffect(2, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 1);
            }});
            itemSet.addEffect(3, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 2);
            }});
            itemSet.addEffect(4, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 3);
            }});
            itemSet.addEffect(5, new LinkedHashMap<PotionEffectType, Integer>(){{
                put(PotionEffectType.FAST_DIGGING, 4);
            }});
            itemSet.buildLoreFromEffects();
            sets.add(itemSet);
        }

        for (ItemSet itemSet : sets) {
            Rarity rarity = itemSet.getRarity();

            raritySets.computeIfAbsent(rarity, k -> new ArrayList<>());
            raritySets.get(rarity).add(itemSet);

            for (CustomItemStack itemStack : itemSet.getItems()) {
                itemSets.put(itemStack, itemSet);
            }
        }
    }
}
