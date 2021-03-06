package me.noip.yanny.auth;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.LoggerHandler;
import me.noip.yanny.utils.PartPlugin;
import me.noip.yanny.utils.PlayerAuthEvent;
import me.noip.yanny.utils.PlayerRegisterEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.noip.yanny.auth.AuthTranslation.*;

public class Auth implements PartPlugin {

    private MainPlugin plugin;
    private LoggerHandler logger;

    private AuthConfiguration authConfiguration;

    private final Map<UUID, AuthPlayerWrapper> loggedPlayers = new HashMap<>();

    public Auth(MainPlugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLoggerHandler();

        authConfiguration = new AuthConfiguration(plugin);
    }

    @Override
    public void onEnable() {
        authConfiguration.load();

        plugin.getServer().getPluginManager().registerEvents(new AuthListener(), plugin);
        plugin.getCommand("login").setExecutor(new LoginExecutor());
        plugin.getCommand("register").setExecutor(new RegisterExecutor());
        plugin.getCommand("changepassword").setExecutor(new ChangePasswordExecutor());
        plugin.getCommand("resetpassword").setExecutor(new ResetPasswordExecutor());

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            AuthPlayerWrapper authPlayerWrapper = new AuthPlayerWrapper(plugin, player);
            authPlayerWrapper.loginAfterReload();
            loggedPlayers.put(player.getUniqueId(), authPlayerWrapper);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.remove(player.getUniqueId());

            if (authPlayerWrapper == null) {
                logger.logWarn(Auth.class, "PlayerQuitEvent: Player not found!" + player.getDisplayName());
                continue;
            }

            authPlayerWrapper.onQuit();
        }

        loggedPlayers.clear();
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
                logger.logWarn(Auth.class, "RegisterExecutor: Player not found!" + player.getDisplayName());
                return false;
            }

            RegisterStatus registerStatus = authPlayerWrapper.register(args[0], args[1]);

            if (registerStatus == RegisterStatus.REGISTERED) {
                plugin.getServer().getPluginManager().callEvent(new PlayerRegisterEvent(player));
                plugin.getServer().getPluginManager().callEvent(new PlayerAuthEvent(player));
            } else if (registerStatus == RegisterStatus.RESET_PASSWORD) {
                plugin.getServer().getPluginManager().callEvent(new PlayerAuthEvent(player));
            }

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
                logger.logWarn(Auth.class, "LoginExecutor: Player not found!" + player.getDisplayName());
                return false;
            }

            if (authPlayerWrapper.login(args[0])) {
                plugin.getServer().getPluginManager().callEvent(new PlayerAuthEvent(player));
            }

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
                logger.logWarn(Auth.class, "ChangePasswordExecutor: Player not found!" + player.getDisplayName());
                return false;
            }

            authPlayerWrapper.changePassword(args[0], args[1]);
            return true;
        }
    }

    class ResetPasswordExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 1)) {
                return false;
            }

            Player player = (Player)commandSender;
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());

            if (authPlayerWrapper == null) {
                logger.logWarn(Auth.class, "ResetPasswordExecutor: Player not found!" + player.getDisplayName());
                return false;
            }

            for (OfflinePlayer tmp : plugin.getServer().getOfflinePlayers()) {
                if (tmp.getName().equals(args[0])) {
                    authPlayerWrapper.resetPassword(tmp.getUniqueId());
                    if (tmp.getPlayer() != null) {
                        tmp.getPlayer().kickPlayer("Tvoje heslo bolo resetnute, zaregistruj sa znova");
                    }
                    return true;
                }
            }

            return false;
        }
    }

    class AuthListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            AuthPlayerWrapper authPlayerWrapper = new AuthPlayerWrapper(plugin, player);

            event.setJoinMessage(null);
            loggedPlayers.put(player.getUniqueId(), authPlayerWrapper);
            logger.logInfo(Auth.class, "Player joined: " + player.getDisplayName());
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.remove(player.getUniqueId());

            if (authPlayerWrapper == null) {
                logger.logWarn(Auth.class, "PlayerQuitEvent: Player not found!" + player.getDisplayName());
                return;
            }

            event.setQuitMessage(null);
            authPlayerWrapper.onQuit();
        }

        @SuppressWarnings("unused")
        @EventHandler
        public void OnPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());

            if (authPlayerWrapper == null) {
                logger.logWarn(Auth.class, "PlayerMoveEvent: Player not found!" + player.getDisplayName());
                return;
            }

            if (!authPlayerWrapper.isLogged()) {
                event.setCancelled(true);
            }
        }

        @SuppressWarnings("unused")
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChatCommand(PlayerCommandPreprocessEvent event){
            Player player = event.getPlayer();
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());

            if (authPlayerWrapper == null) {
                logger.logWarn(Auth.class, "PlayerCommandPreprocessEvent: Player not found!" + player.getDisplayName());
                return;
            }

            if (!authPlayerWrapper.isLogged()){
                String msg = event.getMessage();

                if (!msg.startsWith("/login") && !msg.startsWith("/register")) {
                    player.sendMessage(ERR_COMMAND_PERMISSION.display());
                    event.setCancelled(true);
                }
            }
        }

        @SuppressWarnings("unused")
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onChat(AsyncPlayerChatEvent event){
            Player player = event.getPlayer();
            AuthPlayerWrapper authPlayerWrapper = loggedPlayers.get(player.getUniqueId());

            if (authPlayerWrapper == null) {
                logger.logWarn(Auth.class, "AsyncPlayerChatEvent: Player not found!" + player.getDisplayName());
                return;
            }

            if (event.getMessage().startsWith("/")) {
                return; // do not manage commands
            }

            if (!authPlayerWrapper.isLogged()){
                player.sendMessage(ERR_CHAT_PERMISSION.display());
                event.setCancelled(true);
            }
        }
    }
}
