package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.retrorealms.practiceserver.mechanics.pvp.Alignments;

public class ToggleAlignmentGlowCommand implements CommandExecutor {
	public static boolean runningGlow = false;
	/**
	 * Created by Kaveen K (https://digistart.ca)
	 * 08/08/2018
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		if (sender.isOp()) {

			if (PracticeServer.ALIGNMENT_GLOW) {

				sender.sendMessage(ChatColor.RED + "Globally disabled alignment glow");
				PracticeServer.ALIGNMENT_GLOW = false;
				Bukkit.getOnlinePlayers().forEach(player -> {

					try {

						org.inventivetalent.glow.GlowAPI.setGlowing(player, null, Bukkit.getOnlinePlayers());
					} catch (Exception e) {

					}
				});

			} else {
				sender.sendMessage(ChatColor.GREEN + "Globally enabled alignment glow");

				for (Player e : Bukkit.getOnlinePlayers()) {
					if (Alignments.chaotic.containsKey(e.getName())) {
						Entity entity = (Entity) e;
						org.inventivetalent.glow.GlowAPI.setGlowing(e, org.inventivetalent.glow.GlowAPI.Color.RED,
								Bukkit.getOnlinePlayers());
					}
					if (Alignments.neutral.containsKey(e.getName())) {
						Entity entity = (Entity) e;
						org.inventivetalent.glow.GlowAPI.setGlowing(e, org.inventivetalent.glow.GlowAPI.Color.YELLOW,
								Bukkit.getOnlinePlayers());
					}

				}

				PracticeServer.ALIGNMENT_GLOW = true;
				if (!runningGlow) {
					new BukkitRunnable() {

						@Override
						public void run() {
							if (!PracticeServer.ALIGNMENT_GLOW) {
								Bukkit.getOnlinePlayers().forEach(player -> {

									try {

										org.inventivetalent.glow.GlowAPI.setGlowing(player, null,
												Bukkit.getOnlinePlayers());
									} catch (Exception e) {
										e.printStackTrace();
									}
								});
								runningGlow = false;
								this.cancel();
								return;
							}

							for (Player player : Bukkit.getOnlinePlayers()) {
								if (Alignments.chaotic.containsKey(player.getName())) {

									org.inventivetalent.glow.GlowAPI.setGlowing(player,
											org.inventivetalent.glow.GlowAPI.Color.RED, Bukkit.getOnlinePlayers());
								} else if (Alignments.neutral.containsKey(player.getName())) {

									org.inventivetalent.glow.GlowAPI.setGlowing(player,
											org.inventivetalent.glow.GlowAPI.Color.YELLOW, Bukkit.getOnlinePlayers());
								} else {

									org.inventivetalent.glow.GlowAPI.setGlowing(player, null,
											Bukkit.getOnlinePlayers());
								}

							}

						}
					}.runTaskTimer(PracticeServer.plugin, 20 * 2, 20 * 2);
					runningGlow = true;

				}

			}

		}

		return true;
	}

}
