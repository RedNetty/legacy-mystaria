package me.retrorealms.practiceserver.mechanics.player;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.vendors.ItemVendors;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.*;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.Hash;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentPlayers implements Listener {
    public static ConcurrentHashMap<UUID, PersistentPlayer> persistentPlayers = new ConcurrentHashMap<>();
    public static HashMap<Player, String> confirm = new HashMap<>();
    private static final int TOKENS_PER_QUEST = 5;
    private static final int MAX_QUESTS_PER_DAY = 5;
    private static HashMap<Player, Integer> questProgress = new HashMap<>();

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        startTokenQuests();
    }

    @EventHandler
    void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (!persistentPlayers.containsKey(uuid)) {
            PersistentPlayers.put(uuid, new PersistentPlayer(50, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, null));
        }
        assignRandomQuest(e.getPlayer());
    }

    @EventHandler
    void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (persistentPlayers.containsKey(uuid)) {
            SQLMain.updatePersistentStats(e.getPlayer());
        }
    }

    public static void put(UUID uuid, PersistentPlayer pp) {
        persistentPlayers.put(uuid, pp);
    }

    public static PersistentPlayer get(UUID uuid) {
        return persistentPlayers.get(uuid);
    }

    private void startTokenQuests() {
        Bukkit.getScheduler().runTaskTimer(PracticeServer.plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PersistentPlayer pp = get(player.getUniqueId());
                if (pp.dailyQuestsCompleted < MAX_QUESTS_PER_DAY) {
                    assignRandomQuest(player);
                }
            }
        }, 0L, 60 * 60 * 20L); // Run every hour
    }

    private static void assignRandomQuest(Player player) {
        PersistentPlayer pp = get(player.getUniqueId());
        if (pp.currentQuest != null) {
            player.sendMessage(ChatColor.GRAY + "Your current Token Quest is: " +  ChatColor.YELLOW + pp.currentQuest);
            return;
        }

        String[] quests = {"Kill 50 monsters", "Mine 100 ores", "Defeat 5 players"};
        String quest = quests[new Random().nextInt(quests.length)];
        player.sendMessage(ChatColor.YELLOW + "New Token Quest: " + quest);
        pp.currentQuest = quest;
        questProgress.put(player, 0); // Initialize progress
        SQLMain.updatePersistentStats(player); // Save the assigned quest
    }
    public static String getCurrentQuest(Player player) {
        PersistentPlayer pp = get(player.getUniqueId());
        if (pp.currentQuest == null) {
            return "You don't have an active quest.";
        }
        int progress = questProgress.getOrDefault(player, 0);
        return pp.currentQuest + " - Progress: " + progress;
    }
    public static void updateQuestProgress(Player player, int amount) {
        PersistentPlayer pp = get(player.getUniqueId());
        if (pp.currentQuest == null) {
            return; // No active quest
        }

        int currentProgress = questProgress.getOrDefault(player, 0);
        int newProgress = currentProgress + amount;
        questProgress.put(player, newProgress);

        player.sendMessage(ChatColor.GRAY + "Quest progress updated: " + ChatColor.YELLOW + newProgress);

        // Check if the quest is completed
        int targetProgress = getQuestTarget(pp.currentQuest);
        if (newProgress >= targetProgress) {
            completeQuest(player);
        }
    }
    private static int getQuestTarget(String quest) {
        if (quest.startsWith("Kill")) return 50;
        if (quest.startsWith("Mine")) return 100;
        if (quest.startsWith("Defeat")) return 5;
        return 0; // Default case
    }
    public static ItemStack getConfirmButton(boolean on) {
        ItemStack is = new ItemStack(Material.INK_SACK);
        ItemMeta im = is.getItemMeta();
        if (on) {
            is.setDurability((short) 10);
        } else {
            is.setDurability((short) 8);
        }
        im.setDisplayName(on ? ChatColor.GREEN + "Confirm Upgrade" : ChatColor.RED + "Cancel");
        is.setItemMeta(im);
        return is;
    }
    private void openConfirmMenu(Player player, String perk) {
        Inventory confirmMenu = Bukkit.createInventory(null, 27, "Confirm Upgrade");

        ItemStack confirm = getConfirmButton(true);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = getConfirmButton(false);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancel.setItemMeta(cancelMeta);

        confirmMenu.setItem(11, confirm);
        confirmMenu.setItem(15, cancel);

        player.openInventory(confirmMenu);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals("Token Upgrade Menu")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;

            String itemName = clickedItem.getItemMeta().getDisplayName();
            PersistentPlayer pp = PersistentPlayers.get(player.getUniqueId());
            if (itemName.contains("Refund Upgrades")) {
                openConfirmMenu(player, itemName);
                return;
            }

            String perk = getPerkFromDisplayName(itemName);
            if (perk != null && pp.tokens >= getCost(perk, pp.getLevel(perk))) {
                openConfirmMenu(player, itemName);
            } else {
                player.sendMessage(ChatColor.RED + "You do not have enough tokens to upgrade this perk.");
            }
        } else if (event.getView().getTitle().equals("Confirm Upgrade")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String itemName = clickedItem.getItemMeta().getDisplayName();
            if (itemName.contains("Confirm")) {
                PersistentPlayer pp = PersistentPlayers.get(player.getUniqueId());
                String perk = confirm.get(player);
                int level = pp.getLevel(perk);
                System.out.println("test");
                if (pp.tokens >= getCost(perk, level)) {
                    pp.tokens -= getCost(perk, level);
                    pp.setLevel(perk, level + 1);
                    player.sendMessage(ChatColor.GREEN + "You have successfully upgraded " + perk + " to level " + (level + 1));
                    SQLMain.updatePersistentStats(player);
                    player.closeInventory();
                    ItemVendors.openUpgradeVendorInventory(player); // Reopen the upgrade menu
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have enough tokens to upgrade this perk.");
                }
                confirm.remove(player);
            } else if (itemName.contains("Cancel")) {
                player.closeInventory();
                ItemVendors.openUpgradeVendorInventory(player); // Reopen the upgrade menu
            }
        }
    }
    private String getPerkFromDisplayName(String displayName) {
        if (displayName.contains("Tokens")) return "Tokens";
        if (displayName.contains("Mount Start")) return "Mount";
        if (displayName.contains("Big Bank")) return "BankPages";
        if (displayName.contains("Pickaxe Forging")) return "Pickaxe";
        if (displayName.contains("Farmer")) return "Farmer";
        if (displayName.contains("Last Stand")) return "LastStand";
        if (displayName.contains("Reroller")) return "OrbRolls";
        if (displayName.contains("Luck")) return "Luck";
        if (displayName.contains("Reaper")) return "Reaper";
        if (displayName.contains("Starter's Axe")) return "KitWeapon";
        if (displayName.contains("Starter's Helm")) return "KitHelm";
        if (displayName.contains("Starter's Chestplate")) return "KitChest";
        if (displayName.contains("Starter's Leggings")) return "KitLegs";
        if (displayName.contains("Starter's Boots")) return "KitBoots";
        return null;
    }

    private static void completeQuest(Player player) {
        PersistentPlayer pp = get(player.getUniqueId());
        if (pp.currentQuest != null && pp.dailyQuestsCompleted < MAX_QUESTS_PER_DAY) {
            pp.tokens += TOKENS_PER_QUEST;
            pp.dailyQuestsCompleted++;
            player.sendMessage(ChatColor.GREEN + "Quest completed! You earned " + TOKENS_PER_QUEST + " tokens.");
            player.sendMessage(ChatColor.YELLOW + "You have completed " + pp.dailyQuestsCompleted + "/" + MAX_QUESTS_PER_DAY + " quests today.");
            pp.currentQuest = null;

            if (pp.dailyQuestsCompleted < MAX_QUESTS_PER_DAY) {
                assignRandomQuest(player);
            } else {
                player.sendMessage(ChatColor.RED + "You have completed all available quests for today.");
            }

            SQLMain.updatePersistentStats(player); // Save the updated quest progress and tokens
        }
    }
    public static String getNextUpgradePercentage(String perk, int currentLevel) {
        int nextLevel = currentLevel + 1;
        if (nextLevel > maxLevel(perk)) {
            return "Max Level";
        }

        int currentBonus = getBonus(perk, currentLevel);
        int nextBonus = getBonus(perk, nextLevel);

        return (nextBonus - currentBonus) + "%";
    }
    public static void assignTokens(boolean test) {
        Map<UUID, Integer> tokens = new HashMap<>();
        try {
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
                newTokens += oreMined / 240;

                tokens.put(UUID.fromString(rs.getString("UUID")), newTokens);
            }

            String[] leaderboardCategories = {"T1Kills", "T2Kills", "T3Kills", "T4Kills", "T5Kills", "PlayerKills", "OreMined", "MaxHP", "Gems"};
            for (String category : leaderboardCategories) {
                int i = 0;
                rs = SQLMain.getPlayerData("PlayerData", "UUID, " + category);
                while (rs.next() && i < 10) {
                    UUID uuid = UUID.fromString(rs.getString("UUID"));
                    tokens.put(uuid, tokens.get(uuid) + tokensBasedOnLb(i));
                    i++;
                }
            }

            rs = SQLMain.con.createStatement().executeQuery(
                    "SELECT UUID, playerrank FROM PersistentData NATURAL JOIN PlayerData WHERE persistentdata.playerrank != 'default'");
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("UUID"));
                tokens.put(uuid, Math.round(tokens.get(uuid) * getRankMultiplier(rs.getString("PlayerRank"))));
            }

            if (test) {
                tokens.replaceAll((u, v) -> 0);
            }

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

            for (UUID uuid : persistentPlayers.keySet()) {
                try {
                    SQLMain.updatePersistentStats(Bukkit.getPlayer(uuid)); // Save the updated tokens for each player
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
        List<String> lore = new ArrayList<>();

        switch (perk) {
            case "Tokens":
                is.setType(Material.GOLD_NUGGET);
                im.setDisplayName(ChatColor.YELLOW + "Tokens: " + level);
                lore.addAll(Arrays.asList(
                        ChatColor.GRAY + "You acquire Tokens based on your performance each wipe.",
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
                break;
            case "Mount":
                is.setType(Material.SADDLE);
                im.setDisplayName(ChatColor.YELLOW + "Mount Start " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Start the wipe with a mount");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + "Tier " + getBonus(perk, level));
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + "Tier " + getBonus(perk, level + 1));
                }
                break;
            case "BankPages":
                is.setType(Material.ENDER_CHEST);
                im.setDisplayName(ChatColor.YELLOW + "Big Bank " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Increase the amount of pages in your bank");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + " Page(s)");
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getBonus(perk, level + 1) + " Page(s)");
                }
                break;
            case "Pickaxe":
                is.setType(Material.DIAMOND_PICKAXE);
                im.setDisplayName(ChatColor.YELLOW + "Pickaxe Forging " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Pickaxes you purchase start at a higher level");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + "Level " + getBonus(perk, level));
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + "Level " + getBonus(perk, level + 1) + " (+" + getNextUpgradePercentage(perk, level) + ")");
                }
                break;
            case "Farmer":
                is.setType(Material.GOLD_HOE);
                im.setDisplayName(ChatColor.YELLOW + "Farmer " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Deal increased damage to monsters:");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + "%");
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getBonus(perk, level + 1) + "% (+" + getNextUpgradePercentage(perk, level) + ")");
                }
                break;
            case "LastStand":
                is.setType(Material.DIAMOND_SWORD);
                im.setDisplayName(ChatColor.YELLOW + "Last Stand " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Deal increased damage while under 30% Health:");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + "%");
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getBonus(perk, level + 1) + "% (+" + getNextUpgradePercentage(perk, level) + ")");
                }
                break;
            case "OrbRolls":
                is.setType(Material.MAGMA_CREAM);
                im.setDisplayName(ChatColor.YELLOW + "Reroller " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Legendary Orbs will roll each stat X times and take the maximum");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + " Extra Roll(s)");
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getBonus(perk, level + 1) + " Extra Roll(s)");
                }
                break;
            case "Luck":
                is.setType(Material.GOLD_INGOT);
                im.setDisplayName(ChatColor.YELLOW + "Luck " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Enchants and Altars will be more likely to succeed");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + "%");
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getBonus(perk, level + 1) + "% (+" + getNextUpgradePercentage(perk, level) + ")");
                }
                break;
            case "Reaper":
                is.setType(Material.REDSTONE);
                im.setDisplayName(ChatColor.YELLOW + "Reaper " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Restore a percentage of missing health on kill, tripled vs Players.");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getBonus(perk, level) + "%");
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getBonus(perk, level + 1) + "% (+" + getNextUpgradePercentage(perk, level) + ")");
                }
                break;
            case "KitWeapon":
                is.setType(Material.WOOD_AXE);
                im.setDisplayName(ChatColor.YELLOW + "Starter's Axe " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Start with a T1 Axe, upgradable Rarity");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getRarityFromInt(level - 1));
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getRarityFromInt(level));
                }
                break;
            case "KitHelm":
                is.setType(Material.LEATHER_HELMET);
                im.setDisplayName(ChatColor.YELLOW + "Starter's Helm " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Start with a T1 Helm, upgradable Rarity");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getRarityFromInt(level - 1));
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getRarityFromInt(level));
                }
                break;
            case "KitChest":
                is.setType(Material.LEATHER_CHESTPLATE);
                im.setDisplayName(ChatColor.YELLOW + "Starter's Chestplate " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Start with a T1 Chestplate, upgradable Rarity");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getRarityFromInt(level - 1));
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getRarityFromInt(level));
                }
                break;
            case "KitLegs":
                is.setType(Material.LEATHER_LEGGINGS);
                im.setDisplayName(ChatColor.YELLOW + "Starter's Leggings " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Start with T1 Leggings, upgradable Rarity");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getRarityFromInt(level - 1));
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getRarityFromInt(level));
                }
                break;
            case "KitBoots":
                is.setType(Material.LEATHER_BOOTS);
                im.setDisplayName(ChatColor.YELLOW + "Starter's Boots " + convertRoman(level));
                lore.add(ChatColor.GRAY + "Start with T1 Boots, upgradable Rarity");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + getRarityFromInt(level - 1));
                if (!maxed) {
                    lore.add(ChatColor.GRAY + "Next: " + ChatColor.AQUA + getRarityFromInt(level));
                }
                break;
        }

        if (maxed) {
            lore.add(ChatColor.RED + "Cannot be upgraded further");
        } else {
            lore.add(ChatColor.GREEN + "" + getCost(perk, level) + " Tokens");
        }

        im.setLore(lore);
        is.setItemMeta(im);
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
                return "V";
            default:
                return "";
        }
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
                    openConfirmMenu(p,perk);
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
            if(perk.equals("Reset")){
                PersistentPlayers.put(p.getUniqueId(), new PersistentPlayer(getRefund(p, getSpent(p)) + pp.tokens, 0, 0, 0, 0, 1,
                        0, 0, 0, 1, 0, 0, 0, 0, 0, null));
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
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onLastStand(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
        if (p.getHealth() < p.getMaxHealth() * .3) {
            double lastStandMultiplier = .05 * pp.laststand;
            double newDamage = e.getDamage() * (1 + lastStandMultiplier);
            e.setDamage(newDamage);
        }
        if (!(e.getEntity() instanceof Player)) {
            double farmerMultiplier = .02 * pp.farmer;
            double newDamage = e.getDamage() * (1 + farmerMultiplier);
            e.setDamage(newDamage);
        }
        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) e.getEntity();
            double health = livingEntity.getHealth();
            double damage = e.getDamage();
            if (health - damage <= 0) {
                double reaperHealth = p.getMaxHealth() - p.getHealth();
                reaperHealth *= (.02 * pp.reaper);
                if (livingEntity instanceof Player) reaperHealth *= 3;
                p.setHealth(Math.min(p.getHealth() + reaperHealth, p.getMaxHealth()));
                if (reaperHealth > 1 && Toggles.isToggled(p, "Debug"))
                    p.sendMessage(ChatColor.GREEN + "Reaper: +" + Math.round(reaperHealth) + " HP");
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
