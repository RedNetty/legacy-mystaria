package me.retrorealms.practiceserver.mechanics.item.scroll;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.apis.itemapi.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.inventory.Menu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by Giovanni on 6-5-2017.
 */
public class ScrollGUI extends Menu {

    public ScrollGUI(Player player) {
        super(player, "Dungeoneer", 9);

        IntStream.rangeClosed(0, PracticeServer.t6 ? 4 : 3).forEach(intConsumer -> {
            ItemStack scroll = ItemAPI.getScrollGenerator().next(intConsumer);
            ItemMeta itemMeta1 = scroll.getItemMeta();

            int price = ItemAPI.getScrollGenerator().getPrice(intConsumer);
            List<String> currentLore = itemMeta1.getLore();
            currentLore.addAll(Arrays.asList("", ChatColor.GREEN + "Price: " + price + ChatColor.BOLD + "G"));
            itemMeta1.setLore(currentLore);


            scroll.setItemMeta(itemMeta1);

            NBTAccessor nbtAccessor = new NBTAccessor(scroll);
            nbtAccessor.setInt("guiPrice", price);
            nbtAccessor.setInt("guiTier", intConsumer);

            this.setItem(intConsumer, nbtAccessor.update());

        });
    }

    @Override
    public void onClick(InventoryClickEvent event, int slot) {
        // Unused.
    }
}
