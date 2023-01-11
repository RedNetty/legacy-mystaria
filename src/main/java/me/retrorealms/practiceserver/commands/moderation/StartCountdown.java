package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Created by Giovanni on 19-5-2017.
 */
public class StartCountdown implements CommandExecutor {

    private boolean alreadyStarted = false;

    private int countdownTask = 0;
    private int currentTime = 0;

    private final int flag6 = 3600 * 6;
    private boolean flag3 = false;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof ConsoleCommandSender)) return false;

        if (alreadyStarted) {
            commandSender.sendMessage(ChatColor.RED + "Countdown has already been ran.");

            return false;
        }

        alreadyStarted = true;

        commandSender.sendMessage(ChatColor.RED + "Countdown initiated.");

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe &l6 &adonation-less hours have begun, people will receive their items after these expire."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cIf you're still missing your items &nafter&c, contact us over &nDISCORD&c!"));
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 10F, 0.3F);
        });

        countdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.getInstance(), () -> {

            if (currentTime >= flag6) {

                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe &l6 &adonation-less hours have expired, people will receive their items soon."));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cIf you're still missing your items after 30 minutes, contact us over &nDISCORD&c!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 10F, 0.3F);
                });

                Bukkit.getScheduler().cancelTask(countdownTask);

                return;
            }

            if (currentTime >= flag6 / 2) {

                if (!flag3) {

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&l3 &adonation-less hours have expired, people will receive their items in &l3 &ahours"));
                        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 10F, 0.3F);
                    });
                }

                flag3 = true;
            }

            currentTime++;

        }, 0L, 20);

        return false;
    }
}
