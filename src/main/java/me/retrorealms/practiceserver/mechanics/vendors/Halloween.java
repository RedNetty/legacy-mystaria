package me.retrorealms.practiceserver.mechanics.vendors;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.apis.itemapi.NBTAccessor;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.manager.Manager;
import me.retrorealms.practiceserver.mechanics.donations.Crates.CratesMain;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.utils.Particles;
import me.retrorealms.practiceserver.utils.item.ItemGenerator; // share this
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Jaxon on 10/5/2017.
 */
public class Halloween extends Manager implements Listener {

    @Override
    public void onEnable() {
        listener(this);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (player.getInventory().getHelmet() != null) {
                        if (player.getInventory().getHelmet().getType() == Material.JACK_O_LANTERN) {
                            Particles.FLAME.display(0.1f, 0.1f, 0.1f, 0.02f, 10, player.getLocation().add(0, 2, 0), 20.0);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(PracticeServer.plugin, 20L, 20L);
    }

    @Override
    public void onDisable() {

    }

    /*Pumpkin Masks*/

    public ItemStack halloweenHat() {
        ItemStack itemStack = new ItemGenerator(Material.JACK_O_LANTERN).setName(ChatColor.LIGHT_PURPLE + "Halloween Mask")
                .setLore(Arrays.asList(ChatColor.RED + "Attach onto a helmet to wear.", ChatColor.GRAY.toString() + ChatColor.ITALIC + "A spooky carved out pumpkin mask", ChatColor.GREEN + "Holiday Item"))
                .build();
        NBTAccessor nbtAccessor = new NBTAccessor(itemStack);
        int randomNumber = ThreadLocalRandom.current().nextInt(1000000000) + 1;
        nbtAccessor.setInt("key", randomNumber);
        return nbtAccessor.update();
    }

    public boolean isPumpkinMask(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() == Material.JACK_O_LANTERN && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.LIGHT_PURPLE + "Halloween Mask")) {
            return true;
        } else {
            return false;
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
        if (DeployCommand.patchlockdown) {
        	event.setCancelled(true);
        	return;
        }
        
        if (event.getCurrentItem() != null && event.getCurrentItem().getType().name().contains("_HELMET") && isPumpkinMask(event.getCursor())) {
            ItemStack pumpkinMask = event.getCursor();
            ItemStack helmet = event.getCurrentItem();
            event.setCurrentItem(new ItemStack(Material.AIR));
            pumpkinMask.setItemMeta(helmet.getItemMeta());
            NBTAccessor nbtAccessor = new NBTAccessor(pumpkinMask);
            nbtAccessor.setInt("tier", Items.getTierFromColor(helmet));
            event.setCurrentItem(nbtAccessor.update());
            event.setCursor(new ItemStack(Material.AIR));
            return;
        }
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.JACK_O_LANTERN) {
            NBTAccessor nbtAccessor = new NBTAccessor(event.getCurrentItem());
            if (!nbtAccessor.hasKey("tier")) return;
            if (event.getClick() == ClickType.RIGHT) {
                int helmetTier = nbtAccessor.getInteger("tier");
                ItemStack pumpkinMask = event.getCurrentItem();
                ItemStack itemStack = new ItemStack(Material.LEATHER_HELMET);
                switch (helmetTier) {
                    case 1:
                        itemStack = new ItemStack(Material.LEATHER_HELMET);
                        break;
                    case 2:
                        itemStack = new ItemStack(Material.CHAINMAIL_HELMET);
                        break;
                    case 3:
                        itemStack = new ItemStack(Material.IRON_HELMET);
                        break;
                    case 4:
                        itemStack = new ItemStack(Material.DIAMOND_HELMET);
                        break;
                    case 5:
                        itemStack = new ItemStack(Material.GOLD_HELMET);
                        break;
                }
                itemStack.setItemMeta(pumpkinMask.getItemMeta());
                event.setCurrentItem(halloweenHat());
                event.setCursor(itemStack);
            }
            if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) {
                if (event.getWhoClicked().getInventory().getHelmet() != null) {
                    p.sendMessage(ChatColor.RED + "You must remove your current helmet to put on your mask!");
                    return;
                } else {
                    event.getWhoClicked().getInventory().setHelmet(event.getCurrentItem());
                    event.setCurrentItem(new ItemStack(Material.AIR));
                    p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                }

            }

        }
    }
    /*Pumpkin Masks*/

    /*Trick or Treating*/
    public ItemStack halloweenCandy() {
        return new ItemGenerator(Material.SUGAR).setName(ChatColor.GREEN + "Halloween Candy")
                .setLore(Arrays.asList(ChatColor.RED + "Trade in to the Trick or Treat NPC for a", ChatColor.RED + "chance to win a cool prize! ... Or just eat it.", ChatColor.GRAY.toString() + ChatColor.ITALIC + "A small pouch of sugary treats.", ChatColor.GREEN + "Holiday Item"))
                .build();
    }

    public boolean hasEnoughCandy(Player player) {
        if (player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() == Material.SUGAR && player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() && player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "Halloween Candy") && player.getInventory().getItemInMainHand().getAmount() >= 8) {
            return true;
        } else {
            return false;
        }
    }

    public void trickOrTreat(Player player, boolean crate) {
        if (!crate && !hasEnoughCandy(player)) {
            player.sendMessage(ChatColor.RED + "Come back to me when you have at least 8 Candies!");
            return;
        }
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
        int randomChance = 0;
        randomChance = crate ? ThreadLocalRandom.current().nextInt(125) : ThreadLocalRandom.current().nextInt(200);
        if (randomChance >= 125) {
            int randomBuff = ThreadLocalRandom.current().nextInt(4);
            int potionTime = 200;
            PotionEffectType potionEffectType = PotionEffectType.BLINDNESS;
            switch (randomBuff) {
                case 0:
                    potionEffectType = PotionEffectType.CONFUSION;
                    break;
                case 1:
                    potionEffectType = PotionEffectType.INCREASE_DAMAGE;
                    potionTime = 1200;
                    break;
                case 2:
                    potionEffectType = PotionEffectType.SPEED;
                    potionTime = 1400;
                    break;
            }
            player.addPotionEffect(new PotionEffect(potionEffectType, potionTime, 1));
            player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "A SPOOKY SKELETON GAVE YOU A POTION EFFECT >:(");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 1, 1);
        } else if (randomChance >= 60) {
            int randomOrbAmount = ThreadLocalRandom.current().nextInt(25) + 1;
            ItemStack orbItem = Items.orb(false);
            orbItem.setAmount(randomOrbAmount);
            player.getInventory().addItem(orbItem);
            player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "A SPOOKY SKELETON GAVE YOU ORB(s)!");
        } else if (randomChance >= 50) {
            player.getInventory().addItem(Items.legendaryOrb(false));
            player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "A SPOOKY SKELETON GAVE YOU A LEGENDARY ORB!");
        } else if (randomChance >= 35) {
            int randomTier = crate ? ThreadLocalRandom.current().nextInt(3, 5) : ThreadLocalRandom.current().nextInt(5) + 1;
            player.getInventory().addItem(Items.enchant(randomTier, 0, false));
            player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "A SPOOKY SKELETON GAVE YOU A WEAPON ENCHANT");
        } else if (randomChance >= 8) {
            int randomTier = crate ? ThreadLocalRandom.current().nextInt(3, 5) : ThreadLocalRandom.current().nextInt(5) + 1;
            player.getInventory().addItem(Items.enchant(randomTier, 1, false));
            player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "A SPOOKY SKELETON GAVE YOU N ARMOR ENCHANT");
        } else if (randomChance >= 1) {
            int randomTier = crate ? ThreadLocalRandom.current().nextInt(2, 4) : ThreadLocalRandom.current().nextInt(5);
            player.getInventory().addItem(ItemAPI.getScrollGenerator().next(randomTier));
            player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "A SPOOKY SKELETON GAVE YOU A PROTECT SCROLL");
            if (randomTier >= 3) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1, 1);
            }
        } else if (randomChance < 1) {
            int randomTier = ThreadLocalRandom.current().nextInt(5);
            player.getInventory().addItem(CratesMain.createCrate(5, true));
            player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "A SPOOKY SKELETON GAVE YOU A LEGENDARY HALLOWEEN CRATE!");
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " received a Legendary Halloween Crate from Trick or Treating!");
            if (randomTier >= 3) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1, 1);
            }
        }

        if (!crate) {
            if (player.getInventory().getItemInMainHand().getAmount() > 8) {
                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 8);
            } else {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
        }
    }
    /*Trick or Treating*/

}

