package me.noip.yanny.utils;

public interface PartPlugin {
    void onEnable();
    void onDisable();

    enum PartPluginType {
        AUTH,
        BOSS,
        BULLETIN,
        CHESTLOCKER,
        ESSENTIALS,
        RESIDENCE,
        RPG,
    }
}
