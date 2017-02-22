package me.noip.yanny;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main extends JavaPlugin {

    private static final String DATABASE = "users.db";

    private Connection connection = null;
    private Auth auth;
    private PlayerConfiguration playerConfiguration;
    private Essentials essentials;
    private RPG rpg;
    private ChestLocker chestLocker;
    private Residence residence;

    public Main() {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdirs()) {
                getLogger().warning("Cant create data folder");
            }
        }

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + getDataFolder() + "/" + DATABASE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        auth = new Auth(this, connection);
        playerConfiguration = new PlayerConfiguration(this);
        essentials = new Essentials(this, playerConfiguration, auth);
        rpg = new RPG(this, playerConfiguration);
        chestLocker = new ChestLocker(this, connection);
        residence = new Residence(this, connection);

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
        residence.onEnable();

        getLogger().info("Enabled YannyCraft plugin");
    }

    @Override
    public void onDisable() {
        auth.onDisable();
        playerConfiguration.onDisable();
        essentials.onDisable();
        rpg.onDisable();
        chestLocker.onDisable();
        residence.onDisable();

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("Disabled YannyCraft plugin");
    }

}
