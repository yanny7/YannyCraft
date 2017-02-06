package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

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

    private static final EnumSet<EntityType> ARMOR_WEARERS = EnumSet.of(EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.SKELETON, EntityType.HUSK, EntityType.WITHER_SKELETON);
    private static final EnumSet<EntityType> SWORD_WEARERS = EnumSet.of(EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.HUSK, EntityType.WITHER_SKELETON);

    private static final String BOSS_SPAWN_RATE = "boss_spawn_rate";
    private static final String BOSS_DROP_CHANCE = "boss_drop_chance";
    private static final String BOSS_ARMORED_HEALTH = "boss_armored_health";
    private static final String BOSS_HEALTH = "boss_health";
    private static final String BOSS_ENCHANTMENT_CHANCE = "boss_enchantment_chance";
    private static final String BOSS_DEATH_EXP = "boss_death_exp";

    private static final String CONFIGURATION_NAME = "rpg";
    private static final String TRANSLATION_SECTION = "translation";
    private static final String BOSS_SECTION = "boss";
    private static final String STAT_SECTION = "stat_names";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private RewardWrapper rewardWrapper;
    private Map<String, String> translationMap = new HashMap<>();

    private double bossSpawnRate = 0.1;
    private double bossDropChance = 0.25;
    private int bossArmoredHealth = 40;
    private int bossHealth = 100;
    private double bossEnchantmentChance = 0.1;
    private int bossDeathExp = 100;

    private Plugin plugin;
    private PlayerConfiguration playerConfiguration;
    private Random random;

    RpgConfiguration(Plugin plugin, PlayerConfiguration playerConfiguration) {
        this.plugin = plugin;
        this.playerConfiguration = playerConfiguration;
        random = new Random();

        translationMap.put("msg_reward", "Dostal si odmenu!");
        translationMap.put("msg_stats", "Hodnotenie");

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
        rewardWrapper = new RewardWrapper(serverConfigurationWrapper, plugin);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection bossSection = serverConfigurationWrapper.getConfigurationSection(BOSS_SECTION);
        if (bossSection == null) {
            bossSection = serverConfigurationWrapper.createSection(BOSS_SECTION);
        }
        bossSpawnRate = bossSection.getDouble(BOSS_SPAWN_RATE, bossSpawnRate);
        bossDropChance = bossSection.getDouble(BOSS_DROP_CHANCE, bossDropChance);
        bossArmoredHealth = bossSection.getInt(BOSS_ARMORED_HEALTH, bossArmoredHealth);
        bossHealth = bossSection.getInt(BOSS_HEALTH, bossHealth);
        bossEnchantmentChance = bossSection.getDouble(BOSS_ENCHANTMENT_CHANCE, bossEnchantmentChance);
        bossDeathExp = bossSection.getInt(BOSS_DEATH_EXP, bossDeathExp);

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        translationMap.putAll(ServerConfigurationWrapper.convertMapString(translationSection.getValues(false)));

        ConfigurationSection statsSection = serverConfigurationWrapper.getConfigurationSection(STAT_SECTION);
        if (statsSection == null) {
            statsSection = serverConfigurationWrapper.createSection(STAT_SECTION);
        }
        for (Map.Entry<String, Object> pair : statsSection.getValues(false).entrySet()) {
            RewardWrapper.RewardType rewardType;

            try {
                rewardType = RewardWrapper.RewardType.valueOf(pair.getKey());
            } catch (Exception e) {
                plugin.getLogger().warning("RpgConfiguration.load: cant cast to RewardType: " + pair.getKey());
                continue;
            }

            if (!(pair.getValue() instanceof String)) {
                plugin.getLogger().warning("RpgConfiguration.load: value is not a String: " + pair.getValue());
                continue;
            }

            rewardType.setDisplayName((String)pair.getValue());
        }

        rewardWrapper.load();
        save(); // save defaults
    }

    private void save() {
        ConfigurationSection bossSection = serverConfigurationWrapper.getConfigurationSection(BOSS_SECTION);
        bossSection.set(BOSS_SPAWN_RATE, bossSpawnRate);
        bossSection.set(BOSS_DROP_CHANCE, bossDropChance);
        bossSection.set(BOSS_ARMORED_HEALTH, bossArmoredHealth);
        bossSection.set(BOSS_HEALTH, bossHealth);
        bossSection.set(BOSS_ENCHANTMENT_CHANCE, bossEnchantmentChance);
        bossSection.set(BOSS_DEATH_EXP, bossDeathExp);

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        for (HashMap.Entry<String, String> pair : translationMap.entrySet()) {
            translationSection.set(pair.getKey(), pair.getValue());
        }

        ConfigurationSection statsSection = serverConfigurationWrapper.getConfigurationSection(STAT_SECTION);
        for (RewardWrapper.RewardType rewardType : RewardWrapper.RewardType.values()) {
            statsSection.set(rewardType.name(), rewardType.getDisplayName());
        }

        rewardWrapper.save();
        serverConfigurationWrapper.save();
    }

    String getTranslation(String key) {
        return translationMap.get(key);
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
            } else {
                monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossHealth);
                monster.setHealth(bossHealth);
            }

            //monster.setGlowing(true); // can cause rendering problem for some settings
            monster.setCanPickupItems(true);
            monster.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0, false, true), true);
        }
    }

    void checkForReward(Player player) {
        for (RewardWrapper.RewardType type : RewardWrapper.RewardType.values()) {
            ItemStack reward = rewardWrapper.getReward(type, playerConfiguration.getStatistic(player, type));

            if (reward != null) {
                player.getInventory().addItem(reward);
                player.sendMessage(ChatColor.GREEN + translationMap.get("msg_reward"));
                playerConfiguration.incrementStatistic(player, type);
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
