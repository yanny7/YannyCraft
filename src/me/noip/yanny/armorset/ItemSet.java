package me.noip.yanny.armorset;

import me.noip.yanny.rpg.Rarity;
import me.noip.yanny.utils.CustomItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public class ItemSet {
    private String name;
    private List<CustomItemStack> items;
    private Rarity rarity;
    private Map<Integer, Map<PotionEffectType, Integer>> setEffect;

    ItemSet(String name, List<CustomItemStack> items, Rarity rarity, Map<Integer, Map<PotionEffectType, Integer>> setEffect) {
        this.name = name;
        this.items = items;
        this.rarity = rarity;
        this.setEffect = setEffect;
    }

    String getName() {
        return name;
    }

    List<CustomItemStack> getItems() {
        return items;
    }

    public Rarity getRarity() {
        return rarity;
    }

    Map<PotionEffectType, Integer> getSetEffect(int count) {
        return setEffect.get(count);
    }

    Map<Integer, Map<PotionEffectType, Integer>> getSetEffects() {
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
