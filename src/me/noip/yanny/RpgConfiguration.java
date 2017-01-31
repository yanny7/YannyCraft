package me.noip.yanny;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.Random;

class RpgConfiguration {

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

    private EnumSet<EntityType> ARMOR_WEARERS = EnumSet.of(EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.SKELETON, EntityType.HUSK, EntityType.WITHER_SKELETON);
    private EnumSet<EntityType> SWORD_WEARERS = EnumSet.of(EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.HUSK, EntityType.WITHER_SKELETON);

    private static final String BOSS_SPAWN_RATE = "boss_spawn_rate";
    private static final String BOSS_DROP_CHANCE = "boss_drop_chance";
    private static final String BOSS_ARMORED_HEALTH = "boss_armored_health";
    private static final String BOSS_HEALTH = "boss_health";
    private static final String BOSS_ENCHANTMENT_CHANCE = "boss_enchantment_chance";
    private static final String BOSS_DEATH_EXP = "boss_death_exp";

    private double bossSpawnRate = 0.1;
    private double bossDropChance = 0.25;
    private int bossArmoredHealth = 40;
    private int bossHealth = 100;
    private double bossEnchantmentChance = 0.1;
    private int bossDeathExp = 100;

    private Plugin plugin;
    private Random random;

    RpgConfiguration(Plugin plugin, ConfigurationSection configuration) {
        this.plugin = plugin;
        random = new Random();

        ConfigurationSection rewardSection = configuration.getConfigurationSection("rpg_reward");
        if (rewardSection == null) {

        } else {

        }

        ConfigurationSection bossSection = configuration.getConfigurationSection("rpg_boss");
        if (bossSection == null) {
            bossSection = configuration.createSection("rpg_boss");
            bossSection.set(BOSS_SPAWN_RATE, bossSpawnRate);
            bossSection.set(BOSS_DROP_CHANCE, bossDropChance);
            bossSection.set(BOSS_ARMORED_HEALTH, bossArmoredHealth);
            bossSection.set(BOSS_HEALTH, bossHealth);
            bossSection.set(BOSS_ENCHANTMENT_CHANCE, bossEnchantmentChance);
            bossSection.set(BOSS_DEATH_EXP, bossDeathExp);
        } else {
            bossSpawnRate = bossSection.getDouble(BOSS_SPAWN_RATE, bossSpawnRate);
            bossDropChance = bossSection.getDouble(BOSS_DROP_CHANCE, bossDropChance);
            bossArmoredHealth = bossSection.getInt(BOSS_ARMORED_HEALTH, bossArmoredHealth);
            bossHealth = bossSection.getInt(BOSS_HEALTH, bossHealth);
            bossEnchantmentChance = bossSection.getDouble(BOSS_ENCHANTMENT_CHANCE, bossEnchantmentChance);
            bossDeathExp = bossSection.getInt(BOSS_DEATH_EXP, bossDeathExp);
        }
    }

    ItemStack getReward() {
        return null;
    }

    void bossDeathDrop(EntityDeathEvent event) {
        if (event.getEntity() instanceof Monster) {
            Monster monster = (Monster)event.getEntity();

            if (monster.hasMetadata("RPG")) {
                //List<ItemStack> drops = event.getDrops();
                event.setDroppedExp(bossDeathExp);
            }
        }
    }

    void createBoss(Monster monster, CreatureSpawnEvent.SpawnReason spawnReason) {
        if ((spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL) && (random.nextDouble() <= bossSpawnRate)) {
            monster.setMetadata("RPG", new FixedMetadataValue(plugin, null));

            if (ARMOR_WEARERS.contains(monster.getType())) {
                EntityEquipment equipment = monster.getEquipment();
                equipment.setBoots(randomEnchantment(new ItemStack(Material.DIAMOND_BOOTS), random));
                equipment.setBootsDropChance((float)bossDropChance);
                equipment.setChestplate(randomEnchantment(new ItemStack(Material.DIAMOND_CHESTPLATE), random));
                equipment.setChestplateDropChance((float)bossDropChance);
                equipment.setHelmet(randomEnchantment(new ItemStack(Material.DIAMOND_HELMET), random));
                equipment.setHelmetDropChance((float)bossDropChance);
                equipment.setLeggings(randomEnchantment(new ItemStack(Material.DIAMOND_LEGGINGS), random));
                equipment.setLeggingsDropChance((float)bossDropChance);
                equipment.setItemInOffHand(randomEnchantment(new ItemStack(Material.SHIELD), random));
                equipment.setItemInOffHandDropChance((float)bossDropChance);

                if (SWORD_WEARERS.contains(monster.getType())) {
                    equipment.setItemInMainHand(randomEnchantment(new ItemStack(Material.DIAMOND_SWORD), random));
                    equipment.setItemInMainHandDropChance((float)bossDropChance);
                }

                if (monster.getType() == EntityType.SKELETON) {
                    equipment.setItemInMainHand(randomEnchantment(new ItemStack(Material.BOW), random));
                    equipment.setItemInMainHandDropChance((float)bossDropChance);
                }

                monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossArmoredHealth);
                monster.setHealth(bossArmoredHealth);
                monster.setGlowing(true);
                monster.setCanPickupItems(true);
            } else {
                monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossHealth);
                monster.setHealth(bossHealth);
                monster.setGlowing(true);
                monster.setCanPickupItems(true);
            }
        }
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
                plugin.getLogger().warning("Invalid item for enchanting: " + itemStack.getType().name());
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
