package me.noip.yanny.rpg;

import me.noip.yanny.auth.PlayerRegisterEvent;
import me.noip.yanny.utils.PartPlugin;
import me.noip.yanny.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RPG implements PartPlugin {

    private JavaPlugin plugin;
    private Connection connection;
    private RpgConfiguration rpgConfiguration;
    private Map<UUID, RpgPlayer> rpgPlayerMap = new HashMap<>();
    private RpgBoard rpgBoard;

    public RPG(JavaPlugin plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;

        rpgConfiguration = new RpgConfiguration(plugin);
        rpgBoard = new RpgBoard(plugin, rpgConfiguration, rpgPlayerMap);
    }

    @Override
    public void onEnable() {
        RpgPlayerStatsType.MINING.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_MINING));
        RpgPlayerStatsType.EXCAVATION.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_EXCAVATION));
        RpgPlayerStatsType.WOODCUTTING.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_WOODCUTTING));
        RpgPlayerStatsType.HERBALISM.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_HERBALISM));
        RpgPlayerStatsType.FISHING.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_FISHING));
        RpgPlayerStatsType.UNARMED.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_UNARMED));
        RpgPlayerStatsType.ARCHERY.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_ARCHERY));
        RpgPlayerStatsType.SWORDS.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_SWORDS));
        RpgPlayerStatsType.AXES.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_AXES));
        RpgPlayerStatsType.TAMING.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_TAMING));
        RpgPlayerStatsType.REPAIR.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_REPAIR));
        RpgPlayerStatsType.ACROBATICS.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_ACROBATICS));
        RpgPlayerStatsType.ALCHEMY.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_ALCHEMY));
        RpgPlayerStatsType.SALVAGE.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_SALVAGE));
        RpgPlayerStatsType.SMELTING.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RPG_SMELTING));

        Rarity.SCRAP.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RAR_SCRAP));
        Rarity.COMMON.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RAR_COMMON));
        Rarity.UNCOMMON.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RAR_UNCOMMON));
        Rarity.RARE.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RAR_RARE));
        Rarity.EXOTIC.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RAR_EXOTIC));
        Rarity.HEROIC.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RAR_HEROIC));
        Rarity.EPIC.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RAR_EPIC));
        Rarity.LEGENDARY.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RAR_LEGENDARY));
        Rarity.MYTHIC.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RAR_MYTHIC));
        Rarity.GODLIKE.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_RAR_GODLIKE));

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            rpgPlayerMap.put(player.getUniqueId(), new RpgPlayer(plugin, player, connection, rpgConfiguration, rpgBoard));
        }

        rpgConfiguration.load();
        rpgBoard.onEnable();
        plugin.getServer().getPluginManager().registerEvents(new RpgListener(), plugin);
        plugin.getCommand("stats").setExecutor(new StatsExecutor());
    }

    @Override
    public void onDisable() {
        rpgBoard.onDisable();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            RpgPlayer rpgPlayer = rpgPlayerMap.remove(player.getUniqueId());

            if (rpgPlayer == null) {
                continue;
            }

            rpgPlayer.onQuit();
        }
    }

    class StatsExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;
            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                plugin.getLogger().warning("RPG.PlayerQuitEvent: Player not found!" + player.getDisplayName());
                return true;
            }

            ItemStack book = rpgPlayer.getStatsBook();
            Utils.openBook(book, player);

            return true;
        }
    }

    class RpgListener implements Listener {
        @EventHandler
        void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            RpgPlayer rpgPlayer = new RpgPlayer(plugin, player, connection, rpgConfiguration, rpgBoard);
            rpgPlayerMap.put(player.getUniqueId(), rpgPlayer);
        }

        @EventHandler
        void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            RpgPlayer rpgPlayer = rpgPlayerMap.remove(player.getUniqueId());

            if (rpgPlayer == null) {
                plugin.getLogger().warning("RPG.PlayerQuitEvent: Player not found!" + player.getDisplayName());
                return;
            }

            rpgPlayer.onQuit();
        }

        @EventHandler
        void onPlayerRegister(PlayerRegisterEvent event) {
            Player player = event.getPlayer();
            RpgPlayer.registerPlayer(connection, player);
        }

        @EventHandler
        void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                plugin.getLogger().warning("RPG.onBlockBreak: Player not found!" + player.getDisplayName());
                return;
            }

            rpgPlayer.blockBreak(event);
        }

        @EventHandler
        void onCatchFish(PlayerFishEvent event) {
            Player player = event.getPlayer();
            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                plugin.getLogger().warning("RPG.onCatchFish: Player not found!" + player.getDisplayName());
                return;
            }

            rpgPlayer.catchFish(event);
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
                    //playerConfiguration.incrementStatistic(player, RewardWrapper.RewardType.SWORD);
                    //rpgBoard.updateObjective(RewardWrapper.RewardType.SWORD, player, playerConfiguration.getStatistic(player, RewardWrapper.RewardType.SWORD));
                    //rpgConfiguration.checkForReward(player);
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
