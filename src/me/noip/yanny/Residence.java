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
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.*;

class Residence {

    private JavaPlugin plugin;
    private ResidenceConfiguration residenceConfiguration;

    Residence(JavaPlugin plugin, Connection connection) {
        this.plugin = plugin;
        residenceConfiguration = new ResidenceConfiguration(plugin, connection);
    }

    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new ResidenceListener(), plugin);
        plugin.getCommand("res").setExecutor(new ResExecutor());
        residenceConfiguration.load();
    }

    void onDisable() {

    }

    private Block[] findResidenceArea(Block block) {
        Material resMaterial = residenceConfiguration.getResidenceMaterial();

        if (block.getType() == resMaterial) {
            int west = 0, east = 0, south = 0, north = 0;

            while (block.getRelative(west - 1, 0, 0).getType() == resMaterial) {
                west--;
            }
            while (block.getRelative(east + 1, 0, 0).getType() == resMaterial) {
                east++;
            }
            while (block.getRelative(0, 0, south - 1).getType() == resMaterial) {
                south--;
            }
            while (block.getRelative(0, 0, north + 1).getType() == resMaterial) {
                north++;
            }

            Block first = block.getRelative(west, 0, south);
            Block second = block.getRelative(east, 0, north);

            int maxX = second.getLocation().getBlockX() - first.getLocation().getBlockX();
            int maxZ = second.getLocation().getBlockZ() - first.getLocation().getBlockZ();

            if ((maxX <= 0) || (maxZ <= 0)) {
                return null;
            }

            for (int x = 0; x < maxX; x++) {
                if (first.getRelative(x, 0, 0).getType() != resMaterial) {
                    return null;
                }
                if (second.getRelative(-x, 0, 0).getType() != resMaterial) {
                    return null;
                }
            }
            for (int z = 0; z < maxZ; z++) {
                if (first.getRelative(0, 0, z).getType() != resMaterial) {
                    return null;
                }
                if (second.getRelative(0, 0, -z).getType() != resMaterial) {
                    return null;
                }
            }

            Block[] result = new Block[2];
            result[0] = first;
            result[1] = second;

            return result;
        }

        plugin.getLogger().warning("Residence.findResidenceArea: invalid base block");
        return null;
    }

    private Area inArea(Location location) {
        List<Area> areas = residenceConfiguration.getAllResidence();
        int playerX = location.getBlockX();
        int playerZ = location.getBlockZ();

        for (Area area : areas) {
            if ((area.first.getBlockX() <= playerX) && (area.second.getBlockX() >= playerX) && (area.first.getBlockZ() <= playerZ) && (area.second.getBlockZ() >= playerZ)) {
                return area;
            }
        }

        return null;
    }

    private Area inArea(Area location) {
        List<Area> areas = residenceConfiguration.getAllResidence();
        int a1 = location.first.getBlockX();
        int a2 = location.first.getBlockZ();
        int b1 = location.second.getBlockX();
        int b2 = location.second.getBlockZ();

        for (Area area : areas) {
            if ((area.second.getBlockX() >= a1) && (area.first.getBlockX() <= b1) && (area.second.getBlockZ() >= a2) && (area.first.getBlockZ() <= b2)) {
                return area;
            }
        }

        return null;
    }

    private class ResExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 1)) {
                return false;
            }

            Player player = (Player)commandSender;

            switch (args[0]) {
                case "create": {
                    Block[] blockArea = findResidenceArea(player.getWorld().getBlockAt(player.getLocation().subtract(0, 1, 0)));

                    if (blockArea != null) {
                        Area area = new Area(blockArea[0].getLocation(), blockArea[1].getLocation(), player.getUniqueId().toString());
                        Area res = inArea(area);

                        if (res != null) {
                            player.sendMessage(ChatColor.RED + residenceConfiguration.getTranslation("msg_res_exists"));
                        } else {
                            residenceConfiguration.addResidence(area);
                            player.sendMessage(ChatColor.GREEN + residenceConfiguration.getTranslation("msg_res_created"));
                        }

                    } else {
                        player.sendMessage(ChatColor.GREEN + residenceConfiguration.getTranslation("msg_res_wrong_place"));
                    }

                    return true;
                }
                case "remove": {
                    Area area = inArea(player.getLocation());

                    if (area != null) {
                        UUID owner = UUID.fromString(area.uuid);

                        if (owner.equals(player.getUniqueId()) || player.isOp()) {
                            residenceConfiguration.removeResidence(area.uuid, area);
                            player.sendMessage(ChatColor.GREEN + residenceConfiguration.getTranslation("msg_res_removed"));
                        } else {
                            player.sendMessage(ChatColor.RED + residenceConfiguration.getTranslation("msg_res_not_owned"));
                        }
                    } else {
                        player.sendMessage(ChatColor.GREEN + residenceConfiguration.getTranslation("msg_res_not_exists"));
                    }
                    return true;
                }
                case "info": {
                    Area area = inArea(player.getLocation());

                    if (area != null) {
                        String owner = plugin.getServer().getOfflinePlayer(UUID.fromString(area.uuid)).getName();
                        player.sendMessage(ChatColor.GOLD + residenceConfiguration.getTranslation("msg_res_owner").replace("{player}", ChatColor.GREEN + owner));
                    } else {
                        player.sendMessage(ChatColor.GREEN + residenceConfiguration.getTranslation("msg_res_not_exists"));
                    }
                    return true;
                }
                default:
                    return false;
            }
        }
    }

    private class ResidenceListener implements Listener {
        @EventHandler
        void onBlockBreak(BlockBreakEvent event) {
            Area area = inArea(event.getBlock().getLocation());

            if (area != null) {
                UUID owner = UUID.fromString(area.uuid);
                Player player = event.getPlayer();

                if (!owner.equals(player.getUniqueId()) && !player.isOp()) {
                    player.sendMessage(ChatColor.RED + residenceConfiguration.getTranslation("msg_res_foreign"));
                    event.setCancelled(true);
                }
            }
        }

        @EventHandler
        void onBlockPlace(BlockPlaceEvent event) {
            Area area = inArea(event.getBlock().getLocation());

            if (area != null) {
                UUID owner = UUID.fromString(area.uuid);
                Player player = event.getPlayer();

                if (!owner.equals(player.getUniqueId()) && !player.isOp()) {
                    player.sendMessage(ChatColor.RED + residenceConfiguration.getTranslation("msg_res_foreign"));
                    event.setCancelled(true);
                }
            }
        }

        @EventHandler
        void onEntityExplode(EntityExplodeEvent event) {
            List<Block> removed = new ArrayList<>(event.blockList().size());

            for (Block block : event.blockList()) {
                Area area = inArea(block.getLocation());

                if (area != null) {
                    removed.add(block);
                }
            }

            event.blockList().removeAll(removed);
        }
    }
}
