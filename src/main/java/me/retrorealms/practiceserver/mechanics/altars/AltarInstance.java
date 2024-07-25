package me.retrorealms.practiceserver.mechanics.altars;

import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.enchants.Orbs;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.utils.GlowAPI;
import me.retrorealms.practiceserver.utils.JSONMessage;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

class AltarInstance {
    public Player p;
    public List<ItemStack> items;
    public Location location;
    public List<Item> droppedItems;

    AltarInstance(List<ItemStack> items, Location location, Player p) {
        this.items = items;
        this.location = location.clone(); // Ensure we have a copy of the location
        this.p = p;
        droppedItems = new ArrayList<>();
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public Location getLocation() {
        return location.clone(); // Return a copy to prevent external modification
    }

    public void generateItem(Location alterLoc) {
        Random r = new Random();
        List<Integer> itemIDs = new ArrayList<>();
        for (ItemStack is : items) {
            itemIDs.add(Altar.itemID(is));
        }
        List<String> itemlore = items.get(0).getItemMeta().getLore();

        int itemID = itemIDs.get(r.nextInt(itemIDs.size()));
        int tier = Altar.getTier(items.get(0));
        int rarity = Altar.RarityToInt(itemlore.get(itemlore.size() - 1));

        ItemStack createdItem = CreateDrop.createDrop(tier, itemID, Math.min(rarity + 1, 4));

        String reason = " has created a(n) ";
        final JSONMessage normal = new JSONMessage(p.getDisplayName() + ChatColor.RESET + reason, ChatColor.WHITE);
        List<String> hoveredChat = new ArrayList<>();
        ItemMeta meta = createdItem.getItemMeta();
        hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : createdItem.getType().name()));
        if (meta.hasLore()) hoveredChat.addAll(meta.getLore());
        normal.addHoverText(hoveredChat, ChatColor.getLastColors(createdItem.getItemMeta().getDisplayName()) + ChatColor.BOLD + ChatColor.UNDERLINE + "SHOW");
        normal.addText(" using the Altar!");
        for (Entity near : location.getWorld().getNearbyEntities(location, 50, 50, 50)) {
            if (near instanceof Player) {
                Player nearPlayers = (Player) near;
                normal.sendToPlayer(nearPlayers);
            }
        }
        p.getInventory().addItem(createdItem);

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
        final Firework fw = (Firework) p.getWorld().spawnEntity(alterLoc, EntityType.FIREWORK);
        final FireworkMeta fwm = fw.getFireworkMeta();
        final FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
    }

    public void cancelAltar() {
        if (!items.isEmpty()) {
            for (ItemStack item : items) {
                p.getInventory().addItem(item);
                p.sendMessage(ChatColor.RED + "- " + item.getItemMeta().getDisplayName());
            }
            for (Item i : droppedItems) i.remove();
            Altar.alterTimeout.put(p, System.currentTimeMillis() + (110 * 50L));
            Altar.altarInstances.remove(p);
            p.sendMessage(ChatColor.RED + ">> Altar has been cancelled!");
        }
    }

    public void addItem(Player p, ItemStack hand, int max) {
        items.add(hand);
        p.getInventory().setItemInMainHand(null);
        p.sendMessage(ChatColor.GRAY + "+ " + hand.getItemMeta().getDisplayName() + ChatColor.GRAY + " (" + items.size() + "/" + max + ")");

        ItemStack is = new ItemStack(hand.getType(), 1);
        final ItemMeta meta = is.getItemMeta();
        meta.setLore(Collections.singletonList("notarealitem"));
        is.setItemMeta(meta);
        Item i = location.getWorld().dropItem(randomOffset(location, 2), is);
        droppedItems.add(i);
        GlowAPI.setGlowing(i, ChatColor.AQUA);
    }

    private Location randomOffset(Location loc, int radius) {
        Random r = new Random();
        double xoffset = r.nextInt(radius * 200) / 100.0 - radius;
        double zoffset = r.nextInt(radius * 200) / 100.0 - radius;
        while (Math.sqrt(Math.pow(xoffset, 2) + Math.pow(zoffset, 2)) > radius) {
            xoffset = (r.nextInt(radius * 200) / 100.0) - radius;
            zoffset = (r.nextInt(radius * 200) / 100.0) - radius;
        }
        return loc.clone().add(xoffset, 0, zoffset);
    }
}