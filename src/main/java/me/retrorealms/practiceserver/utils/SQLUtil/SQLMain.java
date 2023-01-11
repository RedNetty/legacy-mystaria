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
import java.sql.*;
import java.util.*;

public class SQLMain implements Listener {
    public static Connection con;

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        if (PracticeServer.DATABASE) {
            PracticeServer.log.info("[RetroDB] has been Enabled");
            try {
                Properties props = new Properties();
                FileInputStream in = new FileInputStream(PracticeServer.plugin.getDataFolder() + "/db.properties");
                props.load(in);
                in.close();

                String driver = props.getProperty("jdbc.driver");
                if (driver != null) {
                    Class.forName(driver);
                }
                String url = props.getProperty("jdbc.url");
                String username = props.getProperty("jdbc.username");
                String password = props.getProperty("jdbc.password");
                con = DriverManager.getConnection(url, username, password);
                System.out.println("[RetroDB] Connection Established");
                new BukkitRunnable() {

                    public void run() {
                        try {
                            con = DriverManager.getConnection(url, username, password);
                            System.out.println("[RetroDB] Connection Established");
                        } catch (Exception e) {
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
        for (Player p : Bukkit.getOnlinePlayers()) {
            updatePlayerStats(p);
            updatePersistentStats(p);
        }
        PracticeServer.log.info("[RetroDB] has been Disabled");
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



    public static void updateGuild(UUID uuid, String guildName) {
        if(PracticeServer.DATABASE) {
            try {
                String query = "UPDATE PlayerData" +
                        " SET GuildName ='" + guildName + "' WHERE UUID = '" + uuid + "';";
                con.createStatement().execute(query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateRespawnData(Player p, List<ItemStack> items) {
        if(PracticeServer.DATABASE) {
            updatePlayerStats(p);
            int i = 0;
            ItemStack[] itemArray = new ItemStack[items.size() + 1];
            for (ItemStack item : items) {
                itemArray[i] = item;
                i++;
            }
            try {
                String finalItems = BukkitSerialization.itemStackArrayToBase64(itemArray);
                String query = "UPDATE PlayerData" +
                        " SET RespawnData = '" + finalItems + "' WHERE UUID = '" + p.getUniqueId() + "';";
                if (finalItems != null && finalItems.length() > 10) con.createStatement().execute(query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadRespawnData(Player p) {
        if(PracticeServer.DATABASE) {
            ResultSet rs = getPlayerData("PlayerData", "RespawnData", p);
            try {
                if (rs.next()) {
                    ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(rs.getString("RespawnData"));
                    for (ItemStack is : items) {
                        if (is != null) p.getInventory().addItem(is);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean updatePlayerStats(Player p) {
        if(PracticeServer.DATABASE) {
            UUID uuid = p.getUniqueId();
            String[] pinv = BukkitSerialization.playerInventoryToBase64(p.getInventory());
            GuildPlayers gp = GuildPlayers.getInstance();
            try {
                ResultSet rs = getPlayerData("PlayerData", "RespawnData", p);
                String respawnData = "";
                if (rs.next()) {
                    respawnData = rs.getString("RespawnData");
                }
                Location loc = p.getLocation();
                String stats = "REPLACE INTO PlayerData (UUID, Username, XCoord, YCoord, ZCoord, Yaw, Pitch, " +
                        "Inventory, Armor, MaxHP, Gems, GuildName, Alignment, " +
                        "AlignTime, HorseTier, T1Kills, T2Kills, T3Kills, T4Kills, T5Kills, T6Kills, " +
                        "Deaths, PlayerKills, OreMined, ChestsOpened, RespawnData) " +
                        "VALUES ('" + uuid.toString() + "'," +
                        " '" + p.getName() + "'," +
                        loc.getX() + "," +
                        loc.getY() + "," +
                        loc.getZ() + "," +
                        loc.getYaw() + "," +
                        loc.getPitch() + "," +
                        " '" + pinv[0] + "'," +
                        " '" + pinv[1] + "'," +
                        p.getMaxHealth() + "," +
                        Economy.getBalance(uuid) + "," +
                        " '" + gp.get(uuid).getGuildName() + "'," +
                        " '" + Alignments.get(p) + "'," +
                        Alignments.getAlignTime(p) + "," +
                        Horses.horseTier.get(p) + "," +
                        gp.get(uuid).getT1Kills() + "," +
                        gp.get(uuid).getT2Kills() + "," +
                        gp.get(uuid).getT3Kills() + "," +
                        gp.get(uuid).getT4Kills() + "," +
                        gp.get(uuid).getT5Kills() + "," +
                        gp.get(uuid).getT6Kills() + "," +
                        gp.get(uuid).getDeaths() + "," +
                        gp.get(uuid).getPlayerKills() + "," +
                        gp.get(uuid).getOreMined() + "," +
                        gp.get(uuid).getLootChestsOpen() + "," +
                        " '" + respawnData + "');";
                con.createStatement().execute(stats);
                PracticeServer.log.info("[RetroDB] Saved Player Data for " + p.getName());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static boolean updatePersistentStats(Player p) {
        if (PracticeServer.DATABASE) {
            UUID uuid = p.getUniqueId();
            PersistentPlayer pp = PersistentPlayers.get(uuid);
            String buddies = "";
            if (Buddies.buddies.get(p.getName()) != null) {
                for (String s : Buddies.buddies.get(p.getName())) {
                    buddies += s;
                    buddies += ",";
                }
            }

            String stats = "REPLACE INTO PersistentData (UUID, Username, Rank, Buddies, PVPToggle, ChaoToggle, FFToggle, DebugToggle, HologramToggle, " +
                    "LVLHPToggle, GlowToggle, PMToggle, TradingToggle, GemsToggle, TrailToggle, DropToggle, KitToggle, " +
                    "Tokens, Mount, BankPages, Pickaxe, Farmer, LastStand, OrbRolls, Luck, Reaper, KitWeapon, KitHelm, KitChest, KitLegs, KitBoots)" +
                    "VALUES ('" + uuid.toString() + "'," +
                    " '" + p.getName() + "'," +
                    " '" + RankEnum.enumToString(ModerationMechanics.getRank(p)) + "'," +
                    " '" + buddies + "'," +
                    Toggles.getToggleStatus(p, "Anti PVP") + "," +
                    Toggles.getToggleStatus(p, "Chaotic") + "," +
                    Toggles.getToggleStatus(p, "Friendly Fire") + "," +
                    Toggles.getToggleStatus(p, "Debug") + "," +
                    Toggles.getToggleStatus(p, "Hologram Damage") + "," +
                    Toggles.getToggleStatus(p, "Level HP") + "," +
                    Toggles.getToggleStatus(p, "Glow Drops") + "," +
                    Toggles.getToggleStatus(p, "Player Messages") + "," +
                    Toggles.getToggleStatus(p, "Trading") + "," +
                    Toggles.getToggleStatus(p, "Gems") + "," +
                    Toggles.getToggleStatus(p, "Trail") + "," +
                    Toggles.getToggleStatus(p, "Drop Protection") + "," +
                    Toggles.getToggleStatus(p, "Disable Kit") + "," +
                    +pp.tokens + "," +
                    +pp.mount + "," +
                    +pp.bankpages + "," +
                    +pp.pickaxe + "," +
                    +pp.farmer + "," +
                    +pp.laststand + "," +
                    +pp.orbrolls + "," +
                    +pp.luck + "," +
                    +pp.reaper + "," +
                    +pp.kitweapon + "," +
                    +pp.kithelm + "," +
                    +pp.kitchest + "," +
                    +pp.kitlegs + "," +
                    +pp.kitboots +
                    ");";
            try {
                con.createStatement().execute(stats);
                PracticeServer.log.info("[RetroDB] Saved Persistent Data for " + p.getName());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }
        return false;
    }

    public static ResultSet getPlayerData(String table, String columns) {
        if(PracticeServer.DATABASE) {
            try {
                return con.createStatement().executeQuery("SELECT " + columns + " FROM " + table);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }

    public static ResultSet getPlayerData(String table, String columns, Player p) {
        if(PracticeServer.DATABASE) {
            try {
                return con.createStatement().executeQuery("SELECT " + columns + " FROM " + table + " WHERE UUID = '" + p.getUniqueId() + "'");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent e) {
        if(PracticeServer.DATABASE) {
            Player p = e.getPlayer();
            ResultSet rs = getPlayerData("PersistentData", "Rank, Mount", p);
            try {
                boolean donor = false;
                if (rs.next()) {
                    ModerationMechanics.rankHashMap.put(p.getUniqueId(), RankEnum.fromString(rs.getString("Rank")));
                    int mount = rs.getInt("Mount") + 1;
                    Horses.horseTier.put(p, mount);
                    if(ModerationMechanics.rankHashMap.get(p.getUniqueId()) == RankEnum.SUPPORTER) {
                        donor = true;
                    }
                } else {
                    ModerationMechanics.rankHashMap.put(p.getUniqueId(), RankEnum.DEFAULT);
                    Horses.horseTier.put(p, 0);
                }
                rs = getPlayerData("PlayerData", "*", p);
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
                    GuildPlayers.add(new GuildPlayer(p.getUniqueId(), p.getName(), guildName, playerKills, t1Kills, t2Kills, t3Kills, t4Kills, t5Kills, t6Kills, lootChestsOpen, oreMined, deaths, 0));
                    int horsetier = rs.getInt("HorseTier");
                    if (horsetier > Horses.horseTier.get(p)) Horses.horseTier.put(p, horsetier);
                    String alignment = rs.getString("Alignment");
                    int aligntime = rs.getInt("AlignTime");
                    if (alignment.contains("CHAOTIC")) Alignments.chaotic.put(p.getName(), aligntime);
                    if (alignment.contains("NEUTRAL")) Alignments.neutral.put(p.getName(), aligntime);
                    p.getInventory().clear();
                    ItemStack[] is = BukkitSerialization.itemStackArrayFromBase64(rs.getString("Inventory"));
                    p.getInventory().setContents(is);
                    ItemStack[] as = BukkitSerialization.itemStackArrayFromBase64(rs.getString("Armor"));
                    p.getInventory().setArmorContents(as);
                    double xcoord = rs.getInt("XCoord");
                    double ycoord = rs.getInt("YCoord");
                    double zcoord = rs.getInt("ZCoord");
                    float yaw = rs.getInt("Yaw");
                    float pitch = rs.getInt("Pitch");
                    p.teleport(new Location(p.getWorld(), xcoord, ycoord, zcoord, yaw, pitch));
                } else {
                    GuildPlayers.add(new GuildPlayer(p.getUniqueId(), p.getName(), "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
                    p.getInventory().clear();
                    Listeners.Kit(p);
                    if(donor) p.getInventory().addItem(Listeners.donorPick());
                    p.teleport(TeleportBooks.DeadPeaks);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    void loadPersistentData() {
        if(PracticeServer.DATABASE) {
            ResultSet rs = SQLMain.getPlayerData("PersistentData", "*");
            try {
                while (rs.next()) {
                    ArrayList<String> toggle = new ArrayList<String>();
                    UUID uuid = UUID.fromString(rs.getString("UUID"));
                    if (rs.getBoolean("PVPToggle")) toggle.add("Anti PvP");
                    if (rs.getBoolean("ChaoToggle")) toggle.add("Chaotic");
                    if (rs.getBoolean("FFToggle")) toggle.add("Friendly Fire");
                    if (rs.getBoolean("DebugToggle")) toggle.add("Debug");
                    if (rs.getBoolean("HologramToggle")) toggle.add("Hologram Damage");
                    if (rs.getBoolean("LVLHPToggle")) toggle.add("Level HP");
                    if (rs.getBoolean("GlowToggle")) toggle.add("Glow Drops");
                    if (rs.getBoolean("PMToggle")) toggle.add("Player Messages");
                    if (rs.getBoolean("TradingToggle")) toggle.add("Trading");
                    if (rs.getBoolean("GemsToggle")) toggle.add("Gems");
                    if (rs.getBoolean("TrailToggle")) toggle.add("Trail");
                    if (rs.getBoolean("DropToggle")) toggle.add("Drop Protection");
                    if (rs.getBoolean("KitToggle")) toggle.add("Disable Kit");
                    ModerationMechanics.rankHashMap.put(uuid, RankEnum.fromString(rs.getString("Rank")));
                    Toggles.toggles.put(uuid, toggle);
                    ArrayList<String> buddies = new ArrayList<>();
                    for (String s : rs.getString("Buddies").split(",")) {
                        if (s.length() > 30) buddies.add(s);
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
                            PersistentPlayers.put(uuid, new PersistentPlayer(50, 0, 0, 0, 0, 1,
                                    0, 0, 0, 1, 0, 0, 0, 0));
                    } else {
                        PersistentPlayers.put(uuid, new PersistentPlayer(tokens, mount, pickaxe, farmer, laststand, bankpages,
                                orbrolls, luck, reaper, kitweapon, kithelm, kitchest, kitlegs, kitboots));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void loadGems() {
        if(PracticeServer.DATABASE) {
            ResultSet rs = SQLMain.getPlayerData("PlayerData", "UUID, Gems");
            try {
                while (rs.next()) {
                    int gems = rs.getInt("Gems");
                    UUID uuid = UUID.fromString(rs.getString("UUID"));
                    Economy.currentBalance.put(uuid, gems);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            String stats = "REPLACE INTO Guilds (GuildName, GuildTag, GuildMOTD, Owner, Officers, Members)" +
                    "VALUES ('" + guild.getName() + "'," +
                    " '" + guild.getTag() + "'," +
                    " '" + guild.getMotd() + "'," +
                    " '" + guild.getOwner().toString() + "'," +
                    " '" + officers + "'," +
                    " '" + members + "');";
            try {
                con.createStatement().execute(stats);
                PracticeServer.log.info("[RetroDB] Saved Guild Data for " + guild.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadGuilds() {
        if(PracticeServer.DATABASE) {
            try {
                ResultSet rs = con.createStatement().executeQuery("select * from Guilds");
                while (rs.next()) {
                    Guild guild = new Guild(rs.getString("GuildName"));
                    guild.setTag(rs.getString("GuildTag"));
                    guild.setMotd(rs.getString("GuildMOTD"));
                    guild.setOwner(UUID.fromString(rs.getString("Owner")));
                    guild.getPlayerRoleMap().put(UUID.fromString(rs.getString("Owner")), Role.LEADER);
                    for (String s : rs.getString("Officers").split(",")) {
                        if (s.length() > 30) guild.getPlayerRoleMap().put(UUID.fromString(s), Role.OFFICER);
                    }
                    for (String s : rs.getString("Members").split(",")) {
                        if (s.length() > 30) guild.getPlayerRoleMap().put(UUID.fromString(s), Role.MEMBER);
                    }
                    GuildManager.guildMap.put(guild.getName(), guild);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteGuild(Guild guild) {
        if(PracticeServer.DATABASE) {
            try {
                con.createStatement().execute("DELETE FROM Guilds WHERE GuildName ='" + guild.getName() + "'");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveBank(Inventory inv, UUID uuid, int page) {
        if(PracticeServer.DATABASE) {
            String table = getTableName(page);
            String items = BukkitSerialization.itemStackArrayToBase64(inv.getContents());
            String stats = "REPLACE INTO " + table + " (UUID, Username, Inventory)" +
                    "VALUES ('" + uuid + "'," +
                    " '" + Bukkit.getOfflinePlayer(uuid).getName() + "'," +
                    " '" + items + "');";
            try {
                con.createStatement().execute(stats);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveGuildBank(Inventory inv, Guild guild) {
        if(PracticeServer.DATABASE) {
            String items = BukkitSerialization.itemStackArrayToBase64(inv.getContents());
            String stats = "REPLACE INTO GuildBanks (GuildName, GuildBank, Mutex, OccupiedBy)" +
                    "VALUES ('" + guild.getName() + "'," +
                    " '" + items + "'," +
                    false + "," +
                    "'None');";
            try {
                con.createStatement().execute(stats);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Inventory getBank(UUID uuid, int page) {

        if(page < 1 || page > 5) return null;
        String table = getTableName(page);
        PersistentPlayer pp = PersistentPlayers.get(uuid);
        Inventory inv = Bukkit.createInventory(null, Banks.banksize, "Bank Chest (" + page + "/" + pp.bankpages + ")");
        try {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM "+ table + " WHERE UUID ='" + uuid.toString() + "'");
            if (rs.next()) {
                ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(rs.getString("Inventory"));
                inv.setContents(items);
            } else {
                return inv;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inv;
    }

    public static String getTableName(int page){
        switch(page){
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
            default: return "Banks";
        }
    }

    public static Inventory getGuildBank(Player p, Guild guild) {

        Inventory inv = Bukkit.createInventory(null, GuildBank.guildBankSize, "Guild Bank Chest (1/1)");
        try {
            con.createStatement().execute("UPDATE GuildBanks SET OccupiedBy = '" + p.getName() + "', Mutex = 1 WHERE GuildName = '" + guild.getName() + "' AND Mutex = 0");
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM GuildBanks WHERE GuildName ='" + guild.getName() + "'");
            if (rs.next()) {
                if (rs.getString("OccupiedBy").equals(p.getName())) {
                    ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(rs.getString("GuildBank"));
                    inv.setContents(items);
                } else {
                    p.sendMessage("Guild Bank Occupied by " + rs.getString("OccupiedBy"));
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inv;
    }

    public static void updateRank(Player p) {
        if(PracticeServer.DATABASE) {
            try {
                String query = "UPDATE PersistentData" +
                        " SET Rank = '" + RankEnum.enumToString(ModerationMechanics.getRank(p)) + "' WHERE UUID = '" + p.getUniqueId() + "';";
                con.createStatement().execute(query);
                System.out.println("[RetroDB] Updated Rank for " + p.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clonePlayer(Player p, String target) {
        try {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM PlayerData WHERE Username='" + target + "'");
            if (rs.next()) {
                p.kickPlayer(ChatColor.GREEN + "Cloning " + target);
                new AsyncTask(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    con.createStatement().execute(
                                            "REPLACE INTO PlayerData (UUID, Username, XCoord, YCoord, ZCoord, Yaw, Pitch, Inventory, Armor, MaxHP, Gems, GuildName, Alignment, " +
                                                    "AlignTime, T1Kills, T2Kills, T3Kills, T4Kills, T5Kills, T6Kills, Deaths, PlayerKills, OreMined, ChestsOpened, RespawnData)" +
                                                    "VALUES (" +
                                                    "'" + p.getUniqueId() + "'," +
                                                    "'" + p.getName() + "'," +
                                                    rs.getInt("XCoord") + "," +
                                                    rs.getInt("YCoord") + "," +
                                                    rs.getInt("ZCoord") + "," +
                                                    rs.getInt("Yaw") + "," +
                                                    rs.getInt("Pitch") + "," +
                                                    "'" + rs.getString("Inventory") + "'," +
                                                    "'" + rs.getString("Armor") + "'," +
                                                    rs.getInt("MaxHP") + "," +
                                                    rs.getInt("Gems") + "," +
                                                    "'" + rs.getString("GuildName") + "'," +
                                                    "'" + rs.getString("Alignment") + "'," +
                                                    rs.getInt("AlignTime") + "," +
                                                    rs.getInt("T1Kills") + "," +
                                                    rs.getInt("T2Kills") + "," +
                                                    rs.getInt("T3Kills") + "," +
                                                    rs.getInt("T4Kills") + "," +
                                                    rs.getInt("T5Kills") + "," +
                                                    rs.getInt("T6Kills") + "," +
                                                    rs.getInt("Deaths") + "," +
                                                    rs.getInt("PlayerKills") + "," +
                                                    rs.getInt("OreMined") + "," +
                                                    rs.getInt("ChestsOpened") + "," +
                                                    "'" + rs.getString("RespawnData") + "')");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                ).setDelay(2).scheduleDelayedTask();
            }else{
                p.sendMessage(ChatColor.RED + "Player does not exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
