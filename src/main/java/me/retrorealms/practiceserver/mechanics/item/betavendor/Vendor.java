package me.retrorealms.practiceserver.mechanics.item.betavendor;


import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.money.Money;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;

public class Vendor implements Listener {

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.getInstance());
    }

    public void onDisable() {
    }


    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Random random = new Random();
        if (e.getInventory().getTitle().equalsIgnoreCase("Gear Selector")) {
            e.setCancelled(true);
            switch (e.getCurrentItem().getType()) {
                case LEATHER_HELMET:
                    p.getInventory().addItem(CreateDrop.createDrop(6, 4, 3));
                    break;
                case LEATHER_CHESTPLATE:
                    p.getInventory().addItem(CreateDrop.createDrop(6, 5, 3));
                    break;
                case LEATHER_LEGGINGS:
                    p.getInventory().addItem(CreateDrop.createDrop(6, 6, 3));
                    break;
                case LEATHER_BOOTS:
                    p.getInventory().addItem(CreateDrop.createDrop(6, 7, 3));
                    break;
                case DIAMOND_SWORD:
                    p.getInventory().addItem(CreateDrop.createDrop(6, 2, 3));
                    break;
                case DIAMOND_AXE:
                    p.getInventory().addItem(CreateDrop.createDrop(6, 3, 3));
                    break;
                case DIAMOND_SPADE:
                    p.getInventory().addItem(CreateDrop.createDrop(6, 1, 3));
                    break;
                case DIAMOND_HOE:
                    p.getInventory().addItem(CreateDrop.createDrop(6, 0, 3));
                    break;
            }
        }
        if (e.getInventory().getTitle().equals("Beta Vendor")) {
            e.setCancelled(true);
            switch (e.getCurrentItem().getType()) {
                case PAPER:
                    p.getInventory().addItem(Money.createBankNote(100000));
                    break;
                case LEATHER_HELMET:
                    p.openInventory(getGearSelection());
                    break;
                case MAGMA_CREAM:
                    ItemStack itemStack = Items.legendaryOrb(false);
                    itemStack.setAmount(64);
                    p.getInventory().addItem(itemStack);
                    break;
                case EMPTY_MAP:
                    if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
                        if (e.getCurrentItem().getItemMeta().getDisplayName().contains("Enchant")) {
                            ItemStack armorEnchant = Items.enchant(6, 1, false);
                            armorEnchant.setAmount(64);
                            ItemStack weaponEnchant = Items.enchant(6, 0, false);
                            weaponEnchant.setAmount(64);
                            p.getInventory().addItem(armorEnchant);
                            p.getInventory().addItem(weaponEnchant);
                        } else {
                            ItemStack protectScroll = ItemAPI.getScrollGenerator().next(5   );
                            protectScroll.setAmount(64);
                            p.getInventory().addItem(protectScroll);

                        }
                    }
            }
        }
    }

    private Inventory getGearSelection() {
        Inventory inv = Bukkit.createInventory(null, 9, "Gear Selector");
        ItemStack helm = new ItemStack(Material.LEATHER_HELMET);
        helm = Items.setItemBlueLeather(helm);
        ItemMeta helmmeta = helm.getItemMeta();
        helmmeta.setDisplayName(ChatColor.BLUE + "Frozen Helmet");
        helmmeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate a random Unique Frozen Helmet"));
        helm.setItemMeta(helmmeta);
        inv.addItem(helm);

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        chest = Items.setItemBlueLeather(chest);
        ItemMeta chestmeta = chest.getItemMeta();
        chestmeta.setDisplayName(ChatColor.BLUE + "Frozen Chestplate");
        chestmeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate a random Unique Frozen Chestplate"));
        chest.setItemMeta(chestmeta);
        inv.addItem(chest);

        ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
        legs = Items.setItemBlueLeather(legs);
        ItemMeta legsmeta = legs.getItemMeta();
        legsmeta.setDisplayName(ChatColor.BLUE + "Frozen Leggings");
        legsmeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate a random Unique Frozen Leggings"));
        legs.setItemMeta(legsmeta);
        inv.addItem(legs);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        boots = Items.setItemBlueLeather(boots);
        ItemMeta bootsmeta = boots.getItemMeta();
        bootsmeta.setDisplayName(ChatColor.BLUE + "Frozen Boots");
        bootsmeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate a random Unique Frozen Boots"));
        boots.setItemMeta(bootsmeta);
        inv.addItem(boots);

        ItemStack staff = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta staffmeta = staff.getItemMeta();
        staffmeta.setDisplayName(ChatColor.BLUE + "Frozen Staff");
        staffmeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate a random Unique Frozen Staff"));
        staff.setItemMeta(staffmeta);
        inv.addItem(staff);

        ItemStack Polearm = new ItemStack(Material.DIAMOND_SPADE);
        ItemMeta Polearmmeta = Polearm.getItemMeta();
        Polearmmeta.setDisplayName(ChatColor.BLUE + "Frozen Polearm");
        Polearmmeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate a random Unique Frozen Polearm"));
        Polearm.setItemMeta(Polearmmeta);
        inv.addItem(Polearm);

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordmeta = sword.getItemMeta();
        swordmeta.setDisplayName(ChatColor.BLUE + "Frozen Sword");
        swordmeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate a random Unique Frozen Sword"));
        sword.setItemMeta(swordmeta);
        inv.addItem(sword);

        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta axemeta = axe.getItemMeta();
        axemeta.setDisplayName(ChatColor.BLUE + "Frozen Axe");
        axemeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate a random Unique Frozen Axe"));
        axe.setItemMeta(axemeta);
        inv.addItem(axe);

        return inv;
    }

    @EventHandler
    public void onBankClick(PlayerInteractEntityEvent e) {
        if ((e.getRightClicked() instanceof HumanEntity)) {
            HumanEntity p = (HumanEntity) e.getRightClicked();
            if (p.getName().equals("Beta Vendor") && PracticeServer.BETA_VENDOR_ENABLED) {
                Inventory inv = org.bukkit.Bukkit.getServer().createInventory(null, 9, "Beta Vendor");
                ItemStack note = new ItemStack(Material.PAPER);
                ItemMeta notemeta = note.getItemMeta();
                notemeta.setDisplayName(ChatColor.GREEN + "Bank Note");
                notemeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate a Bank Note of 100000 Gems"));
                note.setItemMeta(notemeta);
                inv.addItem(note);

                ItemStack helm = new ItemStack(Material.LEATHER_HELMET);
                helm = Items.setItemBlueLeather(helm);
                ItemMeta helmmeta = helm.getItemMeta();
                helmmeta.setDisplayName(ChatColor.BLUE + "Gear");
                helmmeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate Gear"));
                helm.setItemMeta(helmmeta);
                inv.addItem(helm);

                ItemStack enchant = new ItemStack(Material.EMPTY_MAP);
                ItemMeta enchantMeta = enchant.getItemMeta();
                enchantMeta.setDisplayName(ChatColor.YELLOW + "Enchants");
                enchantMeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate Armor and Weapon Enchants"));
                enchant.setItemMeta(enchantMeta);
                inv.addItem(enchant);

                ItemStack protect = new ItemStack(Material.EMPTY_MAP);
                ItemMeta protectMeta = protect.getItemMeta();
                protectMeta.setDisplayName(ChatColor.YELLOW + "Protect Scrolls");
                protectMeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate Armor and Weapon Protect Scrolls"));
                protect.setItemMeta(protectMeta);
                inv.addItem(protect);

                ItemStack orbs = new ItemStack(Material.MAGMA_CREAM);
                ItemMeta orbsMeta = orbs.getItemMeta();
                orbsMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Orbs");
                orbsMeta.setLore(Arrays.asList(ChatColor.GRAY + "Generate 64 Legendary orbs"));
                orbs.setItemMeta(orbsMeta);
                inv.addItem(orbs);

                e.getPlayer().openInventory(inv);
                e.getPlayer().playSound(p.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 1F);
            }
        }
    }
}
