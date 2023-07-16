package me.retrorealms.practiceserver.mechanics.player;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.misc.LeaderboardCommand;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import net.minecraft.server.v1_12_R1.EntityShulker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentPlayers implements Listener {
    public static ConcurrentHashMap<UUID, PersistentPlayer> persistentPlayers = new ConcurrentHashMap<>();
    public static HashMap<Player, String> confirm = new HashMap<>();

    public static PersistentPlayer get(UUID uuid) {
        return persistentPlayers.get(uuid);
    }

    public static void put(UUID uuid, PersistentPlayer pp) {
        persistentPlayers.put(uuid, pp);
    }

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    @EventHandler
    void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (!persistentPlayers.containsKey(uuid)) PersistentPlayers.put(uuid, new PersistentPlayer(50, 0, 0, 0, 0, 1,
                0, 0, 0, 1, 0, 0, 0, 0));
    }


    @EventHandler
    public void onEquip(PlayerInteractEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (!persistentPlayers.containsKey(uuid)) PersistentPlayers.put(uuid, new PersistentPlayer(50, 0, 0, 0, 0, 1,
                0, 0, 0, 1, 0, 0, 0, 0));
    }

    public static void assignTokens(boolean test) {
        Map<UUID, Integer> tokens = new HashMap<>();
        try {
            // Retrieve the necessary columns from PlayerData table
            ResultSet rs = SQLMain.con.createStatement().executeQuery(
                    "SELECT UUID, T1Kills, T2Kills, T3Kills, T4Kills, T5Kills, T6Kills, PlayerKills, Gems, Deaths, MaxHP, OreMined " +
                            "FROM PlayerData");
            while (rs.next()) {
                int newTokens = 0;
                int mobKills = rs.getInt("T1Kills") + rs.getInt("T2Kills") + rs.getInt("T3Kills")
                        + rs.getInt("T4Kills") + rs.getInt("T5Kills") + rs.getInt("T6Kills");
                int deaths = rs.getInt("Deaths");
                int playerKills = rs.getInt("PlayerKills");
                int maxHP = rs.getInt("MaxHP");
                int oreMined = rs.getInt("OreMined");

                if (playerKills > deaths) {
                    newTokens += 5;
                }

                if (mobKills > 200 && deaths < 5) {
                    newTokens += 5;
                }

                newTokens += mobKills / 200;
                newTokens += maxHP / 2000;
                newTokens += oreMined / 64;

                tokens.put(UUID.fromString(rs.getString("UUID")), newTokens);
            }

            // Retrieve leaderboard data for each category and add tokens based on ranking
            String[] leaderboardCategories = { "T1Kills", "T2Kills", "T3Kills", "T4Kills", "T5Kills", "PlayerKills", "OreMined", "MaxHP", "Gems" };
            for (String category : leaderboardCategories) {
                int i = 0;
                rs = LeaderboardCommand.getPlayerData("PlayerData", "UUID, " + category, category);
                while (true) {
                    assert rs != null;
                    if (!(rs.next() && i < 10)) break;
                    UUID uuid = UUID.fromString(rs.getString("UUID"));
                    tokens.put(uuid, tokens.get(uuid) + tokensBasedOnLb(i));
                    i++;
                }
            }

            // Apply rank multiplier to tokens
            rs = SQLMain.con.createStatement().executeQuery(
                    "SELECT UUID, playerrank FROM PersistentData NATURAL JOIN PlayerData WHERE persistentdata.playerrank != 'default'");
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("UUID"));
                tokens.put(uuid, Math.round(tokens.get(uuid) * getRankMultiplier(rs.getString("PlayerRank"))));
            }

            if (test) {
                tokens.replaceAll((u, v) -> 0);
            }

            // Update tokens for each player and save changes to the database
            for (UUID uuid : tokens.keySet()) {
                try {
                    PersistentPlayer pp = persistentPlayers.get(uuid);
                    if (pp != null) {
                        pp.tokens += tokens.get(uuid);
                        OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);
                        System.out.println("Awarded " + tokens.get(uuid) + " Tokens to " + pl.getName());
                        if (pl.isOnline())
                            Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN + "You received " + tokens.get(uuid) + " tokens this wipe!");
                    } else {
                        OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);
                        System.out.println("Error on " + pl.getName());
                    }
                } catch (Exception e) {
                    OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);
                    System.out.println("Error on " + pl.getName());
                }
            }

            // Update PersistentData table with the updated token values
            for (UUID uuid : persistentPlayers.keySet()) {
                try {
                    SQLMain.con.createStatement().execute("UPDATE PersistentData SET Tokens = " + persistentPlayers.get(uuid).tokens + " WHERE UUID ='" + uuid.toString() + "'");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static float getRankMultiplier(String rank) {
        switch (rank) {
            case "default":
                return 1;
            case "sub":
                return 1.25f;
            case "sub+":
            case "youtuber":
                return 1.5f;
            case "sub++":
                return 1.75f;
            case "supporter":
            case "sub+++":
                return 2;
            default:
                return 1.5f;
        }
    }

    public static int tokensBasedOnLb(int i) {
        if (i == 0) return 10;
        if (i == 1) return 5;
        if (i == 2) return 3;
        return 1;
    }

    public static ItemStack getItem(String perk, int level) {
        boolean maxed = maxLevel(perk) == level;
        ItemStack is = new ItemStack(Material.DIAMOND);
        ItemMeta im = is.getItemMeta();
        switch (perk) {
            case "Tokens":
                is.setType(Material.GOLD_NUGGET);
                im.setDisplayName(ChatColor.YELLOW + "Tokens: " + level);
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "You aquire Tokens based on your performance each wipe.",
                        ChatColor.GRAY + "Methods to earn Tokens at the end of the wipe:",
                        ChatColor.GRAY + "Place in the Top 10 in Max HP, Mob Kills, Player Kills or Ore Mined",
                        ChatColor.GRAY + "Have more player kills than deaths",
                        ChatColor.GRAY + "Have over 200 Mob kills with less than 10 deaths",
                        ChatColor.GRAY + "Gain a Token for each 200 Mob Kills",
                        ChatColor.GRAY + "Gain a Token for each 2000 Max HP",
                        ChatColor.GRAY + "Gain a Token for each 64 Ore Mined",
                        "",
                        ChatColor.GREEN + "Click here to refund your upgrades for 20% of",
                        ChatColor.GREEN + "Tokens spent on them. This penalty is reduced ",
                        ChatColor.GREEN + "to 10% for " + ChatColor.DARK_AQUA + ChatColor.BOLD + "S++" + ChatColor.GREEN + " and above players."
                ));
                is.setItemMeta(im);
                break;
            case "Mount":
                is.setType(Material.SADDLE);
                im.setDisplayName(ChatColor.YELLOW + "Mount Start " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Start the wipe with a mount",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + "Tier " + getBonus(perk, level),
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + "" + getCost(perk, level) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "BankPages":
                is.setType(Material.ENDER_CHEST);
                im.setDisplayName(ChatColor.YELLOW + "Big Bank " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Increase the amount of pages in your bank",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + " Page(s)",
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + "" + getCost(perk, level) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "Pickaxe":
                is.setType(Material.DIAMOND_PICKAXE);
                im.setDisplayName(ChatColor.YELLOW + "Pickaxe Forging " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Pickaxes you purchase start at a higher level",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + "Level " + getBonus(perk, level),
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + "" + getCost(perk, level) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "Farmer":
                is.setType(Material.GOLD_HOE);
                im.setDisplayName(ChatColor.YELLOW + "Farmer " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Deal increased damage to monsters:",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + "%",
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + "" + getCost(perk, level) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "LastStand":
                is.setType(Material.DIAMOND_SWORD);
                im.setDisplayName(ChatColor.YELLOW + "Last Stand " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Deal increased damage while under 30% Health:",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + "%",
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + "" + getCost(perk, level) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "OrbRolls":
                is.setType(Material.MAGMA_CREAM);
                im.setDisplayName(ChatColor.YELLOW + "Reroller " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Legendary Orbs will roll each stat X times and take the maximum",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + " Extra Roll(s)",
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + "" + getCost(perk, level) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "Luck":
                is.setType(Material.GOLD_INGOT);
                im.setDisplayName(ChatColor.YELLOW + "Luck " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Enchants and Altars will be more likely to succeed",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + "%",
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + "" + getCost(perk, level) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "Reaper":
                is.setType(Material.REDSTONE);
                im.setDisplayName(ChatColor.YELLOW + "Reaper " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Restore a percentage of missing health on kill, tripled vs Players.",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + "%",
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + String.valueOf(getCost(perk, level)) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "KitWeapon":
                is.setType(Material.WOOD_AXE);
                im.setDisplayName(ChatColor.YELLOW + "Starter's Axe " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Start with a T1 Axe, upgradable Rarity",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getRarityFromInt(level - 1),
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + String.valueOf(getCost(perk, level)) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "KitHelm":
                is.setType(Material.LEATHER_HELMET);
                im.setDisplayName(ChatColor.YELLOW + "Starter's Helm " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Start with a T1 Helm, upgradable Rarity",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getRarityFromInt(level - 1),
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + String.valueOf(getCost(perk, level)) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "KitChest":
                is.setType(Material.LEATHER_CHESTPLATE);
                im.setDisplayName(ChatColor.YELLOW + "Starter's Chestplate " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Start with a T1 Chestplate, upgradable Rarity",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getRarityFromInt(level - 1),
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + String.valueOf(getCost(perk, level)) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "KitLegs":
                is.setType(Material.LEATHER_LEGGINGS);
                im.setDisplayName(ChatColor.YELLOW + "Starter's Leggings " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Start with T1 Leggings, upgradable Rarity",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getRarityFromInt(level - 1),
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + String.valueOf(getCost(perk, level)) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
            case "KitBoots":
                is.setType(Material.LEATHER_BOOTS);
                im.setDisplayName(ChatColor.YELLOW + "Starter's Boots " + convertRoman(level));
                im.setLore(Arrays.asList(
                        ChatColor.GRAY + "Start with T1 Boots, upgradable Rarity",
                        ChatColor.GRAY + "Current: " + ChatColor.AQUA + getRarityFromInt(level - 1),
                        (maxed ? ChatColor.RED + "Cannot be upgraded further" : ChatColor.GREEN + String.valueOf(getCost(perk, level)) + " Tokens")
                ));
                is.setItemMeta(im);
                break;
        }
        return is;
    }

    public static String convertRoman(int level) {
        switch (level) {
            case 0:
                return "";
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V"; //should be all i need for now
        }
        return "";
    }

    public static int getCost(String perk, int level) {
        level++;
        switch (perk) {
            case "Reset":
                return -1;
            case "Mount":
                return 50;
            case "Farmer":
                switch (level) {
                    case 1:
                        return 10;
                    case 2:
                        return 20;
                    case 3:
                        return 30;
                    case 4:
                        return 40;
                    case 5:
                        return 50;
                }
            case "Luck":
            case "LastStand":
            case "Pickaxe":
                switch (level) {
                    case 1:
                        return 20;
                    case 2:
                        return 40;
                    case 3:
                        return 60;
                    case 4:
                        return 80;
                    case 5:
                        return 100;
                }
            case "Reaper":
                switch (level) {
                    case 1:
                        return 30;
                    case 2:
                        return 60;
                    case 3:
                        return 90;
                    case 4:
                        return 120;
                    case 5:
                        return 150;
                }
            case "BankPages":
                switch (level) {
                    case 1:
                        return 0;
                    case 2:
                        return 60;
                    case 3:
                        return 90;
                    case 4:
                        return 120;
                    case 5:
                        return 150;
                }
            case "OrbRolls":
                switch (level) {
                    case 1:
                        return 50;
                    case 2:
                        return 150;
                    case 3:
                        return 500;
                }
            case "KitWeapon":
                switch (level) {
                    case 1:
                        return 0;
                    case 2:
                        return 25;
                    case 3:
                        return 100;
                    case 4:
                        return 250;
                }
            case "KitHelm":
            case "KitChest":
            case "KitLegs":
            case "KitBoots":
                switch (level) {
                    case 1:
                        return 10;
                    case 2:
                        return 20;
                    case 3:
                        return 30;
                    case 4:
                        return 40;
                }
        }
        return 0;
    }

    public static int getBonus(String perk, int level) {
        switch (perk) {
            case "Mount":
                if (level == 0) return 0;
                return level + 1;
            case "BankPages":
            case "OrbRolls":
                return level;
            case "Pickaxe":
                return 20 * level;
            case "Farmer":
            case "Luck":
            case "Reaper":
                return 2 * level;
            case "LastStand":
                return 5 * level;
        }
        return 0;
    }

    public static int maxLevel(String perk) {
        switch (perk) {
            case "Reset":
            case "OrbRolls":
                return 3;
            case "KitWeapon":
            case "Mount":
            case "KitHelm":
            case "KitChest":
            case "KitLegs":
            case "KitBoots":
                return 4;
            case "BankPages":
            case "Pickaxe":
            case "Farmer":
            case "LastStand":
            case "Luck":
            case "Reaper":
                return 5;
        }
        return 0;
    }

    public static String getRarityFromInt(int rarity) {
        switch (rarity) {
            case 0:
                return ChatColor.GRAY + "Common";
            case 1:
                return ChatColor.GREEN + "Uncommon";
            case 2:
                return ChatColor.AQUA + "Rare";
            case 3:
                return ChatColor.YELLOW + "Unique";
            default:
                return "None";
        }
    }

    public String getPerkFromItem(Material material) {
        switch (material) {
            case GOLD_NUGGET:
                return "Reset";
            case SADDLE:
                return "Mount";
            case ENDER_CHEST:
                return "BankPages";
            case DIAMOND_PICKAXE:
                return "Pickaxe";
            case GOLD_HOE:
                return "Farmer";
            case MAGMA_CREAM:
                return "OrbRolls";
            case DIAMOND_SWORD:
                return "LastStand";
            case REDSTONE:
                return "Reaper";
            case GOLD_INGOT:
                return "Luck";
            case WOOD_AXE:
                return "KitWeapon";
            case LEATHER_HELMET:
                return "KitHelm";
            case LEATHER_CHESTPLATE:
                return "KitChest";
            case LEATHER_LEGGINGS:
                return "KitLegs";
            case LEATHER_BOOTS:
                return "KitBoots";
            default:
                return "";
        }
    }

    /*Halloween Candy Shit*/
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory().getTitle().equals("Upgrade Vendor")) {
            e.setCancelled(true);
            PersistentPlayer pp = persistentPlayers.get(p.getUniqueId());
            if (e.getCurrentItem() != null &&
                    (e.getCurrentItem().getType() == Material.SADDLE
                            || e.getCurrentItem().getType() == Material.ENDER_CHEST
                            || e.getCurrentItem().getType() == Material.DIAMOND_PICKAXE
                            || e.getCurrentItem().getType() == Material.GOLD_HOE
                            || e.getCurrentItem().getType() == Material.DIAMOND_SWORD
                            || e.getCurrentItem().getType() == Material.MAGMA_CREAM
                            || e.getCurrentItem().getType() == Material.GOLD_INGOT
                            || e.getCurrentItem().getType() == Material.REDSTONE
                            || e.getCurrentItem().getType() == Material.WOOD_AXE
                            || e.getCurrentItem().getType() == Material.LEATHER_HELMET
                            || e.getCurrentItem().getType() == Material.LEATHER_CHESTPLATE
                            || e.getCurrentItem().getType() == Material.LEATHER_LEGGINGS
                            || e.getCurrentItem().getType() == Material.LEATHER_BOOTS
                            || e.getCurrentItem().getType() == Material.GOLD_NUGGET)) {
                String perk = getPerkFromItem(e.getCurrentItem().getType());
                int price = getCost(perk, pp.getLevel(perk));
                if (pp.getLevel(perk) >= maxLevel(perk)) {
                    p.sendMessage(ChatColor.RED + "This perk cannot be upgraded any further!");
                    p.closeInventory();
                    return;
                }
                if (pp.tokens >= price) {
                    confirm.put(p, perk);
                    if(e.getCurrentItem().getType() == Material.GOLD_NUGGET){
                        int spent = getSpent(p);
                        if (spent == 0) return;
                        p.sendMessage(ChatColor.GREEN + "Are you sure you want to refund your tokens? You will recieve " +
                                getRefund(p, spent) + " out of " + spent + " you have spent so far. Type 'confirm' to continue.");
                    }else{
                        p.sendMessage(ChatColor.GREEN + "Are you sure you want to upgrade this perk? Type 'confirm' to continue.");
                    }
                    p.closeInventory();
                } else {
                    p.sendMessage(ChatColor.RED + "You do NOT have enough tokens to purchase this upgrade.");
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + " Tokens");
                    p.closeInventory();
                }
            }
        }
    }

    @EventHandler
    public void onPromptChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (confirm.containsKey(p)) {
            PersistentPlayer pp = persistentPlayers.get(p.getUniqueId());
            e.setCancelled(true);
            String perk = confirm.get(p);
            int price = getCost(perk, pp.getLevel(perk));
            if (pp.getLevel(perk) >= maxLevel(perk)) {
                p.sendMessage(ChatColor.RED + "This perk cannot be upgraded any further!");
                return;
            }
            if (!e.getMessage().equalsIgnoreCase("confirm")) {
                p.sendMessage(ChatColor.RED + "Purchase of upgrade - " + ChatColor.BOLD + "CANCELLED");
                confirm.remove(p);
                return;
            }
            if (pp.tokens < price) {
                p.sendMessage(ChatColor.RED + "You do NOT have enough tokens to purchase this upgrade.");
                p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + " Tokens");
                return;
            }
            if(perk == "Reset"){
                PersistentPlayers.put(p.getUniqueId(), new PersistentPlayer(getRefund(p, getSpent(p)) + pp.tokens, 0, 0, 0, 0, 1,
                        0, 0, 0, 1, 0, 0, 0, 0));
                p.sendMessage(ChatColor.GREEN + "Refund Successful!");
                confirm.remove(p);
            }else{
                pp.tokens -= price;
                pp.setLevel(perk, pp.getLevel(perk) + 1);
                confirm.remove(p);
                p.sendMessage(ChatColor.GREEN + "Perk Upgrade Successful!");
            }
        }
    }

    @EventHandler
    public void onLastStand(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
        if (p.getHealth() < p.getMaxHealth() * .3) {
            e.setDamage(e.getDamage() * (1 + (.05 * pp.laststand)));
        }
        if (!(e.getEntity() instanceof Player)) {
            e.setDamage(e.getDamage() * (1 + (.02 * pp.farmer)));
        }
        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) e.getEntity();
            if (livingEntity.getHealth() - e.getDamage() < 0) {
                double health = p.getMaxHealth() - p.getHealth();
                health *= (.02 * pp.reaper);
                if (e.getEntity() instanceof Player) health *= 3;
                p.setHealth(p.getHealth() + health);
                if (health > 1 && Toggles.getToggleStatus(p, "Debug"))
                    p.sendMessage(ChatColor.GREEN + "Reaper: +" + Math.round(health) + " HP");
            }
        }
    }

    public int getSpent(Player p) {
        PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
        int tokens = 0;
        String[] perks = {"BankPages", "Mount", "Pickaxe", "Farmer", "LastStand", "OrbRolls", "Luck", "Reaper",
                "KitWeapon", "KitHelm", "KitChest", "KitLegs", "KitBoots"};
        for(String perk : perks){
            for(int i = pp.getLevel(perk); i > 0; i--) {
                tokens += getCost(perk, i-1);
            }
        }
        return tokens;
    }

    public int getRefund(Player p, int tokens){
        if (ModerationMechanics.getRank(p) == RankEnum.DEFAULT
                || ModerationMechanics.getRank(p) == RankEnum.SUB
                || ModerationMechanics.getRank(p) == RankEnum.SUB1
                || ModerationMechanics.getRank(p) == RankEnum.YOUTUBER){
            return Math.round(tokens  * .8f);
        }else{
            return Math.round(tokens  * .9f);
        }
    }

}