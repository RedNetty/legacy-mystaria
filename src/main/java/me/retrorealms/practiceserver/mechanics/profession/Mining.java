package me.retrorealms.practiceserver.mechanics.profession;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.PickTrak;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.GemPouches;
import me.retrorealms.practiceserver.mechanics.money.Money;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Mining
        implements Listener {
    ConcurrentHashMap<Location, Integer> regenores = new ConcurrentHashMap<Location, Integer>();
    HashMap<Location, Material> oretypes = new HashMap<Location, Material>();

    public static ItemStack ore(Player p, int tier) {
        Material m = null;
        ChatColor cc = ChatColor.WHITE;
        String name = "";
        String lore = "";
        if (tier == 1) {
            m = Material.COAL_ORE;
            name = "Coal";
            lore = "A chunk of coal ore.";
        }
        if (tier == 2) {
            m = Material.EMERALD_ORE;
            name = "Emerald";
            lore = "An unrefined piece of emerald ore.";
            cc = ChatColor.GREEN;
        }
        if (tier == 3) {
            m = Material.IRON_ORE;
            name = "Iron";
            lore = "A piece of raw iron.";
            cc = ChatColor.AQUA;
        }
        if (tier == 4) {
            m = Material.DIAMOND_ORE;
            name = "Diamond";
            lore = "A sharp chunk of diamond ore.";
            cc = ChatColor.LIGHT_PURPLE;
        }
        if (tier == 5) {
            m = Material.GOLD_ORE;
            name = "Gold";
            lore = "A sparkling piece of gold ore.";
            cc = ChatColor.YELLOW;
        }
        if (tier == 6) {
            m = Material.LAPIS_ORE;
            name = "Frozen";
            lore = "A cold chunk of icy ore";
            cc = ChatColor.BLUE;
        }
        ItemStack is = new ItemStack(m);
        ItemMeta im = is.getItemMeta();

        if (ModerationMechanics.getRank(p) == RankEnum.SUPPORTER || ModerationMechanics.getRank(p) == RankEnum.SUB3) {
            is.setAmount(6);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&lSUPPORTER &7>> &e&lx6 ORE"));
        } else if (ModerationMechanics.getRank(p) == RankEnum.YOUTUBER) {
            is.setAmount(3);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lYT &7>> &e&lx3 ORE"));
        } else if (ModerationMechanics.getRank(p) == RankEnum.BUILDER) {
            is.setAmount(2);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lBUILDER &7>> &e&lx2 ORE"));
        } else if (ModerationMechanics.getRank(p) == RankEnum.QUALITY) {
            is.setAmount(2);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5&lQUALITY &7>> &e&lx2 ORE"));
        } else if (ModerationMechanics.isStaff(p)) {
            is.setAmount(6);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&lSTAFF &7>> &e&lx2 ORE"));
        } else if (ModerationMechanics.getRank(p) == RankEnum.SUB2) {
            is.setAmount(4);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lS++ &7>> &e&lx4 ORE"));
        } else if (ModerationMechanics.getRank(p) == RankEnum.SUB1) {
            is.setAmount(3);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lS+ &7>> &e&lx3 ORE"));
        } else if (ModerationMechanics.getRank(p) == RankEnum.SUB) {
            is.setAmount(2);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lS &7>> &e&lx2 ORE"));
        }
        im.setDisplayName(cc + name + " Ore");
        im.setLore(Arrays.asList(ChatColor.GRAY.toString() + ChatColor.ITALIC + lore));
        is.setItemMeta(im);
        return is;
    }

    public static int getEXPNeeded(int level) {
        if (level == 1) {
            return 157;
        }
        if (level == 120) {
            return 0;
        }
        int previous_level = level - 1;
        return (int) (Math.pow((previous_level), 2) + ((previous_level) * 20) + 80 + ((previous_level) * 4) + getEXPNeeded((previous_level)));
    }

    public void onEnable() {
        PracticeServer.log.info("[Mining] has been enabled.");

        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        MiningConfig.setup();

        for (final String key : MiningConfig.get().getKeys(false)) {
            final String[] str = key.split(",");
            final World world = Bukkit.getWorld(str[0]);
            final Material material = Material.getMaterial(MiningConfig.get().get(key).toString());
            final double x = Double.valueOf(str[1]);
            final double y = Double.valueOf(str[2]);
            final double z = Double.valueOf(str[3]);
            final Location loc = new Location(world, x, y, z);
            if (loc.getBlock() != null) loc.getBlock().setType(material);
        }
        new BukkitRunnable() {

            public void run() {
                for (Location loc : Mining.this.regenores.keySet()) {
                    int time = Mining.this.regenores.get(loc);
                    if (time < 1) {
                        Mining.this.regenores.remove(loc);
                        loc.getBlock().setType(Mining.this.oretypes.get(loc));
                        continue;
                    }
                    Mining.this.regenores.put(loc, --time);
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20, 20);
    }

    public void onDisable() {
        saveOre();
        PracticeServer.log.info("[Mining] has been disabled.");
    }

    public void saveOre() {
        for (Location loc : Mining.this.regenores.keySet()) {
            final String locString = String.valueOf(loc.getWorld().getName()) + "," + (int) loc.getX() + "," + (int) loc.getY()
                    + "," + (int) loc.getZ();
            Material material = oretypes.containsKey(loc) ? oretypes.get(loc) : Material.COAL_ORE;
            MiningConfig.get().set(locString, material.toString());
            MiningConfig.save();
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!p.getInventory().getItemInMainHand().getType().name().contains("_PICKAXE")) {
            return;
        }
        Material m = e.getBlock().getType();

        if (m == Material.COAL_ORE || m == Material.EMERALD_ORE || m == Material.IRON_ORE || m == Material.DIAMOND_ORE || m == Material.GOLD_ORE || m == Material.LAPIS_ORE) {
            Random random = new Random();
            int dura = random.nextInt(2000);
            int failChance = 100;
            if(ProfessionMechanics.getPickEnchants(p.getInventory().getItemInMainHand(), "MINING SUCCESS") > 0) {
                failChance = failChance - (int)(ProfessionMechanics.getPickEnchants(p.getInventory().getItemInMainHand(), "MINING SUCCESS") * 1.4);
            }
            int fail = random.nextInt(failChance);
            if (dura < p.getInventory().getItemInMainHand().getType().getMaxDurability()) {
                p.getInventory().getItemInMainHand().setDurability((short) 0);
            }
            if (p.getInventory().getItemInMainHand().getDurability() >= p.getInventory().getItemInMainHand().getType().getMaxDurability()) {
                p.setItemInHand(null);
            }
            if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                return;
            }
            p.updateInventory();
            this.oretypes.put(e.getBlock().getLocation(), m);
            int oretier = ProfessionMechanics.getOreTier(m);
            int level = ProfessionMechanics.getPickaxeLevel(p.getInventory().getItemInMainHand());
            if (oretier > 0 && oretier <= ProfessionMechanics.getPickaxeTier(p.getInventory().getItemInMainHand())) {
                e.setCancelled(true);
                e.getBlock().setType(Material.STONE);
                if (p.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                    p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                }
                this.regenores.put(e.getBlock().getLocation(), oretier * 30);
                if (fail < ProfessionMechanics.getFailPercent(oretier, level)) {
                    this.addToInv(p, Mining.ore(p, oretier));
                    PickTrak.incrementPickStat(p.getInventory().getItemInMainHand(), "ores", 1);
                    int gemfind = random.nextInt(100);
                    int treasureFind = ThreadLocalRandom.current().nextInt(90);
                    int dore = random.nextInt(100);
                    int tore = random.nextInt(100);
                    if (gemfind < ProfessionMechanics.getPickEnchants(p.getInventory().getItemInMainHand(), "GEM FIND")) {
                        ItemStack gem;
                        ItemMeta gm;
                        int gemamt = 0;
                        if (oretier == 1) {
                            gemamt = random.nextInt(32) + 1;
                        }
                        else if (oretier == 2) {
                            gemamt = random.nextInt(33) + 32;
                        }
                        else if (oretier == 3) {
                            gemamt = random.nextInt(65) + 64;
                        }
                        else if (oretier == 4) {
                            gemamt = random.nextInt(100) + 50;
                        }
                        else if (oretier == 5) {
                            gemamt = random.nextInt(200) + 100;
                        }
                        else if (oretier == 6) {
                            gemamt = random.nextInt(300) + 150;
                        }
                        p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "FOUND " + gemamt + " GEM(s)");
                        PickTrak.incrementPickStat(p.getInventory().getItemInMainHand(), "gems", gemamt);
                        while (gemamt > 0) {
                            gem = new ItemStack(Material.EMERALD, Math.min(gemamt, 64));
                            gm = gem.getItemMeta();
                            gm.setDisplayName(ChatColor.WHITE + "Gem");
                            gm.setLore(Arrays.asList(ChatColor.GRAY + "The currency of Andalucia"));
                            gem.setItemMeta(gm);
                            if(ProfessionMechanics.hasMagnetism(p.getInventory().getItemInMainHand())){
                                GemPouches.onItemPickup(p, gem);
                            }else {
                                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), gem);
                            }
                            gemamt -= 64;
                        }
                    }
                    if (treasureFind < ProfessionMechanics.getPickEnchants(p.getInventory().getItemInMainHand(), "TREASURE FIND")) {
                        int itemType = ThreadLocalRandom.current().nextInt(3);
                        switch (itemType) {
                            case 0:if(ProfessionMechanics.hasMagnetism(p.getInventory().getItemInMainHand())){
                                this.addToInv(p, Items.orb(false));
                            }else{
                                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), Items.orb(false));
                            }
                            p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "FOUND TREASURE!" + " ORB");
                            break;
                            case 1:
                                if(ProfessionMechanics.hasMagnetism(p.getInventory().getItemInMainHand())){
                                    this.addToInv(p, Items.enchant(ProfessionMechanics.getOreTier(m), 0, false));
                                }else{
                                    e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), Items.enchant(Math.min(5,ProfessionMechanics.getOreTier(m)), 0, false));
                                }
                                p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "FOUND TREASURE!" + " TIER " + ProfessionMechanics.getOreTier(m) + " WEAPON ENCHANT!");
                                break;
                            case 2:
                                if(ProfessionMechanics.hasMagnetism(p.getInventory().getItemInMainHand())){
                                    this.addToInv(p, Items.enchant(ProfessionMechanics.getOreTier(m), 1, false));
                                }else{
                                    e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), Items.enchant(Math.min(5,ProfessionMechanics.getOreTier(m)), 1, false));
                                }
                                p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "FOUND TREASURE!" + " TIER " + ProfessionMechanics.getOreTier(m) + " ARMOR ENCHANT!");
                                break;
                            case 3:
                                int gemNoteAmount = ThreadLocalRandom.current().nextInt(120) + 1;
                                if(ProfessionMechanics.hasMagnetism(p.getInventory().getItemInMainHand())){
                                    this.addToInv(p, Money.createBankNote(gemNoteAmount * ProfessionMechanics.getOreTier(m)));
                                }else{
                                    e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), Money.createBankNote(gemNoteAmount * ProfessionMechanics.getOreTier(m)));
                                }
                                p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "FOUND TREASURE!" + " A GEM BANK NOTE WORTH " + gemNoteAmount * ProfessionMechanics.getOreTier(m) + "g!");
                                break;
                        }

                    }
                    if (dore < ProfessionMechanics.getPickEnchants(p.getInventory().getItemInMainHand(), "DOUBLE ORE")) {
                        p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "DOUBLE ORE DROP" + ChatColor.YELLOW + " (2x)");
                        this.addToInv(p, Mining.ore(p, oretier));
                    }
                    if (tore < ProfessionMechanics.getPickEnchants(p.getInventory().getItemInMainHand(), "TRIPLE ORE")) {
                        p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "TRIPLE ORE DROP" + ChatColor.YELLOW + " (3x)");
                        this.addToInv(p, Mining.ore(p, oretier));
                        this.addToInv(p, Mining.ore(p, oretier));
                    }
                    int xp = ProfessionMechanics.getExpFromOre(oretier);
                    ProfessionMechanics.addExp(p, p.getInventory().getItemInMainHand(), xp, true);
                } else {
                    e.setCancelled(true);
                    p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "You fail to gather any ore.");
                    e.getBlock().getWorld().playEffect(e.getBlock().getLocation(), Effect.STEP_SOUND, Material.STONE);
                }
            } else {
                e.setCancelled(true);
                p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "You cannot mine this ore.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && (event.getItem() != null) && (event.getItem().getType().toString().contains("_PICKAXE")) && (event.getClickedBlock().getType() != Material.AIR)) {
            Block clickedBlock = event.getClickedBlock();
            switch (clickedBlock.getType()) {
                case COAL_ORE:
                case EMERALD_ORE:
                case IRON_ORE:
                case DIAMOND_ORE:
                case GOLD_ORE:
                case LAPIS_ORE:
                    if (!player.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 120, 3, false, false));
                    }
                    break;
            }
        }
    }


    public static void addToInv(Player p, ItemStack is) {
        ItemStack[] arritemStack = p.getInventory().getContents();
        int n = arritemStack.length;
        int n2 = 0;
        while (n2 < n) {
            ItemStack i = arritemStack[n2];
            if (i != null && i.getType() != Material.AIR && (i.getAmount()) < 64 && i.getType() == is.getType() && i.getItemMeta().equals(is.getItemMeta())) {
                p.getInventory().addItem(is);
                return;
            }
            ++n2;
        }
        int slot = p.getInventory().firstEmpty();
        if (slot != -1) {
            p.getInventory().setItem(slot, is);
        } else {
            p.getWorld().dropItemNaturally(p.getLocation(), is);
        }
    }


}

