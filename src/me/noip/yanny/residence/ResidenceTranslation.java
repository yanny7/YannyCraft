package me.noip.yanny.residence;

import org.bukkit.ChatColor;

public enum ResidenceTranslation {
    RES_CREATED("Vytvoril si rezidenciu", ChatColor.GREEN),
    RES_REMOVED("Rezidencia bola zrusena", ChatColor.GREEN),
    RES_OWNER("Majitel: {player}", ChatColor.GREEN),

    ERR_RES_EXISTS("Uz tu je vytvorena rezidencia", ChatColor.RED),
    ERR_RES_WRONG_PLACE("Nestojis na spravnom mieste pre vytvorenie rezidencie", ChatColor.RED),
    ERR_RES_NOT_OWNED("Nevlastnis tuto rezidenciu", ChatColor.RED),
    ERR_RES_NOT_EXISTS("Na tomto mieste nieje ziadna rezidencia", ChatColor.RED),
    ERR_RES_FOREIGN("Nemas opravnenia v tejto rezidencii", ChatColor.RED),
    ;

    private String displayName;
    private ChatColor chatColor;

    ResidenceTranslation(String displayName, ChatColor chatColor) {
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
