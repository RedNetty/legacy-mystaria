package me.retrorealms.practiceserver.mechanics.crafting.items.celestialbeacon;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class CelestialAllyDamageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final CelestialAlly source;
    private final Player owner;
    private final Entity target;
    private double damage;

    public CelestialAllyDamageEvent(CelestialAlly source, Player owner, Entity target, double damage) {
        this.source = source;
        this.owner = owner;
        this.target = target;
        this.damage = damage;
    }

    public CelestialAlly getSource() { return source; }
    public Player getOwner() { return owner; }
    public Entity getTarget() { return target; }
    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }

    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}




