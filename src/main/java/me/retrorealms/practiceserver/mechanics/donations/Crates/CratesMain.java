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

@Getter
public class CratesMain implements Listener {

    // HashMaps to store data related to players and crates
    private final HashMap<Player, Integer> countMap = new HashMap<>();
    private final HashMap<Player, Integer> openingCrateList = new HashMap<>();
    private final HashMap<Player, Integer> timerMap = new HashMap<>();
    private final ArrayList<Player> hallowList = new ArrayList<>();

    // Spawns a firework at the player's location
    public static void doFirework(Player p) {
        Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(false)
                .withColor(Color.YELLOW)
                .withFade(Color.YELLOW)
                .with(FireworkEffect.Type.BURST)
                .trail(true)
                .build();
        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
    }

    // Creates a crate key item stack
    public static ItemStack createKey() {
        ItemStack crate = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta cm = crate.getItemMeta();
        cm.setDisplayName(ChatColor.AQUA + "Crate key");
        cm.setLore(Arrays.asList(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "This key is used for locked crates."));
        crate.setItemMeta(cm);
        return crate;
    }

    // Creates a crate item stack based on the tier and if it's a Halloween crate
    public static ItemStack createCrate(int tier, boolean halloween) {
        ItemStack crate = new ItemStack(Material.TRAPPED_CHEST);
        if (halloween) crate = new ItemStack(Material.PUMPKIN);
        ItemMeta cm = crate.getItemMeta();
        String fLine = ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "Inside:";

        // Set display name and lore based on the tier and if it's a Halloween crate
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

    // Method called when the plugin is enabled
    public void onEnable() {
        PracticeServer.log.info("[PracticeServer] donations Enabled");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    // Method called when the plugin is disabled
    public void onDisable() {
        PracticeServer.log.info("[PracticeServer] donations Disabled");
    }

    // Event handler for inventory click events
    @EventHandler
    public void onCClick(InventoryClickEvent event) {
        final Player p = (Player) event.getWhoClicked();
        if (event.getInventory().getTitle().equalsIgnoreCase("Crate Opening")) {
            event.setCancelled(true);
        }
    }

    // Event handler for inventory click events
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        final Player p = (Player) event.getWhoClicked();

        // Check if the inventory is the crafting table inventory
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) {
            return;
        }

        // Ignore clicks on armor slots
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();

        // Check if the clicked item is a loot crate
        if (currentItem != null && (currentItem.getType() == Material.TRAPPED_CHEST || currentItem.getType() == Material.PUMPKIN) && currentItem.getItemMeta().hasDisplayName() &&
                (currentItem.getItemMeta().getDisplayName().contains("Loot Crate") || currentItem.getItemMeta().getDisplayName().contains("Vote Crate"))) {
            final ItemStack is = currentItem;

            // Check if the player is already opening a crate
            if (getOpeningCrateList().containsKey(p)) {
                p.sendMessage(ChatColor.RED + "ERROR:" + ChatColor.GRAY + " Please wait till later or try again.");
                return;
            }

            // Check if the player's inventory is full
            if (p.getInventory().firstEmpty() == -1) {
                p.sendMessage(ChatColor.RED + "ERROR:" + ChatColor.GRAY + "You cannot open crates while your inventory is full!");
                return;
            }

            // Remove the player's previous crate data
            if (getCountMap().containsKey(p)) getCountMap().remove(p);
            if (getOpeningCrateList().containsKey(p)) getOpeningCrateList().remove(p);
            if (timerMap.containsKey(p)) timerMap.remove(p);

            p.closeInventory();

            boolean halloween = false;

            // Check if it's a Halloween crate
            if (is.getItemMeta().getDisplayName().contains("Halloween")) {
                halloween = true;
            }

            // Open the corresponding crate based on the tier
            if (is.getItemMeta().getDisplayName().contains("Vote")) {
                openCrate(p, 0, halloween);
            } else if (is.getItemMeta().getDisplayName().contains("Legendary")) {
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

            // Reduce the item stack amount or remove it if it's the last one
            if (is.getAmount() > 1) {
                is.setAmount(is.getAmount() - 1);
            } else {
                event.setCurrentItem(new ItemStack(Material.AIR));
            }
        }
    }
    public void openCrate(Player player, int tier, boolean halloween) {
        try {
            Inventory inventory = createCrateInventory(player);
            initializeCrateOpening(player, tier, inventory, halloween);
        } catch (Exception e) {
            // Handle exception
        }
    }

    private Inventory createCrateInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Crate Opening");
        player.openInventory(inventory);
        IntStream.range(0, inventory.getSize()).forEach(slot -> {
            if (slot != 4) {
                inventory.setItem(slot, new ItemGenerator(Material.STAINED_GLASS_PANE).setDurability((short) 8).build());
            }
        });
        return inventory;
    }

    private void initializeCrateOpening(Player player, int tier, Inventory inventory, boolean halloween) {
        countMap.put(player, 0);
        timerMap.put(player, Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.getInstance(), () -> {
            try {
                updateCrateInventory(player, inventory, halloween, tier);
            } catch (Exception e) {
                // Handle exception
            }
        }, 3L, 3L));
    }

    private void updateCrateInventory(Player player, Inventory inventory, boolean halloween, int tier) {
        Random random = new Random();
        int paneColor = random.nextInt(8);
        updatePaneColors(player, inventory, halloween, paneColor);
        if (countMap.get(player) <= 16) {
            updateCrateItem(player, inventory, halloween, tier);
        }
    }

    private void updatePaneColors(Player player, Inventory inventory, boolean halloween, int paneColor) {
        IntStream.range(0, inventory.getSize()).forEach(slot -> {
            if (slot != 4) {
                if (halloween) {
                    updateHalloweenPaneColors(player, inventory, slot);
                } else {
                    inventory.setItem(slot, new ItemGenerator(Material.STAINED_GLASS_PANE).setDurability((short) paneColor).build());
                }
            }
        });
    }

    private void updateHalloweenPaneColors(Player player, Inventory inventory, int slot) {
        Particles.FLAME.display(0.1F, 0.5F, 0.1F, 0, 40, player.getLocation(), 30);
        int halloweenColor = new Random().nextInt(2);
        switch (halloweenColor) {
            case 0:
                inventory.setItem(slot, new ItemGenerator(Material.STAINED_GLASS_PANE).setDurability((short) 1).build());
                break;
            case 1:
                inventory.setItem(slot, new ItemGenerator(Material.STAINED_GLASS_PANE).setDurability((short) 10).build());
                break;
        }
    }

    private void updateCrateItem(Player player, Inventory inventory, boolean halloween, int tier) {
        if (countMap.get(player) < 14) {
            ItemStack itemStack = tier == 0 ? voteCrateItem(player) : randomCrateItem(halloween, tier, player);
            inventory.setItem(4, itemStack);
            playSound(player, halloween);
        }
        if (countMap.get(player) == 14) {
            playFinalSound(player, inventory, halloween);
        }
        if (countMap.get(player) == 16) {
            finalizeCrateOpening(player, inventory, halloween);
        }
        countMap.put(player, countMap.get(player) + 1);
    }

    private void playSound(Player player, boolean halloween) {
        if (halloween) {
            player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 10.0f, 10.0f);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 10.0f, 10.0f);
        }
    }

    private void playFinalSound(Player player, Inventory inventory, boolean halloween) {
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

    private void finalizeCrateOpening(Player player, Inventory inventory, boolean halloween) {
        openingCrateList.remove(player);
        countMap.remove(player);
        hallowList.remove(player);
        player.closeInventory();
        Bukkit.getScheduler().cancelTask(timerMap.get(player));
        timerMap.remove(player);
        if (inventory.getItem(4).getType() != Material.BARRIER) {
            player.getInventory().addItem(inventory.getItem(4));
            if (halloween) {
                PracticeServer.getManagerHandler().getHalloween().trickOrTreat(player, true);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (openingCrateList.containsKey(player)) {
                handleCrateClosing(player, event);
            }
        }
    }

    private void handleCrateClosing(Player player, InventoryCloseEvent event) {
        if (event.getInventory().getTitle().equalsIgnoreCase("Crate Opening") && event.getInventory().getItem(4).getType() != Material.BARRIER) {
            handleCrateItem(player, event);
        }
        hallowList.remove(player);
        Bukkit.getScheduler().cancelTask(timerMap.get(player));
        timerMap.remove(player);
        countMap.remove(player);
        event.getInventory().clear();
        openingCrateList.remove(player);
    }

    private void handleCrateItem(Player player, InventoryCloseEvent event) {
        if (countMap.get(player) >= 14) {
            player.getInventory().addItem(event.getInventory().getItem(4));
        } else {
            ItemStack itemStack = getFinalCrateItem(player, event);
            player.getInventory().addItem(itemStack);
        }
    }

    private ItemStack getFinalCrateItem(Player player, InventoryCloseEvent event) {
        ItemStack itemStack;
        if (event.getInventory().getTitle().toLowerCase().contains("vote")) {
            itemStack = voteCrateItem(player);
        } else if (hallowList.contains(player)) {
            itemStack = randomCrateItem(true, openingCrateList.get(player), player);
            PracticeServer.getManagerHandler().getHalloween().trickOrTreat(player, true);
        } else {
            itemStack = randomCrateItem(false, openingCrateList.get(player), player);
        }
        if (itemStack == null) {
            itemStack = Items.orb(false);
            itemStack.setAmount(13);
        }
        return itemStack;
    }


    // Generate a random item for a vote crate
    public ItemStack voteCrateItem(Player p) {
        int buffAmount = ThreadLocalRandom.current().nextInt(15, 35);
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

    // Generate a random item for a crate
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
