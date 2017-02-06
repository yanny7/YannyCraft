package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class Auth {

    private JavaPlugin plugin;
    private LoginExecutor loginExecutor;
    private RegisterExecutor registerExecutor;
    private ChangePasswordExecutor changePasswordExecutor;
    private AuthListener authListener;
    private Connection connection = null;
    private Statement statement = null;
    private static final String DATABASE = "users.db";
    private AuthConfiguration authConfiguration;
    private EssentialsConfiguration essentialsConfiguration;

    private final Map<UUID, AuthPlayerWrapper> loggedPlayers = new HashMap<>();

    Auth(JavaPlugin plugin) {
        this.plugin = plugin;
        authConfiguration = new AuthConfiguration(plugin);
        authListener = new AuthListener();
        loginExecutor = new LoginExecutor();
        registerExecutor = new RegisterExecutor();
        changePasswordExecutor = new ChangePasswordExecutor();
    }

    void onEnable() {
        authConfiguration.load();

        plugin.getServer().getPluginManager().registerEvents(authListener, plugin);
        plugin.getCommand("login").setExecutor(loginExecutor);
        plugin.getCommand("register").setExecutor(registerExecutor);
        plugin.getCommand("changepassword").setExecutor(changePasswordExecutor);

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/" + DATABASE);

            statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "ID VARCHAR(64) PRIMARY KEY NOT NULL,"
                    + "Password VARCHAR(64) NOT NULL,"
                    + "Inventory TEXT NOT NULL,"
                    + "LastUpdated DATETIME NOT NULL"
                    + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            AuthPlayerWrapper authPlayerWrapper = new AuthPlayerWrapper(plugin, player, statement, authConfiguration, essentialsConfiguration);
            authPlayerWrapper.loginAfterReload();
            loggedPlayers.put(player.getUniqueId(), authPlayerWrapper);
        }
    }

    void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.remove(player.getUniqueId());

            if (authPlayerWrapper == null) {
                plugin.getLogger().warning("PlayerQuitEvent: Player not found!" + player.getDisplayName());
                continue;
            }

            authPlayerWrapper.onQuit();
        }

        loggedPlayers.clear();

        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setEssentialsConfiguration(EssentialsConfiguration essentialsConfiguration) {
        this.essentialsConfiguration = essentialsConfiguration;
    }

    boolean isLogged(Player player) {
        AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());
        return (authPlayerWrapper != null) && (authPlayerWrapper.isLogged());
    }

    /*
     * CLASSES
     */

    class RegisterExecutor implements  CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 2)) {
                return false;
            }

            Player player = (Player)commandSender;
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());

            if (authPlayerWrapper == null) {
                plugin.getLogger().warning("RegisterExecutor: Player not found!" + player.getDisplayName());
                return false;
            }

            authPlayerWrapper.register(args[0], args[1]);
            plugin.getServer().getPluginManager().callEvent(new PlayerRegisterEvent(player));
            plugin.getServer().getPluginManager().callEvent(new PlayerAuthEvent(player));
            return true;
        }
    }

    class LoginExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 1)) {
                return false;
            }

            Player player = (Player)commandSender;
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());

            if (authPlayerWrapper == null) {
                plugin.getLogger().warning("LoginExecutor: Player not found!" + player.getDisplayName());
                return false;
            }

            authPlayerWrapper.login(args[0]);
            plugin.getServer().getPluginManager().callEvent(new PlayerAuthEvent(player));
            return true;
        }
    }

    class ChangePasswordExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 2)) {
                return false;
            }

            Player player = (Player)commandSender;
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());

            if (authPlayerWrapper == null) {
                plugin.getLogger().warning("ChangePasswordExecutor: Player not found!" + player.getDisplayName());
                return false;
            }

            authPlayerWrapper.changePassword(args[0], args[1]);
            return false;
        }
    }

    class AuthListener implements Listener {
        @EventHandler
        void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            AuthPlayerWrapper authPlayerWrapper = new AuthPlayerWrapper(plugin, player, statement, authConfiguration, essentialsConfiguration);

            event.setJoinMessage(null);
            loggedPlayers.put(player.getUniqueId(), authPlayerWrapper);
            plugin.getLogger().info("Player joined: " + player.getDisplayName());
        }

        @EventHandler
        void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.remove(player.getUniqueId());

            if (authPlayerWrapper == null) {
                plugin.getLogger().warning("PlayerQuitEvent: Player not found!" + player.getDisplayName());
                return;
            }

            event.setQuitMessage(null);
            authPlayerWrapper.onQuit();
        }

        @EventHandler
        public void OnPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());

            if (authPlayerWrapper == null) {
                plugin.getLogger().warning("PlayerMoveEvent: Player not found!" + player.getDisplayName());
                return;
            }

            if (!authPlayerWrapper.isLogged()) {
                event.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChatCommand(PlayerCommandPreprocessEvent event){
            Player player = event.getPlayer();
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());

            if (authPlayerWrapper == null) {
                plugin.getLogger().warning("PlayerMoveEvent: Player not found!" + player.getDisplayName());
                return;
            }

            if (!authPlayerWrapper.isLogged()){
                String msg = event.getMessage();

                if (!msg.startsWith("/login") && !msg.startsWith("/register")) {
                    player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_command"));
                    event.setCancelled(true);
                }
            }

        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChat(AsyncPlayerChatEvent event){
            Player player = event.getPlayer();
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());

            if (authPlayerWrapper == null) {
                plugin.getLogger().warning("PlayerMoveEvent: Player not found!" + player.getDisplayName());
                return;
            }

            if (event.getMessage().startsWith("/")) {
                return; // do not manage commands
            }

            if (!authPlayerWrapper.isLogged()){
                player.sendMessage(ChatColor.RED + authConfiguration.getTranslation("msg_err_chat"));
                event.setCancelled(true);
            }

        }

    }

}
