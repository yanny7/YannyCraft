package me.noip.yanny.rpg;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;

class SmeltingSkill extends Skill {

    SmeltingSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new SmeltingSkillListener(), plugin);
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
                        plugin.getLogger().warning("RPG.onInventoryClick: Player not found!" + player.getDisplayName());
                        return;
                    }

                    int exp = rpgConfiguration.getSmeltingExp(itemStack.getType(), itemStack.getAmount());
                    if (exp > 0) {
                        rpgPlayer.set(RpgPlayerStatsType.SMELTING, exp);
                    }
                }
            }
        }
    }
}
