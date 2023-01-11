package me.retrorealms.practiceserver.mechanics.patch;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Created by Giovanni on 7-7-2017.
 */
public class PatchListener implements Listener {


    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory.getName().toLowerCase().contains("patch notes")) event.setCancelled(true);
    }
}

