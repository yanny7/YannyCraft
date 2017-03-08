package me.noip.yanny.armorset;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.utils.CustomItemStack;
import me.noip.yanny.utils.PartPlugin;
import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ArmorSet implements PartPlugin {

    private MainPlugin plugin;
    private ArmorSetConfiguration armorSetConfiguration;

    public ArmorSet(MainPlugin plugin) {
        this.plugin = plugin;
        armorSetConfiguration = new ArmorSetConfiguration(plugin);
    }

    @Override
    public void onEnable() {
        armorSetConfiguration.load();

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                Map<CustomItemStack, ItemSet> armorSets = armorSetConfiguration.getArmorSets();

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    PlayerInventory playerInventory = player.getInventory();
                    List<CustomItemStack> playerItems = new ArrayList<>();

                    for (ItemStack itemStack : playerInventory.getArmorContents()) {
                        if ((itemStack != null) && (armorSetConfiguration.getItemSet(new CustomItemStack(itemStack)) != null)) {
                            playerItems.add(new CustomItemStack(itemStack));
                        }
                    }
                    CustomItemStack shield = new CustomItemStack(playerInventory.getItemInOffHand());
                    if (armorSetConfiguration.getItemSet(shield) != null) {
                        playerItems.add(shield);
                    }

                    Map<ItemSet, MutableInt> sets = new HashMap<>();

                    for (CustomItemStack customItemStack : playerItems) {
                        ItemSet itemSet = armorSets.get(customItemStack);

                        if (sets.containsKey(itemSet)) {
                            sets.get(itemSet).add(1);
                        } else {
                            sets.put(itemSet, new MutableInt(1));
                        }
                    }

                    for (Map.Entry<ItemSet, MutableInt> entry : sets.entrySet()) {
                        ItemSet itemSet = entry.getKey();
                        int count = entry.getValue().intValue();
                        Map<PotionEffectType, Integer> effectFromSet = itemSet.getSetEffect(count);

                        if (effectFromSet != null) {
                            for (Map.Entry<PotionEffectType, Integer> effect : effectFromSet.entrySet()) {
                                PotionEffectType potionEffectType = effect.getKey();
                                PotionEffect potionEffect = new PotionEffect(potionEffectType, 20 * 5 + 1, effect.getValue());

                                if (player.hasPotionEffect(potionEffectType)) {
                                    player.removePotionEffect(potionEffectType);
                                }

                                player.addPotionEffect(potionEffect);
                            }
                        }
                    }
                }
            }, 20, 20 * 5
        );
    }

    @Override
    public void onDisable() {

    }

    public Map<CustomItemStack, ItemSet> getArmorSets() {
        return armorSetConfiguration.getArmorSets();
    }
}
