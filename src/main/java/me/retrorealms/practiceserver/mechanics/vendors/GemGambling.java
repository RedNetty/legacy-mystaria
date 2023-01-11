package me.retrorealms.practiceserver.mechanics.vendors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.mechanics.money.Money;
/**
 * Created by Kaveen K (https://digistart.ca)
 * 08/09/2018
 */
public class GemGambling implements Listener {

	public static ArrayList<String> inPlayGem = new ArrayList<String>();
	public static ArrayList<String> inSecondStage = new ArrayList<String>();
	public static HashMap<String, Integer> gambleamounts = new HashMap<String, Integer>();
	public static HashMap<String, Integer> queuerefund = new HashMap<String, Integer>();
	public static ArrayList<String> gamblerrolled = new ArrayList<String>();

	/*
	 * the inPlayGem is the chathandler arraylist !! It goes in stages, it goes
	 * from inPlayGem to inSecondStage. The stages are not removed one by one!
	 * They're removed all at once!
	 */

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (this.inPlayGem.contains(player.getName())) {
			player.teleport(event.getFrom());
		}

	}

	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (gambleamounts.containsKey(player.getName()) || inPlayGem.contains(player.getName()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		if (gambleamounts.containsKey(player.getName()) || inPlayGem.contains(player.getName()))
			event.setCancelled(true);

	}

	@EventHandler
	public void onInteractChest(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (gambleamounts.containsKey(player.getName()) || inPlayGem.contains(player.getName()))
			event.setCancelled(true);

	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event) {
		if (DeployCommand.patchlockdown) {
			event.setCancelled(true);
			return;
		}
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();

		if (inPlayGem.contains(player.getName())) {
			event.setCancelled(true);
			return;
		}

		if (event.getHand() == EquipmentSlot.OFF_HAND)
			return;

		if (entity.getName().equalsIgnoreCase("Gem Gambler")) {
			inPlayGem.add(player.getName());

			// Get player's inventory so we can iterate over it and run checks
			// and some shit
			// From this point on it's not gonna be the smoothest or cleanest
			// code but it'll work
			// and it won't be laggy :)
			ItemStack[] inventory = player.getInventory().getContents();
			int notecount = 0;
			ArrayList<ItemStack> notes = new ArrayList<ItemStack>();
			for (ItemStack i : inventory) {
				if (i == null || i.getType().equals(null) || i.getType().equals(Material.AIR))
					continue;
				if (i.getType().equals(Material.PAPER)
						&& i.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Bank Note")) {
					if (i.getItemMeta().getLore() != null && i.getItemMeta().getLore().get(1)
							.equals(ChatColor.GRAY + "Exchange at any bank for GEM(s)")) {
						notes.add(i);
						notecount++;
					}
				}
			}
			if (notecount == 0) {
				player.sendMessage(
						ChatColor.translateAlternateColorCodes('&', "&c&lYou have no gem notes to gamble with!"));
				inPlayGem.remove(player.getName());
				return;
			}
			if (notecount > 1) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&c&lYou can only have one gem note in your inventory to gamble! Combine them!"));
				inPlayGem.remove(player.getName());
				return;
			}

			int gambleamount = Money.getGems(notes.get(0));
			/*
			 * I separated inPlayGem from serving its function as a holder for
			 * the gem amount because this way I can consolidate on the amount
			 * the player is going to gamble before they actually accept
			 */
			gambleamounts.put(player.getName(), gambleamount);

			player.sendMessage("");
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&aWould you like to gamble using your &l" + gambleamount + "G&a banknote?"));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aType &l'Y'&a or &l'YES'&a to continue!"));
			player.sendMessage("");
			player.sendMessage(
					ChatColor.translateAlternateColorCodes('&', "&cType &n'cancel'&c to cancel this action."));
			player.sendMessage("");

		}
		if (gambleamounts.containsKey(player.getName()) || inPlayGem.contains(player.getName()))
			event.setCancelled(true);

	}

	/*
	 * Not sure why these checks weren't in the original code, they could've
	 * duped by altering their inventory right before the gambler took its gems
	 * because the code earlier didn't check anything after the first time
	 * before it actually took the gems
	 */
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (gambleamounts.containsKey(player.getName()) || inPlayGem.contains(player.getName()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (gambleamounts.containsKey(player.getName()) || inPlayGem.contains(player.getName()))
			event.setCancelled(true);

	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		// All three of these have to be set for the player before it can start
		// (prevents gay vawke dupes)
		if (!this.inPlayGem.contains(player.getName()))
			return;
		if (inSecondStage.contains(player.getName())) {
			event.setCancelled(true);
			return;
		}

		event.setCancelled(true);

		String message = event.getMessage();

		if (message.toLowerCase().equals("cancel") || message.toLowerCase().equals("no")) {
			removeFromAll(player, false);

			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAction &nCANCELLED"));
			return;
		} else if (message.toLowerCase().equals("yes") || message.toLowerCase().equals("y")) {
			if (!Money.hasEnoughGems(player, gambleamounts.get(player.getName()))) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&cThis gamble was cancelled because the amount of gems you had changed!"));
				removeFromAll(player, false);
				return;
			}
			Money.takeGems(player, gambleamounts.get(player.getName()));
			inSecondStage.add(player.getName());
			initiateGamble(player);

		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cPlease type &a'Y'&c or 'cancel'!"));
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (inSecondStage.contains(player.getName())) {
			if (!gamblerrolled.contains(player.getName())) {

				removeFromAll(player, true);

			} else {

				removeFromAll(player, false);

			}
		} else if (inPlayGem.contains(player.getName())) {
			removeFromAll(player, false);
		}

	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (queuerefund.containsKey(player.getName())) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&cYou left in the middle of a gamble! Please wait until your gem note is refunded..."));
			int refundamount = queuerefund.get(player.getName());
			ItemStack refundgems = Money.createBankNote(refundamount);

			new BukkitRunnable() {
				@Override
				public void run() {
					player.getInventory().addItem(refundgems);
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYour gem note was refunded!"));
					removeFromAll(player, false);

				}
			}.runTaskLater(PracticeServer.plugin, 20 * 3);

		}

	}

	private void initiateGamble(Player player) {
		int gambleamount = gambleamounts.get(player.getName());
		Random rand = new Random();

		int highgambler = rand.nextInt(3);
		int gamblerroll;
		if (highgambler >= 1) {
			gamblerroll = rand.nextInt(60) + 40;
		} else {
			gamblerroll = rand.nextInt(50) + 1;
		}

		int playerroll;
		playerroll = rand.nextInt(100) + 1;

		if (inPlayGem.contains(player.getName())) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eRolling...."));

			new BukkitRunnable() {
				@Override
				public void run() {
					if (!player.isOnline()) {

						this.cancel();
						return;
					}
					if (player.isOnline())
						gamblerrolled.add(player.getName());

					player.sendMessage(
							ChatColor.translateAlternateColorCodes('&', "&eThe gambler rolled &e&l" + gamblerroll));
					new BukkitRunnable() {
						@Override
						public void run() {
							if (!player.isOnline()) {
								this.cancel();
								return;
							}
							player.sendMessage(
									ChatColor.translateAlternateColorCodes('&', "&eYou rolled &e&l" + playerroll));
							if (playerroll > gamblerroll) {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&',
										"&aYou have won! You have recieved &a&l"
												+ gambleamounts.get(player.getName()) * 2 + "&a&lG"));
								player.getInventory()
										.addItem(Money.createBankNote(gambleamounts.get(player.getName()) * 2));

								removeFromAll(player, false);

							} else if (playerroll == gamblerroll) {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&',
										"&eYou tied with the gambler and your money was returned."));
								player.getInventory()
										.addItem(Money.createBankNote(gambleamounts.get(player.getName())));

								removeFromAll(player, false);

							} else {
								player.sendMessage(
										ChatColor.translateAlternateColorCodes('&', "&cYou have lost the gamble! :("));

								removeFromAll(player, false);

							}

						}
					}.runTaskLater(PracticeServer.plugin, 20 * 3);

				}
			}.runTaskLater(PracticeServer.plugin, 20 * 2);
		}

	}

	private void removeFromAll(Player player, boolean queue) {
		inPlayGem.remove(player.getName());
		inSecondStage.remove(player.getName());

		if (!queue) {
			queuerefund.remove(player.getName());
		} else {
			queuerefund.put(player.getName(), gambleamounts.get(player.getName()));
		}
		gambleamounts.remove(player.getName());
		gamblerrolled.remove(player.getName());

	}
}
