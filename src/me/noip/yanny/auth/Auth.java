package me.noip.yanny.auth;

import me.noip.yanny.essentials.SpawnLocationProvider;
import me.noip.yanny.utils.PartPlugin;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Auth implements PartPlugin {

    private JavaPlugin plugin;
    private Connection connection;
    private LoginExecutor loginExecutor;
    private RegisterExecutor registerExecutor;
    private ChangePasswordExecutor changePasswordExecutor;
    private AuthListener authListener;
    private AuthConfiguration authConfiguration;
    private SpawnLocationProvider spawnLocation;

    private final Map<UUID, AuthPlayerWrapper> loggedPlayers = new HashMap<>();

    public Auth(JavaPlugin plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
        authConfiguration = new AuthConfiguration(plugin);
        authListener = new AuthListener();
        loginExecutor = new LoginExecutor();
        registerExecutor = new RegisterExecutor();
        changePasswordExecutor = new ChangePasswordExecutor();
    }

    @Override
    public void onEnable() {
        authConfiguration.load();

        plugin.getServer().getPluginManager().registerEvents(authListener, plugin);
        plugin.getCommand("login").setExecutor(loginExecutor);
        plugin.getCommand("register").setExecutor(registerExecutor);
        plugin.getCommand("changepassword").setExecutor(changePasswordExecutor);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            AuthPlayerWrapper authPlayerWrapper = new AuthPlayerWrapper(plugin, player, connection, authConfiguration, spawnLocation);
            authPlayerWrapper.loginAfterReload();
            loggedPlayers.put(player.getUniqueId(), authPlayerWrapper);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.remove(player.getUniqueId());

            if (authPlayerWrapper == null) {
                plugin.getLogger().warning("PlayerQuitEvent: Player not found!" + player.getDisplayName());
                continue;
            }

            authPlayerWrapper.onQuit();
        }

        loggedPlayers.clear();
    }

    public void setSpawnLocationProvider(SpawnLocationProvider location) {
        this.spawnLocation = location;
    }

    public boolean isLogged(Player player) {
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
            AuthPlayerWrapper authPlayerWrapper = new AuthPlayerWrapper(plugin, player, connection, authConfiguration, spawnLocation);

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
