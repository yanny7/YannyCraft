package me.noip.yanny.auth;

import org.bukkit.ChatColor;

public enum AuthTranslation {
    REGISTER("Zaregistruj sa /register [heslo] [heslo]", ChatColor.GREEN),
    LOGIN("Prihlas sa /login [heslo]", ChatColor.GREEN),
    REGISTERED("Bol si uspesne zaregistrovany", ChatColor.GREEN),
    LOGGED("Bol si uspesne prihlaseny", ChatColor.GREEN),
    PASSWORD_CHANGED("Heslo bolo uspesne zmenene", ChatColor.GREEN),

    REGISTERED_ALL("Hrac {player} sa registroval na server", ChatColor.GOLD),
    LOGGED_ALL("Hrac {player} sa prihlasil na server", ChatColor.GOLD),
    DISCONNECT_ALL("Hrac {player} sa odhlasil zo servera", ChatColor.GOLD),

    ERR_LOGGED("Uz si prihlaseny", ChatColor.RED),
    ERR_REGISTERED("Uz si zaregistrovany", ChatColor.RED),
    ERR_WRONG_PASSWORD("Zadal si zle heslo", ChatColor.RED),
    ERR_NOT_REGISTERED("Najprv sa zaregistruj", ChatColor.RED),
    ERR_PASSWORDS_NOT_SAME("Zadane hesla sa nezhoduju", ChatColor.RED),
    ERR_CHARACTERS("Heslo moze obsahovat len znaky [a-z], [A-Z] a [0-9]", ChatColor.RED),
    ERR_LENGTH("Heslo musi mat 6 - 32 znakov", ChatColor.RED),
    ERR_REGISTER("Chyba pocas registracie, skus znova", ChatColor.RED),
    ERR_PASSWORD_CHANGE("Chyba pocas zmeny hesla, skus znova", ChatColor.RED),
    ERR_COMMAND_PERMISSION("Nemas opravnenie na prikaz ak nie si prihlaseny", ChatColor.RED),
    ERR_CHAT_PERMISSION("Nemas opravnenie na prikaz ak nie si prihlaseny", ChatColor.RED),
    ;

    private String displayName;
    private ChatColor chatColor;

    AuthTranslation(String displayName, ChatColor chatColor) {
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
