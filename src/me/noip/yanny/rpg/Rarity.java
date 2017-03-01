package me.noip.yanny.rpg;

import org.bukkit.ChatColor;

public enum Rarity {
    SCRAP(1.0, "Odpad", ChatColor.GRAY),
    COMMON(0.5, "Bezne", ChatColor.WHITE),
    UNCOMMON(0.2, "Vzacne", ChatColor.YELLOW),
    RARE(0.1, "Rarita", ChatColor.GOLD),
    EXOTIC(0.05, "Exoticke", ChatColor.GREEN),
    HEROIC(0.02, "Hrdinske", ChatColor.DARK_GREEN),
    EPIC(0.01, "Epicke", ChatColor.AQUA),
    LEGENDARY(0.005, "Legendarne", ChatColor.DARK_AQUA),
    MYTHIC(0.002, "Myticke", ChatColor.DARK_PURPLE),
    GODLIKE(0.001, "Bozske", ChatColor.DARK_RED),
    ;

    private double probability;
    private String displayName;
    private ChatColor chatColor;

    Rarity(double probability, String displayName, ChatColor chatColor) {
        this.probability = probability;
        this.displayName = displayName;
        this.chatColor = chatColor;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }
}
