package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

class RPG {

    private static final String RPG_PICKAXE = RewardWrapper.RewardType.PICKAXE.name().toLowerCase();
    private static final String RPG_SPADE = RewardWrapper.RewardType.SPADE.name().toLowerCase();
    private static final String RPG_AXE = RewardWrapper.RewardType.AXE.name().toLowerCase();
    private static final String RPG_SWORD = RewardWrapper.RewardType.SWORD.name().toLowerCase();

    private JavaPlugin plugin;
    private PlayerConfiguration playerConfiguration;
    private ConfigurationSection section;
    private RewardWrapper rewardWrapper;
    private RpgListener rpgListener;
    private RpgConfiguration rpgConfiguration;

    RPG(JavaPlugin plugin, PlayerConfiguration playerConfiguration) {
        this.plugin = plugin;
        this.playerConfiguration = playerConfiguration;
        FileConfiguration configuration = plugin.getConfig();

        section = configuration.getConfigurationSection("rpg");
        if (section == null) {
            section = configuration.createSection("rpg");
        }
        section.addDefault("msg_reward", "Dostal si odmenu!");

        buildRewardSystem(configuration);

        rewardWrapper = new RewardWrapper(configuration.getConfigurationSection("rpg_reward"), plugin);
        rpgListener = new RpgListener();
        rpgConfiguration = new RpgConfiguration(plugin, configuration);
    }

    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(rpgListener, plugin);
    }

    void onDisable() {
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

    private void checkForReward(ConfigurationSection status, Player player) {
        for (RewardWrapper.RewardType type : RewardWrapper.RewardType.values()) {
            String rewardType = type.name().toLowerCase();
            ItemStack reward = rewardWrapper.getReward(type, status.getInt(rewardType));

            if (reward != null) {
                player.getInventory().addItem(reward);
                player.sendMessage(ChatColor.GREEN + section.getString("msg_reward"));
                status.set(rewardType, status.getInt(rewardType, 0) + 1);
            }
        }
    }

    class RpgListener implements Listener {
        @EventHandler
        void onPlayerAuth(PlayerAuthEvent event) {
            Player player = event.getPlayer();
            PlayerConfigurationWrapper configuration = playerConfiguration.getConfiguration(player);

            if (configuration == null) {
                plugin.getLogger().warning("onPlayerRegister: Configuration not found!" + player.getDisplayName());
                return;
            }

            ConfigurationSection section = configuration.getConfigurationSection("rpg");
            if (section == null) {
                section = configuration.createSection("rpg");
                section.set(RPG_PICKAXE, 0);
                section.set(RPG_SPADE, 0);
                section.set(RPG_AXE, 0);
                section.set(RPG_SWORD, 0);
            }
        }

        @EventHandler
        void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            Material material = player.getInventory().getItemInMainHand().getType();
            PlayerConfigurationWrapper configuration = playerConfiguration.getConfiguration(player);

            if (configuration == null) {
                plugin.getLogger().warning("onBlockBreak: Configuration not found!" + player.getDisplayName());
                return;
            }

            ConfigurationSection status = configuration.getConfigurationSection("rpg");

            switch (material) {
                case WOOD_PICKAXE:
                case STONE_PICKAXE:
                case IRON_PICKAXE:
                case GOLD_PICKAXE:
                case DIAMOND_PICKAXE:
                    status.set(RPG_PICKAXE, status.getInt(RPG_PICKAXE, 0) + 1);
                    break;
                case WOOD_SPADE:
                case STONE_SPADE:
                case IRON_SPADE:
                case GOLD_SPADE:
                case DIAMOND_SPADE:
                    status.set(RPG_SPADE, status.getInt(RPG_SPADE, 0) + 1);
                    break;
                case WOOD_AXE:
                case STONE_AXE:
                case IRON_AXE:
                case GOLD_AXE:
                case DIAMOND_AXE:
                    status.set(RPG_AXE, status.getInt(RPG_AXE, 0) + 1);
                    break;
            }

            checkForReward(status, player);
        }

        @EventHandler
        void onMobDamagedByEntity(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player)) {
                return;
            }

            Player player = (Player)event.getDamager();
            PlayerConfigurationWrapper configuration = playerConfiguration.getConfiguration(player);

            if (configuration == null) {
                plugin.getLogger().warning("onMobKilled: Configuration not found!" + player.getDisplayName());
                return;
            }

            ConfigurationSection status = configuration.getConfigurationSection("rpg");

            if (event.getEntity() instanceof Monster) {
                Monster monster = (Monster)event.getEntity();

                if (monster.getHealth() - event.getFinalDamage() <= 0) {
                    status.set(RPG_SWORD, status.getInt(RPG_SWORD, 0) + 1);
                    checkForReward(status, player);
                }
            }
        }

        @EventHandler
        void onMobDamaged(EntityDamageEvent event) {
            if (event.getEntity() instanceof Monster) {
                Monster monster = (Monster)event.getEntity();

                if ((monster.getHealth() - event.getFinalDamage()) < monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                    monster.setCustomName(ChatColor.YELLOW + "" + (int)Math.ceil(monster.getHealth() - event.getFinalDamage()) + "/" + (int)monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                    monster.setCustomNameVisible(true);
                }
            }
        }

        @EventHandler
        void OnMobDeath(EntityDeathEvent event) {
            rpgConfiguration.bossDeathDrop(event);
        }

        @EventHandler
        void onMobSpawned(CreatureSpawnEvent event) {
            if (!(event.getEntity() instanceof Monster)) {
                return;
            }

            rpgConfiguration.createBoss((Monster) event.getEntity(), event.getSpawnReason());
        }
    }
}
