package me.noip.yanny.rpg;

import me.noip.yanny.auth.PlayerAuthEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class RpgBoard {

    private Plugin plugin;
    private RpgConfiguration rpgConfiguration;
    private Map<UUID, RpgPlayer> playerMap;
    private Map<Player, Objective> objectiveMap = new HashMap<>();

    RpgBoard(Plugin plugin, RpgConfiguration rpgConfiguration, Map<UUID, RpgPlayer> playerMap) {
        this.plugin = plugin;
        this.rpgConfiguration = rpgConfiguration;
        this.playerMap = playerMap;
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

    void updateObjective(RpgPlayerStatsType statsType, Player player, int value) {
        Objective objective = objectiveMap.get(player);
        Score score = objective.getScore(ChatColor.GOLD + statsType.getDisplayName());
        score.setScore(value);
    }

    private Objective createObjective(Player player) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("test", "test");

        for (RpgPlayerStatsType statsType : RpgPlayerStatsType.values()) {
            Score score = objective.getScore(ChatColor.GOLD + statsType.getDisplayName());
            score.setScore(playerMap.get(player.getUniqueId()).getStatsLevel(statsType));
        }

        objective.setDisplayName(rpgConfiguration.getTranslation(RpgConfiguration.T_MSG_STATS));
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
