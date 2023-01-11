package me.retrorealms.practiceserver.mechanics.pets.handlers;

import me.retrorealms.practiceserver.apis.nbt.NBTAccessor;
import me.retrorealms.practiceserver.manager.Manager;
import me.retrorealms.practiceserver.mechanics.pets.base.Pet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;

/**
 * Created by Khalid on 8/8/2017.
 */
public class PetManager extends Manager implements Listener {

    private HashMap<String, Pet> pets = new HashMap<>();

    @Override
    public void onEnable() {
        listener(this);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ())
            return;
        if (pets.containsKey(e.getPlayer().getUniqueId().toString())) {
            if (pets.get(e.getPlayer().getUniqueId().toString()).getEntity().isDead()) {
                pets.remove(e.getPlayer().getUniqueId().toString());
                e.getPlayer().sendMessage(ChatColor.RED + "Your pet has died.");
                return;
            }
            pets.get(e.getPlayer().getUniqueId().toString()).walk(e.getPlayer().getLocation());

        }
    }

    @EventHandler
    public void onDmg(EntityDamageByEntityEvent e) {
        if (e.getEntity().hasMetadata("pet")) {
            e.setCancelled(true);
            e.setDamage(0);
            e.getEntity().setInvulnerable(true);
            e.getEntity().setLastDamageCause(null);
            ((Creature)e.getEntity()).setTarget(null);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        if (e.getEntity().hasMetadata("pet")) {
            e.setCancelled(true);
            e.setTarget(null);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (pets.containsKey(e.getPlayer().getUniqueId().toString())) {
            pets.get(e.getPlayer().getUniqueId().toString()).getEntity().remove();
            pets.remove(e.getPlayer().toString());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory() == null)
            return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR || (e.getCurrentItem().getType() != Material.MONSTER_EGG && e.getCurrentItem().getType() != Material.STAINED_GLASS_PANE))
            return;
        if (!ChatColor.stripColor(e.getInventory().getTitle()).equals("Pets"))
            return;
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();
        e.setCancelled(true);

        if (e.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {
            if (pets.containsKey(p.getUniqueId().toString())) {
                pets.get(p.getUniqueId().toString()).getEntity().remove();
                pets.remove(p.getUniqueId().toString());
            }
            p.closeInventory();
            return;
        }

        NBTAccessor accessor = new NBTAccessor(e.getCurrentItem()).check();
        if (accessor.hasKey("pet")) {
            EntityType type = EntityType.valueOf(accessor.getString("pet"));
            boolean baby = false;
            if (type == EntityType.ZOMBIE || type == EntityType.PIG_ZOMBIE || (type == EntityType.SHEEP && e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Baby Sheep Pet")))
                baby = true;
            new Pet(p.getUniqueId().toString(), type, p, baby);
            p.closeInventory();

        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (this.pets.containsKey(e.getPlayer().getUniqueId().toString()))
            this.pets.get(e.getPlayer().getUniqueId().toString()).getEntity().teleport(e.getPlayer().getLocation());
    }

    public void registerPet(Pet pet) {
        if (pets.containsKey(pet.getOwner())) {
            pets.get(pet.getOwner()).getEntity().remove();
        }
        pets.put(pet.getOwner(), pet);
    }

    @Override
    public void onDisable() {

    }
}

