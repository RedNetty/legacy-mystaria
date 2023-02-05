package me.retrorealms.practiceserver.mechanics.shard;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.nbt.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import me.retrorealms.practiceserver.utils.ServerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.sql.ResultSet;

/**
 * Created by Jaxon on 8/20/2017.
 */
public class Shard implements Listener {

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, PracticeServer.getInstance());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getTitle().equalsIgnoreCase("Mystaria - SERVERS")) {
            Player p = Bukkit.getPlayer(event.getWhoClicked().getName());
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == null || event.getCurrentItem().getType() == Material.AIR || !event.getCurrentItem().getItemMeta().hasDisplayName())
                return;
            NBTAccessor nbtAccessor = new NBTAccessor(event.getCurrentItem());
            SQLMain.updatePlayerStats(p);
            SQLMain.updatePersistentStats(p);
            ResultSet sync = SQLMain.getPlayerData("PlayerData", "Username"); //just makes sure the querys happen i think?
            if (!Alignments.isSafeZone(p.getLocation()) && Alignments.tagged.containsKey(p.getName())
                    && System.currentTimeMillis() - Alignments.tagged.get(p.getName()) < 10000) {
                p.sendMessage(ChatColor.RED + "You cannot change servers while in combat!");
                return;
            }if(Duels.duelers.containsKey(p)) {
                p.sendMessage(ChatColor.RED + "You cannot change servers while in a duel!");
                return;
            }
            ServerUtil.sendToServer(event.getWhoClicked().getName(), nbtAccessor.getString("server"));
            event.getWhoClicked().closeInventory();
        }
    }
}
