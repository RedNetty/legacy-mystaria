    package me.retrorealms.practiceserver.mechanics.donations.StatTrak;

    import me.retrorealms.practiceserver.PracticeServer;
    import me.retrorealms.practiceserver.mechanics.donations.Nametags.Nametag;
    import me.retrorealms.practiceserver.mechanics.duels.Duels;
    import me.retrorealms.practiceserver.mechanics.item.Items;
    import org.bukkit.Bukkit;
    import org.bukkit.ChatColor;
    import org.bukkit.Material;
    import org.bukkit.entity.Player;
    import org.bukkit.event.EventHandler;
    import org.bukkit.event.Listener;
    import org.bukkit.event.inventory.InventoryClickEvent;
    import org.bukkit.event.player.AsyncPlayerChatEvent;
    import org.bukkit.event.player.PlayerQuitEvent;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.inventory.meta.ItemMeta;

    import java.util.ArrayList;
    import java.util.HashMap;


    /**
     * Created by Red on 11/11/2017
     */

    public class PickTrak implements Listener {
        public static HashMap<String, ItemStack> currently_using_pikTrak = new HashMap<>();
        public static ItemStack pickaxe_tracker_item;

        static {
            pickaxe_tracker_item = Items.signNewCustomItem(Material.NETHER_STAR, String.valueOf(ChatColor.GOLD.toString()) + "Pickaxe Stat Tracker", String.valueOf(ChatColor.GOLD + "Uses: " + ChatColor.GRAY + "1" + "," + ChatColor.GRAY + ChatColor.ITALIC + "Apply to any pickaxe to start tracking"  + "," + ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "stats as you use it." ));
        }

        public void onEnable() {
            Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        }

        @EventHandler
        public void onPlayerQuit(final PlayerQuitEvent e) {
            final Player p = e.getPlayer();
            if (currently_using_pikTrak.containsKey(p.getName()) && Nametag.getEmptyInventorySlots(p) >= 2) {
                p.getInventory().addItem(currently_using_pikTrak.get(p.getName()));
                p.getInventory().addItem(pickaxe_tracker_item);
            }
            currently_using_pikTrak.remove(p.getName());
        }
        public boolean isPickTrakItem(final ItemStack is) {
            return is != null && is.getType() == Material.NETHER_STAR && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && ChatColor.stripColor(is.getItemMeta().getDisplayName()).equalsIgnoreCase("Pickaxe Stat Tracker");
        }

        public static boolean isPickaxe(ItemStack is) {
            String name = is.getType().name();
            return name.contains("_PICKAXE");
        }

        public void addNewStats(ItemStack itemStack, Player p){
            ItemMeta meta = itemStack.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.addAll(meta.getLore());
            lore.add(" ");
            lore.add(ChatColor.GOLD + "✪" + ChatColor.AQUA + " StatTrak " + ChatColor.GOLD + "✪");
            lore.add(ChatColor.GOLD + "Ores Mined: " + ChatColor.AQUA + "0");
            lore.add(ChatColor.GOLD + "Gems Found: " + ChatColor.AQUA + "0");
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            p.getInventory().addItem(itemStack);
        }
        @EventHandler
        public void onApplyTracker(AsyncPlayerChatEvent e){
            Player p = e.getPlayer();
            if(currently_using_pikTrak.containsKey((p.getName()))){
                e.setCancelled(true);
                if (e.getMessage().equalsIgnoreCase("confirm")){
                    addNewStats(currently_using_pikTrak.get(p.getName()), p);
                    currently_using_pikTrak.remove(p.getName());
                }else{
                    if(Nametag.getEmptyInventorySlots(p) >= 2){
                        ItemStack returnItem = pickaxe_tracker_item;
                        p.getInventory().addItem(returnItem);
                        p.getInventory().addItem((currently_using_pikTrak.get(p.getName())));
                        currently_using_pikTrak.remove(p.getName());
                        p.sendMessage(ChatColor.RED + "StatTrak Application - " + ChatColor.BOLD + "CANCELLED");
                        return;
                    }
                }
            }
        }

        public static ItemStack incrementPickStat(ItemStack itemStack, String stat, int amount){
            String str = "";
            switch(stat){
                case "ores":
                    str = "Ores Mined: ";
                    break;
                case "gems":
                    str = "Gems Found: ";
                    break;
            }
            if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getItemMeta().hasLore()) {
                ItemMeta meta = itemStack.getItemMeta();
                ArrayList<String> lore = new ArrayList<>();
                ArrayList<String> newlore = new ArrayList<>();
                lore.addAll(meta.getLore());
                for(String line : lore) {
                    if (!line.contains(str)){
                        newlore.add(line);
                    }else {
                        int current = Integer.parseInt(line.split(": " + ChatColor.AQUA)[1]);
                        newlore.add(ChatColor.GOLD + str + ChatColor.AQUA + Integer.toString(current+amount));
                    }
                }
                meta.setLore(newlore);
                itemStack.setItemMeta(meta);

            }
            return itemStack;
        }

        @EventHandler
        public void onStatTrakUse(final InventoryClickEvent e) {
            final Player pl = (Player) e.getWhoClicked();
            if (e.getInventory().getName().equalsIgnoreCase("container.crafting") && e.getCursor() != null && this.isPickTrakItem(e.getCursor()) && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                ItemStack target_item = e.getCurrentItem();
                e.setCancelled(true);
                final ItemStack source_item = e.getCurrentItem();
                if (Nametag.isArmororWeapon(target_item)) {
                    pl.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " modify this item.");
                    return;
                }
                if (!isPickaxe(target_item)) {
                    pl.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "can" + ChatColor.RED + " only use this on Pickaxes!");
                    return;
                }
                if (target_item.getAmount() > 1) {
                    pl.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use this on stacked items.");
                    return;
                }if(WepTrak.isStatTrak(target_item)){
                    pl.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use multiple stat traks on one item.");
                    return;
                }if(Duels.duelers.containsKey(pl)){
                    pl.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use this item while in a duel");
                    return;
                }
                source_item.setAmount(1);
                currently_using_pikTrak.put(pl.getName(), source_item);
                final ItemStack is = e.getCursor();
                ItemStack return_is = new ItemStack(Material.AIR);
                if (is.getAmount() > 1) {
                    final int current_amount = is.getAmount();
                    is.setAmount(current_amount - 1);
                    return_is = is;
                }
                e.setCursor(return_is);
                e.setCurrentItem(new ItemStack(Material.AIR));
                if (target_item.getAmount() <= 1) {
                    target_item = new ItemStack(Material.AIR);
                } else if (target_item.getAmount() > 1) {
                    target_item = e.getCurrentItem();
                    final int current_amount = target_item.getAmount();
                    target_item.setAmount(current_amount - 1);
                    pl.getInventory().addItem(target_item);
                    pl.updateInventory();
                }
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin, new Runnable() {
                    @Override
                    public void run() {
                        pl.closeInventory();
                        pl.sendMessage(ChatColor.GOLD + "Are you sure you would like to apply this Stat Tracket to this item?");
                        pl.sendMessage(ChatColor.RED + "This opperation is non-refundable and non-reversable, type " + ChatColor.GREEN + ChatColor.BOLD + "CONFIRM " + ChatColor.RED + "to accept");
                    }
                }, 2L);
            }
        }
    }