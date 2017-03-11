package me.noip.yanny.auth;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.essentials.Essentials;
import me.noip.yanny.utils.LoggerHandler;
import me.noip.yanny.utils.Utils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;

import static me.noip.yanny.auth.AuthTranslation.*;

class AuthPlayerWrapper {

    private PreparedStatement registerStatement;
    private PreparedStatement changePasswordStatement;
    private PreparedStatement getPasswordStatement;
    private PreparedStatement setInventoryStatement;
    private PreparedStatement getInventoryStatement;
    private PreparedStatement resetStatement;
    private PreparedStatement getResetStatement;

    private LoggerHandler logger;
    private Server server;
    private Essentials essentials;
    private Player player;
    private Location loginLocation;
    private GameMode loginGameMode;
    private boolean logged;
    private boolean registered;

    AuthPlayerWrapper(MainPlugin plugin, Player player) {
        this.player = player;
        logger = plugin.getLoggerHandler();
        server = plugin.getServer();
        essentials = plugin.getEssentials();

        Connection connection = plugin.getConnection();

        try {
            registerStatement = connection.prepareStatement("INSERT INTO users (ID, Password, Inventory, HomeLocation, BackLocation, LastUpdated) VALUES (?, ?, ?, ?, ?, DATETIME('now'))");
            changePasswordStatement = connection.prepareStatement("UPDATE users SET Password = ?, ResetPassword = 0 WHERE ID = ?");
            getPasswordStatement = connection.prepareStatement("SELECT Password FROM users WHERE ID = ?");
            setInventoryStatement = connection.prepareStatement("UPDATE users SET Inventory = ?, LastUpdated = DATETIME('now') where ID = ?");
            getInventoryStatement = connection.prepareStatement("SELECT Inventory FROM users WHERE ID = ?");
            resetStatement = connection.prepareStatement("UPDATE users SET ResetPassword = 1 WHERE ID = ?");
            getResetStatement = connection.prepareStatement("SELECT ResetPassword FROM users WHERE ID = ?");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (restoreData(player)) {
            boolean resetPassword = false;
            loginLocation = player.getLocation();
            loginGameMode = player.getGameMode();

            try {
                getResetStatement.setString(1, player.getUniqueId().toString());
                ResultSet rs = getResetStatement.executeQuery();

                if (rs.next()) {
                    resetPassword = rs.getBoolean(1);
                }

                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (resetPassword) {
                registered = false;
                player.sendMessage(REGISTER.display());
            } else {
                registered = true;
                player.sendMessage(LOGIN.display());
            }
        } else {
            loginLocation = plugin.getEssentials().getSpawnLocation();
            loginGameMode = GameMode.SURVIVAL;
            registered = false;
            resetPlayer(player);
            player.sendMessage(REGISTER.display());
        }

        logged = false;

        Location location = plugin.getEssentials().getSpawnLocation();
        if (location == null) {
            location = player.getWorld().getSpawnLocation();
        }

        player.teleport(location);
        player.setGameMode(GameMode.SPECTATOR);
    }

    void onQuit() {
        if (registered) {
            storeData(player);
            server.broadcastMessage(DISCONNECT_ALL.display().replace("{player}", ChatColor.GREEN + player.getDisplayName() + DISCONNECT_ALL.getChatColor()));
        } else {
            logger.logInfo(Auth.class, "Unregistered player has disconnected: " + player);
        }
    }

    RegisterStatus register(String password, String passwordAgain) {
        if (logged || registered) {
            player.sendMessage(ERR_REGISTERED.display());
            return RegisterStatus.FAILED;
        }
        if (!password.equals(passwordAgain)) {
            player.sendMessage(ERR_PASSWORDS_NOT_SAME.display());
            return RegisterStatus.FAILED;
        }
        if (!password.matches("[A-Za-z0-9]+")) {
            player.sendMessage(ERR_CHARACTERS.display());
            return RegisterStatus.FAILED;
        }
        if ((password.length() < 6) || (password.length() > 32)) {
            player.sendMessage(ERR_LENGTH.display());
            return RegisterStatus.FAILED;
        }

        boolean resetPassword = false;
        try {
            getResetStatement.setString(1, player.getUniqueId().toString());
            ResultSet rs = getResetStatement.executeQuery();

            if (rs.next()) {
                resetPassword = rs.getBoolean(1);
            }

            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ERR_PASSWORD_CHANGE.display());
            return RegisterStatus.FAILED;
        }

        if (resetPassword) {
            try {
                String goodPassword = PasswordHash.createHash(password);
                changePasswordStatement.setString(1, goodPassword);
                changePasswordStatement.setString(2, player.getUniqueId().toString());
                changePasswordStatement.execute();
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ERR_PASSWORD_CHANGE.display());
                return RegisterStatus.FAILED;
            }
        } else {
            try {
                String goodPassword = PasswordHash.createHash(password);
                String spawnLocation = Utils.locationToString(essentials.getSpawnLocation());
                registerStatement.setString(1, player.getUniqueId().toString());
                registerStatement.setString(2, goodPassword);
                registerStatement.setString(3, playerToString(player));
                registerStatement.setString(4, spawnLocation);
                registerStatement.setString(5, spawnLocation);
                registerStatement.execute();
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ERR_REGISTER.display());
                return RegisterStatus.FAILED;
            }
        }

        registered = true;
        logged = true;
        server.broadcastMessage(REGISTERED_ALL.display().replace("{player}", ChatColor.GREEN + player.getDisplayName() + REGISTERED_ALL.getChatColor()));
        player.sendMessage(REGISTERED.display());
        player.setGameMode(GameMode.SURVIVAL);

        if (resetPassword) {
            return RegisterStatus.RESET_PASSWORD;
        } else {
            return RegisterStatus.REGISTERED;
        }
    }

    boolean login(String password) {
        if (!registered) {
            player.sendMessage(ERR_NOT_REGISTERED.display());
            return false;
        }
        if (logged) {
            player.sendMessage(ERR_LOGGED.display());
            return false;
        }

        try {
            if (!checkPassword(password)) {
                player.sendMessage(ERR_WRONG_PASSWORD.display());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        logged = true;
        server.broadcastMessage(LOGGED_ALL.display().replace("{player}", ChatColor.GREEN + player.getDisplayName() + LOGGED_ALL.getChatColor()));
        player.sendMessage(LOGGED.display());
        player.setGameMode(loginGameMode);
        player.teleport(loginLocation);
        return true;
    }

    void loginAfterReload() {
        logged = true;
        player.setGameMode(loginGameMode);
        player.teleport(loginLocation);
    }

    void changePassword(String password, String passwordAgain) {
        if (!logged || !registered) {
            player.sendMessage(ERR_REGISTERED.display());
        }
        if (!password.equals(passwordAgain)) {
            player.sendMessage(ERR_PASSWORDS_NOT_SAME.display());
            return;
        }
        if (!password.matches("[A-Za-z0-9]+")) {
            player.sendMessage(ERR_CHARACTERS.display());
            return;
        }
        if ((password.length() < 6) || (password.length() > 32)) {
            player.sendMessage(ERR_LENGTH.display());
            return;
        }

        try {
            String goodPassword = PasswordHash.createHash(password);
            changePasswordStatement.setString(1, goodPassword);
            changePasswordStatement.setString(2, player.getUniqueId().toString());
            changePasswordStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ERR_PASSWORD_CHANGE.display());
            return;
        }

        player.sendMessage(PASSWORD_CHANGED.display());
    }

    void resetPassword(UUID uuid) {
        try {
            resetStatement.setString(1, uuid.toString());
            resetStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isLogged() {
        return logged;
    }

    private boolean checkPassword(String password) {
        try {
            getPasswordStatement.setString(1, player.getUniqueId().toString());
            ResultSet rs = getPasswordStatement.executeQuery();

            if (rs.next()) {
                String hash = rs.getString(1);

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
        try {
            setInventoryStatement.setString(1, playerToString(player));
            setInventoryStatement.setString(2, player.getUniqueId().toString());
            setInventoryStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean restoreData(Player player) {
        try {
            getInventoryStatement.setString(1, player.getUniqueId().toString());
            ResultSet rs = getInventoryStatement.executeQuery();

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

    private void resetPlayer(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.AIR));
        }
        Inventory endInventory = player.getEnderChest();
        for (int i = 0; i < endInventory.getSize(); i++) {
            endInventory.setItem(i, new ItemStack(Material.AIR));
        }
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        player.teleport(player.getWorld().getSpawnLocation());
        player.setGameMode(GameMode.SURVIVAL);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.setBedSpawnLocation(player.getWorld().getSpawnLocation());
    }

    private String playerToString(Player player) {
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

        ConfigurationSection potionEffectsSection = yaml.createSection("potionEffects");
        player.getActivePotionEffects().forEach(potionEffect -> potionEffectsSection.set(potionEffect.getType().getName(), potionEffect));

        yaml.set("bedLocation", player.getBedSpawnLocation());

        return yaml.saveToString();
    }

    private void stringToPlayer(String data, Player player) {
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

        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        ConfigurationSection potionEffectsSection = yaml.getConfigurationSection("potionEffects");

        if (potionEffectsSection != null) {
            Map<String, Object> objects = potionEffectsSection.getValues(false);
            for (Map.Entry<String, Object> entry : objects.entrySet()) {
                if (entry.getValue() instanceof PotionEffect) {
                    player.addPotionEffect((PotionEffect) entry.getValue());
                } else {
                    logger.logWarn(Auth.class, "AuthPlayerWrapper.stringToPlayer: Object is not a PotionEffect");
                }
            }
        }

        player.setBedSpawnLocation((Location) yaml.get("bedLocation"));
    }

}
