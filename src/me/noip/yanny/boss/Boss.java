package me.noip.yanny.boss;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.PartPlugin;
import me.noip.yanny.utils.Rarity;
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
    private Map<Monster, BossMonster> armorStandMap = new HashMap<>();
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
                if (monster.isGlowing()) {
                    Rarity rarity = BossConfiguration.RARITY_TO_HEALTH_MAP.get((int) Math.round(monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));

                    monster.setGlowing(false);

                    if (rarity != null) {
                        List<ArmorStand> armorStands = new ArrayList<>();
                        armorStands.add(spawnArmorStand(monster, 0));
                        armorStands.add(spawnArmorStand(monster, 1));

                        BossMonster bossMonster = new BossMonster(monster, armorStands, rarity);
                        armorStandMap.put(monster, bossMonster);
                        setBossText(bossMonster);
                    }
                }
            }
        }

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            removeList.clear();

            armorStandMap.forEach((monster, bossMonster) -> {
                if (monster.isDead() || !monster.isValid()) {
                    bossMonster.armorStands.forEach(Entity::remove);
                    removeList.add(monster);
                    return;
                }

                for (int i = 0; i < bossMonster.armorStands.size(); i++) {
                    bossMonster.armorStands.get(i).teleport(monster.getEyeLocation().subtract(0, 0.7 - i * 0.25, 0));
                }
            });

            removeList.forEach(monster -> armorStandMap.remove(monster));
        }, 1, 1);
    }

    @Override
    public void onDisable() {
        armorStandMap.forEach((monster, bossMonster) -> {
            monster.setGlowing(true); // used as mark that this is boss type
            bossMonster.armorStands.forEach(Entity::remove);
        });
        armorStandMap.clear();
        Bukkit.getScheduler().cancelTask(taskID);
    }

    private ArmorStand spawnArmorStand(Monster monster, int line) {
        ArmorStand stand = (ArmorStand)monster.getWorld().spawnEntity(monster.getEyeLocation().subtract(0, 0.6 - line * 0.25, 0), EntityType.ARMOR_STAND);
        stand.setRemoveWhenFarAway(true);
        stand.setVisible(false);
        stand.setBasePlate(false);
        stand.setGravity(false);
        stand.setArms(false);
        stand.setOp(true);
        stand.setSmall(true);
        stand.setCollidable(false);
        stand.setCustomNameVisible(true);
        return stand;
    }

    private void setBossText(BossMonster bossMonster) {
        double health = bossMonster.monster.getHealth();
        double maxHealth = bossMonster.monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        int i;

        stringBuilder.setLength(0);
        stringBuilder.append(Math.round(maxHealth)).append(ChatColor.RED).append(" HP");

        bossMonster.armorStands.get(0).setCustomName(stringBuilder.toString());

        stringBuilder.setLength(0);
        stringBuilder.append(bossMonster.rarity.getChatColor()).append(bossMonster.monster.getName()).append(" ").append(ChatColor.GREEN);

        for (i = 0; i < Math.round(health / maxHealth * 10d); i++) {
            stringBuilder.append('|');
        }

        stringBuilder.append(ChatColor.GRAY);

        for (; i < 10; i++) {
            stringBuilder.append('|');
        }

        bossMonster.armorStands.get(1).setCustomName(stringBuilder.toString());
    }

    private class BossListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onMobDeath(EntityDeathEvent event) {
            if (event.getEntity() instanceof Monster) {
                BossMonster bossMonster = armorStandMap.get(event.getEntity());

                if (bossMonster != null) {
                    bossConfiguration.bossDeathDrop(bossMonster, event);
                    bossMonster.armorStands.forEach(Entity::remove);
                    armorStandMap.remove(bossMonster.monster);
                }
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onMobTeleport(EntityTeleportEvent event) {
            if (event.getEntity() instanceof Monster) {
                BossMonster bossMonster = armorStandMap.get(event.getEntity());

                if (bossMonster != null) {
                    bossMonster.armorStands.forEach(armorStand -> armorStand.teleport(event.getTo()));
                }
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onMobDamage(EntityDamageEvent event) {
            if (event.getEntity() instanceof Monster) {
                BossMonster bossMonster = armorStandMap.get(event.getEntity());

                if (bossMonster != null) {
                    setBossText(bossMonster);
                }
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onMobSpawned(CreatureSpawnEvent event) {
            if ((event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) && (event.getEntity() instanceof Monster)) {
                Monster monster = (Monster) event.getEntity();
                Rarity rarity = bossConfiguration.createBoss(monster);

                if (rarity != null) {
                    List<ArmorStand> armorStands = new ArrayList<>();
                    armorStands.add(spawnArmorStand(monster, 0));
                    armorStands.add(spawnArmorStand(monster, 1));

                    BossMonster bossMonster = new BossMonster(monster, armorStands, rarity);
                    armorStandMap.put(monster, bossMonster);
                    setBossText(bossMonster);
                }
            }
        }
    }
}
