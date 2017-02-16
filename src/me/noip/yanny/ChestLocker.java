package me.noip.yanny;

import org.bukkit.ChatColor;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

class ChestLocker {

    private JavaPlugin plugin;
    private ChestConfiguration chestConfiguration;
    private Random random = new Random();

    ChestLocker(JavaPlugin plugin) {
        this.plugin = plugin;
        chestConfiguration = new ChestConfiguration(plugin);
    }

    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new ChestLockerListener(), plugin);
        plugin.getCommand("chest").setExecutor(new ChestExecutor());
        chestConfiguration.load();
    }

    void onDisable() {
        chestConfiguration.getChestLocations().clear();
    }

    private class ChestExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            //Player player = (Player) commandSender;

            return true;
        }
    }

    private class ChestLockerListener implements Listener {

        @EventHandler
        void onChestBroken(BlockBreakEvent event) {
            if (event.getBlock().getType() == Material.CHEST) {
                Player player = event.getPlayer();
                String blockLocation = ChestConfiguration.locationToString(event.getBlock().getLocation());
                Map<String, String> allChests = chestConfiguration.getChestLocations();
                String ownerUUID = allChests.get(blockLocation);
                boolean isFree = ownerUUID == null;
                boolean isOwner = !isFree && ownerUUID.equals(player.getUniqueId().toString());

                if (!isFree) {
                    if (isOwner) {
                        allChests.remove(blockLocation);
                        chestConfiguration.save();
                        player.sendMessage(ChatColor.GREEN + chestConfiguration.getTranslation("msg_chest_destroyed"));
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + chestConfiguration.getTranslation("msg_chest_protected"));
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
                        Map<String, String> allChests = chestConfiguration.getChestLocations();
                        String ownerUUID = allChests.get(blockLocation);
                        boolean isFree = ownerUUID == null;
                        boolean isOwner = !isFree && ownerUUID.equals(player.getUniqueId().toString());

                        if (!isFree) {
                            if (isOwner) {
                                String chestLocation = ChestConfiguration.locationToString(block.getLocation());
                                allChests.put(chestLocation, player.getUniqueId().toString());
                                chestConfiguration.save();
                            } else {
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.RED + chestConfiguration.getTranslation("msg_chest_not_owned"));
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

            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                return;
            }

            if ((block != null) && (block.getType() == Material.CHEST)) {
                Location chestLocation = block.getLocation();
                String blockLocation = ChestConfiguration.locationToString(chestLocation);
                Map<String, String> allChests = chestConfiguration.getChestLocations();
                String ownerUUID = allChests.get(blockLocation);
                boolean isFree = ownerUUID == null;
                boolean isOwner = !isFree && ownerUUID.equals(player.getUniqueId().toString());

                if (isFree) {
                    ItemStack itemStack = player.getInventory().getItemInMainHand();

                    if (player.isSneaking() && (action == Action.RIGHT_CLICK_BLOCK) && (itemStack.getType() == Material.EMERALD)) {
                        allChests.put(blockLocation, player.getUniqueId().toString());
                        player.sendMessage(ChatColor.GREEN + chestConfiguration.getTranslation("msg_chest_lock"));

                        List<Block> around = new ArrayList<>();
                        around.add(block.getRelative(1, 0, 0));
                        around.add(block.getRelative(-1, 0, 0));
                        around.add(block.getRelative(0, 0, 1));
                        around.add(block.getRelative(0, 0, -1));

                        for (Block second : around) {
                            if (second.getType() == Material.CHEST) {
                                String secondLocation = ChestConfiguration.locationToString(second.getLocation());
                                allChests.put(secondLocation, player.getUniqueId().toString());
                                break;
                            }
                        }

                        if (itemStack.getAmount() > 1) {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        }

                        event.setCancelled(true);
                        chestConfiguration.save();
                    }
                } else if (!isOwner) {
                    if (player.isSneaking() && (action == Action.RIGHT_CLICK_BLOCK) && (player.getInventory().getItemInMainHand().getType() == Material.EMERALD)) {
                        ItemStack itemStack = player.getInventory().getItemInMainHand();

                        if (itemStack.getAmount() > 1) {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        }

                        if (random.nextDouble() < chestConfiguration.getLockpickingChance()) {
                            player.sendMessage(ChatColor.GREEN + chestConfiguration.getTranslation("msg_chest_unlocked"));
                            allChests.remove(blockLocation);

                            List<Block> around = new ArrayList<>();
                            around.add(block.getRelative(1, 0, 0));
                            around.add(block.getRelative(-1, 0, 0));
                            around.add(block.getRelative(0, 0, 1));
                            around.add(block.getRelative(0, 0, -1));

                            for (Block second : around) {
                                if (second.getType() == Material.CHEST) {
                                    String secondLocation = ChestConfiguration.locationToString(second.getLocation());
                                    allChests.remove(secondLocation);
                                    break;
                                }
                            }

                            chestConfiguration.save();
                        } else {
                            player.sendMessage(ChatColor.RED + chestConfiguration.getTranslation("msg_chest_lockpicking"));
                            event.setCancelled(true);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + chestConfiguration.getTranslation("msg_chest_locked"));
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
