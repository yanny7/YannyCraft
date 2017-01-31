package me.noip.yanny;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerAuthEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player player;

    PlayerAuthEvent(Player player) {
        this.player = player;
    }

    Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
