package me.noip.yanny;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private Auth auth;
    private PlayerConfiguration playerConfiguration;
    private Essentials essentials;
    private Board board;
    private Home home;
    private RPG rpg;

    public Main() {
        auth = new Auth(this);
        playerConfiguration = new PlayerConfiguration(this);
        essentials = new Essentials(this);
        board = new Board(this);
        home = new Home(this, playerConfiguration, auth);
        rpg = new RPG(this, playerConfiguration);
    }

    @Override
    public void onEnable() {
        auth.onEnable();
        playerConfiguration.onEnable();
        essentials.onEnable();
        board.onEnable();
        home.onEnable();
        rpg.onEnable();

        getLogger().info("Enabled YannyCraft plugin");
    }

    @Override
    public void onDisable() {
        auth.onDisable();
        playerConfiguration.onDisable();
        essentials.onDisable();
        board.onDisable();
        home.onDisable();
        rpg.onDisable();

        getLogger().info("Disabled YannyCraft plugin");
    }

}
