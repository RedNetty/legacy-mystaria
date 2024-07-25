package me.retrorealms.practiceserver.mechanics.player;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.guilds.GuildMechanics;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.pvp.Deadman;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Toggles implements Listener {
    private static final Map<UUID, Set<String>> toggles = new HashMap<>();
    public static HashMap<Player, String> confirm = new HashMap<>();

    public void onEnable() {
        PracticeServer.log.info("[Toggles] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[Toggles] has been disabled.");
    }

    public static boolean isToggled(Player player, String toggle) {
        return toggles.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(toggle);
    }

    public static void setToggle(Player player, String toggle, boolean enabled) {
        Set<String> playerToggles = toggles.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        if (enabled) {
            playerToggles.add(toggle);
        } else {
            playerToggles.remove(toggle);
        }
    }

    public static void changeToggle(Player player, String toggle) {
        boolean newState = !isToggled(player, toggle);
        setToggle(player, toggle, newState);
        player.sendMessage((newState ? ChatColor.GREEN : ChatColor.RED) + ChatColor.BOLD.toString() +
                "Toggle " + toggle + " - " + (newState ? "Enabled!" : "Disabled!"));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNoDamageToggle(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) return;
        if (e.getDamage() <= 0.0) return;

        Player damager = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();

        if (Duels.duelers.containsKey(damager)) return;

        Set<String> damagerToggles = toggles.getOrDefault(damager.getUniqueId(), new HashSet<>());

        if (Buddies.getBuddies(damager.getName()).contains(victim.getName().toLowerCase()) && !damagerToggles.contains("Friendly Fire")) {
            e.setDamage(0.0);
            e.setCancelled(true);
        } else if (Parties.arePartyMembers(damager, victim) || GuildMechanics.getInstance().isInSameGuild(damager, victim)) {
            e.setDamage(0.0);
            e.setCancelled(true);
        } else if (Deadman.deadman && Deadman.stage < 3) {
            e.setDamage(0.0);
            e.setCancelled(true);
        } else if (damagerToggles.contains("Anti PVP")) {
            e.setDamage(0.0);
            e.setCancelled(true);
        } else if (!Alignments.neutral.containsKey(victim.getName()) && !Alignments.chaotic.containsKey(victim.getName()) && damagerToggles.contains("Chaotic")) {
            e.setDamage(0.0);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClickToggle(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (!e.getView().getTitle().equals("Toggle Menu")) return;

        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() == Material.INK_SACK && clickedItem.hasItemMeta()) {
            String toggleName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            changeToggle(player, toggleName);
            e.setCurrentItem(getToggleButton(toggleName, isToggled(player, toggleName)));
        }
    }

    public static Inventory getToggleMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "Toggle Menu");
        addToggleButton(inv, "Anti PVP", player);
        addToggleButton(inv, "Chaotic", player);
        addToggleButton(inv, "Friendly Fire", player);
        addToggleButton(inv, "Debug", player);
        addToggleButton(inv, "Hologram Damage", player);
        addToggleButton(inv, "Level HP", player);
        addToggleButton(inv, "Glow Drops", player);
        addToggleButton(inv, "Player Messages", player);
        addToggleButton(inv, "Trading", player);
        addToggleButton(inv, "Disable Kit", player);
        addToggleButton(inv, "Drop Protection", player);
        RankEnum rank = ModerationMechanics.getRank(player);
        if (rank == RankEnum.SUB2 || rank == RankEnum.SUB3 || rank == RankEnum.SUPPORTER ||
                rank == RankEnum.YOUTUBER || ModerationMechanics.isStaff(player)) {
            addToggleButton(inv, "Gems", player);
        }
        if (ModerationMechanics.isDonator(player)) {
            addToggleButton(inv, "Trail", player);
        }
        return inv;
    }

    private static void addToggleButton(Inventory inv, String toggleName, Player player) {
        inv.addItem(getToggleButton(toggleName, isToggled(player, toggleName)));
    }

    public static ItemStack getToggleButton(String toggleName, boolean enabled) {
        ItemStack item = new ItemStack(Material.INK_SACK, 1, enabled ? (short) 10 : (short) 8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((enabled ? ChatColor.GREEN : ChatColor.RED) + toggleName);
        meta.setLore(Collections.singletonList(ChatColor.GRAY + getToggleDescription(toggleName)));
        item.setItemMeta(meta);
        return item;
    }

    public static String getToggleDescription(String toggle) {
        switch (toggle.toLowerCase()) {
            case "trading": return "Toggles trading.";
            case "trail": return "Toggles Donator Trail";
            case "hologram damage": return "Toggles Holographic Damage";
            case "gems": return "Toggles gems going straight into bank";
            case "debug": return "Toggles displaying combat debug messages.";
            case "friendly fire": return "Toggles friendly-fire between buddies.";
            case "chaotic": return "Toggles killing blows on lawful players (anti-chaotic).";
            case "anti pvp": return "Toggles all outgoing PvP damage (anti-neutral).";
            case "level hp": return "Toggles whether you'd want to show your HP at your level bar or HP bar on top.";
            case "glow drops": return "Toggles whether you'd want to see glowing item drops when a monster dies or not.";
            case "player messages": return "Toggles whether you'd want to receive personal messages or not.";
            case "drop protection": return "Toggles whether items you can only be picked up by you for a few seconds.";
            case "disable kit": return "Toggle to disable receiving spawn items after death.";
            default: return "";
        }
    }

    public static Set<String> getToggles(UUID uuid) {
        return toggles.getOrDefault(uuid, new HashSet<>());
    }

    public static void loadTogglesFromSQL(UUID uuid, ResultSet rs) throws SQLException {
        Set<String> playerToggles = new HashSet<>();
        if (rs.getBoolean("LVLHPToggle")) playerToggles.add("Level HP");
        if (rs.getBoolean("PVPToggle")) playerToggles.add("Anti PVP");
        if (rs.getBoolean("ChaoToggle")) playerToggles.add("Chaotic");
        if (rs.getBoolean("FFToggle")) playerToggles.add("Friendly Fire");
        if (rs.getBoolean("DebugToggle")) playerToggles.add("Debug");
        if (rs.getBoolean("HologramToggle")) playerToggles.add("Hologram Damage");
        if (rs.getBoolean("GlowToggle")) playerToggles.add("Glow Drops");
        if (rs.getBoolean("PMToggle")) playerToggles.add("Player Messages");
        if (rs.getBoolean("TradingToggle")) playerToggles.add("Trading");
        if (rs.getBoolean("GemsToggle")) playerToggles.add("Gems");
        if (rs.getBoolean("TrailToggle")) playerToggles.add("Trail");
        if (rs.getBoolean("DropToggle")) playerToggles.add("Drop Protection");
        if (rs.getBoolean("KitToggle")) playerToggles.add("Disable Kit");

        toggles.put(uuid, playerToggles);
    }

    public static Set<String> getToggleColumns() {
        return new HashSet<>(Arrays.asList(
                "LVLHPToggle", "PVPToggle", "ChaoToggle", "FFToggle", "DebugToggle",
                "HologramToggle", "GlowToggle", "PMToggle", "TradingToggle", "GemsToggle",
                "TrailToggle", "DropToggle", "KitToggle"
        ));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!toggles.containsKey(player.getUniqueId())) {
            toggles.put(player.getUniqueId(), new HashSet<>());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SQLMain.updatePersistentStats(player);
    }
}