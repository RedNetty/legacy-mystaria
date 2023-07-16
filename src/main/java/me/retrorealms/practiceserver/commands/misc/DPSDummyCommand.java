package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.drops.Drops;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;

/**
 * Created by Calen on 8/2/2018.
 * Finished by Kaveen on 8/4/2018.
 */
public class DPSDummyCommand implements CommandExecutor, Listener {
    public static HashMap<String, Entity> activestands = new HashMap<String, Entity>();
    PracticeServer plugin;
    private String name;
    private Location armor;

    public DPSDummyCommand(PracticeServer plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        Player player = (Player) sender;
        name = player.getName();
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only usable by players.");
        }
        if (ModerationMechanics.isDonator(player) || ModerationMechanics.isStaff(player)) {

            if (activestands.containsKey(player.getName())) {
                activestands.get(player.getName()).remove();
                activestands.remove(player.getName());

                player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Your active DPS Dummy was removed!");
                return true;

            }

            // Check if player is in a safezone and in a viable place to place
            // the dummy here!

            // Check if player is on ground

            Location loc = player.getLocation().clone().add(new Vector(0, 1, 0));
            while (!loc.getBlock().getType().isSolid()) {
                loc.add(new Vector(0, -1, 0));
            }
            loc.add(new Vector(0, 1, 0));

            spawnStand(loc, player);

            player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "DPS Dummy Spawned! Thank you "
                    + player.getName() + ".");
        }
        return false;
    }

    private void spawnStand(Location location, Player player) {

        ItemStack itemSkull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) itemSkull.getItemMeta();
        meta.setOwner(name);

        itemSkull.setItemMeta(meta);

        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armor = stand.getLocation();
        stand.setCustomName(ChatColor.GREEN + ChatColor.BOLD.toString() + name + "'s DPS Dummy");

        stand.setCustomNameVisible(true);
        stand.setArms(true);
        stand.setBoots(Drops.createDrop(6, 7));
        stand.setChestplate(new ItemStack(Drops.createDrop(6, 5)));
        stand.setLeggings(new ItemStack(Drops.createDrop(6, 6)));
        stand.setHelmet(itemSkull);
        stand.setGravity(false);
        Spawners.hpCheck(stand);
        LivingEntity standentity = stand;

        standentity.setMaxHealth(100000);
        standentity.setHealth(100000);

        activestands.put(player.getName(), standentity);

    }

    @EventHandler
    private void onLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (activestands.containsKey(player.getName())) {
            activestands.get(player.getName()).remove();
            activestands.remove(player.getName());

        }

    }

    @EventHandler
    private void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType().equals(EntityType.ARMOR_STAND)) {
            if (event.getEntity().getCustomName() != null) {
                LivingEntity le = (LivingEntity) event.getEntity();

                le.setMaxHealth(100000);
                le.setHealth(100000);

            }

        }

    }

    @EventHandler
    private void onHit(ProjectileHitEvent event) {
        Bukkit.getWorld("jew").getNearbyEntities(event.getEntity().getLocation(), 2, 2, 2).forEach(entity -> {

            if (entity.getType().equals(EntityType.ARMOR_STAND) && entity.getCustomName() != null) {


                if (!Alignments.isSafeZone(entity.getLocation())) {
                    try {
                        ArmorStand stand = (ArmorStand) entity;
                        ItemStack itemskull = stand.getHelmet();
                        SkullMeta meta = (SkullMeta) itemskull.getItemMeta();
                        String playername = meta.getOwner();
                        activestands.remove(playername);

                        entity.remove();
                    } catch (Exception e) {

                    }

                }
            }
        });

    }

    @EventHandler
    private void onArmorChange(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked().getType().equals(EntityType.ARMOR_STAND)) {
            ArmorStand stand = (ArmorStand) event.getRightClicked();
            if (!stand.hasGravity()) {
                event.setCancelled(true);
            }

        }
    }
}
