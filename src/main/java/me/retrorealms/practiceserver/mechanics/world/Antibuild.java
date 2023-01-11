/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockFace
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.ItemFrame
 *  org.bukkit.entity.Minecart
 *  org.bukkit.entity.Painting
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.entity.EntityChangeBlockEvent
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.hanging.HangingBreakByEntityEvent
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.event.inventory.InventoryOpenEvent
 *  org.bukkit.event.inventory.InventoryType
 *  org.bukkit.event.inventory.InventoryType$SlotType
 *  org.bukkit.event.player.PlayerBucketEmptyEvent
 *  org.bukkit.event.player.PlayerBucketFillEvent
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 */
package me.retrorealms.practiceserver.mechanics.world;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Antibuild
        implements Listener {
    public void onEnable() {
        PracticeServer.log.info("[Antibuild] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[Antibuild] has been disabled.");
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onItemCraft(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory().getName().equalsIgnoreCase("container.crafting")) {
            if (e.getSlotType() == InventoryType.SlotType.CRAFTING) {
                e.setCancelled(true);
                p.updateInventory();
            }
        } else if (e.getClick() == ClickType.NUMBER_KEY) {
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory().getName().equalsIgnoreCase("container.crafting") && (e.getRawSlots().contains(1) || e.getRawSlots().contains(2) || e.getRawSlots().contains(3) || e.getRawSlots().contains(4))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getPlayer().isOp()) {
            return;
        }
        if (e.getInventory().getName().equalsIgnoreCase("container.dropper")) {
            e.setCancelled(true);
        }
        if (e.getInventory().getName().equalsIgnoreCase("container.dispenser")) {
            e.setCancelled(true);
        }
        if (e.getInventory().getName().equalsIgnoreCase("container.hopper")) {
            e.setCancelled(true);
        }
        if (e.getInventory().getName().equalsIgnoreCase("container.minecart")) {
            e.setCancelled(true);
        }
        if (e.getInventory().getName().equalsIgnoreCase("container.beacon")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (!e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockChange(EntityChangeBlockEvent e) {
        if (e.getBlock().getType() == Material.SOIL) {
            e.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onFire(PlayerInteractEvent e) {
        Block b;
        BlockFace bf;
        if (!e.getPlayer().isOp() && e.getAction() == Action.LEFT_CLICK_BLOCK && (b = e.getClickedBlock()).getRelative(bf = e.getBlockFace()).getType() == Material.FIRE) {
            e.setCancelled(true);
            e.getPlayer().sendBlockChange(b.getRelative(bf).getLocation(), Material.FIRE, (byte) 0);
        }
    }

    @EventHandler
    public void onAnvil(PlayerInteractEvent e) {
        if (!(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock().getType() != Material.ANVIL && e.getClickedBlock().getType() != Material.WORKBENCH && e.getClickedBlock().getType() != Material.BED && e.getClickedBlock().getType() != Material.FURNACE && e.getClickedBlock().getType() != Material.BURNING_FURNACE && e.getClickedBlock().getType() != Material.DROPPER && e.getClickedBlock().getType() != Material.DISPENSER && e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST && e.getClickedBlock().getType() != Material.BREWING_STAND && e.getClickedBlock().getType() != Material.ENCHANTMENT_TABLE && e.getClickedBlock().getType() != Material.DRAGON_EGG || e.getPlayer().isOp())) {
            e.setCancelled(true);
        }
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && !e.getPlayer().isOp() && !e.getClickedBlock().getType().name().contains("_ORE")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemFrameClick(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof ItemFrame && !e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPaintingBreak(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            Player p = (Player) e.getRemover();
            if (!p.isOp()) {
                e.setCancelled(true);
            }
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemFrameHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && (e.getEntity() instanceof ItemFrame || e.getEntity() instanceof Minecart || e.getEntity() instanceof Painting)) {
            if (e.getDamager() instanceof Player) {
                Player p = (Player) e.getDamager();
                if (!p.isOp()) {
                    e.setCancelled(true);
                    e.setDamage(0.0);
                }
            } else {
                e.setCancelled(true);
                e.setDamage(0.0);
            }
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }
}

