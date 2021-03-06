package me.noip.yanny.rpg;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.LoggerHandler;
import me.noip.yanny.utils.PlayerRegisterEvent;
import me.noip.yanny.utils.PartPlugin;
import me.noip.yanny.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.*;

import java.util.*;

public class RPG implements PartPlugin {

    private MainPlugin plugin;
    private LoggerHandler logger;
    private Server server;
    private RpgConfiguration rpgConfiguration;
    private Map<UUID, RpgPlayer> rpgPlayerMap = new HashMap<>();
    private Map<SkillType, Skill> skills = new LinkedHashMap<>();
    private RpgBoard rpgBoard;

    public RPG(MainPlugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLoggerHandler();
        server = plugin.getServer();

        rpgConfiguration = new RpgConfiguration(plugin);
        rpgBoard = new RpgBoard(plugin, rpgPlayerMap);

        skills.put(SkillType.MINING, new MiningSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.EXCAVATION, new ExcavationSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.WOODCUTTING, new WoodcuttingSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.HERBALISM, new HerbalismSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.FISHING, new FishingSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.UNARMED, new UnarmedSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.ARCHERY, new ArcherySkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.SWORDS, new SwordsSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.AXES, new AxesSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.TAMING, new TamingSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.REPAIR, new RepairSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.ACROBATICS, new AcrobaticsSkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.ALCHEMY, new AlchemySkill(plugin, rpgPlayerMap, rpgConfiguration));
        skills.put(SkillType.SMELTING, new SmeltingSkill(plugin, rpgPlayerMap, rpgConfiguration));
    }

    @Override
    public void onEnable() {
        server.getOnlinePlayers().forEach(player -> rpgPlayerMap.put(player.getUniqueId(), new RpgPlayer(plugin, player, rpgBoard, skills)));

        rpgConfiguration.load();
        rpgBoard.onEnable();

        server.getPluginManager().registerEvents(new RpgListener(), plugin);
        plugin.getCommand("stats").setExecutor(new StatsExecutor());
        plugin.getCommand("skill").setExecutor(new SkillExecutor());

        skills.forEach((type, skill) -> skill.onEnable());
    }

    @Override
    public void onDisable() {
        rpgBoard.onDisable();

        for (Player player : server.getOnlinePlayers()) {
            RpgPlayer rpgPlayer = rpgPlayerMap.remove(player.getUniqueId());

            if (rpgPlayer == null) {
                continue;
            }

            rpgPlayer.onQuit();
        }
    }

    class StatsExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 0)) {
                return false;
            }

            Player player = (Player) commandSender;
            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                logger.logWarn(RPG.class, "RPG.StatsExecutor: Player not found!" + player.getDisplayName());
                return true;
            }

            ItemStack book = rpgPlayer.getStatsBook();
            Utils.openBook(book, player);
            return true;
        }
    }

    class SkillExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player) || (args.length != 3)) {
                return false;
            }

            Player player = server.getPlayer(args[0]);
            if (player == null) {
                commandSender.sendMessage(ChatColor.RED + "Player " + args[0] + " does not exists");
                return true;
            }

            SkillType skillType;
            int level;

            try {
                skillType = SkillType.valueOf(args[1]);
                level = Integer.parseInt(args[2]);
            } catch (Exception e) {
                commandSender.sendMessage(ChatColor.RED + "Error: " + e.getLocalizedMessage());
                return true;
            }

            if (level < 0) {
                commandSender.sendMessage(ChatColor.RED + "Level lower than zero");
                return true;
            }

            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

            if (rpgPlayer == null) {
                logger.logWarn(RPG.class, "RPG.SkillExecutor: Player not found!" + player.getDisplayName());
                return true;
            }

            rpgPlayer.setStatsLevel(skillType, level);
            return true;
        }
    }

    class RpgListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            RpgPlayer rpgPlayer = new RpgPlayer(plugin, player, rpgBoard, skills);
            rpgPlayerMap.put(player.getUniqueId(), rpgPlayer);
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            RpgPlayer rpgPlayer = rpgPlayerMap.remove(player.getUniqueId());

            if (rpgPlayer == null) {
                logger.logWarn(RPG.class, "RPG.PlayerQuitEvent: Player not found!" + player.getDisplayName());
                return;
            }

            rpgPlayer.onQuit();
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onPlayerRegister(PlayerRegisterEvent event) {
            Player player = event.getPlayer();
            RpgPlayer.registerPlayer(plugin, player);
        }
    }
}
