package me.retrorealms.practiceserver.mechanics.drops;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;


// Author: Red
public class DropPriority implements Listener {

    static ConcurrentHashMap<LivingEntity, ConcurrentHashMap<Player, Integer>> mobs = new ConcurrentHashMap();
    static ConcurrentHashMap<Item,Integer> droppedTime = new ConcurrentHashMap();
    static ConcurrentHashMap<Item , Player> playerRegistry = new ConcurrentHashMap();

    public void onEnable() {
        PracticeServer.log.info("[Drop Priority] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);

        new BukkitRunnable() {

            public void run() {
                for(Item i : droppedTime.keySet()){
                    if(droppedTime.get(i) > 0){
                        droppedTime.put(i, droppedTime.get(i)-1);
                    }else{
                        droppedTime.remove(i);
                        playerRegistry.remove(i);
                    }
                }
            }
        }.runTaskTimerAsynchronously(PracticeServer.plugin, 0, 20);
    }


    public void onDisable() {
        PracticeServer.log.info("[DropPriority] has been disabled.");
    }

    public static Item DropItem(Entity killer, LivingEntity mob, Location location, ItemStack is){
        Item i = mob.getWorld().dropItemNaturally(location, is);
        if(mobs.containsKey(mob)){
            Player current = null;
            for(Player p : mobs.get(mob).keySet()){
                if(current == null || mobs.get(mob).get(current) < mobs.get(mob).get(p)){
                    current = p;
                }
            }
            if(current != null){
                playerRegistry.put(i, current);
                droppedTime.put(i, rankTime(current));
            }

        }else{
            if(killer instanceof Player) {
                playerRegistry.put(i, (Player) killer);
                droppedTime.put(i, rankTime((Player) killer));
            }
        }

        if(killer instanceof Player && is.getType() != Material.EMERALD)
        Mobdrops.dropShowString((Player) killer, is, mob);
        return i;
    }

    @EventHandler
    void onPickup(PlayerPickupItemEvent e){
        Player p = e.getPlayer();
        Item i = e.getItem();
        if(droppedTime.containsKey(i)){
            if(playerRegistry.get(i) == p) {
                droppedTime.remove(i);
                playerRegistry.remove(i);
            }else{
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onDamage(EntityDamageByEntityEvent e){
        if(!(e.getEntity() instanceof LivingEntity)) return;
        if(e.getEntity() instanceof Player) return;
        if(!(e.getDamager() instanceof Player)) return;
        LivingEntity mob = (LivingEntity) e.getEntity();
        Player damager = (Player) e.getDamager();
        if(!mobs.containsKey(e.getEntity())){
            mobs.put(mob, new ConcurrentHashMap());
        }else {
            ConcurrentHashMap<Player, Integer> damages = mobs.get(mob);
            if (damages.containsKey(damager)) {
                damages.put(damager, damages.get(damager) + (int) e.getDamage());
            } else {
                damages.put(damager, (int) e.getDamage());
            }
        }
    }

    @EventHandler
    void onDrop(PlayerDropItemEvent e){
        if(Toggles.getToggleStatus(e.getPlayer(), "Drop Protection")){
            droppedTime.put(e.getItemDrop(), rankTime(e.getPlayer()));
            playerRegistry.put(e.getItemDrop(), e.getPlayer());
        }
    }

    static int rankTime(Player p){
        RankEnum rank = ModerationMechanics.getRank(p);
        switch(rank){
            case DEFAULT:
                return 2;
            case SUB:
                return 5;
            case SUB1:
                return 8;
            case SUB2:
                return 15;
        }
        return 30;
    }
}
