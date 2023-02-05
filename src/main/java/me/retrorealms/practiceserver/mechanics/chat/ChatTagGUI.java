package me.retrorealms.practiceserver.mechanics.chat;

import me.retrorealms.practiceserver.apis.nbt.NBTAccessor;
import me.retrorealms.practiceserver.enums.chat.ChatTag;
import me.retrorealms.practiceserver.mechanics.inventory.Menu;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Giovanni on 3-5-2017.
 */
public class ChatTagGUI extends Menu {


    public ChatTagGUI(Player player) {
        super(player, "&5&lMYSTARIA &7- CHAT TAGS", 36);


        ChatTag.stream().forEach(chatTag -> {

            if (chatTag == ChatTag.DEFAULT) return;

            ItemStack itemStack = new ItemStack(Material.NAME_TAG);
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', chatTag.getTag()));

            boolean ownsTag = ChatMechanics.hasTagUnlocked(player, chatTag) || ModerationMechanics.isDonator(player) || ModerationMechanics.isStaff(player);

            if (chatTag.isStoreInstance()) {

                if (ownsTag) {
                    List<String> lore = Arrays.asList(
                            "",
                            "&a&lUNLOCKED",
                            "",
                            "&dClick to enable.");

                    itemMeta.setLore(lore.stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList()));
                } else {

                    List<String> lore = Arrays.asList(
                            "",
                            "&7Price: &a&l$1.99",
                            "&eSUB &7or higher gets instant access to all chat-tags!",
                            "",
                            "&dMessage Red for Info");

                    itemMeta.setLore(lore.stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList()));
                }

                itemStack.setItemMeta(itemMeta);

                NBTAccessor nbtAccessor = new NBTAccessor(itemStack).check();

                nbtAccessor.setString("chatTagItem", chatTag.name());
                itemStack = nbtAccessor.update();
            } else {

                if (ownsTag) {
                    List<String> lore = Arrays.asList(
                            "",
                            "&a&lUNLOCKED",
                            "",
                            "&dClick to enable.");

                    itemMeta.setLore(lore.stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList()));
                } else {
                    List<String> lore = Arrays.asList(
                            "",
                            "&7Price: &a&l15000G",
                            "&eSUB &7or higher gets instant access to all chat-tags!",
                            "",
                            "&dMessage Red for Info");

                    itemMeta.setLore(lore.stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList()));
                }

                itemStack.setItemMeta(itemMeta);

                NBTAccessor nbtAccessor = new NBTAccessor(itemStack).check();

                nbtAccessor.setString("chatTagItem", chatTag.name());
                itemStack = nbtAccessor.update();
            }

            ItemStack itemStack1 = new ItemStack(Material.BARRIER);
            ItemMeta itemMeta1 = itemStack1.getItemMeta();
            itemMeta1.setDisplayName(ChatColor.RED + "RESET CHAT TAG");

            List<String> lore1 = Arrays.asList(
                    "",
                    "&dClick to remove your current chat-tag.");

            itemMeta.setLore(lore1.stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList()));

            itemStack1.setItemMeta(itemMeta1);

            setItem(8, itemStack1);

            setItem(getInventory().firstEmpty(), itemStack);

        });

        this.openFor(player);
    }

    @Override
    public void onClick(InventoryClickEvent event, int slot) {
        // Unused.
    }
}
