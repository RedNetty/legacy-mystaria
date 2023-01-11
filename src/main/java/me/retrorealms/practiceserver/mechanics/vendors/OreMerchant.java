package me.retrorealms.practiceserver.mechanics.vendors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.mechanics.item.Items;

/**
 * Created by Giovanni on 12-5-2017.
 */
public class OreMerchant implements Listener {

	public static final ConcurrentHashMap<UUID, Integer> chatInteractive = new ConcurrentHashMap<>();

	@EventHandler
	public void onMerchantInteract(PlayerInteractEntityEvent event) {

		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		if (DeployCommand.patchlockdown) {
			event.setCancelled(true);
			return;
		}

		if (!entity.getName().equalsIgnoreCase("ore merchant"))
			return;

		if (event.getHand() == EquipmentSlot.OFF_HAND)
			return;

		event.setCancelled(true);

		if (chatInteractive.containsKey(player.getUniqueId())) {
			cancel(player);
			return;
		}

		int goldOre = 0;
		HashMap<Integer, ? extends ItemStack> oreMap = player.getInventory().all(Material.GOLD_ORE);

		for (ItemStack itemStack : oreMap.values())
			goldOre += itemStack.getAmount();

		int maxOrbs = goldOre / 10;

		int t6Ore = 0;
		HashMap<Integer, ? extends ItemStack> t6oreMap = player.getInventory().all(Material.LAPIS_ORE);

		for (ItemStack itemStack : t6oreMap.values())
			t6Ore += itemStack.getAmount();

		int maxOrbs2 = t6Ore / 5;

		if (goldOre < 10) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&7You do not have enough &nGOLD ORE&7 to refine into orbs!"));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Required gold ore per orb: &c&l10"));

		} else if (t6Ore < 5) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&7You do not have enough &nLAPIS ORE&7 to refine into orbs!"));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Required lapis ore per orb: &c&l5"));
		}

		chatInteractive.put(player.getUniqueId(), 5);

		player.sendMessage("");
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&a&lTYPE &athe amount of ORBS you'd like to refine your ore into."));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType &n'cancel'&c to cancel this action."));
		player.sendMessage("");

		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Your &nGOLD ORE&7: " + goldOre));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Your &nLAPIS ORE&7: " + t6Ore));
		player.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&7Max. orbs to refine: " + ((double) maxOrbs + maxOrbs2)));
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		ItemStack lapisore = new ItemStack(Material.LAPIS_ORE);
		ItemMeta lapismeta = lapisore.getItemMeta();
		lapismeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&9Frozen Ore"));
		lapismeta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7&oA cold chunk of icy ore")));
		lapisore.setItemMeta(lapismeta);

		ItemStack goldore = new ItemStack(Material.GOLD_ORE);
		ItemMeta goldmeta = goldore.getItemMeta();
		goldmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eGold Ore"));
		goldmeta.setLore(
				Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7&oA sparkling piece of gold ore.")));
		goldore.setItemMeta(goldmeta);

		if (chatInteractive.containsKey(player.getUniqueId())) {

			event.setCancelled(true);

			if (event.getMessage().toLowerCase().contains("cancel")) {
				chatInteractive.remove(player.getUniqueId());

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAction &nCANCELLED"));
				return;
			}

			int toRefine = 0;

			try {
				toRefine = Integer.valueOf(event.getMessage());
			} catch (Exception e) {
				chatInteractive.remove(player.getUniqueId());

				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&cInvalid amount - INPUT: &l" + event.getMessage()));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAction &nCANCELLED"));

				return;
			}

			if (toRefine < 0) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAction &nCANCELLED"));
				chatInteractive.remove(player.getUniqueId());
				return;
			}

			int refined = 0;
			int lapisOreUsed = 0;
			int goldOreUsed = 0;
			while (player.getInventory().containsAtLeast(lapisore, 5)) {
				if (refined == toRefine)
					break;
				lapisOreUsed += 5;
				removeOre2(player, 5);
				refined += 1;
			}
			while (player.getInventory().containsAtLeast(goldore, 10)) {
				if (refined == toRefine)
					break;
				goldOreUsed += 10;
				removeOre(player, 10);
				refined += 1;
			}

			if (refined == toRefine) {

				IntStream.range(0, toRefine).forEach(consumer -> {
					player.getInventory().addItem(Items.orb(false));
				});
				chatInteractive.remove(player.getUniqueId());
				player.sendMessage(
						ChatColor.translateAlternateColorCodes('&', "&aYou have created &l" + toRefine + "&a ORBS!"));

			} else {
				if (lapisOreUsed > 1) {
					lapisore.setAmount(lapisOreUsed);
					player.getInventory().addItem(lapisore);
				}

				if (goldOreUsed > 1) {
					goldore.setAmount(goldOreUsed);
					player.getInventory().addItem(goldore);

				}

				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&cYou do &lNOT&c have enough ORE to refine into &l" + toRefine + "&c ORBS!"));
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 10F, 0.3F);
				chatInteractive.remove(player.getUniqueId());
			}

		}

	}

	public static boolean isRefining(Player player) {
		return chatInteractive.containsKey(player.getUniqueId());
	}

	public static void cancel(Player player) {
		if (isRefining(player)) {
			chatInteractive.remove(player.getUniqueId());
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAction &nCANCELLED"));
		}
	}

	private void removeOre(Player p, int amt) {
		int i = 0;
		while (i < p.getInventory().getSize()) {
			ItemStack is = p.getInventory().getItem(i);
			if (amt > 0) {
				int val;
				if (is != null && is.getType() == Material.GOLD_ORE) {
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

	private void removeOre2(Player p, int amt) {
		int i = 0;
		while (i < p.getInventory().getSize()) {
			ItemStack is = p.getInventory().getItem(i);
			if (amt > 0) {
				int val;
				if (is != null && is.getType() == Material.LAPIS_ORE) {
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
}