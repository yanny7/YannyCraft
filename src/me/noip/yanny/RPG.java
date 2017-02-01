package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

class RPG {

    private JavaPlugin plugin;
    private PlayerConfiguration playerConfiguration;
    private RpgListener rpgListener;
    private RpgConfiguration rpgConfiguration;

    RPG(JavaPlugin plugin, PlayerConfiguration playerConfiguration) {
        this.plugin = plugin;
        this.playerConfiguration = playerConfiguration;

        rpgListener = new RpgListener();
        rpgConfiguration = new RpgConfiguration(plugin, playerConfiguration);
    }

    void onEnable() {
        rpgConfiguration.load();
        plugin.getServer().getPluginManager().registerEvents(rpgListener, plugin);
    }

    void onDisable() {
        rpgConfiguration.save();
    }

    class RpgListener implements Listener {

        @EventHandler
        void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            Material material = player.getInventory().getItemInMainHand().getType();

            switch (material) {
                case WOOD_PICKAXE:
                case STONE_PICKAXE:
                case IRON_PICKAXE:
                case GOLD_PICKAXE:
                case DIAMOND_PICKAXE:
                    playerConfiguration.incrementStatistic(player, RewardWrapper.RewardType.PICKAXE);
                    break;
                case WOOD_SPADE:
                case STONE_SPADE:
                case IRON_SPADE:
                case GOLD_SPADE:
                case DIAMOND_SPADE:
                    playerConfiguration.incrementStatistic(player, RewardWrapper.RewardType.SPADE);
                    break;
                case WOOD_AXE:
                case STONE_AXE:
                case IRON_AXE:
                case GOLD_AXE:
                case DIAMOND_AXE:
                    playerConfiguration.incrementStatistic(player, RewardWrapper.RewardType.AXE);
                    break;
            }

            rpgConfiguration.checkForReward(player);
        }

        @EventHandler
        void onMobDamagedByEntity(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player)) {
                return;
            }

            Player player = (Player)event.getDamager();

            if (event.getEntity() instanceof Monster) {
                Monster monster = (Monster)event.getEntity();

                if (monster.getHealth() - event.getFinalDamage() <= 0) {
                    playerConfiguration.incrementStatistic(player, RewardWrapper.RewardType.SWORD);
                    rpgConfiguration.checkForReward(player);
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
