package me.retrorealms.practiceserver.mechanics.anticheat.listeners;

import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class KnockbackListener implements Listener {
    private AdvancedAntiCheat antiCheat;

    public KnockbackListener(AdvancedAntiCheat antiCheat) {
        this.antiCheat = antiCheat;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            ACPlayerData data = antiCheat.getPlayerData(player);
            if (data != null) {
                data.setLastKnockbackTime(System.currentTimeMillis());
            }
        }
    }
}
