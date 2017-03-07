package me.noip.yanny.rpg;

import me.noip.yanny.utils.PlayerAuthEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.noip.yanny.rpg.RpgTranslation.*;

class RpgBoard {

    private Plugin plugin;
    private Map<UUID, RpgPlayer> playerMap;
    private Map<Player, Objective> objectiveMap = new HashMap<>();

    RpgBoard(Plugin plugin, Map<UUID, RpgPlayer> playerMap) {
        this.plugin = plugin;
        this.playerMap = playerMap;
    }

    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new RpgBoardListener(), plugin);
        plugin.getServer().getOnlinePlayers().forEach(player -> objectiveMap.put(player, createObjective(player)));
    }

    void onDisable() {
        objectiveMap.forEach((k, v) -> v.getScoreboard().resetScores("RPG"));
        objectiveMap.clear();
    }

    void updateObjective(SkillType statsType, Player player, int value) {
        Objective objective = objectiveMap.get(player);
        Score score = objective.getScore(ChatColor.GOLD + statsType.getDisplayName());
        score.setScore(value);
    }

    private Objective createObjective(Player player) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("RPG", "RPG");

        for (SkillType statsType : SkillType.values()) {
            Score score = objective.getScore(ChatColor.GOLD + statsType.getDisplayName());
            score.setScore(playerMap.get(player.getUniqueId()).getStatsLevel(statsType));
        }

        objective.setDisplayName(RPG_STATS.getDisplayName());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
        return objective;
    }

    private class RpgBoardListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onPlayerLogin(PlayerAuthEvent event) {
            Player player = event.getPlayer();
            Objective objective = createObjective(player);
            objectiveMap.put(player, objective);
        }
    }
}
