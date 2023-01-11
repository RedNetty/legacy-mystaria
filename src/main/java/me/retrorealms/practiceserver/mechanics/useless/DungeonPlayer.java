package me.retrorealms.practiceserver.mechanics.useless;

import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Giovanni on 2-5-2017.
 */
public class DungeonPlayer {

    private Player player;

    public DungeonPlayer(Player player) {
        this.player = player;
    }

    public void sendMessage(String message, boolean centered) {
        if (centered)
            StringUtil.sendCenteredMessage(player, ChatColor.translateAlternateColorCodes('&', message));
        else player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void blind(int par2) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * par2, 3));
    }


    public void freeze(int par2) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * par2, 255));
    }

    public void playSound(Sound par1) {
        player.playSound(player.getLocation(), par1, 63f, 1f);
    }

    public void playSound(Sound par1, float volume, float pitch) {
        player.playSound(player.getLocation(), par1, volume, pitch);
    }

    public Player getPlayer() {
        return player;
    }
}
