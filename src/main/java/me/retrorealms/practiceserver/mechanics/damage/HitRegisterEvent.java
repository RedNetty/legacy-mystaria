package me.retrorealms.practiceserver.mechanics.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class HitRegisterEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private LivingEntity entity;
    private double damage;
    private boolean isCancelled;

    public HitRegisterEvent(Player player, LivingEntity entity, double damage) {
        this.player = player;
        this.entity = entity;
        this.damage = damage;
        this.isCancelled = false;
    }

    public void setDamage(double damage) {
        this.damage = damage;
        new EntityDamageByEntityEvent(player, entity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage);
    }

    public double getDamage() {
        return damage;
    }

    public Player getDamager() {
        return player;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
