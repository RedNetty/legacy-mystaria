package me.retrorealms.practiceserver.apis.tab;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.VanishCommand;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.mcsg.double0negative.tabapi.TabAPI;

import java.util.*;

/**
 * Created by Giovanni on 23-7-2017.
 */
public class TabMenu {

    public static BukkitTask task = null;

    public static String RankTag(Player p) {
        if(Alignments.get(p) == "&cCHAOTIC"){
            return ChatColor.RED + "";
        }if(Alignments.get(p) == "&eNEUTRAL"){
            return ChatColor.YELLOW + "";
        }
        switch (ModerationMechanics.getRank(p)) {
            case DEFAULT:
                return ChatColor.GRAY + "";
            case SUB:
                return ChatColor.GREEN + "" + ChatColor.BOLD + "";
            case SUB1:
                return ChatColor.GOLD + "" + ChatColor.BOLD + "";
            case SUB2:
                return ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "";
            case SUB3:
                return ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "";
            case SUPPORTER:
                return ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "";
            case YOUTUBER:
                return ChatColor.WHITE + "";
            case QUALITY:
                return ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "";
            case BUILDER:
                return ChatColor.AQUA + "" + ChatColor.BOLD + "";
            case PMOD:
                return ChatColor.WHITE + "" + ChatColor.BOLD + "";
            case GM:
                return ChatColor.AQUA + "" + ChatColor.BOLD + "";
            case DEV:
                return ChatColor.RED + "" + ChatColor.BOLD + "";
            case MANAGER:
                return ChatColor.YELLOW + "" + ChatColor.BOLD + "";
        }
        return ChatColor.GRAY + "";
    }

    public static String getRank(Player p) {
        switch (ModerationMechanics.getRank(p)) {
            case DEFAULT:
                return ChatColor.GRAY + "NONE";
            case SUB:
                return ChatColor.GREEN + "" + ChatColor.BOLD + "SUB";
            case SUB1:
                return ChatColor.GOLD + "" + ChatColor.BOLD + "SUB+";
            case SUB2:
                return ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "S++";
            case SUB3:
                return ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "SUPP";
            case SUPPORTER:
                return ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "SUPP";
            case YOUTUBER:
                return ChatColor.WHITE + "" + ChatColor.BOLD + "YT";
            case QUALITY:
                return ChatColor.AQUA + "" + ChatColor.BOLD + "QA";
            case BUILDER:
                return ChatColor.AQUA + "" + ChatColor.BOLD + "BLDR";
            case PMOD:
                return ChatColor.WHITE + "" + ChatColor.BOLD + "PMOD";
            case GM:
                return ChatColor.AQUA + "" + ChatColor.BOLD + "GM";
            case DEV:
                return ChatColor.RED + "" + ChatColor.BOLD + "DEV";
            case MANAGER:
                return ChatColor.YELLOW + "" + ChatColor.BOLD + "MNGR";
        }
        return ChatColor.GRAY + "NONE";
    }

    public static void clearTab(Player p){
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 20; j++) {
                TabAPI.setTabString(PracticeServer.plugin, p, j, i, " ");
            }
        }
    }

    public static ArrayList<Player> sortPlayers(Collection<? extends Player> playerlist){
        ArrayList<RankEnum> staff = new ArrayList<RankEnum>(
                Arrays.asList(RankEnum.DEV, RankEnum.MANAGER, RankEnum.GM, RankEnum.BUILDER, RankEnum.PMOD, RankEnum.QUALITY));
        ArrayList<RankEnum> ranks = new ArrayList<RankEnum>(
                Arrays.asList(RankEnum.YOUTUBER, RankEnum.SUPPORTER, RankEnum.SUB3, RankEnum.SUB2, RankEnum.SUB1, RankEnum.SUB, RankEnum.DEFAULT));
        ArrayList<Player> finalList = new ArrayList<>();
        for(RankEnum rank : staff){
            for(Player p : playerlist){
                if(ModerationMechanics.getRank(p) == rank){
                    finalList.add(p);
                }
            }
        }
        Server s = Bukkit.getServer();
        for(String p : Alignments.chaotic.keySet()){
            if(s.getPlayer(p) != null && !finalList.contains(s.getPlayer(p))){
                finalList.add(s.getPlayer(p));
            }
        }
        for(String p : Alignments.neutral.keySet()){
            if(s.getPlayer(p) != null && !finalList.contains(s.getPlayer(p))){
                finalList.add(s.getPlayer(p));
            }
        }
        for(RankEnum rank : ranks) {
            for (Player p : playerlist) {
                if (ModerationMechanics.getRank(p) == rank) {
                    if(!finalList.contains(p)) finalList.add(p);
                }
            }
        }
        return finalList;

    }

    public static void bugFixTab(Player p, int x, int y, String message, int playercount){
        TabAPI.setTabString(PracticeServer.plugin, p, x, y, message);
    }

    public static void init2(){

        task = Bukkit.getScheduler().runTaskTimer(PracticeServer.plugin, new Runnable() {
            @Override
            public void run() {
                Collection<? extends Player> playerlist = Bukkit.getOnlinePlayers();
                playerlist = sortPlayers(playerlist);

                int playercount = playerlist.size();

                for(Player p : playerlist) {
                    clearTab(p);


                    //Top
                    for(int y = 0; y < 4; y++){
                        bugFixTab(p, 0, y, ChatColor.GRAY + "------------", playercount);
                    }
                    for(int y = 0; y < 4; y++){
                        int x = 3;
                        if(y == 1) x = 2;
                        bugFixTab(p, x, y, ChatColor.GRAY + "------------", playercount);
                    }
                    bugFixTab(p, 1, 0, ChatColor.DARK_AQUA + "/buy for store", playercount);
                    bugFixTab(p, 1, 3, ChatColor.DARK_AQUA + "/discord", playercount);
                    bugFixTab(p, 2, 0, ChatColor.DARK_AQUA + "Rank: " + getRank(p), playercount);
                    bugFixTab(p, 1, 1, ChatColor.DARK_GREEN + " WELCOME TO", playercount);
                    bugFixTab(p, 2, 2, ChatColor.DARK_GREEN + "AUTISM REALMS", playercount);
                    bugFixTab(p, 2, 3, "by " + ChatColor.RED + "Red", playercount);



                    //Player List
                    int i = 4;
                    for(Player p2 : playerlist){
                        if(VanishCommand.vanished.contains(p2)) continue;
                        if(i < 19){
                            int y = 0;
                            bugFixTab(p, i, y,RankTag(p2) + p2.getName(), playercount);
                            i++;
                        }if(i == 19 && playercount == 16){
                            bugFixTab(p, 19, 0,RankTag(p2) + p2.getName(), playercount);
                        }
                    }
                    if(playercount > 16){
                        bugFixTab(p, 19,0,ChatColor.WHITE + Integer.toString(playercount - 15) + " more....", playercount);
                    }

                    GuildPlayer guildPlayer = GuildPlayers.getInstance().get(p.getUniqueId());
                    //Stats
                    bugFixTab(p, 4,3,ChatColor.DARK_AQUA + "Alignment:", playercount);
                    bugFixTab(p, 5,3, getAlignment(p) + " " + ((Alignments.getAlignTime(p) > 0) ? Integer.toString(Alignments.getAlignTime(p)) + "s" : ""), playercount);
                    bugFixTab(p, 6,3,ChatColor.YELLOW + "DPS: " + Integer.toString(TabUtil.getDPS(p)), playercount);
                    bugFixTab(p, 7,3,ChatColor.YELLOW + "ARMOR: " + Integer.toString(TabUtil.getArmor(p)), playercount);
                    bugFixTab(p, 8,3,ChatColor.YELLOW + "ENERGY: " + Integer.toString(TabUtil.getEnergy(p)), playercount);
                    bugFixTab(p, 9,3,ChatColor.YELLOW + "HP/s: " + Integer.toString(TabUtil.getHPS(p)), playercount);
                    bugFixTab(p, 10,3,ChatColor.RED + "PKs: " +     Integer.toString(guildPlayer.getPlayerKills()), playercount);
                    bugFixTab(p, 11,3,ChatColor.RED + "Deaths: " + Integer.toString(guildPlayer.getDeaths()), playercount);
                    bugFixTab(p, 12,3,ChatColor.GRAY + "Mined: " + Integer.toString(guildPlayer.getOreMined()), playercount);
                    bugFixTab(p, 13,3,ChatColor.YELLOW + "Mob Kills:", playercount);
                    bugFixTab(p, 14,3,ChatColor.GRAY + "T1: " + Integer.toString(guildPlayer.getT1Kills()), playercount);
                    bugFixTab(p, 15,3,ChatColor.GRAY + "T2: " + Integer.toString(guildPlayer.getT2Kills()), playercount);
                    bugFixTab(p, 16,3,ChatColor.GRAY + "T3: " + Integer.toString(guildPlayer.getT3Kills()), playercount);
                    bugFixTab(p, 17,3,ChatColor.GRAY + "T4: " + Integer.toString(guildPlayer.getT4Kills()), playercount);
                    bugFixTab(p, 18,3,ChatColor.GRAY + "T5: " + Integer.toString(guildPlayer.getT5Kills()), playercount);

                    int ping =((CraftPlayer)p).getHandle().ping;
                    bugFixTab(p, 18,2,ChatColor.DARK_AQUA + "Ping: " + Integer.toString(ping), playercount);
                    //bugFixTab(p, 4,3,ChatColor.AQUA + "Alignment:", 1, WrappedGameProfile.fromOfflinePlayer(Bukkit.getServer().getOfflinePlayer(UUID.fromString("53dc6122-7281-48c5-82a6-70208db4b501"))));
                    TabAPI.updatePlayer(p);
                }
            }
        }, 0, 40);
    }
    public static String getAlignment(Player p) {
        if (Alignments.chaotic.containsKey(p.getName())) {
            return ChatColor.RED + "CHAOTIC";
        }
        if (Alignments.neutral.containsKey(p.getName())) {
            return ChatColor.YELLOW + "NEUTRAL";
        } else {
            return ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "LAWFUL";
        }
    }
}
