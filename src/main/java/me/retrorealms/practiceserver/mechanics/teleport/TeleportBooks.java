/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerPickupItemEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.util.Vector
 */
package me.retrorealms.practiceserver.mechanics.teleport;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.vendors.ItemVendors;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TeleportBooks implements Listener {
    public static Location stonePeaks;
    public static Location theBeneath;
    public static Location tripoli;

    public static Map<String, Location> teleportingLoc = new HashMap<>();
    public static Map<String, Location> castingLoc = new HashMap<>();
    public static Map<String, Integer> castingTime = new HashMap<>();

    AvalonPortal avalonPortal;

    public void onEnable() {
        avalonPortal = new AvalonPortal();
        Bukkit.getServer().getPluginManager().registerEvents(avalonPortal, PracticeServer.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);

        tripoli = new Location(Bukkit.getWorlds().get(0), 807.0, 19.0, 48.0, 1.0f, 1.0f);
        stonePeaks = new Location(Bukkit.getWorlds().get(0), 649.0, 25.0, -297.0, 1.0f, 1.0f);
        theBeneath = new Location(Bukkit.getWorlds().get(0), 636.0, 96.0, 261.0, 1.0f, 1.0f);

        avalonPortal.onLoad();

        new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (!castingTime.containsKey(p.getName())) continue;

                    if (castingTime.get(p.getName()) == 0) {
                        Particles.SPELL_WITCH.display(0.0f, 0.0f, 0.0f, 0.2f, 200, p.getLocation().clone().add(0.0, 1.0, 0.0), 20.0);
                        p.eject();
                        p.teleport(teleportingLoc.get(p.getName()));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 2));
                        castingTime.remove(p.getName());
                        castingLoc.remove(p.getName());
                        teleportingLoc.remove(p.getName());
                        continue;
                    }

                    p.sendMessage(ChatColor.BOLD + "CASTING" + ChatColor.WHITE + " ... " + castingTime.get(p.getName()) + ChatColor.BOLD + "s");
                    castingTime.put(p.getName(), castingTime.get(p.getName()) - 1);
                    Particles.PORTAL.display(0.0f, 0.0f, 0.0f, 4.0f, 300, p.getLocation(), 20.0);
                    p.getWorld().playEffect(p.getLocation().clone().add(0, 0, 0), Effect.STEP_SOUND, Material.PORTAL);
                    p.getWorld().playEffect(p.getLocation().clone().add(0, 1, 0), Effect.STEP_SOUND, Material.PORTAL);
                    p.getWorld().playEffect(p.getLocation().clone().add(0, 2, 0), Effect.STEP_SOUND, Material.PORTAL);
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20, 20);
    }


    public void onDisable() {

    }

    public static ItemStack createTeleportBook(String destination, boolean inShop) {
        ItemStack is = new ItemStack(Material.BOOK);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.WHITE.toString() + ChatColor.BOLD + "Teleport:" + ChatColor.WHITE + " " + destination);
        String price = inShop ? ChatColor.GREEN + "Price: " + ChatColor.WHITE + (destination.equals("Avalon") ? "100g" : "50g") : "";
        im.setLore(Arrays.asList(ChatColor.GRAY + "Teleports the user to " + destination + ".", price));
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack avalonBook(boolean inShop) {
        return createTeleportBook("Avalon", inShop);
    }

    public static ItemStack deadpeaksBook(boolean inShop) {
        return createTeleportBook("Stone-peaks Manor", inShop);
    }

    public static ItemStack tripoliBook(boolean inShop) {
        return createTeleportBook("Tripoli", inShop);
    }

    @EventHandler
    public void onTP(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        if (itemStack != null && itemStack.getType() == Material.BOOK && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().toLowerCase().contains("teleport:")
                && !castingTime.containsKey(p.getName()) && !Horses.mounting.containsKey(p.getName()) && !Duels.duelers.containsKey(p)) {

            String type = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
            Location loc = getLocationFromString(type);
            int seconds = 5;
            if(itemStack.getAmount() <= 1) {
                p.getInventory().setItemInMainHand(null);
            } else {
                itemStack.setAmount(itemStack.getAmount() - 1);
            }
            p.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "CASTING " + ChatColor.WHITE + getTeleportMessage(type) + " ... " + seconds + ChatColor.BOLD + "s");
            teleportingLoc.put(p.getName(), loc);
            castingLoc.put(p.getName(), p.getLocation());
            castingTime.put(p.getName(), seconds);
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (seconds + 3) * 20, 1));
            p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 1.0f);
        }
    }


    @EventHandler
    public void onCancelDamager(EntityDamageByEntityEvent e) {
        Player p;
        if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && castingTime.containsKey((p = (Player) e.getDamager()).getName())) {
            castingTime.remove(p.getName());
            castingLoc.remove(p.getName());
            teleportingLoc.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Teleportation - " + ChatColor.BOLD + "CANCELLED");
            p.removePotionEffect(PotionEffectType.CONFUSION);
        }
    }

    @EventHandler
    public void onCancelDamage(EntityDamageEvent e) {
        if (e.getDamage() <= 0.0 || !(e.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity();
        if (castingTime.containsKey(p.getName())) {
            cancelTeleportation(p);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (castingTime.containsKey(p.getName())) {
            removePlayerFromTeleportation(p);
        }
    }

    @EventHandler
    public void onCancelMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (castingTime.containsKey(p.getName()) && (castingLoc.get(p.getName())).distanceSquared(e.getTo()) >= 2.0) {
            cancelTeleportation(p);
        }
    }

    private void cancelTeleportation(Player p) {
        removePlayerFromTeleportation(p);
        p.sendMessage(ChatColor.RED + "Teleportation - " + ChatColor.BOLD + "CANCELLED");
        p.removePotionEffect(PotionEffectType.CONFUSION);
    }

    private void removePlayerFromTeleportation(Player p) {
        castingTime.remove(p.getName());
        castingLoc.remove(p.getName());
        teleportingLoc.remove(p.getName());
    }

    Location getLocationFromString(String s) {
        String location = s.toLowerCase();
        if (location.contains("avalon")) {
            return theBeneath;
        }
        if (location.contains("stone-peaks")) {
            return stonePeaks;
        }
        if (location.contains("tripoli")) {
            return tripoli;
        }
        return stonePeaks;
    }

    String getTeleportMessage(String s) {
        String location = s.toLowerCase();
        if (location.contains("avalon")) {
            return "Teleport Scroll: Avalon";
        }
        if (location.contains("stonepeaks")) {
            return "Teleport Scroll: Stone-peaks Manor";
        }
        if (location.contains("tripoli")) {
            return "Teleport Scroll: Tripoli";
        }
        return "Teleport Scroll: Stone-peaks Manor";
    }

    private boolean isPlayerInArea(Location location, double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        return location.getX() > minX && location.getX() < maxX && location.getY() > minY && location.getY() < maxY && location.getZ() < minZ && location.getZ() > maxZ;
    }

    public static Location generateRandomSpawnPoint(String s) {
        if (!Alignments.chaotic.containsKey(s)) {
            return stonePeaks;
        }

        ArrayList<Location> spawns = new ArrayList<>();
        spawns.add(new Location(Bukkit.getWorlds().get(0), 672.0, 5.0, -233.0));
        spawns.add(new Location(Bukkit.getWorlds().get(0), 637.0, 4.0, -219.0));
        spawns.add(new Location(Bukkit.getWorlds().get(0), 609.0, 5.0, -237.0));
        return spawns.get(new Random().nextInt(spawns.size()));
    }


}