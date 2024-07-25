package me.retrorealms.practiceserver.mechanics.anticheat.checks.movement;

import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.Check;
import me.retrorealms.practiceserver.mechanics.anticheat.utils.BoundingBox;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

public class NoClipCheck extends Check {
    private static final int MAX_FAILED_CHECKS = 5;
    private static final long CHECK_INTERVAL = 50; // 50ms
    private static final long TELEPORT_GRACE_PERIOD = 1000; // 1 second

    public NoClipCheck(AdvancedAntiCheat antiCheat) {
        super(antiCheat, "NOCLIP");
    }

    @Override
    public boolean isApplicable(Event event) {
        return event instanceof PlayerMoveEvent;
    }

    @Override
    public void check(Player player, ACPlayerData data, Event event) {
        PlayerMoveEvent moveEvent = (PlayerMoveEvent) event;
        Location to = moveEvent.getTo();

        if (isExempt(player, data)) return;

        if (System.currentTimeMillis() - data.getLastNoClipCheck() < CHECK_INTERVAL) return;
        data.setLastNoClipCheck(System.currentTimeMillis());

        if (isInsideSolidBlock(to, player)) {
            data.incrementFailedNoClipChecks();
            if (data.getFailedNoClipChecks() >= MAX_FAILED_CHECKS) {
               // fail(player, data.getFailedNoClipChecks());
                data.resetFailedNoClipChecks();
            }
        } else {
            data.resetFailedNoClipChecks();
        }
    }

    private boolean isExempt(Player player, ACPlayerData data) {
        return player.getGameMode() == GameMode.SPECTATOR ||
                player.isGliding() ||
                player.getLocation().getBlock().getType() == Material.WATER ||
                System.currentTimeMillis() - data.getLastTeleportTime() < TELEPORT_GRACE_PERIOD ||
                isNearPortal(player);
    }

    private boolean isInsideSolidBlock(Location location, Player player) {
        BoundingBox playerBox = getPlayerBoundingBox(player);
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = location.clone().add(x, y, z).getBlock();
                    if (block.getType().isSolid() && !antiCheat.isPassableBlock(block)) {
                        BoundingBox blockBox = getBlockBoundingBox(block);
                        if (playerBox.overlaps(blockBox)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private BoundingBox getPlayerBoundingBox(Player player) {
        Location loc = player.getLocation();
        double width = 0.6;
        double height = 1.8;
        return new BoundingBox(loc.getX() - width/2, loc.getY(), loc.getZ() - width/2,
                loc.getX() + width/2, loc.getY() + height, loc.getZ() + width/2);
    }

    private BoundingBox getBlockBoundingBox(Block block) {
        Location loc = block.getLocation();
        return new BoundingBox(loc.getX(), loc.getY(), loc.getZ(),
                loc.getX() + 1, loc.getY() + 1, loc.getZ() + 1);
    }

    private boolean isNearPortal(Player player) {
        Location loc = player.getLocation();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = loc.getBlock().getRelative(x, y, z);
                    if (block.getType() == Material.PORTAL || block.getType() == Material.ENDER_PORTAL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}