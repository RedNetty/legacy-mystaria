package me.retrorealms.practiceserver.utils.SQLUtil;

import java.sql.Connection;

public class SQLCreate {
    public static String CreatePlayerStats = "CREATE TABLE IF NOT EXISTS  PlayerData (  " +
            "UUID  VARCHAR(36)," +
            "Username  VARCHAR(17)," +
            "XCoord INT," +
            "YCoord INT," +
            "ZCoord INT," +
            "Yaw INT," +
            "Pitch INT," +
            "Inventory MEDIUMTEXT," +
            "Armor MEDIUMTEXT," +
            "MaxHP INT," +
            "Gems INT," +
            "GuildName  VARCHAR(50)," +
            "Alignment  VARCHAR(10)," +
            "AlignTime  INT," +
            "HorseTier  INT," +
            "T1Kills  INT," +
            "T2Kills  INT," +
            "T3Kills  INT," +
            "T4Kills  INT," +
            "T5Kills  INT," +
            "T6Kills  INT," +
            "Deaths  INT," +
            "PlayerKills  INT," +
            "OreMined  INT," +
            "ChestsOpened  INT," +
            "RespawnData MEDIUMTEXT," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateGuilds = "CREATE TABLE IF NOT EXISTS  Guilds (  " +
            "Owner VARCHAR(36)," +
            "GuildName  VARCHAR(50)," +
            "GuildTag  VARCHAR(10)," +
            "GuildMOTD  VARCHAR(200)," +
            "Officers  VARCHAR(1000)," +
            "Members  VARCHAR(2000)," +
            "PRIMARY KEY ( GuildName ) );";

    public static String CreatePersistentStats = "CREATE TABLE IF NOT EXISTS  PersistentData (  " +
            "UUID VARCHAR(36)," +
            "Username VARCHAR(17)," +
            "PlayerRank VARCHAR(10)," +
            "Buddies VARCHAR(1000)," +
            "PVPToggle  BOOLEAN," +
            "ChaoToggle  BOOLEAN," +
            "FFToggle  BOOLEAN," +
            "DebugToggle  BOOLEAN," +
            "HologramToggle  BOOLEAN," +
            "LVLHPToggle  BOOLEAN," +
            "GlowToggle  BOOLEAN," +
            "PMToggle  BOOLEAN," +
            "TradingToggle  BOOLEAN," +
            "GemsToggle  BOOLEAN," +
            "TrailToggle  BOOLEAN," +
            "DropToggle  BOOLEAN," +
            "KitToggle  BOOLEAN," +
            "Tokens INT," +
            "Mount INT," +
            "BankPages INT," +
            "Pickaxe INT," +
            "Farmer INT," +
            "LastStand INT," +
            "OrbRolls INT," +
            "Luck INT," +
            "Reaper INT," +
            "KitWeapon INT," +
            "KitHelm INT," +
            "KitChest INT," +
            "KitLegs INT," +
            "KitBoots INT," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateBanks = "CREATE TABLE IF NOT EXISTS  Banks (  " +
            "UUID VARCHAR(36)," +
            "Username VARCHAR(17)," +
            "Inventory MEDIUMTEXT," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateBanks2 = "CREATE TABLE IF NOT EXISTS  Banks2 (  " +
            "UUID VARCHAR(36)," +
            "Username VARCHAR(17)," +
            "Inventory MEDIUMTEXT," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateBanks3 = "CREATE TABLE IF NOT EXISTS  Banks3 (  " +
            "UUID VARCHAR(36)," +
            "Username VARCHAR(17)," +
            "Inventory MEDIUMTEXT," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateBanks4 = "CREATE TABLE IF NOT EXISTS  Banks4 (  " +
            "UUID VARCHAR(36)," +
            "Username VARCHAR(17)," +
            "Inventory MEDIUMTEXT," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateBanks5 = "CREATE TABLE IF NOT EXISTS  Banks5 (  " +
            "UUID VARCHAR(36)," +
            "Username VARCHAR(17)," +
            "Inventory MEDIUMTEXT," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateGuildBanks = "CREATE TABLE IF NOT EXISTS  GuildBanks (  " +
            "GuildName VARCHAR(50)," +
            "GuildBank MEDIUMTEXT," +
            "Mutex BOOLEAN," +
            "OccupiedBy VARCHAR(17)," +
            "PRIMARY KEY ( GuildName ) );";

    public static void createTables(Connection con){
        try{
            con.createStatement().execute(CreatePlayerStats);
            con.createStatement().execute(CreatePersistentStats);
            con.createStatement().execute(CreateGuilds);
            con.createStatement().execute(CreateBanks);
            con.createStatement().execute(CreateBanks2);
            con.createStatement().execute(CreateBanks3);
            con.createStatement().execute(CreateBanks4);
            con.createStatement().execute(CreateBanks5);
            con.createStatement().execute(CreateGuildBanks);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
