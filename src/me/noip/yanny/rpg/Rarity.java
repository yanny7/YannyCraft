package me.noip.yanny.rpg;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public enum Rarity {
    SCRAP(1.0, "Odpad", ChatColor.GRAY),
    COMMON(0.4, "Bezne", ChatColor.WHITE),
    UNCOMMON(0.16, "Vzacne", ChatColor.YELLOW),
    RARE(0.064, "Rarita", ChatColor.GOLD),
    EXOTIC(0.0256, "Exoticke", ChatColor.GREEN),
    HEROIC(0.01024, "Hrdinske", ChatColor.DARK_GREEN),
    EPIC(0.004096, "Epicke", ChatColor.AQUA),
    LEGENDARY(0.0016384, "Legendarne", ChatColor.DARK_AQUA),
    MYTHIC(0.00065536, "Myticke", ChatColor.DARK_PURPLE),
    GODLIKE(0.000262144, "Bozske", ChatColor.DARK_RED),
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public static void loadDefaults(Map<Rarity, List<Material>> treasureItems) {
        treasureItems.put(Rarity.SCRAP, new LinkedList<>());
        treasureItems.put(Rarity.COMMON, new LinkedList<>());
        treasureItems.put(Rarity.UNCOMMON, new LinkedList<>());
        treasureItems.put(Rarity.RARE, new LinkedList<>());
        treasureItems.put(Rarity.EXOTIC, new LinkedList<>());
        treasureItems.put(Rarity.HEROIC, new LinkedList<>());
        treasureItems.put(Rarity.EPIC, new LinkedList<>());
        treasureItems.put(Rarity.LEGENDARY, new LinkedList<>());
        treasureItems.put(Rarity.MYTHIC, new LinkedList<>());
        treasureItems.put(Rarity.GODLIKE, new LinkedList<>());
    }
}
