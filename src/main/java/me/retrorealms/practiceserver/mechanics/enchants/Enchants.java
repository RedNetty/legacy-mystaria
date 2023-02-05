package me.retrorealms.practiceserver.mechanics.enchants;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.mechanics.vendors.ItemVendors;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

public class Enchants implements Listener {
    public static Enchantment glow;

    static {
        Enchants.glow = new GlowEnchant(69);
    }

    public static boolean registerNewEnchantment() {
        try {
            final Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            try {
                Enchantment.registerEnchantment(Enchants.glow);
                return true;
            } catch (IllegalArgumentException ex) {
            }
        } catch (Exception ex2) {
        }
        return false;
    }

    public static int getPlus(final ItemStack is) {
        if (is.getItemMeta().hasDisplayName()) {
            String name = ChatColor.stripColor(is.getItemMeta().getDisplayName());
            if (name.startsWith("[+")) {
                name = name.split("\\[+")[1].split("\\]")[0];
                try {
                    return Integer.parseInt(name);
                } catch (Exception e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public void onEnable() {
        PracticeServer.log.info("[Enchants] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        registerNewEnchantment();
    }

    public void onDisable() {
        PracticeServer.log.info("[Enchants] has been disabled.");
    }

    @EventHandler
    public void onMapUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand() == null) return;
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;

        if (player.getInventory().getItemInMainHand().getType() == Material.EMPTY_MAP) event.setCancelled(true);
    }

    @EventHandler
    public void onProtectionApply(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;

        ItemStack itemStack = event.getCursor();
        Player player = (Player) event.getWhoClicked();

        if (!ItemAPI.isProtectionScroll(itemStack)) return;

        ItemStack itemStack1 = event.getCurrentItem();

        if (ItemAPI.isProtected(itemStack1)) {

            player.sendMessage(ChatColor.RED + "ITEM ALREADY PROTECTED");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 10F, 1F);

            return;
        }

        if (!ItemAPI.canEnchant(itemStack1, itemStack)) {
            player.sendMessage(ChatColor.RED + "ITEM CAN'T BE PROTECTED: MUST BE THE SAME TIER");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 10F, 1F);

            return;
        }
        event.setCancelled(true);
        event.setCurrentItem(ItemAPI.makeProtected(itemStack1));

        player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "       ->  ITEM PROTECTED");
        player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "       " + itemStack1.getItemMeta().getDisplayName());
        Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.GREEN).withFade(Color.GREEN).with(FireworkEffect.Type.STAR).trail(true).build();
        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);

        if (itemStack.getAmount() == 1) {
            event.setCursor(new ItemStack(Material.AIR));
        } else {
            ItemStack newStack = itemStack.clone();
            newStack.setAmount(newStack.getAmount() - 1);
            event.setCursor(newStack);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInvClick(final InventoryClickEvent e) throws Exception {
        final Player p = (Player) e.getWhoClicked();
        if (!e.getInventory().getName().equalsIgnoreCase("container.crafting")) {
            return;
        }
        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            return;
        }
        if (e.getCursor() != null && e.getCursor().getType() == Material.EMPTY_MAP
                && e.getCursor().getItemMeta().getDisplayName() != null
                && e.getCursor().getItemMeta().getDisplayName().contains("Armor")
                && e.getCurrentItem() != null
                && (e.getCurrentItem().getType().name().contains("_HELMET")
                || e.getCurrentItem().getType().name().contains("_CHESTPLATE")
                || e.getCurrentItem().getType().name().contains("_LEGGINGS")
                || e.getCurrentItem().getType().name().contains("_BOOTS"))
                && e.getCurrentItem().getItemMeta().getLore() != null
                && e.getCurrentItem().getItemMeta().hasDisplayName()
                && ((Items.isBlueLeather(e.getCurrentItem()) && e.getCurrentItem().getType().name().contains("LEATHER_") && e.getCursor().getItemMeta().getDisplayName().contains("Frozen"))
                || (e.getCurrentItem().getType().name().contains("GOLD_") && e.getCursor().getItemMeta().getDisplayName().contains("Gold"))
                || (e.getCurrentItem().getType().name().contains("DIAMOND_") && e.getCursor().getItemMeta().getDisplayName().contains("Diamond"))
                || (e.getCurrentItem().getType().name().contains("IRON_") && e.getCursor().getItemMeta().getDisplayName().contains("Iron"))
                || (e.getCurrentItem().getType().name().contains("CHAINMAIL_") && e.getCursor().getItemMeta().getDisplayName().contains("Chainmail"))
                || (!Items.isBlueLeather(e.getCurrentItem()) && e.getCurrentItem().getType().name().contains("LEATHER_") && e.getCursor().getItemMeta().getDisplayName().contains("Leather")))) {
            final List<String> curlore = e.getCurrentItem().getItemMeta().getLore();
            String name = e.getCurrentItem().getItemMeta().getDisplayName();
            if (name.startsWith(ChatColor.RED + "[+")) {
                name = name.split("] ")[1];
            }if(Duels.duelers.containsKey(p)){
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use this item while in a duel");
                e.setCancelled(true);
                return;
            }
            if(ItemVendors.isRecentlyInteracted(p)) {
                e.setCancelled(true);
                return;
            }
            final double beforehp = Damage.getHp(e.getCurrentItem());
            final double beforehpgen = Damage.getHps(e.getCurrentItem());
            final int beforenrg = Damage.getEnergy(e.getCurrentItem());
            final int plus = getPlus(e.getCurrentItem());
            if (plus < 3) {
                if (e.getCursor().getAmount() > 1) {
                    e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                } else if (e.getCursor().getAmount() == 1) {
                    e.setCursor(null);
                }
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
                final Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                final FireworkMeta fwm = fw.getFireworkMeta();
                final FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
                fwm.addEffect(effect);
                fwm.setPower(0);
                fw.setFireworkMeta(fwm);
                e.setCancelled(true);
                double added = beforehp * 0.05;
                if (added < 1.0) {
                    added = 1.0;
                }
                final int newhp = (int) (beforehp + added);
                final ItemStack is = e.getCurrentItem();
                final ItemMeta im = is.getItemMeta();
                if (ChatColor.stripColor(name).contains("[+")) {
                    name = name.replace("[+" + (plus) + "] ", "");
                }
                im.setDisplayName(ChatColor.RED + "[+" + (plus + 1) + "] " + name);
                final List<String> lore = im.getLore();
                lore.set(1, ChatColor.RED + "HP: +" + newhp);
                if (curlore.get(2).contains("ENERGY REGEN")) {
                    lore.set(2, ChatColor.RED + "ENERGY REGEN: +" + (beforenrg + 1) + "%");
                } else if (curlore.get(2).contains("HP REGEN")) {
                    double addedhps = beforehpgen * 0.05;
                    if (addedhps < 1.0) {
                        addedhps = 1.0;
                    }
                    final int newhps = (int) (beforehpgen + addedhps);
                    lore.set(2, ChatColor.RED + "HP REGEN: +" + newhps + "/s");
                }
                im.setLore(lore);
                is.setItemMeta(im);
                e.setCurrentItem(is);
                ItemVendors.addToRecentlyInteracted(p);
            }
            if (plus >= 3 && plus < 12) {
                if (e.getCursor().getAmount() > 1) {
                    e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                } else if (e.getCursor().getAmount() == 1) {
                    e.setCursor(null);
                }
                final Random random = new Random();
                final int drop = random.nextInt(100) + 1;
                int doifail = 0;
                int[] chance = {0, 0, 0, 30, 40, 50, 65, 75, 80, 85, 90, 95};
                if(plus > 2 && plus < 12) doifail = chance[plus];
                if (PracticeServer.BETA_VENDOR_ENABLED) {
                    doifail = 0;
                }
                PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
                doifail -= pp.luck * 2;
                e.setCancelled(true);
                if (drop <= doifail) {
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 2.0f, 1.25f);
                    Particles.LAVA.display(0.0f, 0.0f, 0.0f, 5.0f, 10, p.getEyeLocation(), 20.0);

                    if (ItemAPI.isProtected(e.getCurrentItem())) {
                        e.setCurrentItem(ItemAPI.removeProtection(e.getCurrentItem()));
                        e.getWhoClicked().sendMessage(ChatColor.GREEN + "YOUR PROTECTION SCROLL HAS PREVENTED THIS ITEM FROM VANISHING");

                        return;
                    }

                    e.setCurrentItem(null);
                } else {
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
                    final Firework fw2 = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                    final FireworkMeta fwm2 = fw2.getFireworkMeta();
                    final FireworkEffect effect2 = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
                    fwm2.addEffect(effect2);
                    fwm2.setPower(0);
                    fw2.setFireworkMeta(fwm2);
                    e.setCancelled(true);
                    double added2 = beforehp * 0.05;
                    if (added2 < 1.0) {
                        added2 = 1.0;
                    }
                    final int newhp2 = (int) (beforehp + added2);
                    final ItemStack is2 = e.getCurrentItem();
                    final ItemMeta im2 = is2.getItemMeta();
                    if (ChatColor.stripColor(name).contains("[+")) {
                        name = name.replace("[+" + (plus) + "] ", "");
                    }
                    im2.setDisplayName(ChatColor.RED + "[+" + (plus + 1) + "] " + name);
                    final List<String> lore2 = im2.getLore();
                    lore2.set(1, ChatColor.RED + "HP: +" + newhp2);
                    if (curlore.get(2).contains("ENERGY REGEN")) {
                        lore2.set(2, ChatColor.RED + "ENERGY REGEN: +" + (beforenrg + 1) + "%");
                    } else if (curlore.get(2).contains("HP REGEN")) {
                        double addedhps2 = beforehpgen * 0.05;
                        if (addedhps2 < 1.0) {
                            addedhps2 = 1.0;
                        }
                        final int newhps2 = (int) (beforehpgen + addedhps2);
                        lore2.set(2, ChatColor.RED + "HP REGEN: +" + newhps2 + "/s");
                    }
                    im2.setLore(lore2);
                    is2.setItemMeta(im2);
                    is2.addUnsafeEnchantment(Enchants.glow, 1);
                    e.setCurrentItem(is2);
                    e.setCurrentItem(ItemAPI.removeProtection(is2));
                    ItemVendors.addToRecentlyInteracted(p);
                }
            }
        }
        if (e.getCursor() != null && e.getCursor().getType() == Material.EMPTY_MAP
                && e.getCursor().getItemMeta().getDisplayName() != null
                && e.getCursor().getItemMeta().getDisplayName().contains("Weapon")
                && e.getCurrentItem() != null && (e.getCurrentItem().getType().name().contains("_SWORD")
                || e.getCurrentItem().getType().name().contains("_HOE")
                || e.getCurrentItem().getType().name().contains("_SPADE")
                || e.getCurrentItem().getType().name().contains("_AXE"))
                && e.getCurrentItem().getItemMeta().getLore() != null
                && e.getCurrentItem().getItemMeta().hasDisplayName()
                && ((e.getCurrentItem().getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString()) && e.getCurrentItem().getType().name().contains("DIAMOND_") && e.getCursor().getItemMeta().getDisplayName().contains("Frozen"))
                || (e.getCurrentItem().getType().name().contains("GOLD_") && e.getCursor().getItemMeta().getDisplayName().contains("Gold"))
                || (!e.getCurrentItem().getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString()) && e.getCurrentItem().getType().name().contains("DIAMOND_") && e.getCursor().getItemMeta().getDisplayName().contains("Diamond"))
                || (e.getCurrentItem().getType().name().contains("IRON_") && e.getCursor().getItemMeta().getDisplayName().contains("Iron"))
                || (e.getCurrentItem().getType().name().contains("STONE_") && e.getCursor().getItemMeta().getDisplayName().contains("Stone"))
                || (e.getCurrentItem().getType().name().contains("WOOD_") && e.getCursor().getItemMeta().getDisplayName().contains("Wooden")))) {
            String name2 = e.getCurrentItem().getItemMeta().getDisplayName();
            if (name2.startsWith(ChatColor.RED + "[+")) {
                name2 = name2.split("] ")[1];
            }if(Duels.duelers.containsKey(p)){
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use this item while in a duel");
                e.setCancelled(true);
                return;
            }
            if(ItemVendors.isRecentlyInteracted(p)) {
                e.setCancelled(true);
                return;
            }
            final double beforemin = Damage.getDamageRange(e.getCurrentItem()).get(0);
            final double beforemax = Damage.getDamageRange(e.getCurrentItem()).get(1);
            final int plus2 = getPlus(e.getCurrentItem());
            if (plus2 < 3) {
                if (e.getCursor().getAmount() > 1) {
                    e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                } else if (e.getCursor().getAmount() == 1) {
                    e.setCursor(null);
                }
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
                final Firework fw3 = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                final FireworkMeta fwm3 = fw3.getFireworkMeta();
                final FireworkEffect effect3 = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
                fwm3.addEffect(effect3);
                fwm3.setPower(0);
                fw3.setFireworkMeta(fwm3);
                e.setCancelled(true);
                double addedmin = beforemin * 0.05;
                if (addedmin < 1.0) {
                    addedmin = 1.0;
                }
                final int min = (int) (beforemin + addedmin);
                double addedmax = beforemax * 0.05;
                if (addedmax < 1.0) {
                    addedmax = 1.0;
                }
                final int max = (int) (beforemax + addedmax);
                final ItemStack is3 = e.getCurrentItem();
                final ItemMeta im3 = is3.getItemMeta();
                if (ChatColor.stripColor(name2).contains("[+")) {
                    name2 = name2.replace("[+" + (plus2) + "] ", "");
                }
                im3.setDisplayName(ChatColor.RED + "[+" + (plus2 + 1) + "] " + name2);
                final List<String> lore3 = im3.getLore();
                lore3.set(0, ChatColor.RED + "DMG: " + min + " - " + max);
                im3.setLore(lore3);
                is3.setItemMeta(im3);
                e.setCurrentItem(is3);
                ItemVendors.addToRecentlyInteracted(p);
            }
            if (plus2 >= 3 && plus2 < 12) {
                if (e.getCursor().getAmount() > 1) {
                    e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                } else if (e.getCursor().getAmount() == 1) {
                    e.setCursor(null);
                }
                final Random random2 = new Random();
                final int drop2 = random2.nextInt(100) + 1;
                int doifail2 = 0;
                int[] chance = {0, 0, 0, 30, 40, 50, 65, 75, 80, 85, 90, 95};
                if(plus2 > 2 && plus2 < 12) doifail2 = chance[plus2];
                if (PracticeServer.BETA_VENDOR_ENABLED) {
                    doifail2 = 0;
                }
                PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
                doifail2 -= pp.luck * 2;

                e.setCancelled(true);
                if (drop2 <= doifail2) {
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 2.0f, 1.25f);
                    Particles.LAVA.display(0.0f, 0.0f, 0.0f, 5.0f, 10, p.getEyeLocation(), 20.0);

                    if (ItemAPI.isProtected(e.getCurrentItem())) {
                        e.setCurrentItem(ItemAPI.removeProtection(e.getCurrentItem()));

                        e.getWhoClicked().sendMessage(ChatColor.GREEN + "YOUR PROTECTION SCROLL HAS PREVENTED THIS ITEM FROM VANISHING");

                        return;
                    }

                    e.setCurrentItem(null);
                } else {
                    final Firework fw4 = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                    final FireworkMeta fwm4 = fw4.getFireworkMeta();
                    final FireworkEffect effect4 = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
                    fwm4.addEffect(effect4);
                    fwm4.setPower(0);
                    fw4.setFireworkMeta(fwm4);
                    e.setCancelled(true);
                    double addedmin2 = beforemin * 0.05;
                    if (addedmin2 < 1.0) {
                        addedmin2 = 1.0;
                    }
                    final int min2 = (int) (beforemin + addedmin2);
                    double addedmax2 = beforemax * 0.05;
                    if (addedmax2 < 1.0) {
                        addedmax2 = 1.0;
                    }
                    final int max2 = (int) (beforemax + addedmax2);
                    final ItemStack is4 = e.getCurrentItem();
                    final ItemMeta im4 = is4.getItemMeta();
                    if (ChatColor.stripColor(name2).contains("[+")) {
                        name2 = name2.replace("[+" + (plus2) + "] ", "");;
                    }
                    im4.setDisplayName(ChatColor.RED + "[+" + (plus2 + 1) + "] " + name2);
                    final List<String> lore4 = im4.getLore();
                    lore4.set(0, ChatColor.RED + "DMG: " + min2 + " - " + max2);
                    im4.setLore(lore4);
                    is4.setItemMeta(im4);
                    is4.addUnsafeEnchantment(Enchants.glow, 1);
                    e.setCurrentItem(is4);
                    e.setCurrentItem(ItemAPI.removeProtection(is4));
                    ItemVendors.addToRecentlyInteracted(p);
                }
            }
        }
    }
}
