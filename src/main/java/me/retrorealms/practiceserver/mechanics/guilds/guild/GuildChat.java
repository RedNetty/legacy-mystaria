package me.retrorealms.practiceserver.mechanics.guilds.guild;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class GuildChat implements Listener {
    private BukkitTask register;

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);

        PracticeServer.log.info("[Guild Chat] has been enabled");
    }
}
