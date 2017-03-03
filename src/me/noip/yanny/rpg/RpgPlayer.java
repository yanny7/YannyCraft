package me.noip.yanny.rpg;

import me.noip.yanny.utils.Utils;
import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

class RpgPlayer {

    private PreparedStatement loadStatsStatement;
    private PreparedStatement saveStatsStatement;

    private final Player player;
    private final RpgConfiguration rpgConfiguration;
    private final RpgBoard rpgBoard;
    private final Stats stats;
    private final Plugin plugin;
    private final Random random = new Random();

    RpgPlayer(Plugin plugin, Player player, Connection connection, RpgConfiguration rpgConfiguration, RpgBoard rpgBoard) {
        this.plugin = plugin;
        this.player = player;
        this.rpgConfiguration = rpgConfiguration;
        this.rpgBoard = rpgBoard;

        try {
            loadStatsStatement = connection.prepareStatement("SELECT Mining, Excavation, Woodcutting, Herbalism, " +
                    "Fishing, Unarmed, Archery, Swords, Axes, Taming, Repair, Acrobatics, Alchemy, Smelting " +
                    "FROM rpg WHERE Player = ?");
            saveStatsStatement = connection.prepareStatement("UPDATE rpg SET Mining = ?, Excavation = ?, " +
                    "Woodcutting = ?, Herbalism = ?, Fishing = ?, Unarmed = ?, Archery = ?, Swords = ?, Axes = ?, " +
                    "Taming = ?, Repair = ?, Acrobatics = ?, Alchemy = ?, Smelting = ? WHERE Player = ?");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int mining = 0;
        int excavation = 0;
        int woodcutting = 0;
        int herbalism = 0;
        int fishing = 0;
        int unarmed = 0;
        int archery = 0;
        int swords = 0;
        int axes = 0;
        int taming = 0;
        int repair = 0;
        int acrobatics = 0;
        int alchemy = 0;
        int smelting = 0;

        try {
            loadStatsStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = loadStatsStatement.executeQuery();
            if (resultSet.next()) {
                mining = resultSet.getInt(1);
                excavation = resultSet.getInt(2);
                woodcutting = resultSet.getInt(3);
                herbalism = resultSet.getInt(4);
                fishing = resultSet.getInt(5);
                unarmed = resultSet.getInt(6);
                archery = resultSet.getInt(7);
                swords = resultSet.getInt(8);
                axes = resultSet.getInt(9);
                taming = resultSet.getInt(10);
                repair = resultSet.getInt(11);
                acrobatics = resultSet.getInt(12);
                alchemy = resultSet.getInt(13);
                smelting = resultSet.getInt(14);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        stats = new Stats(mining, excavation, woodcutting, herbalism, fishing, unarmed, archery, swords, axes, taming,
                repair, acrobatics, alchemy, smelting);
    }

    void onQuit() {
        try {
            int i = 1;
            for (Map.Entry<RpgPlayerStatsType, MutableInt> entry : stats.entrySet()) {
                saveStatsStatement.setInt(i, stats.getValue(entry.getKey()));
                i++;
            }
            saveStatsStatement.setString(i, player.getUniqueId().toString());
            saveStatsStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void blockBreak(BlockBreakEvent event) {
        ItemStack handMaterial = player.getInventory().getItemInMainHand();
        Block destMaterial = event.getBlock();

        switch (handMaterial.getType()) {
            case WOOD_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
            case DIAMOND_PICKAXE: {
                int exp = rpgConfiguration.getMiningExp(destMaterial.getType());
                if (exp > 0) {
                    stats.addValue(RpgPlayerStatsType.MINING, exp);
                    return;
                }
                break;
            }
            case WOOD_SPADE:
            case STONE_SPADE:
            case IRON_SPADE:
            case GOLD_SPADE:
            case DIAMOND_SPADE: {
                int exp = rpgConfiguration.getExcavationExp(destMaterial.getType());
                if (exp > 0) {
                    stats.addValue(RpgPlayerStatsType.EXCAVATION, exp);
                    return;
                }
                break;
            }
            case WOOD_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLD_AXE:
            case DIAMOND_AXE: {
                int exp = rpgConfiguration.getWoodcuttingExp(destMaterial.getType());
                if (exp > 0) {
                    stats.addValue(RpgPlayerStatsType.WOODCUTTING, exp);
                    return;
                }
                break;
            }
        }

        switch (destMaterial.getType()) {
            case MELON_BLOCK:
            case PUMPKIN: {
                int exp = rpgConfiguration.getHerbalismExp(destMaterial.getType());
                if (exp > 0) {
                    stats.addValue(RpgPlayerStatsType.HERBALISM, exp);
                    return;
                }
            }
            case POTATO:
            case CARROT:
            case BEETROOT_BLOCK:
            case CROPS:
            case NETHER_WARTS: {
                if ((destMaterial.getData() == 7)) { // fullyGrown
                    int exp = rpgConfiguration.getHerbalismExp(destMaterial.getType());
                    if (exp > 0) {
                        stats.addValue(RpgPlayerStatsType.HERBALISM, exp);
                        return;
                    }
                }
                break;
            }
            case COCOA: {
                if (((destMaterial.getData() & 0x8) == 8)) { // fullyGrown
                    int exp = rpgConfiguration.getHerbalismExp(destMaterial.getType());
                    if (exp > 0) {
                        stats.addValue(RpgPlayerStatsType.HERBALISM, exp);
                        return;
                    }
                }
                break;
            }
            case CHORUS_FLOWER: {
                if ((destMaterial.getData() == 5)) { // fullyGrown
                    int exp = rpgConfiguration.getHerbalismExp(destMaterial.getType());
                    if (exp > 0) {
                        stats.addValue(RpgPlayerStatsType.HERBALISM, exp);
                        return;
                    }
                }
                break;
            }
        }
    }

    void catchFish(PlayerFishEvent event) {
        Entity entity = event.getCaught();

        if (entity != null) {
            switch (event.getState()) {
                case CAUGHT_ENTITY: {
                    Rarity[] values = Rarity.values();
                    for (int i = values.length - 1; i >= 0; i--) {
                        Rarity next = values[i];
                        double rand = random.nextDouble();

                        if (rand <= next.getProbability()) {
                            int exp = rpgConfiguration.getFishingExp(next);
                            if (exp > 0) {
                                stats.addValue(RpgPlayerStatsType.FISHING, exp);
                                return;
                            }
                            return;
                        }
                    }
                }
                case CAUGHT_FISH: {
                    ItemStack fish = ((Item) event.getCaught()).getItemStack();
                    int exp = -1;

                    switch (fish.getData().getData()) {
                        case 0:
                            exp = rpgConfiguration.getFishingExp(Rarity.SCRAP);
                            break;
                        case 1:
                            exp = rpgConfiguration.getFishingExp(Rarity.UNCOMMON);
                            break;
                        case 2:
                            exp = rpgConfiguration.getFishingExp(Rarity.EXOTIC);
                            break;
                        case 3:
                            exp = rpgConfiguration.getFishingExp(Rarity.EPIC);
                            break;
                    }

                    if (exp > 0) {
                        stats.addValue(RpgPlayerStatsType.FISHING, exp);
                        return;
                    }
                    break;
                }
            }
        }
    }

    void entityDamaged(EntityDamageByEntityEvent event) {
        int exp = rpgConfiguration.getDamageExp(event.getEntityType());
        if (exp > 0) {
            switch (event.getCause()) {
                case PROJECTILE:
                    stats.addValue(RpgPlayerStatsType.ARCHERY, exp);
                    return;
                case ENTITY_ATTACK:
                    switch (player.getInventory().getItemInMainHand().getType()) {
                        case AIR:
                            stats.addValue(RpgPlayerStatsType.UNARMED, exp);
                            break;
                        case STONE_SWORD:
                        case DIAMOND_SWORD:
                        case GOLD_SWORD:
                        case IRON_SWORD:
                        case WOOD_SWORD:
                            stats.addValue(RpgPlayerStatsType.SWORDS, exp);
                            break;
                        case DIAMOND_AXE:
                        case GOLD_AXE:
                        case IRON_AXE:
                        case STONE_AXE:
                        case WOOD_AXE:
                            stats.addValue(RpgPlayerStatsType.AXES, exp);
                            break;
                    }
            }
        }
    }

    void entityTame(EntityTameEvent event) {
        int exp = rpgConfiguration.getTameExp(event.getEntityType());
        if (exp > 0) {
            stats.addValue(RpgPlayerStatsType.TAMING, exp);
        }
    }

    void itemRepair(int cost) {
        int exp = rpgConfiguration.getRepairExp(cost);
        if (exp > 0) {
            stats.addValue(RpgPlayerStatsType.REPAIR, exp);
        }
    }

    void fallDamage(double damage) {
        int exp = rpgConfiguration.getAcrobaticExp(damage);
        if (exp > 0) {
            stats.addValue(RpgPlayerStatsType.ACROBATICS, exp);
        }
    }

    void potionCreated(PotionType potion, Material material) {
        int exp = rpgConfiguration.getPotionExp(potion);
        if (exp > 0) {
            switch (material) {
                case POTION:
                    stats.addValue(RpgPlayerStatsType.ALCHEMY, exp);
                    break;
                case SPLASH_POTION:
                    stats.addValue(RpgPlayerStatsType.ALCHEMY, exp * 2);
                    break;
                case LINGERING_POTION:
                    stats.addValue(RpgPlayerStatsType.ALCHEMY, exp * 10);
                    break;
            }
        }
    }

    void itemSmelted(ItemStack itemStack) {
        int exp = rpgConfiguration.getSmeltingExp(itemStack.getType(), itemStack.getAmount());
        if (exp > 0) {
            stats.addValue(RpgPlayerStatsType.SMELTING, exp);
        }
    }

    ItemStack getStatsBook() {
        Set<Map.Entry<RpgPlayerStatsType, MutableInt>> entrySet = stats.entrySet();
        StringBuilder stringBuilder = new StringBuilder();
        String[] data = new String[entrySet.size() + 1];

        int i = 1;
        for (Map.Entry<RpgPlayerStatsType, MutableInt> entry : entrySet) {
            int xp = entry.getValue().intValue();
            String name = entry.getKey().getDisplayName();
            stringBuilder.append(ChatColor.BLUE).append(name).append(": ").append(ChatColor.RED).append(ChatColor.BOLD).append(getLevelFromXp(xp)).append('\n');
            data[i] = buildSkillPage(name, xp);
            i++;
        }
        data[0] = stringBuilder.toString();

        return Utils.book("RPG STATS", "rpg plugin", data);
    }

    int getStatsLevel(RpgPlayerStatsType type) {
        return getLevelFromXp(stats.getValue(type));
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

    private String buildSkillPage(String name, int xp) {
        StringBuilder out = new StringBuilder();
        int curLevel = getLevelFromXp(xp);
        int nextLevelXp = getXpForLevel(curLevel + 1) - xp;

        out.append(ChatColor.BOLD).append(name).append('\n');
        out.append(ChatColor.RESET).append('\n');
        out.append(ChatColor.RESET).append(rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_LEVEL)).append(": ").append(ChatColor.BOLD).append(curLevel).append('\n');
        out.append(ChatColor.RESET).append(rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_XP)).append(": ").append(ChatColor.BOLD).append(xp).append('\n');
        out.append(ChatColor.RESET).append(rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_NEXT_LEVEL_XP)).append(": ").append(ChatColor.BOLD).append(nextLevelXp).append('\n');
        out.append(ChatColor.RESET).append('\n');

        return out.toString();
    }

    private static int getLevelFromXp(int xp) {
        return (int) Math.floor((Math.sqrt(625 + 100 * xp) - 25) / 50.0);
    }

    private static int getXpForLevel(int lvl) {
        lvl += 1;
        return 25 * lvl * lvl - 25 * lvl;
    }

    class Stats {
        private final Map<RpgPlayerStatsType, MutableInt> stats = new LinkedHashMap<>();

        Stats(int mining, int excavation, int woodcutting, int herbalism, int fishing, int unarmed, int archery, int swords,
              int axes, int taming, int repair, int acrobatics, int alchemy, int smelting) {
            stats.put(RpgPlayerStatsType.MINING, new MutableInt(mining));
            stats.put(RpgPlayerStatsType.EXCAVATION, new MutableInt(excavation));
            stats.put(RpgPlayerStatsType.WOODCUTTING, new MutableInt(woodcutting));
            stats.put(RpgPlayerStatsType.HERBALISM, new MutableInt(herbalism));
            stats.put(RpgPlayerStatsType.FISHING, new MutableInt(fishing));
            stats.put(RpgPlayerStatsType.UNARMED, new MutableInt(unarmed));
            stats.put(RpgPlayerStatsType.ARCHERY, new MutableInt(archery));
            stats.put(RpgPlayerStatsType.SWORDS, new MutableInt(swords));
            stats.put(RpgPlayerStatsType.AXES, new MutableInt(axes));
            stats.put(RpgPlayerStatsType.TAMING, new MutableInt(taming));
            stats.put(RpgPlayerStatsType.REPAIR, new MutableInt(repair));
            stats.put(RpgPlayerStatsType.ACROBATICS, new MutableInt(acrobatics));
            stats.put(RpgPlayerStatsType.ALCHEMY, new MutableInt(alchemy));
            stats.put(RpgPlayerStatsType.SMELTING, new MutableInt(smelting));
        }

        int getValue(RpgPlayerStatsType type) {
            return stats.get(type).intValue();
        }

        void addValue(RpgPlayerStatsType type, int value) {
            plugin.getLogger().info(type.getDisplayName() + " " + value);
            MutableInt mutableInt = stats.get(type);
            int oldLevel = getLevelFromXp(mutableInt.intValue());

            mutableInt.add(value);

            int newLevel = getLevelFromXp(mutableInt.intValue());
            if (newLevel != oldLevel) {
                rpgBoard.updateObjective(type, player, newLevel);
                player.sendMessage(ChatColor.GOLD + rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_LEVELUP)
                        .replace("{STATS_TYPE}", ChatColor.GREEN + type.getDisplayName() + ChatColor.GOLD)
                        .replace("{LEVEL}", ChatColor.GREEN + Integer.toString(newLevel) + ChatColor.GOLD)
                        .replace("{LEVEL_DIFF}", ChatColor.GREEN + "+" + Integer.toString(newLevel - oldLevel) + ChatColor.GOLD));
                plugin.getLogger().info("LevelUp: " + player.getDisplayName() + " " + type.name() + " : " + newLevel);
            }
        }

        Set<Map.Entry<RpgPlayerStatsType, MutableInt>> entrySet() {
            return stats.entrySet();
        }
    }
}
