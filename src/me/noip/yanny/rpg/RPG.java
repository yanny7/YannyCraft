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
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
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
                if (event.getDamager() instanceof Arrow) {
                    Arrow arrow = (Arrow) event.getDamager();
                    if (!(arrow.getShooter() instanceof Player)) {
                        return;
                    }
                } else {
                    return;
                }
            }

            Entity damager = event.getDamager();
            Player player = null;

            if (damager instanceof Player) {
                player = (Player) damager;
            } else if (damager instanceof Arrow) {
                player = (Player) ((Arrow) damager).getShooter();
            }

            if (player == null) {
                plugin.getLogger().warning("RPG.onMobDamagedByEntity: Entity is not a player");
                return;
            }

            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                plugin.getLogger().warning("RPG.onMobDamagedByEntity: Player not found!" + player.getDisplayName());
                return;
            }

            rpgPlayer.entityDamaged(event);
        }

        @EventHandler
        void onEntityTame(EntityTameEvent event) {
            if (!(event.getOwner() instanceof Player)) {
                return;
            }

            Player player = (Player) event.getOwner();
            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                plugin.getLogger().warning("RPG.onEntityTame: Player not found!" + player.getDisplayName());
                return;
            }

            rpgPlayer.entityTame(event);
        }

        @EventHandler
        void onInventoryClick(InventoryClickEvent event) {
            if (event.getInventory() instanceof AnvilInventory) {
                InventoryView inventoryView = event.getView();
                int rawSlot = event.getRawSlot();

                if ((rawSlot != inventoryView.convertSlot(rawSlot)) || (rawSlot != 2)) {
                    return;
                }

                AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
                ItemStack[] items = anvilInventory.getContents();

                if (items[0] == null) {
                    return;
                }

                ItemMeta itemMeta = items[0].getItemMeta();

                if (itemMeta instanceof Repairable) {
                    Repairable repairable = (Repairable) itemMeta;
                    int repairCost = repairable.getRepairCost();

                    Player player = (Player) event.getWhoClicked();
                    if (player.getLevel() >= repairCost + 1) {
                        RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                        if (rpgPlayer == null) {
                            plugin.getLogger().warning("RPG.onInventoryClick: Player not found!" + player.getDisplayName());
                            return;
                        }

                        rpgPlayer.itemRepair(repairCost + 1);
                    }
                }
            } else if (event.getInventory() instanceof BrewerInventory) {
                InventoryView inventoryView = event.getView();
                int rawSlot = event.getRawSlot();

                if ((rawSlot != inventoryView.convertSlot(rawSlot)) || (rawSlot > 2)) {
                    return;
                }

                switch (event.getCurrentItem().getType()) {
                    case POTION:
                    case SPLASH_POTION:
                    case LINGERING_POTION: {
                        ItemStack potion = event.getCurrentItem();
                        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();

                        if (potionMeta.isUnbreakable()) { // disable gaining XP for every take of potion
                            return;
                        }

                        Player player = (Player) event.getWhoClicked();
                        RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                        if (rpgPlayer == null) {
                            plugin.getLogger().warning("RPG.onInventoryClick: Player not found!" + player.getDisplayName());
                            return;
                        }

                        potionMeta.setUnbreakable(true);
                        potionMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                        potion.setItemMeta(potionMeta);
                        rpgPlayer.potionCreated(potionMeta.getBasePotionData().getType(), event.getCurrentItem().getType());
                        break;
                    }
                }
            }
        }

        @EventHandler
        void onBrewPotion(BrewEvent event) {
            BrewerInventory brewerInventory = event.getContents();
            ItemStack[] items = brewerInventory.getContents();

            for (int i = 0; i < 3; i++) { // allow again get XP for alchemy
                if (items[i] != null) {
                    PotionMeta potionMeta = (PotionMeta) items[i].getItemMeta();
                    potionMeta.setUnbreakable(false);
                    items[i].setItemMeta(potionMeta);
                }
            }
        }

        @EventHandler
        void onMobDamaged(EntityDamageEvent event) {
            if ((event.getEntityType() == EntityType.PLAYER) && (event.getCause() == EntityDamageEvent.DamageCause.FALL)) {
                Player player = (Player) event.getEntity();
                RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                if (rpgPlayer == null) {
                    plugin.getLogger().warning("RPG.onMobDamaged: Player not found!" + player.getDisplayName());
                    return;
                }

                rpgPlayer.fallDamage(event.getDamage());
                return;
            }

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
