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
    public static Location DeadPeaks;
    public static Location Avalon;
    public static Location Tripoli;

    public static HashMap<String, Location> teleporting_loc;
    public static HashMap<String, Location> casting_loc;
    public static HashMap<String, Integer> casting_time;

    static {
        teleporting_loc = new HashMap<String, Location>();
        casting_loc = new HashMap<String, Location>();
        casting_time = new HashMap<String, Integer>();
    }

    public void onEnable() {

        AvalonPortal avalonPortal = new AvalonPortal();
        Bukkit.getServer().getPluginManager().registerEvents(avalonPortal, PracticeServer.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        Tripoli = new Location(Bukkit.getWorlds().get(0), 817.0, 9.0, -80.0, 1.0f, 1.0f);
        DeadPeaks = new Location(Bukkit.getWorlds().get(0), 603.0, 35.0, -281.0, 1.0f, 1.0f);
        Avalon = new Location(Bukkit.getWorlds().get(0), 636.0, 97.0, 243.0, 1.0f, 1.0f);
         avalonPortal.onLoad();
        new BukkitRunnable() {

            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (!TeleportBooks.casting_time.containsKey(p.getName())) continue;
                    if (TeleportBooks.casting_time.get(p.getName()) == 0) {
                        Particles.SPELL_WITCH.display(0.0f, 0.0f, 0.0f, 0.2f, 200, p.getLocation().add(0.0, 1.0, 0.0), 20.0);
                        p.eject();
                        p.teleport(TeleportBooks.teleporting_loc.get(p.getName()));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 2));
                        TeleportBooks.casting_time.remove(p.getName());
                        TeleportBooks.casting_loc.remove(p.getName());
                        TeleportBooks.teleporting_loc.remove(p.getName());
                        continue;
                    }
                    p.sendMessage(ChatColor.BOLD + "CASTING" + ChatColor.WHITE + " ... " + TeleportBooks.casting_time.get(p.getName()) + ChatColor.BOLD + "s");
                    TeleportBooks.casting_time.put(p.getName(), TeleportBooks.casting_time.get(p.getName()) - 1);
                    Particles.PORTAL.display(0.0f, 0.0f, 0.0f, 4.0f, 300, p.getLocation(), 20.0);
                    p.getWorld().playEffect(p.getLocation().add(0, 0, 0), Effect.STEP_SOUND, Material.PORTAL);
                    p.getWorld().playEffect(p.getLocation().add(0, 1, 0), Effect.STEP_SOUND, Material.PORTAL);
                    p.getWorld().playEffect(p.getLocation().add(0, 2, 0), Effect.STEP_SOUND, Material.PORTAL);
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20, 20);
    }

    public void onDisable() {

    }

    public static ItemStack avalonBook(boolean inshop) {
        ItemStack is = new ItemStack(Material.BOOK);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.WHITE.toString() + ChatColor.BOLD + "Teleport:" + ChatColor.WHITE + " Avalon");
        if (inshop == false) {
            im.setLore(Arrays.asList(ChatColor.GRAY + "Teleports the user to the lost city of Avalon."));
        } else if (inshop == true) {
            im.setLore(Arrays.asList(ChatColor.GRAY + "Teleports the user to the lost city of Avalon.", ChatColor.GREEN + "Price: " + ChatColor.WHITE + "100g"));
        }
        is.setItemMeta(im);
        return is;
    }


    public static ItemStack deadpeaks_book(boolean inshop) {
        ItemStack is = new ItemStack(Material.BOOK);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.WHITE.toString() + ChatColor.BOLD + "Teleport:" + ChatColor.WHITE + " Deadpeaks Mountain Camp");
        if (!inshop) {
            im.setLore(Arrays.asList(ChatColor.GRAY + "Teleports the user to the Deadpeaks."));
        } else{
            im.setLore(Arrays.asList(ChatColor.GRAY + "Teleports the user to the Deadpeaks.", ChatColor.GREEN + "Price: " + ChatColor.WHITE + "50g"));
        }
        is.setItemMeta(im);
        return is;
    }
    public static ItemStack tripoli_book(boolean inshop) {
        ItemStack is = new ItemStack(Material.BOOK);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.WHITE.toString() + ChatColor.BOLD + "Teleport:" + ChatColor.WHITE + " Tripoli");
        if (!inshop) {
            im.setLore(Arrays.asList(ChatColor.GRAY + "Teleports the user to Tripoli."));
        } else if (inshop) {
            im.setLore(Arrays.asList(ChatColor.GRAY + "Teleports the user to Tripoli.", ChatColor.GREEN + "Price: " + ChatColor.WHITE + "50g"));
        }
        is.setItemMeta(im);
        return is;
    }

    /*@EventHandler
    public void onVoteMessage(PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();

        if (event.getMessage().equalsIgnoreCase("/vote")) {
            event.setCancelled(true);
            String vote0 = ChatColor.translateAlternateColorCodes('&', "&5-----------------------------------------------------");
            String vote1 = ChatColor.translateAlternateColorCodes('&', "&bVote and receive an &d&lORB &bper vote!");
            String vote2 = ChatColor.translateAlternateColorCodes('&', "&bWhen voting you have a chance to receive a T5 Prot Scroll!");
            String vote3 = ChatColor.translateAlternateColorCodes('&', "&5-----------------------------------------------------");
            p.sendMessage(vote0);
            p.sendMessage(vote1);
            p.sendMessage(vote2);
            p.sendMessage(vote3);

            String vote4 = ChatColor.translateAlternateColorCodes('&', "&2Sub = 2 &d&lOrbs &r&2per vote!");
            String vote5 = ChatColor.translateAlternateColorCodes('&', "&2Sub+ = 3 &d&lOrbs &r&2per vote!");
            String vote6 = ChatColor.translateAlternateColorCodes('&', "&2Sub++ = 4 &d&lOrbs &r&2per vote!");
            String vote7 = ChatColor.translateAlternateColorCodes('&', "&2Supporter = 5 &d&lOrbs &r&2per vote!");
            String vote8 = ChatColor.translateAlternateColorCodes('&', "&5-----------------------------------------------------");
            p.sendMessage(vote4);
            p.sendMessage(vote5);
            p.sendMessage(vote6);
            p.sendMessage(vote7);
            p.sendMessage(vote8);


            String[] strings = new String[]
                    {"&bhttp://minecraft-server-list.com/server/396627/vote/",
                            "&bhttp://minecraftservers.org/vote/436173",
                            "&bhttp://topg.org/Minecraft/in-460876",
                            "&bhttp://minecraftservers100.com/vote/4367",
                            "&bhttps://minecraft-server.net/vote/Autismrealms",
                            "&5-----------------------------------------------------"};

            for (String string : strings)
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', string));
        }
    }*/

    @EventHandler
    public void onTP(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        if (itemStack != null && itemStack.getType() == Material.BOOK && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().toLowerCase().contains("teleport:")
                && !casting_time.containsKey(p.getName()) && !Horses.mounting.containsKey(p.getName()) && !Duels.duelers.containsKey(p)) {

            String type = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
            Location loc = getLocationFromString(type);
            int seconds = 5;
            if(itemStack.getAmount() <= 1) {
                p.getInventory().setItemInMainHand(null);
            } else {
                itemStack.setAmount(itemStack.getAmount() - 1);
            }
            p.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "CASTING " + ChatColor.WHITE + getTeleportMessage(type) + " ... " + seconds + ChatColor.BOLD + "s");
            teleporting_loc.put(p.getName(), loc);
            casting_loc.put(p.getName(), p.getLocation());
            casting_time.put(p.getName(), seconds);
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (seconds + 3) * 20, 1));
            p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 1.0f);
        }
    }


    @EventHandler
    public void onCancelDamager(EntityDamageByEntityEvent e) {
        Player p;
        if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && casting_time.containsKey((p = (Player) e.getDamager()).getName())) {
            casting_time.remove(p.getName());
            casting_loc.remove(p.getName());
            teleporting_loc.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Teleportation - " + ChatColor.BOLD + "CANCELLED");
            p.removePotionEffect(PotionEffectType.CONFUSION);
        }
    }

    @EventHandler
    public void onCancelDamage(EntityDamageEvent e) {
        Player p;
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (e.getEntity() instanceof Player && casting_time.containsKey((p = (Player) e.getEntity()).getName())) {
            casting_time.remove(p.getName());
            casting_loc.remove(p.getName());
            teleporting_loc.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Teleportation - " + ChatColor.BOLD + "CANCELLED");
            p.removePotionEffect(PotionEffectType.CONFUSION);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (casting_time.containsKey(p.getName())) {
            casting_time.remove(p.getName());
            casting_loc.remove(p.getName());
            teleporting_loc.remove(p.getName());
        }
    }

    @EventHandler
    public void onCancelMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (casting_time.containsKey(p.getName()) && (casting_loc.get(p.getName())).distanceSquared(e.getTo()) >= 2.0) {
            casting_time.remove(p.getName());
            casting_loc.remove(p.getName());
            teleporting_loc.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Teleportation - " + ChatColor.BOLD + "CANCELLED");
            p.removePotionEffect(PotionEffectType.CONFUSION);
        }
    }


    Location getLocationFromString(String s) {
        if (s.toLowerCase().contains("avalon")) {
            return Avalon;
        }

        if (s.toLowerCase().contains("deadpeaks mountain camp")) {
            return DeadPeaks;
        }

        if (s.toLowerCase().contains("tripoli")) {
            return Tripoli;
        }
        return DeadPeaks;
    }

    String getTeleportMessage(String s) {
        if (s.toLowerCase().contains("avalon")) {
            return "Teleport Scroll: Avalon";
        }
        if (s.toLowerCase().contains("deadpeaks")) {
            return "Teleport Scroll: Deadpeaks Mountain Camp";
        }
        if (s.toLowerCase().contains("tripoli")) {
            return "Teleport Scroll: Tripoli";
        }
        return "Teleport Scroll: Deadpeaks Mountain Camp";
    }

    @EventHandler
    public void onAvalonTp(PlayerMoveEvent e) {
        Location to = e.getTo();
        Location enter = new Location(Bukkit.getWorlds().get(0), -357.5, 171.0, -3440.5);
        Location exit = new Location(Bukkit.getWorlds().get(0), -1158.5, 95.0, -515.5);
        if (to.getX() > -1155.0 && to.getX() < -1145.0 && to.getY() > 90.0 && to.getY() < 100.0 && to.getZ() < -500.0 && to.getZ() > -530.0) {
            e.getPlayer().teleport(enter.setDirection(to.getDirection()));
        }
        if (to.getX() < -360.0 && to.getX() > -370.0 && to.getY() > 165.0 && to.getY() < 190.0 && to.getZ() < -3426.0 && to.getZ() > -3455.0) {
            e.getPlayer().teleport(exit.setDirection(to.getDirection()));
        }
    }

    public static Location generateRandomSpawnPoint(String s) {
        ArrayList<Location> spawns = new ArrayList<Location>();
        if (Alignments.chaotic.containsKey(s)) {
            spawns.add(new Location(Bukkit.getWorlds().get(0), 672.0, 5.0, -233.0));
            spawns.add(new Location(Bukkit.getWorlds().get(0), 637.0, 4.0, -219.0));
            spawns.add(new Location(Bukkit.getWorlds().get(0), 609.0, 5.0, -237.0));
            return spawns.get(new Random().nextInt(spawns.size()));
        }
        return DeadPeaks;
    }

}