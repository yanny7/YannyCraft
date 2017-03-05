package me.noip.yanny;

import me.noip.yanny.auth.Auth;
import me.noip.yanny.boss.Boss;
import me.noip.yanny.bulletin.Bulletin;
import me.noip.yanny.chestlocker.ChestLocker;
import me.noip.yanny.essentials.Essentials;
import me.noip.yanny.residence.Residence;
import me.noip.yanny.rpg.RPG;
import me.noip.yanny.utils.PartPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainPlugin extends JavaPlugin {

    private static final String DATABASE = "users.db";

    private Connection connection = null;
    private Map<PartPlugin.PartPluginType, PartPlugin> pluginMap = new LinkedHashMap<>();

    public MainPlugin() {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdirs()) {
                getLogger().warning("Cant create data folder");
            }
        }

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + getDataFolder() + "/" + DATABASE);

            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "ID VARCHAR(64) PRIMARY KEY NOT NULL,"
                    + "Password VARCHAR(64) NOT NULL,"
                    + "ResetPassword BOOLEAN DEFAULT(0),"
                    + "Inventory TEXT NOT NULL,"
                    + "HomeLocation TEXT NOT NULL,"
                    + "BackLocation TEXT NOT NULL,"
                    + "LastUpdated DATETIME NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS chests ("
                    + "Location VARCHAR(64) PRIMARY KEY NOT NULL,"
                    + "Player VARCHAR(64) NOT NULL)");
            statement.execute("CREATE INDEX IF NOT EXISTS PlayerIndex ON chests( Player )");
            statement.execute("CREATE TABLE IF NOT EXISTS residence ("
                    + "Location1 VARCHAR(64) NOT NULL,"
                    + "Location2 VARCHAR(64) NOT NULL,"
                    + "Player VARCHAR(64) NOT NULL)");
            statement.execute("CREATE INDEX IF NOT EXISTS PlayerIndex ON residence( Player )");
            statement.execute("CREATE TABLE IF NOT EXISTS rpg ("
                    + "Player VARCHAR(64) PRIMARY KEY NOT NULL,"
                    + "Mining INTEGER DEFAULT(0),"
                    + "Excavation INTEGER DEFAULT(0),"
                    + "Woodcutting INTEGER DEFAULT(0),"
                    + "Herbalism INTEGER DEFAULT(0),"
                    + "Fishing INTEGER DEFAULT(0),"
                    + "Unarmed INTEGER DEFAULT(0),"
                    + "Archery INTEGER DEFAULT(0),"
                    + "Swords INTEGER DEFAULT(0),"
                    + "Axes INTEGER DEFAULT(0),"
                    + "Taming INTEGER DEFAULT(0),"
                    + "Repair INTEGER DEFAULT(0),"
                    + "Acrobatics INTEGER DEFAULT(0),"
                    + "Alchemy INTEGER DEFAULT(0),"
                    + "Smelting INTEGER DEFAULT(0))");
        } catch (Exception e) {
            e.printStackTrace();
        }

        pluginMap.put(PartPlugin.PartPluginType.AUTH, new Auth(this));
        pluginMap.put(PartPlugin.PartPluginType.ESSENTIALS, new Essentials(this));
        pluginMap.put(PartPlugin.PartPluginType.RPG, new RPG(this));
        pluginMap.put(PartPlugin.PartPluginType.BOSS, new Boss(this));
        pluginMap.put(PartPlugin.PartPluginType.CHESTLOCKER, new ChestLocker(this));
        pluginMap.put(PartPlugin.PartPluginType.RESIDENCE, new Residence(this));
        pluginMap.put(PartPlugin.PartPluginType.BULLETIN, new Bulletin(this));

        getLogger().info("Started YannyCraft plugin");
    }

    @Override
    public void onEnable() {
        pluginMap.forEach((key, value) -> value.onEnable());
        getLogger().info("Enabled YannyCraft plugin");
    }

    @Override
    public void onDisable() {
        pluginMap.forEach((type, plugin) -> plugin.onDisable());

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("Disabled YannyCraft plugin");
    }

    public Connection getConnection() {
        return connection;
    }

    public Auth getAuth() {
        return (Auth) pluginMap.get(PartPlugin.PartPluginType.AUTH);
    }

    public Essentials getEssentials() {
        return (Essentials) pluginMap.get(PartPlugin.PartPluginType.ESSENTIALS);
    }
}
