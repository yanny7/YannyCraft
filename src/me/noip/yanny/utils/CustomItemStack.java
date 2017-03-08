package me.noip.yanny.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CustomItemStack extends ItemStack {
    public CustomItemStack(Material material) {
        super(material);
    }

    public CustomItemStack(ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public int hashCode() {
        byte hash = 1;
        int hash1 = hash * 31 + this.getTypeId();
        hash1 = hash1 * 31 + this.getAmount();
        hash1 = hash1 * 31 + (this.hasItemMeta()?(this.getItemMeta() == null?this.getItemMeta().hashCode():this.getItemMeta().hashCode()):0);
        return hash1;
    }

    @Override
    public boolean isSimilar(ItemStack stack) {
        return stack != null && (stack == this || this.getTypeId() == stack.getTypeId() && this.hasItemMeta() == stack.hasItemMeta() && (!this.hasItemMeta() || Bukkit.getItemFactory().equals(this.getItemMeta(), stack.getItemMeta())));
    }
}
