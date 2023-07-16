package me.retrorealms.practiceserver.mechanics.money.Economy;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.player.GamePlayer.nonStaticConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class Economy implements Listener {

    public static HashMap<UUID, Integer> currentBalance = new HashMap<>();

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        PracticeServer.log.info("[Practice Server] Economy has been enabled");
        serverStart();
    }

    public void onDisable() {
        PracticeServer.log.info("[Practice Server] Economy has been disabled");
        saveAllBalance();
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        if(PracticeServer.DATABASE) return;
        if (currentBalance.containsKey(event.getPlayer().getUniqueId())) return;
        int balance = nonStaticConfig.get().getInt(event.getPlayer().getUniqueId() + ".Economy.Money Balance");
        currentBalance.put(event.getPlayer().getUniqueId(), balance);
    }

    public static void clearEconomy() {
        currentBalance = new HashMap<>();
    }

    public void saveAllBalance() {
        if(PracticeServer.DATABASE) return;
        for (UUID id : currentBalance.keySet()) {
            int bal = currentBalance.get(id);
            nonStaticConfig.get().set(id.toString() + ".Economy.Money Balance", bal);
            nonStaticConfig.save();
        }
    }

    public void serverStart() {
        if(PracticeServer.DATABASE) return;
        for (String key : nonStaticConfig.get().getKeys(false)) {
            UUID id = UUID.fromString(key);
            int val = nonStaticConfig.get().getInt(id + ".Economy.Money Balance");
            currentBalance.put(id, val);
        }

    }

    public static int getBalance(UUID id) {
        if (currentBalance.containsKey(id)) {
            int balance = currentBalance.get(id);
            return balance;
        } else {
            return 0;
        }
    }

    public static void depositPlayer(UUID id, int amount) {
        if (currentBalance.containsKey(id)) {
            int cB = currentBalance.get(id);
            int nB = cB + amount;
            currentBalance.put(id, nB);
        } else {
            currentBalance.put(id, amount);
        }
    }

    public static void withdrawPlayer(UUID id, int amount) {
        if (currentBalance.containsKey(id)) {
            int cB = currentBalance.get(id);
            int nB = cB - amount;
            currentBalance.remove(id);
            currentBalance.put(id, nB);
        }
    }
}