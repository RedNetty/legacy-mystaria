package me.retrorealms.practiceserver.mechanics.useless.api;

import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 19-8-2016.
 */
public class Actionbar {

    private Player player;
    private String message;

    public Actionbar setPlayer(Player player) {
        this.player = player;

        return this;
    }

    public Actionbar setMessage(String message) {
        this.message = message;

        return this;
    }

    /**
     * A method to craft and send the actionbar to a player.
     */
    public void send() {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat
                (IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', this.message) + "\"}"), (byte) 2));
    }
}
