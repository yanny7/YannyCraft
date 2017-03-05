package me.noip.yanny.chestlocker;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.PartPlugin;
import me.noip.yanny.utils.Utils;
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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ChestLocker implements PartPlugin {

    private MainPlugin plugin;
    private ChestConfiguration chestConfiguration;
    private Random random = new Random();

    public ChestLocker(MainPlugin plugin) {
        this.plugin = plugin;
        chestConfiguration = new ChestConfiguration(plugin);
    }

    @Override
    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new ChestLockerListener(), plugin);
        plugin.getCommand("unlock").setExecutor(new UnlockExecutor());
        chestConfiguration.load();
    }

    @Override
    public void onDisable() {
    }

    private class UnlockExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;
            Block block = player.getTargetBlock((Set<Material>) null, 5);

            if (block.getType() == Material.CHEST) {
                String blockLocation = Utils.locationToString(block.getLocation());
                String ownerUUID = chestConfiguration.getOwner(blockLocation);
                boolean isFree = ownerUUID == null;
                boolean isOwner = !isFree && ownerUUID.equals(player.getUniqueId().toString());

                if (!isFree) {
                    if (isOwner || player.isOp()) {
                        chestConfiguration.removeChest(blockLocation);
                        player.sendMessage(ChatColor.GREEN + chestConfiguration.getTranslation("msg_chest_unlocked"));

                        List<Block> around = new ArrayList<>();
                        around.add(block.getRelative(1, 0, 0));
                        around.add(block.getRelative(-1, 0, 0));
                        around.add(block.getRelative(0, 0, 1));
                        around.add(block.getRelative(0, 0, -1));

                        for (Block second : around) {
                            if (second.getType() == Material.CHEST) {
                                String secondLocation = Utils.locationToString(second.getLocation());
                                chestConfiguration.removeChest(secondLocation);
                                break;
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + chestConfiguration.getTranslation("msg_chest_not_owned"));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + chestConfiguration.getTranslation("msg_chest_not_locked"));
                }
            } else {
                player.sendMessage(ChatColor.RED + chestConfiguration.getTranslation("msg_chest_invalid"));
            }

            return true;
        }
    }

    private class ChestLockerListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onEntityExplode(EntityExplodeEvent event) {
            List<Block> removed = new ArrayList<>(event.blockList().size());

            for (Block block : event.blockList()) {
                if (block.getType() == Material.CHEST) {
                    String blockLocation = Utils.locationToString(block.getLocation());
                    String ownerUUID = chestConfiguration.getOwner(blockLocation);

                    if (ownerUUID != null) {
                        removed.add(block);
                    }
                }
            }

            event.blockList().removeAll(removed);
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onChestBroken(BlockBreakEvent event) {
            if (event.getBlock().getType() == Material.CHEST) {
                Player player = event.getPlayer();
                String blockLocation = Utils.locationToString(event.getBlock().getLocation());
                String ownerUUID = chestConfiguration.getOwner(blockLocation);
                boolean isFree = ownerUUID == null;
                boolean isOwner = !isFree && ownerUUID.equals(player.getUniqueId().toString());

                if (!isFree) {
                    if (isOwner || player.isOp()) {
                        chestConfiguration.removeChest(blockLocation);
                        player.sendMessage(ChatColor.GREEN + chestConfiguration.getTranslation("msg_chest_destroyed"));
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + chestConfiguration.getTranslation("msg_chest_protected"));
                    }
                }
            }
        }

        @SuppressWarnings("unused")
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
                        String blockLocation = Utils.locationToString(second.getLocation());
                        String ownerUUID = chestConfiguration.getOwner(blockLocation);
                        String playerUUID = player.getUniqueId().toString();
                        boolean isFree = ownerUUID == null;
                        boolean isOwner = !isFree && ownerUUID.equals(playerUUID);

                        if (!isFree) {
                            if (isOwner) {
                                String chestLocation = Utils.locationToString(block.getLocation());
                                chestConfiguration.addChest(chestLocation, playerUUID);
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

        @SuppressWarnings("unused")
        @EventHandler
        void onChestClick(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            Action action = event.getAction();
            Block block = event.getClickedBlock();

            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                return;
            }

            if ((block != null) && (block.getType() == Material.CHEST)) {
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                Location chestLocation = block.getLocation();
                String blockLocation = Utils.locationToString(chestLocation);
                String ownerUUID = chestConfiguration.getOwner(blockLocation);
                String playerUUID = player.getUniqueId().toString();
                boolean isFree = ownerUUID == null;
                boolean isOwner = !isFree && ownerUUID.equals(playerUUID);

                if (isFree) {
                    if (player.isSneaking() && (action == Action.RIGHT_CLICK_BLOCK) && (itemStack.getType() == Material.EMERALD)) {
                        chestConfiguration.addChest(blockLocation, playerUUID);
                        player.sendMessage(ChatColor.GREEN + chestConfiguration.getTranslation("msg_chest_lock"));

                        List<Block> around = new ArrayList<>();
                        around.add(block.getRelative(1, 0, 0));
                        around.add(block.getRelative(-1, 0, 0));
                        around.add(block.getRelative(0, 0, 1));
                        around.add(block.getRelative(0, 0, -1));

                        for (Block second : around) {
                            if (second.getType() == Material.CHEST) {
                                String secondLocation = Utils.locationToString(second.getLocation());
                                chestConfiguration.addChest(secondLocation, playerUUID);
                                break;
                            }
                        }

                        if (itemStack.getAmount() > 1) {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        }

                        event.setCancelled(true);
                    }
                } else if (!isOwner) {
                    if (player.isSneaking() && (action == Action.RIGHT_CLICK_BLOCK) && (player.getInventory().getItemInMainHand().getType() == Material.EMERALD)) {
                        if (itemStack.getAmount() > 1) {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        }

                        if (random.nextDouble() < chestConfiguration.getLockpickingChance()) {
                            player.sendMessage(ChatColor.GREEN + chestConfiguration.getTranslation("msg_chest_unlocked"));
                            chestConfiguration.removeChest(blockLocation);

                            List<Block> around = new ArrayList<>();
                            around.add(block.getRelative(1, 0, 0));
                            around.add(block.getRelative(-1, 0, 0));
                            around.add(block.getRelative(0, 0, 1));
                            around.add(block.getRelative(0, 0, -1));

                            for (Block second : around) {
                                if (second.getType() == Material.CHEST) {
                                    String secondLocation = Utils.locationToString(second.getLocation());
                                    chestConfiguration.removeChest(secondLocation);
                                    break;
                                }
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + chestConfiguration.getTranslation("msg_chest_lockpicking"));
                            event.setCancelled(true);
                        }
                    } else if (!player.isOp()) {
                        player.sendMessage(ChatColor.RED + chestConfiguration.getTranslation("msg_chest_locked"));
                        event.setCancelled(true);
                    }
                } else if (player.isSneaking() && (action == Action.RIGHT_CLICK_BLOCK) && (itemStack.getType() == Material.EMERALD)) {
                    player.sendMessage(ChatColor.GREEN + chestConfiguration.getTranslation("msg_chest_owned"));
                }
            }
        }
    }
}
