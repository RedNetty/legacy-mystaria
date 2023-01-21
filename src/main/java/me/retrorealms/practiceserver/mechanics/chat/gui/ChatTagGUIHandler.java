package me.retrorealms.practiceserver.mechanics.chat.gui;

import me.retrorealms.practiceserver.apis.nbt.NBTAccessor;
import me.retrorealms.practiceserver.enums.chat.ChatTag;
import me.retrorealms.practiceserver.mechanics.chat.ChatMechanics;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Giovanni on 3-5-2017.
 */
public class ChatTagGUIHandler implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        ItemStack itemStack = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getName().contains("TAGS")) {

            event.setCancelled(true);

            if (itemStack != null && itemStack.getType() != null && itemStack.getType() != Material.AIR && itemStack.getType() == Material.BARRIER) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aChat tag reset"));
                player.closeInventory();

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10F, 1.4F);

                ChatMechanics.getPlayerTags().remove(player.getUniqueId());
                ChatMechanics.getPlayerTags().put(player.getUniqueId(), ChatTag.DEFAULT);

                return;
            }

            NBTAccessor nbtAccessor = new NBTAccessor(itemStack);

            if (!nbtAccessor.hasTag()) return;

            if (!nbtAccessor.hasKey("chatTagItem")) return;

            String tagName = nbtAccessor.getString("chatTagItem");
            ChatTag chatTag = ChatTag.valueOf(tagName);

            boolean ownsTag = ChatMechanics.hasTagUnlocked(player, chatTag) || ModerationMechanics.isStaff(player) || ModerationMechanics.isDonator(player);

            if (!ownsTag) {

                if (chatTag.isStoreInstance()) {

                    player.closeInventory();

                    StringUtil.sendCenteredMessage(player, ChatColor.translateAlternateColorCodes('&', "&dYou do &l&nNOT&d own this chat-tag!"));
                    StringUtil.sendCenteredMessage(player, ChatColor.translateAlternateColorCodes('&', "&aPurchase it by messaging RED"));

                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 10F, 1F);
                    return;
                }

                if (Money.hasEnoughGems(player, 15000)) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou've purchased the " + chatTag.getTag() + "&a tag for &l15000G!"));

                    ChatMechanics.unlockTag(player, chatTag);
                    player.closeInventory();

                    Money.takeGems(player, 15000);
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNot enough funds! Required: &l15000G"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 10F, 1F);
                    player.closeInventory();
                    return;
                }
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aChat tag updated to: " + chatTag.getTag()));
            player.closeInventory();

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10F, 1.4F);

            ChatMechanics.getPlayerTags().remove(player.getUniqueId());
            ChatMechanics.getPlayerTags().put(player.getUniqueId(), chatTag);
        }
    }
}
