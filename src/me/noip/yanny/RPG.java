package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

class RPG {

    private JavaPlugin plugin;
    private PlayerConfiguration playerConfiguration;
    private RpgConfiguration rpgConfiguration;
    private RpgBoard rpgBoard;

    RPG(JavaPlugin plugin, PlayerConfiguration playerConfiguration) {
        this.plugin = plugin;
        this.playerConfiguration = playerConfiguration;

        rpgConfiguration = new RpgConfiguration(plugin, playerConfiguration);
        rpgBoard = new RpgBoard(plugin, playerConfiguration, rpgConfiguration);
    }

    void onEnable() {
        rpgConfiguration.load();
        rpgBoard.onEnable();
        plugin.getServer().getPluginManager().registerEvents(new RpgListener(), plugin);
    }

    void onDisable() {
        rpgBoard.onDisable();
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
                    rpgBoard.updateObjective(RewardWrapper.RewardType.PICKAXE, player, playerConfiguration.getStatistic(player, RewardWrapper.RewardType.PICKAXE));
                    break;
                case WOOD_SPADE:
                case STONE_SPADE:
                case IRON_SPADE:
                case GOLD_SPADE:
                case DIAMOND_SPADE:
                    playerConfiguration.incrementStatistic(player, RewardWrapper.RewardType.SPADE);
                    rpgBoard.updateObjective(RewardWrapper.RewardType.SPADE, player, playerConfiguration.getStatistic(player, RewardWrapper.RewardType.SPADE));
                    break;
                case WOOD_AXE:
                case STONE_AXE:
                case IRON_AXE:
                case GOLD_AXE:
                case DIAMOND_AXE:
                    playerConfiguration.incrementStatistic(player, RewardWrapper.RewardType.AXE);
                    rpgBoard.updateObjective(RewardWrapper.RewardType.AXE, player, playerConfiguration.getStatistic(player, RewardWrapper.RewardType.AXE));
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
                    rpgBoard.updateObjective(RewardWrapper.RewardType.SWORD, player, playerConfiguration.getStatistic(player, RewardWrapper.RewardType.SWORD));
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
    }
}
