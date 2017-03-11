package me.noip.yanny.armorset;

import me.noip.yanny.MainPlugin;
import me.noip.yanny.rpg.Rarity;
import me.noip.yanny.utils.CustomItemStack;
import me.noip.yanny.utils.PartPlugin;
import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

        plugin.getCommand("armorset").setExecutor(new ArmorSetExecutor());

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
                        Map<PotionEffectType, Integer> effectFromSet = itemSet.getEffect(count);

                        if (effectFromSet != null) {
                            for (Map.Entry<PotionEffectType, Integer> effect : effectFromSet.entrySet()) {
                                PotionEffectType potionEffectType = effect.getKey();
                                PotionEffect potionEffect = new PotionEffect(potionEffectType, 20 * 2 + 1, effect.getValue() - 1);

                                if (player.hasPotionEffect(potionEffectType)) {
                                    player.removePotionEffect(potionEffectType);
                                }

                                player.addPotionEffect(potionEffect);
                            }
                        }
                    }
                }
            }, 20, 20 * 2
        );
    }

    @Override
    public void onDisable() {

    }

    public Map<CustomItemStack, ItemSet> getArmorSets() {
        return armorSetConfiguration.getArmorSets();
    }

    private class ArmorSetExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (args.length == 0) {
                return false;
            }

            switch (args[0]) {
                case "list": {
                    if ((args.length != 2) || (Rarity.getByName(args[1]) == null)) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /armorset list " + Arrays.toString(Rarity.values()));
                        return true;
                    }

                    Rarity rarity = Rarity.getByName(args[1]);
                    List<ItemSet> sets = armorSetConfiguration.getSetByRarity(rarity);
                    ChatColor color = rarity.getChatColor();

                    if (sets != null) {
                        int i = 0;
                        for (ItemSet itemSet : sets) {
                            commandSender.sendMessage(String.format("[%d: %s%s%s] ", i++, color, itemSet.getName(), ChatColor.RESET));
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "No item sets for rarity " + rarity.name());
                    }

                    break;
                }
                case "get": {
                    if (!(commandSender instanceof Player) || (args.length != 3) || (Rarity.getByName(args[2]) == null)) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /armorset get [ID] " + Arrays.toString(Rarity.values()));
                        return true;
                    }

                    int id;

                    try {
                        id = Integer.parseInt(args[1]);
                    } catch (Exception e) {
                        commandSender.sendMessage(ChatColor.RED + "Cant parse item ID: " + e.getLocalizedMessage());
                        return true;
                    }

                    Rarity rarity = Rarity.getByName(args[2]);
                    List<ItemSet> itemSets = armorSetConfiguration.getSetByRarity(rarity);

                    if (itemSets == null) {
                        commandSender.sendMessage(ChatColor.RED + "No item sets for rarity " + rarity.name());
                        return true;
                    }

                    ItemSet itemSet = itemSets.get(id);

                    if (itemSet == null) {
                        commandSender.sendMessage(ChatColor.RED + "No item set with ID " + id);
                        return true;
                    }

                    Player player = (Player) commandSender;

                    for (ItemStack itemStack : itemSet.getItems()) {
                        player.getInventory().addItem(itemStack);
                    }

                    break;
                }
                default:
                    return false;
            }

            return true;
        }
    }
}
