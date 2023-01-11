package me.retrorealms.practiceserver.utils;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 10-6-2017.
 */
public class ServerUtil {

    public static void sendToServer(String playerName, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(playerName);
        out.writeUTF(serverName);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (player != null) {
            player.sendPluginMessage(PracticeServer.getInstance(), "BungeeCord", out.toByteArray());
        }
    }
}
