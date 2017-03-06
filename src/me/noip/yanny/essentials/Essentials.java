package me.noip.yanny.essentials;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.PartPlugin;
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.noip.yanny.essentials.EssentialsTranslation.*;

public class Essentials implements PartPlugin {

    private MainPlugin plugin;

    private Map<UUID, PermissionAttachment> permissionAttachment;
    private Map<Player, Player> teleportRequest;
    private EssentialsConfiguration essentialsConfiguration;

    public Essentials(MainPlugin plugin) {
        this.plugin = plugin;

        permissionAttachment = new HashMap<>();
        teleportRequest = new HashMap<>();
        essentialsConfiguration = new EssentialsConfiguration(plugin);
    }

    @Override
    public void onEnable() {
        essentialsConfiguration.load();

        plugin.getServer().getPluginManager().registerEvents(new EssentialsListener(), plugin);
        plugin.getCommand("spawn").setExecutor(new SpawnExecutor());
        plugin.getCommand("setspawn").setExecutor(new SetSpawnExecutor());
        plugin.getCommand("tp").setExecutor(new TpExecutor());
        plugin.getCommand("tpa").setExecutor(new TpaExecutor());
        plugin.getCommand("tphere").setExecutor(new TphereExecutor());
        plugin.getCommand("tpaccept").setExecutor(new TpacceptExecutor());
        plugin.getCommand("tpdeny").setExecutor(new TpdenyExecutor());
        plugin.getCommand("heal").setExecutor(new HealExecutor());
        plugin.getCommand("feed").setExecutor(new FeedExecutor());
        plugin.getCommand("clrinv").setExecutor(new ClrInvExecutor());
        plugin.getCommand("speed").setExecutor(new SpeedExecutor());
        plugin.getCommand("back").setExecutor(new BackExecutor());
        plugin.getCommand("home").setExecutor(new HomeExecutor());
        plugin.getCommand("sethome").setExecutor(new SetHomeExecutor());

        plugin.getServer().getOnlinePlayers().forEach(player -> permissionAttachment.put(player.getUniqueId(), player.addAttachment(plugin)));
    }

    @Override
    public void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PermissionAttachment attachment = permissionAttachment.remove(player.getUniqueId());
            player.removeAttachment(attachment);
        }

        permissionAttachment.clear();
        teleportRequest.clear();
    }

    public Location getSpawnLocation() {
        return essentialsConfiguration.getSpawnLocation();
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
                player.teleport(essentialsConfiguration.getSpawnLocation());
                player.sendMessage(SPAWN_TELEPORTED.display());
            } else {
                if (player.hasPermission("yannycraft.spawn.other")) {
                    Player target = plugin.getServer().getPlayer(args[0]);

                    if (target != null) {
                        target.teleport(essentialsConfiguration.getSpawnLocation());
                        target.sendMessage(SPAWN_TELEPORTED.display());
                    } else {
                        player.sendMessage(ERR_INVALID_PLAYER.display().replace("{player}", ChatColor.GOLD + args[0] + ERR_INVALID_PLAYER.getChatColor()));
                    }
                } else {
                    player.sendMessage(ERR_PERMISSION.display());
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
            player.sendMessage(SPAWN_SET.display());
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
                player.sendMessage(ERR_INVALID_PLAYER.display().replace("{player}", ChatColor.GOLD + args[0] + ERR_INVALID_PLAYER.getChatColor()));
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
                player.sendMessage(TPA_SENDED.display());
                target.sendMessage(TPA_RECEIVED.display().replace("{player}", ChatColor.GOLD + player.getDisplayName() + TPA_RECEIVED.getChatColor()));
                teleportRequest.put(target, player);
            } else {
                player.sendMessage(ERR_INVALID_PLAYER.display().replace("{player}", ChatColor.GOLD + args[0] + ERR_INVALID_PLAYER.getChatColor()));
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
                target.sendMessage(TELEPORTED.display().replace("{player}", ChatColor.GOLD + player.getDisplayName() + TELEPORTED.getChatColor()));
            } else {
                player.sendMessage(ERR_INVALID_PLAYER.display().replace("{player}", ChatColor.GOLD + args[0] + ERR_INVALID_PLAYER.getChatColor()));
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
                    player.sendMessage(ERR_INVALID_PLAYER.display().replace("{player}", ChatColor.GOLD + args[0] + ERR_INVALID_PLAYER.getChatColor()));
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
                player.sendMessage(TPDENY.display());
                target.sendMessage(ERR_TPDENY.display());
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
                    player.sendMessage(ERR_INVALID_PLAYER.display().replace("{player}", ChatColor.GOLD + args[0] + ERR_INVALID_PLAYER.getChatColor()));
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
                    player.sendMessage(ERR_INVALID_PLAYER.display().replace("{player}", ChatColor.GOLD + args[0] + ERR_INVALID_PLAYER.getChatColor()));
                }
            }
            return true;
        }
    }

    class ClrInvExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length > 1)) {
                return false;
            }

            Player player;

            if (args.length == 1) {
                player = plugin.getServer().getPlayer(args[0]);

                if (player == null) {
                    commandSender.sendMessage(ERR_INVALID_PLAYER.display().replace("{player}", ChatColor.GOLD + args[0] + ERR_INVALID_PLAYER.getChatColor()));
                    return true;
                }
            } else {
                player = (Player)commandSender;
            }

            player.getInventory().clear();
            player.updateInventory();

            commandSender.sendMessage(INV_CLEARED.display());
            return true;
        }
    }

    class SpeedExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 1)) {
                return false;
            }

            Player player = (Player)commandSender;
            float speed;

            try {
                speed = Float.parseFloat(args[0]) / 10f;
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "Error: " + e.getLocalizedMessage());
                return true;
            }

            try {
                if (player.isFlying()) {
                    player.setFlySpeed(speed);
                } else {
                    player.setWalkSpeed(speed);
                }
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "Error: " + e.getLocalizedMessage());
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
            player.teleport(essentialsConfiguration.getBackLocation(player));
            player.sendMessage(BACK_TELEPORTED.display());
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
            player.teleport(essentialsConfiguration.getHomeLocation(player));
            player.sendMessage(HOME_TELEPORTED.display());
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
            essentialsConfiguration.setHomeLocation(player, player.getLocation());
            player.sendMessage(HOME_CREATED.display());
            return true;
        }
    }

    class EssentialsListener implements Listener {
        private EnumSet<PlayerTeleportEvent.TeleportCause> teleportCauses = EnumSet.of(PlayerTeleportEvent.TeleportCause.COMMAND, PlayerTeleportEvent.TeleportCause.PLUGIN, PlayerTeleportEvent.TeleportCause.UNKNOWN);

        @SuppressWarnings("unused")
        @EventHandler
        void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            PermissionAttachment attachment = player.addAttachment(plugin);

            permissionAttachment.put(player.getUniqueId(), attachment);
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            PermissionAttachment attachment = permissionAttachment.get(player.getUniqueId());

            player.removeAttachment(attachment);
            permissionAttachment.remove(player.getUniqueId());
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onPlayerDeath(PlayerDeathEvent event) {
            Player player = event.getEntity();
            essentialsConfiguration.setBackLocation(player, player.getLocation());
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onWorldLoad(WorldLoadEvent event) {
            String spawnWorld = essentialsConfiguration.getSpawnWorld();
            if ((spawnWorld != null) && (spawnWorld.equals(event.getWorld().getName()))) {
                essentialsConfiguration.loadSpawnLocation(spawnWorld);
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onPlayerTeleport(PlayerTeleportEvent event) {
            Player player = event.getPlayer();
            Location from = event.getFrom();

            if (!teleportCauses.contains(event.getCause())) {
                return;
            }

            if (!plugin.getAuth().isLogged(player)) {
                return;
            }

            essentialsConfiguration.setBackLocation(player, from);
        }

        @SuppressWarnings("unused")
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
