package me.noip.yanny.bulletin;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.LoggerHandler;
import me.noip.yanny.utils.ServerConfigurationWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class BulletinConfiguration {

    private static final String CONFIGURATION_NAME = "bulletin";
    private static final String MESSAGES_SECTION = "messages";
    private static final String DELAY_SECTION = "delay";

    private static final String MSG_CONTENT = "content";
    private static final String MSG_DISABLED = "disabled";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private List<Message> messageMap = new ArrayList<>();
    private MainPlugin plugin;
    private LoggerHandler logger;
    private int delay;
    private int schedulerId;
    private Random random = new Random();

    BulletinConfiguration(MainPlugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLoggerHandler();

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);

        delay = 5;
    }

    void load() {
        serverConfigurationWrapper.load();

        delay = serverConfigurationWrapper.getInt(DELAY_SECTION, delay);

        ConfigurationSection messagesSection = serverConfigurationWrapper.getConfigurationSection(MESSAGES_SECTION);
        if (messagesSection == null) {
            messagesSection = serverConfigurationWrapper.createSection(MESSAGES_SECTION);
        }
        for (String string : messagesSection.getKeys(false)) {
            ConfigurationSection messageSection = messagesSection.getConfigurationSection(string);
            String content = messageSection.getString(MSG_CONTENT, "Message not loaded correctly: " + string);
            boolean disabled = messageSection.getBoolean(MSG_DISABLED, false);
            Message message = new Message(content);
            message.disabled = disabled;
            messageMap.add(message);
        }
        scheduleMessage();

        save();
        logger.logInfo(Bulletin.class, String.format("Message delay: %d min", delay));
        logger.logInfo(Bulletin.class, String.format("Messages pool: %d", messageMap.size()));
    }

    private void save()  {
        serverConfigurationWrapper.set(MESSAGES_SECTION, null);
        ConfigurationSection messagesSection = serverConfigurationWrapper.getConfigurationSection(MESSAGES_SECTION);
        if (messagesSection == null) {
            messagesSection = serverConfigurationWrapper.createSection(MESSAGES_SECTION);
        }
        for (int i = 0; i < messageMap.size(); i++) {
            Message message = messageMap.get(i);
            ConfigurationSection messageSection = messagesSection.createSection(Integer.toString(i));
            messageSection.set(MSG_CONTENT, message.content);
            messageSection.set(MSG_DISABLED, message.disabled);
        }

        serverConfigurationWrapper.set(DELAY_SECTION, delay);

        serverConfigurationWrapper.save();
    }

    void addMessage(String content) {
        messageMap.add(new Message(content));
        scheduleMessage();
        save();
    }

    boolean removeMessage(int id) {
        if (id < 0 || id >= messageMap.size()) {
            return false;
        }

        messageMap.remove(id);
        scheduleMessage();
        save();
        return true;
    }

    boolean disableMessage(int id, boolean disable) {
        if (id < 0 || id >= messageMap.size()) {
            return false;
        }

        Message message = messageMap.get(id);
        message.disabled = disable;
        scheduleMessage();
        save();
        return true;
    }

    void setDelay(int delay) {
        this.delay = delay;
        scheduleMessage();
        save();
    }

    void listMessaged(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.GREEN + "Delay: " + delay + " min");

        if (!messageMap.isEmpty()) {
            commandSender.sendMessage(ChatColor.GREEN + "------------------------------------------------");

            for (int i = 0; i < messageMap.size(); i++) {
                BulletinConfiguration.Message message = messageMap.get(i);

                if (message.disabled) {
                    commandSender.sendMessage(String.format("%d: %s", i, ChatColor.GRAY + message.content));
                } else {
                    commandSender.sendMessage(String.format("%d: %s", i, ChatColor.translateAlternateColorCodes('&', message.content)));
                }
            }

            commandSender.sendMessage(ChatColor.GREEN + "------------------------------------------------");
        } else {
            commandSender.sendMessage(ChatColor.RED + "No messages in pool");
        }
    }

    private void scheduleMessage() {
        if (schedulerId != -1) {
            Bukkit.getScheduler().cancelTask(schedulerId);
        }

        schedulerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                List<String> enabledMessages = new ArrayList<>(messageMap.size());

                for (Message message : messageMap) {
                    if (!message.disabled) {
                        enabledMessages.add(message.content);
                    }
                }

                if (!enabledMessages.isEmpty()) {
                    String message = enabledMessages.get(random.nextInt(enabledMessages.size()));
                    plugin.getServer().broadcastMessage(ChatColor.DARK_RED + "[Server] " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message));
                } else {
                    Bukkit.getScheduler().cancelTask(schedulerId);
                    schedulerId = -1;
                }
            }, 20 * 30 * delay, 20 * 60 * delay
        );
    }

    private class Message {
        String content;
        boolean disabled;

        Message(String content) {
            this.content = content;
            disabled = false;
        }
    }
}
