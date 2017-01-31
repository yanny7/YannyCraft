package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;

class Home {

    private static final String CFG_BACK = "backLocation";
    private static final String CFG_HOME = "homeLocation";

    private JavaPlugin plugin;
    private PlayerConfiguration playerConfiguration;
    private Auth auth;
    private ConfigurationSection section;
    private HomeListener homeListener;

    private BackExecutor backExecutor;
    private HomeExecutor homeExecutor;
    private SetHomeExecutor setHomeExecutor;

    Home(JavaPlugin plugin, PlayerConfiguration playerConfiguration, Auth auth) {
        this.plugin = plugin;
        this.playerConfiguration = playerConfiguration;
        this.auth = auth;
        FileConfiguration configuration = plugin.getConfig();

        section = configuration.getConfigurationSection("essentials");
        if (section == null) {
            section = configuration.createSection("essentials");
        }
        section.addDefault("msg_home_created", "Domov bol nastaveny");
        section.addDefault("msg_home", "Bol si teleportovany domov");
        section.addDefault("msg_back", "Bol si teleportovany na poslednu poziciu");

        homeListener = new HomeListener();
        backExecutor = new BackExecutor();
        homeExecutor = new HomeExecutor();
        setHomeExecutor = new SetHomeExecutor();
    }

    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(homeListener, plugin);
        plugin.getCommand("back").setExecutor(backExecutor);
        plugin.getCommand("home").setExecutor(homeExecutor);
        plugin.getCommand("sethome").setExecutor(setHomeExecutor);
    }

    void onDisable() {
    }

    class BackExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;
            PlayerConfigurationWrapper configuration = playerConfiguration.getConfiguration(player);

            if (configuration == null) {
                plugin.getLogger().warning("BackExecutor: Configuration not found!" + player.getDisplayName());
                return false;
            }

            ConfigurationSection homeSection = configuration.getConfigurationSection("home");
            player.teleport((Location) homeSection.get(CFG_BACK));
            player.sendMessage(ChatColor.GREEN + section.getString("msg_back"));
            return true;
        }
    }

    class HomeExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;
            PlayerConfigurationWrapper configuration = playerConfiguration.getConfiguration(player);

            if (configuration == null) {
                plugin.getLogger().warning("HomeExecutor: Configuration not found!" + player.getDisplayName());
                return false;
            }

            ConfigurationSection homeSection = configuration.getConfigurationSection("home");
            player.teleport((Location)homeSection.get(CFG_HOME));
            player.sendMessage(ChatColor.GREEN + section.getString("msg_home"));
            return true;
        }
    }

    class SetHomeExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;
            PlayerConfigurationWrapper configuration = playerConfiguration.getConfiguration(player);

            if (configuration == null) {
                plugin.getLogger().warning("SetHomeExecutor: Configuration not found!" + player.getDisplayName());
                return false;
            }

            ConfigurationSection homeSection = configuration.getConfigurationSection("home");
            homeSection.set(CFG_HOME, player.getLocation());
            player.sendMessage(ChatColor.GREEN + section.getString("msg_home_created"));
            return true;
        }
    }

    class HomeListener implements Listener {
        private EnumSet<PlayerTeleportEvent.TeleportCause> teleportCauses = EnumSet.of(PlayerTeleportEvent.TeleportCause.COMMAND, PlayerTeleportEvent.TeleportCause.PLUGIN, PlayerTeleportEvent.TeleportCause.UNKNOWN);

        @EventHandler
        void onPlayerAuth(PlayerAuthEvent event) {
            Player player = event.getPlayer();
            PlayerConfigurationWrapper configuration = playerConfiguration.getConfiguration(player);

            if (configuration == null) {
                plugin.getLogger().warning("onPlayerAuth: Configuration not found!" + player.getDisplayName());
                return;
            }

            ConfigurationSection homeSection = configuration.getConfigurationSection("home");
            if (homeSection == null) {
                homeSection = configuration.createSection("home");
                homeSection.set(CFG_HOME, section.get("spawn"));
                homeSection.set(CFG_BACK, section.get("spawn"));
            }
        }

        @EventHandler
        void onPlayerDeath(PlayerDeathEvent event) {
            Player player = event.getEntity();
            PlayerConfigurationWrapper configuration = playerConfiguration.getConfiguration(player);

            if (configuration == null) {
                plugin.getLogger().warning("onPlayerAuth: Configuration not found!" + player.getDisplayName());
                return;
            }

            ConfigurationSection homeSection = configuration.getConfigurationSection("home");
            homeSection.set(CFG_BACK, player.getLocation());
        }

        @EventHandler
        void onPlayerTeleport(PlayerTeleportEvent event) {
            Player player = event.getPlayer();
            Location from = event.getFrom();

            if (!teleportCauses.contains(event.getCause())) {
                plugin.getLogger().warning("Invalid cause: " + event.getCause().name());
                return;
            }

            if (!auth.isLogged(player)) {
                return;
            }

            PlayerConfigurationWrapper configuration = playerConfiguration.getConfiguration(player);

            if (configuration == null) {
                plugin.getLogger().warning("onPlayerTeleport: Configuration not found!" + player.getDisplayName());
                return;
            }

            ConfigurationSection homeSection = configuration.getConfigurationSection("home");
            homeSection.set(CFG_BACK, from);
        }
    }
}
