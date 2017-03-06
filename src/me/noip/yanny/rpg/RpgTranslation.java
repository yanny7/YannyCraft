package me.noip.yanny.rpg;

import org.bukkit.ChatColor;

public enum RpgTranslation {

    RPG_STATS("RPG Statistiky", ChatColor.RESET),
    RPG_LEVEL("Level", ChatColor.RESET),
    RPG_XP("Xp", ChatColor.RESET),
    RPG_NEXT_XP("Xp na dalsi lvl", ChatColor.RESET),
    RPG_ABILITIES("Schopnosti", ChatColor.RESET),

    LEVELUP("Tvoj skill '{STATS_TYPE}' sa zvysil na level {LEVEL} ({LEVEL_DIFF})", ChatColor.GOLD),
    TREASURE_FOUND("Nasiel si [{TREASURE}]", ChatColor.GOLD),
    CRITICAL_DAMAGE("Sposobil si kriticky utok {DMG_MULT}", ChatColor.GOLD),
    DAMAGE_REDUCED("Damage bol znizeny o {DMG_PERC}", ChatColor.GOLD),
    ;

    private String displayName;
    private ChatColor chatColor;

    RpgTranslation(String displayName, ChatColor chatColor) {
        this.displayName = displayName;
        this.chatColor = chatColor;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String display() {
        return chatColor + displayName;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }
}
