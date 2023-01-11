package me.retrorealms.practiceserver.mechanics.duels;

import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.useless.task.AsyncTask;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Author: Red
 * January 2018
 */
public class DuelPlayer {

    public DuelPlayer(Player p, int arena, int team){
        inv = p.getInventory().getContents();
        armor = p.getInventory().getArmorContents();
        loc = p.getLocation();
        this.arena = arena;
        name = p.getName();
        player = p;
        this.team = team;


    }
    public ItemStack[] inv;
    public ItemStack[] armor;
    public Location loc;
    public Integer arena;
    public String name;
    public Player player;
    public int team;

    public void exitDuel(boolean ended, boolean logout){
        Duels.stayLawful.put(player, 2);
        new AsyncTask(() -> {
            Duels.stayLawful.remove(player);
        }).setDelay(2).scheduleDelayedTask();
        this.player.setHealth(player.getMaxHealth());
        if(!logout){
            PlayerInventory pinv = player.getInventory();
            pinv.setContents(inv);
            pinv.setArmorContents(armor);
            for(ItemStack i : pinv.getContents()){
                if(i != null && i.getAmount() < 1){
                    i.setAmount(1);
                }
            }
            for(ItemStack i : pinv.getArmorContents()){
                if(i != null && i.getAmount() < 1){
                    i.setAmount(1);
                }
            }
            player.setItemOnCursor(null);
            player.updateInventory();

        }
        //for(int i=0; i<40;i++){
        //    pinv.setItem(i, inv.getItem(i));
        //}
        if(!ended){
            DuelInstance playersDuel = Duels.duels.get(arena);
            for(Player p: Duels.duels.get(arena).team1){
                p.sendMessage(ChatColor.RED + player.getName() + " has been ELIMINATED from the duel.");
            }
            for(Player p: Duels.duels.get(arena).team2){
                p.sendMessage(ChatColor.RED + player.getName() + " has been ELIMINATED from the duel.");
            }
            playersDuel.removePlayerFromDuel(player);
            playersDuel.checkRemaining();
        }
        Alignments.chaotic.remove(player.getName());
        Alignments.neutral.remove(player.getName());
        Alignments.updatePlayerAlignment(player);
        Listeners.combat.remove(player.getName());
        new AsyncTask(() -> {
            Duels.duelers.remove(player);
        }).setDelay(1).scheduleDelayedTask();
        this.player.teleport(loc);
    }
}
