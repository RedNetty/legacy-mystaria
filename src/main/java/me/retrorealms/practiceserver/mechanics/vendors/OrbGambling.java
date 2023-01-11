package me.retrorealms.practiceserver.mechanics.vendors;

import com.google.common.collect.Lists;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.mechanics.useless.task.AsyncTask;
import me.retrorealms.practiceserver.mechanics.useless.task.TaskProperty;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Created by Giovanni on 6-7-2017.
 */
public class OrbGambling implements Listener {

    private static ConcurrentHashMap<UUID, TaskProperty<Integer, Integer, ?>> inPlayOrb = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<UUID, TaskProperty<Integer, Integer, Integer>> inLastPlayOrb = new ConcurrentHashMap<>();

    public static CopyOnWriteArrayList<UUID> chatHandling = Lists.newCopyOnWriteArrayList();

    private int victimAmount = 0;
    private int victims = 0;

    public void initPool() {

        new AsyncTask(() -> {
            inLastPlayOrb.keySet().forEach(uuid -> {

                TaskProperty<Integer, Integer, Integer> property = inLastPlayOrb.get(uuid);
                inLastPlayOrb.remove(uuid);

                int time = property.getPropertyDefault();
                int newTime = time - 1;

                if (newTime <= 0) {

                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null || !player.isOnline())
                        return;

                    int playerRoll = ThreadLocalRandom.current().nextInt(0, 100);
                    int gamblerRoll = property.getPropertyIndex();

                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou've rolled &n" + playerRoll + "&e out of &n100"));

                    int gambled = property.getPropertyOption();

                    // copied lol
                    Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
                        if(playerRoll >= gamblerRoll) {
                            final Firework fw4 = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
                            final FireworkMeta fwm4 = fw4.getFireworkMeta();
                            final FireworkEffect effect4 = FireworkEffect.builder().flicker(false)
                                    .withColor(Color.PURPLE)
                                    .withFade(Color.YELLOW)
                                    .with(FireworkEffect.Type.BURST).trail(true).build();
                            fwm4.addEffect(effect4);
                            fwm4.setPower(0);
                            fw4.setFireworkMeta(fwm4);
                        } else {
                            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2.0f, 1.25f);
                            Particles.LAVA.display(0.0f, 0.0f, 0.0f, 5.0f, 10, player.getEyeLocation(), 20.0);

                        }
                    });

                    if (playerRoll >= gamblerRoll) {

                        int newAmount = gambled * 2;

                        IntStream.range(0, newAmount).forEach(consumer -> player.getInventory().addItem(Items.orb(false)));

                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50, 0.2F);

                        player.sendMessage("");
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou won! Reward: &n" + newAmount + "&6&l ORBS&a!"));
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 50, 0.2F);

                        player.sendMessage("");
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou lost! Loss: &n" + gambled + "&6&l ORBS&a!"));

                        victimAmount += gambled;
                        victimAmount++;
                    }
                    return;
                }

                Player player = Bukkit.getPlayer(uuid);

                if (player == null || !player.isOnline())
                    return;

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 50, ThreadLocalRandom.current().nextFloat());

                TaskProperty<Integer, Integer, Integer> newProperty = new TaskProperty<>(newTime, property.getPropertyIndex(), property.getPropertyOption());
                inLastPlayOrb.put(uuid, newProperty);
            });


            inPlayOrb.keySet().forEach(uuid -> {

                TaskProperty<Integer, Integer, ?> property = inPlayOrb.get(uuid);

                int time = property.getPropertyDefault();
                int newTime = time - 1;

                inPlayOrb.remove(uuid);

                if (newTime <= 0) {

                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null || !player.isOnline())
                        return;

                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 50, 1);

                    int gamblerRoll = ThreadLocalRandom.current().nextInt(0, 100);

                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eGambler has rolled &n" + gamblerRoll + "&e out of &n100"));

                    inLastPlayOrb.put(uuid, new TaskProperty<>(5, gamblerRoll, property.getPropertyIndex()));
                    return;
                }

                Player player = Bukkit.getPlayer(uuid);

                if (player == null || !player.isOnline())
                    return;

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 50, ThreadLocalRandom.current().nextFloat());

                if (newTime == 2) {

                    if (victims > 0 && victimAmount > 0) {

                        player.sendMessage("");
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cOver &4" + new DecimalFormat("#,###").format(victims) + "&c players have lost a combined &4" + new DecimalFormat("#,###").format(victimAmount) + " orbs this wipe!"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cShould've warned you earlier!"));
                    }
                }

                inPlayOrb.put(uuid, new TaskProperty<>(newTime, property.getPropertyIndex(), null));

            });
        }).setUseSharedPool(true).setInterval(1).scheduleRepeatingTask();
    }

    private void removeOrbs(Player p, int amt) {
        int i = 0;
        while (i < p.getInventory().getSize()) {
            ItemStack is = p.getInventory().getItem(i);
            if (amt > 0) {
                if (is != null && is.getType() == Material.MAGMA_CREAM && !is.getItemMeta().hasEnchants()) {
                    if (amt >= is.getAmount()) {
                        amt -= is.getAmount();
                        p.getInventory().setItem(i, null);
                    } else {
                        is.setAmount(is.getAmount() - amt);
                        amt = 0;
                    }
                }
            }
            ++i;
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (inPlayOrb.containsKey(uuid) || inLastPlayOrb.containsKey(uuid)) event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (inPlayOrb.containsKey(uuid) || inLastPlayOrb.containsKey(uuid))
            player.teleport(event.getFrom());
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
    	if (DeployCommand.patchlockdown) {
        	event.setCancelled(true);
        	return;
        }
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        if (entity.getName().equalsIgnoreCase("Orb Gambler")) {

            if (inPlayOrb.containsKey(player.getUniqueId()) || inLastPlayOrb.containsKey(player.getUniqueId()) || chatHandling.contains(player.getUniqueId()))
                return;

            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lTYPE &athe amount of ORBS you'd like to gamble with"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType &n'cancel'&c to cancel this action."));
            player.sendMessage("");

            chatHandling.add(player.getUniqueId());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!chatHandling.contains(player.getUniqueId())) return;

        event.setCancelled(true);

        String message = event.getMessage();

        if (message.toLowerCase().contains("cancel")) {
            chatHandling.remove(player.getUniqueId());

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAction &nCANCELLED"));
            return;
        }

        int toGamble = 0;

        try {
            if(Integer.valueOf(message) <= 16) {
                toGamble = Integer.valueOf(message);
            }else{
                chatHandling.remove(player.getUniqueId());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cMaximum orbs to bet is 16!"));
                return;
            }
        } catch (Exception e) {
            chatHandling.remove(player.getUniqueId());

            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid amount - INPUT: &l" + event.getMessage()));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAction &nCANCELLED"));

            return;
        }

        HashMap<Integer, ? extends ItemStack> orbMap = player.getInventory().all(Material.MAGMA_CREAM);
        int size = 0;

        for (ItemStack itemStack : orbMap.values())
            size += itemStack.getAmount();

        if (toGamble > size) {

            chatHandling.remove(player.getUniqueId());

            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have enough orbs! Owned: " + size + " - Input: " + toGamble));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAction &nCANCELLED"));

            return;
        }

        chatHandling.remove(player.getUniqueId());

        removeOrbs(player, toGamble);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eGambler: Gambling with &n" + toGamble + "&e orbs.."));

        inPlayOrb.put(player.getUniqueId(), new TaskProperty<>(5, toGamble, null));
    }
}
