package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.nbt.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.inventory.Icon;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderboardCommand implements CommandExecutor, Listener {

    private final String[] leaderboardAreas = {
            "T1Kills",
            "T2Kills",
            "T3Kills",
            "T4Kills",
            "T5Kills",
            "PlayerKills",
            "Deaths",
            "MaxHP",
            "Gems"
    };

    private final String[] leaderboardAreaNames = {
            "T1 Kills",
            "T2 Kills",
            "T3 Kills",
            "T4 Kills",
            "T5 Kills",
            "Player Kills",
            "Deaths",
            "Max HP",
            "Gems"
    };

    private final Map<String, List<String>> leaderboardCache = new HashMap<>();
    private final Map<String, ItemStack> categoryItems = new HashMap<>();

    public void registerEvent() {
        Bukkit.getPluginManager().registerEvents(this, PracticeServer.getInstance());
        startCacheUpdateTask();
        initializeLeaderboardCache();
        createCategoryItems();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            openCategoryMenu((Player) sender);
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by players.");
        }
        return true;
    }

    public void openCategoryMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "Leaderboard Categories");

        for (int i = 0; i < leaderboardAreas.length; i++) {
            String categoryName = leaderboardAreaNames[i];
            String areaName = leaderboardAreas[i];
            ItemStack categoryItem = categoryItems.getOrDefault(areaName, createCategoryItem(categoryName, areaName));
            inventory.setItem(i, categoryItem);
        }

        player.openInventory(inventory);
    }

    private void createCategoryItems() {
        for (int i = 0; i < leaderboardAreas.length; i++) {
            String categoryName = leaderboardAreaNames[i];
            String areaName = leaderboardAreas[i];
            ItemStack categoryItem = createCategoryItem(categoryName, areaName);
            categoryItems.put(areaName, categoryItem);
        }
    }

    private ItemStack createCategoryItem(String categoryName, String areaName) {
        Material material = getMaterialByAreaName(areaName);
        ItemStack categoryItem = new ItemStack(material);
        ItemMeta itemMeta = categoryItem.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + categoryName + " Leaderboard");
        categoryItem.setItemMeta(itemMeta);
        categoryItem = setAreaName(categoryItem, areaName);
        return categoryItem;
    }

    private Material getMaterialByAreaName(String areaName) {
        // You can customize the materials based on the areaName here
        // This is just an example, you can replace it with your own logic
        switch (areaName) {
            case "T1Kills":
                return Material.WOOD_SWORD;
            case "T2Kills":
                return Material.STONE_SWORD;
            case "T3Kills":
                return Material.IRON_SWORD;
            case "T4Kills":
                return Material.DIAMOND_SWORD;
            case "T5Kills":
                return Material.GOLD_SWORD;
            case "PlayerKills":
                return Material.SKULL_ITEM;
            case "Gems":
                return Material.EMERALD;
            case "Deaths":
                return Material.BONE;
            case "MaxHP":
                return Material.END_CRYSTAL;
            case "OreMined":
                return Material.GOLD_PICKAXE;
            default:
                return Material.PAPER;
        }
    }

    private ItemStack setAreaName(ItemStack itemStack, String areaName) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemStack.setItemMeta(itemMeta);
        NBTAccessor nbtAccessor = new NBTAccessor(itemStack).check();
        nbtAccessor.setString("areaName", areaName);
        return nbtAccessor.update();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (inventory != null && inventory.getHolder() == null) {
            if (inventory.getTitle().contains("Leaderboard")) {
                event.setCancelled(true);
                NBTAccessor nbtAccessor = new NBTAccessor(clickedItem).check();
                if (clickedItem != null && nbtAccessor.hasKey("areaName")) {
                    ItemMeta itemMeta = clickedItem.getItemMeta();
                    String areaName = nbtAccessor.hasKey("areaName") ? nbtAccessor.getString("areaName") : "T1Kills";
                    if (areaName != null) {
                        player.closeInventory();
                        openLeaderboardMenu(player, areaName);
                    }
                }
            }
        }
    }

    private void openLeaderboardMenu(Player player, String areaName) {
        int rank = 1; // Initialize rank to 1
        int inventorySize = 18;
        List<String> leaderboard = leaderboardCache.getOrDefault(areaName, new ArrayList<>());

        if (!leaderboard.isEmpty()) {
            openLeaderboard(player, areaName, leaderboard, rank); // Pass rank to the openLeaderboard method
        } else {
            fetchLeaderboardData(areaName)
                    .thenAcceptAsync(leaderboardData -> {
                        leaderboardCache.put(areaName, leaderboardData);
                        openLeaderboard(player, areaName, leaderboardData, rank); // Pass rank to the openLeaderboard method
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        }
    }


    private CompletableFuture<List<String>> fetchLeaderboardData(String areaName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = getPlayerDataStatement(areaName, 10);
                ResultSet rs = statement.executeQuery();
                List<String> leaderboard = new ArrayList<>();

                while (rs.next()) {
                    String username = rs.getString("Username");
                    int value = rs.getInt(areaName);
                    leaderboard.add(username + ": " + value);
                }

                // Sort the leaderboard based on the value (in descending order)
                leaderboard.sort((entry1, entry2) -> {
                    int value1 = Integer.parseInt(entry1.split(": ")[1]);
                    int value2 = Integer.parseInt(entry2.split(": ")[1]);
                    return Integer.compare(value2, value1);
                });

                // Add players with no data to the leaderboard
                ResultSet allPlayers = getPlayerData("PlayerData", "Username", areaName);
                while (allPlayers.next()) {
                    String username = allPlayers.getString("Username");
                    if (!leaderboard.stream().anyMatch(entry -> entry.startsWith(username + ":"))) {
                        leaderboard.add(username + ": 0");
                    }
                }

                return leaderboard;
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }




    private PreparedStatement getPlayerDataStatement(String areaName, int limit) throws SQLException {
        String query = "SELECT Username, " + areaName + " FROM PlayerData ORDER BY " + areaName + " DESC LIMIT ?";
        PreparedStatement statement = SQLMain.con.prepareStatement(query);
        statement.setInt(1, limit);
        return statement;
    }

    private void openLeaderboard(Player player, String areaName, List<String> leaderboard, int startingRank) {
        int rank = startingRank;
        int inventorySize = Math.min(leaderboard.size(), 27);
        Inventory inventory = Bukkit.createInventory(null, inventorySize, ChatColor.YELLOW + areaName + " Leaderboard");

        for (String entry : leaderboard) {
            ItemStack playerHead = createPlayerHead(entry, rank);
            inventory.addItem(playerHead);
            rank++;

            if (rank > inventorySize) {
                break;
            }
        }

        player.openInventory(inventory);
    }


    private ItemStack createPlayerHead(String entry, int rank) {
        String[] parts = entry.split(": ");
        String username = parts[0];
        int value = Integer.parseInt(parts[1]);

        Material material = Material.PAPER;
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        String displayName = ChatColor.YELLOW + "#" + rank + " " + ChatColor.WHITE + username;
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Value: " + ChatColor.YELLOW + value);

        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void startCacheUpdateTask() {
        int updateInterval = 500; // Update interval in seconds (adjust as needed)
        Bukkit.getScheduler().runTaskTimerAsynchronously(PracticeServer.getInstance(), this::cacheLeaderboard, 0L, updateInterval * 20L);
    }

    public void cacheLeaderboard() {
        for (String areaName : leaderboardAreas) {
            fetchLeaderboardData(areaName)
                    .thenAcceptAsync(leaderboardData -> leaderboardCache.put(areaName, leaderboardData))
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        }
    }

    private void initializeLeaderboardCache() {
        CompletableFuture<Void> initializationTask = fetchAllLeaderboardData()
                .thenAcceptAsync(leaderboardCache::putAll)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });

        // Wait for the leaderboard cache to be initialized before starting the cache update task
        initializationTask.join();
    }

    private CompletableFuture<Map<String, List<String>>> fetchAllLeaderboardData() {
        Map<String, CompletableFuture<List<String>>> leaderboardTasks = new HashMap<>();

        for (String areaName : leaderboardAreas) {
            CompletableFuture<List<String>> leaderboardTask = fetchLeaderboardData(areaName)
                    .thenApplyAsync(leaderboard -> {
                        leaderboard.sort((entry1, entry2) -> {
                            int value1 = Integer.parseInt(entry1.split(": ")[1]);
                            int value2 = Integer.parseInt(entry2.split(": ")[1]);
                            return Integer.compare(value2, value1);
                        });
                        return leaderboard;
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return new ArrayList<>();
                    });
            leaderboardTasks.put(areaName, leaderboardTask);
        }

        return CompletableFuture.allOf(leaderboardTasks.values().toArray(new CompletableFuture[0]))
                .thenApplyAsync(v -> {
                    Map<String, List<String>> leaderboardData = new HashMap<>();
                    for (Map.Entry<String, CompletableFuture<List<String>>> entry : leaderboardTasks.entrySet()) {
                        String areaName = entry.getKey();
                        List<String> leaderboard = entry.getValue().join();
                        leaderboardData.put(areaName, leaderboard);
                    }
                    return leaderboardData;
                });
    }



    public static ResultSet getPlayerData(String table, String columns, String orderby) throws SQLException {
        PreparedStatement statement = SQLMain.con.prepareStatement("SELECT " + columns + " FROM " + table + " ORDER BY " + orderby + " DESC");
        return statement.executeQuery();
    }
}
