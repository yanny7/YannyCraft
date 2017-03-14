package me.noip.yanny.boss;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.PartPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.*;

public class Boss implements PartPlugin {

    private MainPlugin plugin;
    private Server server;
    private BossConfiguration bossConfiguration;
    private Map<Monster, List<ArmorStand>> armorStandMap = new HashMap<>();
    private List<Monster> removeList = new LinkedList<>();
    private StringBuilder stringBuilder = new StringBuilder();
    private int taskID;

    public Boss(MainPlugin plugin) {
        this.plugin = plugin;
        server = plugin.getServer();
        bossConfiguration = new BossConfiguration(plugin);
    }

    @Override
    public void onEnable() {
        bossConfiguration.load();
        server.getPluginManager().registerEvents(new BossListener(), plugin);

        for (World world : server.getWorlds()) {
            for (Monster monster : world.getEntitiesByClass(Monster.class)) {
                List<ArmorStand> armorStands = new ArrayList<>(2);
                armorStands.add(spawnArmorStand(monster, ChatColor.RED + "" + (int)monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + " HP", 0));
                armorStands.add(spawnArmorStand(monster, monster.getName(), 1));
                armorStandMap.put(monster, armorStands);
            }
        }

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            removeList.clear();

            armorStandMap.forEach((monster, armorStands) -> {
                if (monster.isDead() || !monster.isValid()) {
                    armorStands.forEach(Entity::remove);
                    removeList.add(monster);
                    return;
                }

                for (int i = 0; i < armorStands.size(); i++) {
                    armorStands.get(i).teleport(monster.getEyeLocation().subtract(0, 0.7 - i * 0.25, 0));
                }
            });

            removeList.forEach(monster -> armorStandMap.remove(monster));
        }, 1, 1);
    }

    @Override
    public void onDisable() {
        armorStandMap.forEach((monster, armorStands) -> armorStands.forEach(Entity::remove));
        armorStandMap.clear();
        Bukkit.getScheduler().cancelTask(taskID);
    }

    private ArmorStand spawnArmorStand(Monster monster, String text, int line) {
        ArmorStand stand = (ArmorStand)monster.getWorld().spawnEntity(monster.getEyeLocation().subtract(0, 0.7 - line * 0.25, 0), EntityType.ARMOR_STAND);
        stand.setRemoveWhenFarAway(false);
        stand.setVisible(false);
        stand.setBasePlate(false);
        stand.setGravity(false);
        stand.setArms(false);
        stand.setOp(true);
        stand.setSmall(true);
        stand.setCollidable(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(text);
        return stand;
    }

    class BossListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onMobDeath(EntityDeathEvent event) {
            bossConfiguration.bossDeathDrop(event);

            if (event.getEntity() instanceof Monster) {
                Monster monster = (Monster) event.getEntity();
                monster.getPassengers().forEach(Entity::remove);
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onMobTeleport(EntityTeleportEvent event) {
            if (event.getEntity() instanceof Monster) {
                List<ArmorStand> armorStands = armorStandMap.get(event.getEntity());

                if (armorStands == null) {
                    return;
                }

                armorStands.forEach(armorStand -> armorStand.teleport(event.getTo()));
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onMobDamage(EntityDamageEvent event) {
            if (event.getEntity() instanceof Monster) {
                Monster monster = (Monster) event.getEntity();
                List<ArmorStand> armorStands = armorStandMap.get(monster);

                if (armorStands != null) {
                    double health = monster.getHealth();
                    double maxHealth = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    int i;

                    stringBuilder.setLength(0);
                    stringBuilder.append(monster.getName()).append(" ").append(ChatColor.GREEN);

                    for (i = 0; i < Math.round(health / maxHealth * 10d); i++) {
                        stringBuilder.append('|');
                    }

                    stringBuilder.append(ChatColor.GRAY);

                    for (; i < 10; i++) {
                        stringBuilder.append('|');
                    }

                    armorStands.get(1).setCustomName(stringBuilder.toString());
                }
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onMobSpawned(CreatureSpawnEvent event) {
            if (event.getEntity() instanceof Monster) {
                Monster monster = (Monster) event.getEntity();

                if (bossConfiguration.createBoss(monster, event.getSpawnReason())) {
                    List<ArmorStand> armorStands = new ArrayList<>();
                    armorStands.add(spawnArmorStand(monster, (int)monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + " HP", 0));
                    armorStands.add(spawnArmorStand(monster, monster.getName() + ChatColor.GREEN + " ||||||||||", 1));
                    armorStandMap.put(monster, armorStands);
                }
            }
        }
    }
}
