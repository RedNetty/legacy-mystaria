package me.retrorealms.practiceserver.commands.moderation;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import me.retrorealms.practiceserver.PracticeServer;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;

public class DeployCommand implements CommandExecutor, Listener {
	/**
	 * Created by Kaveen K (https://digistart.ca)
	 * 08/07/2018
	 * 
	 * This is a very sensitive bit of code! It is currently written messily but I will
	 * fix it when I get around to it, this integrates with bitbucket pipelines as well as with
	 * a php front-facing API that runs this command when the pipeline runs. DO NOT TOUCH
	 * THIS IF YOU DON'T KNOW WHAT YOU'RE DOING.
	 */
	
	static PracticeServer plugin;
	private static boolean submitted;
	private static Future submission1, submission2, submission3;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	public static boolean patchlockdown = false;
	public static HashMap<Player, Location> locs = new HashMap<Player, Location>();

	public DeployCommand(PracticeServer plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if (!(sender.getName().equals("Kav_") || sender.getName().equals("Red")
				|| sender.getName().equals("Invested") || sender instanceof ConsoleCommandSender)) {
			sender.sendMessage(ChatColor.RED + "No permission.");
			return true;
		}
		if (submitted) {

			sender.sendMessage(ChatColor.RED + "A deployment has already been initiated.");
			return true;

		}
		// Choose the deployment origin
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Invalid deployment type! /deploy <main/dev>");
			return true;

		}
		String type = args[0];

		Callable<Boolean> callable = () -> {

			try {
				if (type.equalsIgnoreCase("main")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
							"tellall Patch file deployment stage one completed, a new update will be released within the next few minutes!");

					try {
						FileUtils.deleteQuietly(FileUtils.getFile("plugins/RetroServer.jar.old"));
					} catch (Exception e) {
						e.printStackTrace();
					}
					FileUtils.moveFile(FileUtils.getFile("plugins/RetroServer.jar"),
							FileUtils.getFile("plugins/RetroServer.jar.old"));
					FileUtils.copyFile(FileUtils.getFile("/home/deploy/RetroServer.jar"),
							FileUtils.getFile("plugins/RetroServer.jar"));

				} else if (type.equalsIgnoreCase("dev")) {
					if (plugin.devstatus) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
								"tellall Dev server going down automatically for pipeline patch.");
						try {
							FileUtils.deleteQuietly(FileUtils.getFile("/root/dev/plugins/RetroServer.jar.old"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						FileUtils.moveFile(FileUtils.getFile("/root/dev/plugins/RetroServer.jar"),
								FileUtils.getFile("/root/dev/plugins/RetroServer.jar.old"));
						FileUtils.moveFile(FileUtils.getFile("/home/deploy/RetroServer.jar"),
								FileUtils.getFile("/root/dev/plugins/RetroServer.jar"));
					}
				} else {
					return false;
				}
			} catch (Exception e) {

				e.printStackTrace();
				return false;
			}
			return true;
		};
		final Future<Boolean> future = executorService.submit(callable);
		validate(future, type);

		return true;
	}

	private static void validate(Future<Boolean> future, String type) {

		if (!future.isDone()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					validate(future, type);
				}
			}.runTaskLater(plugin, 20 * 5);
		} else {
			try {
				boolean validation = future.get();
				if (validation) {
					if (type.equals("main")) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
								"tellall Patch successful, the server will be locked down in 30 seconds in preparation for patch deployment! Please get to a safe area.");
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
								"tellall Patch successful, the server will be locked down in 30 seconds in preparation for patch deployment! Please get to a safe area.");
						new BukkitRunnable() {
							@Override
							public void run() {
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
										"tellall Pre-deployment server lockdown has started! You will be logged out in 15 seconds..");

								saveAndConsolidate();
								patchlockdown = true;
							}
						}.runTaskLater(plugin, 20 * 30);
						new BukkitRunnable() {
							@Override
							public void run() {
								// Other save stuff here
								PracticeServer.logout.onDisable(true);
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");

							}
						}.runTaskLater(plugin, 20 * 40);
					} else if (type.equals("dev")) {

						new BukkitRunnable() {
							@Override
							public void run() {
								if (plugin.devstatus)
									Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");

							}
						}.runTaskLater(plugin, 20 * 2);
					}
				} else {
					cancelPatch();
					return;
				}

			} catch (Exception e) {
				e.printStackTrace();
				cancelPatch();
				return;
			}

		}

	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if (patchlockdown) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED
					+ "The server is undergoing a patch right now and all functions have been frozen for 15 seconds. ");
		}

	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (patchlockdown) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED
					+ "The server is undergoing a patch right now and all functions have been frozen for 15 seconds. ");
		}

	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (!patchlockdown)
			return;
		Player player = event.getPlayer();
		try {
			Location oldloc = locs.get(player);
			if (player.getLocation().distance(oldloc) > 1) {
				player.teleport(oldloc);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveAndConsolidate() {
		Bukkit.getOnlinePlayers().forEach(player -> {
			Location loc = player.getLocation();
			try {
				Bukkit.getWorld("jew").getNearbyEntities(loc, 5, 5, 5).forEach(entity -> {
					entity.remove();
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (Alignments.chaotic.contains(player.getName()))
				Alignments.chaotic.remove(player.getName());
			if (Alignments.neutral.contains(player.getName()))
				Alignments.neutral.remove(player.getName());
			Alignments.updatePlayerAlignment(player);
			if (Listeners.combat.containsKey(player.getName()))
				Listeners.combat.remove(player.getName());

			locs.put(player, loc);

		});

		PracticeServer.getEconomy().onDisable();
		PracticeServer.getMobs().onDisable();
		PracticeServer.getSpawners().onDisable();
		

	}

	private static void cancelPatch() {
		Bukkit.getOnlinePlayers().forEach(player -> {
			if (player.isOp()) {
				player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString()
						+ "The most recent patch deployment has FAILED! For possible reasons please check the error in console, bitbucket pipeline trace, and the deployment file in /home/deploy!");

			}
		});

	}

}
