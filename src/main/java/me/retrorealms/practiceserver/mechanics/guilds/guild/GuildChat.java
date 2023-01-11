package me.retrorealms.practiceserver.mechanics.guilds.guild;

import fr.rhaz.sockets.socket4mc.Socket4Bukkit;
import fr.rhaz.sockets.client.SocketClient;
import fr.rhaz.sockets.utils.JSONMap;
import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class GuildChat implements Listener {
    private BukkitTask register;

    public SocketClient getClient(){
        return Socket4Bukkit.getClient();
    }
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);

        PracticeServer.log.info("[Guild Chat] has been enabled");
    }

    @EventHandler
    public void onHandshake(Socket4Bukkit.Client.ClientSocketHandshakeEvent e){
        register = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(PracticeServer.plugin, new Runnable() {
            @Override
            public void run() {
                getClient().write("GChat", "register");
            }
        }, 0L, 1 * 20L);
    }

    public static void sendCrossShardMessage(Player p, Guild guild, String message) {

        JSONMap map = new JSONMap(
                "data", "guildmessage",
                "message", message,
                "server", Socket4Bukkit.getClient().getName().toUpperCase(),
                "player", p.getName(),
                "guild", guild.getName() // This is a test
        );
        Socket4Bukkit.getClient().write("GChat", map);

    }
    @EventHandler
    public static void onRecieveMessage(Socket4Bukkit.Client.ClientSocketJSONEvent e){
        Guild guild = GuildManager.getInstance().getIgnoreCase(e.getExtraString("guild"));
        if(Bukkit.getPlayer(e.getExtraString("player")) != null) return;
        guild.sendMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD
                + guild.getTag() + ChatColor.DARK_AQUA + "> "
                + e.getExtraString("player") + " ("
                + e.getExtraString("server") + ")" + ChatColor.GRAY +  ": "
                + e.getExtraString("message"));
    }
}
