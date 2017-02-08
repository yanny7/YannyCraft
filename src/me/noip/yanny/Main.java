package me.noip.yanny;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private Auth auth;
    private PlayerConfiguration playerConfiguration;
    private Essentials essentials;
    private RPG rpg;
    private ChestLocker chestLocker;

    public Main() {
        auth = new Auth(this);
        playerConfiguration = new PlayerConfiguration(this);
        essentials = new Essentials(this, playerConfiguration, auth);
        rpg = new RPG(this, playerConfiguration);
        chestLocker = new ChestLocker(this, playerConfiguration);
        getLogger().info("Started YannyCraft plugin");
    }

    @Override
    public void onEnable() {
        auth.setEssentialsConfiguration(essentials.getEssentialsConfiguration());

        auth.onEnable();
        playerConfiguration.onEnable();
        essentials.onEnable();
        rpg.onEnable();
        chestLocker.onEnable();

        getLogger().info("Enabled YannyCraft plugin");
    }

    @Override
    public void onDisable() {
        auth.onDisable();
        playerConfiguration.onDisable();
        essentials.onDisable();
        rpg.onDisable();
        chestLocker.onDisable();

        getLogger().info("Disabled YannyCraft plugin");
    }

}
