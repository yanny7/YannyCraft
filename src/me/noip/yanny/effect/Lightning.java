package me.noip.yanny.effect;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.PartPlugin;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class Lightning implements PartPlugin {

    private MainPlugin plugin;
    private EffectConfiguration effectConfiguration;
    private Map<Location, LightningInfo> lightningsMap = new HashMap<>();

    public Lightning(MainPlugin plugin) {
        this.plugin = plugin;
        effectConfiguration = new EffectConfiguration(plugin);
    }

    @Override
    public void onEnable() {
        plugin.getCommand("lightning").setExecutor(new LightningExecutor());

        Set<LightningInfo> lightnings = effectConfiguration.getLightnings();

        for(LightningInfo lightningInfo : lightnings) {
            lightningInfo.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                lightningInfo.world.playSound(lightningInfo.location, Sound.ENTITY_LIGHTNING_THUNDER, lightningInfo.distance, lightningInfo.speed);
                lightningInfo.world.spigot().strikeLightning(lightningInfo.location, true);
            }, 20 * lightningInfo.delay, 20 * lightningInfo.delay);

            lightningsMap.put(lightningInfo.location, lightningInfo);
        }
    }

    @Override
    public void onDisable() {

    }

    private class LightningExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length == 0)) {
                return false;
            }

            Player player = (Player) commandSender;

            switch (args[0]) {
                case "create": {
                    if (args.length != 4) {
                        player.sendMessage(ChatColor.RED + "Usage: /lightning create [delay(s)] [distance] [speed]");
                        return true;
                    }

                    float distance;
                    float speed;
                    int delay;

                    try {
                        delay = Integer.parseInt(args[1]);
                        distance = Float.parseFloat(args[2]);
                        speed = Float.parseFloat(args[3]);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Error: " + e.getLocalizedMessage());
                        return true;
                    }

                    Location location = player.getLocation();
                    World world = player.getWorld();
                    LightningInfo lightningInfo = new LightningInfo(location, world, delay, distance, speed);

                    lightningInfo.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                        world.playSound(location, Sound.ENTITY_LIGHTNING_THUNDER, distance, speed);
                        world.spigot().strikeLightning(location, true);
                    }, 20 * delay, 20 * delay);

                    lightningsMap.put(location, lightningInfo);
                    effectConfiguration.addLightning(lightningInfo);
                    player.sendMessage(ChatColor.GREEN + "Lightning created");
                    break;
                }
                case "remove": {
                    if (args.length != 1) {
                        player.sendMessage(ChatColor.RED + "Usage: /lightning remove");
                        return true;
                    }

                    LightningInfo lightningInfo = null;

                    for (Map.Entry<Location, LightningInfo> lightningEntry : lightningsMap.entrySet()) {
                        if (lightningEntry.getKey().distance(player.getLocation()) < 4.0) {
                            lightningInfo = lightningEntry.getValue();
                            break;
                        }
                    }

                    if (lightningInfo != null) {
                        lightningsMap.remove(lightningInfo.location);
                        Bukkit.getScheduler().cancelTask(lightningInfo.id);

                        effectConfiguration.removeLightning(lightningInfo.location);
                        player.sendMessage(ChatColor.GREEN + "Lightning removed");
                    }
                    break;
                }
                case "list": {
                    if (args.length != 1) {
                        player.sendMessage(ChatColor.RED + "Usage: /lightning list");
                        return true;
                    }

                    if (!lightningsMap.isEmpty()) {
                        StringBuilder stringBuilder = new StringBuilder();

                        for (Map.Entry<Location, LightningInfo> lightningEntry : lightningsMap.entrySet()) {
                            Location location = lightningEntry.getKey();
                            LightningInfo lightningInfo = lightningEntry.getValue();

                            stringBuilder.setLength(0);
                            stringBuilder.append("Loc: ").append(location.getBlockX()).append(",").append(location.getBlockY()).append(",").append(location.getBlockZ());
                            stringBuilder.append(" World: ").append(lightningInfo.world.getName());
                            stringBuilder.append(" Delay: ").append(lightningInfo.delay);
                            stringBuilder.append(String.format(" Dist: %.1f Speed: %.1f", lightningInfo.distance, lightningInfo.speed));

                            player.sendMessage(stringBuilder.toString());
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "No lightnings created");
                    }
                    break;
                }
                default:
                    return false;
            }

            return true;
        }
    }
}
