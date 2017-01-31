package me.noip.yanny;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private Auth auth;
    private PlayerConfiguration playerConfiguration;
    private Essentials essentials;
    private Board board;
    private RPG rpg;

    public Main() {
        auth = new Auth(this);
        playerConfiguration = new PlayerConfiguration(this);
        essentials = new Essentials(this, playerConfiguration, auth);
        board = new Board(this);
        rpg = new RPG(this, playerConfiguration);
    }

    @Override
    public void onEnable() {
        auth.setEssentialsConfiguration(essentials.getEssentialsConfiguration());

        auth.onEnable();
        playerConfiguration.onEnable();
        essentials.onEnable();
        board.onEnable();
        rpg.onEnable();

        getLogger().info("Enabled YannyCraft plugin");
    }

    @Override
    public void onDisable() {
        auth.onDisable();
        playerConfiguration.onDisable();
        essentials.onDisable();
        board.onDisable();
        rpg.onDisable();

        getLogger().info("Disabled YannyCraft plugin");
    }

}
