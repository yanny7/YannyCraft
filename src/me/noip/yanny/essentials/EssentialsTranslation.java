package me.noip.yanny.essentials;

import org.bukkit.ChatColor;

enum EssentialsTranslation {
    SPAWN_SET("Nova spawn lokacia bola nastavena", ChatColor.GREEN),
    TPA_SENDED("Poziadavka na teleport bola odoslana hracovi", ChatColor.GREEN),
    TPA_RECEIVED("Hrac {player} sa chce k tebe teleportovat /tpaccept prijmi, /tpdeny zamietni", ChatColor.GREEN),
    TPDENY("Poziadavka na teleport bola zamietnuta", ChatColor.GREEN),
    TELEPORTED("Bol si teleportovany k hracovi {player}", ChatColor.GREEN),
    HOME_CREATED("Domov bol nastaveny", ChatColor.GREEN),
    HOME_TELEPORTED("Bol si teleportovany domov", ChatColor.GREEN),
    BACK_TELEPORTED("Bol si teleportovany na poslednu poziciu", ChatColor.GREEN),
    INV_CLEARED("Vyprazdnil si inventar", ChatColor.GREEN),
    SPAWN_TELEPORTED("Bol si teleportovany na spawn", ChatColor.GREEN),

    ERR_INVALID_PLAYER("Hrac {player} neexistuje", ChatColor.RED),
    ERR_PERMISSION("Na tento prikaz nemas prava", ChatColor.RED),
    ERR_TPDENY("Poziadavka na teleport bola zamietnuta", ChatColor.RED),
    ;

    private String displayName;
    private ChatColor chatColor;

    EssentialsTranslation(String displayName, ChatColor chatColor) {
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
