package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.Statement;

class AuthPlayerWrapper {

    private JavaPlugin plugin;
    private Player player;
    private Statement statement;
    private Location loginLocation;
    private GameMode loginGameMode;
    private AuthConfiguration authConfiguration;
    private boolean logged;
    private boolean registered;

    AuthPlayerWrapper(JavaPlugin plugin, Player player, Statement statement, AuthConfiguration authConfiguration, EssentialsConfiguration essentialsConfiguration) {
        this.plugin = plugin;
        this.player = player;
        this.statement = statement;
        this.authConfiguration = authConfiguration;

        if (restoreData(player)) {
            loginLocation = player.getLocation();
            loginGameMode = player.getGameMode();
            registered = true;
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_login"));
        } else {
            loginLocation = essentialsConfiguration.getSpawnLocation(player);
            loginGameMode = GameMode.SURVIVAL;
            registered = false;
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_register"));
        }

        logged = false;

        Location location = essentialsConfiguration.getSpawnLocation(player);
        if (location == null) {
            location = player.getWorld().getSpawnLocation();
        }

        player.teleport(location);
        player.setGameMode(GameMode.SPECTATOR);
    }

    void onQuit() {
        if (registered) {
            storeData(player);
            plugin.getServer().broadcastMessage(ChatColor.GOLD + authConfiguration.getTranslation("msg_disconnect_all").replace("{player}", ChatColor.GREEN + player.getDisplayName() + ChatColor.GOLD));
        }
    }

    void register(String password, String passwordAgain) {
        if (logged || registered) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_registered"));
            return;
        }
        if (!password.equals(passwordAgain)) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_password_not_same"));
            return;
        }
        if (!password.matches("[A-Za-z0-9]+")) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_characters"));
            return;
        }
        if ((password.length() < 6) || (password.length() > 32)) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_length"));
            return;
        }

        try {
            String goodPassword = PasswordHash.createHash(password);
            statement.executeUpdate("INSERT INTO users (ID, Password, Inventory, LastUpdated) "
                    + "VALUES ('" + player.getUniqueId().toString() + "',"
                    + "'" + goodPassword + "',"
                    + "'" + playerToString(player) + "',"
                    + "DATETIME('now'))");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_register"));
            return;
        }

        registered = true;
        logged = true;
        plugin.getServer().broadcastMessage(ChatColor.GOLD + authConfiguration.getTranslation("msg_registered_all").replace("{player}", ChatColor.GREEN + player.getDisplayName() + ChatColor.GOLD));
        player.sendMessage(ChatColor.GREEN + authConfiguration.getTranslation("msg_registered"));
        player.setGameMode(GameMode.SURVIVAL);
    }

    void login(String password) {
        if (!registered) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_not_registered"));
            return;
        }
        if (logged) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_logged"));
            return;
        }

        try {
            if (!checkPassword(password)) {
                player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_wrong_password"));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        logged = true;
        plugin.getServer().broadcastMessage(ChatColor.GOLD + authConfiguration.getTranslation("msg_logged_all").replace("{player}", ChatColor.GREEN + player.getDisplayName() + ChatColor.GOLD));
        player.sendMessage(ChatColor.GREEN + authConfiguration.getTranslation("msg_logged"));
        player.setGameMode(loginGameMode);
        player.teleport(loginLocation);
    }

    void loginAfterReload() {
        logged = true;
        player.sendMessage(ChatColor.GREEN + authConfiguration.getTranslation("msg_logged"));
        player.setGameMode(loginGameMode);
        player.teleport(loginLocation);
    }

    void changePassword(String password, String passwordAgain) {
        if (!logged || !registered) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_registered"));
        }
        if (!password.equals(passwordAgain)) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_password_not_same"));
            return;
        }
        if (!password.matches("[A-Za-z0-9]+")) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_characters"));
            return;
        }
        if ((password.length() < 6) || (password.length() > 32)) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_length"));
            return;
        }

        try {
            String goodPassword = PasswordHash.createHash(password);
            statement.executeUpdate("UPDATE users SET Password = '" + goodPassword + "' WHERE ID = '" + player.getUniqueId().toString() + "'");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_password"));
            return;
        }

        player.sendMessage(ChatColor.GREEN + authConfiguration.getTranslation("msg_password_changed"));
    }

    boolean isLogged() {
        return logged;
    }

    private boolean checkPassword(String password) {
        try {
            ResultSet rs = statement.executeQuery("SELECT Password FROM users WHERE ID = '" + player.getUniqueId().toString() + "'");

            if (rs.next()) {
                String hash = rs.getString("Password");

                return PasswordHash.validatePassword(password, hash);
            }

            rs.close();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean storeData(Player player) {
        String sql = "UPDATE users SET Inventory = '" + playerToString(player) + "', LastUpdated = DATETIME('now') where ID = '" + player.getUniqueId() + "'";

        try {
            statement.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean restoreData(Player player) {
        try {
            ResultSet rs = statement.executeQuery("SELECT Inventory FROM users WHERE ID = '" + player.getUniqueId().toString() + "'");

            if (rs.next()) {
                String invData = rs.getString("Inventory");
                stringToPlayer(invData, player);
                return true;
            }

            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static String playerToString(Player player) {
        YamlConfiguration yaml = new YamlConfiguration();
        PlayerInventory inv = player.getInventory();
        Inventory endInv = player.getEnderChest();

        for (int i = 0; i < inv.getContents().length; i++) {
            yaml.set("item" + i, inv.getItem(i));
        }
        for (int i = 0; i < endInv.getContents().length; i++) {
            yaml.set("endItem" + i, endInv.getItem(i));
        }
        yaml.set("health", player.getHealth());
        yaml.set("food", player.getFoodLevel());
        yaml.set("exp", (double) player.getExp());
        yaml.set("level", player.getLevel());
        yaml.set("location", player.getLocation());
        yaml.set("gamemode", player.getGameMode().toString());

        return yaml.saveToString();
    }

    private static void stringToPlayer(String data, Player player) {
        YamlConfiguration yaml = new YamlConfiguration();
        PlayerInventory inv = player.getInventory();
        Inventory endInv = player.getEnderChest();

        try {
            yaml.loadFromString(data);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < inv.getContents().length; i++) {
            ItemStack item = (ItemStack)yaml.get("item" + i);
            inv.setItem(i, item);
        }
        for (int i = 0; i < endInv.getContents().length; i++) {
            ItemStack item = (ItemStack)yaml.get("endItem" + i);
            endInv.setItem(i, item);
        }

        double health = (Double) yaml.get("health");
        int food = (Integer) yaml.get("food");
        double exp = (Double) yaml.get("exp");
        int level = (Integer) yaml.get("level");
        Location location = (Location) yaml.get("location");
        GameMode gameMode = GameMode.valueOf(yaml.getString("gamemode"));

        player.setHealth(health);
        player.setFoodLevel(food);
        player.setExp((float) exp);
        player.setLevel(level);
        player.teleport(location);
        player.setGameMode(gameMode);
    }

}
