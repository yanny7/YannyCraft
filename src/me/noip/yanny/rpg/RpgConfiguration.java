package me.noip.yanny.rpg;

import me.noip.yanny.utils.ServerConfigurationWrapper;
import me.noip.yanny.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

class RpgConfiguration {

    static final String T_MSG_STATS = "msg_stats";
    static final String T_MSG_LEVEL = "msg_level";
    static final String T_MSG_XP = "msg_xp";
    static final String T_MSG_NEXT_LEVEL_XP = "msg_next_lvl_xp";
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
    static final String T_RPG_SALVAGE = "rpg_salvage";
    static final String T_RPG_SMELTING = "rpg_smelting";

    private static final String CONFIGURATION_NAME = "rpg";
    private static final String TRANSLATION_SECTION = "translation";

    private static final String EXP_MINING_SECTION = "mining";
    private static final String EXP_EXCAVATION_SECTION = "excavation";
    private static final String EXP_WOODCUTTING_SECTION = "woodcutting";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private Map<String, String> translationMap = new HashMap<>();
    private Map<Material, Integer> miningExp = new HashMap<>();
    private Map<Material, Integer> excavationExp = new HashMap<>();
    private Map<Material, Integer> woodcuttingExp = new HashMap<>();

    private Plugin plugin;

    RpgConfiguration(Plugin plugin) {
        this.plugin = plugin;

        miningExp.put(Material.SANDSTONE, 30);
        miningExp.put(Material.PRISMARINE, 30);
        miningExp.put(Material.RED_SANDSTONE, 30);
        miningExp.put(Material.ENDER_STONE, 30);
        miningExp.put(Material.OBSIDIAN, 30);
        miningExp.put(Material.GLOWSTONE, 30);
        miningExp.put(Material.STAINED_CLAY, 30);
        miningExp.put(Material.HARD_CLAY, 30);
        miningExp.put(Material.MOSSY_COBBLESTONE, 30);
        miningExp.put(Material.NETHERRACK, 50);
        miningExp.put(Material.STONE, 30);
        miningExp.put(Material.PURPUR_BLOCK, 50);
        miningExp.put(Material.COAL_ORE, 50);
        miningExp.put(Material.IRON_ORE, 50);
        miningExp.put(Material.GOLD_ORE, 60);
        miningExp.put(Material.DIAMOND_ORE, 70);
        miningExp.put(Material.EMERALD_ORE, 80);
        miningExp.put(Material.LAPIS_ORE, 80);
        miningExp.put(Material.QUARTZ_ORE, 80);
        miningExp.put(Material.REDSTONE_ORE, 80);
        excavationExp.put(Material.SAND, 20);
        excavationExp.put(Material.CLAY, 20);
        excavationExp.put(Material.DIRT, 20);
        excavationExp.put(Material.GRASS, 20);
        excavationExp.put(Material.GRAVEL, 20);
        excavationExp.put(Material.MYCEL, 20);
        excavationExp.put(Material.SOUL_SAND, 20);
        woodcuttingExp.put(Material.LOG, 30);

        translationMap.put(T_MSG_STATS, "RPG Statistiky");
        translationMap.put(T_MSG_LEVEL, "Level");
        translationMap.put(T_MSG_XP, "Xp");
        translationMap.put(T_MSG_NEXT_LEVEL_XP, "Xp na dalsi lvl");
        translationMap.put(T_RPG_MINING, "Tazenie");
        translationMap.put(T_RPG_EXCAVATION, "Kopanie");
        translationMap.put(T_RPG_WOODCUTTING, "Rubanie");
        translationMap.put(T_RPG_HERBALISM, "Pestovanie");
        translationMap.put(T_RPG_FISHING, "Rybarenie");
        translationMap.put(T_RPG_UNARMED, "Pestny boj");
        translationMap.put(T_RPG_ARCHERY, "Lukostrelba");
        translationMap.put(T_RPG_SWORDS, "Boj mecom");
        translationMap.put(T_RPG_AXES, "Boj sekerou");
        translationMap.put(T_RPG_TAMING, "Krotenie zvierat");
        translationMap.put(T_RPG_REPAIR, "Opravovanie");
        translationMap.put(T_RPG_ACROBATICS, "Akrobacia");
        translationMap.put(T_RPG_ALCHEMY, "Alchymia");
        translationMap.put(T_RPG_SALVAGE, "Rozoberanie");
        translationMap.put(T_RPG_SMELTING, "Tavenie");

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

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        translationMap.putAll(Utils.convertMapString(translationSection.getValues(false)));

        save(); // save defaults
    }

    private void save() {
        ConfigurationSection miningSection = serverConfigurationWrapper.getConfigurationSection(EXP_MINING_SECTION);
        for (Map.Entry<Material, Integer> pair : miningExp.entrySet()) {
            miningSection.set(pair.getKey().name(), pair.getValue());
        }

        ConfigurationSection excavationSection = serverConfigurationWrapper.getConfigurationSection(EXP_EXCAVATION_SECTION);
        for (Map.Entry<Material, Integer> pair : excavationExp.entrySet()) {
            excavationSection.set(pair.getKey().name(), pair.getValue());
        }

        ConfigurationSection woodcuttingSection = serverConfigurationWrapper.getConfigurationSection(EXP_WOODCUTTING_SECTION);
        for (Map.Entry<Material, Integer> pair : woodcuttingExp.entrySet()) {
            woodcuttingSection.set(pair.getKey().name(), pair.getValue());
        }

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        for (Map.Entry<String, String> pair : translationMap.entrySet()) {
            translationSection.set(pair.getKey(), pair.getValue());
        }

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
}
