package me.retrorealms.practiceserver.mechanics.party;

import lombok.Getter;
import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Parties implements Listener {
    @Getter
    public static ConcurrentHashMap<Player, ArrayList<Player>> parties;
    public static ConcurrentHashMap<Player, Player> invite;
    public static ConcurrentHashMap<Player, Long> invitetime;
    public static int maxSize = 8;

    static {
        Parties.parties = new ConcurrentHashMap<Player, ArrayList<Player>>();
        Parties.invite = new ConcurrentHashMap<Player, Player>();
        Parties.invitetime = new ConcurrentHashMap<Player, Long>();
    }

    public static boolean isPartyLeader(final Player p) {
        return Parties.parties.containsKey(p);
    }

    public static Player getPartyLeader(final Player p){
        for(Player member : getEntirePartyOf(p)){
            if(isPartyLeader(member)) return member;
        }
        return p; // shouldnt happen
    }

    @SuppressWarnings("deprecation")
    public static void refreshScoreboard(final Player p) {
        if (isInParty(p)) {
            final ArrayList<Player> mem = Parties.parties.get(getParty(p));
            final Scoreboard sb = Scoreboards.getBoard(p);
            if (sb.getObjective(DisplaySlot.SIDEBAR) != null) {
                final Objective o = sb.getObjective(DisplaySlot.SIDEBAR);

                mem.forEach(pl -> {

                    if (Parties.parties.containsKey(pl)) {
                        String name = ChatColor.BOLD + pl.getName();
                        if (name.length() > 16) {
                            name = name.substring(0, 16);
                        }
                        o.getScore(name).setScore((int) pl.getHealth());
                        o.getScore(name).setScore((int) pl.getHealth());
                    } else {
                        String name = pl.getName();
                        if (name.length() > 16) {
                            name = name.substring(0, 16);
                        }
                        o.getScore(name).setScore((int) pl.getHealth());
                    }
                });
                p.setScoreboard(sb);
                Scoreboards.boards.put(p, sb);
            } else {
                updateScoreboard(p);
            }
        }
    }

    public static ArrayList<Player> getEntirePartyOf(Player player) {
        if (isPartyLeader(player)) return parties.get(player);

        List<Player>[] partyList;

        for (ArrayList<Player> partyValues : parties.values())
            if (partyValues.contains(player))
                return partyValues;

        return null;
    }

    @SuppressWarnings("deprecation")
    public static void updateScoreboard(final Player p) {
        if (isInParty(p)) {
            final ArrayList<Player> mem = Parties.parties.get(getParty(p));
            final Scoreboard sb = Scoreboards.getBoard(p);
            if (sb.getObjective(DisplaySlot.SIDEBAR) != null) {
                sb.getObjective(DisplaySlot.SIDEBAR).unregister();
            }
            final Objective o = sb.registerNewObjective("party_data", "dummy");
            o.setDisplayName(new StringBuilder().append(ChatColor.AQUA).append(ChatColor.BOLD).append("Party").toString());
            o.setDisplaySlot(DisplaySlot.SIDEBAR);
            for (final Player pl : mem) {
                if (Parties.parties.containsKey(pl)) {
                    String name = ChatColor.BOLD + pl.getName();
                    if (name.length() > 16) {
                        name = name.substring(0, 16);
                    }
                    o.getScore(name).setScore((int) pl.getHealth());
                } else {
                    String name = pl.getName();
                    if (name.length() > 16) {
                        name = name.substring(0, 16);
                    }
                    o.getScore(name).setScore((int) pl.getHealth());
                }
            }
            //   p.setScoreboard(sb);
            Scoreboards.boards.put(p, sb);
        } else {
            final Scoreboard sb2 = Scoreboards.getBoard(p);
            if (sb2.getObjective(DisplaySlot.SIDEBAR) != null) {
                sb2.getObjective(DisplaySlot.SIDEBAR).unregister();
            }
        }
    }

    public static void createParty(final Player p) {
        Parties.parties.put(p, new ArrayList<Player>(Arrays.asList(p)));
        updateScoreboard(p);
    }

    public static void addPlayer(final Player added, final Player leader) {
        if (Parties.parties.containsKey(leader)) {
            final ArrayList<Player> mem = Parties.parties.get(leader);
            if (!mem.contains(added)) {
                mem.add(added);
            }
            Parties.parties.put(leader, mem);
            for (final Player p : mem) {
                updateScoreboard(p);
            }
        }
    }

    public static void removePlayer(final Player p) {
        if (isInParty(p)) {
            if (isPartyLeader(p)) {
                if (Parties.parties.get(p).size() > 1) {
                    final ArrayList<Player> mem = Parties.parties.get(p);
                    mem.remove(p);
                    final Player newleader = mem.get(0);
                    Parties.parties.put(newleader, mem);
                    Parties.parties.remove(p);
                    newleader.sendMessage(ChatColor.RED + "You have been made the party leader!");
                    for (final Player pl : mem) {
                        pl.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE.toString()) + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + ">" + ChatColor.GRAY + " " + p.getName() + ChatColor.GRAY.toString() + " has " + ChatColor.LIGHT_PURPLE + ChatColor.UNDERLINE + "left" + ChatColor.GRAY.toString() + " your party.");
                        pl.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE.toString()) + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + "> " + ChatColor.GRAY + ChatColor.LIGHT_PURPLE.toString() + newleader.getName() + ChatColor.GRAY.toString() + " has been promoted to " + ChatColor.UNDERLINE + "Party Leader");
                        updateScoreboard(pl);
                    }
                } else {
                    Parties.parties.remove(p);
                }
            } else {
                for (final Player key : Parties.parties.keySet()) {
                    if (Parties.parties.get(key).contains(p)) {
                        final ArrayList<Player> mem2 = Parties.parties.get(key);
                        mem2.remove(p);
                        Parties.parties.put(key, mem2);
                        for (final Player pl2 : mem2) {
                            pl2.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE.toString()) + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + ">" + ChatColor.GRAY + " " + p.getName() + ChatColor.GRAY.toString() + " has " + ChatColor.RED + ChatColor.UNDERLINE + "left" + ChatColor.GRAY.toString() + " your party.");
                            updateScoreboard(pl2);
                        }
                    }
                }
            }
            updateScoreboard(p);
        }
    }

    public static boolean isInParty(final Player p) {
        for (final Player key : Parties.parties.keySet()) {
            if (Parties.parties.get(key).contains(p)) {
                return true;
            }
        }
        return false;
    }

    public static boolean arePartyMembers(final Player p1, final Player p2) {
        for (final Player key : Parties.parties.keySet()) {
            if (Parties.parties.get(key).contains(p1) && Parties.parties.get(key).contains(p2)) {
                return true;
            }
        }
        return false;
    }

    public static Player getParty(final Player p) {
        if (Parties.parties.containsKey(p)) {
            return p;
        }
        if (isInParty(p)) {
            for (final Player key : Parties.parties.keySet()) {
                if (Parties.parties.get(key).contains(p)) {
                    return key;
                }
            }
        }
        return null;
    }

    public static void inviteToParty(final Player p, final Player owner) {
        if (p == owner) {
            p.sendMessage(ChatColor.RED + "You cannot invite yourself to your own party.");
            return;
        }
        if (!isPartyLeader(owner) && isInParty(owner)) {
            owner.sendMessage(String.valueOf(ChatColor.RED.toString()) + "You are NOT the leader of your party.");
            owner.sendMessage(String.valueOf(ChatColor.GRAY.toString()) + "Type " + ChatColor.BOLD.toString() + "/pquit" + ChatColor.GRAY + " to quit your current party.");
            return;
        }
        if (isInParty(owner) && isPartyLeader(owner) && Parties.parties.get(owner).size() == maxSize) {
            owner.sendMessage(ChatColor.RED + "You cannot have more than " + ChatColor.ITALIC + Integer.toString(maxSize) + " players" + ChatColor.RED + " in a party.");
            owner.sendMessage(ChatColor.GRAY + "You may use /pkick to kick out unwanted members.");
            return;
        }
        if (isInParty(p)) {
            if (getParty(p) == owner) {
                owner.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append(p.getName()).append(ChatColor.RED).append(" is already in your party.").toString());
                owner.sendMessage(ChatColor.GRAY + "Type /pkick " + p.getName() + " to kick them out.");
            } else {
                owner.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append(p.getName()).append(ChatColor.RED).append(" is already in another party.").toString());
            }
            return;
        }
        if (Parties.invite.containsKey(p)) {
            owner.sendMessage(ChatColor.RED + p.getName() + " has a pending party invite.");
            return;
        }
        if (!isInParty(owner)) {
            owner.sendMessage(new StringBuilder().append(ChatColor.GREEN).append(ChatColor.BOLD).append("Party created.").toString());
            owner.sendMessage(ChatColor.GRAY + "To invite more people to join your party, " + ChatColor.UNDERLINE + "Left Click" + ChatColor.GRAY.toString() + " them with your character journal or use " + ChatColor.BOLD + "/pinvite" + ChatColor.GRAY + ". To kick, use " + ChatColor.BOLD + "/pkick" + ChatColor.GRAY + ".");
            createParty(owner);
        }
        p.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE.toString()) + ChatColor.UNDERLINE + owner.getName() + ChatColor.GRAY + " has invited you to join their party. To accept, type " + ChatColor.LIGHT_PURPLE.toString() + "/paccept" + ChatColor.GRAY + " or to decline, type " + ChatColor.LIGHT_PURPLE.toString() + "/pdecline");
        owner.sendMessage(ChatColor.GRAY + "You have invited " + ChatColor.LIGHT_PURPLE.toString() + p.getName() + ChatColor.GRAY + " to join your party.");
        Parties.invite.put(p, owner);
        Parties.invitetime.put(p, System.currentTimeMillis());
    }

    public void onEnable() {
        PracticeServer.log.info("[Parties] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents((Listener) this, PracticeServer.plugin);
        new BukkitRunnable() {
            public void run() {
                for (final Player p : Bukkit.getOnlinePlayers()) {
                    Parties.refreshScoreboard(p);
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 1L, 1L);
        new BukkitRunnable() {
            public void run() {
                for (final Player p : Parties.invite.keySet()) {
                    if (Parties.invitetime.containsKey(p) && System.currentTimeMillis() - Parties.invitetime.get(p) > 30000L) {
                        if (p.isOnline()) {
                            p.sendMessage(ChatColor.RED + "Party invite from " + ChatColor.BOLD + Parties.invite.get(p).getName() + ChatColor.RED + " expired.");
                        }
                        if (p.isOnline()) {
                            Parties.invite.get(p).sendMessage(ChatColor.RED + "Party invite to " + ChatColor.BOLD + p.getName() + ChatColor.RED + " has expired.");
                        }
                        Parties.invite.remove(p);
                        Parties.invitetime.remove(p);
                    }
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20L, 20L);
    }

    public void onDisable() {
        PracticeServer.log.info("[Parties] has been disabled.");
    }


    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        ArrayList<Player> entirePartyOf = getEntirePartyOf(p);
        if (entirePartyOf == null) {
            return;
        }
        if (isInParty(p)) {
            removePlayer(p);
        }
    }
}