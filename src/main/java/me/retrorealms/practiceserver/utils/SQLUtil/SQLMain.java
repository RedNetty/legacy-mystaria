package me.retrorealms.practiceserver.utils.SQLUtil;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.dungeon.task.AsyncTask;
import me.retrorealms.practiceserver.mechanics.guilds.guild.Guild;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildBank;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.guild.Role;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.Banks;
import me.retrorealms.practiceserver.mechanics.money.Economy.Economy;
import me.retrorealms.practiceserver.mechanics.player.*;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import me.retrorealms.practiceserver.mechanics.world.MinigameState;
import me.retrorealms.practiceserver.mechanics.world.races.RaceMinigame;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

public class SQLMain implements Listener {
    private static final RaceMinigame raceMinigame = PracticeServer.getRaceMinigame();
    public static Connection con;

    public static void updateGuild(UUID uuid, String guildName) {
        if (raceMinigame.getGameState() != MinigameState.NONE) return;
        try (PreparedStatement pstmt = con.prepareStatement("UPDATE PlayerData SET GuildName = ? WHERE UUID = ?")) {
            pstmt.setString(1, guildName);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateRespawnData(Player p, List<ItemStack> items) {
        if (raceMinigame.getGameState() == MinigameState.SHRINK) return;
        if (PracticeServer.DATABASE) {
            updatePlayerStats(p);
            int i = 0;
            ItemStack[] itemArray = new ItemStack[items.size() + 1];
            for (ItemStack item : items) {
                itemArray[i] = item;
                i++;
            }
            try {
                String finalItems = BukkitSerialization.itemStackArrayToBase64(items.toArray(new ItemStack[0]));
                System.out.println(finalItems);
                String query = "UPDATE PlayerData SET RespawnData = ? WHERE UUID = ?";
                PreparedStatement pstmt = con.prepareStatement(query);
                pstmt.setString(1, finalItems);
                pstmt.setString(2, p.getUniqueId().toString());
                if (finalItems != null && finalItems.length() > 10) pstmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadRespawnData(Player player) {
        if (raceMinigame.getGameState() == MinigameState.SHRINK) return;
        if (!PracticeServer.DATABASE) {
            return;
        }
        List<ItemStack> itemList = getPlayerData("PlayerData", "RespawnData", player);
        if (itemList == null) return;

        player.getInventory().clear();
        itemList.removeAll(Collections.singleton(null));
        for (ItemStack itemStack : itemList) {
            player.getInventory().addItem(itemStack);
        }
    }

    private static void savePlayerDataOnce(Player player) {
        UUID uuid = player.getUniqueId();
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO PlayerData (UUID, Username) VALUES (?, ?)")) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, player.getName());
            pstmt.executeUpdate();
            PracticeServer.log.info("[RetroDB] Saved Player Data for " + player.getName() + " (Initial Save)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean playerDataExists(UUID uuid) {
        try (PreparedStatement pstmt = con.prepareStatement("SELECT COUNT(*) AS count FROM PlayerData WHERE UUID = ?")) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updatePlayerStats(Player player) {
        if (raceMinigame.getGameState() != MinigameState.NONE) {
            if (!playerDataExists(player.getUniqueId())) {
                savePlayerDataOnce(player);
            }
            return false;
        }
        if (!PracticeServer.DATABASE) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        String[] pinv = BukkitSerialization.playerInventoryToBase64(player.getInventory());
        GuildPlayers gp = GuildPlayers.getInstance();
        GuildPlayer guildPlayer = gp.get(uuid);

        if (guildPlayer == null) {
            Bukkit.getLogger().warning("GuildPlayer is null for " + player.getName() + ". Skipping updatePlayerStats.");
            return false;
        }

        String query = "INSERT INTO PlayerData (UUID, Username, XCoord, YCoord, ZCoord, Yaw, Pitch, " +
                "Inventory, Armor, MaxHP, Gems, GuildName, Alignment, AlignTime, HorseTier, T1Kills, T2Kills, T3Kills, T4Kills, T5Kills, " +
                "T6Kills, Deaths, PlayerKills, OreMined, ChestsOpened, RespawnData) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (UUID) DO UPDATE SET " +
                "Username = EXCLUDED.Username, " +
                "XCoord = EXCLUDED.XCoord, " +
                "YCoord = EXCLUDED.YCoord, " +
                "ZCoord = EXCLUDED.ZCoord, " +
                "Yaw = EXCLUDED.Yaw, " +
                "Pitch = EXCLUDED.Pitch, " +
                "Inventory = EXCLUDED.Inventory, " +
                "Armor = EXCLUDED.Armor, " +
                "MaxHP = EXCLUDED.MaxHP, " +
                "Gems = EXCLUDED.Gems, " +
                "GuildName = EXCLUDED.GuildName, " +
                "Alignment = EXCLUDED.Alignment, " +
                "AlignTime = EXCLUDED.AlignTime, " +
                "HorseTier = EXCLUDED.HorseTier, " +
                "T1Kills = EXCLUDED.T1Kills, " +
                "T2Kills = EXCLUDED.T2Kills, " +
                "T3Kills = EXCLUDED.T3Kills, " +
                "T4Kills = EXCLUDED.T4Kills, " +
                "T5Kills = EXCLUDED.T5Kills, " +
                "T6Kills = EXCLUDED.T6Kills, " +
                "Deaths = EXCLUDED.Deaths, " +
                "PlayerKills = EXCLUDED.PlayerKills, " +
                "OreMined = EXCLUDED.OreMined, " +
                "ChestsOpened = EXCLUDED.ChestsOpened, " +
                "RespawnData = EXCLUDED.RespawnData";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            Location loc = player.getLocation();
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, player.getName());
            pstmt.setDouble(3, loc.getX());
            pstmt.setDouble(4, loc.getY());
            pstmt.setDouble(5, loc.getZ());
            pstmt.setFloat(6, loc.getYaw());
            pstmt.setFloat(7, loc.getPitch());
            pstmt.setString(8, pinv[0]);
            pstmt.setString(9, pinv[1]);
            pstmt.setDouble(10, player.getMaxHealth());
            pstmt.setDouble(11, Economy.getBalance(uuid));
            pstmt.setString(12, guildPlayer.getGuildName());
            pstmt.setString(13, Alignments.get(player));
            pstmt.setInt(14, Alignments.getAlignTime(player));
            pstmt.setInt(15, Horses.horseTier.getOrDefault(player, 0));
            pstmt.setInt(16, guildPlayer.getT1Kills());
            pstmt.setInt(17, guildPlayer.getT2Kills());
            pstmt.setInt(18, guildPlayer.getT3Kills());
            pstmt.setInt(19, guildPlayer.getT4Kills());
            pstmt.setInt(20, guildPlayer.getT5Kills());
            pstmt.setInt(21, guildPlayer.getT6Kills());
            pstmt.setInt(22, guildPlayer.getDeaths());
            pstmt.setInt(23, guildPlayer.getPlayerKills());
            pstmt.setInt(24, guildPlayer.getOreMined());
            pstmt.setInt(25, guildPlayer.getLootChestsOpen());

            if (!player.isDead()) {
                pstmt.setString(26, "");
            } else {
                try (PreparedStatement selectStmt = conn.prepareStatement("SELECT RespawnData FROM PlayerData WHERE UUID = ?")) {
                    selectStmt.setString(1, uuid.toString());
                    try (ResultSet resultSet = selectStmt.executeQuery()) {
                        if (resultSet.next()) {
                            String existingRespawnData = resultSet.getString("RespawnData");
                            pstmt.setString(26, existingRespawnData);
                        } else {
                            pstmt.setString(26, "");
                        }
                    }
                }
            }

            pstmt.executeUpdate();
            Bukkit.getLogger().info("Saved Player Data for " + player.getName());
            return true;
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error updating player stats for " + player.getName(), e);
            return false;
        }
    }
    private void setDefaultInventory(Player player) {
        // Implement logic to set a default inventory for the player
        player.getInventory().clear();
        if (raceMinigame.getGameState() == MinigameState.NONE) {
            player.getInventory().clear();
            Listeners.Kit(player);
            player.teleport(TeleportBooks.DeadPeaks);

            try {
                new BukkitRunnable() {
                    public void run() {
                        if (ModerationMechanics.isDonator(player)) {
                            Items.giveDonorItems(player);
                        }
                    }
                }.runTaskLater(PracticeServer.getInstance(), 80L);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    public static boolean updatePersistentStats(Player player) {
        if (raceMinigame.getGameState() != MinigameState.NONE) return false;
        if (!PracticeServer.DATABASE) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        PersistentPlayer pp = PersistentPlayers.get(uuid);

        String updateQuery = "UPDATE PersistentData SET " +
                "Tokens = ?, Mount = ?, BankPages = ?, Pickaxe = ?, Farmer = ?, LastStand = ?, " +
                "OrbRolls = ?, Luck = ?, Reaper = ?, KitWeapon = ?, KitHelm = ?, KitChest = ?, " +
                "KitLegs = ?, KitBoots = ?, CurrentQuest = ?, DailyQuestsCompleted = ?, " +
                "LVLHPToggle = ?, PVPToggle = ?, ChaoToggle = ?, FFToggle = ?, DebugToggle = ?, " +
                "HologramToggle = ?, GlowToggle = ?, PMToggle = ?, TradingToggle = ?, GemsToggle = ?, " +
                "TrailToggle = ?, DropToggle = ?, KitToggle = ? " +
                "WHERE UUID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setInt(1, pp.tokens);
            pstmt.setInt(2, pp.mount);
            pstmt.setInt(3, pp.bankpages);
            pstmt.setInt(4, pp.pickaxe);
            pstmt.setInt(5, pp.farmer);
            pstmt.setInt(6, pp.laststand);
            pstmt.setInt(7, pp.orbrolls);
            pstmt.setInt(8, pp.luck);
            pstmt.setInt(9, pp.reaper);
            pstmt.setInt(10, pp.kitweapon);
            pstmt.setInt(11, pp.kithelm);
            pstmt.setInt(12, pp.kitchest);
            pstmt.setInt(13, pp.kitlegs);
            pstmt.setInt(14, pp.kitboots);
            pstmt.setString(15, pp.currentQuest);
            pstmt.setInt(16, pp.dailyQuestsCompleted);

            pstmt.setBoolean(17, Toggles.isToggled(player, "Level HP"));
            pstmt.setBoolean(18, Toggles.isToggled(player, "Anti PVP"));
            pstmt.setBoolean(19, Toggles.isToggled(player, "Chaotic"));
            pstmt.setBoolean(20, Toggles.isToggled(player, "Friendly Fire"));
            pstmt.setBoolean(21, Toggles.isToggled(player, "Debug"));
            pstmt.setBoolean(22, Toggles.isToggled(player, "Hologram Damage"));
            pstmt.setBoolean(23, Toggles.isToggled(player, "Glow Drops"));
            pstmt.setBoolean(24, Toggles.isToggled(player, "Player Messages"));
            pstmt.setBoolean(25, Toggles.isToggled(player, "Trading"));
            pstmt.setBoolean(26, Toggles.isToggled(player, "Gems"));
            pstmt.setBoolean(27, Toggles.isToggled(player, "Trail"));
            pstmt.setBoolean(28, Toggles.isToggled(player, "Drop Protection"));
            pstmt.setBoolean(29, Toggles.isToggled(player, "Disable Kit"));

            pstmt.setString(30, uuid.toString());

            pstmt.executeUpdate();
            PracticeServer.log.info("[RetroDB] Updated Persistent Data for " + player.getName());

            return true;
        } catch (SQLException e) {
            PracticeServer.log.severe("[RetroDB] Error updating persistent data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public static List<ItemStack> getPlayerData(String table, String columns, Player player) {
        if (raceMinigame.getGameState() == MinigameState.SHRINK) return null;
        if (!PracticeServer.DATABASE) return null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT " + columns + " FROM " + table + " WHERE UUID = ?")) {
            pstmt.setString(1, player.getUniqueId().toString());
            List<ItemStack> itemList = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(rs.getString("RespawnData"));
                    itemList = Arrays.asList(items);
                    itemList.removeAll(Collections.singleton(null));
                }
            }
            return itemList;
        } catch (SQLException | IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error getting player data for " + player.getName(), e);
            return null;
        }
    }

    public static ResultSet getPlayerSet(String table, String columns, Player player) {
        if (raceMinigame.getGameState() != MinigameState.NONE) return null;
        if (!PracticeServer.DATABASE) return null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT " + columns + " FROM " + table + " WHERE UUID = ?")) {
            pstmt.setString(1, player.getUniqueId().toString());
            return pstmt.executeQuery();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error getting player set for " + player.getName(), e);
            return null;
        }
    }

    public static void saveGuild(Guild guild) {
        if (PracticeServer.DATABASE) {
            String officers = "";
            String members = "";
            Map<UUID, Role> rolemap = guild.getPlayerRoleMap();
            for (UUID uuid : rolemap.keySet()) {
                if (rolemap.get(uuid) == Role.MEMBER) {
                    members += uuid.toString();
                    members += ",";
                }
                if (rolemap.get(uuid) == Role.OFFICER) {
                    officers += uuid.toString();
                    officers += ",";
                }
            }
            members = members.substring(0, Math.max(members.length() - 1, 0));
            officers = officers.substring(0, Math.max(officers.length() - 1, 0)); //getting rid of last space
            String stats = "INSERT INTO Guilds (GuildName, GuildTag, GuildMOTD, Owner, Officers, Members)" + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(stats)) {
                pstmt.setString(1, guild.getName());
                pstmt.setString(2, guild.getTag());
                pstmt.setString(3, guild.getMotd());
                pstmt.setString(4, guild.getOwner().toString());
                pstmt.setString(5, officers);
                pstmt.setString(6, members);
                pstmt.executeUpdate();
                PracticeServer.log.info("[RetroDB] Saved Guild Data for " + guild.getName());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadGuilds() {
        if (raceMinigame.getGameState() != MinigameState.NONE) return;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Guilds")) {

            while (rs.next()) {
                Guild guild = new Guild(rs.getString("GuildName"));
                guild.setTag(rs.getString("GuildTag"));
                guild.setMotd(rs.getString("GuildMOTD"));
                guild.setOwner(UUID.fromString(rs.getString("Owner")));
                guild.getPlayerRoleMap().put(UUID.fromString(rs.getString("Owner")), Role.LEADER);

                String officers = rs.getString("Officers");
                if (officers != null && !officers.isEmpty()) {
                    for (String s : officers.split(",")) {
                        guild.getPlayerRoleMap().put(UUID.fromString(s), Role.OFFICER);
                    }
                }

                String members = rs.getString("Members");
                if (members != null && !members.isEmpty()) {
                    for (String s : members.split(",")) {
                        guild.getPlayerRoleMap().put(UUID.fromString(s), Role.MEMBER);
                    }
                }

                GuildManager.guildMap.put(guild.getName(), guild);
            }
            PracticeServer.log.info("Guilds loaded successfully.");
        } catch (SQLException e) {
            PracticeServer.log.severe("Error loading guilds: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void deleteGuild(Guild guild) {
        if (raceMinigame.getGameState() != MinigameState.NONE) return;
        try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM Guilds WHERE GuildName = ?")) {
            pstmt.setString(1, guild.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void saveBank(Inventory inv, UUID uuid, int page) {
        if (raceMinigame.getGameState() != MinigameState.NONE) return;
        if (!PracticeServer.DATABASE) {
            Banks.TEMP_BANKS.put(uuid, inv);
            return;
        }

        String table = getTableName(page).toLowerCase();
        String items = BukkitSerialization.itemStackArrayToBase64(inv.getContents());

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Check if checksum column exists, if not, add it
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, table, "checksum");
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    String addColumnSQL = "ALTER TABLE " + table + " ADD COLUMN checksum BIGINT";
                    stmt.execute(addColumnSQL);
                    Bukkit.getLogger().info("Added checksum column to table: " + table);
                }
            }

            String query = "INSERT INTO " + table + " (uuid, username, inventory, checksum) VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT (uuid) DO UPDATE SET username = EXCLUDED.username, inventory = EXCLUDED.inventory, checksum = EXCLUDED.checksum";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                long checksum = calculateChecksum(items);

                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, Bukkit.getOfflinePlayer(uuid).getName());
                pstmt.setString(3, items);
                pstmt.setLong(4, checksum);

                pstmt.executeUpdate();
                conn.commit();
                Bukkit.getLogger().log(Level.INFO, "Saved bank data for player " + uuid + " in table " + table);
            } catch (SQLException e) {
                conn.rollback();
                Bukkit.getLogger().log(Level.SEVERE, "Failed to save bank data for player " + uuid, e);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Database connection error while saving bank data for player " + uuid, e);
        }
    }


    public static void saveGuildBank(Inventory inv, Guild guild) {
        if (raceMinigame.getGameState() != MinigameState.NONE) return;
        if (PracticeServer.DATABASE) {
            String items = BukkitSerialization.itemStackArrayToBase64(inv.getContents());
            String query = "SELECT * FROM GuildBanks WHERE GuildName = ?";
            try (PreparedStatement statement = con.prepareStatement(query)) {
                statement.setString(1, guild.getName());
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    // Guild bank record already exists, perform update
                    String updateQuery = "UPDATE GuildBanks SET GuildBank = ?, Mutex = ?, OccupiedBy = ? WHERE GuildName = ?";
                    try (PreparedStatement updateStatement = con.prepareStatement(updateQuery)) {
                        updateStatement.setString(1, items);
                        updateStatement.setBoolean(2, false);
                        updateStatement.setString(3, "None");
                        updateStatement.setString(4, guild.getName());
                        updateStatement.executeUpdate();
                    }
                } else {
                    // Guild bank record doesn't exist, perform insert
                    String insertQuery = "INSERT INTO GuildBanks (GuildName, GuildBank, Mutex, OccupiedBy) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStatement = con.prepareStatement(insertQuery)) {
                        insertStatement.setString(1, guild.getName());
                        insertStatement.setString(2, items);
                        insertStatement.setBoolean(3, false);
                        insertStatement.setString(4, "None");
                        insertStatement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Inventory getBank(UUID uuid, int page) {
        if (page < 1 || page > 5) return null;
        String table = getTableName(page);
        PersistentPlayer pp = PersistentPlayers.get(uuid);
        Inventory inv = Bukkit.createInventory(null, Banks.BANK_SIZE, "Bank Chest (" + page + "/" + pp.bankpages + ")");

        if (!PracticeServer.DATABASE) {
            return Banks.TEMP_BANKS.getOrDefault(uuid, inv);
        }

        try (Connection conn = getConnection()) {
            // Check if checksum column exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet columnRs = meta.getColumns(null, null, table, "checksum");
            boolean checksumExists = columnRs.next();

            String query = "SELECT * FROM " + table + " WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String inventoryData = rs.getString("inventory");
                        Long storedChecksum = null;
                        if (checksumExists) {
                            try {
                                storedChecksum = rs.getLong("checksum");
                            } catch (SQLException e) {
                                // Checksum column might be null, ignore this error
                            }
                        }

                        if (inventoryData != null) {
                            if (!checksumExists || storedChecksum == null || validateChecksum(inventoryData, storedChecksum)) {
                                ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(inventoryData);
                                inv.setContents(items);
                            } else {
                                Bukkit.getLogger().warning("Checksum validation failed for player " + uuid + " in table " + table);
                                loadBackupInventory(inv, uuid, page);
                            }
                        }
                    }
                }
            }
        } catch (SQLException | IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error loading bank data for player " + uuid, e);
            loadBackupInventory(inv, uuid, page);
        }
        return inv;
    }

    public static void checkAndUpdateBankTables() {
        String[] tables = {"banks", "banks2", "banks3", "banks4", "banks5"};
        for (String table : tables) {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                DatabaseMetaData meta = conn.getMetaData();

                // Check if table exists, if not create it
                ResultSet tableRs = meta.getTables(null, null, table, new String[] {"TABLE"});
                if (!tableRs.next()) {
                    try (Statement stmt = conn.createStatement()) {
                        String createTableSQL = "CREATE TABLE " + table + " (" +
                                "uuid VARCHAR(36) PRIMARY KEY, " +
                                "username VARCHAR(16), " +
                                "inventory TEXT, " +
                                "checksum BIGINT" +
                                ")";
                        stmt.execute(createTableSQL);
                        Bukkit.getLogger().info("Created table: " + table);
                    }
                }

                // Check for each required column and add if missing
                String[] requiredColumns = {"uuid", "username", "inventory", "checksum"};
                String[] columnTypes = {"VARCHAR(36)", "VARCHAR(16)", "TEXT", "BIGINT"};

                for (int i = 0; i < requiredColumns.length; i++) {
                    ResultSet columnRs = meta.getColumns(null, null, table, requiredColumns[i]);
                    if (!columnRs.next()) {
                        try (Statement stmt = conn.createStatement()) {
                            String addColumnSQL = "ALTER TABLE " + table + " ADD COLUMN " +
                                    requiredColumns[i] + " " + columnTypes[i];
                            stmt.execute(addColumnSQL);
                            Bukkit.getLogger().info("Added column " + requiredColumns[i] + " to table: " + table);
                        }
                    }
                }

                // Ensure uuid is the primary key
                try (Statement stmt = conn.createStatement()) {
                    String checkPrimaryKeySQL = "SELECT COUNT(*) FROM information_schema.table_constraints " +
                            "WHERE table_name = '" + table + "' AND constraint_type = 'PRIMARY KEY'";
                    ResultSet pkRs = stmt.executeQuery(checkPrimaryKeySQL);
                    if (pkRs.next() && pkRs.getInt(1) == 0) {
                        String addPrimaryKeySQL = "ALTER TABLE " + table + " ADD PRIMARY KEY (uuid)";
                        stmt.execute(addPrimaryKeySQL);
                        Bukkit.getLogger().info("Added primary key constraint to uuid column in table: " + table);
                    }
                }

                conn.commit();
                Bukkit.getLogger().info("Successfully checked and updated table: " + table);
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error checking/updating table " + table, e);
            }
        }
    }
    private static void loadBackupInventory(Inventory inv, UUID uuid, int page) {
        String backupTable = getTableName(page) + "_Backup";
        String query = "SELECT Inventory FROM " + backupTable + " WHERE UUID = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String inventoryData = rs.getString("Inventory");
                    ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(inventoryData);
                    inv.setContents(items);
                    Bukkit.getLogger().info("Loaded backup inventory for player " + uuid + " on page " + page);
                } else {
                    Bukkit.getLogger().warning("No backup found for player " + uuid + " on page " + page + ". Using empty inventory.");
                    inv.clear();
                }
            }
        } catch (SQLException | IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error loading backup inventory for player " + uuid, e);
            inv.clear();
        }
    }
    public static void backupBankData() {
        if (!PracticeServer.DATABASE) return;

        for (int page = 1; page <= 5; page++) {
            String sourceTable = getTableName(page);
            String backupTable = sourceTable + "_Backup";
            String query = "INSERT INTO " + backupTable + " SELECT * FROM " + sourceTable +
                    " ON CONFLICT (UUID) DO UPDATE SET Username = EXCLUDED.Username, " +
                    "Inventory = EXCLUDED.Inventory, Checksum = EXCLUDED.Checksum";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                int rowsAffected = pstmt.executeUpdate();
                Bukkit.getLogger().info("Backed up " + rowsAffected + " records from " + sourceTable + " to " + backupTable);
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to backup bank data for " + sourceTable, e);
            }
        }
    }
    private static long calculateChecksum(String data) {
        CRC32 crc = new CRC32();
        crc.update(data.getBytes());
        return crc.getValue();
    }

    private static boolean validateChecksum(String data, long storedChecksum) {
        return calculateChecksum(data) == storedChecksum;
    }
    public static String getTableName(int page) {
        switch (page) {
            case 1:
                return "Banks";
            case 2:
                return "Banks2";
            case 3:
                return "Banks3";
            case 4:
                return "Banks4";
            case 5:
                return "Banks5";
            default:
                return "Banks";
        }
    }

    public static Inventory getGuildBank(Player p, Guild guild) {
        if (raceMinigame.getGameState() != MinigameState.NONE) return null;
        Inventory inv = Bukkit.createInventory(null, GuildBank.guildBankSize, "Guild Bank Chest (1/1)");
        try {
            PreparedStatement stmt1 = con.prepareStatement("UPDATE GuildBanks SET OccupiedBy = ?, Mutex = 1 WHERE GuildName = ? AND Mutex = 0");
            stmt1.setString(1, p.getName());
            stmt1.setString(2, guild.getName());
            stmt1.execute();

            PreparedStatement stmt2 = con.prepareStatement("SELECT * FROM GuildBanks WHERE GuildName = ?");
            stmt2.setString(1, guild.getName());
            ResultSet rs = stmt2.executeQuery();
            if (rs.next()) {
                if (rs.getString("OccupiedBy").equals(p.getName())) {
                    ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(rs.getString("GuildBank"));
                    inv.setContents(items);
                } else {
                    p.sendMessage("Guild Bank Occupied by " + rs.getString("OccupiedBy"));
                    return null;
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return inv;
    }

    public static void updateRank(Player p) {
        if (PracticeServer.DATABASE) {
            try {
                String query = "UPDATE PersistentData SET PlayerRank = ? WHERE UUID = ?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, RankEnum.enumToString(ModerationMechanics.getRank(p)));
                stmt.setString(2, p.getUniqueId().toString());
                stmt.executeUpdate();
                System.out.println("[RetroDB] Updated Rank for " + p.getName());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void clonePlayer(Player p, String target) {
        try {
            PreparedStatement stmt1 = con.prepareStatement("SELECT * FROM PlayerData WHERE Username = ?");
            stmt1.setString(1, target);
            ResultSet rs = stmt1.executeQuery();
            if (rs.next()) {
                p.kickPlayer(ChatColor.GREEN + "Cloning " + target);
                new AsyncTask(() -> {
                    try {
                        PreparedStatement stmt2 = con.prepareStatement("INSERT INTO PlayerData (UUID, Username, XCoord, YCoord, ZCoord, Yaw, Pitch, Inventory, Armor, MaxHP, Gems, GuildName, Alignment, AlignTime, T1Kills, T2Kills, T3Kills, T4Kills, T5Kills, T6Kills, Deaths, PlayerKills, OreMined, ChestsOpened, RespawnData) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        stmt2.setString(1, p.getUniqueId().toString());
                        stmt2.setString(2, p.getName());
                        stmt2.setInt(3, rs.getInt("XCoord"));
                        stmt2.setInt(4, rs.getInt("YCoord"));
                        stmt2.setInt(5, rs.getInt("ZCoord"));
                        stmt2.setInt(6, rs.getInt("Yaw"));
                        stmt2.setInt(7, rs.getInt("Pitch"));
                        stmt2.setString(8, rs.getString("Inventory"));
                        stmt2.setString(9, rs.getString("Armor"));
                        stmt2.setInt(10, rs.getInt("MaxHP"));
                        stmt2.setInt(11, rs.getInt("Gems"));
                        stmt2.setString(12, rs.getString("GuildName"));
                        stmt2.setString(13, rs.getString("Alignment"));
                        stmt2.setInt(14, rs.getInt("AlignTime"));
                        stmt2.setInt(15, rs.getInt("T1Kills"));
                        stmt2.setInt(16, rs.getInt("T2Kills"));
                        stmt2.setInt(17, rs.getInt("T3Kills"));
                        stmt2.setInt(18, rs.getInt("T4Kills"));
                        stmt2.setInt(19, rs.getInt("T5Kills"));
                        stmt2.setInt(20, rs.getInt("T6Kills"));
                        stmt2.setInt(21, rs.getInt("Deaths"));
                        stmt2.setInt(22, rs.getInt("PlayerKills"));
                        stmt2.setInt(23, rs.getInt("OreMined"));
                        stmt2.setInt(24, rs.getInt("ChestsOpened"));
                        stmt2.setString(25, rs.getString("RespawnData"));
                        stmt2.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }).setDelay(2).scheduleDelayedTask();
            } else {
                p.sendMessage(ChatColor.RED + "Player does not exist!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getPlayerData(String table, String columns) {
        if (raceMinigame.getGameState() == MinigameState.SHRINK) return null;
        if (PracticeServer.DATABASE && con != null) {
            try {
                return con.createStatement().executeQuery("SELECT " + columns + " FROM " + table);
            } catch (SQLException e) {
                PracticeServer.log.severe("[RetroDB] Error executing query: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        if (PracticeServer.DATABASE) {
            try {
                Class.forName("org.postgresql.Driver");
                con = getConnection();
                if (con != null) {
                    Bukkit.getLogger().info("Database connection established");

                    // Ensure tables are created
                    SQLCreate.createTables(con);
                    SQLCreate.addMissingColumns(con);

                    createBackupTables();
                    checkAndUpdateBankTables();
                    updateGuildsSchema();
                    startConnectionKeepAlive();
                    if (raceMinigame.getGameState() == MinigameState.NONE) {
                        loadPersistentData();
                        loadGems();
                    }
                } else {
                    Bukkit.getLogger().severe("Failed to establish database connection!");
                }
            } catch (ClassNotFoundException e) {
                Bukkit.getLogger().severe("PostgreSQL JDBC Driver not found!");
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "SQL Error during initialization", e);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Unexpected error during initialization", e);
            }
        } else {
            Bukkit.getLogger().info("Database functionality is disabled.");
        }
    }
    public static void updateMountData(Player player) {
        if (!PracticeServer.DATABASE) return;

        UUID uuid = player.getUniqueId();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE PlayerData SET Mount = ? WHERE UUID = ?")) {
            pstmt.setInt(1, Horses.horseTier.getOrDefault(player, 0));
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static boolean columnExists(DatabaseMetaData meta, String table, String column) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            return rs.next();
        }
    }
    private void startConnectionKeepAlive() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(PracticeServer.plugin, () -> {
            try {
                if (con != null && !con.isClosed()) {
                    try (Statement stmt = con.createStatement()) {
                        stmt.execute("SELECT 1");
                    }
                    Bukkit.getLogger().fine("Keep-alive query executed successfully");
                } else {
                    con = getConnection();
                    Bukkit.getLogger().info("Database connection re-established");
                }
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error in keep-alive task", e);
            }
        }, 20 * 60, 20 * 60); // Run every minute
    }


    public void onDisable() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                PracticeServer.log.info("[RetroDB] Connection Closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void createBackupTables() {
        String[] tables = {"Banks", "Banks2", "Banks3", "Banks4", "Banks5"};
        for (String table : tables) {
            String backupTable = table + "_Backup";
            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + backupTable + " (" +
                    "UUID VARCHAR(36) PRIMARY KEY, " +
                    "Username VARCHAR(16), " +
                    "Inventory TEXT, " +
                    "Checksum BIGINT" +
                    ")";
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(createTableQuery);
                Bukkit.getLogger().info("Created or verified backup table: " + backupTable);
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error creating backup table " + backupTable, e);
            }
        }
    }
    private List<ItemStack> getPlayerInventory(Player player) {
        List<ItemStack> inventory = new ArrayList<>();

        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] contents = playerInventory.getContents();

        for (ItemStack item : contents) {
            if (item != null && !item.getType().equals(Material.AIR)) {
                inventory.add(item.clone());
            }
        }

        return inventory;
    }

    @EventHandler
    void onLogout(PlayerQuitEvent e) {
        updatePersistentStats(e.getPlayer());
        Player p = e.getPlayer();
        if ((!Alignments.isSafeZone(p.getLocation()) && Alignments.tagged.containsKey(p.getName()) && System.currentTimeMillis() - Alignments.tagged.get(p.getName()) < 10000) || !Alignments.isSafeZone(p.getLocation()) && Listeners.combat.containsKey(p.getName()) && System.currentTimeMillis() - Listeners.combat.get(p.getName()) < 10000) {
            Alignments.logout = true;
            p.setHealth(0.0);
            if (Alignments.chaotic.containsKey(p.getName()) || (Alignments.neutral.containsKey(p.getName()))) {
                for (Player player : p.getWorld().getPlayers()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 1.0F, 1.0F);
                }
            }
        } else {
            updatePlayerStats(e.getPlayer());
        }
    }

    @EventHandler
    void onKick(PlayerKickEvent e) {
        Alignments.tagged.remove(e.getPlayer().getName());
        Listeners.combat.remove(e.getPlayer().getName());
        updatePlayerStats(e.getPlayer());
        updatePersistentStats(e.getPlayer());
    }

    public void loadData(Player player) {
        try (PreparedStatement stmt1 = con.prepareStatement("SELECT * FROM PersistentData WHERE UUID = ?");
             PreparedStatement stmt2 = con.prepareStatement("SELECT * FROM PlayerData WHERE UUID = ?")) {
            stmt1.setString(1, player.getUniqueId().toString());
            stmt2.setString(1, player.getUniqueId().toString());
            ResultSet rs1 = stmt1.executeQuery();
            ResultSet rs2 = stmt2.executeQuery();
            handlePersistentData(player, rs1);
            handlePlayerData(player, rs2);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    @EventHandler
    void onPlayerJoin(PlayerJoinEvent e) {
        if (!PracticeServer.DATABASE) {
            return;
        }

        Player player = e.getPlayer();
        loadData(player);
    }
    public static void getTogglesFromSQL(UUID uuid, ResultSet rs) throws SQLException {
        Set<String> toggles = new HashSet<>();
        Set<String> toggleColumns = Toggles.getToggleColumns();
        for (String column : toggleColumns) {
            if (rs.getBoolean(column)) {
                toggles.add(getToggleNameFromColumn(column));
            }
        }

        Toggles.getToggles(uuid).addAll(toggles);
    }
    private void handlePersistentData(Player player, ResultSet rs) throws SQLException {
        UUID uuid = player.getUniqueId();
        PersistentPlayer pp;

        if (rs.next()) {
            // Data exists, load it
            pp = new PersistentPlayer(
                    rs.getInt("Tokens"),
                    rs.getInt("Mount"),
                    rs.getInt("Pickaxe"),
                    rs.getInt("Farmer"),
                    rs.getInt("LastStand"),
                    rs.getInt("BankPages"),
                    rs.getInt("OrbRolls"),
                    rs.getInt("Luck"),
                    rs.getInt("Reaper"),
                    rs.getInt("KitWeapon"),
                    rs.getInt("KitHelm"),
                    rs.getInt("KitChest"),
                    rs.getInt("KitLegs"),
                    rs.getInt("KitBoots"),
                    rs.getInt("DailyQuestsCompleted"),
                    rs.getString("CurrentQuest")
            );

            String playerRank = rs.getString("PlayerRank");
            RankEnum rank = (playerRank != null) ? RankEnum.fromString(playerRank) : RankEnum.DEFAULT;
            ModerationMechanics.rankHashMap.put(uuid, rank);

            int mount = rs.getInt("Mount") + 1;
            if(PracticeServer.getRaceMinigame().getGameState() == MinigameState.NONE) {
                Horses.horseTier.put(player, mount);
            }

            Toggles.loadTogglesFromSQL(uuid, rs);
        } else {
            // Data doesn't exist, create default values
            pp = new PersistentPlayer(
                    50, // Default tokens
                    0, // Default mount
                    0, // Default pickaxe
                    0, // Default farmer
                    0, // Default laststand
                    1, // Default bank pages
                    0, // Default orb rolls
                    0, // Default luck
                    0, // Default reaper
                    1, // Default kit weapon
                    0, // Default kit helm
                    0, // Default kit chest
                    0, // Default kit legs
                    0, // Default kit boots
                    0, // Default daily quests completed
                    null // Default current quest
            );

            ModerationMechanics.rankHashMap.put(uuid, RankEnum.DEFAULT);
            if(PracticeServer.getRaceMinigame().getGameState() == MinigameState.NONE) {
                Horses.horseTier.put(player, 0);
            } else {
                if(!Horses.horseTier.containsKey(player)) {
                    Horses.horseTier.put(player, 3);
                }
            }

            Toggles.getToggles(uuid).clear(); // Initialize with no toggles

            // Save the default data to the database
            saveDefaultPersistentData(player, pp);
        }

        PersistentPlayers.put(uuid, pp);

        PracticeServer.log.info("Loaded persistent data for " + player.getName());
    }

    private void saveDefaultPersistentData(Player player, PersistentPlayer pp) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO PersistentData (UUID, Username, Tokens, Mount, BankPages, Pickaxe, Farmer, LastStand, " +
                             "OrbRolls, Luck, Reaper, KitWeapon, KitHelm, KitChest, KitLegs, KitBoots, CurrentQuest, DailyQuestsCompleted, PlayerRank) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, player.getName());
            pstmt.setInt(3, pp.tokens);
            pstmt.setInt(4, pp.mount);
            pstmt.setInt(5, pp.bankpages);
            pstmt.setInt(6, pp.pickaxe);
            pstmt.setInt(7, pp.farmer);
            pstmt.setInt(8, pp.laststand);
            pstmt.setInt(9, pp.orbrolls);
            pstmt.setInt(10, pp.luck);
            pstmt.setInt(11, pp.reaper);
            pstmt.setInt(12, pp.kitweapon);
            pstmt.setInt(13, pp.kithelm);
            pstmt.setInt(14, pp.kitchest);
            pstmt.setInt(15, pp.kitlegs);
            pstmt.setInt(16, pp.kitboots);
            pstmt.setString(17, pp.currentQuest);
            pstmt.setInt(18, pp.dailyQuestsCompleted);
            pstmt.setString(19, RankEnum.DEFAULT.toString());

            pstmt.executeUpdate();

            PracticeServer.log.info("Saved default persistent data for " + player.getName());
        } catch (SQLException e) {
            PracticeServer.log.severe("Error saving default persistent data for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void initializeNewPlayer(Player player) {
        PracticeServer.log.info("Initializing new player data for " + player.getName());
        try {
            GuildPlayers.add(new GuildPlayer(player.getUniqueId(), player.getName(), "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

            if (raceMinigame.getGameState() == MinigameState.NONE) {
                setDefaultInventory(player);
                player.teleport(TeleportBooks.DeadPeaks);

            }

            // Initialize other default values
            Horses.horseTier.put(player, 0);
            Economy.depositPlayer(player.getUniqueId(), 0); // Set initial balance to 0 or any default value

            // Save the initial player data
            updatePlayerStats(player);

            PracticeServer.log.info("Successfully initialized new player data for " + player.getName());
        } catch (Exception e) {
            PracticeServer.log.severe("Error initializing new player data for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void handlePlayerData(Player player, ResultSet rs) {
        if (raceMinigame.getGameState() != MinigameState.NONE) {
            PracticeServer.log.info("Skipping handlePlayerData for " + player.getName() + " due to race minigame state.");
            return;
        }

        PracticeServer.log.info("Preparing data for delayed loading for player: " + player.getName());

        final Map<String, Object> playerData = new HashMap<>();
        try {
            if (rs.next()) {
                playerData.put("GuildName", rs.getString("GuildName"));
                playerData.put("PlayerKills", rs.getInt("PlayerKills"));
                playerData.put("T1Kills", rs.getInt("T1Kills"));
                playerData.put("T2Kills", rs.getInt("T2Kills"));
                playerData.put("T3Kills", rs.getInt("T3Kills"));
                playerData.put("T4Kills", rs.getInt("T4Kills"));
                playerData.put("T5Kills", rs.getInt("T5Kills"));
                playerData.put("T6Kills", rs.getInt("T6Kills"));
                playerData.put("ChestsOpened", rs.getInt("ChestsOpened"));
                playerData.put("Deaths", rs.getInt("Deaths"));
                playerData.put("OreMined", rs.getInt("OreMined"));
                playerData.put("HorseTier", rs.getInt("HorseTier"));
                playerData.put("Alignment", rs.getString("Alignment"));
                playerData.put("AlignTime", rs.getInt("AlignTime"));
                playerData.put("Inventory", rs.getString("Inventory"));
                playerData.put("Armor", rs.getString("Armor"));
                playerData.put("XCoord", rs.getDouble("XCoord"));
                playerData.put("YCoord", rs.getDouble("YCoord"));
                playerData.put("ZCoord", rs.getDouble("ZCoord"));
                playerData.put("Yaw", rs.getFloat("Yaw"));
                playerData.put("Pitch", rs.getFloat("Pitch"));
            }
        } catch (SQLException e) {
            PracticeServer.log.severe("Error reading player data from ResultSet for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Schedule the data loading task with a 2-second delay
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    loadPlayerDataWithDelay(player, playerData);
                } catch (Exception e) {
                    PracticeServer.log.severe("Error in delayed player data loading for " + player.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    player.kickPlayer("Error loading player data. Please try again later.");
                }
            }
        }.runTaskLater(PracticeServer.getInstance(), 1L); // 40 ticks = 2 seconds
    }


    private void loadPlayerDataWithDelay(Player player, Map<String, Object> playerData) {
        PracticeServer.log.info("Starting delayed data loading for player: " + player.getName());

        try {
            if (playerData.containsKey("GuildName")) {
                String guildName = (String) playerData.get("GuildName");
                int playerKills = (int) playerData.get("PlayerKills");
                int t1Kills = (int) playerData.get("T1Kills");
                int t2Kills = (int) playerData.get("T2Kills");
                int t3Kills = (int) playerData.get("T3Kills");
                int t4Kills = (int) playerData.get("T4Kills");
                int t5Kills = (int) playerData.get("T5Kills");
                int t6Kills = (int) playerData.get("T6Kills");
                int lootChestsOpen = (int) playerData.get("ChestsOpened");
                int deaths = (int) playerData.get("Deaths");
                int oreMined = (int) playerData.get("OreMined");

                GuildPlayers.add(new GuildPlayer(player.getUniqueId(), player.getName(), guildName,
                        playerKills, t1Kills, t2Kills, t3Kills, t4Kills, t5Kills, t6Kills, lootChestsOpen,
                        oreMined, deaths, 0));

                int horseTier = (int) playerData.get("HorseTier");
                if (horseTier > Horses.horseTier.get(player)) {
                    Horses.horseTier.put(player, horseTier);
                }

                String alignment = (String) playerData.get("Alignment");
                int alignTime = (int) playerData.get("AlignTime");
                if (alignment != null) {
                    if (alignment.contains("CHAOTIC")) {
                        Alignments.chaotic.put(player.getName(), alignTime);
                    } else if (alignment.contains("NEUTRAL")) {
                        Alignments.neutral.put(player.getName(), alignTime);
                    }
                }

                player.getInventory().clear();
                String inventoryBase64 = (String) playerData.get("Inventory");
                String armorBase64 = (String) playerData.get("Armor");
                if (inventoryBase64 != null && armorBase64 != null) {
                    try {
                        ItemStack[] inventoryItems = BukkitSerialization.itemStackArrayFromBase64(inventoryBase64);
                        ItemStack[] armorItems = BukkitSerialization.itemStackArrayFromBase64(armorBase64);
                        player.getInventory().setContents(inventoryItems);
                        player.getInventory().setArmorContents(armorItems);
                    } catch (IllegalArgumentException | IOException e) {
                        PracticeServer.log.warning("Error deserializing inventory for " + player.getName() + ": " + e.getMessage());
                        setDefaultInventory(player);
                    }
                } else {
                    PracticeServer.log.warning("Null inventory or armor data for player: " + player.getName());
                    setDefaultInventory(player);
                }

                if (!player.isDead()) {
                    double xCoord = (double) playerData.get("XCoord");
                    double yCoord = (double) playerData.get("YCoord");
                    double zCoord = (double) playerData.get("ZCoord");
                    float yaw = (float) playerData.get("Yaw");
                    float pitch = (float) playerData.get("Pitch");
                    Location loc = new Location(player.getWorld(), xCoord, yCoord, zCoord, yaw, pitch);
                    if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                        player.teleport(loc);
                    } else {
                        PracticeServer.log.warning("Invalid or unloaded location for " + player.getName() + ". Using default spawn.");
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                }

                PracticeServer.log.info("Successfully loaded data for " + player.getName());
            } else {
                PracticeServer.log.info("No existing data found for " + player.getName() + ". Initializing new player data.");
                initializeNewPlayer(player);
            }
        } catch (Exception e) {
            PracticeServer.log.severe("Unexpected error while handling player data for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.kickPlayer("Error loading player data. Please try again later.");
        }
    }

    public static Set<String> getToggleColumns() {
        return new HashSet<>(Arrays.asList(
                "LVLHPToggle",
                "PVPToggle",
                "ChaoToggle",
                "FFToggle",
                "DebugToggle",
                "HologramToggle",
                "GlowToggle",
                "PMToggle",
                "TradingToggle",
                "GemsToggle",
                "TrailToggle",
                "DropToggle",
                "KitToggle"
        ));
    }


    private static String getToggleNameFromColumn(String column) {
        switch (column) {
            case "LVLHPToggle": return "Level HP";
            case "PVPToggle": return "Anti PVP";
            case "ChaoToggle": return "Chaotic";
            case "FFToggle": return "Friendly Fire";
            case "DebugToggle": return "Debug";
            case "HologramToggle": return "Hologram Damage";
            case "GlowToggle": return "Glow Drops";
            case "PMToggle": return "Player Messages";
            case "TradingToggle": return "Trading";
            case "GemsToggle": return "Gems";
            case "TrailToggle": return "Trail";
            case "DropToggle": return "Drop Protection";
            case "KitToggle": return "Disable Kit";
            default: return "";
        }
    }

    void loadPersistentData() {
        if (PracticeServer.DATABASE) {
            try {
                ResultSet rs = SQLMain.getPlayerData("PersistentData", "*");
                if (rs != null) {
                    while (rs.next()) {
                        String uuidString = rs.getString("UUID");
                        if (uuidString == null) {
                            PracticeServer.log.severe("[RetroDB] UUID is null in PersistentData.");
                            continue;
                        }
                        UUID uuid = UUID.fromString(uuidString);

                        String playerRankString = rs.getString("PlayerRank");
                        if (playerRankString == null) {
                            PracticeServer.log.warning("[RetroDB] PlayerRank is null for UUID: " + uuid);
                        } else {
                            RankEnum playerRank = RankEnum.fromString(playerRankString);
                            ModerationMechanics.rankHashMap.put(uuid, playerRank);
                        }

                        getTogglesFromSQL(uuid, rs);

                        String buddiesString = rs.getString("Buddies");
                        ArrayList<String> buddies = new ArrayList<>();
                        if (buddiesString != null) {
                            for (String s : buddiesString.split(",")) {
                                if (s.length() > 30) buddies.add(s);
                            }
                        }
                        Buddies.buddies.put(rs.getString("Username"), buddies);

                        int tokens = rs.getInt("Tokens");
                        int mount = rs.getInt("Mount");
                        int pickaxe = rs.getInt("Pickaxe");
                        int farmer = rs.getInt("Farmer");
                        int laststand = rs.getInt("LastStand");
                        int bankpages = rs.getInt("BankPages");
                        int orbrolls = rs.getInt("OrbRolls");
                        int luck = rs.getInt("Luck");
                        int reaper = rs.getInt("Reaper");
                        int kitweapon = rs.getInt("KitWeapon");
                        int kithelm = rs.getInt("KitHelm");
                        int kitchest = rs.getInt("KitChest");
                        int kitlegs = rs.getInt("KitLegs");
                        int kitboots = rs.getInt("KitBoots");
                        String currentQuest = rs.getString("CurrentQuest");
                        int dailyQuestsCompleted = rs.getInt("DailyQuestsCompleted");

                        PersistentPlayer persistentPlayer = new PersistentPlayer(
                                tokens, mount, pickaxe, farmer, laststand, bankpages, orbrolls, luck,
                                reaper, kitweapon, kithelm, kitchest, kitlegs, kitboots,
                                dailyQuestsCompleted, currentQuest
                        );

                        PersistentPlayers.put(uuid, persistentPlayer);
                    }
                } else {
                    PracticeServer.log.warning("[RetroDB] No persistent data found.");
                }
            } catch (SQLException e) {
                PracticeServer.log.severe("[RetroDB] SQL Error during loading persistent data: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                PracticeServer.log.severe("[RetroDB] Unexpected error during loading persistent data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void updateGuildsSchema() {
        String createGuildsTable = "CREATE TABLE IF NOT EXISTS Guilds (" +
                "GuildName VARCHAR(255) PRIMARY KEY," +
                "GuildTag VARCHAR(10)," +
                "GuildMOTD TEXT," +
                "Owner UUID," +
                "Officers TEXT," +
                "Members TEXT" +
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createGuildsTable);
            PracticeServer.log.info("Guilds table created or verified.");
        } catch (SQLException e) {
            PracticeServer.log.severe("Error creating Guilds table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        File propertiesFile = new File(PracticeServer.plugin.getDataFolder(), "db.properties");
        if (!propertiesFile.exists()) {
            PracticeServer.log.severe("[RetroDB] db.properties file not found!");
            throw new SQLException("Database configuration file not found.");
        }

        try (FileInputStream in = new FileInputStream(propertiesFile)) {
            props.load(in);
        } catch (IOException e) {
            PracticeServer.log.severe("[RetroDB] Error reading db.properties file: " + e.getMessage());
            throw new SQLException("Error reading database configuration file.", e);
        }

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        if (url == null || username == null || password == null) {
            PracticeServer.log.severe("[RetroDB] Missing database configuration in db.properties!");
            throw new SQLException("Incomplete database configuration.");
        }

        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);

        Connection connection = DriverManager.getConnection(url, connectionProps);
        if (connection == null) {
            PracticeServer.log.severe("[RetroDB] Failed to establish database connection!");
            throw new SQLException("Failed to establish database connection.");
        }

        return connection;
    }

    public static void loadGems() {
        if (!PracticeServer.DATABASE) return;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UUID, Gems FROM PlayerData")) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("UUID"));
                int gems = rs.getInt("Gems");
                Economy.currentBalance.put(uuid, gems);
            }
            Bukkit.getLogger().info("Loaded gem balances for all players");
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error loading gem balances", e);
        }
    }
}
