package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class Essentials {

    private JavaPlugin plugin;
    private PlayerConfiguration playerConfiguration;
    private Auth auth;

    private Map<UUID, PermissionAttachment> permissionAttachment;
    private Map<Player, Player> teleportRequest;
    private EssentialsConfiguration essentialsConfiguration;

    private EssentialsListener essentialsListener;
    private SpawnExecutor spawnExecutor;
    private SetSpawnExecutor setSpawnExecutor;
    private TpExecutor tpExecutor;
    private TpaExecutor tpaExecutor;
    private TphereExecutor tphereExecutor;
    private TpacceptExecutor tpacceptExecutor;
    private TpdenyExecutor tpdenyExecutor;
    private HealExecutor healExecutor;
    private FeedExecutor feedExecutor;
    private BackExecutor backExecutor;
    private HomeExecutor homeExecutor;
    private SetHomeExecutor setHomeExecutor;

    Essentials(JavaPlugin plugin, PlayerConfiguration playerConfiguration, Auth auth) {
        this.plugin = plugin;
        this.playerConfiguration = playerConfiguration;
        this.auth = auth;

        permissionAttachment = new HashMap<>();
        teleportRequest = new HashMap<>();
        essentialsConfiguration = new EssentialsConfiguration(plugin);

        essentialsListener = new EssentialsListener();
        spawnExecutor = new SpawnExecutor();
        setSpawnExecutor = new SetSpawnExecutor();
        tpExecutor = new TpExecutor();
        tpaExecutor = new TpaExecutor();
        tphereExecutor = new TphereExecutor();
        tpacceptExecutor = new TpacceptExecutor();
        tpdenyExecutor = new TpdenyExecutor();
        healExecutor = new HealExecutor();
        feedExecutor = new FeedExecutor();
        backExecutor = new BackExecutor();
        homeExecutor = new HomeExecutor();
        setHomeExecutor = new SetHomeExecutor();
    }

    void onEnable() {
        essentialsConfiguration.load();

        plugin.getServer().getPluginManager().registerEvents(essentialsListener, plugin);
        plugin.getCommand("spawn").setExecutor(spawnExecutor);
        plugin.getCommand("setspawn").setExecutor(setSpawnExecutor);
        plugin.getCommand("tp").setExecutor(tpExecutor);
        plugin.getCommand("tpa").setExecutor(tpaExecutor);
        plugin.getCommand("tphere").setExecutor(tphereExecutor);
        plugin.getCommand("tpaccept").setExecutor(tpacceptExecutor);
        plugin.getCommand("tpdeny").setExecutor(tpdenyExecutor);
        plugin.getCommand("heal").setExecutor(healExecutor);
        plugin.getCommand("feed").setExecutor(feedExecutor);
        plugin.getCommand("back").setExecutor(backExecutor);
        plugin.getCommand("home").setExecutor(homeExecutor);
        plugin.getCommand("sethome").setExecutor(setHomeExecutor);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            permissionAttachment.put(player.getUniqueId(), player.addAttachment(plugin));
        }
    }

    void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PermissionAttachment attachment = permissionAttachment.remove(player.getUniqueId());
            player.removeAttachment(attachment);
        }

        permissionAttachment.clear();
        teleportRequest.clear();
    }

    EssentialsConfiguration getEssentialsConfiguration() {
        return essentialsConfiguration;
    }

    class SpawnExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length > 1)) {
                return false;
            }

            Player player = (Player)commandSender;
            PermissionAttachment permission = permissionAttachment.get(player.getUniqueId());

            if (permission == null) {
                plugin.getLogger().warning("SpawnExecutor: PermissionAttachment not found!" + player.getDisplayName());
                return false;
            }

            if (args.length == 0) {
                player.teleport(essentialsConfiguration.getSpawnLocation(player));
            } else {
                if (player.hasPermission("yannycraft.spawn.other")) {
                    Player target = plugin.getServer().getPlayer(args[0]);

                    if (target != null) {
                        target.teleport(essentialsConfiguration.getSpawnLocation(player));
                    } else {
                        player.sendMessage(ChatColor.RED + essentialsConfiguration.getTranslation("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + essentialsConfiguration.getTranslation("msg_err_permission"));
                }
            }

            return true;
        }
    }

    class SetSpawnExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;
            Location location = player.getLocation();
            essentialsConfiguration.setSpawnLocation(location);
            player.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ()); // update also world location
            player.sendMessage(ChatColor.GREEN + essentialsConfiguration.getTranslation("msg_spawn_set"));
            return true;
        }
    }

    class TpExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 1)) {
                return false;
            }

            Player player = (Player) commandSender;
            Player target = plugin.getServer().getPlayer(args[0]);

            if (target != null) {
                player.teleport(target.getLocation());
            } else {
                player.sendMessage(ChatColor.RED + essentialsConfiguration.getTranslation("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
            }
            return true;
        }
    }

    class TpaExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 1)) {
                return false;
            }

            Player player = (Player) commandSender;
            Player target = plugin.getServer().getPlayer(args[0]);

            if (target != null) {
                player.sendMessage(ChatColor.GREEN + essentialsConfiguration.getTranslation("msg_tpa_sended"));
                target.sendMessage(ChatColor.GREEN + essentialsConfiguration.getTranslation("msg_tpa_received").replace("{player}", ChatColor.GOLD + player.getDisplayName() + ChatColor.GREEN));
                teleportRequest.put(target, player);
            } else {
                player.sendMessage(ChatColor.RED + essentialsConfiguration.getTranslation("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
            }
            return true;
        }
    }

    class TphereExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 1)) {
                return false;
            }

            Player player = (Player) commandSender;
            Player target = plugin.getServer().getPlayer(args[0]);

            if (target != null) {
                target.teleport(player.getLocation());
                target.sendMessage(ChatColor.GREEN + essentialsConfiguration.getTranslation("msg_teleported").replace("{player}", ChatColor.GOLD + player.getDisplayName() + ChatColor.GREEN));
            } else {
                player.sendMessage(ChatColor.RED + essentialsConfiguration.getTranslation("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
            }
            return true;
        }
    }

    class TpacceptExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;

            if (teleportRequest.containsKey(player)) {
                Player target = teleportRequest.remove(player);

                if (target != null) {
                    target.teleport(player.getLocation());
                } else {
                    player.sendMessage(ChatColor.RED + essentialsConfiguration.getTranslation("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
                }
            }

            return true;
        }
    }

    class TpdenyExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;

            if (teleportRequest.containsKey(player)) {
                Player target = teleportRequest.remove(player);
                player.sendMessage(ChatColor.GREEN + essentialsConfiguration.getTranslation("msg_tpdeny"));
                target.sendMessage(ChatColor.RED + essentialsConfiguration.getTranslation("msg_tpdeny"));
            }
            return true;
        }
    }

    class HealExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length > 1)) {
                return false;
            }

            Player player = (Player) commandSender;

            if (args.length == 0) {
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            } else {
                Player target = plugin.getServer().getPlayer(args[0]);

                if (target != null) {
                    target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                } else {
                    player.sendMessage(ChatColor.RED + essentialsConfiguration.getTranslation("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
                }
            }
            return true;
        }
    }

    class FeedExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length > 1)) {
                return false;
            }

            Player player = (Player) commandSender;

            if (args.length == 0) {
                player.setFoodLevel(20);
            } else {
                Player target = plugin.getServer().getPlayer(args[0]);

                if (target != null) {
                    target.setFoodLevel(20);
                } else {
                    player.sendMessage(ChatColor.RED + essentialsConfiguration.getTranslation("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
                }
            }
            return true;
        }
    }

    class BackExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;
            player.teleport(playerConfiguration.getBackLocation(player));
            player.sendMessage(ChatColor.GREEN + essentialsConfiguration.getTranslation("msg_back"));
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
            player.teleport(playerConfiguration.getHomeLocation(player));
            player.sendMessage(ChatColor.GREEN + essentialsConfiguration.getTranslation("msg_home"));
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
            playerConfiguration.setHomeLocation(player, player.getLocation());
            player.sendMessage(ChatColor.GREEN + essentialsConfiguration.getTranslation("msg_home_created"));
            return true;
        }
    }

    class EssentialsListener implements Listener {
        private EnumSet<PlayerTeleportEvent.TeleportCause> teleportCauses = EnumSet.of(PlayerTeleportEvent.TeleportCause.COMMAND, PlayerTeleportEvent.TeleportCause.PLUGIN, PlayerTeleportEvent.TeleportCause.UNKNOWN);

        @EventHandler
        void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            PermissionAttachment attachment = player.addAttachment(plugin);

            permissionAttachment.put(player.getUniqueId(), attachment);
        }

        @EventHandler
        void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            PermissionAttachment attachment = permissionAttachment.get(player.getUniqueId());

            player.removeAttachment(attachment);
            permissionAttachment.remove(player.getUniqueId());
        }

        @EventHandler
        void onPlayerDeath(PlayerDeathEvent event) {
            Player player = event.getEntity();
            playerConfiguration.setBackLocation(player, player.getLocation());
        }

        @EventHandler
        void onWorldLoad(WorldLoadEvent event) {
            String spawnWorld = essentialsConfiguration.getSpawnWorld();
            if ((spawnWorld != null) && (spawnWorld.equals(event.getWorld().getName()))) {
                essentialsConfiguration.loadSpawnLocation(spawnWorld);
            }
        }

        @EventHandler
        void onPlayerTeleport(PlayerTeleportEvent event) {
            Player player = event.getPlayer();
            Location from = event.getFrom();

            if (!teleportCauses.contains(event.getCause())) {
                return;
            }

            if (!auth.isLogged(player)) {
                return;
            }

            playerConfiguration.setBackLocation(player, from);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        void onPlayerChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            String message = event.getMessage();

            if (player.isOp()) {
                event.setFormat(ChatColor.translateAlternateColorCodes('&', essentialsConfiguration.getChatOp().replace("{PLAYER}", player.getDisplayName()).replace("{MSG}", message)));
            } else {
                event.setFormat(ChatColor.translateAlternateColorCodes('&', essentialsConfiguration.getChatNormal().replace("{PLAYER}", player.getDisplayName()).replace("{MSG}", message)));
            }
        }
    }
}
