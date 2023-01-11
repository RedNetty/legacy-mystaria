package me.retrorealms.practiceserver.mechanics.drops.buff;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import me.retrorealms.practiceserver.apis.itemapi.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.useless.task.AsyncTask;
import me.retrorealms.practiceserver.mechanics.enchants.Enchants;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Giovanni on 8-7-2017.
 */
public class BuffHandler implements Listener {

    private LootBuff activeBuff;
    private int improvedDrops = 0;
    private boolean active = false;

    public void init() {

        /* Time update handler */
        new AsyncTask(() -> {

            if (this.activeBuff == null)
                return;

            this.activeBuff.update();

            if (this.activeBuff.expired())
                this.endBuff();

        }).setUseSharedPool(true).setInterval(1).scheduleRepeatingTask();
    }

    public void updateImprovedDrops() {
        int buff = getActiveBuff().getUpdate();
        if(new Random().nextInt(100 + buff) < buff){
            this.improvedDrops += 1;
        }
    }

    public int getImprovedDrops() {
        return improvedDrops;
    }

    public boolean isActive() {
        return this.activeBuff != null;
    }

    public LootBuff getActiveBuff() {
        return activeBuff;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getEquipment().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) return;

        if (!this.isBuffItem(itemStack)) return;

        event.setCancelled(true);

        LootBuff lootBuff = this.readItem(itemStack);
        if (lootBuff == null) {
            return;
        }
        if(Duels.duelers.containsKey(player)){
            player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use this item while in a duel");
            return;
        }
        if (this.activeBuff == null) {
            this.activeBuff = lootBuff;

            this.activeBuff.activate(player.getUniqueId());
            player.getInventory().setItemInMainHand(null);
        } else {
            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAnother player has already activated a loot buff, wait until it expires!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 10F, 1F);
        }
    }

    public void endBuff() {
        this.activeBuff.end();
        this.activeBuff = null;
        this.improvedDrops = 0;
    }

    public boolean isBuffItem(ItemStack itemStack) {
        NBTAccessor nbtAccessor = new NBTAccessor(itemStack);

        if (!nbtAccessor.hasTag()) return false;

        return nbtAccessor.hasKey("buff.Item");
    }

    public LootBuff readItem(ItemStack itemStack) {
        if (!this.isBuffItem(itemStack)) return null;

        NBTAccessor nbtAccessor = new NBTAccessor(itemStack);
        if (nbtAccessor.hasKey("buff.ItemData")) {

            String in = nbtAccessor.getString("buff.ItemData");

            return new Gson().fromJson(in, LootBuff.class);
        }
        return null;
    }

    public ItemStack newBuffItem(String ownerName, UUID ownerId, int improvementRate) {
        LootBuff lootBuff = new LootBuff(ownerName, ownerId, improvementRate);

        ItemStack itemStack = new ItemStack(Material.EXP_BOTTLE);
        ItemMeta itemMeta = itemStack.getItemMeta();

        int percentage = improvementRate / 2;

        List<String> unfixedLore = Arrays.asList(
                "",
                "&d- Increases monster drop rate by " + improvementRate + "%",
                "&d- Increases elite drop rate by " + percentage + "%",
                "&d- Expires after 30 minutes",
                "",
                "&aThank you for supporting the server!");

        List<String> fixedLore = Lists.newArrayList();

        unfixedLore.forEach(string -> {
            fixedLore.add(ChatColor.translateAlternateColorCodes('&', string));
        });

        itemMeta.setLore(fixedLore);
        itemMeta.setDisplayName(ChatColor.GOLD + "LOOT BUFF " + ChatColor.RED + "(30 MINUTES)");
        itemStack.addEnchantment(Enchants.glow, 1);
        itemStack.setItemMeta(itemMeta);

        NBTAccessor nbtAccessor = new NBTAccessor(itemStack).check();
        nbtAccessor.setString("buff.Item", "unexpired");
        nbtAccessor.setString("buff.ItemData", new Gson().toJson(lootBuff));

        return nbtAccessor.update();
    }
}
