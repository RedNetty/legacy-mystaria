package me.retrorealms.practiceserver.mechanics.guilds.guild;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GuildBank implements Listener {
    public static HashMap<Guild, Player> banksee = new HashMap<>();
    public static ArrayList<Player> cooldownList = new ArrayList<>();
    public static int guildBankSize = 54;

    public static Inventory getBank(Player p, Guild guild) {
        if(PracticeServer.DATABASE){
            return SQLMain.getGuildBank(p, guild);
        }else {
            File file;
            String name = guild.getName();
            if (!(file = new File(PracticeServer.plugin.getDataFolder() + "/guild-banks", String.valueOf(name) + ".yml")).exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.load(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Inventory inv = Bukkit.createInventory(null, guildBankSize, "Guild Bank Chest (1/1)");
            int i = 0;
            while (i < inv.getSize()) {
                if (config.contains("" + i)) {
                    inv.setItem(i, config.getItemStack("" + i));
                }
                ++i;
            }
            return inv;
        }
    }

    public void onEnable() {
        PracticeServer.log.info("[GuildBanks] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        if(PracticeServer.DATABASE) return;
        File file = new File(PracticeServer.plugin.getDataFolder(), "guild-banks");
        if (!file.exists()) {
            file.mkdirs();
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Guild guild : banksee.keySet()) {
                    Player player = banksee.get(guild);
                    if (!player.getOpenInventory().getTitle().equalsIgnoreCase("Guild Bank Chest (1/1)")) {
                        banksee.remove(guild);
                        cooldownList.remove(player);
                    }
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 100L, 100L);
    }

    public void onDisable() {
        PracticeServer.log.info("[Banks] has been disabled.");
        if(PracticeServer.DATABASE) return;
        File file = new File(PracticeServer.plugin.getDataFolder(), "guild-banks");
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.BEACON && !Duels.duelers.containsKey(p)) {
            e.setCancelled(true);
            if (!e.getPlayer().getOpenInventory().getTitle().equalsIgnoreCase("Guild Bank Chest (1/1)")) {
                if (!GuildPlayers.getInstance().get(p.getUniqueId()).isInGuild()) {
                    p.sendMessage(ChatColor.RED + "You are currently not in a guild so you cannot use the guild bank system!");
                    return;
                }
                Guild guild = GuildManager.getInstance().get(GuildPlayers.getInstance().get(p.getUniqueId()).getGuildName());
                Inventory inv = this.getBank(e.getPlayer(), guild);
                if(PracticeServer.DATABASE){
                    if(inv == null) return;
                }else{
                    if (banksee.containsKey(guild)) {
                        p.sendMessage(ChatColor.RED + "Your guild bank is currently in use by, " + banksee.get(guild).getName());
                        return;
                    }
                    if (inv == null) {
                        inv = Bukkit.createInventory(null, guildBankSize, "Guild Bank Chest (1/1)");
                    }
                    banksee.put(guild, p);
                }
                p.openInventory(inv);
                if (inv.contains(Material.SADDLE)) {
                    inv.remove(Material.SADDLE);
                }
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1.0f, 1.0f);
            }
            cooldownList.add(p);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (cooldownList.contains(p))
                        cooldownList.remove(p);
                }
            }.runTaskLaterAsynchronously(PracticeServer.getInstance(), 20L);


        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (e.getInventory().getName().equals("Guild Bank Chest (1/1)")) {
            Guild guild = GuildManager.getInstance().get(GuildPlayers.getInstance().get(p.getUniqueId()).getGuildName());
            this.saveGuildBank(e.getInventory(), guild);
//            new BukkitRunnable() {
//
//                public void run() {
//                    saveGuildBank(e.getInventory(), guild);
//                    if (banksee.containsKey(guild)) {
//                        banksee.remove(guild);
//                    }
//                }
//            }.runTaskLater(PracticeServer.plugin, 10L);
        }
    }

//    @EventHandler
//    public void onClickSave(InventoryClickEvent e) {
//        Player p = (Player) e.getWhoClicked();
//        if (e.getInventory().getName().equals("Guild Bank Chest (1/1)")) {
//            Guild guild = GuildManager.getInstance().get(GuildPlayers.getInstance().get(p.getUniqueId()).getGuildName());
//            this.saveGuildBank(e.getInventory(), guild);
//            new BukkitRunnable() {
//                public void run() {
//                    saveGuildBank(e.getInventory(), guild);
//                }
//            }.runTaskLater(PracticeServer.plugin, 1L);
//        }
//    }

    public void saveGuildBank(Inventory inv, Guild guild) {
        if(PracticeServer.DATABASE){
            SQLMain.saveGuildBank(inv, guild);
            return;
        }
        String name = guild.getName();
        File file = new File(PracticeServer.plugin.getDataFolder() + "/guild-banks", String.valueOf(name) + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        int i = 0;
        while (i < inv.getSize()) {
            if (inv.getItem(i) != null) {
                config.set("" + i, inv.getItem(i));
            }
            ++i;
        }
        try {
            config.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}