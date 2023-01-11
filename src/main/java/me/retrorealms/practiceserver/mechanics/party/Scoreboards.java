/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.entity.Player
 *  org.bukkit.scoreboard.DisplaySlot
 *  org.bukkit.scoreboard.Objective
 *  org.bukkit.scoreboard.Score
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.Team
 */
package me.retrorealms.practiceserver.mechanics.party;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;

public class Scoreboards {
    public static HashMap<Player, Scoreboard> boards = new HashMap<Player, Scoreboard>();


    public static Scoreboard getBoard(Player p) {
        if (!boards.containsKey(p)) {
            Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
            Team red = sb.registerNewTeam("red");
            red.setPrefix(ChatColor.RED.toString());
            Team yellow = sb.registerNewTeam("yellow");
            yellow.setPrefix(ChatColor.YELLOW.toString() + (PracticeServer.FFA ? ChatColor.MAGIC.toString() : ""));
            Team white = sb.registerNewTeam("white");
            white.setPrefix(ChatColor.WHITE.toString());
            Team gm = sb.registerNewTeam("gm");
            gm.setPrefix(String.valueOf(ChatColor.AQUA.toString()) + ChatColor.BOLD + "GM " + ChatColor.AQUA);
            Team manager = sb.registerNewTeam("manager");
            manager.setPrefix(String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + "MANAGER " + ChatColor.YELLOW);
            Team dev = sb.registerNewTeam("dev");
            dev.setPrefix(String.valueOf(ChatColor.RED.toString()) + ChatColor.BOLD + "DEV " + ChatColor.RED);
            Objective o = sb.registerNewObjective("showHealth", "health");
            o.setDisplaySlot(DisplaySlot.BELOW_NAME);
            o.setDisplayName(ChatColor.RED + "\u2764");
            boards.put(p, sb);
            return sb;
        }
        return boards.get(p);
    }

    public static void updateAllColors() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = Scoreboards.getBoard(p);
            for (Player pl : Bukkit.getOnlinePlayers()) {
                RankEnum rankEnum = ModerationMechanics.getRank(pl);
                if (rankEnum == RankEnum.DEV) {
                    sb.getTeam("dev").addPlayer(pl);
                    continue;
                }
                if (rankEnum == RankEnum.MANAGER) {
                    sb.getTeam("manager").addPlayer(pl);
                    continue;
                }
                if (rankEnum == RankEnum.GM) {
                    sb.getTeam("gm").addPlayer(pl);
                    continue;
                }
                if (Alignments.neutral.containsKey(pl.getName())) {
                    sb.getTeam("yellow").addPlayer(pl);
                    continue;
                }
                if (Alignments.chaotic.containsKey(pl.getName())) {
                    sb.getTeam("red").addPlayer(pl);
                    continue;
                }
                sb.getTeam("white").addPlayer(pl);
            }
            p.setScoreboard(sb);
            boards.put(p, sb);
        }
    }

    @SuppressWarnings("deprecation")
    public static void updatePlayerHealth() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = Scoreboards.getBoard(p);
            for (Player pl : Bukkit.getOnlinePlayers()) {
                Objective o = sb.getObjective(DisplaySlot.BELOW_NAME);
                o.getScore(pl).setScore((int) pl.getHealth());
            }
            p.setScoreboard(sb);
            boards.put(p, sb);
        }
    }
}
