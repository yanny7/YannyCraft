package me.noip.yanny.rpg;

import me.noip.yanny.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

class RpgPlayer {

    private PreparedStatement loadStatsStatement;
    private PreparedStatement saveStatsStatement;

    private final Plugin plugin;
    private final Player player;
    private final RpgConfiguration rpgConfiguration;
    private final Stats stats;

    RpgPlayer(Plugin plugin, Player player, Connection connection, RpgConfiguration rpgConfiguration) {
        this.plugin = plugin;
        this.player = player;
        this.rpgConfiguration = rpgConfiguration;

        try {
            loadStatsStatement = connection.prepareStatement("SELECT Mining, Excavation, Woodcutting FROM rpg WHERE Player = ?");
            saveStatsStatement = connection.prepareStatement("UPDATE rpg SET Mining = ?, Excavation = ?, Woodcutting = ? WHERE Player = ?");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int mining = 0;
        int excavation = 0;
        int woodcutting = 0;

        try {
            loadStatsStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = loadStatsStatement.executeQuery();
            if (resultSet.next()) {
                mining = resultSet.getInt(1);
                excavation = resultSet.getInt(2);
                woodcutting = resultSet.getInt(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        stats = new Stats(mining, excavation, woodcutting);
    }

    void onQuit() {
        try {
            saveStatsStatement.setInt(1, stats.mining);
            saveStatsStatement.setInt(2, stats.excavation);
            saveStatsStatement.setInt(3, stats.woodcutting);
            saveStatsStatement.setString(4, player.getUniqueId().toString());
            saveStatsStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void blockBreak(BlockBreakEvent event) {
        Material material = player.getInventory().getItemInMainHand().getType();
        Material blockMaterial = event.getBlock().getType();

        switch (material) {
            case WOOD_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
            case DIAMOND_PICKAXE: {
                int exp = rpgConfiguration.getMiningExp(blockMaterial);
                if (exp > 0) {
                    stats.mining += exp;
                }
                break;
            }
            case WOOD_SPADE:
            case STONE_SPADE:
            case IRON_SPADE:
            case GOLD_SPADE:
            case DIAMOND_SPADE: {
                int exp = rpgConfiguration.getExcavationExp(blockMaterial);
                if (exp > 0) {
                    stats.excavation += exp;
                }
                break;
            }
            case WOOD_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLD_AXE:
            case DIAMOND_AXE: {
                int exp = rpgConfiguration.getWoodcuttingExp(blockMaterial);
                if (exp > 0) {
                    stats.woodcutting += exp;
                }
                break;
            }
        }
    }

    ItemStack getStatsBook() {
        String overview =
                ChatColor.BLUE + "Mining: " + ChatColor.RED + ChatColor.BOLD + getLevelFromXp(stats.mining) + '\n' +
                ChatColor.BLUE + "Excavation: " + ChatColor.RED + ChatColor.BOLD + getLevelFromXp(stats.excavation) + '\n' +
                ChatColor.BLUE + "Woodcutting: " + ChatColor.RED + ChatColor.BOLD + getLevelFromXp(stats.woodcutting) + '\n';

        String mining =
                ChatColor.BOLD + "Mining\n\n" +
                ChatColor.RESET + "Level: " + getLevelFromXp(stats.mining) + '\n' +
                ChatColor.RESET + "XP: " + stats.mining + '\n' +
                ChatColor.RESET + "Next lvl XP: " + (getXpForLevel(getLevelFromXp(stats.mining) + 2) - stats.mining) + '\n' +
                ChatColor.RESET + "\n" +
                ChatColor.RESET + ChatColor.BOLD +"Active abilities:\n";

        return Utils.book("RPG STATS", "rpg plugin", overview, mining);
    }

    static void registerPlayer(Connection connection, Player player) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO rpg (Player) VALUES (?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getLevelFromXp(int xp) {
        return (int) Math.floor((Math.sqrt(625 + 100 * xp) - 25) / 50.0);
    }

    private static int getXpForLevel(int lvl) {
        return 25 * lvl * lvl - 25 * lvl;
    }

    class Stats {
        int mining;
        int excavation;
        int woodcutting;

        Stats(int mining, int excavation, int woodcutting) {
            this.mining = mining;
            this.excavation = excavation;
            this.woodcutting = woodcutting;
        }
    }
}
