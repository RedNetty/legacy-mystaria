package me.retrorealms.practiceserver.apis.tab;

import com.keenant.tabbed.Tabbed;
import com.keenant.tabbed.item.PlayerTabItem;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.VanishCommand;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.drops.Mobdrops;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBoss;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBossHandler;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Giovanni on 23-7-2017.
 */
public class TabMenu {

    public static BukkitTask task = null;
    private static Tabbed tabbed;

    public static String RankTag(Player p) {
        if (Alignments.get(p) == "&cCHAOTIC") {
            return String.valueOf(ChatColor.RED);
        }
        if (Alignments.get(p) == "&eNEUTRAL") {
            return String.valueOf(ChatColor.YELLOW);
        }
        switch (ModerationMechanics.getRank(p)) {
            case DEFAULT:
                return String.valueOf(ChatColor.GRAY);
            case SUB:
                return ChatColor.GREEN + String.valueOf(ChatColor.BOLD);
            case SUB1:
                return ChatColor.GOLD + String.valueOf(ChatColor.BOLD);
            case SUB2:
                return ChatColor.DARK_AQUA + String.valueOf(ChatColor.BOLD);
            case SUB3:
                return ChatColor.DARK_AQUA + String.valueOf(ChatColor.BOLD);
            case SUPPORTER:
                return ChatColor.LIGHT_PURPLE + String.valueOf(ChatColor.BOLD);
            case YOUTUBER:
                return String.valueOf(ChatColor.WHITE);
            case QUALITY:
                return ChatColor.DARK_PURPLE + String.valueOf(ChatColor.BOLD);
            case BUILDER:
                return ChatColor.AQUA + String.valueOf(ChatColor.BOLD);
            case PMOD:
                return ChatColor.WHITE + String.valueOf(ChatColor.BOLD);
            case GM:
                return ChatColor.AQUA + String.valueOf(ChatColor.BOLD);
            case DEV:
                return ChatColor.RED + String.valueOf(ChatColor.BOLD);
            case MANAGER:
                return ChatColor.YELLOW + String.valueOf(ChatColor.BOLD);
        }
        return String.valueOf(ChatColor.GRAY);
    }

    public static String getRank(Player p) {
        switch (ModerationMechanics.getRank(p)) {
            case DEFAULT:
                return ChatColor.GRAY + "NONE";
            case SUB:
                return ChatColor.GREEN + String.valueOf(ChatColor.BOLD) + "SUB";
            case SUB1:
                return ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "SUB+";
            case SUB2:
                return ChatColor.DARK_AQUA + String.valueOf(ChatColor.BOLD) + "S++";
            case SUB3:
                return ChatColor.DARK_AQUA + String.valueOf(ChatColor.BOLD) + "SUPP";
            case SUPPORTER:
                return ChatColor.LIGHT_PURPLE + String.valueOf(ChatColor.BOLD) + "SUPP";
            case YOUTUBER:
                return ChatColor.WHITE + String.valueOf(ChatColor.BOLD) + "YT";
            case QUALITY:
                return ChatColor.AQUA + String.valueOf(ChatColor.BOLD) + "QA";
            case BUILDER:
                return ChatColor.AQUA + String.valueOf(ChatColor.BOLD) + "BLDR";
            case PMOD:
                return ChatColor.WHITE + String.valueOf(ChatColor.BOLD) + "PMOD";
            case GM:
                return ChatColor.AQUA + String.valueOf(ChatColor.BOLD) + "GM";
            case DEV:
                return ChatColor.RED + String.valueOf(ChatColor.BOLD) + "DEV";
            case MANAGER:
                return ChatColor.YELLOW + String.valueOf(ChatColor.BOLD) + "MNGR";
        }
        return ChatColor.GRAY + "NONE";
    }


    public static ArrayList<Player> sortPlayers(Collection<? extends Player> playerlist) {
        ArrayList<RankEnum> staff = new ArrayList<RankEnum>(
                Arrays.asList(RankEnum.DEV, RankEnum.MANAGER, RankEnum.GM, RankEnum.BUILDER, RankEnum.PMOD, RankEnum.QUALITY));
        ArrayList<RankEnum> ranks = new ArrayList<RankEnum>(
                Arrays.asList(RankEnum.YOUTUBER, RankEnum.SUPPORTER, RankEnum.SUB3, RankEnum.SUB2, RankEnum.SUB1, RankEnum.SUB, RankEnum.DEFAULT));
        ArrayList<Player> finalList = new ArrayList<>();
        for (RankEnum rank : staff) {
            for (Player p : playerlist) {
                if (ModerationMechanics.getRank(p) == rank) {
                    if (!finalList.contains(p)) finalList.add(p);
                }
            }
        }
        Server s = Bukkit.getServer();
        for (String p : Alignments.chaotic.keySet()) {
            if (s.getPlayer(p) != null && !finalList.contains(s.getPlayer(p))) {
                finalList.add(s.getPlayer(p));
            }
        }
        for (String p : Alignments.neutral.keySet()) {
            if (s.getPlayer(p) != null && !finalList.contains(s.getPlayer(p))) {
                finalList.add(s.getPlayer(p));
            }
        }
        for (RankEnum rank : ranks) {
            for (Player p : playerlist) {
                if (ModerationMechanics.getRank(p) == rank) {
                    if (!finalList.contains(p)) finalList.add(p);
                }
            }
        }
        return finalList;

    }

    public static void bugFixTab(Player p, int x, int y, String message, boolean player) {
        try {
            TableTabList tabList = getTabList(p);
            if (player && Bukkit.getPlayer(ChatColor.stripColor(message)) != null) {
                tabList.set(y, x, new PlayerTabItem(Bukkit.getPlayer(ChatColor.stripColor(message)), new PlayerTabItem.PlayerProvider<String>() {
                    @Override
                    public String get(Player player) {
                        return " " + message;
                    }
                }));
            } else {
                tabList.set(y, x, new TextTabItem(message));
            }
        } catch (NoClassDefFoundError e) {

        }
    }


    public static TableTabList getTabList(Player player) {
        TableTabList tabList;
        if (tabbed.getTabList(player) != null) {
            tabList = (TableTabList) tabbed.getTabList(player);
        } else {
            tabList = tabbed.newTableTabList(player);
        }
        return tabList;
    }

    public static void init2() {

        tabbed = new Tabbed(PracticeServer.getInstance());
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Collection<? extends Player> playerlist = Bukkit.getOnlinePlayers();
                    playerlist = sortPlayers(playerlist);
                    int playercount = playerlist.size();
                    String boss = WorldBossHandler.getActiveBoss() == null ? "" : ChatColor.YELLOW + WorldBossHandler.getActiveBoss().bossEnum.getDisplayName();
                    String location = WorldBossHandler.getBossSpawnLocation() == null ? "" : ChatColor.GRAY + WorldBossHandler.getBossSpawnLocation().toString();

                    int bossKillsRequired = WorldBossHandler.getTotalT5Kills();
                    WorldBoss worldBoss = WorldBossHandler.getActiveBoss();
                    for (Player p : playerlist) {
                        TableTabList tabList = getTabList(p);
                        tabList.setBatchEnabled(true);

                        tabList.setHeader(ChatColor.YELLOW + "\nMystaria MMORPG\n ");
                        tabList.setFooter(ChatColor.GRAY + "/discord | Players Online: " + ChatColor.YELLOW + Bukkit.getOnlinePlayers().size());
                        //Top
                        bugFixTab(p, 0, 0, ChatColor.DARK_AQUA + "Players:", false);
                        //Player List
                        for (int i = 2; i <= 19; i++) {
                            // Clear the existing tab item at index i
                            bugFixTab(p, i, 0, "", false);
                            i++;
                        }
                        int i = 2;
                        // Iterate through the playerlist
                        for (Player p2 : playerlist) {
                            if (VanishCommand.vanished.contains(p2)) continue;
                            if (i < 19) {
                                int y = 0;
                                bugFixTab(p, i, y, RankTag(p2) + p2.getName(), true);
                                i++;
                            }
                            if (i == 19 && playercount == 16) {
                                bugFixTab(p, 19, 0, RankTag(p2) + p2.getName(), true);
                            }
                        }

// Add the remaining players if playercount is greater than 16
                        if (playercount > 16) {
                            bugFixTab(p, 19, 0, ChatColor.WHITE + Integer.toString(playercount - 15) + " more....", false);
                        }

                        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(p.getUniqueId());
                        //Stats
                        //3rd Column
                        bugFixTab(p, 0, 1, "", false);
                        bugFixTab(p, 0, 2, ChatColor.DARK_AQUA + "Drop Rates:", false);

                        bugFixTab(p, 2, 2, ChatColor.GRAY + "T1: " + Mobdrops.getT1RATES() + "%", false);
                        bugFixTab(p, 3, 2, ChatColor.GRAY + "T2: " + Mobdrops.getT2RATES() + "%", false);
                        bugFixTab(p, 4, 2, ChatColor.GRAY + "T3: " + Mobdrops.getT3RATES() + "%", false);
                        bugFixTab(p, 5, 2, ChatColor.GRAY + "T4: " + Mobdrops.getT4RATES() + "%", false);
                        bugFixTab(p, 6, 2, ChatColor.GRAY + "T5: " + Mobdrops.getT5RATES() + "%", false);


                        // Fourth Column
                        bugFixTab(p, 0, 3, ChatColor.DARK_AQUA + "Alignment:", false);
                        bugFixTab(p, 2, 3, getAlignment(p) + " " + ((Alignments.getAlignTime(p) > 0) ? Alignments.getAlignTime(p) + "s" : ""), false);
                        bugFixTab(p, 4, 3, String.valueOf(ChatColor.DARK_AQUA), false);
                        bugFixTab(p, 5, 3, ChatColor.DARK_AQUA + "My Stats: ", false);
                        bugFixTab(p, 7, 3, ChatColor.GRAY + "DPS: " + TabUtil.getDPS(p), false);
                        bugFixTab(p, 8, 3, ChatColor.GRAY + "ARMOR: " + TabUtil.getArmor(p), false);
                        bugFixTab(p, 9, 3, ChatColor.GRAY + "ENERGY: " + TabUtil.getEnergy(p), false);
                        bugFixTab(p, 10, 3, ChatColor.GRAY + "HP/s: " + TabUtil.getHPS(p), false);
                        bugFixTab(p, 11, 3, ChatColor.GRAY + "PKs: " + guildPlayer.getPlayerKills(), false);
                        bugFixTab(p, 12, 3, ChatColor.GRAY + "Deaths: " + guildPlayer.getDeaths(), false);
                        bugFixTab(p, 13, 3, ChatColor.GRAY + "Mined: " + guildPlayer.getOreMined(), false);
                        bugFixTab(p, 14, 3, ChatColor.GRAY + "T1 Kills: " + guildPlayer.getT1Kills(), false);
                        bugFixTab(p, 15, 3, ChatColor.GRAY + "T2 Kills: " + guildPlayer.getT2Kills(), false);
                        bugFixTab(p, 16, 3, ChatColor.GRAY + "T3 Kills: " + guildPlayer.getT3Kills(), false);
                        bugFixTab(p, 17, 3, ChatColor.GRAY + "T4 Kills: " + guildPlayer.getT4Kills(), false);
                        bugFixTab(p, 18, 3, ChatColor.GRAY + "T5 Kills: " + guildPlayer.getT5Kills(), false);

                        int ping = ((CraftPlayer) p).getHandle().ping;
                        bugFixTab(p, 17, 1, ChatColor.DARK_AQUA + "World Boss:", false);
                        if (worldBoss != null) {
                            bugFixTab(p, 18, 1, boss, false);
                            bugFixTab(p, 19, 1, location, false);
                        } else {
                            bugFixTab(p, 18, 1, ChatColor.GRAY + "T5 mobs left", false);
                            bugFixTab(p, 19, 1, ChatColor.GRAY + String.valueOf(450 - bossKillsRequired), false);
                        }
                        bugFixTab(p, 17, 2, ChatColor.DARK_AQUA + "Ping: " + ping, false);
                        //bugFixTab(p, 4,3,ChatColor.AQUA + "Alignment:", 1, WrappedGameProfile.fromOfflinePlayer(Bukkit.getServer().getOfflinePlayer(UUID.fromString("53dc6122-7281-48c5-82a6-70208db4b501"))));
                        tabList.batchUpdate();
                        tabList.setBatchEnabled(false);
                        tabList.batchReset();
                    }
                } catch (NullPointerException e) {

                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0, 45);
    }

    public static String getAlignment(Player p) {
        if (Alignments.chaotic.containsKey(p.getName())) {
            return ChatColor.RED + "CHAOTIC";
        }
        if (Alignments.neutral.containsKey(p.getName())) {
            return ChatColor.YELLOW + "NEUTRAL";
        } else {
            return ChatColor.GREEN.toString() + ChatColor.BOLD + "LAWFUL";
        }
    }
}
