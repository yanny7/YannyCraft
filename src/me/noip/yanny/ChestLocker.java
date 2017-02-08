package me.noip.yanny;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

class ChestLocker {

    private JavaPlugin plugin;
    private PlayerConfiguration playerConfiguration;
    private ChestConfiguration chestConfiguration;

    ChestLocker(JavaPlugin plugin, PlayerConfiguration playerConfiguration) {
        this.plugin = plugin;
        this.playerConfiguration = playerConfiguration;
        chestConfiguration = new ChestConfiguration(plugin);
    }

    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new ChestLockerListener(), plugin);
        plugin.getCommand("chest").setExecutor(new ChestExecutor());
        chestConfiguration.load();
    }

    void onDisable() {
        chestConfiguration.save();
    }

    private class ChestExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;

            return true;
        }
    }

    private class ChestLockerListener implements Listener {

        @EventHandler
        void onChestBroken(BlockBreakEvent event) {
            if (event.getBlock().getType() == Material.CHEST) {
                Player player = event.getPlayer();
                String blockLocation = ChestConfiguration.locationToString(event.getBlock().getLocation());
                List<String> playerChests = playerConfiguration.getChestLocations(player);
                List<String> allChests = chestConfiguration.getChestsLocation();
                boolean isOwner = playerChests.contains(blockLocation);
                boolean isFree = !allChests.contains(blockLocation);

                if (!isFree) {
                    if (isOwner) {
                        playerChests.remove(blockLocation);
                        allChests.remove(blockLocation);
                        player.sendMessage("Your chest removed from protection!");
                    } else {
                        event.setCancelled(true);
                        player.sendMessage("This chest is protected!");
                    }
                }
            }
        }

        @EventHandler
        void onChestCreated(BlockPlaceEvent event) {
            Block block = event.getBlockPlaced();

            if (block.getType() == Material.CHEST) {
                List<Block> around = new ArrayList<>();
                around.add(block.getRelative(1, 0, 0));
                around.add(block.getRelative(-1, 0, 0));
                around.add(block.getRelative(0, 0, 1));
                around.add(block.getRelative(0, 0, -1));

                for (Block second : around) {
                    if (second.getType() == Material.CHEST) {
                        Player player = event.getPlayer();
                        String blockLocation = ChestConfiguration.locationToString(second.getLocation());
                        List<String> playerChests = playerConfiguration.getChestLocations(player);
                        List<String> allChests = chestConfiguration.getChestsLocation();
                        boolean isOwner = playerChests.contains(blockLocation);
                        boolean isFree = !allChests.contains(blockLocation);

                        if (!isFree) {
                            if (isOwner) {
                                String chestLocation = ChestConfiguration.locationToString(block.getLocation());
                                playerChests.add(chestLocation);
                                allChests.add(chestLocation);
                                player.sendMessage("Second chest added to protection!");
                            } else {
                                event.setCancelled(true);
                                player.sendMessage("This chest nearby is protected!");
                            }

                            break;
                        }
                    }
                }
            }
        }

        @EventHandler
        void onChestClick(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            Action action = event.getAction();
            Block block = event.getClickedBlock();

            if ((block != null) && (block.getType() == Material.CHEST)) {
                Location chestLocation = block.getLocation();
                String blockLocation = ChestConfiguration.locationToString(chestLocation);
                List<String> playerChests = playerConfiguration.getChestLocations(player);
                List<String> allChests = chestConfiguration.getChestsLocation();
                boolean isOwner = playerChests.contains(blockLocation);
                boolean isFree = !allChests.contains(blockLocation);

                if (isFree) {
                    ItemStack itemStack = player.getInventory().getItemInMainHand();

                    if (player.isSneaking() && (action == Action.RIGHT_CLICK_BLOCK) && (itemStack.getType() == Material.EMERALD)) {
                        playerChests.add(blockLocation);
                        allChests.add(blockLocation);
                        player.sendMessage("Chest is now yours!");

                        List<Block> around = new ArrayList<>();
                        around.add(block.getRelative(1, 0, 0));
                        around.add(block.getRelative(-1, 0, 0));
                        around.add(block.getRelative(0, 0, 1));
                        around.add(block.getRelative(0, 0, -1));

                        for (Block second : around) {
                            if (second.getType() == Material.CHEST) {
                                String secondLocation = ChestConfiguration.locationToString(second.getLocation());
                                playerChests.add(secondLocation);
                                allChests.add(secondLocation);
                                player.sendMessage("Double chest detected and added!");
                                break;
                            }
                        }

                        return;
                    }

                    plugin.getLogger().warning("Chest opened - its free");
                } else if (isOwner) {
                    plugin.getLogger().warning("Chest opened - its yours");
                } else {
                    if (player.isSneaking() && (action == Action.RIGHT_CLICK_BLOCK) && (player.getInventory().getItemInMainHand().getType() == Material.EMERALD)) {
                        plugin.getLogger().warning("Chest locked - lockpicking");
                        event.setCancelled(true);
                    } else {
                        player.sendMessage("Chest is locked!");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
