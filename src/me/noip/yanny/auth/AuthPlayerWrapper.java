package me.noip.yanny.auth;

import me.noip.yanny.essentials.SpawnLocationProvider;
import me.noip.yanny.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.omg.PortableInterceptor.SUCCESSFUL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;

class AuthPlayerWrapper {

    private PreparedStatement registerStatement;
    private PreparedStatement changePasswordStatement;
    private PreparedStatement getPasswordStatement;
    private PreparedStatement setInventoryStatement;
    private PreparedStatement getInventoryStatement;
    private PreparedStatement resetStatement;
    private PreparedStatement getResetStatement;

    private JavaPlugin plugin;
    private Player player;
    private Location loginLocation;
    private GameMode loginGameMode;
    private AuthConfiguration authConfiguration;
    private SpawnLocationProvider locationProvider;
    private boolean logged;
    private boolean registered;

    AuthPlayerWrapper(JavaPlugin plugin, Player player, Connection connection, AuthConfiguration authConfiguration, SpawnLocationProvider locationProvider) {
        this.plugin = plugin;
        this.player = player;
        this.authConfiguration = authConfiguration;
        this.locationProvider = locationProvider;

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
            }

            if (resetPassword) {
                registered = false;
                player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_register"));
            } else {
                registered = true;
                player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_login"));
            }
        } else {
            loginLocation = locationProvider.getSpawnLocation();
            loginGameMode = GameMode.SURVIVAL;
            registered = false;
            resetPlayer(player);
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_register"));
        }

        logged = false;

        Location location = locationProvider.getSpawnLocation();
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

    RegisterStatus register(String password, String passwordAgain) {
        if (logged || registered) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_registered"));
            return RegisterStatus.FAILED;
        }
        if (!password.equals(passwordAgain)) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_password_not_same"));
            return RegisterStatus.FAILED;
        }
        if (!password.matches("[A-Za-z0-9]+")) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_characters"));
            return RegisterStatus.FAILED;
        }
        if ((password.length() < 6) || (password.length() > 32)) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_length"));
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
        }

        if (resetPassword) {
            try {
                String goodPassword = PasswordHash.createHash(password);
                changePasswordStatement.setString(1, goodPassword);
                changePasswordStatement.setString(2, player.getUniqueId().toString());
                changePasswordStatement.execute();
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_password"));
                return RegisterStatus.FAILED;
            }
        } else {
            try {
                String goodPassword = PasswordHash.createHash(password);
                String spawnLocation = Utils.locationToString(locationProvider.getSpawnLocation());
                registerStatement.setString(1, player.getUniqueId().toString());
                registerStatement.setString(2, goodPassword);
                registerStatement.setString(3, playerToString(player));
                registerStatement.setString(4, spawnLocation);
                registerStatement.setString(5, spawnLocation);
                registerStatement.execute();
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_register"));
                return RegisterStatus.FAILED;
            }
        }

        registered = true;
        logged = true;
        plugin.getServer().broadcastMessage(ChatColor.GOLD + authConfiguration.getTranslation("msg_registered_all").replace("{player}", ChatColor.GREEN + player.getDisplayName() + ChatColor.GOLD));
        player.sendMessage(ChatColor.GREEN + authConfiguration.getTranslation("msg_registered"));
        player.setGameMode(GameMode.SURVIVAL);

        if (resetPassword) {
            return RegisterStatus.RESET_PASSWORD;
        } else {
            return RegisterStatus.REGISTERED;
        }
    }

    boolean login(String password) {
        if (!registered) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_not_registered"));
            return false;
        }
        if (logged) {
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_logged"));
            return false;
        }

        try {
            if (!checkPassword(password)) {
                player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_wrong_password"));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        logged = true;
        plugin.getServer().broadcastMessage(ChatColor.GOLD + authConfiguration.getTranslation("msg_logged_all").replace("{player}", ChatColor.GREEN + player.getDisplayName() + ChatColor.GOLD));
        player.sendMessage(ChatColor.GREEN + authConfiguration.getTranslation("msg_logged"));
        player.setGameMode(loginGameMode);
        player.teleport(loginLocation);
        return true;
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
            changePasswordStatement.setString(1, goodPassword);
            changePasswordStatement.setString(2, player.getUniqueId().toString());
            changePasswordStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_password"));
            return;
        }

        player.sendMessage(ChatColor.GREEN + authConfiguration.getTranslation("msg_password_changed"));
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
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
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
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            potionEffectsSection.set(potionEffect.getType().getName(), potionEffect);
        }

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

        ConfigurationSection potionEffectsSection = yaml.getConfigurationSection("potionEffects");
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        if (potionEffectsSection != null) {
            Map<String, Object> objects = potionEffectsSection.getValues(false);
            for (Map.Entry<String, Object> entry : objects.entrySet()) {
                if (entry.getValue() instanceof PotionEffect) {
                    player.addPotionEffect((PotionEffect) entry.getValue());
                } else {
                    plugin.getLogger().warning("AuthPlayerWrapper.stringToPlayer: Object is not a PotionEffect");
                }
            }
        }

        player.setBedSpawnLocation((Location) yaml.get("bedLocation"));
    }

}
