/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.InventoryView
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 */
package me.retrorealms.practiceserver.mechanics.player;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.guilds.GuildMechanics;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.pvp.Deadman;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Toggles implements Listener {
    public static ConcurrentHashMap<UUID, ArrayList<String>> toggles = new ConcurrentHashMap<>();

    public static boolean hasTrading(Player player) {
        ArrayList<String> toggles = Toggles.getToggles(player.getUniqueId());

        return toggles.contains("Trading");
    }

    public static void enableTrades(Player player) {
        toggles.get(player.getName()).add("Trading");
    }

    public static boolean hasLevelBarHP(Player player) {
        ArrayList<String> toggles = Toggles.getToggles(player.getUniqueId());

        return toggles.contains("Level HP");
    }

    public static boolean hasGlowOnDrops(Player player) {
        ArrayList<String> toggles = Toggles.getToggles(player.getUniqueId());

        return toggles.contains("Glow Drops");
    }

    public static boolean hasPMEnabled(Player player) {
        ArrayList<String> toggles = Toggles.getToggles(player.getUniqueId());

        return toggles.contains("Player Messages");
    }

    public static void enablePM(Player player) {
        ArrayList<String> toggles1 = Toggles.getToggles(player.getUniqueId());

        toggles1.add("Player Messages");

        toggles.remove(player.getName());

        toggles.put(player.getUniqueId(), toggles1);
    }


    public void onEnable() {
        PracticeServer.log.info("[Toggles] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        if(PracticeServer.DATABASE) return;
        File file = new File(PracticeServer.plugin.getDataFolder(), "toggles.yml");
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
            ArrayList<String> toggle = new ArrayList<String>();
            toggle.addAll(config.getStringList(p));
            toggles.put(UUID.fromString(p), toggle);
        }
    }

    public void onDisable() {
        PracticeServer.log.info("[Toggles] has been disabled.");
        if(!PracticeServer.DATABASE){
            File file = new File(PracticeServer.plugin.getDataFolder(), "toggles.yml");
            YamlConfiguration config = new YamlConfiguration();
            for (UUID uuid : toggles.keySet()) {
                config.set(String.valueOf(uuid) , toggles.get(uuid));
            }
            try {
                config.save(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean getToggleStatus(Player player, String toggle) {
        if(getToggles(player.getUniqueId()).contains(toggle)) {
            return true;
        }else{
            return false;
        }

    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onNoDamageToggle(EntityDamageByEntityEvent e) {
        ArrayList<String> gettoggles = Toggles.getToggles(e.getDamager().getUniqueId());
        ArrayList<String> buddies = Buddies.getBuddies(e.getDamager().getName());
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            Player pp = (Player) e.getEntity();
            if(Duels.duelers.containsKey(p)) {
                return;
            }
            if (e.getDamage() <= 0.0) {
                return;
            }
            if (buddies.contains(pp.getName().toLowerCase()) && !gettoggles.contains("Friendly Fire")) {
                e.setDamage(0.0);
                e.setCancelled(true);
                return;
            }
            if (Parties.arePartyMembers(p, pp)) {
                e.setDamage(0.0);
                e.setCancelled(true);
                return;
            }
            if (GuildMechanics.getInstance().isInSameGuild(p, pp)) {
                e.setDamage(0.0);
                e.setCancelled(true);
                return;
            }
            if(Deadman.deadman && e.getDamager() instanceof Player && e.getEntity() instanceof Player && Deadman.stage < 3){
                e.setDamage(0.0);
                e.setCancelled(true);
                return;
            }
            if (gettoggles.contains("Anti PVP")) {
                e.setDamage(0.0);
                e.setCancelled(true);
                return;
            }
            if (!Alignments.neutral.containsKey(pp.getName()) && !Alignments.chaotic.containsKey(pp.getName()) && gettoggles.contains("Chaotic")) {
                e.setDamage(0.0);
                e.setCancelled(true);
                return;
            }
        }
    }


    @EventHandler
    public void onClickToggle(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (p.getOpenInventory().getTopInventory().getName().equals("Toggle Menu")) {
            e.setCancelled(true);
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && e.getCurrentItem().getType() == Material.INK_SACK && e.getCurrentItem().getItemMeta().hasDisplayName()) {
                    String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                    changeToggle(p, name);
                    boolean on = e.getCurrentItem().getItemMeta().getDisplayName().contains(ChatColor.RED.toString());
                    e.setCurrentItem(Toggles.getToggleButton(name, on));
            }
        }
    }

    public static void changeToggle(Player p, String name) {
        ArrayList<String> playerToggles = toggles.get(p.getUniqueId());
        if(playerToggles.contains(name)) {
            playerToggles.remove(name);
        }else {
            playerToggles.add(name);
        }
        if(getToggleStatus(p, name)) {
            p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "Toggle " + name + " - Enabled!");
        }else{
            p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Toggle " + name + " - Disabled!");
        }
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
        toggles.remove(p.getUniqueId());
        toggles.put(p.getUniqueId(), playerToggles);
    }

    public static ArrayList<String> getToggles(UUID s) {
        if (toggles.containsKey(s)) {
            return toggles.get(s);
        }
        return new ArrayList<>();
    }

    public static Inventory getToggleMenu(Player p) {
        Inventory inv = Bukkit.getServer().createInventory(null, 18, "Toggle Menu");
        inv.addItem(Toggles.getToggleButton("Anti PVP", getToggleStatus(p, "Anti PVP")));
        inv.addItem(Toggles.getToggleButton("Chaotic", getToggleStatus(p, "Chaotic")));
        inv.addItem(Toggles.getToggleButton("Friendly Fire", getToggleStatus(p, "Friendly Fire")));
        inv.addItem(Toggles.getToggleButton("Debug", getToggleStatus(p, "Debug")));
        inv.addItem(Toggles.getToggleButton("Hologram Damage", getToggleStatus(p, "Hologram Damage")));
        inv.addItem(Toggles.getToggleButton("Level HP", getToggleStatus(p, "Level HP")));
        inv.addItem(Toggles.getToggleButton("Glow Drops", getToggleStatus(p, "Glow Drops")));
        inv.addItem(Toggles.getToggleButton("Player Messages", getToggleStatus(p, "Player Messages")));
        inv.addItem(Toggles.getToggleButton("Trading", getToggleStatus(p, "Trading")));
        inv.addItem(Toggles.getToggleButton("Disable Kit", getToggleStatus(p, "Disable Kit")));
        if (ModerationMechanics.getRank(p) == RankEnum.SUB2 || ModerationMechanics.getRank(p) == RankEnum.SUB3 || ModerationMechanics.getRank(p) == RankEnum.SUPPORTER || ModerationMechanics.getRank(p) == RankEnum.YOUTUBER || ModerationMechanics.getRank(p) == RankEnum.SUB3 || ModerationMechanics.isStaff(p)) {
            inv.addItem(Toggles.getToggleButton("Gems", getToggleStatus(p, "Gems")));
        }
        if(ModerationMechanics.isDonator(p)) {
            inv.addItem(Toggles.getToggleButton("Trail", getToggleStatus(p, "Trail")));
        }if(ModerationMechanics.isDonator(p) || ModerationMechanics.isStaff(p)) {
            inv.addItem(Toggles.getToggleButton("Drop Protection", getToggleStatus(p, "Drop Protection")));
        }
        return inv;
    }

    public static ItemStack getToggleButton(String s, boolean on) {
        ItemStack is = new ItemStack(Material.INK_SACK);
        ItemMeta im = is.getItemMeta();
        ChatColor cc = null;
        if (on) {
            is.setDurability((short) 10);
            cc = ChatColor.GREEN;
        } else {
            is.setDurability((short) 8);
            cc = ChatColor.RED;
        }
        im.setDisplayName(cc + s);
        im.setLore(Arrays.asList(Toggles.getToggleDescription(s)));
        is.setItemMeta(im);
        return is;
    }

    public static String getToggleDescription(String toggle) {
        String desc = ChatColor.GRAY.toString();
        if (toggle.equalsIgnoreCase("Trading")) {
            desc = desc + "Toggles trading.";
        }
        if(toggle.equalsIgnoreCase("Trail")) {
            desc = desc + "Toggles Donator Trail";
        }
        if(toggles.equals("Hologram Damage")) {
            desc = desc + "Toggles Holographic Damage";
        }
        if (toggle.equalsIgnoreCase("Gems")) {
            desc = desc + "Toggles gems going straight into bank";
        }
        if (toggle.equalsIgnoreCase("Debug")) {
            desc = String.valueOf(desc) + "Toggles displaying combat debug messages.";
        }
        if (toggle.equalsIgnoreCase("Friendly Fire")) {
            desc = String.valueOf(desc) + "Toggles friendly-fire between buddies.";
        }
        if (toggle.equalsIgnoreCase("Chaotic")) {
            desc = String.valueOf(desc) + "Toggles killing blows on lawful players (anti-chaotic).";
        }
        if (toggle.equalsIgnoreCase("Anti PVP")) {
            desc = String.valueOf(desc) + "Toggles all outgoing PvP damage (anti-neutral).";
        }
        if (toggle.equalsIgnoreCase("Level HP")) {
            desc = desc + "Toggles whether you'd want to show your HP at your level bar or HP bar on top.";
        }
        if (toggle.equalsIgnoreCase("Glow Drops")) {
            desc = desc + "Toggles whether you'd want to see glowing item drops when a monster dies or not.";
        }
        if (toggle.equalsIgnoreCase("Player Messages")) {
            desc = desc + "Toggles whether you'd want to receive personal messages or not.";
        }
        if (toggle.equalsIgnoreCase("Drop Protection")) {
            desc = desc + "Toggles whether items you can only be picked up by you for a few seconds.";
        }
        if (toggle.equalsIgnoreCase("Disable Kit")) {
            desc = desc + "Toggle to disable recieving spawn items after death.";
        }
        return desc;
    }
}

