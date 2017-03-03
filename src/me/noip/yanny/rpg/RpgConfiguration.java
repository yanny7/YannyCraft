package me.noip.yanny.rpg;

import me.noip.yanny.utils.ServerConfigurationWrapper;
import me.noip.yanny.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionType;

import java.util.LinkedHashMap;
import java.util.Map;

class RpgConfiguration {

    static final String T_MSG_STATS = "msg_stats";
    static final String T_MSG_LEVEL = "msg_level";
    static final String T_MSG_XP = "msg_xp";
    static final String T_MSG_NEXT_LEVEL_XP = "msg_next_lvl_xp";
    static final String T_MSG_LEVELUP = "msg_levelup";

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

    private static final String REPAIR_XP = "repair_xp";
    private static final String ACROBATICS_XP = "acrobatics_xp";

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

    RpgConfiguration(Plugin plugin) {
        miningExp.put(Material.SANDSTONE, 20);
        miningExp.put(Material.NETHERRACK, 20);
        miningExp.put(Material.STONE, 30);
        miningExp.put(Material.RED_SANDSTONE, 30);
        miningExp.put(Material.PRISMARINE, 30);
        miningExp.put(Material.HARD_CLAY, 40);
        miningExp.put(Material.STAINED_CLAY, 40);
        miningExp.put(Material.ENDER_STONE, 50);
        miningExp.put(Material.MOSSY_COBBLESTONE, 60);
        miningExp.put(Material.OBSIDIAN, 80);
        miningExp.put(Material.GLOWSTONE, 80);
        miningExp.put(Material.QUARTZ_ORE, 100);
        miningExp.put(Material.PURPUR_BLOCK, 100);
        miningExp.put(Material.COAL_ORE, 100);
        miningExp.put(Material.REDSTONE_ORE, 150);
        miningExp.put(Material.IRON_ORE, 200);
        miningExp.put(Material.LAPIS_ORE, 300);
        miningExp.put(Material.GOLD_ORE, 400);
        miningExp.put(Material.DIAMOND_ORE, 500);
        miningExp.put(Material.EMERALD_ORE, 1000);

        excavationExp.put(Material.SAND, 30);
        excavationExp.put(Material.DIRT, 30);
        excavationExp.put(Material.GRASS, 30);
        excavationExp.put(Material.GRAVEL, 50);
        excavationExp.put(Material.SOUL_SAND, 80);
        excavationExp.put(Material.CLAY, 100);
        excavationExp.put(Material.MYCEL, 200);

        woodcuttingExp.put(Material.LOG, 30);

        herbalismExp.put(Material.CROPS, 30);
        herbalismExp.put(Material.COCOA, 40);
        herbalismExp.put(Material.POTATO, 50);
        herbalismExp.put(Material.CARROT, 50);
        herbalismExp.put(Material.MELON_STEM, 60);
        herbalismExp.put(Material.BEETROOT_BLOCK, 60);
        herbalismExp.put(Material.PUMPKIN, 70);
        herbalismExp.put(Material.NETHER_WARTS, 100);
        herbalismExp.put(Material.CHORUS_FLOWER, 150);

        fishingExp.put(Rarity.SCRAP, 20);
        fishingExp.put(Rarity.COMMON, 30);
        fishingExp.put(Rarity.UNCOMMON, 50);
        fishingExp.put(Rarity.RARE, 100);
        fishingExp.put(Rarity.EXOTIC, 200);
        fishingExp.put(Rarity.HEROIC, 400);
        fishingExp.put(Rarity.EPIC, 600);
        fishingExp.put(Rarity.LEGENDARY, 1000);
        fishingExp.put(Rarity.MYTHIC, 5000);
        fishingExp.put(Rarity.GODLIKE, 10000);

        // passive mobs
        damageExp.put(EntityType.VILLAGER, 10);
        damageExp.put(EntityType.BAT, 20);
        damageExp.put(EntityType.CHICKEN, 20);
        damageExp.put(EntityType.COW, 20);
        damageExp.put(EntityType.PIG, 20);
        damageExp.put(EntityType.RABBIT, 20);
        damageExp.put(EntityType.SHEEP, 20);
        damageExp.put(EntityType.SQUID, 20);
        damageExp.put(EntityType.SKELETON_HORSE, 100);
        // neutral mobs
        damageExp.put(EntityType.CAVE_SPIDER, 50);
        damageExp.put(EntityType.POLAR_BEAR, 50);
        damageExp.put(EntityType.SPIDER, 50);
        damageExp.put(EntityType.ENDERMAN, 60);
        damageExp.put(EntityType.PIG_ZOMBIE, 100);
        // hostile mobs
        damageExp.put(EntityType.ZOMBIE, 50);
        damageExp.put(EntityType.SLIME, 50);
        damageExp.put(EntityType.HUSK, 50);
        damageExp.put(EntityType.CREEPER, 50);
        damageExp.put(EntityType.SILVERFISH, 50);
        damageExp.put(EntityType.SKELETON, 50);
        damageExp.put(EntityType.WITCH, 50);
        damageExp.put(EntityType.ZOMBIE_HORSE, 50);
        damageExp.put(EntityType.ZOMBIE_VILLAGER, 50);
        damageExp.put(EntityType.MAGMA_CUBE, 100);
        damageExp.put(EntityType.BLAZE, 100);
        damageExp.put(EntityType.ENDERMITE, 100);
        damageExp.put(EntityType.GHAST, 100);
        damageExp.put(EntityType.STRAY, 100);
        damageExp.put(EntityType.GUARDIAN, 100);
        damageExp.put(EntityType.VEX, 100);
        damageExp.put(EntityType.VINDICATOR, 100);
        damageExp.put(EntityType.EVOKER, 150);
        damageExp.put(EntityType.WITHER_SKELETON, 150);
        damageExp.put(EntityType.SHULKER, 150);
        damageExp.put(EntityType.ELDER_GUARDIAN, 200);
        // tameable mobs
        damageExp.put(EntityType.DONKEY, 20);
        damageExp.put(EntityType.HORSE, 20);
        damageExp.put(EntityType.MULE, 20);
        damageExp.put(EntityType.OCELOT, 20);
        damageExp.put(EntityType.LLAMA, 30);
        damageExp.put(EntityType.WOLF, 50);
        // boss mobs
        damageExp.put(EntityType.ENDER_DRAGON, 450);
        damageExp.put(EntityType.WITHER, 500);
        // utility mobs
        damageExp.put(EntityType.IRON_GOLEM, 30);
        damageExp.put(EntityType.SNOWMAN, 30);

        tameExp.put(EntityType.HORSE, 100);
        tameExp.put(EntityType.DONKEY, 200);
        tameExp.put(EntityType.MULE, 300);
        tameExp.put(EntityType.LLAMA, 250);
        tameExp.put(EntityType.OCELOT, 500);
        tameExp.put(EntityType.WOLF, 100);

        repairExp = 50;

        acrobaticsExp = 50; // per hearth

        alchemyExp.put(PotionType.MUNDANE, 10);
        alchemyExp.put(PotionType.THICK, 10);
        alchemyExp.put(PotionType.AWKWARD, 10);
        alchemyExp.put(PotionType.NIGHT_VISION, 50);
        alchemyExp.put(PotionType.INVISIBILITY, 50);
        alchemyExp.put(PotionType.JUMP, 50);
        alchemyExp.put(PotionType.FIRE_RESISTANCE, 100);
        alchemyExp.put(PotionType.SPEED, 50);
        alchemyExp.put(PotionType.SLOWNESS, 50);
        alchemyExp.put(PotionType.WATER_BREATHING, 50);
        alchemyExp.put(PotionType.INSTANT_HEAL, 50);
        alchemyExp.put(PotionType.INSTANT_DAMAGE, 50);
        alchemyExp.put(PotionType.POISON, 50);
        alchemyExp.put(PotionType.REGEN, 100);
        alchemyExp.put(PotionType.STRENGTH, 50);
        alchemyExp.put(PotionType.WEAKNESS, 50);
        alchemyExp.put(PotionType.LUCK, 50);

        // food
        smeltingExp.put(Material.GRILLED_PORK, 30);
        smeltingExp.put(Material.COOKED_BEEF, 30);
        smeltingExp.put(Material.COOKED_CHICKEN, 30);
        smeltingExp.put(Material.COOKED_FISH, 30);
        smeltingExp.put(Material.BAKED_POTATO, 30);
        smeltingExp.put(Material.COOKED_MUTTON, 30);
        smeltingExp.put(Material.COOKED_RABBIT, 30);
        // ore and material
        smeltingExp.put(Material.STONE, 30);
        smeltingExp.put(Material.GLASS, 40);
        smeltingExp.put(Material.NETHER_BRICK_ITEM, 40);
        smeltingExp.put(Material.CLAY_BRICK, 50);
        smeltingExp.put(Material.IRON_INGOT, 50);
        smeltingExp.put(Material.GOLD_INGOT, 70);
        smeltingExp.put(Material.SMOOTH_BRICK, 80);
        smeltingExp.put(Material.HARD_CLAY, 100);
        // wasting ores
        smeltingExp.put(Material.COAL, 30);
        smeltingExp.put(Material.DIAMOND, 200);
        smeltingExp.put(Material.INK_SACK, 200); //LAPIS LAZULI
        smeltingExp.put(Material.REDSTONE, 200);
        smeltingExp.put(Material.EMERALD, 200);
        smeltingExp.put(Material.QUARTZ, 200);
        // tools
        smeltingExp.put(Material.IRON_NUGGET, 100);
        smeltingExp.put(Material.GOLD_NUGGET, 100);
        // other
        smeltingExp.put(Material.SPONGE, 100);
        smeltingExp.put(Material.CHORUS_FRUIT_POPPED, 150);

        translationMap.put(T_MSG_STATS, "RPG Statistiky");
        translationMap.put(T_MSG_LEVEL, "Level");
        translationMap.put(T_MSG_XP, "Xp");
        translationMap.put(T_MSG_NEXT_LEVEL_XP, "Xp na dalsi lvl");
        translationMap.put(T_MSG_LEVELUP, "Tvoj skill '{STATS_TYPE}' sa zvysil na level {LEVEL} ({LEVEL_DIFF})");

        translationMap.put(T_RPG_MINING, RpgPlayerStatsType.MINING.getDisplayName());
        translationMap.put(T_RPG_EXCAVATION, RpgPlayerStatsType.EXCAVATION.getDisplayName());
        translationMap.put(T_RPG_WOODCUTTING, RpgPlayerStatsType.WOODCUTTING.getDisplayName());
        translationMap.put(T_RPG_HERBALISM, RpgPlayerStatsType.HERBALISM.getDisplayName());
        translationMap.put(T_RPG_FISHING, RpgPlayerStatsType.FISHING.getDisplayName());
        translationMap.put(T_RPG_UNARMED, RpgPlayerStatsType.UNARMED.getDisplayName());
        translationMap.put(T_RPG_ARCHERY, RpgPlayerStatsType.ARCHERY.getDisplayName());
        translationMap.put(T_RPG_SWORDS, RpgPlayerStatsType.SWORDS.getDisplayName());
        translationMap.put(T_RPG_AXES, RpgPlayerStatsType.AXES.getDisplayName());
        translationMap.put(T_RPG_TAMING, RpgPlayerStatsType.TAMING.getDisplayName());
        translationMap.put(T_RPG_REPAIR, RpgPlayerStatsType.REPAIR.getDisplayName());
        translationMap.put(T_RPG_ACROBATICS, RpgPlayerStatsType.ACROBATICS.getDisplayName());
        translationMap.put(T_RPG_ALCHEMY, RpgPlayerStatsType.ALCHEMY.getDisplayName());
        translationMap.put(T_RPG_SMELTING, RpgPlayerStatsType.SMELTING.getDisplayName());

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

        ConfigurationSection herbalismSection = serverConfigurationWrapper.getConfigurationSection(EXP_HERBALISM_SECTION);
        for (Map.Entry<Material, Integer> pair : herbalismExp.entrySet()) {
            herbalismSection.set(pair.getKey().name(), pair.getValue());
        }

        ConfigurationSection fishingSection = serverConfigurationWrapper.getConfigurationSection(EXP_FISHING_SECTION);
        for (Map.Entry<Rarity, Integer> pair : fishingExp.entrySet()) {
            fishingSection.set(pair.getKey().name(), pair.getValue());
        }

        ConfigurationSection damageSection = serverConfigurationWrapper.getConfigurationSection(EXP_DAMAGE_SECTION);
        for (Map.Entry<EntityType, Integer> pair : damageExp.entrySet()) {
            damageSection.set(pair.getKey().name(), pair.getValue());
        }

        ConfigurationSection tameSection = serverConfigurationWrapper.getConfigurationSection(EXP_TAME_SECTION);
        for (Map.Entry<EntityType, Integer> pair : tameExp.entrySet()) {
            tameSection.set(pair.getKey().name(), pair.getValue());
        }

        ConfigurationSection repairSection = serverConfigurationWrapper.getConfigurationSection(EXP_REPAIR_SECTION);
        repairSection.set(REPAIR_XP, repairExp);

        ConfigurationSection acrobaticsSection = serverConfigurationWrapper.getConfigurationSection(EXP_ACROBATICS_SECTION);
        acrobaticsSection.set(ACROBATICS_XP, acrobaticsExp);

        ConfigurationSection alchemySection = serverConfigurationWrapper.getConfigurationSection(EXP_ALCHEMY_SECTION);
        for (Map.Entry<PotionType, Integer> pair : alchemyExp.entrySet()) {
            alchemySection.set(pair.getKey().name(), pair.getValue());
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
}
