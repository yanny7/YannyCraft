package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class Essentials {

    private JavaPlugin plugin;

    private Map<UUID, PermissionAttachment> permissionAttachment;
    private Map<Player, Player> teleportRequest;

    private EssentialsListener essentialsListener;
    private ConfigurationSection section;

    private SpawnExecutor spawnExecutor;
    private SetSpawnExecutor setSpawnExecutor;
    private TpExecutor tpExecutor;
    private TpaExecutor tpaExecutor;
    private TphereExecutor tphereExecutor;
    private TpacceptExecutor tpacceptExecutor;
    private TpdenyExecutor tpdenyExecutor;
    private HealExecutor healExecutor;
    private FeedExecutor feedExecutor;

    Essentials(JavaPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration configuration = plugin.getConfig();

        section = configuration.getConfigurationSection("essentials");
        if (section == null) {
            section = configuration.createSection("essentials");
        }

        section.addDefault("msg_spawn_set", "Nova spawn lokacia bola nastavena");
        section.addDefault("msg_tpa_sended", "Poziadavka na teleport bola odoslana hracovi");
        section.addDefault("msg_tpa_received", "Hrac {player} sa chce k tebe teleportovat /tpaccept prijmi, /tpdeny zamietni");
        section.addDefault("msg_tpdeny", "Poziadavka na teleport bola zamietnuta");
        section.addDefault("msg_teleported", "Bol si teleportovany k hracovi {player}");

        section.addDefault("msg_err_invalid_user", "Hrac {player} neexistuje");
        section.addDefault("msg_err_permission", "Na tento prikaz nemas prava");

        permissionAttachment = new HashMap<>();
        teleportRequest = new HashMap<>();

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
    }

    void onEnable() {
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
                player.teleport((Location)section.get("spawn"));
            } else {
                if (player.hasPermission("yannycraft.spawn.other")) {
                    Player target = plugin.getServer().getPlayer(args[0]);

                    if (target != null) {
                        target.teleport((Location)section.get("spawn"));
                    } else {
                        player.sendMessage(ChatColor.RED + section.getString("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + section.getString("msg_err_permission"));
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
            section.set("spawn", player.getLocation());
            player.sendMessage(ChatColor.GREEN + section.getString("msg_spawn_set"));
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
                player.sendMessage(ChatColor.RED + section.getString("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
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
                player.sendMessage(ChatColor.GREEN + section.getString("msg_tpa_sended"));
                target.sendMessage(ChatColor.GREEN + section.getString("msg_tpa_received").replace("{player}", ChatColor.GOLD + player.getDisplayName() + ChatColor.GREEN));
                teleportRequest.put(target, player);
            } else {
                player.sendMessage(ChatColor.RED + section.getString("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
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
                target.sendMessage(ChatColor.GREEN + section.getString("msg_teleported").replace("{player}", ChatColor.GOLD + player.getDisplayName() + ChatColor.GREEN));
            } else {
                player.sendMessage(ChatColor.RED + section.getString("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
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
                    player.sendMessage(ChatColor.RED + section.getString("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
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
                player.sendMessage(ChatColor.GREEN + section.getString("msg_tpdeny"));
                target.sendMessage(ChatColor.RED + section.getString("msg_tpdeny"));
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
                    player.sendMessage(ChatColor.RED + section.getString("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
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
                    player.sendMessage(ChatColor.RED + section.getString("msg_err_invalid_user").replace("{player}", ChatColor.GOLD + args[0] + ChatColor.RED));
                }
            }
            return true;
        }
    }

    class EssentialsListener implements Listener {
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
        void onWorldLoaded(WorldLoadEvent event) {
            if (!section.isSet("spawn")) {
                section.addDefault("spawn", event.getWorld().getSpawnLocation());
            }
        }
    }
}
