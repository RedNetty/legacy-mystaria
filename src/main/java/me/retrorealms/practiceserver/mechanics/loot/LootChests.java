package me.retrorealms.practiceserver.mechanics.loot;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class LootChests implements Listener {

    public static HashMap<Location, Integer> loot = new HashMap<Location, Integer>();
    public static HashMap<Location, Integer> respawn = new HashMap<Location, Integer>();
    public static HashMap<Location, Inventory> opened = new HashMap<Location, Inventory>();
    public static HashMap<Player, Location> viewers = new HashMap<Player, Location>();

    public static void clearChestInventories() {
        opened.clear();
    }

    public static int getPlayerTier(Player e) {
        ItemStack is = e.getInventory().getItemInMainHand();
        if (is != null && is.getType() != Material.AIR) {
            if (is.getType().name().contains("WOOD_")) {
                return 1;
            }
            if (is.getType().name().contains("STONE_")) {
                return 2;
            }
            if (is.getType().name().contains("IRON_")) {
                return 3;
            }
            if (is.getType().name().contains("DIAMOND_")) {
                return 4;
            }
            if (is.getType().name().contains("GOLD_")) {
                return 5;
            }
            if (is.getType().name().contains("ICE")) {
                return 6;
            }
        }
        return 0;
    }

    public void onEnable() {
        PracticeServer.log.info("[LootChests] has been enabled.");

        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);

        spawnParticles();
        handleRespawn();
        loadConfiguration();
    }

    private void spawnParticles() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Location location : loot.keySet()) {
                    Location cloneLocation = location.clone().add(0.5, 1.5, 0.5);
                    cloneLocation.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, cloneLocation, 1, .1, .1, .1, -0.0005);
                }
            }
        };
        runnable.runTaskTimer(PracticeServer.plugin, 0, 5);
    }

    private void handleRespawn() {
        new BukkitRunnable() {
            public void run() {
                for (Location loc : LootChests.loot.keySet()) {
                    if (LootChests.respawn.containsKey(loc)) {
                        if (LootChests.respawn.get(loc) >= 1) {
                            LootChests.respawn.put(loc, LootChests.respawn.get(loc) - 1);
                            continue;
                        }
                        LootChests.respawn.remove(loc);
                        continue;
                    }
                    if (!loc.getWorld().getChunkAt(loc).isLoaded() || loc.getWorld().getBlockAt(loc).getType().equals(Material.GLOWSTONE))
                        continue;
                    loc.getWorld().getBlockAt(loc).setType(Material.CHEST);
                    Location cloneLocation = loc.clone().add(0.5, 1.5, 0.5);
                    Particles.ParticleColor particleColor = getParticleColor(loot.get(loc));
                    Particles.REDSTONE.display(particleColor, cloneLocation, 15);
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20, 20);
    }

    private Particles.ParticleColor getParticleColor(int loot) {
        switch (loot) {
            case 1:
                return new Particles.OrdinaryColor(Color.WHITE);
            case 2:
                return new Particles.OrdinaryColor(Color.GREEN);
            case 3:
                return new Particles.OrdinaryColor(Color.AQUA);
            case 4:
                return new Particles.OrdinaryColor(Color.PURPLE);
            case 5:
            default:
                return new Particles.OrdinaryColor(Color.YELLOW);
        }
    }

    private void loadConfiguration() {
        File file = new File(PracticeServer.plugin.getDataFolder(), "loot.yml");

        YamlConfiguration config = new YamlConfiguration();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            config.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String key : config.getKeys(false)) {
            int val = config.getInt(key);
            String[] str = key.split(",");
            World world = Bukkit.getWorld(str[0]);
            double x = Double.valueOf(str[1]);
            double y = Double.valueOf(str[2]);
            double z = Double.valueOf(str[3]);
            Location loc = new Location(world, x, y, z);
            loot.put(loc, val);
        }
    }


    public void onDisable() {
        File file = new File(PracticeServer.plugin.getDataFolder(), "loot.yml");
        if (file.exists()) {
            file.delete();
        }
        YamlConfiguration config = new YamlConfiguration();
        if (!loot.isEmpty()) {
            saveAll(file, config);
        }
        PracticeServer.log.info("[LootChests] has been disabled.");
    }

    private void saveAll(File file, YamlConfiguration config) {
        for (Location loc1 : loot.keySet()) {
            String s = loc1.getWorld().getName() + "," + (int) loc1.getX() + "," + (int) loc1.getY() + "," + (int) loc1.getZ();
            config.set(s, loot.get(loc1));
            try {
                config.save(file);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public boolean isMobNear(Location location) {
        for (Entity ent : location.getWorld().getNearbyEntities(location, 6.0, 6.0, 6.0)) {
            if (!(ent instanceof LivingEntity) || ent instanceof Player || ent instanceof Horse || ent.hasMetadata("pet"))
                continue;
            return true;
        }
        return false;
    }

    @EventHandler
    public void onChestClick(PlayerInteractEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;

        Player p = e.getPlayer();
        if (!e.hasBlock()) return;

        if (e.getClickedBlock().getType() == Material.CHEST) {
            handleChestInteraction(e, p);
        } else if (e.getClickedBlock().getType() == Material.GLOWSTONE && p.isOp()) {
            handleGlowstoneInteraction(e, p);
        }
    }

    private void handleChestInteraction(PlayerInteractEvent e, Player p) {
        Location loc = e.getClickedBlock().getLocation();
        if (!loot.containsKey(loc)) {
            if (!p.isOp()) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.GRAY + "The chest is locked.");
            }
            return;
        }

        e.setCancelled(true);
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleChestRightClick(e, p, loc);
        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleChestLeftClick(e, p, loc);
        }
    }

    private void handleChestRightClick(PlayerInteractEvent e, Player p, Location loc) {
        if (isMobNear(loc)) {
            sendWarningMessage(p);
            return;
        }

        if (!opened.containsKey(loc)) {
            openNewChest(e, p, loc);
        } else {
            openExistingChest(e, p, loc);
        }
    }

    private void openNewChest(PlayerInteractEvent e, Player p, Location loc) {
        Inventory inv = Bukkit.createInventory(null, 27, "Loot Chest");
        inv.addItem(LootDrops.createLootDrop(loot.get(loc)));
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        viewers.put(p, loc);
        opened.put(loc, inv);
        incrementPlayerChestOpenCount(p);
    }

    private void openExistingChest(PlayerInteractEvent e, Player p, Location loc) {
        p.openInventory(opened.get(loc));
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        viewers.put(p, loc);
    }

    private void incrementPlayerChestOpenCount(Player p) {
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(p.getUniqueId());
        guildPlayer.setLootChestsOpen(guildPlayer.getLootChestsOpen() + 1);
    }

    private void handleChestLeftClick(PlayerInteractEvent e, Player p, Location loc) {
        if (isMobNear(loc)) {
            sendWarningMessage(p);
            return;
        }

        if (opened.containsKey(loc)) {
            handleOpenedChest(e, p, loc);
        } else {
            handleUnopenedChest(e, p, loc);
        }
    }

    private void sendWarningMessage(Player p) {
        p.sendMessage(ChatColor.RED + "It is " + ChatColor.BOLD + "NOT" + ChatColor.RED + " safe to open that right now.");
        p.sendMessage(ChatColor.GRAY + "Eliminate the monsters in the area first.");
    }

    private void handleOpenedChest(PlayerInteractEvent e, Player p, Location loc) {
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(p.getUniqueId());
        guildPlayer.setLootChestsOpen(guildPlayer.getLootChestsOpen() + 1);
        loc.getWorld().getBlockAt(loc).setType(Material.AIR);
        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 0.5f, 1.2f);
        dropItems(loc, opened.get(loc).getContents());
        opened.remove(loc);
        int tier = loot.get(loc);
        respawn.put(loc, 60 * tier);
        closeViewers(loc);
    }

    private void handleUnopenedChest(PlayerInteractEvent e, Player p, Location loc) {
        loc.getWorld().getBlockAt(loc).setType(Material.AIR);
        loc.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.WOOD);
        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 0.5f, 1.2f);
        loc.getWorld().dropItemNaturally(loc, LootDrops.createLootDrop(loot.get(loc)));
        int tier = loot.get(loc);
        respawn.put(loc, 60 * tier);
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(p.getUniqueId());
        guildPlayer.setLootChestsOpen(guildPlayer.getLootChestsOpen() + 1);
        closeViewers(loc);
    }

    private void dropItems(Location loc, ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null) {
                loc.getWorld().dropItemNaturally(loc, item);
            }
        }
    }

    private void closeViewers(Location loc) {
        for (Player v : viewers.keySet()) {
            if (!viewers.get(v).equals(loc)) continue;
            viewers.remove(v);
            v.closeInventory();
            v.playSound(v.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 0.5f, 1.2f);
            v.playSound(v.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
        }
    }

    private void handleGlowstoneInteraction(PlayerInteractEvent e, Player p) {
        Location loc = e.getClickedBlock().getLocation();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && LootChests.getPlayerTier(p) > 0) {
            e.setCancelled(true);
            createLootChest(p, loc);
            saveLootConfiguration();
        }
    }

    private void createLootChest(Player p, Location loc) {
        loot.put(loc, LootChests.getPlayerTier(p));
        p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "     *** LOOT CHEST CREATED ***");
        loc.getWorld().getBlockAt(loc).setType(Material.CHEST);
        loc.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.CHEST);
    }

    private void saveLootConfiguration() {
        File file = new File(PracticeServer.plugin.getDataFolder(), "loot.yml");
        if (file.exists()) {
            file.delete();
        }
        YamlConfiguration config = new YamlConfiguration();
        saveAll(file, config);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Location loc;
        Player p = e.getPlayer();
        if (p.isOp() && e.getBlock().getType().equals(Material.GLOWSTONE) && loot.containsKey((loc = e.getBlock().getLocation()))) {
            loot.remove(loc);
            p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "     *** LOOT CHEST REMOVED ***");
            loc.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.CHEST);
        }
    }

    @EventHandler
    public void onCloseChest(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player) {
            Player p = (Player) e.getPlayer();
            if (e.getInventory().getName().contains("Loot Chest") && viewers.containsKey(p)) {
                Location loc = viewers.get(p);
                viewers.remove(p);
                p.playSound(p.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
                boolean isempty = true;
                ItemStack[] arritemStack = e.getInventory().getContents();
                int n = arritemStack.length;
                int n2 = 0;
                while (n2 < n) {
                    ItemStack itms = arritemStack[n2];
                    if (itms != null && itms.getType() != Material.AIR) {
                        isempty = false;
                    }
                    ++n2;
                }
                if (isempty) {
                    loc.getWorld().getBlockAt(loc).setType(Material.AIR);
                    loc.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.WOOD);
                    p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 0.5f, 1.2f);
                    opened.remove(loc);
                    int tier = loot.get(loc);
                    respawn.put(loc, 60 * tier);
                    closeViewers(loc);
                }
            }
        }
    }

}

