package me.retrorealms.practiceserver.mechanics.player;

import lombok.Getter;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.API;
import me.retrorealms.practiceserver.apis.actionbar.ActionBar;
import me.retrorealms.practiceserver.apis.itemapi.NBTAccessor;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.commands.moderation.ToggleGMCommand;
import me.retrorealms.practiceserver.commands.moderation.VanishCommand;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.donations.Crates.CratesMain;
import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.drops.EliteDrops;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.enchants.Enchants;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.item.Journal;
import me.retrorealms.practiceserver.mechanics.mobs.Mobs;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.player.GamePlayer.PlayerEntity;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.teleport.Hearthstone;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import me.retrorealms.practiceserver.utils.Particles;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.glow.GlowAPI;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@SuppressWarnings("deprecation")
public class Listeners implements Listener {

	public static ConcurrentHashMap<UUID, Long> named = new ConcurrentHashMap<UUID, Long>();
	@Getter
	public ConcurrentHashMap<Player, LivingEntity> playerGuildTag = new ConcurrentHashMap<>();
	ConcurrentHashMap<String, Long> update = new ConcurrentHashMap<String, Long>();
	public static ConcurrentHashMap<String, Long> combat = new ConcurrentHashMap<String, Long>();
	public static ConcurrentHashMap<UUID, Long> mobd = new ConcurrentHashMap<UUID, Long>();
	public static ArrayList<Player> previo = new ArrayList<Player>();
	ConcurrentHashMap<UUID, Long> firedmg = new ConcurrentHashMap<UUID, Long>();

	public static boolean isInCombat(Player p) {
		if (Listeners.combat.containsKey(p.getName())
				&& System.currentTimeMillis() - Listeners.combat.get(p.getName()) <= 10000) {
			return true;
		} else {
			return false;
		}
	}

	public static void hpCheck(Player p) {
		if (p.isOp() && !ToggleGMCommand.togglegm.contains(p.getName())) {
			return;
		}
		PlayerInventory i = p.getInventory();
		double a = 50.0;
		double vital = 0.0;
		ItemStack[] arritemStack = i.getArmorContents();
		int n = arritemStack.length;
		int n2 = 0;
		while (n2 < n) {
			ItemStack is = arritemStack[n2];
			if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
				double health = Damage.getHp(is);
				int vit = Damage.getElem(is, "VIT");
				a += health;
				vital += (double) vit;
			}
			++n2;
		}
		if (vital > 0.0) {
			double mod = vital * 0.05;
			a += a * (mod / 100.0);
			p.setMaxHealth((double) ((int) a));
		} else {
			p.setMaxHealth(a);
		}
		p.setHealthScale(20.0);
		p.setHealthScaled(true);
	}

	public void onEnable() {
		PracticeServer.log.info("[Listeners] has been enabled.");
		Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
		new BukkitRunnable() {

			public void run() {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					p.setFoodLevel(20);
					p.setSaturation(20.0f);
				}
			}
		}.runTaskTimerAsynchronously(PracticeServer.plugin, 200, 100);
		new BukkitRunnable() {

			public void run() {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					if (VanishCommand.vanished.contains(p.getName())) {
						continue;
					}
					Random r = new Random();
					float y = r.nextFloat() - 0.2F;
					float x = r.nextFloat() - 0.2F;
					float z = r.nextFloat() - 0.2F;

					if (!ModerationMechanics.isDonator(p) || !Toggles.getToggles(p.getUniqueId()).contains("Trail"))
						continue;
					if (ModerationMechanics.getRank(p) == RankEnum.SUB) {
						Particles.VILLAGER_HAPPY.display(0.125f, 0.125f, 0.125f, 0.02f, 10,
								p.getLocation().clone().add(x, y, z), 20.0);
					}
					if (ModerationMechanics.getRank(p) == RankEnum.SUB1) {
						Particles.FLAME.display(0.0f, 0.0f, 0.0f, 0.02f, 10, p.getLocation().clone().add(x, y, z), 20.0);
					}
					if (ModerationMechanics.getRank(p) == RankEnum.SUPPORTER
							|| ModerationMechanics.getRank(p) == RankEnum.SUB3) {
						double phi = 0;
						phi = phi + Math.PI / 8;
						double x1, y1, z1;

						Location location1 = p.getLocation();
						for (double t = 0; t <= 2 * Math.PI; t = t + Math.PI / 16) {
							for (double i = 0; i <= 1; i = i + 1) {
								x1 = 0.4 * (2 * Math.PI - t) * 0.5 * Math.cos(t + phi + i * Math.PI);
								y1 = 0.5 * t;
								z1 = 0.4 * (2 * Math.PI - t) * 0.5 * Math.sin(t + phi + i * Math.PI);
								location1.add(x1, y1, z1);
								Particles.REDSTONE.display(0, 0, 0, 0, 1, location1, 20.0);
								location1.subtract(x1, y1, z1);
							}

						}

						if (phi > 10 * Math.PI) {
							this.cancel();
						}
					}

					if (ModerationMechanics.getRank(p) == RankEnum.SUB2) {
						Particles.SPELL_WITCH.display(0.0f, 0.0f, 0.0f, 1.0f, 10, p.getLocation().clone().add(x, y, z), 20.0);
					}
				}
			}
		}.runTaskTimerAsynchronously(PracticeServer.plugin, 0, 3);
	}

	public void onDisable() {
		PracticeServer.log.info("[Listeners] has been disabled.");
	}

	@EventHandler
	public void dropItem(PlayerDropItemEvent e) {

		if (DeployCommand.patchlockdown) {
			e.setCancelled(true);
			return;
		}

		if (e.getPlayer().isOp() && !ToggleGMCommand.togglegm.contains(e.getPlayer().getName())
				&& !e.getPlayer().getName().equalsIgnoreCase("Kav_")
				&& !e.getPlayer().getName().equalsIgnoreCase("Red")
				&& !e.getPlayer().getName().equalsIgnoreCase("Razelesh")
				&& !e.getPlayer().getName().equalsIgnoreCase("Palua")
				&& !e.getPlayer().getName().equalsIgnoreCase("NekoNoPantsu")
				&& !e.getPlayer().getName().equalsIgnoreCase("Red29")
				&& !e.getPlayer().getName().equalsIgnoreCase("oopsjpeg")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (event.getCurrentItem() == null || (event.getCurrentItem().getType() == Material.AIR)) {
			return;
		}
		if (event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem() != null) {
			NBTAccessor nbtAccessor = new NBTAccessor(event.getCurrentItem());
			if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Spectral")
					&& !nbtAccessor.hasKey("fixedgear")) {
				switch (event.getCurrentItem().getType()) {
				case DIAMOND_HELMET:
				case DIAMOND_CHESTPLATE:
				case DIAMOND_LEGGINGS:
				case DIAMOND_BOOTS:
					event.getWhoClicked().getInventory().addItem(EliteDrops.createCustomEliteDrop("spectralKnight"));
					break;
				default:
					return;
				}
			}
		}
	}

	@EventHandler
	public void onExit(VehicleExitEvent event) {
		try {
            Location location = event.getVehicle().getLocation();
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getExited().teleport(location);
                }
            }.runTaskLaterAsynchronously(PracticeServer.getInstance(), 4L);
        }catch(Exception e){
		    System.out.println("Listeners.java:249 AAC btw haHAA");
        }
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (DeployCommand.patchlockdown) {
			e.setCancelled(true);
			return;
		}
		if (e.getPlayer().getInventory().getItemInOffHand().getType() != Material.AIR) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void FuckCazza(InventoryClickEvent e) {// Cazza is a snake
		if (e.getInventory().getName().equals("Bank Chest (1/1)")) {
			HumanEntity p = e.getWhoClicked();
			if (!p.isOp() && !Alignments.isSafeZone(p.getLocation())) {
				p.closeInventory();
				p.closeInventory();
			}
		}
	}

	public static boolean isWeapon(ItemStack itemStack) {
		return itemStack.getType().name().contains("_AXE") || itemStack.getType().name().contains("_SWORD")
				|| itemStack.getType().name().contains("_HOE") || itemStack.getType().name().contains("_SPADE");
	}

	@EventHandler
	public void onPreJoin(AsyncPlayerPreLoginEvent event) {

		if (DeployCommand.patchlockdown) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
					ChatColor.RED + "The server is in the middle of deploying a patch. Please join in a few seconds.");
		}
	}
	@EventHandler
	public void on(EntityCombustEvent event) {
		if (event.getEntityType() == EntityType.DROPPED_ITEM) {
			event.setCancelled(true);
		}
	}
	@EventHandler
	public void on(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.DROPPED_ITEM) {
			event.setCancelled(true);
		}
	}
	public static ItemStack donorPick(){
		ItemStack P = new ItemStack(Material.DIAMOND_PICKAXE);
		ItemMeta pickmeta = P.getItemMeta();
		pickmeta.setDisplayName(ChatColor.BLUE + "Donator Pickaxe");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Level: " + ChatColor.BLUE + "120");
		lore.add(ChatColor.GRAY + "0 / 0");
		lore.add(ChatColor.GRAY + "EXP: " + ChatColor.BLUE + "||||||||||||||||||||||||||||||||||||||||||||||||||");
		lore.add(ChatColor.RED + "DOUBLE ORE: 10%");
		lore.add(ChatColor.RED + "GEM FIND: 10%");
		lore.add(ChatColor.RED + "TRIPLE ORE: 5%");
		lore.add(ChatColor.RED + "TREASURE FIND: 3%");
		lore.add(ChatColor.RED + "MINING SUCCESS: 20%");
		lore.add(ChatColor.RED + "DURABILITY: 15%");
		lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of ice.");
		pickmeta.setLore(lore);
		P.setItemMeta(pickmeta);
		return P;
	}


	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (event.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR && entity instanceof Player) {
			Player player = (Player) entity;
			Block damager = player.getLocation().getBlock();
			if (damager.getType() == Material.MAGMA) {
				event.setCancelled(true);
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void on(ServerListPingEvent event) {
		try {
			event.setMotd(ChatColor.translateAlternateColorCodes('&', "                    &3&lMYSTARIA&r\n            &7&oA Minecraft MMORPG/Looter"));
		}catch (Exception e) {

		}
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {

		Player player = e.getPlayer();
		if (API.getPlayerRegistry().request(player) == null)
			new PlayerEntity(player.getUniqueId());

		player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)
				.setBaseValue(1024.0D); /* Fixes 1.9+ Combat */

		/* Initiates the player Login by setting the basic Data */
		player.setLevel(100);
		player.setExp(1.0f);
		player.setHealthScale(20.0);
		player.setHealthScaled(true);
		player.getInventory().setHeldItemSlot(0);

		/* Used to make the Clink sound on login */
		if (player.getInventory().getItem(0) != null && isWeapon(player.getInventory().getItem(0))) {
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.5f);
		}
		/* Message of the Day goes here */
		IntStream.range(0, 20).forEach(number -> player.sendMessage(" "));

		StringUtil.sendCenteredMessage(player,
				ChatColor.BOLD + "Mystaria Patch " + PracticeServer.getInstance().getDescription().getVersion());
		player.sendMessage("");
		StringUtil.sendCenteredMessage(player,
				ChatColor.GRAY + "" + ChatColor.ITALIC + "This server is still in early development, expect bugs.");
		StringUtil.sendCenteredMessage(player,
				ChatColor.GRAY + "" + ChatColor.ITALIC + "(New Map Coming in 1-2 Weeks)");
		player.sendMessage("");
		StringUtil.sendCenteredMessage(player,
				ChatColor.GRAY + "" +  "Voting Links");
		StringUtil.sendCenteredMessage(player,
				ChatColor.GRAY + "" + ChatColor.ITALIC + "http://bit.ly/3j5hdLb");
		StringUtil.sendCenteredMessage(player,
				ChatColor.GRAY + "" + ChatColor.ITALIC + "https://bit.ly/3kHRaKd");
		StringUtil.sendCenteredMessage(player,
				ChatColor.GRAY + "" + ChatColor.ITALIC + "http://bit.ly/407DSXE");

		e.setJoinMessage(ChatColor.AQUA + "[+] " + ChatColor.GRAY + player.getName());
		hpCheck(player); /*
							 * Updates the players HP by getting gear and
							 * setting HP.
							 */
		if (player.isOp()) {
			if(!ToggleGMCommand.togglegm.contains(player.getName())) ActionBar.sendActionBar(player, "&bYou are in GM Mode", 5);
			player.sendMessage(ChatColor.BLUE + "You are currently not vanished! Please use /psvanish to vanish.");
			player.setMaxHealth(10000);
			player.setHealth(10000);
		}
		Toggles.enablePM(
				player); /* Enables the players PM toggle for some reason */
		Toggles.toggles.get(player.getUniqueId()).add("Debug");
		Toggles.toggles.get(player.getUniqueId()).add("Glow Drops");
	}

	@EventHandler
	public void onClickFurnace(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (event.getPlayer().getItemInHand() == null
					|| event.getPlayer().getItemInHand().getType().equals(Material.AIR)) {
				if (event.getClickedBlock().getType().equals(Material.FURNACE)) {
					event.getPlayer().sendMessage(ChatColor.RED
							+ "This furnace can be used to cook fish! Right click this furnace while holding raw fish to cook it.");
                    event.setCancelled(true);
				}

			}

		}

	}

	@EventHandler
	public void onLeave(PlayerKickEvent e) {
		e.setLeaveMessage(null);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		e.setQuitMessage(null);
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		Alignments.tagged.remove(p.getName());
		combat.remove(p.getName());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDeath(EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			LivingEntity s = (LivingEntity) e.getEntity();
			if (e.getDamage() >= s.getHealth()) {
				if (mobd.containsKey(s.getUniqueId())) {
					mobd.remove(s.getUniqueId());
				}
				if (this.firedmg.containsKey(s.getUniqueId())) {
					this.firedmg.remove(s.getUniqueId());
				}
				if (Mobs.sound.containsKey(s.getUniqueId())) {
					Mobs.sound.remove(s.getUniqueId());
				}
				if (named.containsKey(s.getUniqueId())) {
					named.remove(s.getUniqueId());
				}
			}
		}
	}

	@EventHandler
	public void onHealthBar(EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof Player) && e.getDamage() > 0.0) {
			LivingEntity s = (LivingEntity) e.getEntity();
			double max = s.getMaxHealth();
			double hp = s.getHealth() - e.getDamage();
			s.setCustomName(Mobs.generateOverheadBar(s, hp, max, Mobs.getMobTier(s)));
			s.setCustomNameVisible(true);
			named.put(s.getUniqueId(), System.currentTimeMillis());
		}
	}

	@EventHandler
	public void onPotDrink(PlayerInteractEvent e) {
		Player p;
		if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& (p = e.getPlayer()).getInventory().getItemInMainHand().getType() == Material.POTION
				&& p.getInventory().getItemInMainHand() != null) {
			e.setCancelled(true);
			if (p.getInventory().getItemInMainHand().getItemMeta().hasLore()) {
				String l = ChatColor.stripColor(p.getInventory().getItemInMainHand().getItemMeta().getLore().get(0));
				l = l.split("HP")[0];
				int hp = 0;
				try {
					hp = Integer.parseInt(l.split(" ")[4]);
				} catch (Exception ex) {
					hp = 0;
				}
				if (hp > 0) {
					p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);
					p.getInventory().setItemInMainHand(null);
					if (p.getHealth() + (double) hp > p.getMaxHealth()) {
						p.sendMessage("               " + ChatColor.GREEN + ChatColor.BOLD + "+" + ChatColor.GREEN + hp
								+ ChatColor.BOLD + " HP" + ChatColor.GRAY + " [" + (int) p.getMaxHealth() + "/"
								+ (int) p.getMaxHealth() + "HP]");
						p.setHealth(p.getMaxHealth());
					} else {
						p.sendMessage("               " + ChatColor.GREEN + ChatColor.BOLD + "+" + ChatColor.GREEN + hp
								+ ChatColor.BOLD + " HP" + ChatColor.GRAY + " [" + (int) (p.getHealth() + (double) hp)
								+ "/" + (int) p.getMaxHealth() + "HP]");
						p.setHealth(p.getHealth() + (double) hp);
					}
				}
				PlayerInventory inv = p.getInventory();
				int slot = inv.getHeldItemSlot();
				new BukkitRunnable() {
					public void run() {
						for (int i = 36; i > -1; i--) {
							if (inv.getItem(i) != null && inv.getItem(i).getType() == Material.POTION) {
								inv.setItem(slot, inv.getItem(i));
								inv.setItem(i, null);
								break;
							}
						}
					}
				}.runTaskLater(PracticeServer.plugin, 10);
			}
		}
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if (e.toWeatherState()) {
			e.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBookOpen(PlayerInteractEvent e) {
		if (DeployCommand.patchlockdown) {
			e.setCancelled(true);
			return;
		}
		Player p = e.getPlayer();
		PlayerInventory i = p.getInventory();
		if (p.getInventory().getItemInMainHand() != null
				&& p.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK
				&& (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			BookMeta bm = (BookMeta) book.getItemMeta();
			String s = ChatColor.DARK_GREEN.toString() + ChatColor.UNDERLINE + "Lawful";
			String desc = ChatColor.BLACK.toString() + ChatColor.ITALIC + "-30% Durability Arm/Wep on Death";
			if (Alignments.chaotic.containsKey(p.getName())) {
				s = ChatColor.DARK_RED.toString() + ChatColor.UNDERLINE + "Chaotic\n" + ChatColor.BLACK + ChatColor.BOLD
						+ "Neutral" + ChatColor.BLACK + " in " + Alignments.chaotic.get(p.getName()) + "s";
				desc = ChatColor.BLACK.toString() + ChatColor.ITALIC + "Inventory LOST on Death";
			}
			if (Alignments.neutral.containsKey(p.getName())) {
				s = ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "Neutral\n" + ChatColor.BLACK + ChatColor.BOLD
						+ "Lawful" + ChatColor.BLACK + " in " + Alignments.neutral.get(p.getName()) + "s";
				desc = ChatColor.BLACK.toString() + ChatColor.ITALIC + "25%/50% Arm/Wep LOST on Death";
			}
			int dps = 0;
			int arm = 0;
			int amt = 5;
			int nrg = 100;
			int block = 0;
			int dodge = 0;
			int intel = 0;
			int str = 0;
			int vit = 0;
			int dex = 0;
			int arm_pcnt = 0;
			int sword_dmg = 0;
			int pole_dmg = 0;
			int axe_dmg = 0;
			int block_pcnt = 0;
			int health_pcnt = 0;
			int hps_pcnt = 0;
			int nrg_pcnt = 0;
			int crit_pcnt = 0;
			int ele_resist = 0;
			int ele_dmg = 0;
			int staff_dmg = 0;
			int dps_pcnt = 0;
			int dodge_pcnt = 0;
			int armpen = 0;
			ItemStack[] arritemStack = i.getArmorContents();
			int n = arritemStack.length;
			int n2 = 0;
			while (n2 < n) {
				ItemStack is = arritemStack[n2];
				if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
					int adddps = Damage.getDps(is);
					dps += adddps;
					int addarm = Damage.getArmor(is);
					arm += addarm;
					int added = Damage.getHps(is);
					amt += added;
					int addednrg = Damage.getEnergy(is);
					nrg += addednrg;
					int addeddodge = Damage.getPercent(is, "DODGE");
					dodge += addeddodge;
					int addedblock = Damage.getPercent(is, "BLOCK");
					block += addedblock;
					int addedint = Damage.getElem(is, "INT");
					intel += addedint;
					int addedstr = Damage.getElem(is, "STR");
					str += addedstr;
					int addedvit = Damage.getElem(is, "VIT");
					vit += addedvit;
					int addeddex = Damage.getElem(is, "DEX");
					dex += addeddex;
				}
				++n2;
			}
			if (intel > 0) {
				nrg += Math.round(intel / 125);
				nrg_pcnt = (int) Math.round((double) intel * 0.009);
				crit_pcnt = (int) Math.round((double) intel * 0.015);
				staff_dmg = Math.round(intel / 50);
				ele_dmg = Math.round(intel / 30);
			}
			if (vit > 0) {
				sword_dmg = Math.round(vit / 50);
				health_pcnt = (int) Math.round((double) vit * 0.05);
				hps_pcnt = (int) Math.round((double) vit * 0.3);
				amt += hps_pcnt;

			}
			if (str > 0) {
				pole_dmg = Math.round(str / 50);
				axe_dmg = Math.round(str / 50);
				block_pcnt = (int) Math.round((double) str * 0.015);
				block = (int) ((long) block + Math.round((double) str * 0.015));
				arm_pcnt = (int) Math.round(str * 0.012);
				arm = (int) ((long) arm + Math.round((double) str * 0.012));
			}
			if (dex > 0) {
				dodge_pcnt = (int) Math.round((double) dex * 0.015);
				dodge = (int) ((long) dodge + Math.round((double) dex * 0.015));
				dps_pcnt = (int) Math.round(dex * 0.012);
				dps = (int) ((long) dps + Math.round((double) dex * 0.012));
				armpen = Math.round(dex / 50);

			}
			bm.addPage(ChatColor.UNDERLINE.toString() + ChatColor.BOLD + "  Your Character  \n\n" + ChatColor.RESET
					+ ChatColor.BOLD + "Alignment: " + s + "\n" + desc + "\n\n" + ChatColor.BLACK + "  "
					+ (int) p.getHealth() + " / " + (int) p.getMaxHealth() + ChatColor.BOLD + " HP\n" + ChatColor.BLACK
					+ "  " + arm + " - " + arm + "%" + ChatColor.BOLD + " Armor\n" + ChatColor.BLACK + "  " + dps
					+ " - " + dps + "%" + ChatColor.BOLD + " DPS\n" + ChatColor.BLACK + "  " + amt + ChatColor.BOLD
					+ " HP/s\n" + ChatColor.BLACK + "  " + nrg + "% " + ChatColor.BOLD + "Energy\n" + ChatColor.BLACK
					+ "  " + dodge + "% " + ChatColor.BOLD + "Dodge\n" + ChatColor.BLACK + "  " + block + "% "
					+ ChatColor.BOLD + "Block");
			bm.addPage(ChatColor.BLACK.toString() + ChatColor.BOLD + "+ " + str + " Strength\n" + "  " + ChatColor.BLACK
					+ ChatColor.UNDERLINE + "'The Warrior'\n" + ChatColor.BLACK + "+" + arm_pcnt + "% Armor\n"
					+ ChatColor.BLACK + "+" + block_pcnt + "% Block\n" + ChatColor.BLACK + "+" + axe_dmg + "% Axe DMG\n"
					+ ChatColor.BLACK + "+" + pole_dmg + "% Polearm DMG\n\n" + ChatColor.BLACK + ChatColor.BOLD + "+ "
					+ vit + " Vitality\n\n" + "  " + ChatColor.BLACK + ChatColor.UNDERLINE + "'The Defender'\n"
					+ ChatColor.BLACK + "+" + health_pcnt + "% Health\n" + ChatColor.BLACK + "+" + hps_pcnt
					+ "   HP/s\n" + ChatColor.BLACK + "+" + sword_dmg + "% Sword DMG");
			bm.addPage(ChatColor.BLACK + "" + ChatColor.BOLD + "+ " + intel + " Intellect\n" + "  " + ChatColor.BLACK
					+ ChatColor.UNDERLINE + "'The Mage'\n" + ChatColor.BLACK + "+" + staff_dmg + "% Staff DMG\n"
					+ ChatColor.BLACK + "+" + nrg_pcnt + "% Energy\n" + ChatColor.BLACK + "+" + ele_dmg
					+ "% Ele Damage\n" + ChatColor.BLACK + "+" + crit_pcnt + "% Critical Hit\n\n" + ChatColor.BLACK + ""
					+ ChatColor.BOLD + "+ " + dex + " Dexterity\n" + "  " + ChatColor.BLACK + ChatColor.UNDERLINE
					+ "'The Archer'\n\n" + ChatColor.BLACK + "+" + dodge_pcnt + "% Dodge\n" + ChatColor.BLACK + "+"
					+ dps_pcnt + "% DPS\n" + ChatColor.BLACK + "+" + armpen + "% Armor Pen.\n ");
			bm.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD + "Character Journal");
			bm.setLore(
					Arrays.asList(ChatColor.GRAY + "A book that displays", ChatColor.GRAY + "your character's stats"));
			book.setItemMeta(bm);
			p.setItemInHand(book);
			p.updateInventory();
			p.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.25f);
			if (!this.update.containsKey(p.getName())
					|| System.currentTimeMillis() - this.update.get(p.getName()) > 2000) {
				p.closeInventory();
			}
			this.update.put(p.getName(), System.currentTimeMillis());
		}
	}

	@EventHandler
	public void onCloseChest(InventoryCloseEvent e) {
		if (e.getInventory().getName().contains("Bank Chest") && e.getPlayer() instanceof Player) {
			Player p = (Player) e.getPlayer();
			p.playSound(p.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
		}
	}

	@EventHandler
	public void onArmourPutOn(PlayerInteractEvent e) {
		if (DeployCommand.patchlockdown) {
			e.setCancelled(true);
			return;
		}
		Player p = e.getPlayer();
		if (p.getInventory().getItemInMainHand() != null
				&& (p.getInventory().getItemInMainHand().getType().name().contains("HELMET")
						|| p.getInventory().getItemInMainHand().getType().name().contains("CHESTPLATE")
						|| p.getInventory().getItemInMainHand().getType().name().contains("LEGGINGS")
						|| p.getInventory().getItemInMainHand().getType().name().contains("BOOTS"))
				&& (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			e.setCancelled(true);
			p.updateInventory();
		}
	}

	@EventHandler
	public void onCombatTag(EntityDamageByEntityEvent e) {
		if (DeployCommand.patchlockdown) {
			e.setCancelled(true);
			return;
		}
		if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
			if (e.getDamage() <= 0.0) {
				return;
			}
			Player p = (Player) e.getDamager();
			if (Alignments.isSafeZone(p.getLocation())) {
				e.setCancelled(true);
				return;
			}
			if (Alignments.isSafeZone(e.getEntity().getLocation())) {
				e.setCancelled(true);
				return;
			}
			combat.put(p.getName(), System.currentTimeMillis());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onNoAutoclick(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof LivingEntity
				&& !(e.getDamager() instanceof Player)) {
			LivingEntity s = (LivingEntity) e.getDamager();
			if (!mobd.containsKey(s.getUniqueId()) || mobd.containsKey(s.getUniqueId())
					&& System.currentTimeMillis() - mobd.get(s.getUniqueId()) > 1000) {
				mobd.put(s.getUniqueId(), System.currentTimeMillis());
			} else if (!(e.getDamager() instanceof MagmaCube)) {
				e.setDamage(0.0);
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onNoDamager(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof LivingEntity && e.getDamager() instanceof LivingEntity
				&& Alignments.isSafeZone(e.getDamager().getLocation())) {
			if (e.getEntity().getType().equals(EntityType.ARMOR_STAND)
					|| (e.getEntity().getCustomName() != null && e.getEntity().getCustomName().contains("DPS Dummy")))
				return;

			e.setDamage(0.0);
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onNoDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity && Alignments.isSafeZone(e.getEntity().getLocation())) {
			if (e.getEntity().getType().equals(EntityType.ARMOR_STAND)
					|| (e.getEntity().getCustomName() != null && e.getEntity().getCustomName().contains("DPS Dummy")))
				return;
			e.setDamage(0.0);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onLoginShiny(PlayerJoinEvent e) {
		ItemStack is;
		Player p = e.getPlayer();
		ItemStack[] arritemStack = p.getInventory().getContents();
		int n = arritemStack.length;
		int n2 = 0;
		while (n2 < n) {
			is = arritemStack[n2];
			if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasDisplayName()
					&& Enchants.getPlus(is) > 3) {
				is.addUnsafeEnchantment(Enchants.glow, 1);
			}
			++n2;
		}
		arritemStack = p.getInventory().getArmorContents();
		n = arritemStack.length;
		n2 = 0;
		while (n2 < n) {
			is = arritemStack[n2];
			if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasDisplayName()
					&& Enchants.getPlus(is) > 3) {
				is.addUnsafeEnchantment(Enchants.glow, 1);
			}
			++n2;
		}
	}

	@EventHandler
	public void onOpenShinyShiny(InventoryOpenEvent e) {
		if (e.getInventory().getName().contains("Bank Chest")) {
			ItemStack[] arritemStack = e.getInventory().getContents();
			int n = arritemStack.length;
			int n2 = 0;
			while (n2 < n) {
				ItemStack is = arritemStack[n2];
				if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasDisplayName()
						&& Enchants.getPlus(is) > 3) {
					is.addUnsafeEnchantment(Enchants.glow, 1);
				}
				++n2;
			}
		}
	}

	@EventHandler
	public void onMapOpen(PlayerInteractEvent e) {
		if (DeployCommand.patchlockdown) {
			e.setCancelled(true);
			return;
		}
		Player p = e.getPlayer();
		if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& p.getInventory().getItemInMainHand().getType() == Material.EMPTY_MAP) {
			e.setCancelled(true);
			p.updateInventory();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGemPickup(PlayerPickupItemEvent e) {
		if (DeployCommand.patchlockdown) {
			e.setCancelled(true);
			return;
		}
		Player p = e.getPlayer();
		ItemStack itemStack = e.getItem().getItemStack();
		if (!e.isCancelled() && itemStack.getType() == Material.EMERALD) {
			e.getItem().remove();
			e.setCancelled(true);
			if (!Toggles.getToggles(p.getUniqueId()).contains("Gems")) {
				p.getInventory().addItem(itemStack);
				p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "                    +" + ChatColor.GREEN
						+ itemStack.getAmount() + ChatColor.GREEN + ChatColor.BOLD + "G");
				p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onDamagePercent(EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			if (e.getDamage() <= 0.0) {
				return;
			}
			if (DeployCommand.patchlockdown) {
				e.setCancelled(true);
				return;
			}
			LivingEntity p = (LivingEntity) e.getEntity();
			if (e.getCause().equals(DamageCause.FIRE) || e.getCause().equals(DamageCause.LAVA)
					|| e.getCause().equals(DamageCause.FIRE_TICK)) {
				if (!this.firedmg.containsKey(p.getUniqueId()) || this.firedmg.containsKey(p.getUniqueId())
						&& System.currentTimeMillis() - this.firedmg.get(p.getUniqueId()) > 500) {
					this.firedmg.put(p.getUniqueId(), System.currentTimeMillis());
					double multiplier = 0.01;
					if (e.getCause().equals(DamageCause.FIRE) || e.getCause().equals(DamageCause.LAVA)) {
						multiplier = 0.03;
					}
					if (p.getMaxHealth() * multiplier < 1.0) {
						e.setDamage(1.0);
					} else {
						e.setDamage(p.getMaxHealth() * multiplier);
					}
				} else {
					e.setDamage(0.0);
					e.setCancelled(true);
				}
			} else if (e.getCause().equals(DamageCause.POISON)) {
				if (p.getMaxHealth() * 0.01 >= p.getHealth()) {
					e.setDamage(p.getHealth() - 1.0);
				} else if (p.getMaxHealth() * 0.01 < 1.0) {
					e.setDamage(1.0);
				} else {
					e.setDamage(p.getMaxHealth() * 0.01);
				}
			} else if (e.getCause().equals(DamageCause.DROWNING)) {
				if (p.getMaxHealth() * 0.04 < 1.0) {
					e.setDamage(1.0);
				} else {
					e.setDamage(p.getMaxHealth() * 0.04);
				}
			} else if (e.getCause().equals(DamageCause.WITHER)) {
				e.setCancelled(true);
				e.setDamage(0.0);
				if (p.hasPotionEffect(PotionEffectType.WITHER)) {
					p.removePotionEffect(PotionEffectType.WITHER);
				}
			} else if (e.getCause().equals(DamageCause.SUFFOCATION)) {
				e.setDamage(0.0);
				e.setCancelled(true);
				Location loc = p.getLocation();
				while ((loc.getBlock().getType() != Material.AIR
						|| loc.add(0.0, 1.0, 0.0).getBlock().getType() != Material.AIR) && loc.getY() < 255.0) {
					loc.add(0.0, 1.0, 0.0);
				}
				p.teleport(loc);
			} else if (e.getCause().equals(DamageCause.VOID)) {
				e.setDamage(0.0);
				e.setCancelled(true);
				if (p instanceof Player) {
					Player pl = (Player) p;
					if (Alignments.chaotic.containsKey(pl.getName())) {
						p.teleport(TeleportBooks.generateRandomSpawnPoint(pl.getName()));
					} else {
						p.teleport(TeleportBooks.stonePeaks);
					}
				}
			} else if (e.getCause().equals(DamageCause.FALL)) {
				if (e.getDamage() * p.getMaxHealth() * 0.02 >= p.getHealth()) {
					e.setDamage(p.getHealth() - 1.0);
				} else if (e.getDamage() * p.getMaxHealth() * 0.02 < 1.0) {
					e.setDamage(1.0);
				} else {
					e.setDamage(e.getDamage() * p.getMaxHealth() * 0.02);
				}
			}
			if (e.getDamage() > p.getHealth() && Duels.duelers.containsKey(p)) {
				Duels.duelers.get(p).exitDuel(false, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
		e.setDroppedExp(0);
		e.setDeathMessage(null);
		Alignments.tagged.remove(p.getName());
		combat.remove(p.getName());
	}



	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (DeployCommand.patchlockdown) {
			e.setCancelled(true);
			return;
		}
		Player p = (Player) e.getWhoClicked();
		if (e.getSlotType() == InventoryType.SlotType.ARMOR && (e.isLeftClick() || e.isRightClick() || e.isShiftClick())
				&& (Items.isArmor(e.getCurrentItem()) && Items.isArmor(e.getCursor())
						|| Items.isArmor(e.getCurrentItem())
								&& (e.getCursor() == null || e.getCursor().getType() == Material.AIR)
						|| (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
								&& Items.isArmor(e.getCursor()))) {
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
		}
		if (e.getInventory().getHolder() == p) {
			if (e.isShiftClick() && e.getCurrentItem().getType().name().contains("_HELMET")
					&& p.getInventory().getHelmet() == null) {
				p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
			}
			if (e.isShiftClick() && e.getCurrentItem().getType().name().contains("_CHESTPLATE")
					&& p.getInventory().getChestplate() == null) {
				p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
			}
			if (e.isShiftClick() && e.getCurrentItem().getType().name().contains("_LEGGINGS")
					&& p.getInventory().getLeggings() == null) {
				p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
			}
			if (e.isShiftClick() && e.getCurrentItem().getType().name().contains("_BOOTS")
					&& p.getInventory().getBoots() == null) {
				p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
			}
		}
		new BukkitRunnable() {

			public void run() {
				Listeners.hpCheck(p);
			}
		}.runTaskLaterAsynchronously(PracticeServer.plugin, 1);
	}

	@EventHandler
	public void onWeaponSwitch(PlayerItemHeldEvent e) {
		if (DeployCommand.patchlockdown) {
			e.setCancelled(true);
			return;
		}
		Player p = e.getPlayer();
		ItemStack newItem = p.getInventory().getItem(e.getNewSlot());
		if (newItem != null && (newItem.getType().name().contains("_SWORD") || newItem.getType().name().contains("_AXE")
				|| newItem.getType().name().contains("_HOE") || newItem.getType().name().contains("_SPADE"))) {
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.5f);
		}
	}

	@EventHandler
	public void onTag(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getDamage() <= 0.0) {
				return;
			}
			Player p = (Player) e.getEntity();
			if (e.getCause() != DamageCause.FALL) {
				Alignments.tagged.put(p.getName(), System.currentTimeMillis());
			}
		}
	}

	@EventHandler
	public void onHitTag(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity) {
			if (e.getDamage() <= 0.0) {
				return;
			}
			Player p = (Player) e.getDamager();
			Alignments.tagged.put(p.getName(), System.currentTimeMillis());
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (DeployCommand.patchlockdown) {
			event.setCancelled(true);
			return;
		}
		ItemStack itemStack = event.getItemDrop().getItemStack();
		if (itemStack != null && (itemStack.getType() != Material.AIR) && (itemStack.hasItemMeta())
				&& (itemStack.getItemMeta().hasLore())) {
			me.retrorealms.practiceserver.utils.GlowAPI.setGlowing(event.getItemDrop(), groupOf(itemStack));
		}
	}

	public static GlowAPI.Color groupOf(ItemStack itemStack) {

		for (String string : itemStack.getItemMeta().getLore()) {
			if (string.contains("Common")) {
				return GlowAPI.Color.WHITE;
			} else if (string.contains("Uncommon")) {
				return GlowAPI.Color.GREEN;
			} else if (string.contains("Rare")) {
				return GlowAPI.Color.AQUA;
			} else if (string.contains("Unique")) {
				return GlowAPI.Color.YELLOW;
			}
		}

		return null;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onKickLog(PlayerKickEvent e) {
		Player p = e.getPlayer();
		if (!Alignments.isSafeZone(p.getLocation()) && Alignments.tagged.containsKey(p.getName())
				&& System.currentTimeMillis() - Alignments.tagged.get(p.getName()) < 10000) {
			p.setHealth(0.0);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuitLog(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if ((!Alignments.isSafeZone(p.getLocation()) && Alignments.tagged.containsKey(p.getName())
				&& System.currentTimeMillis() - Alignments.tagged.get(p.getName()) < 10000)
				|| !Alignments.isSafeZone(p.getLocation()) && combat.containsKey(p.getName())
						&& System.currentTimeMillis() - combat.get(p.getName()) < 10000) {
			Alignments.logout = true;
			p.setHealth(0.0);
			if (Alignments.chaotic.containsKey(p.getName()) || (Alignments.neutral.containsKey(p.getName()))) {
				for (Player player : p.getWorld().getPlayers()) {
					player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 1.0F, 1.0F);
				}
			}
		}
	}

	@EventHandler
	public void onHealthRegen(EntityRegainHealthEvent e) {
		e.setCancelled(true);
	}

	public static void Kit(Player p) {
		ItemStack S;
		if(!Toggles.getToggleStatus(p,"Disable Kit")){
			PlayerInventory i = p.getInventory();
			Random random = new Random();
			int min = random.nextInt(2) + 4;
			int max = random.nextInt(2) + 8;
			int wep = random.nextInt(2) + 1;
			if (wep == 1)
			{
				ItemStack s = new ItemStack(Material.WOOD_SWORD);
				ItemMeta smeta = s.getItemMeta();
				smeta.setDisplayName(ChatColor.WHITE.toString() +
						"Training Sword");
				List<String> slore = new ArrayList<String>();
				slore.add(ChatColor.RED + "DMG: " + min + " - " +
						max);
				slore.add(ChatColor.GRAY + "Untradeable");
				smeta.setLore(slore);
				s.setItemMeta(smeta);
				i.addItem(new ItemStack[] { s });
			}
			if (wep == 2)
			{
				ItemStack s = new ItemStack(Material.WOOD_AXE);
				ItemMeta smeta = s.getItemMeta();
				smeta.setDisplayName(ChatColor.WHITE.toString() +
						"Training Hatchet");
				List<String> slore = new ArrayList<String>();
				slore.add(ChatColor.RED.toString() + "DMG: " + min + " - " +
						max);
				slore.add(ChatColor.GRAY.toString() + "Untradeable");
				smeta.setLore(slore);
				s.setItemMeta(smeta);
				i.addItem(new ItemStack[] { s });
			}
		}

        p.getInventory().addItem(Horses.mount(Horses.horseTier.get(p), false));
		p.getInventory().addItem(Hearthstone.hearthstone());
		p.getInventory().addItem(Journal.journal());
		p.setMaxHealth(50.0);
		p.setHealth(50.0);
		p.setHealthScale(20.0);
		p.setHealthScaled(true);
	}

	@EventHandler
	public void onhit(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof LivingEntity))
			return;
		LivingEntity l = (LivingEntity) e.getEntity();
		if (Mobs.isFrozenBoss(l) || Mobs.isGolemBoss(l)) {
			e.setCancelled(true);
			e.getEntity().setVelocity(new Vector());
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), new Runnable() {
				public void run() {
					e.getEntity().setVelocity(new Vector());
				}
			}, 1L);
		}
	}

	private static ItemStack createPotion() {
		Potion potion = new Potion(PotionType.INSTANT_HEAL);
		ItemStack itemStack = potion.toItemStack(1);
		PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
		potionMeta.setDisplayName(ChatColor.GREEN + "Health Potion");
		potionMeta.setLore(Arrays.asList(ChatColor.GRAY + "A potion that restores " + ChatColor.AQUA + "75HP",
				ChatColor.GRAY + "Untradeable"));
		for (ItemFlag itemFlag : ItemFlag.values()) {
			potionMeta.addItemFlags(itemFlag);
		}
		itemStack.setItemMeta(potionMeta);
		return itemStack;
	}

}