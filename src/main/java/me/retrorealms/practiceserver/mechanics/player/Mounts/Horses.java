package me.retrorealms.practiceserver.mechanics.player.Mounts;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.Buddies;
import me.retrorealms.practiceserver.mechanics.player.GamePlayer.nonStaticConfig;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.vendors.ItemVendors;
import me.retrorealms.practiceserver.utils.Particles;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.PacketPlayOutMount;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.util.*;

public class Horses
        implements Listener {
    public static HashMap<String, Integer> mounting = new HashMap<String, Integer>();
    static HashMap<String, Integer> horsetier = new HashMap<String, Integer>();
    static HashMap<String, Location> mountingloc = new HashMap<String, Location>();
    static HashMap<UUID, Location> currentLoc = new HashMap<UUID, Location>();
    static HashMap<String, ItemStack> buyingitem = new HashMap<String, ItemStack>();
    static HashMap<String, Integer> buyingprice = new HashMap<String, Integer>();
    public static HashMap<Player, Integer> horseTier = new HashMap<>();

    public void onEnable() {
        PracticeServer.log.info("[Horses] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        new BukkitRunnable() {

            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (!p.isOnline() || !Horses.mounting.containsKey(p.getName())) continue;
                    Particles.SPELL.display(0.0f, 0.0f, 0.0f, 0.5f, 80, p.getLocation().add(0.0, 0.15, 0.0), 20.0);
                    if (Horses.mounting.get(p.getName()) == 0) {
                        Particles.CRIT.display(0.0f, 0.0f, 0.0f, 0.5f, 80, p.getLocation().add(0.0, 1.0, 0.0), 20.0);
                        Horses.mounting.remove(p.getName());
                        Horses.mountingloc.remove(p.getName());
                        Horses.horse(p, Horses.horsetier.get(p.getName()));
                        continue;
                    }
                    if (Horses.mounting.get(p.getName()) == 6) {
                        String name = Horses.mount(Horses.horsetier.get(p.getName()), false).getItemMeta().getDisplayName();
                        p.sendMessage(ChatColor.BOLD + "SUMMONING " + name + ChatColor.WHITE + " ... " + Horses.mounting.get(p.getName()) + "s");
                        Horses.mounting.put(p.getName(), Horses.mounting.get(p.getName()) - 1);
                        continue;
                    }
                    p.sendMessage(ChatColor.BOLD + "SUMMONING" + ChatColor.WHITE + " ... " + Horses.mounting.get(p.getName()) + "s");
                    Horses.mounting.put(p.getName(), Horses.mounting.get(p.getName()) - 1);
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
            if (currentLoc.containsKey(horse.getUniqueId())) {
                currentLoc.remove(horse.getUniqueId());
            }
            currentLoc.put(horse.getUniqueId(), horse.getLocation());
        }
    }

    public static ItemStack mount(int tier, boolean inshop) {
        ItemStack is = new ItemStack(Material.SADDLE);
        ItemMeta im = is.getItemMeta();
        String name = ChatColor.GREEN + "Old Horse Mount";
        String req = "";
        ArrayList<String> lore = new ArrayList<String>();
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
        }
        if (tier == 4) {
            name = ChatColor.LIGHT_PURPLE + "Knight's Horse Mount";
            req = ChatColor.AQUA + "Traveler's Horse Mount";
            line = "A fast well-bred horse.";
            speed = 140;
            jump = 110;
            price = 2500;
        }
        if (tier == 5) {
            name = ChatColor.YELLOW + "War Stallion Mount";
            req = ChatColor.LIGHT_PURPLE + "Knight's Horse Mount";
            line = "A trusty powerful steed.";
            speed = 150;
            jump = 120;
            price = 4000;
        }
        im.setDisplayName(name);
        lore.add(ChatColor.RED + "Speed: " + speed + "%");
        if (jump > 0) {
            lore.add(ChatColor.RED + "Jump: " + jump + "%");
        }
        if (req != "" && inshop) {
            lore.add(ChatColor.RED.toString() + ChatColor.BOLD + "REQ: " + req);
        }
        lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + line);
        lore.add(ChatColor.GRAY + "Permanent Untradeable");
        if (inshop) {
            lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + price + "g");
        }
        im.setLore(lore);
        is.setItemMeta(im);
        if(tier < 2) return new ItemStack(Material.AIR);
        return is;
    }

    public static int getMountTier(ItemStack is) {
        if (is != null && is.getType() == Material.SADDLE && is.getItemMeta().hasDisplayName()) {
            String name = is.getItemMeta().getDisplayName();
            if (name.contains(ChatColor.GREEN.toString())) {
                return 2;
            }
            if (name.contains(ChatColor.AQUA.toString())) {
                return 3;
            }
            if (name.contains(ChatColor.LIGHT_PURPLE.toString())) {
                return 4;
            }
            if (name.contains(ChatColor.YELLOW.toString())) {
                return 5;
            }
        }
        return 0;
    }

    public static Horse horse(Player p, int tier) {
        double speed = 0.30;
        double jump = 0.75;
        if (tier == 3) {
            speed = 0.42;
            jump = 0.85;
        }
        if (tier == 4) {
            speed = 0.48;
            jump = 0.95;
        }
        if (tier == 5) {
            speed = 0.55;
            jump = 1.05;
        }
        Horse h = (Horse) p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);
        new CreatureSpawnEvent(h, CreatureSpawnEvent.SpawnReason.CUSTOM);
        h.setVariant(Horse.Variant.HORSE);
        h.setAdult();
        h.setTamed(true);
        h.setOwner(p);
        h.setColor(Horse.Color.BLACK);
        h.setAgeLock(true);
        h.setStyle(Horse.Style.NONE);
        h.setDomestication(100);
        h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        h.getInventory().setArmor(new ItemStack(Material.IRON_BARDING));
        if (tier == 4) {
            h.getInventory().setArmor(new ItemStack(Material.DIAMOND_BARDING));
        }
        if (tier == 5) {
            h.getInventory().setArmor(new ItemStack(Material.GOLD_BARDING));
        }
        if (ModerationMechanics.isDonator(p)) {
            h.getInventory().setArmor(null);

            if (ModerationMechanics.getRank(p) == RankEnum.SUB) {
                h.setVariant(Horse.Variant.UNDEAD_HORSE);
            }
            if (ModerationMechanics.getRank(p) == RankEnum.SUB1) {
                h.setColor(Horse.Color.GRAY);
                h.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
            }
            if (ModerationMechanics.getRank(p) == RankEnum.SUB2) {
                h.setVariant(Horse.Variant.SKELETON_HORSE);
            }
            if (ModerationMechanics.getRank(p) == RankEnum.SUPPORTER || ModerationMechanics.getRank(p) == RankEnum.SUB3) {
                h.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
                h.setVariant(Horse.Variant.SKELETON_HORSE);
            }
        }
        h.setMaxHealth(20.0);
        h.setHealth(20.0);
        h.setJumpStrength(jump);
        ((CraftLivingEntity) h).getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        h.setPassenger(p);

        return h;
    }

    @EventHandler
    public void onAnimalTamerClick(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Player && e.getRightClicked().hasMetadata("NPC")) {
            Player at = (Player) e.getRightClicked();
            Player p = e.getPlayer();
            if (at.getName().equalsIgnoreCase("animal tamer")) {
                Inventory inv = Bukkit.createInventory(null, 9, "Animal Tamer");
                inv.addItem(Horses.mount(2, true));
                inv.addItem(Horses.mount(3, true));
                inv.addItem(Horses.mount(4, true));
                inv.addItem(Horses.mount(5, true));
                p.openInventory(inv);
                p.playSound(p.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onBuyHorse(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (p.getOpenInventory().getTopInventory().getTitle().contains("Animal Tamer")) {
            List<?> lore;
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.SADDLE && e.getCurrentItem().getItemMeta().hasLore() && ((String) (lore = e.getCurrentItem().getItemMeta().getLore()).get(lore.size() - 1)).contains("Price:")) {
                int price = ItemVendors.getPriceFromLore(e.getCurrentItem());
                if (Money.hasEnoughGems(p, price)) {
                    int currtier = 0;
                    if(Horses.horseTier.containsKey(p)) {
                        currtier = Horses.horseTier.get(p);
                    }
                    int newtier = Horses.getMountTier(e.getCurrentItem());
                    if (currtier == 0) {
                        currtier = 1;
                    }
                    if (newtier == currtier + 1) {
                        p.sendMessage(ChatColor.GRAY + "The '" + e.getCurrentItem().getItemMeta().getDisplayName() + ChatColor.GRAY + "' costs " + ChatColor.GREEN + ChatColor.BOLD + price + " GEM(s)" + ChatColor.GRAY + ".");
                        p.sendMessage(ChatColor.GRAY + "This item is non-refundable. type " + ChatColor.GREEN + ChatColor.BOLD + "Y" + ChatColor.GRAY + " to confirm.");
                        buyingitem.put(p.getName(), Horses.mount(newtier, false));
                        buyingprice.put(p.getName(), price);
                        p.closeInventory();
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "You do not have enough gems to purchase this mount.");
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
                    p.closeInventory();
                }
            }
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getOpenInventory().getTopInventory().getTitle().contains("Horse")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoinMount(PlayerRespawnEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), new Runnable() {
            @Override
            public void run() {
                Player p = event.getPlayer();
                if(PracticeServer.DATABASE){
                    try{
                        ResultSet rs = SQLMain.getPlayerData("PlayerData", "HorseTier", p);
                        int tier;
                        if(rs.next()){
                            tier = rs.getInt("HorseTier");
                        }else{
                            tier = 0;
                        }
                        if (!event.getPlayer().getInventory().contains(Material.SADDLE)) {
                            event.getPlayer().getInventory().addItem(mount(tier, false));
                        }
                    }catch (Exception e){
                    }
                }else {
                    if (nonStaticConfig.get().getInt(event.getPlayer().getUniqueId() + ".Info.Horse Tier") >= 1) {
                        if (!event.getPlayer().getInventory().contains(Material.SADDLE)) {
                            int tier = nonStaticConfig.get().getInt(event.getPlayer().getUniqueId() + ".Info.Horse Tier");
                            event.getPlayer().getInventory().addItem(mount(tier, false));
                        }
                    }
                }
            }
        }, 50);
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent e) {
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL || e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION
                    || e.getCause() == EntityDamageEvent.DamageCause.CONTACT || e.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK ||
                    e.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL || e.getCause() == EntityDamageEvent.DamageCause.CUSTOM || e.getCause() == EntityDamageEvent.DamageCause.DROWNING
                    || e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
                if (p.isInsideVehicle() && p.getVehicle().getType() == EntityType.HORSE) {
                    e.setDamage(0.0);
                    e.setCancelled(true);
                }
            } else if (p.isInsideVehicle() && p.getVehicle().getType() == EntityType.HORSE) {
                p.getVehicle().remove();
                p.teleport(p.getVehicle().getLocation().add(0.0, 1.0, 0.0));
            }
        }
        if (e.getEntity() instanceof Horse) {
            Horse h = (Horse) e.getEntity();
            if (e.getCause() != EntityDamageEvent.DamageCause.FALL && e.getCause() != EntityDamageEvent.DamageCause.SUFFOCATION) {
                EntityDamageByEntityEvent evt;
                Entity p = h.getPassenger();
                if (e instanceof EntityDamageByEntityEvent && (evt = (EntityDamageByEntityEvent) e).getDamager() instanceof Player && p instanceof Player) {
                    Player d = (Player) evt.getDamager();
                    ArrayList<String> toggles = Toggles.getToggles(d.getUniqueId());
                    ArrayList<String> buddies = Buddies.getBuddies(d.getName());
                    if (buddies.contains(p.getName().toLowerCase()) && !toggles.contains("Friendly Fire")) {
                        e.setDamage(0.0);
                        e.setCancelled(true);
                        return;
                    }
                    if (toggles.contains("Anti PVP")) {
                        e.setDamage(0.0);
                        e.setCancelled(true);
                        return;
                    }
                    if (!Alignments.neutral.containsKey(p.getName()) && !Alignments.chaotic.containsKey(p.getName()) && toggles.contains("Chaotic")) {
                        e.setDamage(0.0);
                        e.setCancelled(true);
                        return;
                    }
                }
                h.remove();
                if (p != null) {
                    p.teleport(h.getLocation().add(0.0, 2.0, 0.0));
                }
            }
            e.setDamage(0.0);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamager(EntityDamageByEntityEvent e) {
        Player p;
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (e.getDamager() instanceof Player && (p = (Player) e.getDamager()).getVehicle() != null && p.getVehicle().getType() == EntityType.HORSE) {
            Location location;
            if (currentLoc.containsKey(p.getVehicle().getUniqueId())) {
                location = currentLoc.get(p.getVehicle().getUniqueId());
            } else {
                location = p.getVehicle().getLocation();
            }
            currentLoc.remove(p.getVehicle().getUniqueId());
            p.teleport(location.add(0.0, 1.0, 0.0));
            if(p.getVehicle() != null) p.getVehicle().remove();
        }
    }

    @EventHandler
    public void onDismount(VehicleExitEvent e) {
        if (e.getExited() instanceof Player && e.getVehicle() instanceof Horse) {
            Player p = (Player) e.getExited();
            Location location;
            if (currentLoc.containsKey(e.getVehicle().getUniqueId())) {
                location = currentLoc.get(e.getVehicle().getUniqueId());
            } else {
                location = e.getVehicle().getLocation();
            }
            currentLoc.remove(e.getVehicle().getUniqueId());
            e.getVehicle().remove();
            e.getExited().teleport(location);

        }
    }

    @EventHandler
    public void onMountedPlayerChunkChange(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (p.getVehicle() == null) return;

        if (!event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            Bukkit.getScheduler().runTaskAsynchronously(PracticeServer.getInstance(), () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PacketPlayOutMount packetPlayOutMount = new PacketPlayOutMount(((CraftEntity) p).getHandle());
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutMount);
                }
            });
        }
    }

    @EventHandler
    public void onMountSummon(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK || p.getInventory().getItemInMainHand() == null || Horses.getMountTier(p.getInventory().getItemInMainHand()) <= 0 || p.getVehicle() != null || mounting.containsKey(p.getName()) || Duels.duelers.containsKey(p))) {
            int mountTime;
            if (Alignments.isSafeZone(p.getLocation())) {
                mountTime = 1;
            } else {
                mountTime = 6;
            }
            mounting.put(p.getName(), mountTime);
            mountingloc.put(p.getName(), p.getLocation());
            horsetier.put(p.getName(), Horses.getMountTier(p.getInventory().getItemInMainHand()));
        }
    }

    @EventHandler
    public void onCancelDamager(EntityDamageByEntityEvent e) {
        Player p;
        if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && mounting.containsKey((p = (Player) e.getDamager()).getName())) {
            mounting.remove(p.getName());
            mountingloc.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Mount Summon - " + ChatColor.BOLD + "CANCELLED");
        }
    }

    @EventHandler
    public void onCancelDamage(EntityDamageEvent e) {
        Player p;
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (e.getEntity() instanceof Player && mounting.containsKey((p = (Player) e.getEntity()).getName())) {
            mounting.remove(p.getName());
            mountingloc.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Mount Summon - " + ChatColor.BOLD + "CANCELLED");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (mounting.containsKey(p.getName())) {
            mounting.remove(p.getName());
            mountingloc.remove(p.getName());
        }
        if (p.getVehicle() != null && p.getVehicle().getType() == EntityType.HORSE) {
            p.teleport(p.getVehicle().getLocation().add(0.0, 1.0, 0.0));
            p.getVehicle().remove();
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (mounting.containsKey(p.getName())) {
            mounting.remove(p.getName());
            mountingloc.remove(p.getName());
        }
        Entity vehicle = p.getVehicle();
        if (vehicle != null && vehicle instanceof Horse) {
            if (currentLoc.containsKey(vehicle.getUniqueId())) {
                currentLoc.remove(vehicle.getUniqueId());
            }
        }
        p.eject();
    }

    @EventHandler
    public void onCancelMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (mounting.containsKey(p.getName()) && (mountingloc.get(p.getName())).distanceSquared(e.getTo()) >= 2.0) {
            mounting.remove(p.getName());
            p.sendMessage(ChatColor.RED + "Mount Summon - " + ChatColor.BOLD + "CANCELLED");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPromptChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (buyingitem.containsKey(p.getName()) && buyingprice.containsKey(p.getName())) {
            e.setCancelled(true);
            int price = buyingprice.get(p.getName());
            ItemStack is = buyingitem.get(p.getName());
            if (e.getMessage().equalsIgnoreCase("y")) {
                if (!Money.hasEnoughGems(p, price)) {
                    p.sendMessage(ChatColor.RED + "You do not have enough gems to purchase this mount.");
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
                    buyingprice.remove(p.getName());
                    buyingitem.remove(p.getName());
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
                if(!PracticeServer.DATABASE) {
                    nonStaticConfig.get().set(p.getUniqueId() + ".Info.Horse Tier", getMountTier(is));
                    nonStaticConfig.save();
                }
                Horses.horseTier.put(p, getMountTier(is));
                p.sendMessage(ChatColor.RED + "-" + price + ChatColor.BOLD + "G");
                p.sendMessage(ChatColor.GREEN + "Transaction successful.");
                p.sendMessage(ChatColor.GRAY + "You are now the proud owner of a mount -- " + ChatColor.UNDERLINE + "to summon your new mount, simply right click with the saddle in your player's hand.");
                buyingprice.remove(p.getName());
                buyingitem.remove(p.getName());
            } else {
                p.sendMessage(ChatColor.RED + "Purchase - " + ChatColor.BOLD + "CANCELLED");
                buyingprice.remove(p.getName());
                buyingitem.remove(p.getName());
                return;
            }
        }
    }
}

