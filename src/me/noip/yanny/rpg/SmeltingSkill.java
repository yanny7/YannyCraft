package me.noip.yanny.rpg;

import me.noip.yanny.MainPlugin;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

class SmeltingSkill extends Skill {

    SmeltingSkill(MainPlugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.DAMAGE_REDUCED, new DamageReductionAbility(SkillType.SMELTING, "Fire skin", 0));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new SmeltingSkillListener(), plugin);
    }

    static void loadDefaults(Map<Material, Integer> exp) {
        // food
        exp.put(Material.GRILLED_PORK, 30);
        exp.put(Material.COOKED_BEEF, 30);
        exp.put(Material.COOKED_CHICKEN, 30);
        exp.put(Material.COOKED_FISH, 30);
        exp.put(Material.BAKED_POTATO, 30);
        exp.put(Material.COOKED_MUTTON, 30);
        exp.put(Material.COOKED_RABBIT, 30);
        // ore and material
        exp.put(Material.STONE, 30);
        exp.put(Material.GLASS, 40);
        exp.put(Material.NETHER_BRICK_ITEM, 40);
        exp.put(Material.CLAY_BRICK, 50);
        exp.put(Material.IRON_INGOT, 50);
        exp.put(Material.GOLD_INGOT, 70);
        exp.put(Material.SMOOTH_BRICK, 80);
        exp.put(Material.HARD_CLAY, 100);
        // wasting ores
        exp.put(Material.COAL, 30);
        exp.put(Material.DIAMOND, 200);
        exp.put(Material.INK_SACK, 200); //LAPIS LAZULI
        exp.put(Material.REDSTONE, 200);
        exp.put(Material.EMERALD, 200);
        exp.put(Material.QUARTZ, 200);
        // tools
        exp.put(Material.IRON_NUGGET, 100);
        exp.put(Material.GOLD_NUGGET, 100);
        // other
        exp.put(Material.SPONGE, 100);
        exp.put(Material.CHORUS_FRUIT_POPPED, 150);
    }

    private class SmeltingSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onInventoryClick(InventoryClickEvent event) {
            if (event.getInventory() instanceof FurnaceInventory) {
                InventoryView inventoryView = event.getView();
                int rawSlot = event.getRawSlot();

                if ((rawSlot != inventoryView.convertSlot(rawSlot)) || (rawSlot != 2)) {
                    return;
                }

                ItemStack itemStack = event.getCurrentItem();

                if (itemStack.getType() != Material.AIR) {
                    Player player = (Player) event.getWhoClicked();
                    RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                    if (rpgPlayer == null) {
                        logger.logWarn(RPG.class, "SmeltingSkill.InventoryClickEvent: Player not found!" + player.getDisplayName());
                        return;
                    }

                    int exp = rpgConfiguration.getSmeltingExp(itemStack.getType(), itemStack.getAmount());
                    if (exp > 0) {
                        rpgPlayer.set(SkillType.SMELTING, exp);
                    }
                }
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onMobDamaged(EntityDamageEvent event) {
            if ((event.getEntityType() == EntityType.PLAYER)) {
                switch (event.getCause()) {
                    case LAVA:
                    case FIRE:
                    case FIRE_TICK: {
                        Player player = (Player) event.getEntity();
                        RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                        if (rpgPlayer == null) {
                            logger.logWarn(RPG.class, "SmeltingSkill.EntityDamageEvent: Player not found!" + player.getDisplayName());
                            return;
                        }

                        event.setDamage(((DamageReductionAbility) abilities.get(AbilityType.DAMAGE_REDUCED)).execute(rpgPlayer, event.getDamage()));
                        break;
                    }
                }
            }
        }
    }
}
