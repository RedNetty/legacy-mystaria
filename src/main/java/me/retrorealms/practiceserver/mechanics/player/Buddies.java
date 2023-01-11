package me.retrorealms.practiceserver.mechanics.player;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Buddies implements Listener {
    public static ConcurrentHashMap<String, ArrayList<String>> buddies = new ConcurrentHashMap<String, ArrayList<String>>();

    public void onEnable() {
        PracticeServer.log.info("[Buddies] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        if(PracticeServer.DATABASE) return;
        File file = new File(PracticeServer.plugin.getDataFolder(), "buddies.yml");
        YamlConfiguration config = new YamlConfiguration();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            config.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String p : config.getKeys(false)) {
            ArrayList<String> buddy = new ArrayList<String>();
            for (String t : config.getStringList(p)) {
                buddy.add(t);
            }
            buddies.put(p, buddy);
        }
    }

    public void onDisable() {
        PracticeServer.log.info("[Buddies] has been disabled.");
        if(PracticeServer.DATABASE) return;
        File file = new File(PracticeServer.plugin.getDataFolder(), "buddies.yml");
        YamlConfiguration config = new YamlConfiguration();
        for (String s : buddies.keySet()) {
            config.set(s, buddies.get(s));
        }
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static ArrayList<String> getBuddies(String s) {
        if (buddies.containsKey(s)) {
            return buddies.get(s);
        }
        return new ArrayList<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.isOp()) {
            return;
        }
        for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
            if (!buddies.containsKey(pl.getName()) || !(Buddies.getBuddies(pl.getName())).contains(p.getName().toLowerCase()))
                continue;
            pl.sendMessage(ChatColor.YELLOW + p.getName() + " has joined this server.");
            pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 1.2f);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (p.isOp()) {
            return;
        }
        for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
            if (!buddies.containsKey(pl.getName()) || !(Buddies.getBuddies(pl.getName())).contains(p.getName().toLowerCase()))
                continue;
            pl.sendMessage(ChatColor.YELLOW + p.getName() + " has logged out.");
            pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 0.5f);
        }
    }
}

