package me.retrorealms.practiceserver.mechanics.item;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Durability implements Listener {
    public static ItemStack scrap(final int tier) {
        Material m = null;
        ChatColor cc = ChatColor.WHITE;
        String name = "";
        String lore = "";
        short dura = 0;
        if (tier == 1) {
            m = Material.LEATHER;
            name = "Leather";
            lore = "Leather";
        }
        if (tier == 2) {
            m = Material.IRON_FENCE;
            name = "Chainmail";
            lore = "Chainmail";
            cc = ChatColor.GREEN;
        }
        if (tier > 2) {
            m = Material.INK_SACK;
        }
        if (tier == 3) {
            name = "Iron";
            lore = "Iron";
            dura = 7;
            cc = ChatColor.AQUA;
        }
        if (tier == 4) {
            name = "Diamond";
            lore = "Diamond";
            dura = 12;
            cc = ChatColor.LIGHT_PURPLE;
        }
        if (tier == 5) {
            name = "Golden";
            lore = "Gold";
            dura = 11;
            cc = ChatColor.YELLOW;
        }
        if (tier == 6) {
            name = "Frozen";
            lore = "Frozen";
            dura = 6;
            cc = ChatColor.BLUE;
        }
        final ItemStack is = new ItemStack(m);
        final ItemMeta im = is.getItemMeta();
        im.setDisplayName(cc + name + " Armor Scrap");
        im.setLore((List<String>) Arrays.asList(ChatColor.GRAY + "Recovers 3% Durability of " + cc + lore + " Equipment"));
        is.setItemMeta(im);
        is.setDurability(dura);
        return is;
    }

    public static float getDuraPercent(final ItemStack is) {
        final float max = is.getType().getMaxDurability();
        final float curr = is.getType().getMaxDurability() - is.getDurability();
        final float dura = curr / max * 100.0f;
        return Math.round(dura);
    }

    public static ItemStack addDura(final ItemStack is, final float amt) {
        final float add = amt / 100.0f;
        final float max = is.getType().getMaxDurability();
        float adding = max * add;
        if (adding < 1.0f) {
            adding = 1.0f;
        }
        adding = Math.round(adding);
        short dura = (short) (is.getDurability() - adding);
        if (dura < 0) {
            dura = 0;
        }
        is.setDurability(dura);
        return is;
    }

    public static ItemStack takeDura(final ItemStack is, final float amt) {
        final float add = amt / 100.0f;
        final float max = is.getType().getMaxDurability();
        float adding = max * add;
        if (adding < 1.0f) {
            adding = 1.0f;
        }
        adding = Math.round(adding);
        short dura = (short) (is.getDurability() + adding);
        if (dura < 0) {
            dura = 0;
        }
        is.setDurability(dura);
        return is;
    }

    public static boolean isArmor(final ItemStack is) {
        return is != null && is.getType() != Material.AIR && (is.getType().name().contains("_HELMET") || is.getType().name().contains("_CHESTPLATE") || is.getType().name().contains("_LEGGINGS") || is.getType().name().contains("_BOOTS") || is.getType().name().contains("_SWORD") || is.getType().name().contains("_AXE") || is.getType().name().contains("_HOE") || is.getType().name().contains("_SPADE") || is.getType().name().contains("_PICKAXE"));
    }

    public static boolean isScrap(final ItemStack is) {
        if (is == null) {
            return false;
        }
        if (is.getType() == Material.AIR) {
            return false;
        }
        if (!is.getItemMeta().hasDisplayName()) {
            return false;
        }
        if (!is.getItemMeta().getDisplayName().contains("Armor Scrap")) {
            return false;
        }
        if (is.getType() == Material.LEATHER) {
            return true;
        }
        if (is.getType() == Material.IRON_FENCE) {
            return true;
        }
        if (is.getType() == Material.INK_SACK) {
            final short dura = is.getDurability();
            if (dura == 7 || dura == 12 || dura == 11 || dura == 6) {
                return true;
            }
        }
        return false;
    }

    public void onEnable() {
        PracticeServer.log.info("[Durability] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.getInstance());
    }

    public void onDisable() {
        PracticeServer.log.info("[Durability] has been disabled.");
    }

    @EventHandler
    public void onDamage(final EntityDamageByEntityEvent e) {
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (e.getDamager() instanceof Player) {
            final Player d = (Player) e.getDamager();
            if (d.getInventory().getItemInMainHand() == null || d.getInventory().getItemInMainHand().getType() == Material.AIR || e.getDamage() == 1.0) {
                return;
            }
        }
        if (e.getEntity() instanceof Player) {
            final Player p = (Player) e.getEntity();
            final Random r = new Random();
            final int dodura = r.nextInt(1500);
            if(Listeners.isWeapon(p.getInventory().getItemInMainHand())) {
                p.getInventory().getItemInMainHand().setDurability((short) 0);
            }
            if (p.getInventory().getHelmet() != null && p.getInventory().getHelmet().getType() != Material.AIR) {
                final ItemStack is = p.getInventory().getHelmet();
                if(p.getInventory().getHelmet().getType() != Material.JACK_O_LANTERN) {
                    if (dodura <= is.getType().getMaxDurability()) {
                        if (is.getDurability() >= is.getType().getMaxDurability()) {
                            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                            Listeners.hpCheck(p);
                        } else {
                            is.setDurability((short) 0);
                        }
                    }
                }
            }
            if (p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() != Material.AIR) {
                final ItemStack is = p.getInventory().getChestplate();
                if (dodura <= is.getType().getMaxDurability()) {
                    if (is.getDurability() >= is.getType().getMaxDurability()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                        p.getInventory().setChestplate((ItemStack) null);
                        Listeners.hpCheck(p);
                    } else {
                        is.setDurability((short) 0);
                    }
                }
            }
            if (p.getInventory().getLeggings() != null && p.getInventory().getLeggings().getType() != Material.AIR) {
                final ItemStack is = p.getInventory().getLeggings();
                if (dodura <= is.getType().getMaxDurability()) {
                    if (is.getDurability() >= is.getType().getMaxDurability()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                        p.getInventory().setLeggings((ItemStack) null);
                        Listeners.hpCheck(p);
                    } else {
                        is.setDurability((short) 0);
                    }
                }
            }
            if (p.getInventory().getBoots() != null && p.getInventory().getBoots().getType() != Material.AIR) {
                final ItemStack is = p.getInventory().getBoots();
                if (dodura <= is.getType().getMaxDurability()) {
                    if (is.getDurability() >= is.getType().getMaxDurability()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                        p.getInventory().setBoots((ItemStack) null);
                        Listeners.hpCheck(p);
                    } else {
                        is.setDurability((short) 0);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamager(final EntityDamageByEntityEvent e) {
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (e.getDamager() instanceof Player) {
            final Player p = (Player) e.getDamager();
            if (p.getInventory().getItemInMainHand().getType().getMaxDurability() >= 1) {
                p.getInventory().getItemInMainHand().setDurability((short) 0);
            }
        }
    }

    @EventHandler
    public void onDuraUpdate(final EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            final Player p = (Player) e.getDamager();
            p.updateInventory();
        }
    }
}
