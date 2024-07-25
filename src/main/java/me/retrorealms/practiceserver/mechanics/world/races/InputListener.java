package me.retrorealms.practiceserver.mechanics.world.races;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class InputListener implements Listener {
    private Player player;
    private String setting;
    private RaceSettingsMenu menu;

    public InputListener(Player player, String setting, RaceSettingsMenu menu) {
        this.player = player;
        this.setting = setting;
        this.menu = menu;
        PracticeServer.getInstance().getServer().getPluginManager().registerEvents(this, PracticeServer.getInstance());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer() == player) {
            event.setCancelled(true);
            try {
                double value = Double.parseDouble(event.getMessage());
                RaceSettingsMenu.updateSetting(setting, value);
                player.sendMessage(ChatColor.GREEN + setting + " has been updated to " + value);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid input. Please enter a number.");
            }
            HandlerList.unregisterAll(this);
        }
    }
}
