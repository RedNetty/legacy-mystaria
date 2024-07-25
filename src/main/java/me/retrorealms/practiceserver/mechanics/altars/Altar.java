package me.retrorealms.practiceserver.mechanics.altars;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.utils.GlowAPI;
import me.retrorealms.practiceserver.utils.JSONMessage;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Altar implements Listener {

    public static Map<Player, AltarInstance> altarInstances = new ConcurrentHashMap<>();
    public static Map<Player, Long> alterTimeout = new ConcurrentHashMap<>();

    public void onEnable() {
        PracticeServer.log.info("[Altars] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        startAltarCheckTask();
    }

    private void startAltarCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Player, AltarInstance> entry : altarInstances.entrySet()) {
                    Player player = entry.getKey();
                    AltarInstance instance = entry.getValue();
                    if (player.getLocation().distance(instance.location) > 10) {
                        instance.cancelAltar();
                    }
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20, 50);
    }

    @EventHandler
    public void refundLogout(PlayerQuitEvent e) {
        if (altarInstances.containsKey(e.getPlayer())) {
            altarInstances.get(e.getPlayer()).cancelAltar();
        }
    }

    @EventHandler
    public void dropOnDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (altarInstances.containsKey(p)) {
            altarInstances.get(p).cancelAltar();
        }
    }

    @EventHandler
    public void altarInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action a = e.getAction();
        Block b = e.getClickedBlock();
        if (b != null && a == Action.RIGHT_CLICK_BLOCK && b.getType() == Material.ENCHANTMENT_TABLE) {
            e.setCancelled(true);
            Location altarLocation = b.getLocation();
            PracticeServer.log.info("Altar interaction at: " + altarLocation.toString());
            addArmor(p, altarLocation);
        } else if (b != null && a == Action.LEFT_CLICK_BLOCK && b.getType() == Material.ENCHANTMENT_TABLE && altarInstances.containsKey(p)) {
            e.setCancelled(true);
            altarInstances.get(p).cancelAltar();
        }
    }

    public void addArmor(Player p, Location loc) {
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (!hand.hasItemMeta()) return;

        List<String> lore = hand.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return;

        String rare = ChatColor.stripColor(lore.get(lore.size() - 1));
        int max = getItemAmoutByRarirty(rare);
        int rarity = RarityToInt(lore.get(lore.size() - 1));

        if (rarity > 3) {
            p.sendMessage(ChatColor.YELLOW + ">> This item is too rare to be upgraded.");
            return;
        }

        if (alterTimeout.containsKey(p) && System.currentTimeMillis() < alterTimeout.get(p)) {
            p.sendMessage(ChatColor.YELLOW + ">> Please wait before using the Altar again.");
            return;
        }

        AltarInstance altarInstance = altarInstances.get(p);
        if (altarInstance == null || altarInstance.items.size() < max) {
            alterTimeout.remove(p);
            if (altarInstance == null || altarInstance.items.isEmpty()) {
                p.sendMessage(ChatColor.YELLOW + "Punch Altar to cancel at any time.");
                altarInstance = new AltarInstance(new ArrayList<>(), loc, p);
                altarInstances.put(p, altarInstance);
            }
            processItemAddition(p, hand, altarInstance, max);
        }
    }

    private void processItemAddition(Player p, ItemStack hand, AltarInstance altarInstance, int max) {
        if (altarInstance.items.isEmpty()) {
            altarInstance.addItem(p, hand, max);
        } else {
            ItemStack firstItem = altarInstance.items.get(0);
            if (areItemsCompatible(firstItem, hand)) {
                altarInstance.addItem(p, hand, max);
                if (altarInstance.items.size() == max) {
                    performAltar(p, altarInstance);
                }
            } else {
                p.sendMessage(ChatColor.YELLOW + ">> The items must be the same tier and rarity.");
            }
        }
    }

    private boolean areItemsCompatible(ItemStack item1, ItemStack item2) {
        return (isWeapon(item1) && isWeapon(item2) || isArmour(item1) && isArmour(item2)) &&
                getTier(item1) == getTier(item2) &&
                getRarity(item1).equals(getRarity(item2));
    }

    private void performAltar(Player p, AltarInstance altarInstance) {
        p.sendMessage(ChatColor.GREEN + ">> Performing Altar");

        final long animationDuration = 5000; // 5 seconds in milliseconds
        new EnhancedAltarAnimation(altarInstance.location, 1, 20, animationDuration).start();

        PracticeServer.log.info("Starting altar process at: " + altarInstance.location.toString());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (altarInstances.containsKey(p)) {
                    PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
                    int rollChance = new Random().nextInt(100) - (pp.luck * 2);
                    if (rollChance < 50) {
                        altarInstance.generateItem(altarInstance.location);
                    } else {
                        altarInstance.location.getWorld().playSound(altarInstance.location, Sound.BLOCK_FIRE_EXTINGUISH, 2.0f, 1.25f);
                        Particles.LAVA.display(0.0f, 0.0f, 0.0f, 5.0f, 10, altarInstance.location, 20.0);
                        p.sendMessage(ChatColor.RED + "You failed your Altar!");
                    }
                    for (Item i : altarInstance.droppedItems) i.remove();
                    altarInstances.remove(p);

                    PracticeServer.log.info("Completed altar process at: " + altarInstance.location.toString());
                }
            }
        }.runTaskLater(PracticeServer.plugin, animationDuration / 50); // Convert milliseconds to ticks
    }

    public static int getItemAmoutByRarirty(String rare) {
        if (rare.contains("Common")) return 4;
        if (rare.contains("Uncommon")) return 3;
        if (rare.contains("Rare")) return 2;
        return 2;
    }

    public static int getTier(ItemStack is) {
        String name = is.getItemMeta().getDisplayName();
        if (name.contains(ChatColor.WHITE.toString())) return 1;
        if (name.contains(ChatColor.GREEN.toString())) return 2;
        if (name.contains(ChatColor.AQUA.toString())) return 3;
        if (name.contains(ChatColor.LIGHT_PURPLE.toString())) return 4;
        if (name.contains(ChatColor.YELLOW.toString())) return 5;
        if (name.contains(ChatColor.BLUE.toString())) return 6;
        return 0;
    }

    public static int RarityToInt(String rare) {
        if (rare.contains("Common")) return 1;
        if (rare.contains("Uncommon")) return 2;
        if (rare.contains("Rare")) return 3;
        if (rare.contains("Unique")) return 4;
        return 1;
    }

    public static int itemID(ItemStack is) {
        String material = is.getType().name();
        if (material.contains("HOE")) return 1;
        if (material.contains("SPADE")) return 2;
        if (material.contains("SWORD")) return 3;
        if (material.contains("AXE")) return 4;
        if (material.contains("HELMET")) return 5;
        if (material.contains("CHESTPLATE")) return 6;
        if (material.contains("LEGGINGS")) return 7;
        if (material.contains("BOOTS")) return 8;
        return 8;
    }

    public static boolean isArmour(ItemStack itemStack) {
        String item = itemStack.getType().name();
        return item.contains("HELMET") || item.contains("CHESTPLATE") || item.contains("LEGGINGS") || item.contains("BOOTS");
    }

    public static boolean isWeapon(ItemStack itemStack) {
        String item = itemStack.getType().name();
        return item.contains("HOE") || item.contains("SWORD") || (item.contains("AXE") && !item.contains("PICK")) || item.contains("SPADE");
    }

    private static String getRarity(ItemStack item) {
        List<String> lore = item.getItemMeta().getLore();
        return lore.get(lore.size() - 1);
    }

    private static class EnhancedAltarAnimation {
        private final Location center;
        private final double radius;
        private final int particleCount;
        private final long duration;
        private int taskId;

        public EnhancedAltarAnimation(Location center, double radius, int particleCount, long duration) {
            this.center = center.clone().add(0.5, 1, 0.5);
            this.radius = radius;
            this.particleCount = particleCount;
            this.duration = duration;
        }

        public void start() {
            final long startTime = System.currentTimeMillis();
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.plugin, new Runnable() {
                @Override
                public void run() {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    if (elapsedTime >= duration) {
                        Bukkit.getScheduler().cancelTask(taskId);
                        return;
                    }

                    double progress = (double) elapsedTime / duration;
                    double angle = progress * Math.PI * 2 * 5; // 5 full rotations
                    double height = progress * 3; // Max height of 3 blocks

                    for (int i = 0; i < particleCount; i++) {
                        double x = center.getX() + radius * Math.cos(angle + (2 * Math.PI * i / particleCount));
                        double z = center.getZ() + radius * Math.sin(angle + (2 * Math.PI * i / particleCount));
                        Location particleLocation = new Location(center.getWorld(), x, center.getY() + height, z);

                        center.getWorld().spawnParticle(Particle.SPELL_WITCH, particleLocation, 1, 0, 0, 0, 0);
                        center.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, particleLocation, 1, 0, 0, 0, 0);
                    }
                }
            }, 0L, 1L);
        }
    }
}