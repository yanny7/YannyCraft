package me.noip.yanny;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

class Boss {

    private Plugin plugin;
    private BossConfiguration bossConfiguration;

    Boss(Plugin plugin) {
        this.plugin = plugin;
        bossConfiguration = new BossConfiguration(plugin);
    }

    void onEnable() {
        bossConfiguration.load();
        plugin.getServer().getPluginManager().registerEvents(new BossListener(), plugin);
    }

    void onDisable() {

    }

    class BossListener implements Listener {

        @EventHandler
        void OnMobDeath(EntityDeathEvent event) {
            bossConfiguration.bossDeathDrop(event);
        }

        @EventHandler
        void onMobSpawned(CreatureSpawnEvent event) {
            if (!(event.getEntity() instanceof Monster)) {
                return;
            }

            bossConfiguration.createBoss((Monster) event.getEntity(), event.getSpawnReason());
        }
    }
}
