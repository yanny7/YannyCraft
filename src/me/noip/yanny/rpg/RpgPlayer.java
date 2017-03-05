package me.noip.yanny.rpg;

import me.noip.yanny.utils.Utils;
import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class RpgPlayer {

    private PreparedStatement loadStatsStatement;
    private PreparedStatement saveStatsStatement;

    private final Player player;
    private final RpgConfiguration rpgConfiguration;
    private final RpgBoard rpgBoard;
    private final Stats stats;
    private final Plugin plugin;
    private final Map<SkillType, Skill> skills;

    RpgPlayer(Plugin plugin, Player player, Connection connection, RpgConfiguration rpgConfiguration, RpgBoard rpgBoard, Map<SkillType, Skill> skills) {
        this.plugin = plugin;
        this.player = player;
        this.rpgConfiguration = rpgConfiguration;
        this.rpgBoard = rpgBoard;
        this.skills = skills;

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
            for (Map.Entry<SkillType, MutableInt> entry : stats.entrySet()) {
                saveStatsStatement.setInt(i, stats.getValue(entry.getKey()));
                i++;
            }
            saveStatsStatement.setString(i, player.getUniqueId().toString());
            saveStatsStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void set(SkillType type, int exp) {
        stats.addValue(type, exp);
    }

    ItemStack getStatsBook() {
        Set<Map.Entry<SkillType, MutableInt>> entrySet = stats.entrySet();
        String[] data = new String[entrySet.size()];

        int i = 0;
        for (Map.Entry<SkillType, MutableInt> entry : entrySet) {
            SkillType statsType = entry.getKey();
            int xp = entry.getValue().intValue();
            String name = statsType.getDisplayName();
            data[i] = buildSkillPage(skills.get(statsType), name, xp);
            i++;
        }

        return Utils.book("RPG STATS", "rpg plugin", data);
    }

    int getStatsLevel(SkillType type) {
        return getLevelFromXp(stats.getValue(type));
    }

    void setStatsLevel(SkillType type, int level) {
        stats.setValue(type, (int)getXpForLevel(level));
    }

    Player getPlayer() {
        return player;
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

    private String buildSkillPage(Skill skill, String name, int xp) {
        StringBuilder out = new StringBuilder();
        int curLevel = getLevelFromXp(xp);
        long nextLevelXp = getXpForLevel(curLevel + 1) - xp;

        out.append(ChatColor.BOLD).append(name).append('\n');
        out.append(ChatColor.RESET).append('\n');
        out.append(ChatColor.RESET).append(rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_LEVEL)).append(": ").append(ChatColor.BOLD).append(curLevel).append('\n');
        out.append(ChatColor.RESET).append(rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_XP)).append(": ").append(ChatColor.BOLD).append(xp).append('\n');
        out.append(ChatColor.RESET).append(rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_NEXT_LEVEL_XP)).append(": ").append(ChatColor.BOLD).append(nextLevelXp).append('\n');
        out.append(ChatColor.RESET).append('\n');

        Collection<Ability> abilities = skill.getAbilities();

        if (abilities.size() > 0) {
            out.append(ChatColor.RESET).append(ChatColor.BOLD).append(rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_ABILITIES)).append('\n');
            out.append(ChatColor.RESET).append('\n');

            for (Ability ability : abilities) {
                out.append(ChatColor.RESET).append(ability.getName()).append(": ");

                if (ability.getFromLevel() <= curLevel) {
                    out.append(ChatColor.BOLD).append(ability.toString(this));
                } else {
                    out.append(ChatColor.GRAY).append(ChatColor.ITALIC).append("lvl ").append(ability.getFromLevel()).append("+");
                }

                out.append('\n');
            }
        }

        return out.toString();
    }

    private static int getLevelFromXp(int xp) {
        return (int) Math.floor((Math.sqrt(625 + 100.0 * xp) - 25) / 50.0);
    }

    private static long getXpForLevel(int lvl) {
        lvl += 1;
        return (long)(25.0 * lvl * lvl - 25.0 * lvl);
    }

    class Stats {
        private final Map<SkillType, MutableInt> stats = new LinkedHashMap<>();

        Stats(int mining, int excavation, int woodcutting, int herbalism, int fishing, int unarmed, int archery, int swords,
              int axes, int taming, int repair, int acrobatics, int alchemy, int smelting) {
            stats.put(SkillType.MINING, new MutableInt(mining));
            stats.put(SkillType.EXCAVATION, new MutableInt(excavation));
            stats.put(SkillType.WOODCUTTING, new MutableInt(woodcutting));
            stats.put(SkillType.HERBALISM, new MutableInt(herbalism));
            stats.put(SkillType.FISHING, new MutableInt(fishing));
            stats.put(SkillType.UNARMED, new MutableInt(unarmed));
            stats.put(SkillType.ARCHERY, new MutableInt(archery));
            stats.put(SkillType.SWORDS, new MutableInt(swords));
            stats.put(SkillType.AXES, new MutableInt(axes));
            stats.put(SkillType.TAMING, new MutableInt(taming));
            stats.put(SkillType.REPAIR, new MutableInt(repair));
            stats.put(SkillType.ACROBATICS, new MutableInt(acrobatics));
            stats.put(SkillType.ALCHEMY, new MutableInt(alchemy));
            stats.put(SkillType.SMELTING, new MutableInt(smelting));
        }

        int getValue(SkillType type) {
            return stats.get(type).intValue();
        }

        void addValue(SkillType type, int value) {
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

        void setValue(SkillType type, int value) {
            stats.get(type).setValue(value);
            rpgBoard.updateObjective(type, player, getLevelFromXp(value));
        }

        Set<Map.Entry<SkillType, MutableInt>> entrySet() {
            return stats.entrySet();
        }
    }
}
