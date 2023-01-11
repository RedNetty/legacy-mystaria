package me.retrorealms.practiceserver.mechanics.drops.buff;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

import java.util.UUID;

/**
 * Created by Giovanni on 8-7-2017.
 */
public class LootBuff {

    private String ownerName;
    private UUID ownerId;

    private int update;

    private int second = 0;

    public LootBuff(String owner, UUID ownerId, int update) {
        this.ownerName = owner;
        this.ownerId = ownerId;
        this.update = update;
    }

    public int getUpdate() {
        return update;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void activate(UUID ownerId) {

        String fixedName = Bukkit.getPlayer(ownerId).getName();

        Bukkit.getOnlinePlayers().forEach(player -> {

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 0.2F);
            player.sendMessage("");
            player.sendMessage("");
            StringUtil.sendCenteredMessage(player, "&6&lLOOT BUFF");
            StringUtil.sendCenteredMessage(player, "&b&o" + fixedName + "&b has started a loot buff!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d        - Drop rates improved by &l" + update + "&d%"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d        - Elite drop rates improved by &l" + ((int) update / 2) + "&d%"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d        - Elite scroll drop rates improved by &l" + ((int) update / 2) + "&d%"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d        - Time after expiry: &n30 minutes"));
        });

    }

    public void end() {
        int improvedDrops = PracticeServer.buffHandler().getImprovedDrops();

        //int random = this.calculateByRetention(improvedDrops);

        Bukkit.getOnlinePlayers().forEach(player -> {

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 0.2F);

            player.sendMessage("");
            player.sendMessage("");
            StringUtil.sendCenteredMessage(player, "&c&lLOOT BUFF EXPIRED");
            StringUtil.sendCenteredMessage(player, "&b&o" + this.ownerName + "'s loot buff has expired!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d        - Over &n" + improvedDrops + "&d drops were made by this buff!"));
            //player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d        - Thats +&n" + random + "&d% overall in the last 60 minutes."));
        });
    }

    public void update() {
        this.second++;
    }

    public boolean expired() {
        return second >= 3200;
    } //3200

    public int getTimeLeft() { return 3200 - second; }

}
