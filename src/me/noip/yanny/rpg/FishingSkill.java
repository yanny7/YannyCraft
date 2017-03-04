package me.noip.yanny.rpg;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

class FishingSkill extends Skill {

    private final Random random = new Random();

    FishingSkill(Plugin plugin, Map<UUID, RpgPlayer> rpgPlayerMap, RpgConfiguration rpgConfiguration) {
        super(plugin, rpgPlayerMap, rpgConfiguration);
    }

    @Override
    void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new FishingSkillListener(), plugin);
    }

    @Override
    Collection<Ability> getAbilities() {
        return new ArrayList<>();
    }

    private class FishingSkillListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        void onCatchFish(PlayerFishEvent event) {
            if (event.getCaught() != null) {
                switch (event.getState()) {
                    case CAUGHT_ENTITY: {
                        Rarity[] values = Rarity.values();
                        for (int i = values.length - 1; i >= 0; i--) {
                            Rarity next = values[i];
                            double rand = random.nextDouble();

                            if (rand <= next.getProbability()) {
                                int exp = rpgConfiguration.getFishingExp(next);
                                if (exp > 0) {
                                    Player player = event.getPlayer();
                                    RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                                    if (rpgPlayer == null) {
                                        plugin.getLogger().warning("RPG.onCatchFish: Player not found!" + player.getDisplayName());
                                        return;
                                    }
                                    rpgPlayer.set(RpgPlayerStatsType.FISHING, exp);
                                    return;
                                }
                                return;
                            }
                        }
                        break;
                    }
                    case CAUGHT_FISH: {
                        ItemStack fish = ((Item) event.getCaught()).getItemStack();
                        int exp = -1;

                        switch (fish.getData().getData()) {
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

                        if (exp > 0) {
                            Player player = event.getPlayer();
                            RpgPlayer rpgPlayer = rpgPlayerMap.get(player.getUniqueId());

                            if (rpgPlayer == null) {
                                plugin.getLogger().warning("RPG.onCatchFish: Player not found!" + player.getDisplayName());
                                return;
                            }
                            rpgPlayer.set(RpgPlayerStatsType.FISHING, exp);
                            return;
                        }
                        break;
                    }
                }
            }
        }
    }
}
