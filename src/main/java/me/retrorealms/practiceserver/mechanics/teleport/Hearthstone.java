package me.retrorealms.practiceserver.mechanics.teleport;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;

public class Hearthstone
        implements Listener {
    public static HashMap<String, Integer> casting = new HashMap<String, Integer>();
    public static HashMap<String, Location> castingloc = new HashMap<String, Location>();

    public void onEnable() {
        PracticeServer.log.info("[Hearthstone] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        new BukkitRunnable() {

            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (!p.isOnline() || !Hearthstone.casting.containsKey(p.getName())) continue;
                    if (Hearthstone.casting.get(p.getName()) == 0) {
                        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
                        Hearthstone.casting.remove(p.getName());
                        Hearthstone.castingloc.remove(p.getName());
                        p.eject();
                        p.teleport(TeleportBooks.stonePeaks);
                        continue;
                    }
                    Particles.SPELL.display(0.0f, 0.0f, 0.0f, 0.5f, 80, p.getLocation().clone().add(0.0, 0.15, 0.0), 20.0);
                    p.sendMessage(ChatColor.BOLD + "TELEPORTING" + ChatColor.WHITE + " ... " + Hearthstone.casting.get(p.getName()) + "s");
                    Hearthstone.casting.put(p.getName(), Hearthstone.casting.get(p.getName()) - 1);
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20, 20);
    }

    public void onDisable() {
        PracticeServer.log.info("[Hearthstone] has been disabled.");
    }

    public static ItemStack hearthstone() {
        ItemStack is = new ItemStack(Material.QUARTZ);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Hearthstone");
        im.setLore(Arrays.asList(ChatColor.GRAY + "Teleports you to your home town.", ChatColor.GRAY + "Talk to an Innkeeper to change your home town.", ChatColor.GREEN + "Location: Dead Peaks"));
        is.setItemMeta(im);
        return is;
    }


    @EventHandler
    public void onCancelDamager(EntityDamageByEntityEvent e) {
        Player p;
        if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && casting.containsKey((p = (Player) e.getDamager()).getName())) {
            casting.remove(p.getName());
            castingloc.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Hearthstone - " + ChatColor.BOLD + "CANCELLED");
        }
    }

    @EventHandler
    public void onCancelDamage(EntityDamageEvent e) {
        Player p;
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (e.getEntity() instanceof Player && casting.containsKey((p = (Player) e.getEntity()).getName())) {
            casting.remove(p.getName());
            castingloc.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Hearthstone - " + ChatColor.BOLD + "CANCELLED");
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK || p.getInventory().getItemInMainHand() == null || !p.getInventory().getItemInMainHand().equals(Hearthstone.hearthstone()) || casting.containsKey(p.getName()) || Horses.mounting.containsKey(p.getName()) && !Duels.duelers.containsKey(p))) {
            if (Alignments.chaotic.containsKey(p.getName())) {
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " do this while chaotic!");
            }else if(Duels.duelers.containsKey(p)) {
                return;
            }else {
                p.sendMessage(ChatColor.BOLD + "TELEPORTING" + ChatColor.WHITE + " - " + ChatColor.AQUA + "Deadpeaks Mountain Camp" + ChatColor.WHITE + " ... " + 10 + "s");
                casting.put(p.getName(), 10);
                castingloc.put(p.getName(), p.getLocation());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (casting.containsKey(p.getName())) {
            casting.remove(p.getName());
            castingloc.remove(p.getName());
        }
    }

    @EventHandler
    public void onCancelMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (casting.containsKey(p.getName()) && (castingloc.get(p.getName())).distanceSquared(e.getTo()) >= 2.0) {
            casting.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Hearthstone - " + ChatColor.BOLD + "CANCELLED");
        }
    }

}

