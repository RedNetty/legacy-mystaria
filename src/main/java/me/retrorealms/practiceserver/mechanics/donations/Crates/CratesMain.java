package me.retrorealms.practiceserver.mechanics.donations.Crates;
import lombok.Getter;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.donations.Nametags.Nametag;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.PickTrak;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.WepTrak;
import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.enchants.Enchants;
import me.retrorealms.practiceserver.mechanics.enchants.Orbs;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.item.scroll.ScrollGUI;
import me.retrorealms.practiceserver.mechanics.item.scroll.ScrollGenerator;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.utils.Particles;
import me.retrorealms.practiceserver.utils.StringUtil;
import me.retrorealms.practiceserver.utils.item.ItemGenerator;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class CratesMain implements Listener {

    private HashMap<Player, Integer> countMap = new HashMap<>();
    private HashMap<Player, Integer> openingCrateList = new HashMap<>();
    private HashMap<Player, Integer> timerMap = new HashMap<>();
    private ArrayList<Player> hallowList = new ArrayList<>();

    public static void doFirework(Player p) {
        Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
    }

    public static ItemStack createKey() {
        ItemStack crate = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta cm = crate.getItemMeta();
        cm.setDisplayName(ChatColor.AQUA + "Crate key");
        cm.setLore(Arrays.asList(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "This key is used for locked crates."));
        crate.setItemMeta(cm);
        return crate;
    }

    public static ItemStack createCrate(int tier, boolean halloween) {
        ItemStack crate = new ItemStack(Material.TRAPPED_CHEST);
        if (halloween) crate = new ItemStack(Material.PUMPKIN);
        ItemMeta cm = crate.getItemMeta();
        String fLine = ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "Inside:";
        switch (tier) {
            case 6:
                cm.setDisplayName(ChatColor.BLUE + "Frozen Loot Crate");
                if (halloween) cm.setDisplayName(ChatColor.BLUE + "Frozen Halloween Loot Crate");
                fLine = fLine + ChatColor.BLUE + " Randomized Piece of Tier 6";
                break;
            case 5:
                cm.setDisplayName(ChatColor.YELLOW + "Legendary Loot Crate");
                if (halloween) cm.setDisplayName(ChatColor.YELLOW + "Legendary Halloween Loot Crate");
                fLine = fLine + ChatColor.YELLOW + " Randomized Piece of Tier 5";
                break;
            case 4:
                cm.setDisplayName(ChatColor.LIGHT_PURPLE + "Ancient Loot Crate");
                if (halloween) cm.setDisplayName(ChatColor.LIGHT_PURPLE + "Ancient Halloween Loot Crate");
                fLine = fLine + ChatColor.LIGHT_PURPLE + " Randomized Piece of Tier 4";
                break;
            case 3:
                cm.setDisplayName(ChatColor.AQUA + "War Loot Crate");
                if (halloween) cm.setDisplayName(ChatColor.AQUA + "War Halloween Loot Crate");
                fLine = fLine + ChatColor.AQUA + " Randomized Piece of Tier 3";
                break;
            case 2:
                cm.setDisplayName(ChatColor.GREEN + "Medium Loot Crate");
                if (halloween) cm.setDisplayName(ChatColor.GREEN + "Medium Halloween Loot Crate");
                fLine = fLine + ChatColor.GREEN + " Randomized Piece of Tier 2";
                break;
            case 1:
                cm.setDisplayName(ChatColor.WHITE + "Basic Loot Crate");
                if (halloween) cm.setDisplayName(ChatColor.WHITE + "Basic Halloween Loot Crate");
                fLine = fLine + ChatColor.WHITE + " Randomized Piece of Tier 1";
                break;
            case 0:
                cm.setDisplayName(ChatColor.WHITE + "Vote Crate");
                if (halloween) cm.setDisplayName(ChatColor.WHITE + "Halloween Voting Crate");
                fLine = fLine + ChatColor.WHITE + " Rarest Item - Loot Buff (35%)";
                break;
            default:
                cm.setDisplayName(ChatColor.WHITE + "Basic Loot Crate");
                if (halloween) cm.setDisplayName(ChatColor.WHITE + "Basic Halloween Loot Crate");
                fLine = fLine + ChatColor.WHITE + " Randomized Piece of Tier 1";
                break;
        }
        cm.setLore(Arrays.asList(fLine, ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Unlocked loot crate.", ChatColor.GRAY + "Right-Click in hand to get scrap", ChatColor.GRAY + "Click the item in your inventory to open"));
        if (halloween) crate.addUnsafeEnchantment(Enchants.glow, 1);
        crate.setItemMeta(cm);
        return crate;
    }

    public void onEnable() {
        PracticeServer.log.info("[PracticeServer] donations Enabled");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[PracticeServer] donations Disabled");

    }
    public HashMap<Player, Integer> getCountMap() {
        return countMap;
    }
    public HashMap<Player, Integer> getOpeningCrateList() {
        return openingCrateList;
    }

    @EventHandler
    public void onCClick(InventoryClickEvent event) {
        final Player p = (Player) event.getWhoClicked();
        if (event.getInventory().getTitle().equalsIgnoreCase("Crate Opening")) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onClick(InventoryClickEvent event) {

        final Player p = (Player) event.getWhoClicked();
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) {
            return;
        }
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            return;
        }
        if (event.getCurrentItem() != null && (event.getCurrentItem().getType() == Material.TRAPPED_CHEST || event.getCurrentItem().getType() == Material.PUMPKIN) && event.getCurrentItem().getItemMeta().hasDisplayName() &&
                (event.getCurrentItem().getItemMeta().getDisplayName().contains("Loot Crate") || event.getCurrentItem().getItemMeta().getDisplayName().contains("Vote Crate"))) {
            final ItemStack is = event.getCurrentItem();
            if (getOpeningCrateList().containsKey(p)) {
                p.sendMessage(ChatColor.RED + "ERROR:" + ChatColor.GRAY + " Please wait till later or try again.");
                return;
            }

            if (p.getInventory().firstEmpty() == -1) {
                p.sendMessage(ChatColor.RED + "ERROR:" + ChatColor.GRAY + "You cannot open crates while your inventory is full!");
                return;
            }
            if (getCountMap().containsKey(p)) getCountMap().remove(p);
            if (getOpeningCrateList().containsKey(p)) getOpeningCrateList().remove(p);
            if (timerMap.containsKey(p)) timerMap.remove(p);

            p.closeInventory();

            boolean halloween = false;
            if (is.getItemMeta().getDisplayName().contains("Halloween")) {
                halloween = true;
            }
            if(is.getItemMeta().getDisplayName().contains("Vote")) {
                openCrate(p,0, halloween);
            }
            if (is.getItemMeta().getDisplayName().contains("Legendary")) {
                openCrate(p, 5, halloween);
            } else if (is.getItemMeta().getDisplayName().contains("Ancient")) {
                openCrate(p, 4, halloween);
            } else if (is.getItemMeta().getDisplayName().contains("War")) {
                openCrate(p, 3, halloween);
            } else if (is.getItemMeta().getDisplayName().contains("Medium")) {
                openCrate(p, 2, halloween);
            } else if (is.getItemMeta().getDisplayName().contains("Basic")) {
                openCrate(p, 1, halloween);
            } else if (is.getItemMeta().getDisplayName().contains("Frozen")) {
                openCrate(p, 6, halloween);
            }
            event.setCancelled(true);
            if (is.getAmount() > 1) {
                is.setAmount(is.getAmount() - 1);
            } else {
                event.setCurrentItem(new ItemStack(Material.AIR));
            }
        }
    }

    public void openCrate(Player player, int tier, boolean halloween) {
        try {
            Inventory inventory = Bukkit.createInventory(null, 9, "Crate Opening");
            player.openInventory(inventory);
            if (halloween) {
                hallowList.add(player);
                IntStream.range(0, 12).forEach(intConsumer -> {
                    Location location = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY() + 1.35, player.getLocation().getZ());
                    Entity entity = player.getWorld().spawnEntity(location, EntityType.BAT);
                    new CreatureSpawnEvent((LivingEntity) entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            entity.remove();
                            Particles.EXPLOSION_NORMAL.display(0.2F, 0.3F, 0.2F, 0, 3, entity.getLocation(), 30);
                            this.cancel();
                        }
                    }.runTaskLaterAsynchronously(PracticeServer.getInstance(), 40L);
                });
                Particles.MOB_APPEARANCE.display(0, 0, 0, 0, 1, player.getLocation(), 1);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 1F, 1F);
            }
            IntStream.range(0, inventory.getSize()).forEach(slot -> {
                if (slot != 4) {
                    inventory.setItem(slot, new ItemGenerator(Material.STAINED_GLASS_PANE).setDurability((short) 8).build());
                }
            });

            if (countMap.containsKey(player)) {
                countMap.remove(player);
            }
            Random random = new Random();
            getOpeningCrateList().put(player, tier);
            countMap.put(player, 0);
            timerMap.put(player, Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.getInstance(), new Runnable() {
                @Override
                public void run() {
                    int paneColor = random.nextInt(8);

                    /**
                     Orbs: 10%
                     Common: 40%
                     Uncommon: 30%
                     Rare: 10
                     Unique: 5
                     */
                    try {
                        IntStream.range(0, inventory.getSize()).forEach(slot -> {
                            if (slot != 4) {
                                if (halloween) {
                                    Particles.FLAME.display(0.1F, 0.5F, 0.1F, 0, 40, player.getLocation(), 30);
                                    int halloweenColor = random.nextInt(2);
                                    switch (halloweenColor) {
                                        case 0:
                                            inventory.setItem(slot, new ItemGenerator(Material.STAINED_GLASS_PANE).setDurability((short) 1).build());
                                            break;
                                        case 1:
                                            inventory.setItem(slot, new ItemGenerator(Material.STAINED_GLASS_PANE).setDurability((short) 10).build());
                                            break;
                                    }
                                } else {
                                    inventory.setItem(slot, new ItemGenerator(Material.STAINED_GLASS_PANE).setDurability((short) paneColor).build());
                                }
                            }
                        });
                        if (CratesMain.this.getCountMap().get(player) <= 16) {
                            if (CratesMain.this.getCountMap().get(player) < 14) {
                                if (halloween) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 10.0f, 10.0f);
                                } else {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 10.0f, 10.0f);
                                }
                                ItemStack itemStack = randomCrateItem(halloween, tier, player);
                                if(tier == 0) itemStack = voteCrateItem(player);
                                inventory.setItem(4, itemStack);
                            }
                            if (CratesMain.this.getCountMap().get(player) == 14) {
                                if (inventory.getItem(4).getType() != Material.BARRIER) {
                                    CratesMain.doFirework(player);
                                } else {
                                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2.0f, 1.25f);
                                    if (halloween) {
                                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, 10.0f, 10.0f);
                                    }
                                    Particles.LAVA.display(0.0f, 0.0f, 0.0f, 5.0f, 10, player.getEyeLocation(), 20.0);
                                }
                            }
                            if (CratesMain.this.getCountMap().get(player) == 16) {
                                CratesMain.this.getOpeningCrateList().remove(player);
                                countMap.remove(player);
                                hallowList.remove(player);
                                player.closeInventory();
                                Bukkit.getScheduler().cancelTask(timerMap.get(player));
                                CratesMain.this.timerMap.remove(player);
                                if (inventory.getItem(4).getType() != Material.BARRIER) {
                                    player.getInventory().addItem(inventory.getItem(4));
                                    if (halloween) {
                                        PracticeServer.getManagerHandler().getHalloween().trickOrTreat(player, true);
                                    }
                                }
                            }
                            if (getCountMap().containsKey(player)) {
                                CratesMain.this.getCountMap().put(player, CratesMain.this.getCountMap().get(player) + 1);
                            }
                        }
                    }catch (Exception e) {

                    }
                }
            }, 3L, 3L));
        }catch (Exception e) {}
    }

    @EventHandler
    public void onClick(InventoryCloseEvent event) {
        try {
            if (event.getPlayer() instanceof Player) {
                final Player p = (Player) event.getPlayer();
                if (openingCrateList.containsKey(p)) {
                    if (event.getInventory().getTitle().equalsIgnoreCase("Crate Opening") && event.getInventory().getItem(4).getType() != Material.BARRIER) {
                        if (getCountMap().get(p) >= 14) {
                            p.getInventory().addItem(event.getInventory().getItem(4));
                        } else {
                            ItemStack itemStack;
                            if(event.getInventory().getTitle().toLowerCase().contains("vote")) {
                                itemStack = voteCrateItem(p);
                            } else if (hallowList.contains(p)) {
                                itemStack = randomCrateItem(true, getOpeningCrateList().get(p), p);
                                PracticeServer.getManagerHandler().getHalloween().trickOrTreat(p, true);
                            } else {
                                itemStack = randomCrateItem(false, getOpeningCrateList().get(p), p);
                            }
                            if(itemStack == null) {
                               itemStack = Items.orb(false);
                               itemStack.setAmount(13);
                            }
                            p.getInventory().addItem(itemStack);

                        }
                    }
                    hallowList.remove(p);
                    Bukkit.getScheduler().cancelTask(timerMap.get(p));
                    timerMap.remove(p);
                    countMap.remove(p);
                    event.getInventory().clear();
                    openingCrateList.remove(p);
                }
            }
        }catch(Exception e){
            System.out.println("Some retard closed their inv too soon in a crate");
        }
    }
    public ItemStack voteCrateItem(Player p) {
        int buffAmount = ThreadLocalRandom.current().nextInt(15,35);
        int randomNumber = ThreadLocalRandom.current().nextInt(1, 100);
        int orbAmt = ThreadLocalRandom.current().nextInt(10, 12);
        ItemStack itemStack;
        if (randomNumber >= 80) {
            int legOrb = ThreadLocalRandom.current().nextInt(0, 1);
            if (legOrb == 1) {
                orbAmt = 1;
                itemStack = Items.legendaryOrb(false);
            }
            itemStack = Items.orb(false);
            itemStack.setAmount(orbAmt);
            return itemStack;
        } else if (randomNumber >= 50) {
            return Money.createBankNote(ThreadLocalRandom.current().nextInt(2500, 5000));
        } else if (randomNumber >= 25) {
            return Items.enchant(5, 0, false);
        } else if (randomNumber >= 15) {
            return Items.enchant(5, 1, false);
        } else if (randomNumber >= 6) {
            return new ScrollGenerator().next(4).clone();
        } else if (randomNumber <= 5) {
            return PracticeServer.buffHandler().newBuffItem(p.getName(), p.getUniqueId(), buffAmount);
        }
        return Nametag.item_ownership_tag.clone();
    }


    public ItemStack randomCrateItem(boolean halloween, int tier, Player p) {
        Random random = new Random();
        int randomizedItem = random.nextInt(8);
        int randomnum = random.nextInt(100);
        int legendaryOrbChance = random.nextInt(4);
        int rarity = 0;
        if (randomnum >= 80) {
            ItemStack itemStack;
            int orbAmount = ThreadLocalRandom.current().nextInt(1, 3);
            orbAmount *= tier;
            if (halloween) orbAmount *= 2;
            itemStack = Items.orb(false);
            itemStack.setAmount(orbAmount);
            return itemStack;
        } else if (randomnum >= 50) {
            rarity = 1;
        } else if (randomnum >= 25) {
            rarity = 2;
        } else if (randomnum >= 15) {
            rarity = 3;
        } else if (randomnum >= 10) {
            rarity = 4;
        } else if (randomnum >= 5) {
            return WepTrak.weapon_tracker_item;
        } else if (randomnum >= 3) {
            return PickTrak.pickaxe_tracker_item;
        } else if (randomnum >= 0) {
            return Nametag.item_ownership_tag;
        }
        return halloween && legendaryOrbChance == 3 ? Orbs.randomizeLegendaryStats(CreateDrop.createDrop(tier, randomizedItem, rarity), p) : CreateDrop.createDrop(tier, randomizedItem, rarity);
    }
}