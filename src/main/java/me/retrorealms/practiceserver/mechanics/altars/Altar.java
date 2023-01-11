package me.retrorealms.practiceserver.mechanics.altars;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
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

class AltarInstance {
    public Player p;
    public List<ItemStack> items;
    public Location location;
    public List<Item> droppedItems;

    AltarInstance(List<ItemStack> items, Location location, Player p){
        this.items = items;
        this.location = location;
        this.p = p;
        droppedItems = new ArrayList<>();
    }

    public List<ItemStack> getItems() { return items; }
    public Location getLocation() {return location; }

    public void generateItem(){
        Random r = new Random();
        List<Integer> itemIDs = new ArrayList<>();
        for(ItemStack is : items){
            itemIDs.add(Altar.itemID(is));
        }
        List<String> itemlore = items.get(0).getItemMeta().getLore();

        int itemID = itemIDs.get(r.nextInt(itemIDs.size()));
        int tier = Altar.getTier(items.get(0));
        int rarity =  Altar.RarityToInt(itemlore.get(itemlore.size() - 1));

        ItemStack createdItem = CreateDrop.createDrop(tier, itemID, Math.min(rarity+1, 3));


        String reason = " has created a(n) ";
        final JSONMessage normal = new JSONMessage(p.getDisplayName() + ChatColor.RESET + reason, ChatColor.WHITE);
        List<String> hoveredChat = new ArrayList<>();
        ItemMeta meta = createdItem.getItemMeta();
        hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : createdItem.getType().name()));
        if (meta.hasLore()) hoveredChat.addAll(meta.getLore());
        normal.addHoverText(hoveredChat, ChatColor.getLastColors(createdItem.getItemMeta().getDisplayName()) + ChatColor.BOLD.toString() +  ChatColor.UNDERLINE + "SHOW");
        normal.addText(" using the Altar!");
        for (Entity near : location.getWorld().getNearbyEntities(location, 50, 50, 50)) {
            if (near instanceof Player) {
                Player nearPlayers = (Player) near;
                normal.sendToPlayer(nearPlayers);
            }
        }
        p.getInventory().addItem(createdItem);

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
        final Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
        final FireworkMeta fwm = fw.getFireworkMeta();
        final FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
    }

    public void cancelAltar(){
        if (Altar.altarInstances.get(p).items.size() > 0) {
            for (ItemStack item1 : Altar.altarInstances.get(p).items) {
                p.getInventory().addItem(item1);
                p.sendMessage(ChatColor.RED + "- " + item1.getItemMeta().getDisplayName());
            }
            for(Item i: droppedItems) i.remove();
            Altar.altarInstances.remove(p);
            p.sendMessage(ChatColor.RED + ">> Altar has been cancelled!");
        }
    }

    Location randomOffset(Location loc, int radius){
        Random r = new Random();
        double xoffset = r.nextInt(radius * 200)/100 - radius;
        double zoffset = r.nextInt(radius * 200)/100 - radius;
        while(Math.sqrt(Math.pow(xoffset, 2) + Math.pow(zoffset, 2)) > radius){ //make sure its an actual radius not a fucking box
            xoffset = (r.nextInt(radius * 200)/100) - radius;
            zoffset = (r.nextInt(radius * 200)/100) - radius;
        }
        return loc.add(xoffset, 0, zoffset);
    }

    public void addItem(Player p, ItemStack hand, int max){
        Altar.altarInstances.get(p).items.add(hand);
        p.getInventory().setItemInMainHand(null);
        p.sendMessage(ChatColor.GRAY + "+ " + hand.getItemMeta().getDisplayName() + ChatColor.GRAY + " (" + Altar.altarInstances.get(p).items.size() + "/" + max + ")");

        ItemStack is = new ItemStack(hand.getType(), 1);
        final ItemMeta meta = is.getItemMeta();
        meta.setLore(Arrays.asList("notarealitem"));
        is.setItemMeta(meta);
        Item i = location.getWorld().dropItem(randomOffset(location, 2), is);
        droppedItems.add(i);
        GlowAPI.setGlowing(i, Listeners.groupOf(hand));
    }
}

public class Altar implements Listener {

    public static Map<Player,AltarInstance> altarInstances = new HashMap<>();

    public void onEnable() {
        PracticeServer.log.info("[Altars] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        altarInstances = new HashMap<>();
        new BukkitRunnable() {
            @Override
            public void run(){
                for(Player p : altarInstances.keySet()){
                    if(p.getLocation().distance(altarInstances.get(p).location) > 10){
                        altarInstances.get(p).cancelAltar();
                    }
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20, 50);
    }

    public static int getItemAmoutByRarirty(String rare){
        if(rare.contains("Common")){
            return 4;
        }else if(rare.contains("Uncommon")){
            return 3;
        }else if(rare.contains("Rare")){
            return 2;
        }
        return 2;
    }

    public static int getTier(ItemStack is){
        String name = is.getItemMeta().getDisplayName();
        int tier = 0;
        if(name.contains(ChatColor.WHITE.toString())){
            tier = 1;
        } else if(name.contains(ChatColor.GREEN.toString())){
            tier = 2;
        } else if(name.contains(ChatColor.AQUA.toString())){
            tier = 3;
        } else if(name.contains(ChatColor.LIGHT_PURPLE.toString())){
            tier = 4;
        } else if(name.contains(ChatColor.YELLOW.toString())){
            tier = 5;
        } else if(name.contains(ChatColor.BLUE.toString())){
            tier = 6;
        }
        return tier;
    }

    public static int RarityToInt(String rare){
        int rar = 0;
        if(rare.contains("Common")){
            rar = 0;
        }else if(rare.contains("Uncommon")){
            rar = 1;
        }else if(rare.contains("Rare")){
            rar = 2;
        }else if(rare.contains("Unique")){
            rar =  3;
        }
        return rar;
    }

    public static int itemID(ItemStack is){
        String material = is.getType().name();
        if(material.contains("HOE")){
            return 0;
        }else if(material.contains("SPADE")){
            return 1;
        }else if(material.contains("SWORD")){
            return 2;
        }else if(material.contains("AXE")){
            return 3;
        }else if(material.contains("HELMET")){
            return 4;
        }else if(material.contains("CHESTPLATE")){
            return 5;
        }else if(material.contains("LEGGINGS")){
            return 6;
        }else if(material.contains("BOOTS")){
            return 7;
        }
        return 8;
    }

    public static boolean isArmour(ItemStack itemStack){
        String item = itemStack.getType().name();
        if(item.contains("HELMET")
                || item.contains("CHESTPLATE")
                || item.contains("LEGGINGS")
                || item.contains("BOOTS")) return true;
        return false;
    }

    public static boolean isWeapon(ItemStack itemStack) {
        String item = itemStack.getType().name();
        if (item.contains("HOE")
                || item.contains("SWORD")
                || (item.contains("AXE") && !item.contains("PICK"))
                || item.contains("SPADE")) return true;
        return false;
    }

    @EventHandler
    public void refundLogout(PlayerQuitEvent e){
        if(altarInstances.containsKey(e.getPlayer())){
            altarInstances.get(e.getPlayer()).cancelAltar();
        }
    }

    @EventHandler
    public void dropOnDeath(PlayerDeathEvent e){
        Player p = e.getEntity();
        if(altarInstances.containsKey(p)){
            if (Altar.altarInstances.get(p).items.size() > 0) {
                for (ItemStack item1 : Altar.altarInstances.get(p).items) {
                    p.getWorld().dropItem(p.getLocation(), item1);
                    p.sendMessage(ChatColor.RED + "- " + item1.getItemMeta().getDisplayName());
                }
                for(Item i: altarInstances.get(p).droppedItems) i.remove();
                Altar.altarInstances.remove(p);
                p.sendMessage(ChatColor.RED + ">> Altar has been cancelled!");
            }
        }
    }

    @EventHandler
    public void altarInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action a = e.getAction();
        Block b = e.getClickedBlock();
            if ((a == Action.RIGHT_CLICK_BLOCK) && (b.getType() == Material.ENCHANTMENT_TABLE)) {
                e.setCancelled(true);
                addArmor(p, b.getLocation());
            }
            if (a == Action.LEFT_CLICK_BLOCK && b.getType() == Material.ENCHANTMENT_TABLE && altarInstances.containsKey(p)) {
                e.setCancelled(true);
                altarInstances.get(p).cancelAltar();
            }
    }

    public void addArmor(Player p, Location loc) {
        if (p.getInventory().getItemInMainHand().hasItemMeta()) {
            ItemStack hand = p.getInventory().getItemInMainHand();
            List<String> lore = hand.getItemMeta().getLore();
            String rare = ChatColor.stripColor(lore.get(lore.size() - 1));
            int max = getItemAmoutByRarirty(rare);
            Random r = new Random();

            int rarity = Altar.RarityToInt(lore.get(lore.size() - 1));
            if (rarity > 3) {
                p.sendMessage(ChatColor.YELLOW + ">> This item is too rare to be upgraded.");
                return;
            }

            if (!altarInstances.containsKey(p)
                    || altarInstances.get(p).items.size() <= (max - 1)) {
                if (!altarInstances.containsKey(p) || altarInstances.get(p).items.size() < 1) {
                    p.sendMessage(ChatColor.YELLOW + "Punch Altar to cancel at any time.");
                    altarInstances.put(p, new AltarInstance(new ArrayList<>(), loc, p));
                    altarInstances.get(p).addItem(p, hand, max);
                } else {
                    ItemStack item1 = altarInstances.get(p).items.get(0);
                    if ((isWeapon(item1)) && (isWeapon(hand)) || (isArmour(item1)) && (isArmour(hand))) {
                        if (getTier(hand) == getTier(item1)) {
                            if (lore.get(lore.size() - 1).contains(item1.getItemMeta().getLore().get(item1.getItemMeta().getLore().size() - 1))) {
                                altarInstances.get(p).addItem(p, hand, max);
                                PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
                                int rollChance = r.nextInt(100) - (pp.luck*2);
                                if (altarInstances.get(p).items.size() == max) {
                                    p.sendMessage(ChatColor.GREEN + ">> Performing Altar");
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.25f);
                                            Particles.CRIT_MAGIC.display(0.0f, 0.0f, 0.0f, 0.5f, 20, altarInstances.get(p).location.add(0.0, 1.0, 0.0), 20.0);
                                        }
                                    }.runTaskLater(PracticeServer.plugin, 10L);
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.25f);
                                            Particles.CRIT_MAGIC.display(0.0f, 0.0f, 0.0f, 0.5f, 20, altarInstances.get(p).location.add(0.0, 1.0, 0.0), 20.0);
                                        }
                                    }.runTaskLater(PracticeServer.plugin, 20L);
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.25f);
                                            Particles.CRIT_MAGIC.display(0.0f, 0.0f, 0.0f, 0.5f, 20, altarInstances.get(p).location.add(0.0, 1.0, 0.0), 20.0);
                                        }
                                    }.runTaskLater(PracticeServer.plugin, 30L);
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            if (rollChance < 50) {
                                                altarInstances.get(p).generateItem();
                                            } else {
                                                p.sendMessage(ChatColor.RED + "You failed your Altar!");
                                            }
                                            for(Item i: altarInstances.get(p).droppedItems) i.remove();
                                            altarInstances.remove(p);
                                        }
                                    }.runTaskLater(PracticeServer.plugin, 40L);
                                }
                            } else {
                                p.sendMessage(ChatColor.YELLOW + ">> Must be the same rarity.");
                            }
                        } else {
                            p.sendMessage(ChatColor.YELLOW + ">> The items must be the same tier.");
                        }
                    } else {
                        p.sendMessage(ChatColor.YELLOW + ">> Item is neither armor, nor weapon");
                    }
                }
            }
        }

    }
}