package me.noip.yanny.rpg;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import me.noip.yanny.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.*;

class RpgConfiguration {

    static final String T_MSG_STATS = "msg_stats";
    static final String T_MSG_LEVEL = "msg_level";
    static final String T_MSG_XP = "msg_xp";
    static final String T_MSG_NEXT_LEVEL_XP = "msg_next_lvl_xp";
    static final String T_MSG_LEVELUP = "msg_levelup";
    static final String T_MSG_ABILITIES = "msg_abilities";
    static final String T_MSG_TREASURE_FOUND = "msg_treasure_found";
    static final String T_MSG_CRITICAL_DAMAGE = "msg_critical_damage";
    static final String T_MSG_DAMAGE_REDUCED = "msg_damage_reduced";

    static final String T_RPG_MINING = "rpg_mining";
    static final String T_RPG_EXCAVATION = "rpg_excavation";
    static final String T_RPG_WOODCUTTING = "rpg_woodcutting";
    static final String T_RPG_HERBALISM = "rpg_herbalism";
    static final String T_RPG_FISHING = "rpg_fishing";
    static final String T_RPG_UNARMED = "rpg_unarmed";
    static final String T_RPG_ARCHERY = "rpg_archery";
    static final String T_RPG_SWORDS = "rpg_swords";
    static final String T_RPG_AXES = "rpg_axes";
    static final String T_RPG_TAMING = "rpg_taming";
    static final String T_RPG_REPAIR = "rpg_repair";
    static final String T_RPG_ACROBATICS = "rpg_acrobatics";
    static final String T_RPG_ALCHEMY = "rpg_alchemy";
    static final String T_RPG_SMELTING = "rpg_smelting";

    static final String T_RAR_SCRAP = "rar_scrap";
    static final String T_RAR_COMMON = "rar_common";
    static final String T_RAR_UNCOMMON = "rar_uncommon";
    static final String T_RAR_RARE = "rar_rare";
    static final String T_RAR_EXOTIC = "rar_exotic";
    static final String T_RAR_HEROIC = "rar_heroic";
    static final String T_RAR_EPIC = "rar_epic";
    static final String T_RAR_LEGENDARY = "rar_legendary";
    static final String T_RAR_MYTHIC = "rar_mythic";
    static final String T_RAR_GODLIKE = "rar_godlike";

    private static final String CONFIGURATION_NAME = "rpg";
    private static final String TRANSLATION_SECTION = "translation";
    private static final String TREASURE_SECTION = "treasure";

    private static final String EXP_MINING_SECTION = "mining";
    private static final String EXP_EXCAVATION_SECTION = "excavation";
    private static final String EXP_WOODCUTTING_SECTION = "woodcutting";
    private static final String EXP_HERBALISM_SECTION = "herbalism";
    private static final String EXP_FISHING_SECTION = "fishing";
    private static final String EXP_DAMAGE_SECTION = "damage";
    private static final String EXP_TAME_SECTION = "tame";
    private static final String EXP_REPAIR_SECTION = "repair";
    private static final String EXP_ACROBATICS_SECTION = "acrobatics";
    private static final String EXP_ALCHEMY_SECTION = "alchemy";
    private static final String EXP_SMELTING_SECTION = "smelting";

    private static final String REPAIR_XP = "repair_xp";
    private static final String ACROBATICS_XP = "acrobatics_xp";

    private MainPlugin plugin;
    private ServerConfigurationWrapper serverConfigurationWrapper;
    private Map<String, String> translationMap = new LinkedHashMap<>();
    private Map<Material, Integer> miningExp = new LinkedHashMap<>();
    private Map<Material, Integer> excavationExp = new LinkedHashMap<>();
    private Map<Material, Integer> woodcuttingExp = new LinkedHashMap<>();
    private Map<Material, Integer> herbalismExp = new LinkedHashMap<>();
    private Map<Rarity, Integer> fishingExp = new LinkedHashMap<>();
    private Map<EntityType, Integer> damageExp = new LinkedHashMap<>();
    private Map<EntityType, Integer> tameExp = new LinkedHashMap<>();
    private int repairExp;
    private int acrobaticsExp;
    private Map<PotionType, Integer> alchemyExp = new LinkedHashMap<>();
    private Map<Material, Integer> smeltingExp = new LinkedHashMap<>();
    private Map<Rarity, List<ItemStack>> treasureItems = new LinkedHashMap<>();

    RpgConfiguration(MainPlugin plugin) {
        this.plugin = plugin;

        MiningSkill.loadDefaults(miningExp);
        ExcavationSkill.loadDefaults(excavationExp);
        WoodcuttingSkill.loadDefaults(woodcuttingExp);
        HerbalismSkill.loadDefaults(herbalismExp);
        FishingSkill.loadDefaults(fishingExp);
        SwordsSkill.loadDefaults(damageExp);
        TamingSkill.loadDefaults(tameExp);
        AlchemySkill.loadDefaults(alchemyExp);
        SmeltingSkill.loadDefaults(smeltingExp);
        Rarity.loadDefaults(treasureItems);

        repairExp = 50;
        acrobaticsExp = 50; // per hearth

        translationMap.put(T_MSG_STATS, "RPG Statistiky");
        translationMap.put(T_MSG_LEVEL, "Level");
        translationMap.put(T_MSG_XP, "Xp");
        translationMap.put(T_MSG_NEXT_LEVEL_XP, "Xp na dalsi lvl");
        translationMap.put(T_MSG_LEVELUP, "Tvoj skill '{STATS_TYPE}' sa zvysil na level {LEVEL} ({LEVEL_DIFF})");
        translationMap.put(T_MSG_ABILITIES, "Schopnosti");
        translationMap.put(T_MSG_TREASURE_FOUND, "Nasiel si poklad");
        translationMap.put(T_MSG_CRITICAL_DAMAGE, "Sposobil si kriticky utok");
        translationMap.put(T_MSG_DAMAGE_REDUCED, "Damage bol znizeny o");

        translationMap.put(T_RPG_MINING, SkillType.MINING.getDisplayName());
        translationMap.put(T_RPG_EXCAVATION, SkillType.EXCAVATION.getDisplayName());
        translationMap.put(T_RPG_WOODCUTTING, SkillType.WOODCUTTING.getDisplayName());
        translationMap.put(T_RPG_HERBALISM, SkillType.HERBALISM.getDisplayName());
        translationMap.put(T_RPG_FISHING, SkillType.FISHING.getDisplayName());
        translationMap.put(T_RPG_UNARMED, SkillType.UNARMED.getDisplayName());
        translationMap.put(T_RPG_ARCHERY, SkillType.ARCHERY.getDisplayName());
        translationMap.put(T_RPG_SWORDS, SkillType.SWORDS.getDisplayName());
        translationMap.put(T_RPG_AXES, SkillType.AXES.getDisplayName());
        translationMap.put(T_RPG_TAMING, SkillType.TAMING.getDisplayName());
        translationMap.put(T_RPG_REPAIR, SkillType.REPAIR.getDisplayName());
        translationMap.put(T_RPG_ACROBATICS, SkillType.ACROBATICS.getDisplayName());
        translationMap.put(T_RPG_ALCHEMY, SkillType.ALCHEMY.getDisplayName());
        translationMap.put(T_RPG_SMELTING, SkillType.SMELTING.getDisplayName());

        translationMap.put(T_RAR_SCRAP, Rarity.SCRAP.getDisplayName());
        translationMap.put(T_RAR_COMMON, Rarity.COMMON.getDisplayName());
        translationMap.put(T_RAR_UNCOMMON, Rarity.UNCOMMON.getDisplayName());
        translationMap.put(T_RAR_RARE, Rarity.RARE.getDisplayName());
        translationMap.put(T_RAR_EXOTIC, Rarity.EXOTIC.getDisplayName());
        translationMap.put(T_RAR_HEROIC, Rarity.HEROIC.getDisplayName());
        translationMap.put(T_RAR_EPIC, Rarity.EPIC.getDisplayName());
        translationMap.put(T_RAR_LEGENDARY, Rarity.LEGENDARY.getDisplayName());
        translationMap.put(T_RAR_MYTHIC, Rarity.MYTHIC.getDisplayName());
        translationMap.put(T_RAR_GODLIKE, Rarity.GODLIKE.getDisplayName());

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection miningSection = serverConfigurationWrapper.getConfigurationSection(EXP_MINING_SECTION);
        if (miningSection == null) {
            miningSection = serverConfigurationWrapper.createSection(EXP_MINING_SECTION);
        }
        miningExp.putAll(Utils.convertMapMaterialInteger(miningSection.getValues(false)));

        ConfigurationSection excavationSection = serverConfigurationWrapper.getConfigurationSection(EXP_EXCAVATION_SECTION);
        if (excavationSection == null) {
            excavationSection = serverConfigurationWrapper.createSection(EXP_EXCAVATION_SECTION);
        }
        excavationExp.putAll(Utils.convertMapMaterialInteger(excavationSection.getValues(false)));

        ConfigurationSection woodcuttingSection = serverConfigurationWrapper.getConfigurationSection(EXP_WOODCUTTING_SECTION);
        if (woodcuttingSection == null) {
            woodcuttingSection = serverConfigurationWrapper.createSection(EXP_WOODCUTTING_SECTION);
        }
        woodcuttingExp.putAll(Utils.convertMapMaterialInteger(woodcuttingSection.getValues(false)));

        ConfigurationSection herbalismSection = serverConfigurationWrapper.getConfigurationSection(EXP_HERBALISM_SECTION);
        if (herbalismSection == null) {
            herbalismSection = serverConfigurationWrapper.createSection(EXP_HERBALISM_SECTION);
        }
        herbalismExp.putAll(Utils.convertMapMaterialInteger(herbalismSection.getValues(false)));

        ConfigurationSection fishingSection = serverConfigurationWrapper.getConfigurationSection(EXP_FISHING_SECTION);
        if (fishingSection == null) {
            fishingSection = serverConfigurationWrapper.createSection(EXP_FISHING_SECTION);
        }
        fishingExp.putAll(Utils.convertMapRarityInteger(fishingSection.getValues(false)));

        ConfigurationSection damageSection = serverConfigurationWrapper.getConfigurationSection(EXP_DAMAGE_SECTION);
        if (damageSection == null) {
            damageSection = serverConfigurationWrapper.createSection(EXP_DAMAGE_SECTION);
        }
        damageExp.putAll(Utils.convertMapEntityTypeInteger(damageSection.getValues(false)));

        ConfigurationSection tameSection = serverConfigurationWrapper.getConfigurationSection(EXP_TAME_SECTION);
        if (tameSection == null) {
            tameSection = serverConfigurationWrapper.createSection(EXP_TAME_SECTION);
        }
        tameExp.putAll(Utils.convertMapEntityTypeInteger(tameSection.getValues(false)));

        ConfigurationSection repairSection = serverConfigurationWrapper.getConfigurationSection(EXP_REPAIR_SECTION);
        if (repairSection == null) {
            repairSection = serverConfigurationWrapper.createSection(EXP_REPAIR_SECTION);
        }
        repairExp = repairSection.getInt(REPAIR_XP, repairExp);

        ConfigurationSection acrobaticsSection = serverConfigurationWrapper.getConfigurationSection(EXP_ACROBATICS_SECTION);
        if (acrobaticsSection == null) {
            acrobaticsSection = serverConfigurationWrapper.createSection(EXP_ACROBATICS_SECTION);
        }
        acrobaticsExp = acrobaticsSection.getInt(ACROBATICS_XP, acrobaticsExp);

        ConfigurationSection alchemySection = serverConfigurationWrapper.getConfigurationSection(EXP_ALCHEMY_SECTION);
        if (alchemySection == null) {
            alchemySection = serverConfigurationWrapper.createSection(EXP_ALCHEMY_SECTION);
        }
        alchemyExp.putAll(Utils.convertMapPotionTypeInteger(alchemySection.getValues(false)));

        ConfigurationSection smeltingSection = serverConfigurationWrapper.getConfigurationSection(EXP_SMELTING_SECTION);
        if (smeltingSection == null) {
            smeltingSection = serverConfigurationWrapper.createSection(EXP_SMELTING_SECTION);
        }
        smeltingExp.putAll(Utils.convertMapMaterialInteger(smeltingSection.getValues(false)));

        ConfigurationSection treasureSection = serverConfigurationWrapper.getConfigurationSection(TREASURE_SECTION);
        if (treasureSection == null) {
            treasureSection = serverConfigurationWrapper.createSection(TREASURE_SECTION);
        }
        for (Rarity rarity : Rarity.values()) {
            List<String> items = treasureSection.getStringList(rarity.name());
            for (String item : items) {
                String[] tokens = item.split(" ");
                Material material = Material.AIR;
                short subtype = 0;

                if (tokens.length > 2) {
                    plugin.getLogger().warning("RpgConfiguration.load: Cant load ItemStack '" + item + "'");
                    continue;
                }

                try {
                    material = Material.valueOf(tokens[0]);
                    if (tokens.length == 2) {
                        subtype = Short.parseShort(tokens[1]);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("RpgConfiguration.load: Error: " + e.getLocalizedMessage());
                }

                ItemStack itemStack = new ItemStack(material, 1, subtype);
                List<ItemStack> rarityItems = treasureItems.get(rarity);
                boolean duplicate = false;

                for (ItemStack it : rarityItems) {
                    if ((it.getType() == itemStack.getType()) && (it.getData().getData() == itemStack.getData().getData())) {
                        duplicate = true;
                        break;
                    }
                }


                if (!duplicate) {
                    rarityItems.add(itemStack);
                }
            }
        }

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        translationMap.putAll(Utils.convertMapString(translationSection.getValues(false)));

        save(); // save defaults
    }

    private void save() {
        ConfigurationSection miningSection = serverConfigurationWrapper.getConfigurationSection(EXP_MINING_SECTION);
        miningExp.forEach(((material, value) -> miningSection.set(material.name(), value)));

        ConfigurationSection excavationSection = serverConfigurationWrapper.getConfigurationSection(EXP_EXCAVATION_SECTION);
        excavationExp.forEach(((material, value) -> excavationSection.set(material.name(), value)));

        ConfigurationSection woodcuttingSection = serverConfigurationWrapper.getConfigurationSection(EXP_WOODCUTTING_SECTION);
        woodcuttingExp.forEach(((material, value) -> woodcuttingSection.set(material.name(), value)));

        ConfigurationSection herbalismSection = serverConfigurationWrapper.getConfigurationSection(EXP_HERBALISM_SECTION);
        herbalismExp.forEach(((material, value) -> herbalismSection.set(material.name(), value)));

        ConfigurationSection fishingSection = serverConfigurationWrapper.getConfigurationSection(EXP_FISHING_SECTION);
        fishingExp.forEach(((rarity, value) -> fishingSection.set(rarity.name(), value)));

        ConfigurationSection damageSection = serverConfigurationWrapper.getConfigurationSection(EXP_DAMAGE_SECTION);
        damageExp.forEach(((type, value) -> damageSection.set(type.name(), value)));

        ConfigurationSection tameSection = serverConfigurationWrapper.getConfigurationSection(EXP_TAME_SECTION);
        tameExp.forEach(((type, value) -> tameSection.set(type.name(), value)));

        ConfigurationSection repairSection = serverConfigurationWrapper.getConfigurationSection(EXP_REPAIR_SECTION);
        repairSection.set(REPAIR_XP, repairExp);

        ConfigurationSection acrobaticsSection = serverConfigurationWrapper.getConfigurationSection(EXP_ACROBATICS_SECTION);
        acrobaticsSection.set(ACROBATICS_XP, acrobaticsExp);

        ConfigurationSection alchemySection = serverConfigurationWrapper.getConfigurationSection(EXP_ALCHEMY_SECTION);
        alchemyExp.forEach(((type, value) -> alchemySection.set(type.name(), value)));

        ConfigurationSection smeltingSection = serverConfigurationWrapper.getConfigurationSection(EXP_SMELTING_SECTION);
        smeltingExp.forEach(((type, value) -> smeltingSection.set(type.name(), value)));

        ConfigurationSection treasureSection = serverConfigurationWrapper.getConfigurationSection(TREASURE_SECTION);
        for (Map.Entry<Rarity, List<ItemStack>> entry : treasureItems.entrySet()) {
            List<String> items = new LinkedList<>();
            for (ItemStack itemStack : entry.getValue()) {
                String item = itemStack.getType().name();

                if (itemStack.getData().getData() != 0) {
                    item += " " + itemStack.getData().getData();
                }

                items.add(item);
            }
            treasureSection.set(entry.getKey().name(), items);
        }

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        translationMap.forEach(translationSection::set);

        serverConfigurationWrapper.save();
    }

    String getTranslation(String key) {
        return translationMap.get(key);
    }

    int getMiningExp(Material material) {
        Integer result = miningExp.get(material);

        if (result != null) {
            return result;
        } else {
            return -1;
        }
    }

    int getExcavationExp(Material material) {
        Integer result = excavationExp.get(material);

        if (result != null) {
            return result;
        } else {
            return -1;
        }
    }

    int getWoodcuttingExp(Material material) {
        Integer result = woodcuttingExp.get(material);

        if (result != null) {
            return result;
        } else {
            return -1;
        }
    }

    int getHerbalismExp(Material material) {
        Integer result = herbalismExp.get(material);

        if (result != null) {
            return result;
        } else {
            return -1;
        }
    }

    int getFishingExp(Rarity rarity) {
        Integer result = fishingExp.get(rarity);

        if (result != null) {
            return result;
        } else {
            return -1;
        }
    }

    int getDamageExp(EntityType entityType) {
        Integer result = damageExp.get(entityType);

        if (result != null) {
            return result;
        } else {
            return -1;
        }
    }

    int getTameExp(EntityType entityType) {
        Integer result = tameExp.get(entityType);

        if (result != null) {
            return result;
        } else {
            return -1;
        }
    }

    int getRepairExp(int cost) {
        return repairExp * cost;
    }

    int getAcrobaticExp(double damage) {
        return (int)(Math.floor((acrobaticsExp * damage) / 10) * 10); // round to ten`s
    }

    int getPotionExp(PotionType potionType) {
        Integer result = alchemyExp.get(potionType);

        if (result != null) {
            return result;
        } else {
            return -1;
        }
    }

    int getSmeltingExp(Material material, int count) {
        Integer result = smeltingExp.get(material);

        if (result != null) {
            return result * count;
        } else {
            return -1;
        }
    }

    List<ItemStack> getTreasure(Rarity rarity) {
        List<ItemStack> result = treasureItems.get(rarity);

        if (result != null) {
            return result;
        } else {
            return new ArrayList<>();
        }
    }

}
