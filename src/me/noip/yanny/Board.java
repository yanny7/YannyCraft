package me.noip.yanny;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

class Board {

    private JavaPlugin plugin;
    private BoardListener boardListener;

    Board(JavaPlugin plugin) {
        this.plugin = plugin;

        boardListener = new BoardListener();
    }

    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(boardListener, plugin);
    }

    void onDisable() {

    }

    class BoardListener implements Listener {
        @EventHandler
        void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective("objective", "dummy");
            Score score = objective.getScore("Yanny");

            objective.setDisplayName("DisplayName");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            score.setScore(200);
            player.setScoreboard(scoreboard);
        }
    }
}
