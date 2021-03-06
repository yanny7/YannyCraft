package me.noip.yanny.rpg;

import me.noip.yanny.MainPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.UUID;

class SwordsSkill extends Skill {

    SwordsSkill(MainPlugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DOUBLE_DAMAGE, new DoubleDamageAbility(SkillType.SWORDS, "Iron skill", 0));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new SwordsSkillListener(), plugin);
    }

    static void loadDefaults(Map<EntityType, Integer> exp) {
        // passive mobs
        exp.put(EntityType.VILLAGER, 10);
        exp.put(EntityType.BAT, 20);
        exp.put(EntityType.CHICKEN, 20);
        exp.put(EntityType.COW, 20);
        exp.put(EntityType.PIG, 20);
        exp.put(EntityType.RABBIT, 20);
        exp.put(EntityType.SHEEP, 20);
        exp.put(EntityType.SQUID, 20);
        exp.put(EntityType.SKELETON_HORSE, 100);
        // neutral mobs
        exp.put(EntityType.CAVE_SPIDER, 50);
        exp.put(EntityType.POLAR_BEAR, 50);
        exp.put(EntityType.SPIDER, 50);
        exp.put(EntityType.ENDERMAN, 60);
        exp.put(EntityType.PIG_ZOMBIE, 100);
        // hostile mobs
        exp.put(EntityType.ZOMBIE, 50);
        exp.put(EntityType.SLIME, 50);
        exp.put(EntityType.HUSK, 50);
        exp.put(EntityType.CREEPER, 50);
        exp.put(EntityType.SILVERFISH, 50);
        exp.put(EntityType.SKELETON, 50);
        exp.put(EntityType.WITCH, 50);
        exp.put(EntityType.ZOMBIE_HORSE, 50);
        exp.put(EntityType.ZOMBIE_VILLAGER, 50);
        exp.put(EntityType.MAGMA_CUBE, 100);
        exp.put(EntityType.BLAZE, 100);
        exp.put(EntityType.ENDERMITE, 100);
        exp.put(EntityType.GHAST, 100);
        exp.put(EntityType.STRAY, 100);
        exp.put(EntityType.GUARDIAN, 100);
        exp.put(EntityType.VEX, 100);
        exp.put(EntityType.VINDICATOR, 100);
        exp.put(EntityType.EVOKER, 150);
        exp.put(EntityType.WITHER_SKELETON, 150);
        exp.put(EntityType.SHULKER, 150);
        exp.put(EntityType.ELDER_GUARDIAN, 200);
        // tameable mobs
        exp.put(EntityType.DONKEY, 20);
        exp.put(EntityType.HORSE, 20);
        exp.put(EntityType.MULE, 20);
        exp.put(EntityType.OCELOT, 20);
        exp.put(EntityType.LLAMA, 30);
        exp.put(EntityType.WOLF, 50);
        // boss mobs
        exp.put(EntityType.ENDER_DRAGON, 450);
        exp.put(EntityType.WITHER, 500);
        // utility mobs
        exp.put(EntityType.IRON_GOLEM, 30);
        exp.put(EntityType.SNOWMAN, 30);
    }

    private class SwordsSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onMobDamagedByEntity(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player) || (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                return;
            }

            Player player = (Player) event.getDamager();

            switch (player.getInventory().getItemInMainHand().getType()) {
                case STONE_SWORD:
                case DIAMOND_SWORD:
                case GOLD_SWORD:
                case IRON_SWORD:
                case WOOD_SWORD: {
                    int exp = rpgConfiguration.getDamageExp(event.getEntityType());

                    if (exp > 0) {
                        RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                        if (rpgPlayer == null) {
                            logger.logWarn(RPG.class, "SwordsSkill.EntityDamagedByEntityEvent: Player not found!" + player.getDisplayName());
                            return;
                        }

                        ((DoubleDamageAbility) abilities.get(AbilityType.DOUBLE_DAMAGE)).execute(rpgPlayer, event.getEntity(), event.getFinalDamage());
                        rpgPlayer.set(SkillType.SWORDS, exp);
                    }
                    break;
                }
            }

        }
    }
}
