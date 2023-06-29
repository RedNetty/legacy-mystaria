package me.retrorealms.practiceserver.mechanics.market;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.API;
import me.retrorealms.practiceserver.apis.itemapi.ItemSerializer;
import me.retrorealms.practiceserver.apis.nbt.NBTAccessor;
import me.retrorealms.practiceserver.manager.Manager;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Khalid on 7/12/2017.
 */
public class GlobalMarket extends Manager {

    private transient List<ListedItem> items = new LinkedList<>();

    @SerializedName("market.Items")
    private final List<String> serializedItems = Lists.newArrayList();

    private transient HashMap<String, ItemStack> specifyPrice = new HashMap<>();

    public HashMap<String, ItemStack> getSpecifyPrice() {
        return specifyPrice;
    }

    public void onEnable() {
        super.listener(new MarketHandler());
        items = loadItems();
        if (items == null) {
            items = Lists.newArrayList();

            // Prevent GC pauses so dont let this nigga be above 4655, if ever.
            if (this.items.size() > 4655) {

                Stream.of(this.items.size() - 4655).forEach(b -> {
                    this.items.remove(this.items.get(b));
                });

                throw new StackOverflowError("Expected GlobalMarket item size is 4655.");
            }

            this.items.forEach(ListedItem::init);
        }
        for(ListedItem item : items) {
          System.out.println(item.getOwner());
        }
    }

    public void onDisable() {
        serialize();
    }

    public List<ListedItem> loadItems() {
        if (!PracticeServer.getMarketData().getFile().exists()) return Lists.newArrayList();

        List<ListedItem> list = new ArrayList<>();

        try {

            FileReader reader = new FileReader(PracticeServer.getMarketData().getFile());
            BufferedReader bufferedReader = new BufferedReader(reader);
            List<String> strings = API.getGson().fromJson(bufferedReader, new TypeToken<List<String>>() {
            }.getType());
            if (strings != null) {
                strings.forEach(s -> {
                    ListedItem item = API.getGson().fromJson(s, ListedItem.class).init();
                    list.add(item);
                });
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        return list;
    }

    public void serialize() {
        items.forEach(b -> serializedItems.add(b.serializeItem()));
        String s = API.getGson().toJson(serializedItems);
        FileWriter writer;
        try {
            writer = new FileWriter(PracticeServer.getMarketData().getFile());
            writer.write(s);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPlayerToSpecify(Player p, ItemStack i) {
        specifyPrice.put(p.getUniqueId().toString(), i);
    }

    public void removePlayerFromSpecify(Player p) {
        if (specifyPrice.containsKey(p.getUniqueId().toString())) specifyPrice.remove(p.getUniqueId().toString());
    }

    public void openListGUI(Player p) {
        Inventory i = Bukkit.createInventory(null, 45, "Choose an item to list");
        for (ItemStack itemStack : p.getInventory().getStorageContents()) {
            if (itemStack != null) i.addItem(itemStack);
        }
        p.openInventory(i);
    }

    public void listItem(Player owner, int price, ItemStack item) {
        if(Duels.duelers.containsKey(owner)){
            owner.sendMessage(ChatColor.RED + "You cannot list items in the market while in a duel");
            return;
        }
        ListedItem listedItem = new ListedItem(UUID.randomUUID(), item, price, owner.getUniqueId(), ItemSerializer.itemStackToBase64(item));
        items.add(listedItem);
    }

    public void unlistItem(ListedItem item) {
        ListedItem item1 = null;
        for (ListedItem listedItem : items) {
            if (listedItem != null && listedItem.getItemId().toString().equals(item.getItemId().toString())) {
                item1 = listedItem;
                Player p = Bukkit.getPlayer(item.getOwner());
                if (p.getInventory().firstEmpty() == -1) {
                    p.getWorld().dropItemNaturally(p.getLocation(), item.getItemStack());
                    p.sendMessage(ChatColor.GOLD + "The item has been dropped due to your inventory being full..");
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_BREAK, 2L, 2L);
                } else {
                    p.getInventory().addItem(item.getItemStack());
                }
            }
        }
        if (item1 != null)
            items.remove(item1);
    }

    public int getCurrentPage(Inventory i) {
        String name = ChatColor.stripColor(i.getTitle());
        if (name.contains("Global Market")) {
            name = name.replace("Global Market - ", "");
            name = name.replaceAll(" ", "");
        } else {
            name = name.replace("Your listed items - ", "");
            name = name.replaceAll(" ", "");
        }
        return Integer.valueOf(name);
    }

    public void openMarketGUI(Player p, int page) {
        int min = (page * 45) - 45;
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Global Market - " + page);
        int s = 0;
        for (int i = min; i < (min + 45); i++) {
            if (i >= items.size()) {
                break;
            }
            ItemStack it = items.get(i).getItemStack().clone();
            ItemMeta m = it.getItemMeta();
            if (it.hasItemMeta()) {
                if (m.hasLore()) {
                    List<String> loreList = m.getLore();
                    loreList.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + items.get(i).getPrice() + "g");
                    m.setLore(loreList);
                } else {
                    m.setLore(Arrays.asList(ChatColor.GREEN + "Price: " + ChatColor.WHITE + items.get(i).getPrice() + "g", ChatColor.GRAY + "Seller: " + Bukkit.getOfflinePlayer(items.get(i).getOwner()).getName()));
                }
            } else {
                m.setLore(Arrays.asList(ChatColor.GREEN + "Price: " + ChatColor.WHITE + items.get(i).getPrice() + "g", ChatColor.GRAY + "Seller: " + Bukkit.getOfflinePlayer(items.get(i).getOwner()).getName()));
            }
            it.setItemMeta(m);
            NBTAccessor a = new NBTAccessor(it);
            a.check().setString("marketItem.id", items.get(i).getItemId().toString());
            ItemStack nit = a.update();
            inv.setItem(s, nit);
            s++;
        }
        if ((page * 45) < items.size()) {
            ItemStack next = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getWoolData());
            ItemMeta nm = next.getItemMeta();
            nm.setDisplayName(ChatColor.GREEN + "Next Page");
            next.setItemMeta(nm);
            inv.setItem(53, next);
        }
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.WOOL, 1, DyeColor.RED.getWoolData());
            ItemMeta nm = prev.getItemMeta();
            nm.setDisplayName(ChatColor.RED + "Previous Page");
            prev.setItemMeta(nm);
            inv.setItem(45, prev);
        }
        p.openInventory(inv);
    }

    public void openChoiceGUI(Player p) {
        Inventory i = Bukkit.createInventory(null, 9, "Market Actions");

        ItemStack listedItems = new ItemStack(Material.BOOK);
        ItemMeta lm = listedItems.getItemMeta();
        lm.setDisplayName(ChatColor.GRAY + "View your listed items");
        listedItems.setItemMeta(lm);

        ItemStack list = new ItemStack(Material.ANVIL);
        ItemMeta lsm = list.getItemMeta();
        lsm.setDisplayName(ChatColor.GRAY + "List an item for sale");
        list.setItemMeta(lsm);

        ItemStack market = new ItemStack(Material.BEACON);
        ItemMeta mm = market.getItemMeta();
        mm.setDisplayName(ChatColor.GRAY + "View the market-place");
        market.setItemMeta(mm);


        i.setItem(2, listedItems);
        i.setItem(4, market);
        i.setItem(6, list);

        p.openInventory(i);
    }

    private List<ListedItem> getListedItems(Player p) {
        List<ListedItem> l = new ArrayList<>();
        for (ListedItem item : items) {
            if (item.getOwner().toString().equals(p.getUniqueId().toString()))
                l.add(item);
        }
        return l;
    }

    public void openListedItemGUI(Player p, int page) {
        int min = (page * 45) - 45;
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Your listed items - " + page);
        List<ListedItem> l = getListedItems(p);
        int s = 0;
        for (int i = min; i < (min + 45); i++) {
            if (i >= l.size()) {
                break;
            }
            ItemStack it = l.get(i).getItemStack().clone();
            ItemMeta m = it.getItemMeta();
            if (it.hasItemMeta()) {
                if (m.hasDisplayName()) {
                    m.setDisplayName(m.getDisplayName() + ChatColor.GREEN + "  Price: " + ChatColor.WHITE + items.get(i).getPrice() + "g");
                } else {
                    m.setDisplayName(ChatColor.GREEN + "  Price: " + ChatColor.WHITE + items.get(i).getPrice() + "g");
                }
            } else {
                m.setDisplayName(ChatColor.GREEN + "Price: " + ChatColor.WHITE + items.get(i).getPrice() + "g");
            }
            it.setItemMeta(m);
            NBTAccessor a = new NBTAccessor(it);
            a.check().setString("marketItem.id", l.get(i).getItemId().toString());
            ItemStack nit = a.update();
            inv.setItem(s, nit);
            s++;
        }
        if ((page * 45) < items.size()) {
            ItemStack next = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getWoolData());
            ItemMeta nm = next.getItemMeta();
            nm.setDisplayName(ChatColor.GREEN + "Next Page");
            next.setItemMeta(nm);
            inv.setItem(53, next);
        }
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.WOOL, 1, DyeColor.RED.getWoolData());
            ItemMeta nm = prev.getItemMeta();
            nm.setDisplayName(ChatColor.RED + "Previous Page");
            prev.setItemMeta(nm);
            inv.setItem(45, prev);
        }
        p.openInventory(inv);
    }

    public void removeListedItem(ListedItem item) {
        items.remove(item);
    }

    public ListedItem getListedItem(ItemStack i) {
        NBTAccessor a = new NBTAccessor(i);
        String s = a.getString("marketItem.id");
        for (ListedItem item : items) {
            if (item.getItemId().toString().equals(s)) {
                return item;
            }
        }
        return null;
    }

    public List<ListedItem> getItems() {
        return items;
    }
}
