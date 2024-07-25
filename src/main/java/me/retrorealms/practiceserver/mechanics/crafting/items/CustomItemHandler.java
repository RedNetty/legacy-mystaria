package me.retrorealms.practiceserver.mechanics.crafting.items;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.crafting.items.celestialbeacon.CelestialBeacon;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CustomItemHandler implements Listener {

    private final PracticeServer plugin;
    private final Map<String, CustomItem> registeredItems;

    public CustomItemHandler(PracticeServer plugin) {
        this.plugin = plugin;
        this.registeredItems = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void registerItem(CustomItem item) {
        registeredItems.put(item.getId(), item);
    }

    public ItemStack createCustomItem(String itemId) {
        try {
            CustomItem customItem = registeredItems.get(itemId);
            if (customItem == null) return null;

            ItemStack item = new ItemStack(customItem.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(customItem.getName());
            meta.setLore(customItem.getLore());
            item.setItemMeta(meta);

            net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
            compound.setString("CustomItemId", itemId);
            compound.setString("ActivationType", customItem.getActivationType().name());
            nmsItem.setTag(compound);

            return CraftItemStack.asBukkitCopy(nmsItem);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error creating custom item: " + itemId, e);
            return null;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        CustomItem customItem = getCustomItemFromItemStack(item);
        if (customItem == null) return;

        if (customItem.getActivationType() == CustomItem.ActivationType.RIGHT_CLICK) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                customItem.applyEffects(event.getPlayer());
            }
        }
    }

    public void startEffectApplication() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item == null) continue;
                        CustomItem customItem = getCustomItemFromItemStack(item);
                        if (customItem != null && customItem.getActivationType() == CustomItem.ActivationType.INVENTORY) {
                            customItem.applyEffects(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public CustomItem getCustomItemFromItemStack(ItemStack item) {
        if (item == null) return null;
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem.hasTag() && nmsItem.getTag().hasKey("CustomItemId")) {
            String itemId = nmsItem.getTag().getString("CustomItemId");
            return registeredItems.get(itemId);
        }
        return null;
    }

    public boolean isCustomItem(ItemStack item, String customItemId) {
        if (item == null) return false;
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem.hasTag() && nmsItem.getTag().hasKey("CustomItemId")) {
            String itemId = nmsItem.getTag().getString("CustomItemId");
            return itemId.equals(customItemId);
        }
        return false;
    }

    public String getCustomItemId(ItemStack item) {
        if (item == null) return null;
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem.hasTag() && nmsItem.getTag().hasKey("CustomItemId")) {
            return nmsItem.getTag().getString("CustomItemId");
        }
        return null;
    }

    public void registerRareItems() {
        registerItem(new CustomItem("essence_of_frost", ChatColor.AQUA + "Essence of Frost", Material.PRISMARINE_CRYSTALS,.02, CustomItem.ActivationType.NONE) {
            @Override
            public void applyEffects(Player player) {
                // No direct effects, used for crafting
            }
        });

        registerItem(new CustomItem("infernal_core", ChatColor.RED + "Infernal Core", Material.MAGMA_CREAM,.02, CustomItem.ActivationType.NONE) {
            @Override
            public void applyEffects(Player player) {
                // No direct effects, used for crafting
            }
        });

        registerItem(new CustomItem("windsong_feather", ChatColor.WHITE + "Windsong Feather", Material.FEATHER,.02, CustomItem.ActivationType.NONE) {
            @Override
            public void applyEffects(Player player) {
                // No direct effects, used for crafting
            }
        });

        registerItem(new CustomItem("elemental_harmony", ChatColor.GOLD + "Elemental Harmony", Material.NETHER_STAR,.02, CustomItem.ActivationType.INVENTORY) {
            @Override
            public void applyEffects(Player player) {
                // Effects are handled by ModifierManager
            }
        });

        registerItem(new CustomItem("starlight_essence", ChatColor.YELLOW + "Starlight Essence", Material.GLOWSTONE_DUST,.02, CustomItem.ActivationType.NONE) {
            @Override
            public void applyEffects(Player player) {
                // No direct effects, used for crafting
            }
        });

        registerItem(new CustomItem("angelic_feather", ChatColor.WHITE + "Angelic Feather", Material.FEATHER,.02, CustomItem.ActivationType.NONE) {
            @Override
            public void applyEffects(Player player) {
                // No direct effects, used for crafting
            }
        });

        registerItem(new CustomItem("divine_clay", ChatColor.AQUA + "Divine Clay", Material.CLAY_BALL,.02, CustomItem.ActivationType.NONE) {
            @Override
            public void applyEffects(Player player) {
                // No direct effects, used for crafting
            }
        });

        registerItem(new CelestialBeacon());
    }
}