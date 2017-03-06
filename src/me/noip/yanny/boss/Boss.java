package me.noip.yanny.boss;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.PartPlugin;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class Boss implements PartPlugin {

    private MainPlugin plugin;
    private BossConfiguration bossConfiguration;

    public Boss(MainPlugin plugin) {
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
        @SuppressWarnings("unused")
        @EventHandler
        void OnMobDeath(EntityDeathEvent event) {
            bossConfiguration.bossDeathDrop(event);
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onMobSpawned(CreatureSpawnEvent event) {
            if (event.getEntity() instanceof Monster) {
                bossConfiguration.createBoss((Monster) event.getEntity(), event.getSpawnReason());
            }
        }
    }
}
