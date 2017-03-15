package me.noip.yanny.boss;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.armorset.ItemSet;
import me.noip.yanny.utils.CustomItemStack;
import me.noip.yanny.utils.LoggerHandler;
import me.noip.yanny.utils.Rarity;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

class BossConfiguration {

    static final Map<Integer, Rarity> RARITY_TO_HEALTH_MAP = new HashMap<Integer, Rarity>() {{
        put(50, Rarity.SCRAP);
        put(70, Rarity.COMMON);
        put(100, Rarity.UNCOMMON);
        put(150, Rarity.RARE);
        put(200, Rarity.EXOTIC);
        put(300, Rarity.HEROIC);
        put(400, Rarity.EPIC);
        put(600, Rarity.LEGENDARY);
        put(1000, Rarity.MYTHIC);
        put(2000, Rarity.GODLIKE);
    }};

    private static final Enchantment[] ARMOR_ENCHANTMENT = {
            Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_EXPLOSIONS,
            Enchantment.PROTECTION_PROJECTILE, Enchantment.PROTECTION_FIRE, Enchantment.THORNS, Enchantment.DURABILITY
    };
    private static final Enchantment[] HELMET_ENCHANTMENT = {
            Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_EXPLOSIONS,
            Enchantment.PROTECTION_PROJECTILE, Enchantment.PROTECTION_FIRE, Enchantment.THORNS, Enchantment.DURABILITY,
            Enchantment.OXYGEN, Enchantment.WATER_WORKER
    };
    private static final Enchantment[] BOOTS_ENCHANTMENT = {
            Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.DURABILITY,
            Enchantment.PROTECTION_FALL, Enchantment.PROTECTION_PROJECTILE, Enchantment.PROTECTION_FIRE, Enchantment.THORNS,
            Enchantment.DEPTH_STRIDER, Enchantment.FROST_WALKER
    };
    private static final Enchantment[] SWORD_ENCHANTMENT = {
            Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD, Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK,
            Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS, Enchantment.SWEEPING_EDGE, Enchantment.DURABILITY
    };
    private static final Enchantment[] BOW_ENCHANTMENT = {
            Enchantment.DURABILITY, Enchantment.ARROW_DAMAGE, Enchantment.ARROW_KNOCKBACK, Enchantment.ARROW_FIRE,
            Enchantment.ARROW_INFINITE
    };

    private static final Map<Rarity, BossStats> RARITY_BOSS_STATS = new HashMap<Rarity, BossStats>() {{
        put(Rarity.SCRAP, new BossStats(50, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.WOOD_SWORD));
        put(Rarity.COMMON, new BossStats(70, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.WOOD_SWORD));
        put(Rarity.UNCOMMON, new BossStats(100, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.IRON_SWORD));
        put(Rarity.RARE, new BossStats(150, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.IRON_SWORD));
        put(Rarity.EXOTIC, new BossStats(200, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.IRON_SWORD));
        put(Rarity.HEROIC, new BossStats(300, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.GOLD_SWORD));
        put(Rarity.EPIC, new BossStats(400, Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, Material.GOLD_SWORD));
        put(Rarity.LEGENDARY, new BossStats(600, Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, Material.DIAMOND_SWORD));
        put(Rarity.MYTHIC, new BossStats(1000, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.DIAMOND_SWORD));
        put(Rarity.GODLIKE, new BossStats(2000, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.DIAMOND_SWORD));
    }};

    private static final EnumSet<EntityType> ARMOR_WEARERS = EnumSet.of(EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.SKELETON, EntityType.HUSK, EntityType.WITHER_SKELETON);
    private static final EnumSet<EntityType> SWORD_WEARERS = EnumSet.of(EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.HUSK, EntityType.WITHER_SKELETON);

    private static final String BOSS_SPAWN_RATE = "boss_spawn_rate";
    private static final String BOSS_DROP_CHANCE = "boss_drop_chance";
    private static final String BOSS_ENCHANTMENT_CHANCE = "boss_enchantment_chance";
    private static final String BOSS_DEATH_EXP = "boss_death_exp";

    private static final String CONFIGURATION_NAME = "boss";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private MainPlugin plugin;
    private LoggerHandler logger;
    private Random random = new Random();

    private double bossSpawnRate = 0.05;
    private double bossDropChance = 0.2;
    private double bossEnchantmentChance = 0.1;
    private int bossDeathExp = 100;

    BossConfiguration(MainPlugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLoggerHandler();

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
    }

    void load() {
        serverConfigurationWrapper.load();

        bossSpawnRate = serverConfigurationWrapper.getDouble(BOSS_SPAWN_RATE, bossSpawnRate);
        bossDropChance = serverConfigurationWrapper.getDouble(BOSS_DROP_CHANCE, bossDropChance);
        bossEnchantmentChance = serverConfigurationWrapper.getDouble(BOSS_ENCHANTMENT_CHANCE, bossEnchantmentChance);
        bossDeathExp = serverConfigurationWrapper.getInt(BOSS_DEATH_EXP, bossDeathExp);

        save();
        logger.logInfo(Boss.class, String.format("Spawn rate: %.2f%%", bossSpawnRate * 100));
        logger.logInfo(Boss.class, String.format("Drop chance: %.2f%%", bossDropChance * 100));
        logger.logInfo(Boss.class, String.format("Armor enchantment chance: %.2f%%", bossEnchantmentChance * 100));
        logger.logInfo(Boss.class, String.format("Exp on boss death: %d", bossDeathExp));
    }

    private void save()  {
        serverConfigurationWrapper.set(BOSS_SPAWN_RATE, bossSpawnRate);
        serverConfigurationWrapper.set(BOSS_DROP_CHANCE, bossDropChance);
        serverConfigurationWrapper.set(BOSS_ENCHANTMENT_CHANCE, bossEnchantmentChance);
        serverConfigurationWrapper.set(BOSS_DEATH_EXP, bossDeathExp);

        serverConfigurationWrapper.save();
    }

    void bossDeathDrop(BossMonster bossMonster, EntityDeathEvent event) {
        List<ItemSet> itemSets = plugin.getArmorSet().getArmorSets(bossMonster.rarity);
        BossStats bossStats = RARITY_BOSS_STATS.get(bossMonster.rarity);

        if ((itemSets != null) && (itemSets.size() != 0)) {
            ItemSet itemSet = itemSets.get(random.nextInt(itemSets.size() - 1));

            if (itemSet != null) {
                List<ItemStack> itemStacks = event.getDrops();
                List<CustomItemStack> armorSet = itemSet.getItems();
                ItemStack itemStack = armorSet.get(random.nextInt(armorSet.size() - 1));

                itemStacks.add(itemStack);
            }
        }

        event.setDroppedExp(bossStats.health);
    }

    Rarity createBoss(Monster monster) {
        if (random.nextDouble() <= bossSpawnRate) {
            Rarity rarity = Rarity.randomRarity();
            BossStats bossStats = RARITY_BOSS_STATS.get(rarity);

            if (ARMOR_WEARERS.contains(monster.getType())) {
                EntityEquipment equipment = monster.getEquipment();
                equipment.setBoots(randomEnchantment(new ItemStack(bossStats.boots), random));
                equipment.setBootsDropChance((float)bossDropChance);
                equipment.setChestplate(randomEnchantment(new ItemStack(bossStats.chestplate), random));
                equipment.setChestplateDropChance((float)bossDropChance);
                equipment.setHelmet(randomEnchantment(new ItemStack(bossStats.helmet), random));
                equipment.setHelmetDropChance((float)bossDropChance);
                equipment.setLeggings(randomEnchantment(new ItemStack(bossStats.leggings), random));
                equipment.setLeggingsDropChance((float)bossDropChance);
                equipment.setItemInOffHand(randomEnchantment(new ItemStack(Material.SHIELD), random));
                equipment.setItemInOffHandDropChance((float)bossDropChance);

                if (SWORD_WEARERS.contains(monster.getType())) {
                    equipment.setItemInMainHand(randomEnchantment(new ItemStack(bossStats.sword), random));
                    equipment.setItemInMainHandDropChance((float)bossDropChance);
                }

                if (monster.getType() == EntityType.SKELETON) {
                    equipment.setItemInMainHand(randomEnchantment(new ItemStack(Material.BOW), random));
                    equipment.setItemInMainHandDropChance((float)bossDropChance);
                }
            }

            monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossStats.health);
            monster.setHealth(bossStats.health);
            monster.setCanPickupItems(true);
            return rarity;
        }

        return null;
    }

    private ItemStack randomEnchantment(ItemStack itemStack, Random random) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        switch (itemStack.getType()) {
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLD_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
                for (Enchantment enchantment : HELMET_ENCHANTMENT) {
                    if (random.nextDouble() <= bossEnchantmentChance) {
                        itemMeta.addEnchant(enchantment, random.nextInt() % enchantment.getMaxLevel() + 1, false);
                    }
                }
                break;
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLD_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                for (Enchantment enchantment : BOOTS_ENCHANTMENT) {
                    if (random.nextDouble() <= bossEnchantmentChance) {
                        itemMeta.addEnchant(enchantment, random.nextInt() % enchantment.getMaxLevel() + 1, false);
                    }
                }
                break;
            case STONE_SWORD:
            case DIAMOND_SWORD:
            case GOLD_SWORD:
            case IRON_SWORD:
            case WOOD_SWORD:
                for (Enchantment enchantment : SWORD_ENCHANTMENT) {
                    if (random.nextDouble() <= bossEnchantmentChance) {
                        itemMeta.addEnchant(enchantment, random.nextInt() % enchantment.getMaxLevel() + 1, false);
                    }
                }
                break;
            case SHIELD:
                if (random.nextDouble() <= bossEnchantmentChance) {
                    itemMeta.addEnchant(Enchantment.DURABILITY, random.nextInt() % Enchantment.DURABILITY.getMaxLevel() + 1, false);
                }
                break;
            case LEATHER_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLD_LEGGINGS:
            case IRON_LEGGINGS:
            case CHAINMAIL_CHESTPLATE:
            case GOLD_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                for (Enchantment enchantment : ARMOR_ENCHANTMENT) {
                    if (random.nextDouble() <= bossEnchantmentChance) {
                        itemMeta.addEnchant(enchantment, random.nextInt() % enchantment.getMaxLevel() + 1, false);
                    }
                }
                break;
            case BOW:
                for (Enchantment enchantment : BOW_ENCHANTMENT) {
                    if (random.nextDouble() <= bossEnchantmentChance) {
                        itemMeta.addEnchant(enchantment, random.nextInt() % enchantment.getMaxLevel() + 1, false);
                    }
                }
                break;
            default:
                logger.logWarn(Boss.class, "Invalid item for enchanting: " + itemStack.getType().name());
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
