package me.noip.yanny.rpg;

import me.noip.yanny.PlayerConfiguration;
import me.noip.yanny.auth.PlayerAuthEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

class RpgBoard {

    private Plugin plugin;
    private PlayerConfiguration playerConfiguration;
    private RpgConfiguration rpgConfiguration;
    private Map<Player, Objective> objectiveMap = new HashMap<>();

    RpgBoard(Plugin plugin, PlayerConfiguration playerConfiguration, RpgConfiguration rpgConfiguration) {
        this.plugin = plugin;
        this.playerConfiguration = playerConfiguration;
        this.rpgConfiguration = rpgConfiguration;
    }

    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new RpgBoardListener(), plugin);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Objective objective = createObjective(player);
            objectiveMap.put(player, objective);
        }
    }

    void onDisable() {
        for (Map.Entry<Player, Objective> entry : objectiveMap.entrySet()) {
            Objective objective = entry.getValue();
            objective.getScoreboard().resetScores("test");
        }
        objectiveMap.clear();
    }

    void updateObjective(RewardWrapper.RewardType rewardType, Player player, int value) {
        Objective objective = objectiveMap.get(player);
        Score score = objective.getScore(rewardType.getDisplayName());
        score.setScore(value);
    }

    private Objective createObjective(Player player) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("test", "test");

        for (RewardWrapper.RewardType rewardType : RewardWrapper.RewardType.values()) {
            Score score = objective.getScore(rewardType.getDisplayName());
            score.setScore(playerConfiguration.getStatistic(player, rewardType));
        }

        objective.setDisplayName(rpgConfiguration.getTranslation("msg_stats"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
        return objective;
    }

    private class RpgBoardListener implements Listener {
        @EventHandler
        void onPlayerLogin(PlayerAuthEvent event) {
            Player player = event.getPlayer();
            Objective objective = createObjective(player);
            objectiveMap.put(player, objective);
        }
    }
}
