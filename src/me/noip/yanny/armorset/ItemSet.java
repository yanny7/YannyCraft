package me.noip.yanny.armorset;

import me.noip.yanny.rpg.Rarity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class ItemSet {
    private String name;
    private List<ItemStack> items;
    private Rarity rarity;
    private Map<Integer, Map<Enchantment, Integer>> setEffect;

    ItemSet(String name, List<ItemStack> items, Rarity rarity, Map<Integer, Map<Enchantment, Integer>> setEffect) {
        this.name = name;
        this.items = items;
        this.rarity = rarity;
        this.setEffect = setEffect;
    }

    String getName() {
        return name;
    }

    List<ItemStack> getItems() {
        return items;
    }

    public Rarity getRarity() {
        return rarity;
    }

    Map<Integer, Map<Enchantment, Integer>> getSetEffect() {
        return setEffect;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ItemSet) {
            ItemSet obj = (ItemSet) o;

            return this.name.equals(obj.name);
        }

        return false;
    }
}
