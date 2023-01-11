package me.retrorealms.practiceserver.commands.guilds;

import me.retrorealms.practiceserver.mechanics.guilds.guild.Guild;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import me.retrorealms.practiceserver.mechanics.pvp.Deadman;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Matthew E on 8/4/2017.
 */
public class GuildCreateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if(Deadman.deadman && !sender.isOp()){
                sender.sendMessage(ChatColor.RED + "Guilds cannot be created during Deadman Tournaments");
            }
            Player player = (Player) sender;
            if (args.length == 2) {
                String name = args[0];
                String tag = args[1];
                if (GuildManager.getInstance().isGuild(name, tag)) {
                    player.sendMessage(ChatColor.RED + "The guilds already exists.");
                    return true;
                }
                GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
                if (guildPlayer.isInGuild()) {
                    Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildName());
                    if (guild.getOwner().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
                        player.sendMessage(ChatColor.RED + "You're already the owner of a guilds type /gdisband to disband you're guilds");
                        return true;
                    }
                    player.sendMessage(ChatColor.RED + "You're already in a guilds type /gquit to leave your current guilds.");
                    return true;
                }
                if (!Money.hasEnoughGems(player, 5000)) {
                    player.sendMessage(ChatColor.RED + "You don't have enough gems");
                    return true;
                }
                if (tag.length() > 4 && !player.isOp()) {
                    player.sendMessage(ChatColor.RED + "Error: Max tag length is 4");
                    return true;
                }if (name.length() > 50) {
                    player.sendMessage(ChatColor.RED + "Error: Max name length is 50");
                    return true;
                }
                if (tag.toLowerCase().contains("kkk")) {
                    player.sendMessage(ChatColor.RED + "This tag is banned.");
                    return true;
                }
                Pattern pattern = Pattern.compile("^([A-Za-z]|[0-9])+$");
                Matcher matcher = pattern.matcher(name);
                if (!matcher.find()) {
                    player.sendMessage(ChatColor.RED + "You guild name can only contain alphanumerical values.");
                    return true;
                }
                if (!pattern.matcher(tag).find()) {
                    player.sendMessage(ChatColor.RED + "You guild tag can only contain alphanumerical values.");
                    return true;
                }
                Money.takeGems(player, 5000);
                player.sendMessage(ChatColor.RED + "-5,000" + ChatColor.BOLD + "g");
                player.sendMessage("");
                player.sendMessage("Congratulations, you are now the proud owner of the '" + name + "' guilds!");
                player.sendMessage("Hold [TAB] on your keyboard to view your guilds's dashboard.");
                player.sendMessage(ChatColor.GRAY + "You can now chat in your guilds chat with " + ChatColor.BOLD + "/g <msg>" + ChatColor.GRAY + ", invite players with " + ChatColor.BOLD + "/ginvite <player>" + ChatColor.GRAY + " and much more -- Check out your character journal for more information!");
                GuildManager.getInstance().createGuild(name, tag, player.getUniqueId());
                guildPlayer.setGuildName(name);
                Guild guild = GuildManager.guildMap.get(name);
                guild.save();
                SQLMain.updateGuild(guildPlayer.getUuid(), guildPlayer.getGuildName());
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Correct usage: /gcreate <name> <tag> - COSTS 5,000g");
            }
        }
        return true;
    }
}
