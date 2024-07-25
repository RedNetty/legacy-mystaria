package me.retrorealms.practiceserver.utils.SQLUtil;

import me.retrorealms.practiceserver.PracticeServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
                    + "CurrentQuest VARCHAR(100),"
                    + "DailyQuestsCompleted INT DEFAULT 0,"
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
        for (String sql : CREATE_TABLE_QUERIES) {
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.execute();
                PracticeServer.log.info("[RetroDB] Successfully created or verified table: " + sql.split(" ")[5]);
            } catch (SQLException e) {
                PracticeServer.log.severe("[RetroDB] Error creating table: " + e.getMessage());
                PracticeServer.log.severe("[RetroDB] SQL statement: " + sql);
                e.printStackTrace();
            }
        }
    }

    public static void addMissingColumns(Connection con) {
        String[] newColumns = {
                "CurrentQuest VARCHAR(100)",
                "DailyQuestsCompleted INT DEFAULT 0"
        };
        String table = "PersistentData";

        for (String column : newColumns) {
            String columnName = column.split(" ")[0];
            String sql = "ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS " + column;
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.execute();
                PracticeServer.log.info("[RetroDB] Successfully added or verified column: " + columnName);
            } catch (SQLException e) {
                PracticeServer.log.severe("[RetroDB] Error adding column: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
