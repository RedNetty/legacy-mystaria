package me.retrorealms.practiceserver.mechanics.guilds.player;

import me.retrorealms.practiceserver.mechanics.guilds.guild.Guild;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;

import java.util.UUID;

public class GuildPlayer {
    private UUID uuid;
    private String username;
    private String guildName;
    private int playerKills;
    private int t1Kills;
    private int t2Kills;
    private int t3Kills;
    private int t4Kills;
    private int t5Kills;
    private int t6Kills;
    private int lootChestsOpen;
    private int oreMined;
    private int deaths;
    private int fishCaught;
    private String guildInviteName;
    private long guildInviteTime;


    public String getGuildInviteName() {
        return guildInviteName;
    }

    public void setGuildInviteName(String guildInviteName) {
        this.guildInviteName = guildInviteName;
    }

    public long getGuildInviteTime() {
        return guildInviteTime;
    }

    public void setGuildInviteTime(long guildInviteTime) {
        this.guildInviteTime = guildInviteTime;
    }

    public GuildPlayer(UUID uuid, String username, String guildName) {
        this(uuid, username, guildName, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public GuildPlayer(UUID uuid, String username, String guildName, int playerKills, int t1Kills, int t2Kills, int t3Kills, int t4Kills, int t5Kills, int t6Kills, int lootChestsOpen, int oreMined, int deaths, int fishCaught) {
        this.uuid = uuid;
        this.username = username;
        this.guildName = guildName;
        this.playerKills = playerKills;
        this.t1Kills = t1Kills;
        this.t2Kills = t2Kills;
        this.t3Kills = t3Kills;
        this.t4Kills = t4Kills;
        this.t5Kills = t5Kills;
        this.t6Kills = t6Kills;
        this.lootChestsOpen = lootChestsOpen;
        this.oreMined = oreMined;
        this.deaths = deaths;
        this.fishCaught = fishCaught;
        if (getGuildName() != null && (!GuildManager.getInstance().isGuild(getGuildName()))) {
            this.guildName = "";
        }
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(int playerKills) { this.playerKills = playerKills; }

    public int getT1Kills() {
        return t1Kills;
    }

    public void setT1Kills(int t1Kills) { this.t1Kills = t1Kills; }

    public int getT2Kills() {
        return t2Kills;
    }

    public void setT2Kills(int t2Kills) {
        this.t2Kills = t2Kills;
    }

    public int getT3Kills() {
        return t3Kills;
    }

    public void setT3Kills(int t3Kills) {
        this.t3Kills = t3Kills;
    }

    public int getT4Kills() {
        return t4Kills;
    }

    public void setT4Kills(int t4Kills) {
        this.t4Kills = t4Kills;
    }

    public int getT5Kills() { return t5Kills; }

    public void setT5Kills(int t5Kills) {this.t5Kills = t5Kills;}

    public int getT6Kills() { return t6Kills; }

    public void setT6Kills(int t6Kills) {
        this.t6Kills = t6Kills;
    }

    public int getLootChestsOpen() {
        return lootChestsOpen;
    }

    public void setLootChestsOpen(int lootChestsOpen) {
        this.lootChestsOpen = lootChestsOpen;
    }

    public int getOreMined() {
        return oreMined;
    }

    public void setOreMined(int oreMined) {
        this.oreMined = oreMined;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getFishCaught() {
        return fishCaught;
    }

    public void setFishCaught(int fishCaught) {
        this.fishCaught = fishCaught;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getGuildName() {
        if (guildName == null || guildName.equalsIgnoreCase("")) {
            return null;
        }
        return guildName;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGuildName(String guildName) {
        if (guildName == null || (guildName.equalsIgnoreCase("null"))) {
            this.guildName = "";
            return;
        }
        if (getGuildName() != null && (!GuildManager.getInstance().isGuild(getGuildName()))) {
            this.guildName = "";
            return;
        }
        this.guildName = guildName;
    }

    public boolean isInGuild() {
        if (getGuildName() != null) {
            Guild guild = GuildManager.getInstance().get(getGuildName());
            return guild != null;
        }
        return getGuildName() != null;
    }

    public boolean hasPendingInvite() {
        return guildInviteName != null;
    }
}
