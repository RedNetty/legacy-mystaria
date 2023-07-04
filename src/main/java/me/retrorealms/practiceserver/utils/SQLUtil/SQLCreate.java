package me.retrorealms.practiceserver.utils.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SQLCreate {
    private static final String[] CREATE_TABLE_QUERIES = {
            "CREATE TABLE IF NOT EXISTS PlayerData ("
                    + "UUID VARCHAR(36),"
                    + "Username VARCHAR(17),"
                    + "XCoord INT,"
                    + "YCoord INT,"
                    + "ZCoord INT,"
                    + "Yaw INT,"
                    + "Pitch INT,"
                    + "Inventory TEXT,"
                    + "Armor TEXT,"
                    + "MaxHP INT,"
                    + "Gems INT,"
                    + "GuildName VARCHAR(50),"
                    + "Alignment VARCHAR(10),"
                    + "AlignTime INT,"
                    + "HorseTier INT,"
                    + "T1Kills INT,"
                    + "T2Kills INT,"
                    + "T3Kills INT,"
                    + "T4Kills INT,"
                    + "T5Kills INT,"
                    + "T6Kills INT,"
                    + "Deaths INT,"
                    + "PlayerKills INT,"
                    + "OreMined INT,"
                    + "ChestsOpened INT,"
                    + "RespawnData TEXT,"
                    + "PRIMARY KEY (UUID));",

            "CREATE TABLE IF NOT EXISTS Guilds ("
                    + "Owner VARCHAR(36),"
                    + "GuildName VARCHAR(50),"
                    + "GuildTag VARCHAR(10),"
                    + "GuildMOTD VARCHAR(200),"
                    + "Officers VARCHAR(1000),"
                    + "Members VARCHAR(2000),"
                    + "PRIMARY KEY (GuildName));",

            "CREATE TABLE IF NOT EXISTS PersistentData ("
                    + "UUID VARCHAR(36),"
                    + "Username VARCHAR(17),"
                    + "PlayerRank VARCHAR(10),"
                    + "Buddies VARCHAR(1000),"
                    + "PVPToggle BOOLEAN,"
                    + "ChaoToggle BOOLEAN,"
                    + "FFToggle BOOLEAN,"
                    + "DebugToggle BOOLEAN,"
                    + "HologramToggle BOOLEAN,"
                    + "LVLHPToggle BOOLEAN,"
                    + "GlowToggle BOOLEAN,"
                    + "PMToggle BOOLEAN,"
                    + "TradingToggle BOOLEAN,"
                    + "GemsToggle BOOLEAN,"
                    + "TrailToggle BOOLEAN,"
                    + "DropToggle BOOLEAN,"
                    + "KitToggle BOOLEAN,"
                    + "Tokens INT,"
                    + "Mount INT,"
                    + "BankPages INT,"
                    + "Pickaxe INT,"
                    + "Farmer INT,"
                    + "LastStand INT,"
                    + "OrbRolls INT,"
                    + "Luck INT,"
                    + "Reaper INT,"
                    + "KitWeapon INT,"
                    + "KitHelm INT,"
                    + "KitChest INT,"
                    + "KitLegs INT,"
                    + "KitBoots INT,"
                    + "PRIMARY KEY (UUID));",

            "CREATE TABLE IF NOT EXISTS Banks ("
                    + "UUID VARCHAR(36),"
                    + "Username VARCHAR(17),"
                    + "Inventory TEXT,"
                    + "PRIMARY KEY (UUID));",

            "CREATE TABLE IF NOT EXISTS Banks2 ("
                    + "UUID VARCHAR(36),"
                    + "Username VARCHAR(17),"
                    + "Inventory TEXT,"
                    + "PRIMARY KEY (UUID));",

            "CREATE TABLE IF NOT EXISTS Banks3 ("
                    + "UUID VARCHAR(36),"
                    + "Username VARCHAR(17),"
                    + "Inventory TEXT,"
                    + "PRIMARY KEY (UUID));",

            "CREATE TABLE IF NOT EXISTS Banks4 ("
                    + "UUID VARCHAR(36),"
                    + "Username VARCHAR(17),"
                    + "Inventory TEXT,"
                    + "PRIMARY KEY (UUID));",

            "CREATE TABLE IF NOT EXISTS Banks5 ("
                    + "UUID VARCHAR(36),"
                    + "Username VARCHAR(17),"
                    + "Inventory TEXT,"
                    + "PRIMARY KEY (UUID));",

            "CREATE TABLE IF NOT EXISTS GuildBanks ("
                    + "GuildName VARCHAR(50),"
                    + "GuildBank TEXT,"
                    + "Mutex BOOLEAN,"
                    + "OccupiedBy VARCHAR(17),"
                    + "PRIMARY KEY (GuildName));"
    };

    public static void createTables(Connection con) {
        try {
            for (String query : CREATE_TABLE_QUERIES) {
                try (PreparedStatement stmt = con.prepareStatement(query)) {
                    stmt.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
