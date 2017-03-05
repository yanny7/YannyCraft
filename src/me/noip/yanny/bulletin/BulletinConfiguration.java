package me.noip.yanny.bulletin;

import me.noip.yanny.utils.ServerConfigurationWrapper;
import me.noip.yanny.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.*;

class BulletinConfiguration {

    private static final String CONFIGURATION_NAME = "bulletin";
    private static final String TRANSLATION_SECTION = "translation";
    private static final String MESSAGES_SECTION = "messages";
    private static final String DELAY_SECTION = "delay";

    private static final String MSG_CONTENT = "content";
    private static final String MSG_DISABLED = "disabled";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private Map<String, String> translationMap = new HashMap<>();
    private List<Message> messageMap = new ArrayList<>();
    private Plugin plugin;
    private int delay;
    private int schedulerId;
    private Random random = new Random();

    BulletinConfiguration(Plugin plugin) {
        this.plugin = plugin;

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
            String content = messageSection.getString(MSG_CONTENT, "Message not loaded: " + string);
            boolean disabled = messageSection.getBoolean(MSG_DISABLED, false);
            Message message = new Message(content);
            message.disabled = disabled;
            messageMap.add(message);
        }
        scheduleMessage();

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        translationMap.putAll(Utils.convertMapString(translationSection.getValues(false)));

        save();
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

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        for (HashMap.Entry<String, String> pair : translationMap.entrySet()) {
            translationSection.set(pair.getKey(), pair.getValue());
        }

        serverConfigurationWrapper.save();
    }

    String getTranslation(String key) {
        return translationMap.get(key);
    }

    void addMessage(String content) {
        messageMap.add(new Message(content));
        scheduleMessage();
        save();
    }

    List<Message> getMessages() {
        return messageMap;
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

    int getDelay() {
        return delay;
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
                    plugin.getServer().broadcastMessage("[Server] " + ChatColor.translateAlternateColorCodes('&', message));
                } else {
                    Bukkit.getScheduler().cancelTask(schedulerId);
                    schedulerId = -1;
                }
            }, 20 * 30 * delay, 20 * 60 * delay);
    }

    class Message {
        String content;
        boolean disabled;

        Message(String content) {
            this.content = content;
            disabled = false;
        }
    }
}
