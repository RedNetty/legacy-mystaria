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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SQLMain implements Listener {
    public static Connection con;

    public static void updateGuild(UUID uuid, String guildName) {
        try (PreparedStatement pstmt = con.prepareStatement("UPDATE PlayerData SET GuildName = ? WHERE UUID = ?")) {
            pstmt.setString(1, guildName);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateRespawnData(Player player, List<ItemStack> items) {
        if (!PracticeServer.DATABASE) {
            return;
        }

        updatePlayerStats(player);

        try (PreparedStatement pstmt = con.prepareStatement("UPDATE PlayerData SET RespawnData = ? WHERE UUID = ?")) {
            pstmt.setString(1, BukkitSerialization.itemStackArrayToBase64(items.toArray(new ItemStack[0])));
            pstmt.setString(2, player.getUniqueId().toString());
            if (!items.isEmpty()) {
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadRespawnData(Player player) {
        if (!PracticeServer.DATABASE) {
            return;
        }

        try (ResultSet rs = getPlayerData("PlayerData", "RespawnData", player)) {
            if (rs.next()) {
                ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(rs.getString("RespawnData"));
                List<ItemStack> itemList = Arrays.asList(items);
                itemList.removeAll(Collections.singleton(null));
                for (ItemStack itemStack : itemList) {
                    player.getInventory().addItem(itemStack);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean updatePlayerStats(Player player) {
        if (!PracticeServer.DATABASE) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        String[] pinv = BukkitSerialization.playerInventoryToBase64(player.getInventory());
        GuildPlayers gp = GuildPlayers.getInstance();
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO PlayerData (UUID, Username, XCoord, YCoord, ZCoord, Yaw, Pitch, " +
                "Inventory, Armor, MaxHP, Gems, GuildName, Alignment, AlignTime, HorseTier, T1Kills, T2Kills, T3Kills, T4Kills, T5Kills, " +
                "T6Kills, Deaths, PlayerKills, OreMined, ChestsOpened, RespawnData) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (UUID) DO UPDATE SET " +
                "Username = excluded.Username, " +
                "XCoord = excluded.XCoord, " +
                "YCoord = excluded.YCoord, " +
                "ZCoord = excluded.ZCoord, " +
                "Yaw = excluded.Yaw, " +
                "Pitch = excluded.Pitch, " +
                "Inventory = excluded.Inventory, " +
                "Armor = excluded.Armor, " +
                "MaxHP = excluded.MaxHP, " +
                "Gems = excluded.Gems, " +
                "GuildName = excluded.GuildName, " +
                "Alignment = excluded.Alignment, " +
                "AlignTime = excluded.AlignTime, " +
                "HorseTier = excluded.HorseTier, " +
                "T1Kills = excluded.T1Kills, " +
                "T2Kills = excluded.T2Kills, " +
                "T3Kills = excluded.T3Kills, " +
                "T4Kills = excluded.T4Kills, " +
                "T5Kills = excluded.T5Kills, " +
                "T6Kills = excluded.T6Kills, " +
                "Deaths = excluded.Deaths, " +
                "PlayerKills = excluded.PlayerKills, " +
                "OreMined = excluded.OreMined, " +
                "ChestsOpened = excluded.ChestsOpened, " +
                "RespawnData = excluded.RespawnData")) {

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
            pstmt.setString(12, gp.get(uuid).getGuildName());
            pstmt.setString(13, Alignments.get(player));
            pstmt.setInt(14, Alignments.getAlignTime(player));
            pstmt.setInt(15, Horses.horseTier.get(player));
            pstmt.setInt(16, gp.get(uuid).getT1Kills());
            pstmt.setInt(17, gp.get(uuid).getT2Kills());
            pstmt.setInt(18, gp.get(uuid).getT3Kills());
            pstmt.setInt(19, gp.get(uuid).getT4Kills());
            pstmt.setInt(20, gp.get(uuid).getT5Kills());
            pstmt.setInt(21, gp.get(uuid).getT6Kills());
            pstmt.setInt(22, gp.get(uuid).getDeaths());
            pstmt.setInt(23, gp.get(uuid).getPlayerKills());
            pstmt.setInt(24, gp.get(uuid).getOreMined());
            pstmt.setInt(25, gp.get(uuid).getLootChestsOpen());
            pstmt.setString(26, "");

            pstmt.executeUpdate();
            PracticeServer.log.info("[RetroDB] Saved Player Data for " + player.getName());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePersistentStats(Player player) {
        if (!PracticeServer.DATABASE) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        PersistentPlayer pp = PersistentPlayers.get(uuid);
        String buddies = "";

        if (Buddies.buddies.get(player.getName()) != null) {
            for (String s : Buddies.buddies.get(player.getName())) {
                buddies += s;
                buddies += ",";
            }
        }

        String rank = RankEnum.DEFAULT.toString();
        try {
            rank = RankEnum.enumToString(ModerationMechanics.getRank(player));
        } catch (NoClassDefFoundError ignored) {
        }

        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO PersistentData (UUID, Username, PlayerRank, Buddies, PVPToggle, " +
                "ChaoToggle, FFToggle, DebugToggle, HologramToggle, LVLHPToggle, GlowToggle, PMToggle, TradingToggle, GemsToggle, " +
                "TrailToggle, DropToggle, KitToggle, Tokens, Mount, BankPages, Pickaxe, Farmer, LastStand, OrbRolls, Luck, Reaper, " +
                "KitWeapon, KitHelm, KitChest, KitLegs, KitBoots) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (UUID) DO UPDATE SET " +
                "Username = excluded.Username, " +
                "PlayerRank = excluded.PlayerRank, " +
                "Buddies = excluded.Buddies, " +
                "PVPToggle = excluded.PVPToggle, " +
                "ChaoToggle = excluded.ChaoToggle, " +
                "FFToggle = excluded.FFToggle, " +
                "DebugToggle = excluded.DebugToggle, " +
                "HologramToggle = excluded.HologramToggle, " +
                "LVLHPToggle = excluded.LVLHPToggle, " +
                "GlowToggle = excluded.GlowToggle, " +
                "PMToggle = excluded.PMToggle, " +
                "TradingToggle = excluded.TradingToggle, " +
                "GemsToggle = excluded.GemsToggle, " +
                "TrailToggle = excluded.TrailToggle, " +
                "DropToggle = excluded.DropToggle, " +
                "KitToggle = excluded.KitToggle, " +
                "Tokens = excluded.Tokens, " +
                "Mount = excluded.Mount, " +
                "BankPages = excluded.BankPages, " +
                "Pickaxe = excluded.Pickaxe, " +
                "Farmer = excluded.Farmer, " +
                "LastStand = excluded.LastStand, " +
                "OrbRolls = excluded.OrbRolls, " +
                "Luck = excluded.Luck, " +
                "Reaper = excluded.Reaper, " +
                "KitWeapon = excluded.KitWeapon, " +
                "KitHelm = excluded.KitHelm, " +
                "KitChest = excluded.KitChest, " +
                "KitLegs = excluded.KitLegs, " +
                "KitBoots = excluded.KitBoots")) {


            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, player.getName());
            pstmt.setString(3, rank);
            pstmt.setString(4, buddies);
            pstmt.setBoolean(5, Toggles.getToggleStatus(player, "Anti PVP"));
            pstmt.setBoolean(6, Toggles.getToggleStatus(player, "Chaotic"));
            pstmt.setBoolean(7, Toggles.getToggleStatus(player, "Friendly Fire"));
            pstmt.setBoolean(8, Toggles.getToggleStatus(player, "Debug"));
            pstmt.setBoolean(9, Toggles.getToggleStatus(player, "Hologram Damage"));
            pstmt.setBoolean(10, Toggles.getToggleStatus(player, "Level HP"));
            pstmt.setBoolean(11, Toggles.getToggleStatus(player, "Glow Drops"));
            pstmt.setBoolean(12, Toggles.getToggleStatus(player, "Player Messages"));
            pstmt.setBoolean(13, Toggles.getToggleStatus(player, "Trading"));
            pstmt.setBoolean(14, Toggles.getToggleStatus(player, "Gems"));
            pstmt.setBoolean(15, Toggles.getToggleStatus(player, "Trail"));
            pstmt.setBoolean(16, Toggles.getToggleStatus(player, "Drop Protection"));
            pstmt.setBoolean(17, Toggles.getToggleStatus(player, "Disable Kit"));
            pstmt.setInt(18, pp.tokens);
            pstmt.setInt(19, pp.mount);
            pstmt.setInt(20, pp.bankpages);
            pstmt.setInt(21, pp.pickaxe);
            pstmt.setInt(22, pp.farmer);
            pstmt.setInt(23, pp.laststand);
            pstmt.setInt(24, pp.orbrolls);
            pstmt.setInt(25, pp.luck);
            pstmt.setInt(26, pp.reaper);
            pstmt.setInt(27, pp.kitweapon);
            pstmt.setInt(28, pp.kithelm);
            pstmt.setInt(29, pp.kitchest);
            pstmt.setInt(30, pp.kitlegs);
            pstmt.setInt(31, pp.kitboots);

            pstmt.executeUpdate();
            PracticeServer.log.info("[RetroDB] Saved Persistent Data for " + player.getName());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ResultSet getPlayerData(String table, String columns, Player player) {
        if (!PracticeServer.DATABASE) {
            return null;
        }

        try (PreparedStatement pstmt = con.prepareStatement("SELECT " + columns + " FROM " + table + " WHERE UUID = ?")) {
            pstmt.setString(1, player.getUniqueId().toString());
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
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
            String stats = "INSERT INTO Guilds (GuildName, GuildTag, GuildMOTD, Owner, Officers, Members)" +
                    "VALUES (?, ?, ?, ?, ?, ?)";
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
        try (ResultSet rs = con.createStatement().executeQuery("SELECT * FROM Guilds")) {
            while (rs.next()) {
                Guild guild = new Guild(rs.getString("GuildName"));
                guild.setTag(rs.getString("GuildTag"));
                guild.setMotd(rs.getString("GuildMOTD"));
                guild.setOwner(UUID.fromString(rs.getString("Owner")));
                guild.getPlayerRoleMap().put(UUID.fromString(rs.getString("Owner")), Role.LEADER);
                for (String s : rs.getString("Officers").split(",")) {
                    if (!s.isEmpty()) guild.getPlayerRoleMap().put(UUID.fromString(s), Role.OFFICER);
                }
                for (String s : rs.getString("Members").split(",")) {
                    if (!s.isEmpty()) guild.getPlayerRoleMap().put(UUID.fromString(s), Role.MEMBER);
                }
                GuildManager.guildMap.put(guild.getName(), guild);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void deleteGuild(Guild guild) {
        try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM Guilds WHERE GuildName = ?")) {
            pstmt.setString(1, guild.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveBank(Inventory inv, UUID uuid, int page) {
        if (PracticeServer.DATABASE) {
            String table = getTableName(page);
            String items = BukkitSerialization.itemStackArrayToBase64(inv.getContents());
            String query = "INSERT INTO " + table + " (UUID, Username, Inventory) VALUES (?, ?, ?)";
            try (PreparedStatement statement = con.prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                statement.setString(2, Bukkit.getOfflinePlayer(uuid).getName());
                statement.setString(3, items);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveGuildBank(Inventory inv, Guild guild) {
        if (PracticeServer.DATABASE) {
            String items = BukkitSerialization.itemStackArrayToBase64(inv.getContents());
            String stats = "INSERT INTO GuildBanks (GuildName, GuildBank, Mutex, OccupiedBy) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(stats)) {
                pstmt.setString(1, guild.getName());
                pstmt.setString(2, items);
                pstmt.setBoolean(3, false);
                pstmt.setString(4, "None");
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Inventory getBank(UUID uuid, int page) {
        if (page < 1 || page > 5)
            return null;
        String table = getTableName(page);
        PersistentPlayer pp = PersistentPlayers.get(uuid);
        Inventory inv = Bukkit.createInventory(null, Banks.banksize, "Bank Chest (" + page + "/" + pp.bankpages + ")");
        try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM " + table + " WHERE UUID = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(rs.getString("Inventory"));
                inv.setContents(items);
            } else {
                return inv;
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return inv;
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
                        PreparedStatement stmt2 = con.prepareStatement(
                                "INSERT INTO PlayerData (UUID, Username, XCoord, YCoord, ZCoord, Yaw, Pitch, Inventory, Armor, MaxHP, Gems, GuildName, Alignment, AlignTime, T1Kills, T2Kills, T3Kills, T4Kills, T5Kills, T6Kills, Deaths, PlayerKills, OreMined, ChestsOpened, RespawnData) "
                                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
        if (PracticeServer.DATABASE) {
            try {
                return con.createStatement().executeQuery("SELECT " + columns + " FROM " + table);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        if (PracticeServer.DATABASE) {
            PracticeServer.log.info("[RetroDB] has been Enabled");
            try {
                Class.forName("org.postgresql.Driver");

                Properties props = new Properties();
                FileInputStream in = new FileInputStream(PracticeServer.plugin.getDataFolder() + "/db.properties");
                props.load(in);
                in.close();

                String url = props.getProperty("jdbc.url");
                String username = props.getProperty("jdbc.username");
                String password = props.getProperty("jdbc.password");

                // Set connection properties using the Properties object
                Properties connectionProps = new Properties();
                connectionProps.put("user", username);
                connectionProps.put("password", password);

                con = DriverManager.getConnection(url, connectionProps);
                System.out.println("[RetroDB] Connection Established");

                new BukkitRunnable() {
                    public void run() {
                        try {
                            if (con.isClosed()) {
                                con = DriverManager.getConnection(url, connectionProps);
                                System.out.println("[RetroDB] Connection Reestablished");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }.runTaskTimer(PracticeServer.plugin, 20, 6000);

                SQLCreate.createTables(con);
            } catch (Exception e) {
                e.printStackTrace();
            }

            loadPersistentData();
            loadGems();
        }
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

    @EventHandler
    void onLogout(PlayerQuitEvent e) {
        updatePlayerStats(e.getPlayer());
        updatePersistentStats(e.getPlayer());
    }

    @EventHandler
    void onKick(PlayerKickEvent e) {
        updatePlayerStats(e.getPlayer());
        updatePersistentStats(e.getPlayer());
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent e) {
        if (!PracticeServer.DATABASE) {
            return;
        }

        Player player = e.getPlayer();
        try (
                PreparedStatement stmt1 = con.prepareStatement("SELECT * FROM PersistentData WHERE Username = ?");
                PreparedStatement stmt2 = con.prepareStatement("SELECT * FROM PlayerData WHERE UUID = ?")
        ) {
            stmt1.setString(1, player.getName());
            stmt2.setString(1, player.getUniqueId().toString());
            ResultSet rs1 = stmt1.executeQuery();
            ResultSet rs2 = stmt2.executeQuery();
            handlePersistentData(player, rs1);
            handlePlayerData(player, rs2);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void handlePersistentData(Player player, ResultSet rs) throws SQLException {
        if (rs.next()) {
            if (ModerationMechanics.rankHashMap.get(player.getUniqueId()) != null) {
                String playerRank = rs.getString("PlayerRank");
                if (RankEnum.fromString(playerRank) != RankEnum.DEFAULT) {
                    ModerationMechanics.rankHashMap.put(player.getUniqueId(), RankEnum.fromString(playerRank));
                }
            }
            int mount = rs.getInt("Mount") + 1;
            Horses.horseTier.put(player, mount);
            getTogglesFromSQL(player.getUniqueId(), rs);
        } else {
            ModerationMechanics.rankHashMap.put(player.getUniqueId(), RankEnum.DEFAULT);
            Horses.horseTier.put(player, 0);
        }
    }

    private void handlePlayerData(Player player, ResultSet rs) throws SQLException, IOException {
        if (rs.next()) {
            String guildName = rs.getString("GuildName");
            int playerKills = rs.getInt("PlayerKills");
            int t1Kills = rs.getInt("T1Kills");
            int t2Kills = rs.getInt("T2Kills");
            int t3Kills = rs.getInt("T3Kills");
            int t4Kills = rs.getInt("T4Kills");
            int t5Kills = rs.getInt("T5Kills");
            int t6Kills = rs.getInt("T6Kills");
            int lootChestsOpen = rs.getInt("ChestsOpened");
            int deaths = rs.getInt("Deaths");
            int oreMined = rs.getInt("OreMined");
            GuildPlayers.add(new GuildPlayer(player.getUniqueId(), player.getName(), guildName, playerKills, t1Kills,
                    t2Kills, t3Kills, t4Kills, t5Kills, t6Kills, lootChestsOpen, oreMined, deaths, 0));
            int horsetier = rs.getInt("HorseTier");
            if (horsetier > Horses.horseTier.get(player)) {
                Horses.horseTier.put(player, horsetier);
            }
            String alignment = rs.getString("Alignment");
            int aligntime = rs.getInt("AlignTime");
            if (alignment.contains("CHAOTIC")) {
                Alignments.chaotic.put(player.getName(), aligntime);
            }
            if (alignment.contains("NEUTRAL")) {
                Alignments.neutral.put(player.getName(), aligntime);
            }
            player.getInventory().clear();
            ItemStack[] is = BukkitSerialization.itemStackArrayFromBase64(rs.getString("Inventory"));
            player.getInventory().setContents(is);
            ItemStack[] as = BukkitSerialization.itemStackArrayFromBase64(rs.getString("Armor"));
            player.getInventory().setArmorContents(as);
            double xcoord = rs.getFloat("XCoord");
            double ycoord = rs.getFloat("YCoord");
            double zcoord = rs.getFloat("ZCoord");
            float yaw = rs.getFloat("Yaw");
            float pitch = rs.getFloat("Pitch");
            player.teleport(new Location(player.getWorld(), xcoord, ycoord, zcoord, yaw, pitch));
        } else {
            GuildPlayers.add(
                    new GuildPlayer(player.getUniqueId(), player.getName(), "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            player.getInventory().clear();
            Listeners.Kit(player);
            try {
                new BukkitRunnable() {
                    public void run() {
                        if (ModerationMechanics.isDonator(player)) {
                            Items.giveDonorItems(player);
                        }
                    }
                }.runTaskLater(PracticeServer.getInstance(), 80L);
            } catch (Exception ex) {
                // Handle the exception appropriately, e.g., log it
            }
            player.teleport(TeleportBooks.stonePeaks);
        }
    }

    public void getTogglesFromSQL(UUID uuid, ResultSet rs) throws SQLException {
        ArrayList<String> toggles = new ArrayList<>();
        String[] toggleColumns = {"LVLHPToggle", "PVPToggle", "ChaoToggle", "FFToggle",
                "DebugToggle", "HologramToggle", "GlowToggle", "PMToggle",
                "TradingToggle", "GemsToggle", "TrailToggle", "DropToggle", "KitToggle"};

        for (String column : toggleColumns) {
            if (rs.getBoolean(column)) {
                toggles.add(getToggleNameFromColumn(column));
            }
        }

        Toggles.toggles.put(uuid, toggles);
    }

    private String getToggleNameFromColumn(String column) {
        switch (column) {
            case "LVLHPToggle":
                return "Level HP";
            case "PVPToggle":
                return "Anti PVP";
            case "ChaoToggle":
                return "Chaotic";
            case "FFToggle":
                return "Friendly Fire";
            case "DebugToggle":
                return "Debug";
            case "HologramToggle":
                return "Hologram Damage";
            case "GlowToggle":
                return "Glow Drops";
            case "PMToggle":
                return "Player Messages";
            case "TradingToggle":
                return "Trading";
            case "GemsToggle":
                return "Gems";
            case "TrailToggle":
                return "Trail";
            case "DropToggle":
                return "Drop Protection";
            case "KitToggle":
                return "Disable Kit";
            default:
                return "";
        }
    }

    void loadPersistentData() {
        if (PracticeServer.DATABASE) {
            try {
                ResultSet rs = SQLMain.getPlayerData("PersistentData", "*");
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("UUID"));
                    ArrayList<String> toggle = new ArrayList<String>();
                    getTogglesFromSQL(uuid, rs);
                    ModerationMechanics.rankHashMap.put(uuid, RankEnum.fromString(rs.getString("PlayerRank")));
                    Toggles.toggles.put(uuid, toggle);
                    ArrayList<String> buddies = new ArrayList<>();
                    for (String s : rs.getString("Buddies").split(",")) {
                        if (s.length() > 30)
                            buddies.add(s);
                    }
                    Buddies.buddies.put(rs.getString("Username"), buddies);
                    Integer tokens = rs.getInt("Tokens");
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
                    if (tokens == null || kitweapon < 1) {
                        if (!PersistentPlayers.persistentPlayers.containsKey(uuid))
                            PersistentPlayers.put(uuid,
                                    new PersistentPlayer(50, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0));
                    } else {
                        PersistentPlayers.put(uuid,
                                new PersistentPlayer(tokens, mount, pickaxe, farmer, laststand, bankpages, orbrolls,
                                        luck, reaper, kitweapon, kithelm, kitchest, kitlegs, kitboots));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    void loadGems() {
        if (PracticeServer.DATABASE) {
            ResultSet rs = SQLMain.getPlayerData("PlayerData", "UUID, Gems");
            try {
                while (rs.next()) {
                    int gems = rs.getInt("Gems");
                    UUID uuid = UUID.fromString(rs.getString("UUID"));
                    Economy.currentBalance.put(uuid, gems);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
