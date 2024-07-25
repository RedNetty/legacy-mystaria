package me.retrorealms.practiceserver.mechanics.player.Mounts;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.tab.TabMenu;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.Buddies;
import me.retrorealms.practiceserver.mechanics.player.GamePlayer.nonStaticConfig;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.vendors.ItemVendors;
import me.retrorealms.practiceserver.utils.Particles;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.PacketPlayOutMount;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Horses implements Listener {
    public static final HashMap<String, Integer> mounting = new HashMap<>();
    public static final HashMap<Player, Integer> horseTier = new HashMap<>();
    public static final HashMap<String, Integer> horseTierByPlayer = new HashMap<>();
    public static final HashMap<String, Location> mountingLocations = new HashMap<>();
    public static final HashMap<UUID, Location> currentLoc = new HashMap<>();
    public static final HashMap<String, ItemStack> buyingItem = new HashMap<>();
    public static final HashMap<String, Integer> buyingPrice = new HashMap<>();

    public static ItemStack createMount(int tier, boolean inShop) {
        ItemStack itemStack = new ItemStack(Material.SADDLE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        String name = ChatColor.GREEN + "Old Horse Mount";
        String req = "";
        ArrayList<String> lore = new ArrayList<>();
        String line = "An old brown starter horse.";
        int speed = 115;
        int jump = 0;
        int price = 1000;
        if (tier == 3) {
            name = ChatColor.AQUA + "Traveler's Horse Mount";
            req = ChatColor.GREEN + "Old Horse Mount";
            line = "A standard healthy horse.";
            speed = 130;
            jump = 105;
            price = 1500;
        } else if (tier == 4) {
            name = ChatColor.LIGHT_PURPLE + "Knight's Horse Mount";
            req = ChatColor.AQUA + "Traveler's Horse Mount";
            line = "A fast well-bred horse.";
            speed = 140;
            jump = 110;
            price = 2500;
        } else if (tier == 5) {
            name = ChatColor.YELLOW + "War Stallion Mount";
            req = ChatColor.LIGHT_PURPLE + "Knight's Horse Mount";
            line = "A trusty powerful steed.";
            speed = 150;
            jump = 120;
            price = 4000;
        }
        itemMeta.setDisplayName(name);
        lore.add(ChatColor.RED + "Speed: " + speed + "%");
        if (jump > 0) {
            lore.add(ChatColor.RED + "Jump: " + jump + "%");
        }
        if (!req.isEmpty() && inShop) {
            lore.add(ChatColor.RED.toString() + ChatColor.BOLD + "REQ: " + req);
        }
        lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + line);
        lore.add(ChatColor.GRAY + "Permanent Untradeable");
        if (inShop) {
            lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + price + "g");
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        if (tier < 2) {
            return new ItemStack(Material.AIR);
        }
        return itemStack;
    }

    public static void clearHorses() {
        horseTierByPlayer.clear();
        horseTier.clear();
    }

    public static int getMountTier(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() == Material.SADDLE && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            String name = itemStack.getItemMeta().getDisplayName();
            if (name.contains(ChatColor.GREEN.toString())) {
                return 2;
            } else if (name.contains(ChatColor.AQUA.toString())) {
                return 3;
            } else if (name.contains(ChatColor.LIGHT_PURPLE.toString())) {
                return 4;
            } else if (name.contains(ChatColor.YELLOW.toString())) {
                return 5;
            }
        }
        return 0;
    }

    public static Horse createHorse(Player player, int tier) {
        double speed = 0.30;
        double jump = 0.75;
        if (tier == 3) {
            speed = 0.42;
            jump = 0.85;
        } else if (tier == 4) {
            speed = 0.48;
            jump = 0.95;
        } else if (tier == 5) {
            speed = 0.55;
            jump = 1.05;
        }
        EntityType horseType = EntityType.HORSE;
        Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), horseType);
        new CreatureSpawnEvent(horse, CreatureSpawnEvent.SpawnReason.CUSTOM);
        horse.setAdult();
        horse.setTamed(true);
        horse.setOwner(player);
        horse.setColor(Horse.Color.BLACK);
        horse.setAgeLock(true);
        horse.setStyle(Horse.Style.NONE);
        horse.setDomestication(100);
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        if (tier == 3) {
            horse.getInventory().setArmor(new ItemStack(Material.IRON_BARDING));
        } else if (tier == 4) {
            horse.getInventory().setArmor(new ItemStack(Material.DIAMOND_BARDING));
        } else if (tier == 5) {
            horse.getInventory().setArmor(new ItemStack(Material.GOLD_BARDING));
        }
        horse.setMaxHealth(20.0);
        horse.setHealth(20.0);
        horse.setJumpStrength(jump);
        ((CraftLivingEntity) horse).getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        horse.setPassenger(player);
        return horse;
    }

    public void onEnable() {
        PracticeServer.log.info("[Horses] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    try {
                        if (!player.isOnline() || !mounting.containsKey(player.getName())) {
                            continue;
                        }
                        Particles.SPELL.display(0.0f, 0.0f, 0.0f, 0.5f, 80, player.getLocation().clone().add(0.0, 0.15, 0.0), 20.0);
                        int mountTime = mounting.get(player.getName());
                        if (mountTime == 0) {
                            Particles.CRIT.display(0.0f, 0.0f, 0.0f, 0.5f, 80, player.getLocation().clone().add(0.0, 1.0, 0.0), 20.0);
                            mounting.remove(player.getName());
                            mountingLocations.remove(player.getName());
                            createHorse(player, horseTierByPlayer.get(player.getName()));
                            continue;
                        } else if (mountTime == 6) {
                            String name = createMount(horseTierByPlayer.get(player.getName()), false).getItemMeta().getDisplayName();
                            player.sendMessage(ChatColor.BOLD + "SUMMONING " + name + ChatColor.WHITE + " ... " + mountTime + "s");
                        } else {
                            player.sendMessage(ChatColor.BOLD + "SUMMONING" + ChatColor.WHITE + " ... " + mountTime + "s");
                        }
                        mounting.put(player.getName(), mountTime - 1);
                    } catch (Exception e) {
                        // Handle exception
                    }
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20, 20);
    }

    public void onDisable() {
        PracticeServer.log.info("[Horses] has been disabled.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Horse) {
            Horse horse = (Horse) event.getVehicle();
            currentLoc.put(horse.getUniqueId(), horse.getLocation());
        }
    }

    @EventHandler
    public void onAnimalTamerClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player && event.getRightClicked().hasMetadata("NPC")) {
            Player animalTamer = (Player) event.getRightClicked();
            Player player = event.getPlayer();
            if (animalTamer.getName().equalsIgnoreCase("animal tamer")) {
                Inventory inventory = Bukkit.createInventory(null, 9, "Animal Tamer");
                inventory.addItem(createMount(2, true));
                inventory.addItem(createMount(3, true));
                inventory.addItem(createMount(4, true));
                inventory.addItem(createMount(5, true));
                player.openInventory(inventory);
                player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onBuyHorse(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (inventory.getTitle().contains("Animal Tamer")) {
            event.setCancelled(true);
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem != null && currentItem.getType() == Material.SADDLE && currentItem.hasItemMeta() && currentItem.getItemMeta().hasLore()) {
                List<String> lore = currentItem.getItemMeta().getLore();
                if (lore.get(lore.size() - 1).contains("Price:")) {
                    int price = ItemVendors.getPriceFromLore(currentItem);
                    if (Money.hasEnoughGems(player, price)) {
                        int currentTier = horseTier.containsKey(player) ? horseTier.get(player) : 0;
                        int newTier = getMountTier(currentItem);
                        if (currentTier == 0) {
                            currentTier = 1;
                        }
                        if (newTier == currentTier + 1) {
                            player.sendMessage(ChatColor.GRAY + "The '" + currentItem.getItemMeta().getDisplayName() + ChatColor.GRAY + "' costs " + ChatColor.GREEN + ChatColor.BOLD + price + " GEM(s)" + ChatColor.GRAY + ".");
                            player.sendMessage(ChatColor.GRAY + "This item is non-refundable. type " + ChatColor.GREEN + ChatColor.BOLD + "Y" + ChatColor.GRAY + " to confirm.");
                            buyingItem.put(player.getName(), createMount(newTier, false));
                            buyingPrice.put(player.getName(), price);
                            player.closeInventory();
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have enough gems to purchase this mount.");
                        player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
                        player.closeInventory();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getOpenInventory().getTopInventory().getTitle().contains("Horse")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoinMount(PlayerRespawnEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
            Player player = event.getPlayer();
            if (PracticeServer.DATABASE) {
                try {
                    ResultSet resultSet = SQLMain.getPlayerSet("PlayerData", "HorseTier", player);
                    int tier = resultSet.next() ? resultSet.getInt("HorseTier") : 0;
                    if (!player.getInventory().contains(Material.SADDLE)) {
                        player.getInventory().addItem(createMount(tier, false));
                    }
                } catch (Exception e) {
                    // Handle exception
                }
            } else {
                if (nonStaticConfig.get().getInt(player.getUniqueId() + ".Info.Horse Tier") >= 1) {
                    if (!player.getInventory().contains(Material.SADDLE)) {
                        int tier = nonStaticConfig.get().getInt(player.getUniqueId() + ".Info.Horse Tier");
                        player.getInventory().addItem(createMount(tier, false));
                    }
                }
            }
        }, 50);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent event) {
        if (event.getDamage() <= 0.0) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION
                    || event.getCause() == EntityDamageEvent.DamageCause.CONTACT || event.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK ||
                    event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL || event.getCause() == EntityDamageEvent.DamageCause.CUSTOM || event.getCause() == EntityDamageEvent.DamageCause.DROWNING
                    || event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
                if (player.isInsideVehicle() && player.getVehicle().getType() == EntityType.HORSE) {
                    event.setDamage(0.0);
                    event.setCancelled(true);
                }
            } else if (player.isInsideVehicle() && player.getVehicle().getType() == EntityType.HORSE) {
                player.getVehicle().remove();
                player.teleport(player.getVehicle().getLocation().clone().add(0.0, 1.0, 0.0));
            }
        }
        if (event.getEntity() instanceof Horse) {
            Horse horse = (Horse) event.getEntity();
            if (event.getCause() != EntityDamageEvent.DamageCause.FALL && event.getCause() != EntityDamageEvent.DamageCause.SUFFOCATION) {
                EntityDamageByEntityEvent entityDamageByEntityEvent;
                Entity entity = horse.getPassenger();
                if (event instanceof EntityDamageByEntityEvent && (entityDamageByEntityEvent = (EntityDamageByEntityEvent) event).getDamager() instanceof Player && entity instanceof Player) {
                    Player damager = (Player) entityDamageByEntityEvent.getDamager();
                    ArrayList<String> buddies = Buddies.getBuddies(damager.getName());
                    if (buddies.contains(entity.getName().toLowerCase()) && !Toggles.isToggled(damager,"Friendly Fire")) {
                        event.setDamage(0.0);
                        event.setCancelled(true);
                        return;
                    }
                    if (Toggles.isToggled(damager, "Anti PVP")) {
                        event.setDamage(0.0);
                        event.setCancelled(true);
                        return;
                    }
                    if (!Alignments.neutral.containsKey(entity.getName()) && !Alignments.chaotic.containsKey(entity.getName()) && Toggles.isToggled(damager,"Chaotic")) {
                        event.setDamage(0.0);
                        event.setCancelled(true);
                        return;
                    }
                }
                horse.remove();
                if (entity != null) {
                    entity.teleport(horse.getLocation().clone().add(0.0, 2.0, 0.0));
                }
            }
            event.setDamage(0.0);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamager(EntityDamageByEntityEvent event) {
        Player player;
        if (event.getDamage() <= 0.0) {
            return;
        }
        if (event.getDamager() instanceof Player && (player = (Player) event.getDamager()).getVehicle() != null && player.getVehicle().getType() == EntityType.HORSE) {
            Location location;
            if (currentLoc.containsKey(player.getVehicle().getUniqueId())) {
                location = currentLoc.get(player.getVehicle().getUniqueId());
            } else {
                location = player.getVehicle().getLocation();
            }
            currentLoc.remove(player.getVehicle().getUniqueId());
            player.teleport(location.add(0.0, 1.0, 0.0));
            if (player.getVehicle() != null) {
                player.getVehicle().remove();
            }
        }
    }

    @EventHandler
    public void onDismount(VehicleExitEvent event) {
        if (event.getExited() instanceof Player && event.getVehicle() instanceof Horse) {
            Player player = (Player) event.getExited();
            Location location;
            if (currentLoc.containsKey(event.getVehicle().getUniqueId())) {
                location = currentLoc.get(event.getVehicle().getUniqueId());
            } else {
                location = event.getVehicle().getLocation();
            }
            currentLoc.remove(event.getVehicle().getUniqueId());
            event.getVehicle().remove();
        }
    }

    @EventHandler
    public void onMountedPlayerChunkChange(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getVehicle() == null) {
            return;
        }
        if (!event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            Bukkit.getScheduler().runTaskAsynchronously(PracticeServer.getInstance(), () -> {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    PacketPlayOutMount packetPlayOutMount = new PacketPlayOutMount(((CraftEntity) player).getHandle());
                    ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(packetPlayOutMount);
                }
            });
        }
    }

    @EventHandler
    public void onMountSummon(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK || player.getInventory().getItemInMainHand() == null || getMountTier(player.getInventory().getItemInMainHand()) <= 0 || player.getVehicle() != null || mounting.containsKey(player.getName()) || Duels.duelers.containsKey(player))) {
            int mountTime = 6;
            if (TabMenu.getAlignment(player).toLowerCase().contains("chaotic")) {
                mountTime = 8;
            }
            if (Alignments.isSafeZone(player.getLocation())) {
                mountingLocations.put(player.getName(), player.getLocation());
                horseTierByPlayer.put(player.getName(), getMountTier(player.getInventory().getItemInMainHand()));
                createHorse(player, getMountTier(player.getInventory().getItemInMainHand()));
            } else {
                mounting.put(player.getName(), mountTime);
                mountingLocations.put(player.getName(), player.getLocation());
                horseTierByPlayer.put(player.getName(), getMountTier(player.getInventory().getItemInMainHand()));
            }
        }
    }

    @EventHandler
    public void onCancelDamager(EntityDamageByEntityEvent event) {
        Player player;
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity && mounting.containsKey((player = (Player) event.getDamager()).getName())) {
            mounting.remove(player.getName());
            mountingLocations.remove(player.getName());
            player.sendMessage(ChatColor.RED + "Mount Summon - " + ChatColor.BOLD + "CANCELLED");
        }
    }

    @EventHandler
    public void onCancelDamage(EntityDamageEvent event) {
        Player player;
        if (event.getDamage() <= 0.0) {
            return;
        }
        if (event.getEntity() instanceof Player && mounting.containsKey((player = (Player) event.getEntity()).getName())) {
            mounting.remove(player.getName());
            mountingLocations.remove(player.getName());
            player.sendMessage(ChatColor.RED + "Mount Summon - " + ChatColor.BOLD + "CANCELLED");
        }
    }
    @EventHandler
    public void onCancelMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (mounting.containsKey(p.getName()) && (mountingLocations.get(p.getName())).distanceSquared(e.getTo()) >= 2.0) {
            mounting.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Mount Summon - " + ChatColor.BOLD + "CANCELLED");
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (mounting.containsKey(player.getName())) {
            mounting.remove(player.getName());
            mountingLocations.remove(player.getName());
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPromptChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (buyingItem.containsKey(p.getName()) && buyingPrice.containsKey(p.getName())) {
            e.setCancelled(true);
            int price = buyingPrice.get(p.getName());
            ItemStack is = buyingItem.get(p.getName());
            if (e.getMessage().equalsIgnoreCase("y")) {
                if (!Money.hasEnoughGems(p, price)) {
                    p.sendMessage(ChatColor.RED + "You do not have enough gems to purchase this mount.");
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
                    buyingPrice.remove(p.getName());
                    buyingItem.remove(p.getName());
                    return;
                }
                if (p.getInventory().contains(Material.SADDLE)) {
                    p.getInventory().remove(Material.SADDLE);
                }
                if (p.getInventory().firstEmpty() == -1) {
                    p.sendMessage(ChatColor.RED + "No space available in inventory. Type 'cancel' or clear some room.");
                    return;
                }
                Money.takeGems(p, price);
                p.getInventory().setItem(p.getInventory().firstEmpty(), is);
                if (!PracticeServer.DATABASE) {
                    nonStaticConfig.get().set(p.getUniqueId() + ".Info.Horse Tier", getMountTier(is));
                    nonStaticConfig.save();
                }
                Horses.horseTier.put(p, getMountTier(is));
                p.sendMessage(ChatColor.RED + "-" + price + ChatColor.BOLD + "G");
                p.sendMessage(ChatColor.GREEN + "Transaction successful.");
                p.sendMessage(ChatColor.GRAY + "You are now the proud owner of a mount -- " + ChatColor.UNDERLINE + "to summon your new mount, simply right click with the saddle in your player's hand.");
                buyingPrice.remove(p.getName());
                buyingItem.remove(p.getName());
            } else {
                p.sendMessage(ChatColor.RED + "Purchase - " + ChatColor.BOLD + "CANCELLED");
                buyingPrice.remove(p.getName());
                buyingItem.remove(p.getName());
            }
        }
    }
}