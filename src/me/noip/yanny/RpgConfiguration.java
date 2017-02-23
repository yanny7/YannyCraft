package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

class RpgConfiguration {

    private static final String CONFIGURATION_NAME = "rpg";
    private static final String TRANSLATION_SECTION = "translation";
    private static final String STAT_SECTION = "stat_names";

    private ServerConfigurationWrapper serverConfigurationWrapper;
    private RewardWrapper rewardWrapper;
    private Map<String, String> translationMap = new HashMap<>();

    private Plugin plugin;
    private PlayerConfiguration playerConfiguration;

    RpgConfiguration(Plugin plugin, PlayerConfiguration playerConfiguration) {
        this.plugin = plugin;
        this.playerConfiguration = playerConfiguration;

        translationMap.put("msg_reward", "Dostal si odmenu!");
        translationMap.put("msg_stats", "Hodnotenie");

        serverConfigurationWrapper = new ServerConfigurationWrapper(plugin, CONFIGURATION_NAME);
        rewardWrapper = new RewardWrapper(serverConfigurationWrapper, plugin);
    }

    void load() {
        serverConfigurationWrapper.load();

        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        if (translationSection == null) {
            translationSection = serverConfigurationWrapper.createSection(TRANSLATION_SECTION);
        }
        translationMap.putAll(ServerConfigurationWrapper.convertMapString(translationSection.getValues(false)));

        ConfigurationSection statsSection = serverConfigurationWrapper.getConfigurationSection(STAT_SECTION);
        if (statsSection == null) {
            statsSection = serverConfigurationWrapper.createSection(STAT_SECTION);
        }
        for (Map.Entry<String, Object> pair : statsSection.getValues(false).entrySet()) {
            RewardWrapper.RewardType rewardType;

            try {
                rewardType = RewardWrapper.RewardType.valueOf(pair.getKey());
            } catch (Exception e) {
                plugin.getLogger().warning("RpgConfiguration.load: cant cast to RewardType: " + pair.getKey());
                continue;
            }

            if (!(pair.getValue() instanceof String)) {
                plugin.getLogger().warning("RpgConfiguration.load: value is not a String: " + pair.getValue());
                continue;
            }

            rewardType.setDisplayName((String)pair.getValue());
        }

        rewardWrapper.load();
        save(); // save defaults
    }

    private void save() {
        ConfigurationSection translationSection = serverConfigurationWrapper.getConfigurationSection(TRANSLATION_SECTION);
        for (HashMap.Entry<String, String> pair : translationMap.entrySet()) {
            translationSection.set(pair.getKey(), pair.getValue());
        }

        ConfigurationSection statsSection = serverConfigurationWrapper.getConfigurationSection(STAT_SECTION);
        for (RewardWrapper.RewardType rewardType : RewardWrapper.RewardType.values()) {
            statsSection.set(rewardType.name(), rewardType.getDisplayName());
        }

        rewardWrapper.save();
        serverConfigurationWrapper.save();
    }

    String getTranslation(String key) {
        return translationMap.get(key);
    }

    void checkForReward(Player player) {
        for (RewardWrapper.RewardType type : RewardWrapper.RewardType.values()) {
            ItemStack reward = rewardWrapper.getReward(type, playerConfiguration.getStatistic(player, type));

            if (reward != null) {
                player.getInventory().addItem(reward);
                player.sendMessage(ChatColor.GREEN + translationMap.get("msg_reward"));
                playerConfiguration.incrementStatistic(player, type);
            }
        }
    }
}
