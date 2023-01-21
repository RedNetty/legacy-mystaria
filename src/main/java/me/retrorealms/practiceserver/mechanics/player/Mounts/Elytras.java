package me.retrorealms.practiceserver.mechanics.player.Mounts;

import java.util.ArrayList;
import java.util.HashMap;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.teleport.AvalonPortal;
import me.retrorealms.practiceserver.utils.ArmorEquipEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class Elytras implements Listener {
	/**
	 * Created by Kaveen K (https://digistart.ca) 08/09/2018
	 */

	PracticeServer plugin;

	private static ArrayList<Player> gliding = new ArrayList<Player>();
	private static HashMap<Player, Location> summoning = new HashMap<Player, Location>();
	private static HashMap<String, ItemStack> queuechestplate = new HashMap<String, ItemStack>();
	public static HashMap<String, BukkitTask> taskmap = new HashMap<String, BukkitTask>();
	private boolean launched = false;
	private static ArrayList<Player> queueheal = new ArrayList<Player>();

	public Elytras(PracticeServer plugin) {
		this.plugin = plugin;
		//plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onCancelMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (summoning.containsKey(player)) {
			Location oldloc = summoning.get(player);
			Location currentloc = player.getLocation();
			if (oldloc.distance(currentloc) > 2) {
				summoning.remove(player);
				player.sendMessage(
						ChatColor.RED + ChatColor.BOLD.toString() + "CANCELLED ELYTRA MOUNT DUE TO MOVEMENT");

			}

		}

	}

	@EventHandler
	public void onDamageAll(EntityDamageEvent event) {
	/*	if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();

		if (queueheal.contains(player)) {
			if (event.getCause().equals(DamageCause.FALL)) {
				event.setDamage(0);
				event.setCancelled(true);

			}

		}
		if (summoning.containsKey(player)) {
			summoning.remove(player);
			player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "CANCELLED ELYTRA MOUNT DUE TO DAMAGE");
		}
		if (gliding.contains(player)) {
			stopGliding(player);
		}
	}*/
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		if (summoning.containsKey(player)) {
			summoning.remove(player);
			player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "CANCELLED ELYTRA MOUNT DUE TO DAMAGE");
		}
		if (gliding.contains(player)) {
			stopGliding(player);
		}

	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (summoning.containsKey(player)) {
			summoning.remove(player);
			player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "CANCELLED ELYTRA MOUNT DUE TO MOVEMENT");
		}
		if (gliding.contains(player)) {
			stopGliding(player);
		}

	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (gliding.contains(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onFlyClick(InventoryClickEvent event) {
		if (gliding.contains(event.getWhoClicked()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (event.getItemDrop().getItemStack().getType().equals(Material.ELYTRA)) {
			event.setCancelled(true);
			return;
		}

	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event) {
		if (event.getItem().getItemStack().getType().equals(Material.ELYTRA)) {
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onMove(InventoryMoveItemEvent event) {

	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (event.getClick() == ClickType.NUMBER_KEY) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.ELYTRA))
				event.setCancelled(true);
			if (event.getCursor() != null && event.getCursor().getType().equals(Material.ELYTRA))
				event.setCancelled(true);
			if (event.getSlotType().equals(SlotType.ARMOR))
				event.setCancelled(true);
		}

		if (event.getSlotType().equals(SlotType.ARMOR)) {
			if (event.getCursor() != null && event.getCursor().getType().equals(Material.ELYTRA)) {
				event.setCancelled(true);
			}

		}
		if (event.isShiftClick()) {
			if ((event.getCursor() != null && event.getCursor().getType().equals(Material.ELYTRA))
					|| (event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.ELYTRA))) {
				event.setCancelled(true);
			}

		}
		if (event.getInventory().getName() != null && (event.getInventory().getName().equals("Bank Chest (1/1)")
				|| event.getInventory().getName().contains(event.getWhoClicked().getName()))) {
			if (event.getCursor() != null && event.getCursor().getType().equals(Material.ELYTRA)) {
				event.setCancelled(true);
			}

		}
		return;

	}

	@EventHandler
	public void onEquip(ArmorEquipEvent event) {
		if (event.getNewArmorPiece() != null && event.getNewArmorPiece().getType().equals(Material.ELYTRA)) {
			event.setCancelled(true);
			return;
		}

	}

	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		if (((HumanEntity) event.getEntity()).getInventory().contains(new ItemStack(Material.ELYTRA))) {
			event.getDrops().remove(new ItemStack(Material.ELYTRA));
		} else {
			// If you would like send a message for not having the item on
			// death. *optional*
		}

	}

	@EventHandler
	public void onMount(PlayerInteractEvent event) {
		// TODO - this is just a temporary catch block to silence the stupid ass
		// annoying nullpointers I haven't slept in 43 hours
		try {
			if (event.getAction().equals(Action.RIGHT_CLICK_AIR)
					|| event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				Player player = event.getPlayer();

				if (player.getItemInHand() != null && player.getItemInHand().getItemMeta().getDisplayName() != null
						&& player.getItemInHand().getItemMeta().getDisplayName()
								.equals(ChatColor.AQUA + "Elytra Mount")) {
					if (summoning.containsKey(player) || gliding.contains(player)
							|| !player.hasPermission("retrorealms.betaelytra")) {
						event.setCancelled(true);
						return;
					}
					event.setCancelled(true);
					if (isFrostFall(player)) {
						if (!player.getLocation().add(new Vector(0, 15, 0)).getBlock().getType().equals(Material.AIR)
								|| player.getLocation().add(new Vector(0, 15, 0)).getY() >= 196) {
							launched = false;
							player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString()
									+ "Unable to launch elytra mount, something would block your flight above you, or you are too high up to launch.");

							return;
						}
					} else if (isAvalon(player)) {

						if (!player.getLocation().add(new Vector(0, 15, 0)).getBlock().getType().equals(Material.AIR)
								|| player.getLocation().add(new Vector(0, 15, 0)).getY() >= 130) {
							launched = false;
							player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString()
									+ "Unable to launch elytra mount, something would block your flight above you, or you are too high up to launch.");

							return;
						}
					} else if (isDeadpeaks(player)) {
						if (!player.getLocation().add(new Vector(0, 15, 0)).getBlock().getType().equals(Material.AIR)
								|| player.getLocation().add(new Vector(0, 15, 0)).getY() >= 70) {
							launched = false;
							player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString()
									+ "Unable to launch elytra mount, something would block your flight above you, or you are too high up to launch.");

							return;
						}

					} else {
						if (!player.getLocation().add(new Vector(0, 15, 0)).getBlock().getType().equals(Material.AIR)) {
							player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString()
									+ "Unable to launch elytra mount, this is not a safe mount location.");

							return;
						}
					}

					summoning.put(player, player.getLocation());

					new BukkitRunnable() {
						int count = 5;

						@Override

						public void run() {
							if (!summoning.containsKey(player))
								this.cancel();
							if (count <= 0) {

								summoning.remove(player);
								player.setVelocity(new Vector(0, 2, 0));

								launched = true;

								new BukkitRunnable() {

									@Override
									public void run() {
										if (!launched)
											this.cancel();

										player.sendMessage(ChatColor.WHITE + ChatColor.BOLD.toString()
												+ "ACTIVATED ELYTRA MOUNT!");// PRESS SHIFT TO BOOST WHILE GLIDING!");

										ItemStack[] armorcontents = player.getInventory().getArmorContents();
										queuechestplate.put(player.getName(), armorcontents[2]);
										armorcontents[2] = new ItemStack(Material.ELYTRA);
										player.getInventory().setArmorContents(armorcontents);

										player.setGliding(true);
										launched = false;
										summoning.remove(player);
										BukkitTask task = new BukkitRunnable() {
											@Override
											public void run() {
												if (gliding.contains(player)) {
													stopGliding(player);
												}
											}

										}.runTaskLater(plugin, 20 * 30);
										taskmap.put(player.getName(), task);

									}
								}.runTaskLater(plugin, 10);

								this.cancel();
							}
							player.sendMessage(ChatColor.WHITE + ChatColor.BOLD.toString()
									+ "SUMMONING ELYTRA MOUNT.... " + count);
							count -= 1;

						}
					}.runTaskTimer(plugin, 0, 20);

				}

			}
		} catch (Exception e) {
			// Silence
		}

	}

//	@EventHandler
//	public void onShift(PlayerToggleSneakEvent event) {
//		Player player = event.getPlayer();
//		if (player.isGliding()) {
//			if (gliding.contains(player)) {
//				player.setVelocity(player.getLocation().getDirection().multiply(1.001));
//			}
//		}
//	}

	@EventHandler
	public void onElytra(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (player.isGliding()) {
			if (!gliding.contains(player)) {
				gliding.add(player);
			} else if (gliding.contains(player)) {
				if (isFrostFall(player)) {

					if (player.getLocation().getY() >= 195) {
						stopGliding(player, true);
					}

				} else if (isDeadpeaks(player)) {

					if (player.getLocation().getY() >= 71) {
						stopGliding(player, true);
					}
				} else if (isAvalon(player)) {

					if (player.getLocation().getY() >= 136) {
						stopGliding(player, true);
					}
				} else {
					if (player.getLocation().getY() >= 70) {

						stopGliding(player, true);
					}
				}

			}

		} else {
			if (gliding.contains(player)) {
				gliding.remove(player);

				Listeners.hpCheck(player);
				queuechestplate.remove(player.getName());
				try {

					taskmap.get(player.getName()).cancel();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}

	}

	// Overload
	private void stopGliding(Player player) {
		stopGliding(player, false);
	}

	private void stopGliding(Player player, boolean blister) {
/*		player.setGliding(false);
		player.setVelocity(new Vector(0, -5, 0));
		player.setFallDistance(0); // For some reason this doesn't work for DR
		AvalonPortal.glidingPlayers.remove(player);
		if (!blister) {
			player.sendMessage(ChatColor.RED + "Your elytra wings have blistered and are unable to fly anymore.");
		} else {
			player.sendMessage(
					ChatColor.RED + "You have flown too high and your elytra wings have blistered. Please fly lower.");
		}
		queueheal.add(player);*/
	}

	private boolean isDeadpeaks(Player player) {
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();
		if (x >= 620) {
			if (z >= -270 && z < 230) {
				return true;

			}
		}
		return false;

	}

	private boolean isFrostFall(Player player) {
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();
		if (x <= 270 && x >= -100) {
			if (z >= 100 && z <= 267) {
				return true;
			}
		}
		return false;
	}

	private boolean isAvalon(Player player) {
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();
		if (x <= 645) {
			if (z >= 240) {
				return true;
			}
		}
		return false;

	}

}
