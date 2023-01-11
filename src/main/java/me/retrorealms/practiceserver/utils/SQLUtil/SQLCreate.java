package me.retrorealms.practiceserver.utils.SQLUtil;

import java.sql.Connection;

public class SQLCreate {
    public static String CreatePlayerStats = "CREATE TABLE IF NOT EXISTS  PlayerData (  " +
            "UUID CHAR(36)," +
            "Username CHAR(17)," +
            "XCoord INT," +
            "YCoord INT," +
            "ZCoord INT," +
            "Yaw INT," +
            "Pitch INT," +
            "Inventory VARCHAR(25000)," +
            "Armor VARCHAR(20000)," +
            "MaxHP INT," +
            "Gems INT," +
            "GuildName  CHAR(50)," +
            "Alignment  CHAR(10)," +
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
            "RespawnData VARCHAR(15000)," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateGuilds = "CREATE TABLE IF NOT EXISTS  Guilds (  " +
            "Owner CHAR(36)," +
            "GuildName  CHAR(50)," +
            "GuildTag  CHAR(10)," +
            "GuildMOTD  CHAR(200)," +
            "Officers  VARCHAR(1000)," +
            "Members  VARCHAR(2000)," +
            "PRIMARY KEY ( GuildName ) );";

    public static String CreatePersistentStats = "CREATE TABLE IF NOT EXISTS  PersistentData (  " +
            "UUID CHAR(36)," +
            "Username CHAR(17)," +
            "Rank CHAR(10)," +
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
            "UUID CHAR(36)," +
            "Username CHAR(17)," +
            "Inventory VARCHAR(50000)," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateBanks2 = "CREATE TABLE IF NOT EXISTS  Banks2 (  " +
            "UUID CHAR(36)," +
            "Username CHAR(17)," +
            "Inventory VARCHAR(50000)," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateBanks3 = "CREATE TABLE IF NOT EXISTS  Banks3 (  " +
            "UUID CHAR(36)," +
            "Username CHAR(17)," +
            "Inventory VARCHAR(50000)," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateBanks4 = "CREATE TABLE IF NOT EXISTS  Banks4 (  " +
            "UUID CHAR(36)," +
            "Username CHAR(17)," +
            "Inventory VARCHAR(50000)," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateBanks5 = "CREATE TABLE IF NOT EXISTS  Banks5 (  " +
            "UUID CHAR(36)," +
            "Username CHAR(17)," +
            "Inventory VARCHAR(50000)," +
            "PRIMARY KEY ( UUID ) );";

    public static String CreateGuildBanks = "CREATE TABLE IF NOT EXISTS  GuildBanks (  " +
            "GuildName CHAR(50)," +
            "GuildBank VARCHAR(50000)," +
            "Mutex BOOLEAN," +
            "OccupiedBy CHAR(17)," +
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
