package me.noip.yanny.armorset;

import me.noip.yanny.utils.Rarity;
import me.noip.yanny.utils.CustomItemStack;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ItemSet {
    private String name;
    private List<CustomItemStack> items;
    private Rarity rarity;
    private Map<Integer, Map<PotionEffectType, Integer>> effects;

    ItemSet(String name, List<CustomItemStack> items, Rarity rarity, Map<Integer, Map<PotionEffectType, Integer>> setEffect) {
        this.name = name;
        this.items = items;
        this.rarity = rarity;
        this.effects = setEffect;
        buildLoreFromEffects();
    }

    String getName() {
        return name;
    }

    public List<CustomItemStack> getItems() {
        return items;
    }

    public Rarity getRarity() {
        return rarity;
    }

    Map<PotionEffectType, Integer> getEffect(int setCount) {
        return effects.get(setCount);
    }

    Map<Integer, Map<PotionEffectType, Integer>> getEffects() {
        return effects;
    }

    private void buildLoreFromEffects() {
        List<String> lore = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        lore.add("Set bonus:");

        for (Map.Entry<Integer, Map<PotionEffectType, Integer>> effect : effects.entrySet()) {
            stringBuilder.setLength(0);
            stringBuilder.append(ChatColor.DARK_PURPLE).append(effect.getKey()).append(" items:").append(ChatColor.AQUA);

            for (Map.Entry<PotionEffectType, Integer> eff : effect.getValue().entrySet()) {
                String name = eff.getKey().getName().toLowerCase();
                stringBuilder.append(' ').append(StringUtils.capitalize(name).replace("_", " ")).append('(').append(eff.getValue()).append("),");
            }

            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            lore.add(stringBuilder.toString());
        }

        for (CustomItemStack customItemStack : items) {
            ItemMeta itemMeta = customItemStack.getItemMeta();
            itemMeta.setLore(lore);
            customItemStack.setItemMeta(itemMeta);
        }
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
