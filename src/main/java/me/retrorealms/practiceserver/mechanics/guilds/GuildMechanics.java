package me.retrorealms.practiceserver.mechanics.guilds;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.guilds.*;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildBank;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.listeners.ChatListener;
import me.retrorealms.practiceserver.mechanics.guilds.listeners.JoinListener;
import me.retrorealms.practiceserver.mechanics.guilds.listeners.QuitListener;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.io.File;

public final class GuildMechanics {
    private static GuildMechanics instance;

    private static GuildBank guildBank;

    public static GuildMechanics getInstance() {
        return instance == null ? instance = new GuildMechanics() : instance;
    }

    public GuildMechanics() {
        instance = this;
    }

    public void onEnable() {
        guildBank = new GuildBank();
        guildBank.onEnable();
        this.registerListeners();
        if(!PracticeServer.DATABASE) {
            if (!this.getDataFolder().exists())
                this.getDataFolder().mkdirs();
        }
        GuildManager.getInstance();
        this.registerCommands();
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(PracticeServer.plugin, () -> GuildPlayers.getInstance().getPendingInvitePlayers().forEach(guildPlayer -> {
            if (!GuildManager.getInstance().isGuild(guildPlayer.getGuildInviteName())) {
                guildPlayer.setGuildInviteTime(0L);
                guildPlayer.setGuildInviteName(null);
                Player player = Bukkit.getPlayer(guildPlayer.getUuid());
                if (player != null && (player.isOnline())) {
                    player.sendMessage(ChatColor.RED + "Guild invite has " + ChatColor.RED + " expired.");
                }
            } else {
                guildPlayer.setGuildInviteTime(0L);
                guildPlayer.setGuildInviteName(null);
                Player player = Bukkit.getPlayer(guildPlayer.getUuid());
                if (player != null && (player.isOnline())) {
                    player.sendMessage(ChatColor.RED + "Guild invite has " + ChatColor.RED + " expired.");
                }
            }
        }), 40L, 40L);
    }

    public File getDataFolder() {
        return new File(PracticeServer.getInstance().getDataFolder() + "/guilds/");
    }

    private void registerCommands() {
        PracticeServer.getInstance().getCommand("guildcreate").setExecutor(new GuildCreateCommand());
        PracticeServer.getInstance().getCommand("guildshow").setExecutor(new GuildShowCommand());
        PracticeServer.getInstance().getCommand("guildquit").setExecutor(new GuildQuitCommand());
        PracticeServer.getInstance().getCommand("guildinvite").setExecutor(new GuildInviteCommand());
        PracticeServer.getInstance().getCommand("guilddecline").setExecutor(new GuildDeclineCommand());
        PracticeServer.getInstance().getCommand("guildaccept").setExecutor(new GuildAcceptCommand());
        PracticeServer.getInstance().getCommand("guild").setExecutor(new GuildCommand());
        PracticeServer.getInstance().getCommand("guildkick").setExecutor(new GuildKickCommand());
        PracticeServer.getInstance().getCommand("guildpromote").setExecutor(new GuildPromoteCommand());
        PracticeServer.getInstance().getCommand("guilddemote").setExecutor(new GuildDemoteCommand());
    }

    private void registerListeners() {
        PluginManager pluginManager = PracticeServer.getInstance().getServer().getPluginManager();
        pluginManager.registerEvents(new JoinListener(), PracticeServer.getInstance());
        pluginManager.registerEvents(new GuildBank(), PracticeServer.getInstance());
        pluginManager.registerEvents(new ChatListener(), PracticeServer.getInstance());
        pluginManager.registerEvents(new QuitListener(), PracticeServer.getInstance());
    }


    public void onDisable() {
        guildBank.onDisable();
    }

    public boolean isInSameGuild(Player shooter, Player entity) {
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(shooter.getUniqueId());
        GuildPlayer guildPlayer1 = GuildPlayers.getInstance().get(entity.getUniqueId());
        return ((guildPlayer.getGuildName() != null) && (guildPlayer1.getGuildName() != null)) && ((guildPlayer.getGuildName() != "") && (guildPlayer1.getGuildName() != "")) && (guildPlayer.getGuildName().equals(guildPlayer1.getGuildName()));
    }

    public void wipe() {
        GuildManager.getInstance().wipe();
    }
}
