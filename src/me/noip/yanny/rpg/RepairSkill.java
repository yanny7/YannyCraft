package me.noip.yanny.rpg;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;

class RepairSkill extends Skill {
    RepairSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new RepairSkillListener(), plugin);
    }

    private class RepairSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onInventoryClick(InventoryClickEvent event) {
            if (event.getInventory() instanceof AnvilInventory) {
                InventoryView inventoryView = event.getView();
                int rawSlot = event.getRawSlot();

                if ((rawSlot != inventoryView.convertSlot(rawSlot)) || (rawSlot != 2)) {
                    return;
                }

                AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
                ItemStack[] items = anvilInventory.getContents();

                if (items[0] == null) {
                    return;
                }

                ItemMeta itemMeta = items[0].getItemMeta();

                if (itemMeta instanceof Repairable) {
                    Repairable repairable = (Repairable) itemMeta;
                    int repairCost = repairable.getRepairCost();
                    Player player = (Player) event.getWhoClicked();

                    if (player.getLevel() >= repairCost + 1) {
                        RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                        if (rpgPlayer == null) {
                            plugin.getLogger().warning("RPG.onInventoryClick: Player not found!" + player.getDisplayName());
                            return;
                        }

                        int exp = rpgConfiguration.getRepairExp(repairCost);
                        if (exp > 0) {
                            rpgPlayer.set(RpgPlayerStatsType.REPAIR, exp);
                        }
                    }
                }
            }
        }
    }
}
