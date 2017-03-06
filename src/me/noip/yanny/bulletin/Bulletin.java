package me.noip.yanny.bulletin;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.PartPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Bulletin implements PartPlugin {

    private MainPlugin plugin;
    private BulletinConfiguration bulletinConfiguration;

    public Bulletin(MainPlugin plugin) {
        this.plugin = plugin;

        bulletinConfiguration = new BulletinConfiguration(plugin);
    }

    @Override
    public void onEnable() {
        bulletinConfiguration.load();

        plugin.getCommand("bulletin").setExecutor(new BulletinExecutor());
    }

    @Override
    public void onDisable() {

    }

    private class BulletinExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (args.length == 0) {
                return false;
            }

            switch (args[0]) {
                case "add": {
                    if (args.length < 2) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /bulletin add [message]");
                        return true;
                    }

                    StringBuilder content = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        content.append(args[i]).append(" ");
                    }

                    bulletinConfiguration.addMessage(content.toString());
                    commandSender.sendMessage(ChatColor.GREEN + "Bulletin message was successfully added");
                    break;
                }
                case "remove": {
                    if (args.length != 2) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /bulletin remove [id]");
                        return true;
                    }

                    int id;

                    try {
                        id = Integer.parseInt(args[1]);
                    } catch (Exception e) {
                        commandSender.sendMessage(ChatColor.RED + "BulletinExecutor: " + e.getLocalizedMessage());
                        return true;
                    }

                    if (bulletinConfiguration.removeMessage(id)) {
                        commandSender.sendMessage(ChatColor.GREEN + "Message was successfully removed");
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Message was not removed");
                    }
                    break;
                }
                case "list": {
                    if (args.length != 1) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /bulletin list");
                        return true;
                    }

                    bulletinConfiguration.listMessaged(commandSender);
                    break;
                }
                case "enable": {
                    if (args.length != 2) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /bulletin enable [id]");
                        return true;
                    }

                    int id;

                    try {
                        id = Integer.parseInt(args[1]);
                    } catch (Exception e) {
                        commandSender.sendMessage(ChatColor.RED + "BulletinExecutor: " + e.getLocalizedMessage());
                        return true;
                    }

                    if (bulletinConfiguration.disableMessage(id, false)) {
                        commandSender.sendMessage(ChatColor.GREEN + "Message was successfully enabled");
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Message was not enabled");
                    }
                    break;
                }
                case "disable": {
                    if (args.length != 2) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /bulletin disable [id]");
                        return true;
                    }

                    int id;

                    try {
                        id = Integer.parseInt(args[1]);
                    } catch (Exception e) {
                        commandSender.sendMessage(ChatColor.RED + "BulletinExecutor: " + e.getLocalizedMessage());
                        return true;
                    }

                    if (bulletinConfiguration.disableMessage(id, true)) {
                        commandSender.sendMessage(ChatColor.GREEN + "Message was successfully disabled");
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Message was not disabled");
                    }
                    break;
                }
                case "delay": {
                    if (args.length != 2) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /bulletin delay [delay(min)]");
                        return true;
                    }

                    int delay;

                    try {
                        delay = Integer.parseInt(args[1]);
                    } catch (Exception e) {
                        commandSender.sendMessage(ChatColor.RED + "BulletinExecutor: " + e.getLocalizedMessage());
                        return true;
                    }

                    bulletinConfiguration.setDelay(delay);
                    commandSender.sendMessage(ChatColor.GREEN + "Delay was successfully set");
                    break;
                }
                default:
                    return false;
            }

            return true;
        }
    }
}
