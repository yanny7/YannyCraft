package me.noip.yanny.rpg;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

class FishingSkill extends Skill {

    FishingSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);

        abilities.put(AbilityType.TREASURE_HUNTER, new TreasureHunterAbility(plugin, SkillType.FISHING, rpgConfiguration));
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new FishingSkillListener(), plugin);
    }

    static void loadDefaults(Map<Rarity, Integer> exp) {
        exp.put(Rarity.SCRAP, 50);
        exp.put(Rarity.COMMON, 100);
        exp.put(Rarity.UNCOMMON, 200);
        exp.put(Rarity.RARE, 500);
        exp.put(Rarity.EXOTIC, 1000);
        exp.put(Rarity.HEROIC, 2000);
        exp.put(Rarity.EPIC, 6000);
        exp.put(Rarity.LEGENDARY, 10000);
        exp.put(Rarity.MYTHIC, 50000);
        exp.put(Rarity.GODLIKE, 100000);
    }

    private class FishingSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onCatchFish(PlayerFishEvent event) {
            if (event.getCaught() != null) {
                switch (event.getState()) {
                    case CAUGHT_ENTITY: {
                        Player player = event.getPlayer();
                        RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                        if (rpgPlayer == null) {
                            plugin.getLogger().warning("RPG.onCatchFish: Player not found!" + player.getDisplayName());
                            return;
                        }

                        event.getCaught().remove();
                        Rarity rarity = ((TreasureHunterAbility) abilities.get(AbilityType.TREASURE_HUNTER)).execute(rpgPlayer, event.getCaught());

                        if (rarity != null) {
                            int exp = rpgConfiguration.getFishingExp(rarity);
                            if (exp > 0) {
                                rpgPlayer.set(SkillType.FISHING, exp);
                            }
                        }

                        break;
                    }
                    case CAUGHT_FISH: {
                        ItemStack item = ((Item) event.getCaught()).getItemStack();
                        Player player = event.getPlayer();
                        RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());
                        int exp = -1;

                        if (rpgPlayer == null) {
                            plugin.getLogger().warning("RPG.onCatchFish: Player not found!" + player.getDisplayName());
                            return;
                        }

                        if (item.getType() == Material.RAW_FISH) {
                            switch (item.getData().getData()) {
                                case 0:
                                    exp = rpgConfiguration.getFishingExp(Rarity.SCRAP);
                                    break;
                                case 1:
                                    exp = rpgConfiguration.getFishingExp(Rarity.UNCOMMON);
                                    break;
                                case 2:
                                    exp = rpgConfiguration.getFishingExp(Rarity.EXOTIC);
                                    break;
                                case 3:
                                    exp = rpgConfiguration.getFishingExp(Rarity.EPIC);
                                    break;
                            }
                        } else {
                            exp = rpgConfiguration.getFishingExp(Rarity.UNCOMMON); // default random treasure set as uncommon treasure
                        }

                        Rarity rarity = ((TreasureHunterAbility) abilities.get(AbilityType.TREASURE_HUNTER)).execute(rpgPlayer, event.getCaught());

                        if (rarity != null) {
                            exp = rpgConfiguration.getFishingExp(rarity);
                            event.getCaught().remove(); // override default catch item
                        }

                        if (exp > 0) {
                            rpgPlayer.set(SkillType.FISHING, exp);
                            return;
                        }

                        break;
                    }
                }
            }
        }
    }
}
