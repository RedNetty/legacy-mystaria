package me.retrorealms.practiceserver.mechanics.player.trading;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.VanishCommand;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.money.Banks;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.vendors.MerchantMechanics;
import me.retrorealms.practiceserver.utils.JsonBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Trading implements Listener {
    public static ConcurrentHashMap<Player, Player> trade_map;
    public static ConcurrentHashMap<String, ItemStack> destroying_soulbound;
    static Inventory TradeWindowTemplate;
    static ConcurrentHashMap<Player, Player> trade_partners;
    static ConcurrentHashMap<Player, Inventory> trade_secure;
    static ConcurrentHashMap<String, Long> last_inventory_close;

    static {
        Trading.trade_map = new ConcurrentHashMap<Player, Player>();
        Trading.trade_partners = new ConcurrentHashMap<Player, Player>();
        Trading.trade_secure = new ConcurrentHashMap<Player, Inventory>();
        Trading.last_inventory_close = new ConcurrentHashMap<String, Long>();
        Trading.destroying_soulbound = new ConcurrentHashMap<String, ItemStack>();
    }

    public ItemStack gray_button;
    public ItemStack green_button;
    Logger log;
    ItemStack divider;
    Trading tm;

    public Trading() {
        this.log = Logger.getLogger("Minecraft");
        this.divider = new ItemStack(Material.THIN_GLASS, 1);
        this.gray_button = setIinfo(new ItemStack(Material.INK_SACK, 1, (short) 8),
                String.valueOf(ChatColor.YELLOW.toString()) + "Click to ACCEPT Trade", "");
        this.green_button = setIinfo(new ItemStack(Material.INK_SACK, 1, (short) 10),
                String.valueOf(ChatColor.GREEN.toString()) + "Trade ACCEPTED.",
                String.valueOf(ChatColor.GRAY.toString()) + "Modify the trade to unaccept.");
        this.tm = null;
    }

    public static ItemStack setIinfo(final ItemStack orig_i, final String name, final String desc) {
        final List<String> new_lore = new ArrayList<String>();
        String[] split;
        for (int length = (split = desc.split(",")).length, i = 0; i < length; ++i) {
            final String s = split[i];
            if (s.length() > 1) {
                new_lore.add(s);
            }
        }
        final ItemMeta im = orig_i.getItemMeta();
        im.setLore(new_lore);
        im.setDisplayName(name);
        orig_i.setItemMeta(im);
        return orig_i;
    }

    @EventHandler
    public void onTradeClick(InventoryClickEvent e) {
        if (!(e.getClick() == ClickType.LEFT) && trade_map.containsKey(e.getWhoClicked())) {
            e.setCancelled(true);
            //e.getWhoClicked().sendMessage(ChatColor.RED + "ez fix lul");
        }
    }


    @SuppressWarnings("unused")
    public static Player getTarget(final Player trader) {
        final List<Entity> nearbyE = trader.getNearbyEntities(4.0, 4.0, 4.0);
        final ArrayList<Player> livingE = new ArrayList<Player>();
        for (final Entity e : nearbyE) {
            if (e.getType() == EntityType.PLAYER && !e.hasMetadata("NPC")) {
                livingE.add((Player) e);
            }
        }
        Player target = null;
        final BlockIterator bItr = new BlockIterator(trader, 4);
        if (bItr == null) {
            return null;
        }
        while (bItr.hasNext()) {
            final Block block = bItr.next();
            final int bx = block.getX();
            final int by = block.getY();
            final int bz = block.getZ();
            for (final LivingEntity e2 : livingE) {
                if (e2 instanceof Player && VanishCommand.vanished.contains(e2.getName())) {
                    continue;
                }
                final Location loc = e2.getLocation();
                final double ex = loc.getX();
                final double ey = loc.getY();
                final double ez = loc.getZ();
                if (bx - 0.75 <= ex && ex <= bx + 1.75 && bz - 0.75 <= ez && ez <= bz + 1.75 && by - 1 <= ey
                        && ey <= by + 2.5) {
                    target = (Player) e2;
                    break;
                }
            }
        }
        return target;
    }

    public static String generateTitle(final String lPName, final String rPName) {
        String title;
        for (title = "  " + lPName; title.length() + rPName.length() < 28; title = String.valueOf(title) + " ") {
        }
        String return_string;
        title = (return_string = String.valueOf(title) + rPName);
        if (return_string.length() >= 32) {
            return_string = return_string.substring(0, 32);
        }
        return return_string;
    }

    public void onEnable() {
        this.tm = this;
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        final ItemMeta im = this.divider.getItemMeta();
        im.setDisplayName(" ");
        this.divider.setItemMeta(im);
        new BukkitRunnable() {
            public void run() {
                Trading.this.doOverheadEffect();
            }
        }.runTaskTimerAsynchronously(PracticeServer.plugin, 40L, 20L);
        this.loadTradeTemplate();
        this.log.info("[Trading] has been enabled.");
    }

    public void onDisable() {
        this.log.info("[Trading] has been disabled.");
    }

    public void doOverheadEffect() {
        for (@SuppressWarnings("unused") final Player pl : Trading.trade_map.keySet()) {
            try {
                // ParticleEffect.sendToLocation(ParticleEffect.VILLAGER_HAPPY,
                // pl.getLocation().clone().add(0.0, 2.0, 0.0), new
                // Random().nextFloat(), new Random().nextFloat(), new
                // Random().nextFloat(), 2.0f, 1);
            } catch (Exception ex) {
            }
        }
        for (final String s : MerchantMechanics.in_npc_shop) {
            try {
                if (Bukkit.getPlayer(s) == null) {
                    continue;
                }
            } catch (Exception ex2) {
            }
        }
    }

    public void loadTradeTemplate() {
        (Trading.TradeWindowTemplate = Bukkit.getServer().createInventory(null, 27)).setItem(4,
                this.divider);
        Trading.TradeWindowTemplate.setItem(13, this.divider);
        Trading.TradeWindowTemplate.setItem(22, this.divider);
        Trading.TradeWindowTemplate.setItem(0, new ItemStack(this.gray_button));
        Trading.TradeWindowTemplate.setItem(8, new ItemStack(this.gray_button));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerHit(final EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getDamage() <= 0.0 || e.isCancelled()) {
                return;
            }
            final Player closer = (Player) e.getEntity();
            final Player trade_partner = Trading.trade_map.get(closer);
            if (Trading.trade_map.containsKey(closer)) {
                boolean left_side = false;
                final Inventory tradeInv = closer.getOpenInventory().getTopInventory();
                if (Trading.trade_partners.containsKey(closer)) {
                    left_side = true;
                }
                if (Trading.trade_partners.containsKey(trade_partner)) {
                    left_side = false;
                }
                int slot_var = -1;
                if (left_side) {
                    while (slot_var <= 27) {
                        if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9
                                && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18
                                && slot_var != 19 && slot_var != 20 && slot_var != 21) {
                            continue;
                        }
                        final ItemStack i = tradeInv.getItem(slot_var);
                        if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                            continue;
                        }
                        if (i.getType() == Material.THIN_GLASS) {
                            continue;
                        }
                        closer.getInventory().setItem(closer.getInventory().firstEmpty(), i);
                    }
                    slot_var = -1;
                    while (slot_var <= 27) {
                        if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14
                                && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23
                                && slot_var != 24 && slot_var != 25 && slot_var != 26) {
                            continue;
                        }
                        final ItemStack i = tradeInv.getItem(slot_var);
                        if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                            continue;
                        }
                        if (i.getType() == Material.THIN_GLASS) {
                            continue;
                        }
                        trade_partner.getInventory().setItem(trade_partner.getInventory().firstEmpty(), i);
                    }
                }
                if (!left_side) {
                    while (slot_var <= 27) {
                        if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9
                                && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18
                                && slot_var != 19 && slot_var != 20 && slot_var != 21) {
                            continue;
                        }
                        final ItemStack i = tradeInv.getItem(slot_var);
                        if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                            continue;
                        }
                        if (i.getType() == Material.THIN_GLASS) {
                            continue;
                        }
                        trade_partner.getInventory().setItem(trade_partner.getInventory().firstEmpty(), i);
                    }
                    slot_var = -1;
                    while (slot_var <= 27) {
                        if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14
                                && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23
                                && slot_var != 24 && slot_var != 25 && slot_var != 26) {
                            continue;
                        }
                        final ItemStack i = tradeInv.getItem(slot_var);
                        if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                            continue;
                        }
                        if (i.getType() == Material.THIN_GLASS) {
                            continue;
                        }
                        closer.getInventory().setItem(closer.getInventory().firstEmpty(), i);
                    }
                }
                if (closer.getOpenInventory().getTopInventory().getName().contains(closer.getName())) {
                    closer.getOpenInventory().getTopInventory().clear();
                }
                if (trade_partner.getOpenInventory().getTopInventory().getName().contains(trade_partner.getName())) {
                    trade_partner.getOpenInventory().getTopInventory().clear();
                }
                Trading.trade_map.remove(closer);
                Trading.trade_map.remove(trade_partner);
                Trading.trade_partners.remove(closer);
                Trading.trade_partners.remove(trade_partner);
                Trading.trade_secure.remove(closer);
                Trading.trade_secure.remove(trade_partner);
                closer.closeInventory();
                trade_partner.closeInventory();
                closer.sendMessage(ChatColor.RED + "Trade cancelled, entered combat.");
                trade_partner.sendMessage(
                        new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append(closer.getName())
                                .append(ChatColor.RED).append(" entered combat, trade cancelled.").toString());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickup(final PlayerPickupItemEvent e) {
        final Player pl = e.getPlayer();
        if (Trading.trade_map.containsKey(pl) || pl.getOpenInventory().getTitle().contains(pl.getName())) {
            e.setCancelled(true);
        }
        if (Trading.last_inventory_close.containsKey(pl.getName())
                && System.currentTimeMillis() - Trading.last_inventory_close.get(pl.getName()) <= 2000L) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        final Player closer = e.getPlayer();
        final Player trade_partner = Trading.trade_map.get(closer);
        Trading.last_inventory_close.remove(closer.getName());
        if (Trading.trade_map.containsKey(closer)) {
            boolean left_side = false;
            final Inventory tradeInv = closer.getOpenInventory().getTopInventory();
            if (Trading.trade_partners.containsKey(closer)) {
                left_side = true;
            }
            if (Trading.trade_partners.containsKey(trade_partner)) {
                left_side = false;
            }
            int slot_var = -1;
            if (left_side) {
                while (slot_var <= 27) {
                    if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9
                            && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18 && slot_var != 19
                            && slot_var != 20 && slot_var != 21) {
                        continue;
                    }
                    ItemStack i = tradeInv.getItem(slot_var);
                    if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                        continue;
                    }
                    if (i.getType() == Material.THIN_GLASS) {
                        continue;
                    }
                    if (i.getType() == Material.EMERALD) {
                        i = Banks.makeGems(i.getAmount());
                    }
                    closer.getInventory().setItem(closer.getInventory().firstEmpty(), this.makeNormal(i));
                }
                slot_var = -1;
                while (slot_var <= 27) {
                    if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14
                            && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23 && slot_var != 24
                            && slot_var != 25 && slot_var != 26) {
                        continue;
                    }
                    ItemStack i = tradeInv.getItem(slot_var);
                    if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                        continue;
                    }
                    if (i.getType() == Material.THIN_GLASS) {
                        continue;
                    }
                    if (i.getType() == Material.EMERALD) {
                        i = Banks.makeGems(i.getAmount());
                    }
                    trade_partner.getInventory().setItem(trade_partner.getInventory().firstEmpty(), this.makeNormal(i));
                }
            }
            if (!left_side) {
                while (slot_var <= 27) {
                    if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9
                            && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18 && slot_var != 19
                            && slot_var != 20 && slot_var != 21) {
                        continue;
                    }
                    final ItemStack i = tradeInv.getItem(slot_var);
                    if (i == null || i.getType() == Material.AIR) {
                        continue;
                    }
                    if (MerchantMechanics.isTradeButton(i) | i.getType() == Material.THIN_GLASS) {
                        continue;
                    }
                    trade_partner.getInventory().setItem(trade_partner.getInventory().firstEmpty(), this.makeNormal(i));
                }
                slot_var = -1;
                while (slot_var <= 27) {
                    if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14
                            && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23 && slot_var != 24
                            && slot_var != 25 && slot_var != 26) {
                        continue;
                    }
                    ItemStack i = tradeInv.getItem(slot_var);
                    if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                        continue;
                    }
                    if (i.getType() == Material.THIN_GLASS) {
                        continue;
                    }
                    if (i.getType() == Material.EMERALD) {
                        i = Banks.makeGems(i.getAmount());
                    }
                    closer.getInventory().setItem(closer.getInventory().firstEmpty(), this.makeNormal(i));
                }
            }
            if (closer.getOpenInventory().getTopInventory().getName().contains(closer.getName())) {
                closer.getOpenInventory().getTopInventory().clear();
            }
            if (trade_partner.getOpenInventory().getTopInventory().getName().contains(trade_partner.getName())) {
                trade_partner.getOpenInventory().getTopInventory().clear();
            }
            trade_partner.closeInventory();
            Trading.trade_map.remove(closer);
            Trading.trade_map.remove(trade_partner);
            Trading.trade_partners.remove(closer);
            Trading.trade_partners.remove(trade_partner);
            Trading.trade_secure.remove(closer);
            Trading.trade_secure.remove(trade_partner);
            trade_partner.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                    .append(closer.getName()).append(ChatColor.RED).append(" logged out, trade cancelled.").toString());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onPlayerDropItem(final PlayerDropItemEvent e) {
        final Player trader = e.getPlayer();
        final Player tradie = getTarget(trader);
        if (tradie == null || (tradie.isOp() && !trader.isOp())) {
            return;
        }
        if(Duels.duelers.containsKey(trader) || Duels.duelers.containsKey(tradie)){
            trader.sendMessage(ChatColor.RED + "Error: Players cannot trade while in a duel");
            return;
        }
        tradie.getUniqueId();
        if (tradie.hasMetadata("no_trade") || trader.hasMetadata("no_trade")) {
            e.setCancelled(true);
            trader.updateInventory();
            return;
        }
        if (tradie.hasMetadata("NPC") || tradie.getPlayerListName().equalsIgnoreCase("")) {
            return;
        }
        final ItemStack being_dropped = e.getItemDrop().getItemStack();
        if (!Items.isItemTradeable(being_dropped)) {
            return;
        }
        if (e.getItemDrop().getItemStack().getType() == Material.WRITTEN_BOOK
                || e.getItemDrop().getItemStack().getType() == Material.QUARTZ
                || e.getItemDrop().getItemStack().getType() == Material.NETHER_STAR) {
            return;
        }
        if (e.isCancelled()) {
            return;
        }
        if (tradie != null) {
            if (!Toggles.isToggled(tradie, "Trading")) {
                trader.sendMessage(ChatColor.YELLOW + tradie.getName() + " has trading disabled.");
                e.setCancelled(true);
                return;
            }

            if (Trading.trade_map.containsKey(tradie)) {
                trader.sendMessage(ChatColor.YELLOW + tradie.getName() + " is already trading with someone else.");
                e.setCancelled(true);
                PracticeServer.plugin.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin,
                        new Runnable() {
                            @Override
                            public void run() {
                                trader.updateInventory();
                            }
                        }, 2L);
                return;
            }
            if (tradie.getOpenInventory().getTopInventory().getName().startsWith("Bank Chest")
                    || tradie.getOpenInventory().getTopInventory().getName().contains("@")
                    || tradie.getOpenInventory().getTopInventory().getName().equalsIgnoreCase("Loot Chest")
                    || tradie.getOpenInventory().getTopInventory().getName().equalsIgnoreCase("Collection Bin")
                    || tradie.getOpenInventory().getTopInventory().getName().equalsIgnoreCase("Chest")
                    || tradie.getOpenInventory().getTopInventory().getName().equalsIgnoreCase("Realm Material Store")
                    || tradie.getOpenInventory().getTopInventory().getName().contains("     ")
                    || tradie.getOpenInventory().getTopInventory().getName().contains("container.chest")
                    || tradie.getOpenInventory().getTopInventory().getName().contains("container.bigchest")) {
                trader.sendMessage(ChatColor.YELLOW + tradie.getName() + " is currently busy.");
                e.setCancelled(true);
                PracticeServer.plugin.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin,
                        new Runnable() {
                            @Override
                            public void run() {
                                trader.updateInventory();
                            }
                        }, 2L);
                return;
            }
            if (trader.hasMetadata("no_trade") || tradie.hasMetadata("no_trade")) {
                this.log.info("Skipping trade due to no_trade -- " + trader.getName() + " -> " + tradie.getName());
                e.setCancelled(true);
                trader.updateInventory();
                return;
            }
            e.setCancelled(true);
            trader.setMetadata("no_trade", new FixedMetadataValue(PracticeServer.plugin, true));
            tradie.setMetadata("no_trade", new FixedMetadataValue(PracticeServer.plugin, true));
            this.log.info("TRADE EVENT: " + trader.getName() + " -> " + tradie.getName());
            final Inventory TradeWindow = Bukkit.createInventory(null, 27,
                    generateTitle(trader.getName(), tradie.getName()));
            TradeWindow.setItem(4, this.divider);
            TradeWindow.setItem(13, this.divider);
            TradeWindow.setItem(22, this.divider);
            TradeWindow.setItem(0, setIinfo(new ItemStack(Material.INK_SACK, 1, (short) 8),
                    String.valueOf(ChatColor.YELLOW.toString()) + "Click to ACCEPT Trade", ""));
            TradeWindow.setItem(8, setIinfo(new ItemStack(Material.INK_SACK, 1, (short) 8),
                    String.valueOf(ChatColor.YELLOW.toString()) + "Click to ACCEPT Trade", ""));
            trader.setItemOnCursor(new ItemStack(Material.AIR, 1));
            if (tradie.getItemOnCursor() != null) {
                if (Items.isItemTradeable(tradie.getItemOnCursor())) {
                    TradeWindow.setItem(5, this.makeUnique(tradie.getItemOnCursor()));
                } else {
                    tradie.getInventory().addItem(tradie.getItemOnCursor());
                }
                tradie.setItemOnCursor(new ItemStack(Material.AIR));
            }
            trader.closeInventory();
            tradie.closeInventory();
            Trading.trade_partners.put(trader, tradie);
            Trading.trade_map.put(trader, tradie);
            Trading.trade_map.put(tradie, trader);
            trader.openInventory(TradeWindow);
            tradie.openInventory(TradeWindow);
            trader.playSound(trader.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 0.8f);
            tradie.playSound(tradie.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 0.8f);
            trader.sendMessage(
                    ChatColor.YELLOW + "Trading with " + ChatColor.BOLD + tradie.getName() + ChatColor.YELLOW + "...");
            tradie.sendMessage(
                    ChatColor.YELLOW + "Trading with " + ChatColor.BOLD + trader.getName() + ChatColor.YELLOW + "...");
            trader.updateInventory();
            new BukkitRunnable() {
                public void run() {
                    if(trader !=null)
                    trader.removeMetadata("no_trade", PracticeServer.plugin);
                    tradie.removeMetadata("no_trade", PracticeServer.plugin);
                }
            }.runTaskLater(PracticeServer.plugin, 40L);
        }
    }

    @EventHandler
    public void onInventoryOpenEvent(final InventoryOpenEvent e) {
        final Player p = (Player) e.getPlayer();
        if (Trading.trade_map.containsKey(p)
                && !e.getInventory().getName().toLowerCase().contains(p.getName().toLowerCase())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(final InventoryCloseEvent e) {
        Trading.last_inventory_close.put(e.getPlayer().getName(), System.currentTimeMillis());
        if (!Trading.trade_map.containsKey(e.getPlayer())) {
            return;
        }
        final Player closer = (Player) e.getPlayer();
        final Player trade_partner = Trading.trade_map.get(closer);
        boolean left_side = false;
        final Inventory tradeInv = closer.getOpenInventory().getTopInventory();
        if (Trading.trade_partners.containsKey(closer)) {
            left_side = true;
        }
        if (Trading.trade_partners.containsKey(trade_partner)) {
            left_side = false;
        }
        final ItemStack closer_oc = closer.getItemOnCursor();
        final ItemStack trade_partner_oc = trade_partner.getItemOnCursor();
        closer.setItemOnCursor(new ItemStack(Material.AIR));
        trade_partner.setItemOnCursor(new ItemStack(Material.AIR));
        if (closer.getInventory().firstEmpty() != -1) {
            closer.getInventory().setItem(closer.getInventory().firstEmpty(), closer_oc);
        }
        if (trade_partner.getInventory().firstEmpty() != -1) {
            trade_partner.getInventory().setItem(trade_partner.getInventory().firstEmpty(), trade_partner_oc);
        }
        int slot_var = -1;
        if (left_side) {
            while (slot_var <= 27) {
                if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9
                        && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18 && slot_var != 19
                        && slot_var != 20 && slot_var != 21) {
                    continue;
                }
                ItemStack i = tradeInv.getItem(slot_var);
                if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                    continue;
                }
                if (i.getType() == Material.THIN_GLASS) {
                    continue;
                }
                if (i.getType() == Material.EMERALD) {
                    i = Banks.makeGems(i.getAmount());
                }
                if (closer.getInventory().firstEmpty() == -1) {
                    closer.getWorld().dropItemNaturally(closer.getLocation(), i);
                } else {
                    closer.getInventory().setItem(closer.getInventory().firstEmpty(), this.makeNormal(i));
                }
            }
            slot_var = -1;
            while (slot_var <= 27) {
                if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14
                        && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23 && slot_var != 24
                        && slot_var != 25 && slot_var != 26) {
                    continue;
                }
                ItemStack i = tradeInv.getItem(slot_var);
                if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                    continue;
                }
                if (i.getType() == Material.THIN_GLASS) {
                    continue;
                }
                if (i.getType() == Material.EMERALD) {
                    i = Banks.makeGems(i.getAmount());
                }
                if (trade_partner.getInventory().firstEmpty() == -1) {
                    trade_partner.getWorld().dropItemNaturally(trade_partner.getLocation(), i);
                } else {
                    trade_partner.getInventory().setItem(trade_partner.getInventory().firstEmpty(), this.makeNormal(i));
                }
            }
        }
        if (!left_side) {
            while (slot_var <= 27) {
                if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9
                        && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18 && slot_var != 19
                        && slot_var != 20 && slot_var != 21) {
                    continue;
                }
                ItemStack i = tradeInv.getItem(slot_var);
                if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                    continue;
                }
                if (i.getType() == Material.THIN_GLASS) {
                    continue;
                }
                if (i.getType() == Material.EMERALD) {
                    i = Banks.makeGems(i.getAmount());
                }
                if (trade_partner.getInventory().firstEmpty() == -1) {
                    trade_partner.getWorld().dropItemNaturally(trade_partner.getLocation(), i);
                } else {
                    trade_partner.getInventory().setItem(trade_partner.getInventory().firstEmpty(), this.makeNormal(i));
                }
            }
            slot_var = -1;
            while (slot_var <= 27) {
                if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14
                        && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23 && slot_var != 24
                        && slot_var != 25 && slot_var != 26) {
                    continue;
                }
                ItemStack i = tradeInv.getItem(slot_var);
                if (i == null || i.getType() == Material.AIR || MerchantMechanics.isTradeButton(i)) {
                    continue;
                }
                if (i.getType() == Material.THIN_GLASS) {
                    continue;
                }
                if (i.getType() == Material.EMERALD) {
                    i = Banks.makeGems(i.getAmount());
                }
                if (closer.getInventory().firstEmpty() == -1) {
                    closer.getWorld().dropItemNaturally(closer.getLocation(), i);
                } else {
                    closer.getInventory().setItem(closer.getInventory().firstEmpty(), this.makeNormal(i));
                }
            }
        }
        if (closer.getOpenInventory().getTopInventory().getName().contains(closer.getName())) {
            closer.getOpenInventory().getTopInventory().clear();
        }
        if (trade_partner.getOpenInventory().getTopInventory().getName().contains(trade_partner.getName())) {
            trade_partner.getOpenInventory().getTopInventory().clear();
        }
        Trading.trade_map.remove(closer);
        Trading.trade_map.remove(trade_partner);
        Trading.trade_partners.remove(closer);
        Trading.trade_partners.remove(trade_partner);
        Trading.trade_secure.remove(closer);
        Trading.trade_secure.remove(trade_partner);
        trade_partner.closeInventory();
        closer.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.BOLD)
                .append("Trade cancelled.").toString());
        trade_partner.sendMessage(ChatColor.YELLOW + "Trade cancelled by " + ChatColor.BOLD.toString()
                + closer.getName() + ChatColor.YELLOW.toString() + ".");
        PracticeServer.plugin.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin,
                new Runnable() {
                    @Override
                    public void run() {
                    }
                }, 2L);
    }

    public boolean isItemTradeable(final ItemStack i) {
        return Items.isItemTradeable(i);
    }

    public ItemStack makeUnique(final ItemStack is) {
        return is;
    }

    public ItemStack makeNormal(final ItemStack is) {
        return is;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInventoryClick(final InventoryClickEvent e) {
        final Player trade_partner = Trading.trade_map.get(e.getWhoClicked());
        if (e.getWhoClicked().getType() != EntityType.PLAYER) {
            return;
        }
        final Player clicker = (Player) e.getWhoClicked();
        final String p_name = clicker.getName();
        if (clicker.hasMetadata("click_event")) {
            e.setCancelled(true);
            clicker.updateInventory();
            return;
        }
        if (!clicker.getOpenInventory().getTitle().equalsIgnoreCase("container.crafting")) {
            clicker.setMetadata("click_event",
                    new FixedMetadataValue(PracticeServer.plugin, true));
        }
        PracticeServer.plugin.getServer().getScheduler().runTaskLater(PracticeServer.plugin,
                new Runnable() {
                    @Override
                    public void run() {
                        if (Bukkit.getPlayer(p_name) != null) {
                            Bukkit.getPlayer(p_name).removeMetadata("click_event", PracticeServer.plugin);
                        }
                    }
                }, 5L);
        if (!Trading.trade_map.containsKey(clicker)) {
            if (clicker.getInventory().getName().contains(clicker.getName())
                    && !clicker.getInventory().getName().contains("Merchant")) {
                clicker.closeInventory();
            }
            return;
        }
        final Inventory tradeWin = e.getInventory();
        Material m = Material.AIR;
        if (e.getCurrentItem() != null) {
            m = e.getCurrentItem().getType();
        }
        if (e.isLeftClick() && e.isRightClick()) {
            e.setCancelled(true);
            clicker.updateInventory();
            clicker.sendMessage(ChatColor.RED + "This feature has been " + ChatColor.UNDERLINE + "temporarily"
                    + ChatColor.RED + " disabled.");
            return;
        }
        final Material cursor = e.getCursor().getType();
        boolean left_side = false;
        if (e.getCurrentItem() != null
                && (e.getCurrentItem().getType() == Material.NETHER_STAR
                || e.getCurrentItem().getType() == Material.QUARTZ
                || e.getCurrentItem().getType() == Material.WRITTEN_BOOK)
                || !this.isItemTradeable(e.getCursor()) || !this.isItemTradeable(e.getCurrentItem())) {
            e.setCancelled(true);
            clicker.updateInventory();
            clicker.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED
                    + " perform this action with an " + ChatColor.ITALIC + "untradeable" + ChatColor.RED + " item.");
            return;
        }
        if (e.isRightClick()) {
            e.setCancelled(true);
            clicker.updateInventory();
            return;
        }
        if (m == Material.THIN_GLASS) {
            e.setCancelled(true);
            clicker.updateInventory();
            return;
        }
        final int slot_num = e.getRawSlot();
        if (Trading.trade_partners.containsKey(clicker)) {
            left_side = true;
            if (e.isShiftClick()) {
                clicker.sendMessage(ChatColor.RED + "No Shift Clicking Allowed!");
                e.setCancelled(true);
            }
            if (!e.isShiftClick() || (e.isShiftClick() && slot_num < 27)) {
                if (slot_num >= 27) {
                    return;
                }
                if (slot_num != 0 && slot_num != 1 && slot_num != 2 && slot_num != 3 && slot_num != 9 && slot_num != 10
                        && slot_num != 11 && slot_num != 12 && slot_num != 18 && slot_num != 19 && slot_num != 20
                        && slot_num != 21 && slot_num <= 27) {
                    e.setCancelled(true);
                    clicker.updateInventory();
                    if (MerchantMechanics.isTradeButton(e.getCurrentItem())) {
                        clicker.sendMessage(ChatColor.RED + "Wrong button.");
                    }
                    return;
                }
            }
        }
        if (!Trading.trade_partners.containsKey(clicker)) {
            left_side = false;
            if (e.isShiftClick()) {
                clicker.sendMessage(ChatColor.RED + "No Shift Clicking Allowed!");
                e.setCancelled(true);
            }
            if (!e.isShiftClick() || (e.isShiftClick() && slot_num < 27)) {
                if (e.getInventory().getItem(0).getType() != Material.INK_SACK) {
                    return;
                }
                if (slot_num >= 27) {
                    return;
                }
                if (slot_num != 5 && slot_num != 6 && slot_num != 7 && slot_num != 8 && slot_num != 14 && slot_num != 15
                        && slot_num != 16 && slot_num != 17 && slot_num != 23 && slot_num != 24 && slot_num != 25
                        && slot_num != 26 && slot_num <= 27) {
                    e.setCancelled(true);
                    clicker.updateInventory();
                    if (MerchantMechanics.isTradeButton(e.getCurrentItem())) {
                        clicker.sendMessage(ChatColor.RED + "Wrong button.");
                    }
                    return;
                }
            }
        }
        if (!MerchantMechanics.isTradeButton(e.getCurrentItem()) && (m != Material.AIR || cursor != Material.AIR)
                && Trading.trade_secure.containsKey(clicker)) {
            Trading.trade_secure.remove(clicker);
            Trading.trade_secure.remove(Trading.trade_map.get(clicker));
            tradeWin.setItem(0, setIinfo(new ItemStack(Material.INK_SACK, 1, (short) 8),
                    String.valueOf(ChatColor.YELLOW.toString()) + "Click to ACCEPT Trade", ""));
            tradeWin.setItem(8, setIinfo(new ItemStack(Material.INK_SACK, 1, (short) 8),
                    String.valueOf(ChatColor.YELLOW.toString()) + "Click to ACCEPT Trade", ""));
            clicker.sendMessage(ChatColor.RED + "Trade modified, unaccepted.");
            Trading.trade_map.get(clicker).sendMessage(ChatColor.RED + "Trade modified by " + ChatColor.BOLD
                    + clicker.getName() + ChatColor.RED + ", unaccepted.");
            PracticeServer.plugin.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin,
                    new Runnable() {
                        @Override
                        public void run() {
                            clicker.updateInventory();
                            Trading.trade_map.get(clicker).updateInventory();
                        }
                    }, 1L);
            return;
        }
        if (e.isShiftClick() && slot_num > 27 && !e.isCancelled()) {
            e.setCancelled(true);
            final ItemStack to_move = e.getCurrentItem();
            if (to_move == null) {
                return;
            }
            final int local_to_move_slot = e.getSlot();
            int x = -1;
            if (left_side) {
                while (x <= 27) {
                    if (++x != 0 && x != 1 && x != 2 && x != 3 && x != 9 && x != 10 && x != 11 && x != 12 && x != 18
                            && x != 19 && x != 20 && x != 21) {
                        continue;
                    }
                    final ItemStack i = tradeWin.getItem(x);
                    if (i != null && i.getType() != Material.AIR) {
                        continue;
                    }
                    tradeWin.setItem(x, this.makeUnique(to_move));
                    clicker.getInventory().remove(local_to_move_slot);
                    clicker.getInventory().setItem(local_to_move_slot, new ItemStack(Material.AIR));
                    clicker.updateInventory();
                    break;
                }
            }
            if (!left_side) {
                while (x <= 27) {
                    if (++x != 5 && x != 6 && x != 7 && x != 8 && x != 14 && x != 15 && x != 16 && x != 17 && x != 23
                            && x != 24 && x != 25 && x != 26) {
                        continue;
                    }
                    final ItemStack i = tradeWin.getItem(x);
                    if (i != null && i.getType() != Material.AIR) {
                        continue;
                    }
                    tradeWin.setItem(x, this.makeUnique(to_move));
                    clicker.getInventory().remove(local_to_move_slot);
                    clicker.getInventory().setItem(local_to_move_slot, new ItemStack(Material.AIR));
                    clicker.updateInventory();
                    break;
                }
            }
        }
        if (MerchantMechanics.isTradeButton(e.getCurrentItem())) {
            e.setCancelled(true);
            if (clicker.getItemOnCursor() != null && clicker.getItemOnCursor().getType() != Material.AIR) {
                clicker.updateInventory();
                return;
            }
            if (e.getCurrentItem().getDurability() == 8) {
                e.getCurrentItem().setDurability((short) 10);
                e.setCurrentItem(setIinfo(new ItemStack(Material.INK_SACK, 1, (short) 10),
                        String.valueOf(ChatColor.GREEN.toString()) + "Trade ACCEPTED.",
                        String.valueOf(ChatColor.GRAY.toString()) + "Modify the trade to unaccept."));
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 2.0f);
                if (tradeWin.getItem(0).getDurability() == 10 && tradeWin.getItem(8).getDurability() == 10) {
                    final Player tradie = Trading.trade_map.get(clicker);
                    int tradie_slots = 0;
                    int clicker_slots = 0;
                    int tradie_slots_needed = 0;
                    int clicker_slots_needed = 0;
                    ItemStack[] contents;
                    for (int length = (contents = tradeWin.getContents()).length, n = 0; n < length; ++n) {
                        final ItemStack is = contents[n];
                        if (is != null) {
                            if (!Items.isItemTradeable(is)) {
                                tradeWin.remove(is);
                            }
                        }
                    }
                    ItemStack[] contents2;
                    for (int length2 = (contents2 = tradie.getInventory()
                            .getContents()).length, n2 = 0; n2 < length2; ++n2) {
                        final ItemStack j = contents2[n2];
                        if (j == null || j.getType() == Material.AIR) {
                            ++tradie_slots;
                        }
                    }
                    ItemStack[] contents3;
                    for (int length3 = (contents3 = clicker.getInventory()
                            .getContents()).length, n3 = 0; n3 < length3; ++n3) {
                        final ItemStack j = contents3[n3];
                        if (j == null || j.getType() == Material.AIR) {
                            ++clicker_slots;
                        }
                    }
                    int slot_var = -1;
                    if (left_side) {
                        slot_var = -1;
                        while (slot_var <= 27) {
                            if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9
                                    && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18
                                    && slot_var != 19 && slot_var != 20 && slot_var != 21) {
                                continue;
                            }
                            final ItemStack k = tradeWin.getItem(slot_var);
                            if (k == null || k.getType() == Material.AIR || MerchantMechanics.isTradeButton(k)) {
                                continue;
                            }
                            if (k.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            ++tradie_slots_needed;
                        }
                        if (tradie_slots < tradie_slots_needed) {
                            tradie.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                                    .append("Not enough room.").toString());
                            tradie.sendMessage(
                                    ChatColor.GRAY + "You need " + ChatColor.BOLD + (tradie_slots_needed - tradie_slots)
                                            + ChatColor.GRAY + " more free slots to complete this trade.");
                            clicker.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                                    .append(tradie.getName()).append(" does not have enough room for this trade.")
                                    .toString());
                            PracticeServer.plugin.getServer().getScheduler()
                                    .scheduleSyncDelayedTask(PracticeServer.plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            final InventoryCloseEvent close_tradie = new InventoryCloseEvent(
                                                    tradie.getOpenInventory());
                                            final InventoryCloseEvent close_clicker = new InventoryCloseEvent(
                                                    clicker.getOpenInventory());
                                            Bukkit.getServer().getPluginManager().callEvent(close_tradie);
                                            Bukkit.getServer().getPluginManager().callEvent(close_clicker);
                                        }
                                    }, 2L);
                            return;
                        }
                        slot_var = -1;
                        while (slot_var <= 27) {
                            if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14
                                    && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23
                                    && slot_var != 24 && slot_var != 25 && slot_var != 26) {
                                continue;
                            }
                            final ItemStack k = tradeWin.getItem(slot_var);
                            if (k == null || k.getType() == Material.AIR || MerchantMechanics.isTradeButton(k)) {
                                continue;
                            }
                            if (k.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            ++clicker_slots_needed;
                        }
                        if (clicker_slots < clicker_slots_needed) {
                            clicker.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                                    .append("Not enough room.").toString());
                            clicker.sendMessage(
                                    ChatColor.GRAY + "You need " + ChatColor.BOLD + (tradie_slots_needed - tradie_slots)
                                            + ChatColor.GRAY + " more free slots to complete this trade.");
                            tradie.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                                    .append(clicker.getName()).append(" does not have enough room for this trade.")
                                    .toString());
                            PracticeServer.plugin.getServer().getScheduler()
                                    .scheduleSyncDelayedTask(PracticeServer.plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            final InventoryCloseEvent close_tradie = new InventoryCloseEvent(
                                                    tradie.getOpenInventory());
                                            final InventoryCloseEvent close_clicker = new InventoryCloseEvent(
                                                    clicker.getOpenInventory());
                                            Bukkit.getServer().getPluginManager().callEvent(close_tradie);
                                            Bukkit.getServer().getPluginManager().callEvent(close_clicker);
                                        }
                                    }, 2L);
                            return;
                        }
                        slot_var = -1;
                        while (slot_var <= 27) {
                            if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14 && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23 && slot_var != 24 && slot_var != 25 && slot_var != 26) {
                                continue;
                            }
                            final ItemStack k = tradeWin.getItem(slot_var);
                            if (k == null || k.getType() == Material.AIR || MerchantMechanics.isTradeButton(k)) {
                                continue;
                            }
                            if (k.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            ++clicker_slots_needed;
                        }
                        if (clicker_slots < clicker_slots_needed) {
                            clicker.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Not enough room.").toString());
                            clicker.sendMessage(ChatColor.GRAY + "You need " + ChatColor.BOLD + (tradie_slots_needed - tradie_slots) + ChatColor.GRAY + " more free slots to complete this trade.");
                            tradie.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append(clicker.getName()).append(" does not have enough room for this trade.").toString());
                            PracticeServer.plugin.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin, new Runnable() {
                                @Override
                                public void run() {
                                    final InventoryCloseEvent close_tradie = new InventoryCloseEvent(tradie.getOpenInventory());
                                    final InventoryCloseEvent close_clicker = new InventoryCloseEvent(clicker.getOpenInventory());
                                    Bukkit.getServer().getPluginManager().callEvent(close_tradie);
                                    Bukkit.getServer().getPluginManager().callEvent(close_clicker);
                                }
                            }, 2L);
                            return;
                        }
                        slot_var = -1;
                        int tradeItem = 0;
                        String strTradeItem = "";
                        String itemData = "";
                        final JsonBuilder data = new JsonBuilder("trader_1", tradie.getName());
                        data.setData("trader_2", p_name);
                        while (slot_var <= 27) {
                            if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9 && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18 && slot_var != 19 && slot_var != 20 && slot_var != 21) {
                                continue;
                            }
                            ItemStack l = tradeWin.getItem(slot_var);
                            if (l == null || l.getType() == Material.AIR || MerchantMechanics.isTradeButton(l)) {
                                continue;
                            }
                            if (l.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            if (l.getType() == Material.NETHER_STAR || l.getType() == Material.QUARTZ || l.getType() == Material.WRITTEN_BOOK) {
                                continue;
                            }
                            if (!this.isItemTradeable(l)) {
                                continue;
                            }
                            if (l.getType() == Material.EMERALD) {
                                l = Banks.makeGems(l.getAmount());
                            }
                            strTradeItem = String.valueOf(++tradeItem);
                            final ItemMeta im = l.getItemMeta();
                            tradie.getInventory().setItem(tradie.getInventory().firstEmpty(), this.makeNormal(l));
                            tradie.updateInventory();
                            clicker.updateInventory();
                            Trading.trade_map.remove(clicker);
                            Trading.trade_map.remove(tradie);
                            Trading.trade_partners.remove(clicker);
                            Trading.trade_partners.remove(tradie);
                            Trading.trade_secure.remove(clicker);
                            Trading.trade_secure.remove(tradie);
                            Trading.trade_secure.remove(trade_partner);
                            Trading.trade_partners.remove(trade_partner);
                            Trading.trade_map.remove(trade_partner);
                            tradie.closeInventory();
                            clicker.closeInventory();
                            itemData = String.valueOf(itemData) + "name_" + strTradeItem + ": " + ((im.getDisplayName() == null) ? l.getType().name() : im.getDisplayName()) + ", lore_" + strTradeItem + ": " + ((im.getLore() == null) ? "" : im.getLore()) + ", damage_" + strTradeItem + ": " + l.getDurability() + ", amount_" + strTradeItem + ": " + l.getAmount();
                        }
                        data.setData("items_to_trader_1", itemData);
                        itemData = "";
                        tradeItem = 0;
                        tradie.sendMessage(new StringBuilder().append(ChatColor.GREEN).append(ChatColor.BOLD).append("Trade accepted.").toString());
                        tradie.playSound(tradie.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 1.5f);
                        slot_var = -1;
                        while (slot_var <= 27) {
                            if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14 && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23 && slot_var != 24 && slot_var != 25 && slot_var != 26) {
                                continue;
                            }
                            ItemStack l = tradeWin.getItem(slot_var);
                            if (l == null || l.getType() == Material.AIR || MerchantMechanics.isTradeButton(l)) {
                                continue;
                            }
                            if (l.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            if (l.getType() == Material.NETHER_STAR || l.getType() == Material.QUARTZ || l.getType() == Material.WRITTEN_BOOK) {
                                continue;
                            }
                            if (!this.isItemTradeable(l)) {
                                continue;
                            }
                            if (l.getType() == Material.EMERALD) {
                                l = Banks.makeGems(l.getAmount());
                            }
                            strTradeItem = String.valueOf(++tradeItem);
                            final ItemMeta im = l.getItemMeta();
                            clicker.getInventory().setItem(clicker.getInventory().firstEmpty(), this.makeNormal(l));
                            tradie.updateInventory();
                            clicker.updateInventory();
                            Trading.trade_map.remove(clicker);
                            Trading.trade_map.remove(tradie);
                            Trading.trade_partners.remove(clicker);
                            Trading.trade_partners.remove(tradie);
                            Trading.trade_secure.remove(clicker);
                            Trading.trade_secure.remove(tradie);
                            Trading.trade_secure.remove(trade_partner);
                            Trading.trade_partners.remove(trade_partner);
                            Trading.trade_map.remove(trade_partner);
                            tradie.closeInventory();
                            clicker.closeInventory();
                            itemData = String.valueOf(itemData) + "name_" + strTradeItem + ": " + ((im.getDisplayName() == null) ? l.getType().name() : im.getDisplayName()) + ", lore_" + strTradeItem + ": " + ((im.getLore() == null) ? "" : im.getLore()) + ", damage_" + strTradeItem + ": " + l.getDurability() + ", amount_" + strTradeItem + ": " + l.getAmount();
                        }
                        data.setData("items_to_trader_2", itemData);
                        clicker.sendMessage(new StringBuilder().append(ChatColor.GREEN).append(ChatColor.BOLD).append("Trade accepted.").toString());
                        clicker.playSound(clicker.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 1.5f);
                    }
                    if (!left_side) {
                        slot_var = -1;
                        while (slot_var <= 27) {
                            if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9 && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18 && slot_var != 19 && slot_var != 20 && slot_var != 21) {
                                continue;
                            }
                            final ItemStack k = tradeWin.getItem(slot_var);
                            if (k == null || k.getType() == Material.AIR || MerchantMechanics.isTradeButton(k)) {
                                continue;
                            }
                            if (k.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            ++clicker_slots_needed;
                        }
                        if (clicker_slots < clicker_slots_needed) {
                            clicker.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Not enough room.").toString());
                            clicker.sendMessage(ChatColor.GRAY + "You need " + ChatColor.BOLD + (tradie_slots_needed - tradie_slots) + ChatColor.GRAY + " more free slots to complete this trade.");
                            tradie.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append(clicker.getName()).append(" does not have enough room for this trade.").toString());
                            PracticeServer.plugin.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin, new Runnable() {
                                @Override
                                public void run() {
                                    final InventoryCloseEvent close_tradie = new InventoryCloseEvent(tradie.getOpenInventory());
                                    final InventoryCloseEvent close_clicker = new InventoryCloseEvent(clicker.getOpenInventory());
                                    Bukkit.getServer().getPluginManager().callEvent(close_tradie);
                                    Bukkit.getServer().getPluginManager().callEvent(close_clicker);
                                }
                            }, 2L);
                            return;
                        }
                        slot_var = -1;
                        while (slot_var <= 27) {
                            if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14 && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23 && slot_var != 24 && slot_var != 25 && slot_var != 26) {
                                continue;
                            }
                            ItemStack k = tradeWin.getItem(slot_var);
                            if (k == null || k.getType() == Material.AIR || MerchantMechanics.isTradeButton(k)) {
                                continue;
                            }
                            if (k.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            if (k.getType() == Material.NETHER_STAR || k.getType() == Material.QUARTZ || k.getType() == Material.WRITTEN_BOOK) {
                                continue;
                            }
                            if (!this.isItemTradeable(k)) {
                                continue;
                            }
                            if (k.getType() == Material.EMERALD) {
                                k = Banks.makeGems(k.getAmount());
                            }
                            ++tradie_slots_needed;
                        }
                        if (tradie_slots < tradie_slots_needed) {
                            tradie.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Not enough room.").toString());
                            tradie.sendMessage(ChatColor.GRAY + "You need " + ChatColor.BOLD + (tradie_slots_needed - tradie_slots) + ChatColor.GRAY + " more free slots to complete this trade.");
                            clicker.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append(tradie.getName()).append(" does not have enough room for this trade.").toString());
                            PracticeServer.plugin.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin, new Runnable() {
                                @Override
                                public void run() {
                                    final InventoryCloseEvent close_tradie = new InventoryCloseEvent(tradie.getOpenInventory());
                                    final InventoryCloseEvent close_clicker = new InventoryCloseEvent(clicker.getOpenInventory());
                                    Bukkit.getServer().getPluginManager().callEvent(close_tradie);
                                    Bukkit.getServer().getPluginManager().callEvent(close_clicker);
                                }
                            }, 2L);
                            return;
                        }
                        slot_var = -1;
                        int tradeItem = 0;
                        String strTradeItem = "";
                        String itemData = "";
                        final JsonBuilder data = new JsonBuilder("trader_1", p_name);
                        data.setData("trader_2", tradie.getName());
                        while (slot_var <= 27) {
                            if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9 && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18 && slot_var != 19 && slot_var != 20 && slot_var != 21) {
                                continue;
                            }
                            ItemStack l = tradeWin.getItem(slot_var);
                            if (l == null || l.getType() == Material.AIR || MerchantMechanics.isTradeButton(l)) {
                                continue;
                            }
                            if (l.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            if (l.getType() == Material.NETHER_STAR || l.getType() == Material.QUARTZ || l.getType() == Material.WRITTEN_BOOK) {
                                continue;
                            }
                            if (!this.isItemTradeable(l)) {
                                continue;
                            }
                            if (l.getType() == Material.EMERALD) {
                                l = Banks.makeGems(l.getAmount());
                            }
                            strTradeItem = String.valueOf(++tradeItem);
                            final ItemMeta im = l.getItemMeta();
                            clicker.getInventory().setItem(clicker.getInventory().firstEmpty(), this.makeNormal(l));
                            tradie.updateInventory();
                            clicker.updateInventory();
                            Trading.trade_map.remove(clicker);
                            Trading.trade_map.remove(tradie);
                            Trading.trade_partners.remove(clicker);
                            Trading.trade_partners.remove(tradie);
                            Trading.trade_secure.remove(clicker);
                            Trading.trade_secure.remove(tradie);
                            Trading.trade_secure.remove(trade_partner);
                            Trading.trade_partners.remove(trade_partner);
                            Trading.trade_map.remove(trade_partner);
                            tradie.closeInventory();
                            clicker.closeInventory();
                            itemData = String.valueOf(itemData) + "name_" + strTradeItem + ": " + ((im.getDisplayName() == null) ? l.getType().name() : im.getDisplayName()) + ", lore_" + strTradeItem + ": " + ((im.getLore() == null) ? "" : im.getLore()) + ", damage_" + strTradeItem + ": " + l.getDurability() + ", amount_" + strTradeItem + ": " + l.getAmount();
                        }
                        data.setData("items_to_trader_1", itemData);
                        itemData = "";
                        tradeItem = 0;
                        clicker.sendMessage(new StringBuilder().append(ChatColor.GREEN).append(ChatColor.BOLD).append("Trade accepted.").toString());
                        clicker.playSound(clicker.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 1.5f);
                        slot_var = -1;
                        slot_var = -1;
                        while (slot_var <= 27) {
                            if (++slot_var != 5 && slot_var != 6 && slot_var != 7 && slot_var != 8 && slot_var != 14 && slot_var != 15 && slot_var != 16 && slot_var != 17 && slot_var != 23 && slot_var != 24 && slot_var != 25 && slot_var != 26) {
                                continue;
                            }
                            ItemStack l = tradeWin.getItem(slot_var);
                            if (l == null || l.getType() == Material.AIR || MerchantMechanics.isTradeButton(l)) {
                                continue;
                            }
                            if (l.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            if (l.getType() == Material.NETHER_STAR || l.getType() == Material.QUARTZ || l.getType() == Material.WRITTEN_BOOK) {
                                continue;
                            }
                            if (!this.isItemTradeable(l)) {
                                continue;
                            }
                            if (l.getType() == Material.EMERALD) {
                                l = Banks.makeGems(l.getAmount());
                            }
                            strTradeItem = String.valueOf(++tradeItem);
                            final ItemMeta im = l.getItemMeta();
                            tradie.getInventory().setItem(tradie.getInventory().firstEmpty(), this.makeNormal(l));
                            tradie.updateInventory();
                            clicker.updateInventory();
                            Trading.trade_map.remove(clicker);
                            Trading.trade_map.remove(tradie);
                            Trading.trade_partners.remove(clicker);
                            Trading.trade_partners.remove(tradie);
                            Trading.trade_secure.remove(clicker);
                            Trading.trade_secure.remove(tradie);
                            Trading.trade_secure.remove(trade_partner);
                            Trading.trade_partners.remove(trade_partner);
                            Trading.trade_map.remove(trade_partner);
                            tradie.closeInventory();
                            clicker.closeInventory();
                            itemData = String.valueOf(itemData) + "name_" + strTradeItem + ": " + ((im.getDisplayName() == null) ? l.getType().name() : im.getDisplayName()) + ", lore_" + strTradeItem + ": " + ((im.getLore() == null) ? "" : im.getLore()) + ", damage_" + strTradeItem + ": " + l.getDurability() + ", amount_" + strTradeItem + ": " + l.getAmount();
                        }
                        data.setData("items_to_trader_2", itemData);
                        tradie.playSound(tradie.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 1.5f);
                        tradie.sendMessage(new StringBuilder().append(ChatColor.GREEN).append(ChatColor.BOLD).append("Trade accepted.").toString());
                    }
                    if (!tradeWin.getName().equalsIgnoreCase("container.crafting")) {
                        tradeWin.clear();
                    }
                    PracticeServer.plugin.getServer().getScheduler()
                            .scheduleSyncDelayedTask(PracticeServer.plugin, new Runnable() {
                                @Override
                                public void run() {
                                    tradie.updateInventory();
                                    clicker.updateInventory();
                                    Trading.trade_map.remove(clicker);
                                    Trading.trade_map.remove(tradie);
                                    Trading.trade_partners.remove(clicker);
                                    Trading.trade_partners.remove(tradie);
                                    Trading.trade_secure.remove(clicker);
                                    Trading.trade_secure.remove(tradie);
                                    tradie.closeInventory();
                                    clicker.closeInventory();

                                }
                            }, 1L);
                } else {
                    Trading.trade_secure.put(clicker, tradeWin);
                    Trading.trade_secure.put(Trading.trade_map.get(clicker), tradeWin);
                    clicker.sendMessage(ChatColor.YELLOW + "Trade accepted, waiting for " + ChatColor.BOLD
                            + Trading.trade_map.get(clicker).getName() + ChatColor.YELLOW + "...");
                    Trading.trade_map.get(clicker)
                            .sendMessage(ChatColor.GREEN + clicker.getName() + " has accepted the trade.");
                    Trading.trade_map.get(clicker)
                            .sendMessage(ChatColor.GRAY + "Click the gray button (dye) to confirm.");
                }
            }
        }
    }
}