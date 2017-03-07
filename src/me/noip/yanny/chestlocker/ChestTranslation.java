package me.noip.yanny.chestlocker;

import org.bukkit.ChatColor;

enum ChestTranslation {
    CHEST_LOCK("Uzamkol si truhlicu", ChatColor.GREEN),
    CHEST_UNLOCKED("Odomkol si truhlicu", ChatColor.GREEN),
    CHEST_DESTROYED("Znicil si uzamknutu truhlicu", ChatColor.GREEN),
    CHEST_OWNED("Truhlica je uz uzamknuta", ChatColor.GREEN),

    ERR_CHEST_NOT_LOCKED("Truhlica je odomknuta", ChatColor.RED),
    ERR_CHEST_NOT_OWNED("Nevlastnis tuto truhlicu", ChatColor.RED),
    ERR_CHEST_INVALID("Nemieris na truhlicu", ChatColor.RED),
    ERR_CHEST_PROTECTED("Truhlica je uzamknuta", ChatColor.RED),
    ERR_CHEST_LOCKPICKING("Nepodarilo sa ti odomknut truhlicu", ChatColor.RED),
    ERR_CHEST_LOCKED("Truhlica je uzamknuta", ChatColor.RED),
    ;

    private String displayName;
    private ChatColor chatColor;

    ChestTranslation(String displayName, ChatColor chatColor) {
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
}
