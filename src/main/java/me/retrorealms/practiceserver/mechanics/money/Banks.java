package me.retrorealms.practiceserver.mechanics.money;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.money.Economy.Economy;
import me.retrorealms.practiceserver.mechanics.player.GamePlayer.nonStaticConfig;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.mechanics.world.MinigameState;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Banks - Handles the banking system for the RetroRealms server
 * Allows players to store items and currency in a secure inventory
 */
public class Banks implements Listener {
    // Constants
    public static final int BANK_SIZE = 54;
    public static final int BANK_CONTENT_SIZE = BANK_SIZE - 9; // Usable area excluding bottom row

    // Concurrent collections for thread safety
    private static final Map<UUID, UUID> BANK_VIEW_MAP = new ConcurrentHashMap<>();
    private static final Set<UUID> WITHDRAW_PROMPT = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<UUID, Inventory> TEMP_BANKS = new ConcurrentHashMap<>();

    // Bank item identifiers
    private static final String BANK_TITLE_PREFIX = "Bank Chest (";
    private static final String BANK_TITLE_SUFFIX = "/1)";
    private static final String BANK_NOTE_NAME = ChatColor.GREEN + "Bank Note";

    /**
     * Initialize the Banks module
     */
    public void onEnable() {
        PracticeServer.log.info("[Banks] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    /**
     * Save all bank data when the plugin is disabled
     */
    public void onDisable() {
        PracticeServer.log.info("[Banks] has been disabled.");
        saveBanks();
    }

    /**
     * Save all temporary banks to persistent storage
     */
    private void saveBanks() {
        try {
            PracticeServer.log.info("[Banks] Saving " + TEMP_BANKS.size() + " banks...");
            for (Map.Entry<UUID, Inventory> entry : TEMP_BANKS.entrySet()) {
                saveBank(entry.getValue(), entry.getKey(), 1);
            }
            TEMP_BANKS.clear();
            PracticeServer.log.info("[Banks] All banks saved successfully.");
        } catch (Exception e) {
            PracticeServer.log.log(Level.SEVERE, "[Banks] Error saving banks", e);
        }
    }

    /**
     * Reset all temporary bank data (used during server resets)
     */
    public static void resetTempBanks() {
        TEMP_BANKS.clear();
    }

    /**
     * Save a specific bank inventory to storage
     *
     * @param inv The bank inventory to save
     * @param playerUUID The UUID of the player whose bank is being saved
     * @param page The page number of the bank
     */
    private void saveBank(Inventory inv, UUID playerUUID, int page) {
        try {
            if (PracticeServer.DATABASE) {
                SQLMain.saveBank(inv, playerUUID, page);
            } else {
                TEMP_BANKS.put(playerUUID, inv);
            }
        } catch (Exception e) {
            PracticeServer.log.log(Level.SEVERE, "[Banks] Error saving bank for player " + playerUUID, e);
        }
    }

    /**
     * Get a player's bank inventory for the specified page
     *
     * @param player The player whose bank to retrieve
     * @param page The page number of the bank
     * @return The bank inventory
     */
    public static Inventory getBank(Player player, int page) {
        if (player == null) return null;

        UUID viewingUUID = BANK_VIEW_MAP.getOrDefault(player.getUniqueId(), player.getUniqueId());
        String title = BANK_TITLE_PREFIX + page + BANK_TITLE_SUFFIX;

        try {
            if (PracticeServer.DATABASE && PracticeServer.getRaceMinigame().getGameState() == MinigameState.NONE) {
                return SQLMain.getBank(viewingUUID, page);
            }

            return TEMP_BANKS.computeIfAbsent(viewingUUID, uuid -> {
                Inventory newBank = Bukkit.createInventory(null, BANK_SIZE, title);
                initializeBankInventory(newBank);
                return newBank;
            });
        } catch (Exception e) {
            PracticeServer.log.log(Level.SEVERE, "[Banks] Error retrieving bank for player " + viewingUUID, e);
            // Return an empty bank as fallback
            Inventory fallbackBank = Bukkit.createInventory(null, BANK_SIZE, title);
            initializeBankInventory(fallbackBank);
            return fallbackBank;
        }
    }

    /**
     * Initialize a new bank inventory with the bottom row UI elements
     *
     * @param inventory The inventory to initialize
     */
    private static void initializeBankInventory(Inventory inventory) {
        ItemStack glass = createItem(Material.THIN_GLASS, " ", null);
        for (int i = BANK_CONTENT_SIZE; i < BANK_SIZE; i++) {
            inventory.setItem(i, glass);
        }
        inventory.setItem(BANK_SIZE - 9, createItem(Material.ARROW, ChatColor.GREEN + "Previous Page", null));
        inventory.setItem(BANK_SIZE - 1, createItem(Material.ARROW, ChatColor.GREEN + "Next Page", null));
    }

    /* Event Handlers */

    /**
     * Handle right-clicking on an ender chest to open the bank
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK ||
                event.getClickedBlock().getType() != Material.ENDER_CHEST) {
            return;
        }

        Player player = event.getPlayer();

        // Check for restrictions
        if (DeployCommand.patchlockdown) {
            player.sendMessage(ChatColor.RED + "Banks are currently locked down for maintenance.");
            event.setCancelled(true);
            return;
        }

        if (Duels.duelers.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "You cannot access your bank while in a duel.");
            event.setCancelled(true);
            return;
        }

        // Prevent default chest opening
        event.setCancelled(true);

        // Don't reopen if already viewing bank
        if (player.getOpenInventory().getTitle().contains(BANK_TITLE_PREFIX)) {
            return;
        }

        // Open bank inventory
        try {
            Inventory bankInv = getBank(player, 1);
            player.openInventory(bankInv);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "There was an error opening your bank.");
            PracticeServer.log.log(Level.SEVERE, "[Banks] Error opening bank for player " + player.getName(), e);
        }
    }

    /**
     * Handle inventory open event to update the bank UI
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        String title = inventory.getTitle();

        if (title.contains(BANK_TITLE_PREFIX) && !title.contains("Guild")) {
            updateBankUI(inventory, player);
        }
    }

    /**
     * Update the bank UI elements
     */
    private void updateBankUI(Inventory inventory, Player player) {
        // Update the gem balance display
        inventory.setItem(BANK_SIZE - 5, createGemBankItem(player));
    }

    /**
     * Handle inventory close event to save the bank
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        String title = event.getInventory().getTitle();
        if (title.contains(BANK_TITLE_PREFIX) && !title.contains("Guild")) {
            Player player = (Player) event.getPlayer();

            try {
                int page = Integer.parseInt(title.substring(title.indexOf("(") + 1, title.indexOf("/")));

                // Use async task to save the bank without blocking the main thread
                Bukkit.getScheduler().runTaskAsynchronously(PracticeServer.getInstance(), () ->
                        saveBank(event.getInventory(), player.getUniqueId(), page));

                // Remove from viewing map
                BANK_VIEW_MAP.remove(player.getUniqueId());
            } catch (Exception e) {
                PracticeServer.log.log(Level.SEVERE, "[Banks] Error saving bank on close for player " + player.getName(), e);
            }
        }
    }

    /**
     * Handle inventory click events within the bank
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        String title = inventory.getTitle();

        // Handle bank note merging
        if (isBankNote(event.getCurrentItem()) && isBankNote(event.getCursor())) {
            handleBankNoteMerge(event, player);
            return;
        }

        // Only proceed for bank inventories
        if (!title.contains(BANK_TITLE_PREFIX) || title.contains("Guild")) {
            return;
        }

        // Determine click location and action
        if (event.getRawSlot() >= BANK_SIZE) {
            // Clicked in player inventory
            if (event.isShiftClick()) {
                event.setCancelled(true);
                handleShiftClickToBank(event, player);
            }
        } else if (event.getRawSlot() < BANK_CONTENT_SIZE) {
            // Clicked in bank content area
            if (event.isShiftClick()) {
                event.setCancelled(true);
                handleShiftClickFromBank(event, player);
            }
        } else {
            // Clicked in bank UI (bottom row)
            event.setCancelled(true);
            handleBankUIClick(event, player);
        }
    }

    /**
     * Handle merging of bank notes
     */
    private void handleBankNoteMerge(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        try {
            int firstValue = extractGemValue(currentItem);
            int secondValue = extractGemValue(cursorItem);

            if (firstValue <= 0 || secondValue <= 0) {
                player.sendMessage(ChatColor.RED + "Invalid bank note value detected.");
                return;
            }

            int totalValue = firstValue + secondValue;
            ItemStack mergedNote = Money.createBankNote(totalValue);

            event.setCurrentItem(mergedNote);
            event.setCursor(null);

            player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 1.0f, 1.2f);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to merge bank notes.");
            PracticeServer.log.log(Level.WARNING, "[Banks] Error merging bank notes for player " + player.getName(), e);
        }
    }

    /**
     * Handle shift-clicking an item from player inventory to bank
     */
    private void handleShiftClickToBank(InventoryClickEvent event, Player player) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Inventory bankInv = event.getInventory();

        // Check if the item is currency
        if (isCurrencyItem(clickedItem)) {
            // Process currency deposit
            processCurrencyDeposit(player, clickedItem);
        } else {
            // Check if there's space in the bank
            int firstEmpty = findFirstEmptyInBank(bankInv);

            if (firstEmpty != -1) {
                // Move the item to the bank
                ItemStack itemToAdd = clickedItem.clone();
                bankInv.setItem(firstEmpty, itemToAdd);
                event.setCurrentItem(null);
            } else {
                player.sendMessage(ChatColor.RED + "Your bank is full. Unable to deposit items.");
            }
        }

        player.updateInventory();
    }

    /**
     * Find the first empty slot in the bank (excluding UI elements)
     */
    private int findFirstEmptyInBank(Inventory bankInv) {
        for (int i = 0; i < BANK_CONTENT_SIZE; i++) {
            if (bankInv.getItem(i) == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Handle shift-clicking an item from bank to player inventory
     */
    private void handleShiftClickFromBank(InventoryClickEvent event, Player player) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        // Try to add the item to the player's inventory
        HashMap<Integer, ItemStack> notAdded = player.getInventory().addItem(clickedItem.clone());

        if (notAdded.isEmpty()) {
            // All items were added to player inventory
            event.setCurrentItem(null);
        } else {
            // Some items couldn't be added, update the bank slot with remaining items
            clickedItem.setAmount(notAdded.get(0).getAmount());
        }

        player.updateInventory();
    }

    /**
     * Handle clicks on the bank UI elements (bottom row)
     */
    private void handleBankUIClick(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();

        // Gem balance - open withdrawal prompt
        if (slot == BANK_SIZE - 5 && event.getClick() == ClickType.RIGHT) {
            promptForWithdraw(player);
        }
        // Previous page
        else if (slot == BANK_SIZE - 9) {
            changeBankPage(player, -1);
        }
        // Next page
        else if (slot == BANK_SIZE - 1) {
            changeBankPage(player, 1);
        }
    }

    /**
     * Change to a different bank page
     */
    private void changeBankPage(Player player, int delta) {
        try {
            String title = player.getOpenInventory().getTitle();
            int currentPage = Integer.parseInt(title.substring(title.indexOf("(") + 1, title.indexOf("/")));
            int newPage = currentPage + delta;

            PersistentPlayer persistentPlayer = PersistentPlayers.get(player.getUniqueId());

            if (newPage < 1 || newPage > persistentPlayer.bankpages) {
                player.sendMessage(ChatColor.RED + "You do not have access to that bank page.");
                return;
            }

            // Save current page before switching
            int finalCurrentPage = currentPage;
            Bukkit.getScheduler().runTaskAsynchronously(PracticeServer.getInstance(), () ->
                    saveBank(player.getOpenInventory().getTopInventory(), player.getUniqueId(), finalCurrentPage));

            // Open new page
            player.closeInventory();
            player.openInventory(getBank(player, newPage));
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.25f);

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Could not change bank page.");
            PracticeServer.log.log(Level.WARNING, "[Banks] Error changing bank page for player " + player.getName(), e);
        }
    }

    /**
     * Process the deposit of currency items
     */
    private void processCurrencyDeposit(Player player, ItemStack item) {
        int totalAmount = 0;

        try {
            if (item.getType() == Material.EMERALD) {
                // Regular gems
                totalAmount = item.getAmount();
                player.getInventory().removeItem(item);
            } else if (item.getType() == Material.PAPER && isBankNote(item)) {
                // Bank note
                totalAmount = extractGemValue(item);
                player.getInventory().removeItem(item);
            } else if (item.getType() == Material.INK_SACK && item.getDurability() == 0) {
                // Gem pouch
                totalAmount = processGemPouch(player, item);
            }

            if (totalAmount > 0) {
                Economy.depositPlayer(player.getUniqueId(), totalAmount);
                updatePlayerBalance(player, totalAmount, true);
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to process currency deposit.");
            PracticeServer.log.log(Level.WARNING, "[Banks] Error processing currency deposit for player " + player.getName(), e);
        }
    }

    /**
     * Process a gem pouch deposit
     */
    private int processGemPouch(Player player, ItemStack pouch) {
        try {
            int currentValue = GemPouches.getCurrentValue(pouch);

            if (currentValue > 0) {
                // Empty the pouch and keep it in inventory
                GemPouches.setPouchBal(pouch, 0);

                // Find and update the pouch in inventory
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null && item.equals(pouch)) {
                        player.getInventory().setItem(i, pouch);
                        break;
                    }
                }

                return currentValue;
            } else if (player.getOpenInventory().getTopInventory().firstEmpty() != -1) {
                // Empty pouch, move to bank if there's space
                player.getOpenInventory().getTopInventory().addItem(pouch);
                player.getInventory().removeItem(pouch);
            }
        } catch (Exception e) {
            PracticeServer.log.log(Level.WARNING, "[Banks] Error processing gem pouch for player " + player.getName(), e);
        }

        return 0;
    }

    /**
     * Display the withdraw prompt to the player
     */
    private void promptForWithdraw(Player player) {
        int balance = Economy.getBalance(player.getUniqueId());

        StringUtil.sendCenteredMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Current Balance: " +
                ChatColor.GREEN + balance + " GEM(s)");

        if (balance <= 0) {
            StringUtil.sendCenteredMessage(player, ChatColor.RED + "You have no gems to withdraw.");
            return;
        }

        WITHDRAW_PROMPT.add(player.getUniqueId());
        StringUtil.sendCenteredMessage(player, ChatColor.GRAY + "Please enter the amount you'd like to CONVERT into a gem note. " +
                "Alternatively, type " + ChatColor.RED + "'cancel'" + ChatColor.GRAY + " to void this operation.");
        player.closeInventory();
    }

    /**
     * Handle chat input for withdraw prompt
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!WITHDRAW_PROMPT.contains(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();

        // Handle cancel command
        if (message.equalsIgnoreCase("cancel")) {
            WITHDRAW_PROMPT.remove(player.getUniqueId());
            StringUtil.sendCenteredMessage(player, ChatColor.RED + "Withdraw operation - " + ChatColor.BOLD + "CANCELLED");
            return;
        }

        // Check if player has a balance
        if (Economy.getBalance(player.getUniqueId()) <= 0) {
            WITHDRAW_PROMPT.remove(player.getUniqueId());
            StringUtil.sendCenteredMessage(player, ChatColor.RED + "Your bank balance is zero. Withdrawal cannot be processed.");
            return;
        }

        // Parse amount
        try {
            int amount = Integer.parseInt(message);
            // Schedule processing on the main thread for safety
            Bukkit.getScheduler().runTask(PracticeServer.getInstance(), () -> processWithdraw(player, amount));
        } catch (NumberFormatException e) {
            StringUtil.sendCenteredMessage(player, ChatColor.RED + "Please enter a NUMBER, the amount you'd like to WITHDRAW " +
                    "from your bank account. Or type 'cancel' to void the withdrawal.");
        }
    }

    /**
     * Process the withdrawal request
     */
    private void processWithdraw(Player player, int amount) {
        if (!WITHDRAW_PROMPT.contains(player.getUniqueId())) {
            return; // Player may have disconnected or operation was cancelled
        }

        int balance = Economy.getBalance(player.getUniqueId());

        if (amount <= 0) {
            StringUtil.sendCenteredMessage(player, ChatColor.RED + "You must enter a POSITIVE amount.");
        } else if (amount > balance) {
            StringUtil.sendCenteredMessage(player, ChatColor.GRAY + "You cannot withdraw more GEMS than you have stored. " +
                    "Current balance: " + balance + " GEM(s)");
        } else {
            WITHDRAW_PROMPT.remove(player.getUniqueId());

            // Process the withdrawal
            Economy.withdrawPlayer(player.getUniqueId(), amount);
            givePlayerBankNote(player, amount);
            updatePlayerBalance(player, amount, false);
        }
    }

    /**
     * Give a bank note to a player
     */
    private void givePlayerBankNote(Player player, int amount) {
        ItemStack bankNote = Money.createBankNote(amount);

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(bankNote);
        } else {
            // Drop at player's feet if inventory is full
            player.getWorld().dropItemNaturally(player.getLocation(), bankNote);
            player.sendMessage(ChatColor.YELLOW + "Your inventory was full. The bank note was dropped at your feet.");
        }
    }

    /**
     * Update the player's balance and display
     */
    private void updatePlayerBalance(Player player, int amount, boolean isDeposit) {
        try {
            // Display messages
            String prefix = isDeposit ? "&a+" : "&c-";
            StringUtil.sendCenteredMessage(player, prefix + amount + (isDeposit ? "&a&lG" : "&c&lG") +
                    " &7➜ " + (isDeposit ? "Your Bank" : "Your Inventory"));

            int newBalance = Economy.getBalance(player.getUniqueId());
            StringUtil.sendCenteredMessage(player, "&a&lNew Balance: &a" + newBalance + " &aGEM(s)");

            // Play sound effect
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            // Update bank UI if open
            if (player.getOpenInventory().getTitle().contains(BANK_TITLE_PREFIX)) {
                player.getOpenInventory().setItem(BANK_SIZE - 5, createGemBankItem(player));
            }

            // Update inventory
            player.updateInventory();

            // Save data if not using database
            if (!PracticeServer.DATABASE) {
                nonStaticConfig.get().set(player.getUniqueId() + ".Economy.Money Balance", newBalance);
                nonStaticConfig.save();
            }
        } catch (Exception e) {
            PracticeServer.log.log(Level.WARNING, "[Banks] Error updating player balance for " + player.getName(), e);
        }
    }

    /* Utility Methods */

    /**
     * Check if an item is a currency item
     */
    private boolean isCurrencyItem(ItemStack item) {
        if (item == null) return false;

        return item.getType() == Material.EMERALD ||
                isBankNote(item) ||
                (item.getType() == Material.INK_SACK && item.getDurability() == 0);
    }

    /**
     * Check if an item is a bank note
     */
    private boolean isBankNote(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().equals(BANK_NOTE_NAME) &&
                meta.hasLore() && !meta.getLore().isEmpty() &&
                meta.getLore().get(0).contains("Value");
    }

    /**
     * Extract the gem value from a bank note
     */
    private int extractGemValue(ItemStack item) {
        if (!isBankNote(item)) return 0;

        try {
            List<String> lore = item.getItemMeta().getLore();
            String valueLine = ChatColor.stripColor(lore.get(0));
            String[] parts = valueLine.split(": ");
            if (parts.length < 2) return 0;

            String valueStr = parts[1].split(" Gems")[0];
            return Integer.parseInt(valueStr);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Create the gem balance item for the bank UI
     */
    public static ItemStack createGemBankItem(Player player) {
        int balance = Economy.getBalance(player.getUniqueId());
        return createItem(
                Material.EMERALD,
                ChatColor.GREEN.toString() + balance + ChatColor.GREEN + ChatColor.BOLD + " GEM(s)",
                Collections.singletonList(ChatColor.GRAY + "Right Click to create " + ChatColor.GREEN + "A GEM NOTE")
        );
    }

    /**
     * Create an ItemStack with custom name and lore
     */
    private static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a gem item with specified amount
     */
    public static ItemStack makeGems(int amount) {
        if (amount <= 0) return null;

        ItemStack gem = new ItemStack(Material.EMERALD, Math.min(amount, 64)); // Cap at stack size

        ItemMeta meta = gem.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Gem");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "The currency of Andalucia"));

        gem.setItemMeta(meta);
        return gem;
    }
}