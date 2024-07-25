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

public class Banks implements Listener {
    public static final int BANK_SIZE = 54;
    public static final Map<UUID, UUID> BANK_SEE = new ConcurrentHashMap<>();
    public static final Set<UUID> WITHDRAW_PROMPT = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static final Map<UUID, Inventory> TEMP_BANKS = new ConcurrentHashMap<>();

    public void onEnable() {
        PracticeServer.log.info("[Banks] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[Banks] has been disabled.");
        saveBanks();
    }

    private void saveBanks() {
        for (Map.Entry<UUID, Inventory> entry : TEMP_BANKS.entrySet()) {
            saveBank(entry.getValue(), entry.getKey(), 1);
        }
        TEMP_BANKS.clear();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (DeployCommand.patchlockdown || e.getAction() != Action.RIGHT_CLICK_BLOCK
                || e.getClickedBlock().getType() != Material.ENDER_CHEST || Duels.duelers.containsKey(e.getPlayer())) {
            return;
        }

        e.setCancelled(true);
        Player p = e.getPlayer();
        if (!p.getOpenInventory().getTitle().contains("Bank Chest")) {
            Inventory inv = getBank(p, 1);
            p.openInventory(inv);
            p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory().getTitle().contains("Bank Chest") && !e.getInventory().getTitle().contains("Guild")) {
            Player p = (Player) e.getPlayer();
            int page = Integer.parseInt(e.getInventory().getTitle().substring(12, 13));
            Bukkit.getScheduler().runTaskAsynchronously(PracticeServer.getInstance(), () -> saveBank(e.getInventory(), p.getUniqueId(), page));
            BANK_SEE.remove(p.getUniqueId());
        }
    }

    private void saveBank(Inventory inv, UUID playerUUID, int page) {
        if (PracticeServer.DATABASE) {
            SQLMain.saveBank(inv, playerUUID, page);
        } else {
            TEMP_BANKS.put(playerUUID, inv);
        }
    }

    public static void resetTempBanks() {
        TEMP_BANKS.clear();
    }

    public static Inventory getBank(Player p, int page) {
        UUID playerUUID = BANK_SEE.getOrDefault(p.getUniqueId(), p.getUniqueId());

        if (PracticeServer.DATABASE && PracticeServer.getRaceMinigame().getGameState() == MinigameState.NONE) {
            return SQLMain.getBank(playerUUID, page);
        }

        return TEMP_BANKS.computeIfAbsent(playerUUID, k -> Bukkit.createInventory(null, BANK_SIZE, "Bank Chest (1/1)"));
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getInventory().getTitle().contains("Bank Chest") && !e.getInventory().getTitle().contains("Guild")) {
            Inventory inv = e.getInventory();
            setupBankInventory(inv, (Player) e.getPlayer());
        }
    }

    private void setupBankInventory(Inventory inv, Player player) {
        ItemStack glass = createNamedItem(Material.THIN_GLASS, " ");
        for (int i = BANK_SIZE - 9; i < BANK_SIZE; i++) {
            inv.setItem(i, glass);
        }
        inv.setItem(BANK_SIZE - 5, getGemBankItem(player));
        inv.setItem(BANK_SIZE - 1, createNamedItem(Material.ARROW, ChatColor.GREEN + "Next Page"));
        inv.setItem(BANK_SIZE - 9, createNamedItem(Material.ARROW, ChatColor.GREEN + "Previous Page"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.PAPER
                && e.getCurrentItem().getItemMeta().hasLore() && e.getCursor().getType() == Material.PAPER
                && e.getCursor().getItemMeta().hasLore()) {
            e.setCancelled(true);
            final Player p = (Player) e.getWhoClicked();

            final int first = this.getGems(e.getCurrentItem());
            final int second = this.getGems(e.getCursor());
            final ItemStack gem = new ItemStack(Material.PAPER);
            final ItemMeta im = gem.getItemMeta();
            im.setDisplayName(ChatColor.GREEN + "Bank Note");
            im.setLore(Arrays.asList(
                    new StringBuilder().append(ChatColor.WHITE).append(ChatColor.BOLD).append("Value: ")
                            .append(ChatColor.WHITE).append(first + second).append(" Gems").toString(),
                    ChatColor.GRAY + "Exchange at any bank for GEM(s)"));
            gem.setItemMeta(im);
            e.setCurrentItem(gem);
            e.setCursor(null);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 1.0f, 1.2f);
        }
        if (!(e.getWhoClicked() instanceof Player) || !e.getInventory().getTitle().contains("Bank Chest") || e.getInventory().getTitle().contains("Guild")) {
            return;
        }

        Player p = (Player) e.getWhoClicked();

        if (e.getRawSlot() >= BANK_SIZE) {
            // Clicked in player inventory
            if (e.isShiftClick()) {
                e.setCancelled(true);
                handleShiftClickToBank(e, p);
            }
            // Normal clicks are allowed by default
        } else if (e.getRawSlot() < BANK_SIZE - 9) {
            // Clicked in bank inventory (excluding bottom row)
            if (e.isShiftClick()) {
                e.setCancelled(true);
                handleShiftClickFromBank(e, p);
            }
            // Normal clicks are allowed by default
        } else {
            // Clicked in bottom row of bank inventory
            e.setCancelled(true);
            handleBottomRowClick(e, p);
        }
    }

    private void handleShiftClickToBank(InventoryClickEvent e, Player p) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;

        Inventory bankInv = e.getInventory();
        int firstEmpty = bankInv.firstEmpty();

        if (isCurrencyItem(clickedItem)) {
            handleCurrencyDeposit(p, clickedItem);
        } else {
            if (firstEmpty != -1 && firstEmpty < BANK_SIZE - 9) {
                // Move the entire stack to the bank if there's space
                bankInv.addItem(clickedItem.clone());
                e.setCurrentItem(null);
            } else {
                // Bank is full, display a message to the player
                p.sendMessage(ChatColor.RED + "Your bank is full. Unable to deposit the item.");
            }
        }

        p.updateInventory();
    }

    private void handleShiftClickFromBank(InventoryClickEvent e, Player p) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;

        // Try to add the item to the player's inventory
        HashMap<Integer, ItemStack> notAdded = p.getInventory().addItem(clickedItem.clone());

        if (notAdded.isEmpty()) {
            // All items were added to the player's inventory
            e.setCurrentItem(null);
        } else {
            // Some items couldn't be added, update the bank slot with the remaining items
            clickedItem.setAmount(notAdded.get(0).getAmount());
        }
        p.updateInventory();
    }

    private void handleBottomRowClick(InventoryClickEvent e, Player p) {
        if (e.getSlot() == BANK_SIZE - 5 && e.getClick() == ClickType.RIGHT) {
            promptForWithdraw(p);
        } else if (e.getSlot() == BANK_SIZE - 9) {
            changeBankPage(p, -1);
        } else if (e.getSlot() == BANK_SIZE - 1) {
            changeBankPage(p, 1);
        }
    }

    private void handleCurrencyDeposit(Player p, ItemStack item) {
        int totalAmount = 0;

        if (item.getType() == Material.EMERALD) {
            totalAmount = item.getAmount();
            p.getInventory().removeItem(item);
        } else if (item.getType() == Material.PAPER) {
            totalAmount = getGems(item);
            p.getInventory().removeItem(item);
        } else if (item.getType() == Material.INK_SACK && item.getDurability() == 0) {
            totalAmount = handleGemSack(p, item);
        } else {
            return;
        }

        if (totalAmount > 0) {
            Economy.depositPlayer(p.getUniqueId(), totalAmount);
            updatePlayerBalance(p, totalAmount, true);
        }

        p.updateInventory();
    }

    private int handleGemSack(Player p, ItemStack sack) {
        int currentValue = GemPouches.getCurrentValue(sack);
        if (currentValue > 0) {
            GemPouches.setPouchBal(sack, 0);
            p.getInventory().setItem(p.getInventory().first(sack), sack); // Update the sack in the inventory
            return currentValue;
        } else if (p.getOpenInventory().getTopInventory().firstEmpty() != -1) {
            // If the sack is empty and there's room in the bank, move it to the bank
            p.getOpenInventory().getTopInventory().addItem(sack);
            p.getInventory().removeItem(sack);
        }
        return 0;
    }


    private void promptForWithdraw(Player p) {
        StringUtil.sendCenteredMessage(p, ChatColor.GREEN + "" + ChatColor.BOLD + "Current Balance: " + ChatColor.GREEN + Economy.getBalance(p.getUniqueId()) + " GEM(s)");
        WITHDRAW_PROMPT.add(p.getUniqueId());
        StringUtil.sendCenteredMessage(p, ChatColor.GRAY + "Please enter the amount you'd like to CONVERT into a gem note. Alternatively, type " + ChatColor.RED + "'cancel'" + ChatColor.GRAY + " to void this operation.");
        p.closeInventory();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!WITHDRAW_PROMPT.contains(p.getUniqueId())) {
            return;
        }

        e.setCancelled(true);
        String message = e.getMessage();

        if (message.equalsIgnoreCase("cancel")) {
            WITHDRAW_PROMPT.remove(p.getUniqueId());
            StringUtil.sendCenteredMessage(p, ChatColor.RED + "Withdraw operation - " + ChatColor.BOLD + "CANCELLED");
            return;
        }

        // Check if the player's bank balance is zero
        if (Economy.getBalance(p.getUniqueId()) == 0) {
            WITHDRAW_PROMPT.remove(p.getUniqueId());
            StringUtil.sendCenteredMessage(p, ChatColor.RED + "Your bank balance is zero. Withdrawal cannot be processed.");
            return;
        }

        try {
            int amount = Integer.parseInt(message);
            handleWithdraw(p, amount);
        } catch (NumberFormatException ex) {
            StringUtil.sendCenteredMessage(p, ChatColor.RED + "Please enter a NUMBER, the amount you'd like to WITHDRAW from your bank account. Or type 'cancel' to void the withdrawal.");
        }
    }

    private void handleWithdraw(Player p, int amount) {
        if (amount <= 0) {
            StringUtil.sendCenteredMessage(p, ChatColor.RED + "You must enter a POSITIVE amount.");
        } else if (amount > Economy.getBalance(p.getUniqueId())) {
            StringUtil.sendCenteredMessage(p, ChatColor.GRAY + "You cannot withdraw more GEMS than you have stored.");
        } else {
            WITHDRAW_PROMPT.remove(p.getUniqueId());
            Economy.withdrawPlayer(p.getUniqueId(), amount);
            givePlayerBankNote(p, amount);
            updatePlayerBalance(p, amount, false);
        }
    }

    private void givePlayerBankNote(Player p, int amount) {
        ItemStack bankNote = Money.createBankNote(amount);
        if (p.getInventory().firstEmpty() != -1) {
            p.getInventory().addItem(bankNote);
        } else {
            p.getWorld().dropItemNaturally(p.getLocation(), bankNote);
            p.sendMessage(ChatColor.YELLOW + "Your inventory was full. The bank note was dropped at your feet.");
        }
    }

    private void updatePlayerBalance(Player p, int amount, boolean isDeposit) {
        String prefix = isDeposit ? "&a+" : "&c-";
        StringUtil.sendCenteredMessage(p, prefix + amount + (isDeposit ? "&a&lG" : "&c&lG") + " &7➜ " + (isDeposit ? "Your Bank" : "Your Inventory"));
        StringUtil.sendCenteredMessage(p, "&a&lNew Balance: &a" + Economy.getBalance(p.getUniqueId()) + " &aGEM(s)");
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        // Update the gem balance display in the bank inventory if it's open
        if (p.getOpenInventory().getTitle().contains("Bank Chest")) {
            p.getOpenInventory().setItem(BANK_SIZE - 5, getGemBankItem(p));
        }

        p.updateInventory();

        if (!PracticeServer.DATABASE) {
            nonStaticConfig.get().set(p.getUniqueId() + ".Economy.Money Balance", Economy.getBalance(p.getUniqueId()));
            nonStaticConfig.save();
        }
    }

    private void changeBankPage(Player p, int delta) {
        int currentPage = Integer.parseInt(p.getOpenInventory().getTitle().substring(12, 13));
        int newPage = currentPage + delta;
        PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());

        if (newPage < 1 || newPage > pp.bankpages) {
            StringUtil.sendCenteredMessage(p, ChatColor.RED + "You do not have access to that bank page.");
            return;
        }

        p.closeInventory();
        p.openInventory(getBank(p, newPage));
        p.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.25f);
    }

    private boolean isCurrencyItem(ItemStack item) {
        return item.getType() == Material.EMERALD ||
                (item.getType() == Material.PAPER && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().get(0).contains("Value")) ||
                (item.getType() == Material.INK_SACK && item.getDurability() == 0);
    }

    private int getGems(ItemStack item) {
        if (item != null && item.getType() == Material.PAPER && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            if (!lore.isEmpty() && lore.get(0).contains("Value")) {
                try {
                    String line = ChatColor.stripColor(lore.get(0));
                    return Integer.parseInt(line.split(": ")[1].split(" Gems")[0]);
                } catch (Exception e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public static ItemStack getGemBankItem(Player player) {
        return createNamedItem(Material.EMERALD,
                ChatColor.GREEN.toString() + Economy.getBalance(player.getUniqueId()) + ChatColor.GREEN + ChatColor.BOLD + " GEM(s)",
                Arrays.asList(ChatColor.GRAY + "Right Click to create " + ChatColor.GREEN + "A GEM NOTE"));
    }

    private static ItemStack createNamedItem(Material material, String name) {
        return createNamedItem(material, name, null);
    }

    private static ItemStack createNamedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeGems(final int amount) {
        final ItemStack i = new ItemStack(Material.EMERALD, amount);
        final List<String> new_lore = new ArrayList<String>(
                Arrays.asList(String.valueOf(ChatColor.GRAY.toString()) + "The currency of Andalucia"));
        final ItemMeta im = i.getItemMeta();
        im.setLore(new_lore);
        im.setDisplayName(String.valueOf(ChatColor.WHITE.toString()) + "Gem");
        i.setItemMeta(im);
        i.setAmount(amount);
        return i;
    }
}