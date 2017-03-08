package me.noip.yanny.armorset;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.PartPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ArmorSet implements PartPlugin {

    private MainPlugin plugin;
    private ArmorSetConfiguration armorSetConfiguration;
    private Map<UUID, Set<ItemStack>> playerSets = new HashMap<>();

    public ArmorSet(MainPlugin plugin) {
        this.plugin = plugin;
        armorSetConfiguration = new ArmorSetConfiguration(plugin);
    }

    @Override
    public void onEnable() {
        armorSetConfiguration.load();

        plugin.getServer().getPluginManager().registerEvents(new ArmorSetListener(), plugin);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerInventory playerInventory = player.getInventory();
            UUID uuid = player.getUniqueId();
            List<ItemStack> items = new ArrayList<>();

            items.addAll(Arrays.asList(playerInventory.getArmorContents()));
            items.add(playerInventory.getItemInOffHand());

            if (!playerSets.containsKey(uuid)) {
                playerSets.put(uuid, new HashSet<>());
            }

            for (ItemStack item : items) {
                if (armorSetConfiguration.getItemSet(item) != null) {
                    playerSets.get(uuid).add(item);
                }
            }
        }
    }

    @Override
    public void onDisable() {

    }

    public Map<ItemStack, ItemSet> getArmorSets() {
        return armorSetConfiguration.getArmorSets();
    }

    private class ArmorSetListener implements Listener {
        private final Set<Integer> slots = new HashSet<>(Arrays.asList(new Integer[]{ 5, 6, 7, 8, 45 }));

        //@SuppressWarnings("unused")
        @EventHandler
        void InventoryClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player) || !(event.getInventory() instanceof CraftingInventory) ||
                    !slots.contains(event.getRawSlot())) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            UUID uuid = player.getUniqueId();
            ItemStack handItem = event.getCurrentItem();
            ItemStack slotItem = event.getCursor();

            if (!playerSets.containsKey(uuid)) {
                playerSets.put(uuid, new HashSet<>());
            }

            if (armorSetConfiguration.getItemSet(slotItem) != null) {
                plugin.getLogger().info("Put Set item on player!");
                playerSets.get(uuid).add(slotItem);
            } else if (armorSetConfiguration.getItemSet(handItem) != null) {
                plugin.getLogger().info("Removed Set item from player!");
                playerSets.get(uuid).remove(handItem);
            }
        }

        @EventHandler
        void PlayerDeath(PlayerDeathEvent event) {
            Player player = event.getEntity();
            Set<ItemStack> sets = playerSets.get(player.getUniqueId());

            if (playerSets.get(player.getUniqueId()) != null) {
                for (ItemStack itemStack : event.getDrops()) {
                    if (sets.contains(itemStack)) {
                        plugin.getLogger().info("Removed Set item from player!");
                        sets.remove(itemStack);
                    }
                }
            }
        }
    }
}
