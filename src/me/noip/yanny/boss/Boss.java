package me.noip.yanny.boss;

import me.noip.yanny.utils.PartPlugin;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

public class Boss implements PartPlugin {

    private Plugin plugin;
    private BossConfiguration bossConfiguration;

    public Boss(Plugin plugin) {
        this.plugin = plugin;
        bossConfiguration = new BossConfiguration(plugin);
    }

    @Override
    public void onEnable() {
        bossConfiguration.load();
        plugin.getServer().getPluginManager().registerEvents(new BossListener(), plugin);
    }

    @Override
    public void onDisable() {

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