package me.retrorealms.practiceserver;

import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;

import me.retrorealms.practiceserver.commands.items.*;
import me.retrorealms.practiceserver.commands.misc.*;
import me.retrorealms.practiceserver.commands.moderation.*;
import me.retrorealms.practiceserver.commands.toggles.*;
import me.retrorealms.practiceserver.mechanics.altars.Altar;
import me.retrorealms.practiceserver.mechanics.drops.Mobdrops;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.loot.LootChests;
import me.retrorealms.practiceserver.mechanics.mobs.elite.GolemElite;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBossHandler;
import me.retrorealms.practiceserver.mechanics.player.*;
import me.retrorealms.practiceserver.mechanics.pvp.Deadman;
import me.retrorealms.practiceserver.mechanics.vendors.*;
import me.retrorealms.practiceserver.utils.CustomFilter;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.retrorealms.practiceserver.apis.API;
import me.retrorealms.practiceserver.apis.files.MarketData;
import me.retrorealms.practiceserver.apis.files.PlayerData;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.apis.tab.TabMenu;
import me.retrorealms.practiceserver.commands.buddy.AddCommand;
import me.retrorealms.practiceserver.commands.buddy.DeleteCommand;
import me.retrorealms.practiceserver.commands.chat.ChatTagCommand;
import me.retrorealms.practiceserver.commands.chat.GlobalCommand;
import me.retrorealms.practiceserver.commands.chat.MessageCommand;
import me.retrorealms.practiceserver.commands.chat.ReplyCommand;
import me.retrorealms.practiceserver.commands.duels.DuelAcceptCommand;
import me.retrorealms.practiceserver.commands.duels.DuelCommand;
import me.retrorealms.practiceserver.commands.duels.DuelQuitCommand;
import me.retrorealms.practiceserver.commands.duels.PartyDuelAcceptCommand;
import me.retrorealms.practiceserver.commands.duels.PartyDuelCommand;
import me.retrorealms.practiceserver.commands.guilds.GuildWipeAllCommand;
import me.retrorealms.practiceserver.commands.party.PAcceptCommand;
import me.retrorealms.practiceserver.commands.party.PDeclineCommand;
import me.retrorealms.practiceserver.commands.party.PInviteCommand;
import me.retrorealms.practiceserver.commands.party.PKickCommand;
import me.retrorealms.practiceserver.commands.party.PQuitCommand;
import me.retrorealms.practiceserver.commands.party.PartyCommand;
import me.retrorealms.practiceserver.manager.ManagerHandler;
import me.retrorealms.practiceserver.mechanics.chat.ChatMechanics;
import me.retrorealms.practiceserver.mechanics.chat.gui.ChatTagGUIHandler;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.damage.Staffs;
import me.retrorealms.practiceserver.mechanics.donations.Crates.CratesMain;
import me.retrorealms.practiceserver.mechanics.donations.Nametags.Nametag;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.PickTrak;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.WepTrak;
import me.retrorealms.practiceserver.mechanics.drops.DropPriority;
import me.retrorealms.practiceserver.mechanics.drops.buff.BuffHandler;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.enchants.Enchants;
import me.retrorealms.practiceserver.mechanics.enchants.Orbs;
import me.retrorealms.practiceserver.mechanics.guilds.GuildMechanics;
import me.retrorealms.practiceserver.mechanics.item.Durability;
import me.retrorealms.practiceserver.mechanics.item.Repairing;
import me.retrorealms.practiceserver.mechanics.item.Untradeable;
import me.retrorealms.practiceserver.mechanics.item.betavendor.Vendor;
import me.retrorealms.practiceserver.mechanics.item.scroll.ScrollGUIHandler;
import me.retrorealms.practiceserver.mechanics.market.MarketHandler;
import me.retrorealms.practiceserver.mechanics.mobs.Mobs;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import me.retrorealms.practiceserver.mechanics.mobs.elite.SkeletonElite;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.Banks;
import me.retrorealms.practiceserver.mechanics.money.GemPouches;
import me.retrorealms.practiceserver.mechanics.money.Economy.Economy;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.patch.PatchIO;
import me.retrorealms.practiceserver.mechanics.patch.PatchListener;
import me.retrorealms.practiceserver.mechanics.player.GamePlayer.GamePlayer;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Elytras;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.profession.Enchanter;
import me.retrorealms.practiceserver.mechanics.profession.Fishing;
import me.retrorealms.practiceserver.mechanics.profession.Mining;
import me.retrorealms.practiceserver.mechanics.profession.ProfessionMechanics;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.pvp.ForceField;
import me.retrorealms.practiceserver.mechanics.pvp.Respawn;
import me.retrorealms.practiceserver.mechanics.shard.Shard;
import me.retrorealms.practiceserver.mechanics.teleport.Hearthstone;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import me.retrorealms.practiceserver.mechanics.useless.command.CommandStart;
import me.retrorealms.practiceserver.mechanics.world.Antibuild;
import me.retrorealms.practiceserver.mechanics.world.Logout;
import me.retrorealms.practiceserver.mechanics.world.region.RegionHandler;
import me.retrorealms.practiceserver.utils.ArmorListener;

/**
 *
 * @author Jaxson (Red29 - uncureableAutism@outlook.com)
 * @author Giovanni N. (VawkeNetty - development@vawke.io)
 * @author Subby (Availor - Haven't learnt any java but is already fluent???)
 *         (xxbboy)
 *         <p>
 *         Original Authors -> - I Can't Code (BPWeber - Naughty, Naughty,
 *         Naughty) - Randal Gay Boy (iFamasssRAWRxD - Hentai, Hentai, Hentai)
 *         <p>
 *         Updated to Minecraft 1.9 -> - Written by Giovanni (VawkeNetty) 2017.
 *         - Written by Jaxson (Red29) 2016/2017.
 *         <p>
 *         Development continued by -> - Written by Jaxson (Red29) 2016/2017. -
 *         Written by Brandon (Kayaba) 2017. (Big scammer) (Stole $2k from
 *         Jaxson)
 *         <p>
 *         --------------- From Feb 2017 to May 2017 -------------- Server
 *         offline --------------------- - Written by Giovanni N. (VawkeNetty)
 *         2017-2017. (April 30th to August 1st) - Subby (Availor) April 30th
 *         2017 - ??? (Brought back AR with Giovanni). - Jaxson (Red) (HES BACK)
 *         (HE LEFT AGAIN) 10th July 2017 - 17th October 2017 - Khalid
 *         (Lightlord323) (Took temporary leave) 25th July 2017 - 1st Sepetmber
 *         2017
 *         <p>
 *         - Subby (Availor) April 30th 2017 - ????
 *         - Khalid (Lightlord323) 25th July 2017 - ????
 *         - Egimfun (Disregard) 7th November 2017 - ????
 *         - Invested (Screm111) August 2018 - ????
 *         - Kav_ (Kaveen) August 2018 - ????
 *         - CUXNT October 2018 - ????
 *         - Zinkz October 2018 - ????
 *         - MistaCat December 2018 - ????
 */

public class PracticeServer extends JavaPlugin {

	public static final boolean OPEN_BETA_STATS = true;
	public static final boolean FFA = false;
	public static final boolean BETA_VENDOR_ENABLED = false;
	public static final double MIN_DAMAGE_MULTIPLIER = 1;
	public static final boolean DOUBLE_DROP_RATE = false;
	public static final double MAX_DAMAGE_MULTIPLIER = 1;
	public static final double HPS_MULTIPLIER = 5;
	public static final boolean GLOWING_NAMED_ELITE_DROP = true;
	public static final boolean RANDOM_DURA_NAMED_ELITE_DROP = false;
	public static final double HP_MULTIPLIER = 1.35;
	public static final String VERSION_STRING = "1.1.2.4";
	public static boolean ALIGNMENT_GLOW = false;
	public static boolean DATABASE = true;
	public static boolean t6 = false;

	public static PracticeServer plugin;
	public static Logger log;
	private static PatchIO patchIO;
	private static Alignments alignments;
	private static Antibuild antibuild;
	private static Banks banks;
	private static Buddies buddies;
	private static DropPriority dropPriority;
	private static ChatMechanics chatMechanics;
	private static Damage damage;
	private static Durability durability;
	private static Enchants enchants;
	private static Energy energy;
	private static GemPouches gemPouches;
	private static Hearthstone hearthstone;
	private static Horses horses;
	private static Altar altars;
	private static ItemVendors itemVendors;
	private static Listeners listeners;
	public static Logout logout;
	private static LootChests lootChests;
	private static MerchantMechanics merchantMechanics;
	private static Mining mining;
	private static Mobdrops mobdrops;
	private static Mobs mobs;
	private static GuildMechanics guildMechanics;
	private static Orbs orbs;
	private static Parties parties;
	private static ProfessionMechanics professionMechanics;
	private static Repairing repairing;
	private static Deadman deadman;
	private static Respawn respawn;
	private static Spawners spawners;
	private static Speedfish speedfish;
	private static Staffs staffs;
	private static Duels duels;
	private static TeleportBooks teleportBooks;
	private static Toggles toggles;
	private static Untradeable untradeable;
	private static Trading trading;
	private static CratesMain cm;
	private static Economy em;
	private static ForceField ff;
	private static Nametag nt;
	private static WepTrak wepTrak;
	private static GolemElite golemElite;
	private static PickTrak pickTrak;
	private static GamePlayer gap;
	private static Vendor vendor;
	private static ModerationMechanics moderationMechanics;
	private static SQLMain sqlmain;
	private static WorldBossHandler worldBoss;
	private static PracticeServer instance;
	private static MarketData marketData;
	private static PlayerData playerData;
	private static ManagerHandler managerHandler; // here
	private static BuffHandler buffHandler;
	private static CustomFilter customFilter;
	private static NewMerchant merchant;
	private static GemGambling gemGambling;
	private static Shard shard;
	private static PersistentPlayers persistentPlayers;
	public static boolean devstatus;
	public static ArrayList<String> patchnotes = new ArrayList<String>();

	public static Spawners getSpawners() {
		return spawners;
	}

	public static Economy getEconomy() {
		return em;
	}

	public static Mobs getMobs() {
		return mobs;
	}

	public static PracticeServer getInstance() {
		return instance;
	}

	public static BuffHandler buffHandler() {
		return buffHandler;
	}
	public static WorldBossHandler getWorldBossHandler() {
	    return worldBoss;
	}

	public static PatchIO getPatchIO() {
		return patchIO;
	}

	public static MarketData getMarketData() {
		return marketData;
	}

	public static PlayerData getPlayerData() {
		return playerData;
	}

	public static ManagerHandler getManagerHandler() {
		return managerHandler;
	}

	@Override
	public void onEnable() {
		plugin = this;
		instance = this;
		loadDefaultDevStatusConfig();
		this.devstatus = plugin.getConfig().getBoolean("dev-server");
		Bukkit.getWorlds().get(0).setAutoSave(false);
		new BukkitRunnable() {

			public void run() {
				Bukkit.getServer().getOnlinePlayers().forEach(Player::saveData);
			}
		}.runTaskTimerAsynchronously(this, 6000, 6000);
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		marketData = new MarketData(this);
		playerData = new PlayerData(this);
		Bukkit.getPluginManager().registerEvents(new ChatTagGUIHandler(), this);
		Bukkit.getPluginManager().registerEvents(new Altar(), this);
		Bukkit.getPluginManager().registerEvents(new ScrollGUIHandler(), this);
		Bukkit.getPluginManager().registerEvents(new MarketHandler(), this);
		managerHandler = new ManagerHandler();
		altars = new Altar();
		sqlmain = new SQLMain();
		log = plugin.getLogger();
		gap = new GamePlayer();
		customFilter = new CustomFilter();
		Bukkit.getServer().getLogger().setFilter(customFilter);
        PracticeServer.plugin.getLogger().setFilter(customFilter);
        log.setFilter(customFilter);
		moderationMechanics = new ModerationMechanics();
		cm = new CratesMain();
		guildMechanics = GuildMechanics.getInstance();
		trading = new Trading();
		deadman = new Deadman();
		shard = new Shard();
		nt = new Nametag();
		wepTrak = new WepTrak();
		pickTrak = new PickTrak();
		alignments = new Alignments();
		antibuild = new Antibuild();
		worldBoss = new WorldBossHandler();
		banks = new Banks();
		buddies = new Buddies();
		ff = new ForceField();
		vendor = new Vendor();
		duels = new Duels();
		chatMechanics = new ChatMechanics();
		damage = new Damage();
		durability = new Durability();
		enchants = new Enchants();
		energy = new Energy();
		gemPouches = new GemPouches();
		hearthstone = new Hearthstone();
		horses = new Horses();
		itemVendors = new ItemVendors();
		listeners = new Listeners();
		em = new Economy();
		dropPriority = new DropPriority();
		logout = new Logout();
		lootChests = new LootChests();
		merchantMechanics = new MerchantMechanics();
		mining = new Mining();
		mobdrops = new Mobdrops();
		mobs = new Mobs();
		orbs = new Orbs();
		parties = new Parties();
		professionMechanics = new ProfessionMechanics();
		repairing = new Repairing();
		respawn = new Respawn();
		spawners = new Spawners();
		speedfish = new Speedfish();
		staffs = new Staffs();
		persistentPlayers = new PersistentPlayers();
		teleportBooks = new TeleportBooks();
		toggles = new Toggles();
		untradeable = new Untradeable();
		golemElite = new GolemElite();
		merchant = new NewMerchant();
		GuildPlayers.getInstance();
		managerHandler.onEnable();
		gap.onEnable();
		if(PracticeServer.DATABASE)
        sqlmain.onEnable();
		moderationMechanics.onEnable();
		alignments.onEnable();
		antibuild.onEnable();
		banks.onEnable();
		buddies.onEnable();
		chatMechanics.onEnable();
		merchant.onEnable();
		vendor.onEnable();
		damage.onEnable();
		durability.onEnable();
		enchants.onEnable();
		worldBoss.onLoad();
		nt.onEnable();
		duels.onEnable();
		wepTrak.onEnable();
		pickTrak.onEnable();
		energy.onEnable();
		persistentPlayers.onEnable();
		gemPouches.onEnable();
		hearthstone.onEnable();
		altars.onEnable();
		dropPriority.onEnable();
		horses.onEnable();
		itemVendors.onEnable();
		ff.onEnable();
		cm.onEnable();
		listeners.onEnable();
		logout.onEnable();
		lootChests.onEnable();
		merchantMechanics.onEnable();
		mining.onEnable();
		mobdrops.onEnable();
		mobs.onEnable();
		orbs.onEnable();
		parties.onEnable();
		professionMechanics.onEnable();
		repairing.onEnable();
		respawn.onEnable();
		spawners.onEnable();
		speedfish.onEnable();
		shard.onEnable();
		staffs.onEnable();
		teleportBooks.onEnable();
		em.onEnable();
		toggles.onEnable();
		guildMechanics.onEnable();
		trading.onEnable();
		untradeable.onEnable();
		golemElite.onEnable();
		registerCommands();
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		ItemAPI.init();
		new RegionHandler().init();
		new SkeletonElite().init();
		Fishing.getInstance().loadFishingLocations();
		OrbGambling orbGambling = new OrbGambling();
		gemGambling = new GemGambling();
		orbGambling.initPool();
		buffHandler = new BuffHandler();
		getServer().getPluginManager().registerEvents(buffHandler, this);
		getServer().getPluginManager().registerEvents(orbGambling, this);
		getServer().getPluginManager().registerEvents(gemGambling, this);

		buffHandler.init();
		getServer().getPluginManager().registerEvents(new OreMerchant(), this);
		getServer().getPluginManager().registerEvents(new Enchanter(), this);
		Fishing.checkMonkRegion();
		//patchIO = new PatchIO();
		getServer().getPluginManager().registerEvents(new PatchListener(), this);
		TabMenu.init2();
		API.getRainbowSheepTask().init();
		new Elytras(this);
		new ArmorListener(this);

	}

	public void onDisable() {
		instance = null;
		if(PracticeServer.DATABASE)
		sqlmain.onDisable();
		duels.onDisable();
		managerHandler.onDisable();
		trading.onDisable();
		em.onDisable();
		logout.onDisable(false);
		moderationMechanics.onDisable();
		buddies.onDisable();
		alignments.onDisable();
		antibuild.onDisable();
		banks.onDisable();
		lootChests.onDisable();
		spawners.onDisable();
		chatMechanics.onDisable();
		damage.onDisable();
		durability.onDisable();
		enchants.onDisable();
		dropPriority.onDisable();
		energy.onDisable();
		gemPouches.onDisable();
		hearthstone.onDisable();
		horses.onDisable();
		itemVendors.onDisable();
		listeners.onDisable();
		merchantMechanics.onDisable();
		mining.onDisable();
		mobdrops.onDisable();
		mobs.onDisable();
		orbs.onDisable();
		parties.onDisable();
		professionMechanics.onDisable();
		merchant.onDisable();
		repairing.onDisable();
		respawn.onDisable();
		speedfish.onDisable();
		staffs.onDisable();
		teleportBooks.onDisable();
		toggles.onDisable();
		untradeable.onDisable();
		guildMechanics.onDisable();
	}

	public void registerCommands() {
		getCommand("setrates").setExecutor(new SetRatesCommand());
		getCommand("droprates").setExecutor(new DropRatesCommand());
		getCommand("givemask").setExecutor(new GiveMaskCommand()); // share all
		getCommand("givecandy").setExecutor(new GiveCandyCommand()); // tjis
		getCommand("givehallowcrate").setExecutor(new HalloweenCrateCommand()); // this
		getCommand("CheckGems").setExecutor(new CheckGemsCommand()); // this
		getCommand("giveLegendaryOrb").setExecutor(new LegendaryOrbCommand()); // this
		getCommand("shard").setExecutor(new ShardCommand());
		getCommand("toggleff").setExecutor(new ToggleFFCommand());
		getCommand("givetokens").setExecutor(new GiveTokensCommand());
        getCommand("awardtokens").setExecutor(new AwardTokensCommand());
		getCommand("maxpartysize").setExecutor(new MaxPartySizeCommand());
		getCommand("togglemobs").setExecutor(new ToggleMobsCommand());
		getCommand("listparties").setExecutor(new ListPartiesCommand());
		getCommand("togglechaos").setExecutor(new ToggleChaosCommand());
		getCommand("togglepvp").setExecutor(new TogglePVPCommand());
		getCommand("playerclone").setExecutor(new CloneCommand());
		getCommand("toggledebug").setExecutor(new ToggleDebugCommand());
		getCommand("setAlignment").setExecutor(new SetAlignmentCommand());
		getCommand("market").setExecutor(new MarketCommand());
		getCommand("lootbuff").setExecutor(new BuffCommand());
		//getCommand("duel").setExecutor(new DuelCommand());
		getCommand("daccept").setExecutor(new DuelAcceptCommand());
		getCommand("testcommand").setExecutor(new TestCommand());
		getCommand("pduel").setExecutor(new PartyDuelCommand());
		getCommand("pdaccept").setExecutor(new PartyDuelAcceptCommand());
		getCommand("dquit").setExecutor(new DuelQuitCommand());
		getCommand("createBankNote").setExecutor(new CreateBankNoteCommand());
		getCommand("giveScroll").setExecutor(new GiveProtectionScrollCommand());
		getCommand("tags").setExecutor(new ChatTagCommand());
		getCommand("tellall").setExecutor(new TellAllCommand());
		getCommand("gl").setExecutor(new GlobalCommand());
		getCommand("givePouch").setExecutor(new GivePouchCommand());
		getCommand("GiveCrate").setExecutor(new GiveCrateCommand());
		getCommand("GiveOrb").setExecutor(new GiveOrbCommand());
		getCommand("GiveNameTag").setExecutor(new GiveNameTagCommand());
		getCommand("giveEnchant").setExecutor(new GiveEnchantCommand());
		getCommand("giveGodPick").setExecutor(new GiveGodPickCommand());
		getCommand("giveInsanePick").setExecutor(new GiveInsanePickCommand());
		getCommand("giveAll").setExecutor(new GiveAllCommand());
		getCommand("giveMagnetPick").setExecutor(new GiveMagnetPickCommand());
		getCommand("mount").setExecutor(new MountCommand());
		getCommand("spawn").setExecutor(new SpawnCommand());
		getCommand("giveWepTrak").setExecutor(new GiveWepTrakCommand());
		getCommand("givePickTrak").setExecutor(new GivePickTrakCommand());
		getCommand("pet").setExecutor(new PetCommand());
		getCommand("message").setExecutor(new MessageCommand());
		getCommand("reply").setExecutor(new ReplyCommand());
		getCommand("roll").setExecutor(new RollCommand());
		getCommand("fakeroll").setExecutor(new FakeRollCommand());
		getCommand("toggle").setExecutor(new ToggleCommand());
		getCommand("add").setExecutor(new AddCommand());
		getCommand("del").setExecutor(new DeleteCommand());
		getCommand("logout").setExecutor(new LogoutCommand());
		getCommand("setrank").setExecutor(new SetRankCommand());
		getCommand("banksee").setExecutor(new BankSeeCommand());
		getCommand("psvanish").setExecutor(new VanishCommand());
		getCommand("invsee").setExecutor(new InvSeeCommand());
		getCommand("guildwipeall").setExecutor(new GuildWipeAllCommand());
		getCommand("drheal").setExecutor(new HealCommand());
		getCommand("createdrop").setExecutor(new CreateDropCommand());
		getCommand("sc").setExecutor(new StaffChatCommand());
		getCommand("showms").setExecutor(new ShowMSCommand());
		getCommand("hidems").setExecutor(new HideMSCommands());
		getCommand("killall").setExecutor(new KillAllCommand());
		getCommand("monspawn").setExecutor(new MonSpawnCommand());
		getCommand("showloot").setExecutor(new ShowLootCommand());
		getCommand("hideloot").setExecutor(new HideLootCommand());
		getCommand("pinvite").setExecutor(new PInviteCommand());
		getCommand("paccept").setExecutor(new PAcceptCommand());
		getCommand("pkick").setExecutor(new PKickCommand());
		getCommand("pquit").setExecutor(new PQuitCommand());
		getCommand("pdecline").setExecutor(new PDeclineCommand());
		getCommand("p").setExecutor(new PartyCommand());
		getCommand("food").setExecutor(new FeedCommand());
		getCommand("dungeonstart").setExecutor(new CommandStart());
		getCommand("patch").setExecutor(new PatchCommand());
		getCommand("givebuff").setExecutor(new GiveBuffCommand());
		getCommand("orbs").setExecutor(new OrbsCommand());
		getCommand("armorsee").setExecutor(new ArmorSeeCommand());
		getCommand("psmute").setExecutor(new MuteCommand());
		getCommand("psunmute").setExecutor(new UnmuteCommand());
		getCommand("discord").setExecutor(new DiscordCommand());
		getCommand("leaderboard").setExecutor(new LeaderboardCommand());
		getCommand("toggleenergy").setExecutor(new ToggleEnergyCommand());
		getCommand("dpsdummy").setExecutor(new DPSDummyCommand(this));
		getCommand("deploy").setExecutor(new DeployCommand(this));
		getCommand("rrversion").setExecutor(new RRVersionCommand());
		getCommand("togglegm").setExecutor(new ToggleGMCommand());
		getCommand("togglegems").setExecutor(new ToggleGemCommand());
		getCommand("togglealignmentglow").setExecutor(new ToggleAlignmentGlowCommand());
		getCommand("elytra").setExecutor(new ElytraCommand());
		getCommand("scrap").setExecutor(new ScrapItem());
		getCommand("combust").setExecutor(new CombustOrb());


	}

	public static void refreshPatchNotes() {

		try {
			patchnotes.clear();
			URL url = new URL("https://retrorealms.net/patchnotes.txt");
			Scanner scan = new Scanner(url.openStream());
			while (scan.hasNext()) {
				PracticeServer.patchnotes.add(scan.nextLine());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void loadDefaultDevStatusConfig() {
		plugin.getConfig().addDefault("dev-server", false);
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();

	}

}
