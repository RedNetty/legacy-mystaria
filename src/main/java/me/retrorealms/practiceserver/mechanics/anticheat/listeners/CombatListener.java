package me.retrorealms.practiceserver.mechanics.anticheat.listeners;

import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;

public class CombatListener implements Listener {
    private final AdvancedAntiCheat antiCheat;

    public CombatListener(AdvancedAntiCheat antiCheat) {
        this.antiCheat = antiCheat;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            antiCheat.getCheckManager().runChecks(event, damager);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
            Player player = event.getPlayer();
            antiCheat.getPlayerData(player).addAttackTime(System.currentTimeMillis());
        }
    }
}