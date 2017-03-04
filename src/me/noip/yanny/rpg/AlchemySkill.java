package me.noip.yanny.rpg;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

class AlchemySkill extends Skill {

    AlchemySkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new AlchemySkillListener(), plugin);
    }

    @Override
    Collection<Ability> getAbilities() {
        return new ArrayList<>();
    }

    static void loadDefaults(Map<PotionType, Integer> exp) {
        exp.put(PotionType.MUNDANE, 10);
        exp.put(PotionType.THICK, 10);
        exp.put(PotionType.AWKWARD, 10);
        exp.put(PotionType.NIGHT_VISION, 50);
        exp.put(PotionType.INVISIBILITY, 50);
        exp.put(PotionType.JUMP, 50);
        exp.put(PotionType.SPEED, 50);
        exp.put(PotionType.SLOWNESS, 50);
        exp.put(PotionType.WATER_BREATHING, 50);
        exp.put(PotionType.INSTANT_HEAL, 50);
        exp.put(PotionType.INSTANT_DAMAGE, 50);
        exp.put(PotionType.POISON, 50);
        exp.put(PotionType.STRENGTH, 50);
        exp.put(PotionType.WEAKNESS, 50);
        exp.put(PotionType.LUCK, 50);
        exp.put(PotionType.REGEN, 100);
        exp.put(PotionType.FIRE_RESISTANCE, 100);
    }

    private class AlchemySkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onInventoryClick(InventoryClickEvent event) {
            if (event.getInventory() instanceof BrewerInventory) {
                InventoryView inventoryView = event.getView();
                int rawSlot = event.getRawSlot();

                if ((rawSlot != inventoryView.convertSlot(rawSlot)) || (rawSlot > 2)) {
                    return;
                }

                switch (event.getCurrentItem().getType()) {
                    case POTION:
                    case SPLASH_POTION:
                    case LINGERING_POTION: {
                        ItemStack potion = event.getCurrentItem();
                        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();

                        if (potionMeta.isUnbreakable()) { // disable gaining XP for every take of potion
                            return;
                        }

                        Player player = (Player) event.getWhoClicked();
                        RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                        if (rpgPlayer == null) {
                            plugin.getLogger().warning("RPG.onInventoryClick: Player not found!" + player.getDisplayName());
                            return;
                        }

                        potionMeta.setUnbreakable(true);
                        potionMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                        potion.setItemMeta(potionMeta);

                        int exp = rpgConfiguration.getPotionExp(potionMeta.getBasePotionData().getType());
                        if (exp > 0) {
                            switch (event.getCurrentItem().getType()) {
                                case POTION:
                                    rpgPlayer.set(RpgPlayerStatsType.ALCHEMY, exp);
                                    break;
                                case SPLASH_POTION:
                                    rpgPlayer.set(RpgPlayerStatsType.ALCHEMY, exp * 2);
                                    break;
                                case LINGERING_POTION:
                                    rpgPlayer.set(RpgPlayerStatsType.ALCHEMY, exp * 10);
                                    break;
                            }
                        }
                        break;
                    }
                }
            }
        }

        @SuppressWarnings("unused")
        @EventHandler
        void onBrewPotion(BrewEvent event) {
            BrewerInventory brewerInventory = event.getContents();
            ItemStack[] items = brewerInventory.getContents();

            for (int i = 0; i < 3; i++) { // allow again get XP for alchemy
                if (items[i] != null) {
                    PotionMeta potionMeta = (PotionMeta) items[i].getItemMeta();
                    potionMeta.setUnbreakable(false);
                    items[i].setItemMeta(potionMeta);
                }
            }
        }
    }
}
