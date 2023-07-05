
package me.retrorealms.practiceserver.mechanics.damage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.commands.moderation.ToggleGMCommand;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.WepTrak;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.guilds.GuildMechanics;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.mobs.Mobs;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBossHandler;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses.Frostwing;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.player.Energy;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.vendors.Merchant;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


import me.retrorealms.practiceserver.mechanics.player.Toggles;

public class Damage implements Listener {
	HashMap<Player, Long> playerslow = new HashMap<Player, Long>();
	public static HashMap<Player, Long> lasthit = new HashMap<Player, Long>();
	public static HashMap<Player, Player> lastphit = new HashMap<Player, Player>();
	ConcurrentHashMap<UUID, Long> kb = new ConcurrentHashMap<UUID, Long>();
	ArrayList<String> p_arm = new ArrayList<String>();

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, PracticeServer.getInstance());

		new BukkitRunnable() {

			public void run() {

				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					double healthPercentage = (p.getHealth() / p.getMaxHealth());
					String safeZone = Alignments.isSafeZone(p.getLocation()) ? ChatColor.GRAY + " - " + ChatColor.GREEN.toString() + ChatColor.BOLD + "SAFE-ZONE" : "";
					if (healthPercentage * 100.0F > 100.0F) {
						healthPercentage = 1.0;
					}
					float pcnt = (float) (healthPercentage * 1.F);
					BarColor barColor = getBarColor(p);
					ChatColor titleColor = barTitleColor(p);
					if (!Alignments.playerBossBars.containsKey(p)) {
						// Set new one
						BossBar bossBar = Bukkit.createBossBar(
								titleColor + "" + ChatColor.BOLD + "HP " + titleColor
										+ (int) p.getHealth() + titleColor + ChatColor.BOLD + " / "
										+ titleColor + (int) p.getMaxHealth() + safeZone,
								barColor, BarStyle.SOLID);
						bossBar.addPlayer(p);
						Alignments.playerBossBars.put(p, bossBar);
						Alignments.playerBossBars.get(p).setProgress(pcnt);
					} else {
						Alignments.playerBossBars.get(p).setColor(barColor);
						Alignments.playerBossBars.get(p)
								.setTitle(titleColor + "" + ChatColor.BOLD + "HP "
										+ titleColor + (int) p.getHealth() + titleColor
										+ ChatColor.BOLD + " / " + titleColor + (int) p.getMaxHealth() + safeZone);
						Alignments.playerBossBars.get(p).setProgress(pcnt);
					}
				}
			}
		}.runTaskTimerAsynchronously(PracticeServer.plugin, 0, 1);
		new BukkitRunnable() {

			public void run() {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					if (Damage.this.playerslow.containsKey(p)) {
						if (System.currentTimeMillis() - Damage.this.playerslow.get(p) <= 3000)
							continue;
						syncSpeed(p, 0.2f);
						continue;
					}
					if (p.getWalkSpeed() == 0.2f)
						continue;
					syncSpeed(p, 0.2f);
				}
			}
		}.runTaskTimerAsynchronously(PracticeServer.plugin, 20, 20);
	}

	public void syncSpeed(Player player, float f) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin, () -> player.setWalkSpeed(f));
	}

	public void onDisable() {
	}

	public static BarColor getBarColor(Player player) {
		double maxHealth = player.getMaxHealth();
		double currentHealth = player.getHealth();
		if((currentHealth / maxHealth) > .5) return BarColor.GREEN;
		if((currentHealth / maxHealth) < .25) return BarColor.RED;
		if((currentHealth / maxHealth) < .5) return BarColor.YELLOW;
		return BarColor.RED;
	}

	public static ChatColor barTitleColor(Player player){
		double maxHealth = player.getMaxHealth();
		double currentHealth = player.getHealth();
		if((currentHealth / maxHealth) > .5) return ChatColor.GREEN;
		if((currentHealth / maxHealth) < .25) return ChatColor.RED;
		if((currentHealth / maxHealth) < .5) return ChatColor.YELLOW;
		return ChatColor.RED;
	}

	public static int getHp(ItemStack is) {
		List<String> lore;
		if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()
				&& (lore = is.getItemMeta().getLore()).size() > 1 && lore.get(1).contains("HP")) {
			try {
				return Integer.parseInt(lore.get(1).split(": +")[1]);
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}

	public static int getArmor(ItemStack is) {
		List<String> lore;
		if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()
				&& (lore = is.getItemMeta().getLore()).size() > 0 && lore.get(0).contains("ARMOR")) {
			try {
				return Integer.parseInt(lore.get(0).split(" - ")[1].split("%")[0]);
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}

	public static int getDps(ItemStack is) {
		List<String> lore;
		if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()
				&& (lore = is.getItemMeta().getLore()).size() > 0 && lore.get(0).contains("DPS")) {
			try {
				return Integer.parseInt(lore.get(0).split(" - ")[1].split("%")[0]);
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}

	public static int getEnergy(ItemStack is) {
		List<String> lore;
		if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()
				&& (lore = is.getItemMeta().getLore()).size() > 2 && lore.get(2).contains("ENERGY REGEN")) {
			try {
				return Integer.parseInt(lore.get(2).split(": +")[1].split("%")[0]);
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}



	public static int getHps(ItemStack is) {
		List<String> lore;
		if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()
				&& (lore = is.getItemMeta().getLore()).size() > 2 && lore.get(2).contains("HP REGEN")) {
			try {
				return Integer.parseInt(lore.get(2).split(": +")[1].split("/s")[0]);
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}

	public static int getPercent(ItemStack is, String type) {
		if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()) {
			List<String> lore = is.getItemMeta().getLore();
			for (String s : lore) {
				if (!s.contains(type))
					continue;
				try {
					return Integer.parseInt(s.split(": ")[1].split("%")[0]);
				} catch (Exception e) {
					return 0;
				}
			}
		}
		return 0;
	}

	public static int getElem(ItemStack is, String type) {
		if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()) {
			List<String> lore = is.getItemMeta().getLore();
			for (String s : lore) {
				if (!s.contains(type))
					continue;
				try {
					return Integer.parseInt(s.split(": +")[1]);
				} catch (Exception e) {
					return 0;
				}
			}
		}
		return 0;
	}

	public static List<Integer> getDamageRange(ItemStack is) {
		List<String> lore;
		ArrayList<Integer> dmg = new ArrayList<Integer>();
		dmg.add(1);
		dmg.add(1);
		if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()
				&& (lore = is.getItemMeta().getLore()).size() > 0 && lore.get(0).contains("DMG")) {
			try {
				int min = 1;
				int max = 1;
				min = Integer.parseInt(lore.get(0).split("DMG: ")[1].split(" - ")[0]);
				max = Integer.parseInt(lore.get(0).split(" - ")[1]);
				dmg.set(0, min);
				dmg.set(1, max);
			} catch (Exception e) {
				dmg.set(0, 1);
				dmg.set(1, 1);
			}
		}
		return dmg;
	}

	public static int getCrit(Player p) {
		int crit = 0;
		ItemStack wep = p.getInventory().getItemInMainHand();
		if (Staffs.staff.containsKey(p)) {
			wep = Staffs.staff.get(p);
		}
		if (wep != null && wep.getType() != Material.AIR && wep.getItemMeta().hasLore()) {
			List<String> lore = wep.getItemMeta().getLore();
			for (String line : lore) {
				if (!line.contains("CRITICAL HIT"))
					continue;
				crit = Damage.getPercent(wep, "CRITICAL HIT");
			}
			if (wep.getType().name().contains("_AXE")) {
				crit += 10;
			}
			int intel = 0;
			ItemStack[] arritemStack = p.getInventory().getArmorContents();
			int n = arritemStack.length;
			int n2 = 0;
			while (n2 < n) {
				ItemStack is = arritemStack[n2];
				if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
					int addint = Damage.getElem(is, "INT");
					intel += addint;
				}
				++n2;
			}
			if (intel > 0) {
				crit = (int) ((long) crit + Math.round((double) intel * 0.015));
			}
		}
		return crit;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onNpcDamage(EntityDamageEvent e) {
		if (e.getEntity().hasMetadata("pet"))
			e.setCancelled(true);

		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();

			if (p.hasMetadata("NPC")) {
				e.setCancelled(true);
				e.setDamage(0.0);
			}
			if (p.isOp() || p.getGameMode() == GameMode.CREATIVE || p.isFlying()) {
				if (!ToggleGMCommand.togglegm.contains(p.getName())) {
					e.setCancelled(true);
					e.setDamage(0.0);
				}
			}
		}
	}

	public void callHGDMG(Player p, LivingEntity le, String d, int dmg) {
		ArrayList<String> gettoggles = Toggles.getToggles(p.getUniqueId());

		if (gettoggles.contains("Hologram Damage")) {
			Random r = new Random();
			float x = r.nextFloat();
			float y = r.nextFloat();
			float z = r.nextFloat();
			int dmgs = dmg;
			Hologram hologram = HologramsAPI.createHologram(PracticeServer.getInstance(), le.getLocation().clone().add(x, 0.5 + y, z));
			if (d.equalsIgnoreCase("dmg")) {
				hologram.appendTextLine( ChatColor.RED + "-" + dmg + "❤");
			}
			if (d.equalsIgnoreCase("dodge")) {
				hologram.appendTextLine( ChatColor.RED + "*DODGE*");
			}
			if (d.equalsIgnoreCase("block")) {
				hologram.appendTextLine( ChatColor.RED + "*BLOCK*");
			}
			new BukkitRunnable() {
				@Override
				public void run() {
					hologram.delete();
				}
			}.runTaskLater(PracticeServer.plugin, 20L);
		}
	}

	@EventHandler
	public void holoDMG(EntityDamageByEntityEvent e) {
		if (e.getDamage() <= 0) {
			return;
		}
		try {
			ArrayList<String> gettoggles = Toggles.getToggles(e.getDamager().getUniqueId());
			if (e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Player) {
				if (e.getDamage() > 0 && !e.isCancelled()) {
					Player p = (Player) e.getDamager();
					LivingEntity le = (LivingEntity) e.getEntity();
					int dmg = (int) e.getDamage();
					callHGDMG(p, le, "dmg", dmg);
				}
			}
		} catch (Exception ex) {
			System.out.println("HoloDMG");
		}
	}

	@EventHandler
	public void onDummyUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {

			Block block = event.getClickedBlock();

			if (block == null)
				return;

			if (block.getType() == Material.ARMOR_STAND) {
				ItemStack wep = player.getInventory().getItemInMainHand();

				if (wep != null && wep.getType() != Material.AIR && wep.getItemMeta().hasLore()) {

					int min = Damage.getDamageRange(wep).get(0);
					int max = Damage.getDamageRange(wep).get(1);

					int damage = ThreadLocalRandom.current().nextInt(min, max);

					for (String line : wep.getItemMeta().getLore()) {
						int eldmg;
						if (line.contains("ICE DMG")) {
							eldmg = Damage.getElem(wep, "ICE DMG");
							damage += eldmg;
						}
						if (line.contains("POISON DMG")) {
							eldmg = Damage.getElem(wep, "POISON DMG");
							damage += eldmg;
						}
						if (line.contains("FIRE DMG")) {
							eldmg = Damage.getElem(wep, "FIRE DMG");
							damage += eldmg;
						}
						if (!line.contains("PURE DMG")) {
							eldmg = Damage.getElem(wep, "PURE DMG");
							damage += eldmg;
						}
					}

					double dps = 0.0;
					double vit = 0.0;
					double dex = 0.0;
					double intel = 0.0;
					double str = 0.0;
					ItemStack[] arritemStack = player.getInventory().getArmorContents();
					int n = arritemStack.length;
					int n4 = 0;
					while (n4 < n) {
						ItemStack is = arritemStack[n4];
						if (is != null && is.getType() != Material.AIR && is.hasItemMeta()
								&& is.getItemMeta().hasLore()) {
							int adddps = Damage.getDps(is);
							dps += (double) adddps;
							int addvit = Damage.getElem(is, "VIT");
							vit += (double) addvit;
							int adddex = Damage.getElem(is, "DEX");
							dex += (double) adddex;
							int addint = Damage.getElem(is, "INT");
							intel += (double) addint;
							int addstr = Damage.getElem(is, "STR");
							str += (double) addstr;
						}
						++n4;
					}
					if (vit > 0.0 && wep.getType().name().contains("_SWORD")) {
						double divide = vit / 5000.0;
						double pre = (double) damage * divide;
						damage = (int) ((double) damage + pre);
					}
					if (str > 0.0 && wep.getType().name().contains("_AXE")) {
						double divide = str / 5000.0;
						double pre = (double) damage * divide;
						damage = (int) ((double) damage + pre);
					}
					if (intel > 0.0 && wep.getType().name().contains("_HOE")) {
						double divide = intel / 100.0;
						double pre = (double) damage * divide;
						damage = (int) ((double) damage + pre);
					}
					if (dps > 0.0) {
						double divide = dps / 100.0;
						double pre = (double) damage * divide;
						damage = (int) ((double) damage + pre);
					}

					event.setCancelled(true);

					player.sendMessage(
							ChatColor.RED + "            " + damage + ChatColor.RED + ChatColor.BOLD + " DMG "
									+ ChatColor.RED + "-> " + ChatColor.RESET + "DPS DUMMY" + " [" + 99999999 + "HP]");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlodge(EntityDamageByEntityEvent e) {
		if (DeployCommand.patchlockdown) {
			e.setCancelled(true);
			return;
		}
		Player p;
		int crit = 0;
		if (e.getDamager() instanceof Player) {
			Player dmgr = (Player) e.getDamager();
			crit = Damage.getCrit(dmgr);
		}
		Random random = new Random();
		int drop = random.nextInt(100) + 1;
		if (e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Player) {
				Player d = (Player) e.getDamager();
				Player x = (Player) e.getEntity();
				if (Duels.duelers.containsKey(x) && Duels.duelers.containsKey(d)) {
					if (Duels.duelers.get(x).team == Duels.duelers.get(d).team) {
						e.setDamage(0.0);
						e.setCancelled(true);
					}
				}
			}
			if (e.getDamage() <= 0.0) {
				return;
			}
			p = (Player) e.getEntity();
			PlayerInventory i = p.getInventory();
			p.setNoDamageTicks(0);
			int block = 0;
			int dodge = 0;
			if (p.getHealth() > 0.0) {
				ItemStack[] arritemStack = i.getArmorContents();
				int n = arritemStack.length;
				int n2 = 0;
				while (n2 < n) {
					ItemStack is = arritemStack[n2];
					if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
						int addedblock = Damage.getPercent(is, "BLOCK");
						block += addedblock;
						int addeddodge = Damage.getPercent(is, "DODGE");
						dodge += addeddodge;
					}
					++n2;
				}
				int str = 0;
				int dex = 0;
				ItemStack[] addedblock = p.getInventory().getArmorContents();
				int n3 = addedblock.length;
				n = 0;
				while (n < n3) {
					ItemStack is = addedblock[n];
					if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
						int addstr = Damage.getElem(is, "STR");
						str += addstr;
						int adddex = Damage.getElem(is, "DEX");
						dex += adddex;
					}
					++n;
				}
				if (str > 0) {
					block = (int) ((long) block + Math.round((double) str * 0.015));
				}
				if (dex > 0) {
					dodge = (int) ((long) dodge + Math.round((double) dex * 0.015));
				}
				random = new Random();
				int dodger = random.nextInt(110) + 1;
				int blockr = random.nextInt(110) + 1;
				if (drop < crit && ((Player) e.getDamager()).getInventory().getItemInMainHand().getType().name()
						.contains("_AXE")) {
					block = 0;
					dodge = 0;
				}
				if (e.getDamager() instanceof Player) {
					int accuracy;
					Player d = (Player) e.getDamager();
					ItemStack wep = d.getInventory().getItemInMainHand();
					if (Staffs.staff.containsKey(d)) {
						wep = Staffs.staff.get(d);
					}
					if ((accuracy = Damage.getPercent(wep, "ACCURACY")) > 0) {
						block -= accuracy;
						dodge -= accuracy;
					}
					if (blockr <= block) {
						e.setDamage(0.0);
						e.setCancelled(true);
						p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
						callHGDMG(d, p, "block", 0);
						if(Toggles.getToggleStatus(d, "Debug")) d.sendMessage("          " + ChatColor.RED + ChatColor.BOLD + "*OPPONENT BLOCKED* ("
								+ (PracticeServer.FFA ? "Anonymous" : p.getName()) + ")");
						if(Toggles.getToggleStatus(p, "Debug"))p.sendMessage("          " + ChatColor.DARK_GREEN + ChatColor.BOLD + "*BLOCK* ("
								+ (PracticeServer.FFA ? "Anonymous" : d.getName()) + ")");
					} else if (dodger <= dodge) {
						e.setDamage(0.0);
						e.setCancelled(true);
						p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);
						callHGDMG(d, p, "dodge", 0);
						if(Toggles.getToggleStatus(d, "Debug"))d.sendMessage("          " + ChatColor.RED + ChatColor.BOLD + "*OPPONENT DODGED* ("
								+ (PracticeServer.FFA ? "Anonymous" : p.getName()) + ")");
						if(Toggles.getToggleStatus(p, "Debug"))p.sendMessage("          " + ChatColor.GREEN + ChatColor.BOLD + "*DODGE* ("
								+ (PracticeServer.FFA ? "Anonymous" : d.getName()) + ")");
					} else if (blockr <= 80 && p.isBlocking()) {
						e.setDamage((double) ((int) e.getDamage() / 2));
						callHGDMG(d, p, "block", 0);
						p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
						if(Toggles.getToggleStatus(d, "Debug")) d.sendMessage("          " + ChatColor.RED + ChatColor.BOLD + "*OPPONENT BLOCKED* ("
								+ (PracticeServer.FFA ? "Anonymous" : p.getName()) + ")");
						if(Toggles.getToggleStatus(p, "Debug")) p.sendMessage("          " + ChatColor.DARK_GREEN + ChatColor.BOLD + "*BLOCK* ("
								+ (PracticeServer.FFA ? "Anonymous" : d.getName()) + ")");
					}
				} else if (e.getDamager() instanceof LivingEntity) {
					LivingEntity li = (LivingEntity) e.getDamager();
					if (Mobs.isFrozenBoss(li) || Mobs.isGolemBoss(li)) {
						block -= 25;
						dodge -= 25;
					}
					String mname = "";
					if (li.hasMetadata("name")) {
						mname = li.getMetadata("name").get(0).asString();
					}
					if (blockr <= block) {
						e.setDamage(0.0);
						e.setCancelled(true);
						callHGDMG(p, li, "block", 0);
						p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
						if(Toggles.getToggleStatus(p, "Debug"))p.sendMessage("          " + ChatColor.DARK_GREEN + ChatColor.BOLD + "*BLOCK* (" + mname
								+ ChatColor.DARK_GREEN + ")");
					} else if (dodger <= dodge) {
						e.setDamage(0.0);
						e.setCancelled(true);
						callHGDMG(p, li, "dodge", 0);
						p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);
						if(Toggles.getToggleStatus(p, "Debug")) p.sendMessage("          " + ChatColor.GREEN + ChatColor.BOLD + "*DODGE* (" + mname
								+ ChatColor.GREEN + ")");
					} else if (blockr <= 80 && p.isBlocking()) {
						e.setDamage((double) ((int) e.getDamage() / 2));
						callHGDMG(p, li, "block", 0);
						p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
						if(Toggles.getToggleStatus(p , "Debug"))p.sendMessage("          " + ChatColor.DARK_GREEN + ChatColor.BOLD + "*BLOCK* (" + mname
								+ ChatColor.DARK_GREEN + ")");
					}
				}
			}
		}
		if (e.getDamage() <= 0.0) {
			return;
		}
		if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity) {
			p = (Player) e.getDamager();
			LivingEntity li = (LivingEntity) e.getEntity();
			ItemStack wep = p.getInventory().getItemInMainHand();
			if (Staffs.staff.containsKey(p)) {
				wep = Staffs.staff.get(p);
			}
			if (p.getInventory().getItemInOffHand().getType() != Material.AIR) {
				ItemStack material = p.getInventory().getItemInOffHand();
				p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
				if (p.getInventory().firstEmpty() == -1) {
					p.getWorld().dropItemNaturally(p.getLocation(), material);
				} else {
					p.getInventory().addItem(material);
				}
			}
			if (wep != null && wep.getType() != Material.AIR && wep.getItemMeta().hasLore()) {
				int min = Damage.getDamageRange(wep).get(0);
				int max = Damage.getDamageRange(wep).get(1);
				p.setNoDamageTicks(0);
				random = new Random();
				int dmg = random.nextInt(max - min + 1) + min;

				int tier = Items.getTierFromColor(wep);
				List<String> lore = wep.getItemMeta().getLore();
				for (String line : lore) {
					int eldmg;
					if (line.contains("ICE DMG")) {
						DyeColor blockColor = DyeColor.LIGHT_BLUE;
						MaterialData data = new MaterialData(Material.STAINED_GLASS_PANE, (byte)3);
						li.getWorld().spawnParticle(Particle.BLOCK_CRACK, li.getLocation().clone().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.01, data);
						li.getWorld().playSound(li.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1, 1);
						eldmg = Damage.getElem(wep, "ICE DMG");
						int elemult = Math.round(eldmg * (1 + Math.round(Damage.getElem(wep, "DEX") / 3000)));
						dmg += elemult;

						if (tier == 1) {
							li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
						}
						if (tier == 2) {
							li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
						}
						if (tier == 3) {
							li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
						}
						if (tier == 4) {
							li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
						}
						if (tier >= 5) {
							li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));
						}
					}
					if (line.contains("POISON DMG")) {
						boolean color = ThreadLocalRandom.current().nextBoolean();
						byte bytes = color ? (byte)5 : (byte)13;
						MaterialData data = new MaterialData(Material.STAINED_GLASS, bytes);
						li.getWorld().spawnParticle(Particle.BLOCK_CRACK, li.getLocation().clone().add(0,1,0), 10, 0.5, 0.5, 0.5, 0.01, data);
						li.getWorld().playSound(li.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1, 1);
						eldmg = Damage.getElem(wep, "POISON DMG");
						int elemult = Math.round(eldmg * (1 + Math.round(Damage.getElem(wep, "DEX") / 3000)));
						dmg += elemult;

						if (tier == 1) {
							li.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 15, 0));
						}
						if (tier == 2) {
							li.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 25, 0));
						}
						if (tier == 3) {
							li.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 1));
						}
						if (tier == 4) {
							li.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 35, 1));
						}
						if (tier >= 5) {
							li.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
						}
					}
					if (line.contains("FIRE DMG")) {
						eldmg = Damage.getElem(wep, "FIRE DMG");
						int elemult = Math.round(eldmg * (1 + Math.round(Damage.getElem(wep, "DEX") / 3000)));
						dmg += elemult;
						if (tier == 1) {
							li.setFireTicks(15);
						}
						if (tier == 2) {
							li.setFireTicks(25);
						}
						if (tier == 3) {
							li.setFireTicks(30);
						}
						if (tier == 4) {
							li.setFireTicks(35);
						}
						if (tier >= 5) {
							li.setFireTicks(40);
						}
					}
					if (line.contains("PURE DMG")) {
						eldmg = Damage.getElem(wep, "PURE DMG");
						int elemult = Math.round(eldmg * (1 + Math.round(Damage.getElem(wep, "DEX") / 3000)));
						dmg += elemult;
					}
					if (li instanceof Player && line.contains("VS PLAYERS")) {
						int addedDMG = dmg * getPercent(wep, "VS PLAYERS") / 100;
						dmg += addedDMG;
					} else if (!(li instanceof Player) && line.contains("VS MONSTERS")) {
						int addedDMG = dmg * getPercent(wep, "VS MONSTERS") / 100;
						dmg += addedDMG;
					}

				}
				if (drop <= crit) {
					e.setCancelled(false);
					dmg *= 2;
					p.playSound(p.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.5f, 0.5f);
					Particles.CRIT_MAGIC.display(0.0f, 0.0f, 0.0f, 1.0f, 50, li.getLocation(), 20.0);
				}
				PlayerInventory i = p.getInventory();
				double dps = 0.0;
				double vit = 0.0;
				double dex = 0.0;
				double str = 0.0;
				ItemStack[] arritemStack = i.getArmorContents();
				int n = arritemStack.length;
				int n4 = 0;
				while (n4 < n) {
					ItemStack is = arritemStack[n4];
					if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
						int adddps = Damage.getDps(is);
						dps += (double) adddps;
						int addvit = Damage.getElem(is, "VIT");
						vit += (double) addvit;
						int addstr = Damage.getElem(is, "STR");
						str += (double) addstr;
						int adddex = Damage.getElem(is, "DEX");
						dps += (double) Math.round(adddex * 0.012);
					}
					++n4;
				}
				if (vit > 0.0 && wep.getType().name().contains("_SWORD")) {
					double divide = vit / 5000.0;
					double pre = (double) dmg * divide;
					dmg = (int) ((double) dmg + pre);
				}
				if (str > 0.0 && wep.getType().name().contains("_AXE")) {
					double divide = str / 4500.0;
					double pre = (double) dmg * divide;
					dmg = (int) ((double) dmg + pre);
				}
				if (dps > 0.0) {
					double divide = dps / 100.0;
					double pre = (double) dmg * divide;
					dmg = (int) ((double) dmg + pre);
				}
				for (String line2 : lore) {
					ArrayList<String> toggles;
					if (!line2.contains("LIFE STEAL"))
						continue;
					if (e.getEntityType().equals(EntityType.ARMOR_STAND))
						continue;

					li.getWorld().playEffect(li.getEyeLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
					double base = Damage.getPercent(wep, "LIFE STEAL");
					double pcnt = base / 100.0;
					int life = 1;
					if ((int) (pcnt * (double) dmg) > 0) {
						life = (int) (pcnt * (double) dmg);
					}
					if (p.getHealth() < p.getMaxHealth() - (double) life) {
						p.setHealth(p.getHealth() + (double) life);
						toggles = Toggles.getToggles(p.getUniqueId());
						if (!toggles.contains("Debug"))
							continue;
						p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "            +" + ChatColor.GREEN
								+ life + ChatColor.GREEN + ChatColor.BOLD + " HP " + ChatColor.GRAY + "["
								+ (int) p.getHealth() + "/" + (int) p.getMaxHealth() + "HP]");
						continue;
					}
					if (p.getHealth() < p.getMaxHealth() - (double) life)
						continue;
					p.setHealth(p.getMaxHealth());
					toggles = Toggles.getToggles(p.getUniqueId());
					if (!toggles.contains("Debug"))
						continue;
					p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + " " + "           +" + ChatColor.GREEN
							+ life + ChatColor.GREEN + ChatColor.BOLD + " HP " + ChatColor.GRAY + "["
							+ (int) p.getMaxHealth() + "/" + (int) p.getMaxHealth() + "HP]");
				}
				e.setDamage((double) dmg);
				return;
			}
			e.setDamage(1.0);
		}
	}

	private boolean areSame(Player shooter, Player entity) {
		return shooter.getUniqueId().equals(entity.getUniqueId());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onArmor(EntityDamageByEntityEvent e) {
		Entity damagerentity = e.getDamager();
		Player damager;
		if (damagerentity instanceof Player) {
			damager = (Player) damagerentity;
		} else {
			damager = null;
		}
		if (e.getDamage() <= 0.0) {
			return;
		}
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			PlayerInventory i = p.getInventory();
			double dmg = e.getDamage();
			double arm = 0.0;
			ItemStack[] arritemStack = i.getArmorContents();
			int n = arritemStack.length;
			int n2 = 0;
			while (n2 < n) {
				ItemStack is = arritemStack[n2];
				if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
					int addarm = Damage.getArmor(is);
					int str;
					str = Damage.getElem(is, "STR");
					arm += (double) addarm;
					arm = (double) ((long) arm + Math.round((double) str * 0.012));
				}
				++n2;
			}
			ItemStack[] arritemStack2;

			double armorPen = 0;
			if (e.getDamager() instanceof LivingEntity && !(e.getDamager() instanceof Player)) {
				LivingEntity mobDamager = (LivingEntity) e.getDamager();
				if(WorldBossHandler.getActiveBoss() != null && WorldBossHandler.getActiveBoss() instanceof Frostwing) {
					Frostwing frostwing = (Frostwing) WorldBossHandler.getActiveBoss();
					if(frostwing.isBezerk()) {
						dmg = (e.getDamage() * .5) + e.getDamage();

					}
				}
				if (Mobs.isFrozenBoss(mobDamager) || Mobs.isGolemBoss(mobDamager)) {
					armorPen = arm / 3;
				}
			}
			if (damager != null) {
				arritemStack2 = damager.getInventory().getArmorContents();
				int n3 = arritemStack2.length;
				int n4 = 0;
				armorPen = Damage.getElem(damager.getInventory().getItemInMainHand(), "ARMOR PEN");
				while (n4 < n3) {
					ItemStack is = arritemStack2[n4];
					if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
						int dex = Damage.getElem(is, "DEX");
						armorPen = (double) ((long) armorPen + Math.round((double) dex * 0.02));
					}
					++n4;
				}
			}
			arm -= armorPen;
			if (arm > 80)
				arm = 80;
			if (arm > 0.0) {
				double divide = (arm / 1.3) / 100.0; //Artificially Lowers the Armor a Little so its not so OP
				if(divide > 0.65) divide = 0.65;
				double pre = dmg * divide;
				int cleaned = (int) (dmg - pre);
				if (cleaned <= 1) {
					cleaned = 1;
				}
				dmg = cleaned;
				int health = 0;
				if (p.getHealth() - (double) cleaned > 0.0) {
					health = (int) (p.getHealth() - (double) cleaned);
				}
				if ((Toggles.getToggles(p.getUniqueId())).contains("Debug")) {
					if (health < 0) {
						health = 0;
					}
					p.sendMessage(ChatColor.RED + "            -" + cleaned + ChatColor.RED + ChatColor.BOLD + "HP "
							+ ChatColor.GRAY + "[-" + (int) arm + "%A -> -" + (int) pre + ChatColor.BOLD + "DMG"
							+ ChatColor.GRAY + "] " + ChatColor.GREEN + "[" + health + ChatColor.BOLD + "HP"
							+ ChatColor.GREEN + "]");
				}
				e.setDamage((double) cleaned);
			} else {
				ArrayList<String> toggles = Toggles.getToggles(p.getUniqueId());
				if (toggles.contains("Debug")) {
					int health = (int) (p.getHealth() - dmg);
					if (health < 0) {
						health = 0;
					}
					p.sendMessage(ChatColor.RED + "            -" + (int) dmg + ChatColor.RED + ChatColor.BOLD + "HP "
							+ ChatColor.GRAY + "[-0%A -> -0" + ChatColor.BOLD + "DMG" + ChatColor.GRAY + "] "
							+ ChatColor.GREEN + "[" + health + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
				}

				e.setDamage(dmg);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDebug(EntityDamageByEntityEvent e) {
		try {
			if (e.getDamage() <= 0.0) {
				return;
			}
			if (e.getCause() == EntityDamageEvent.DamageCause.FIRE) {
				return;
			}
			if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
				Player p = (Player) e.getEntity();
				Player d = (Player) e.getDamager();
				int dmg = (int) e.getDamage();
				p.setNoDamageTicks(0);
				int health = 0;
				if (p.getHealth() - (double) dmg > 0.0) {
					health = (int) (p.getHealth() - (double) dmg);
				}
				ArrayList<String> toggles = Toggles.getToggles(d.getUniqueId());
				if (toggles.contains("Debug")) {
					d.sendMessage(ChatColor.RED + "            " + dmg + ChatColor.RED + ChatColor.BOLD + " DMG "
							+ ChatColor.RED + "-> " + (PracticeServer.FFA
									? (d.isOp() ? p.getName().toString() : "Anonymous") : p.getName().toString())
							+ " [" + health + "HP]");
				}
				lastphit.put(p, d);
				lasthit.put(p, System.currentTimeMillis());
			} else if (e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Player) {
				LivingEntity p = (LivingEntity) e.getEntity();
				Player d = (Player) e.getDamager();
				p.setNoDamageTicks(0);
				int dmg = (int) e.getDamage();
				int health = 0;
				if (p.getHealth() - (double) dmg > 0.0) {
					health = (int) (p.getHealth() - (double) dmg);
				}
				String name = "";
				if (p.hasMetadata("name")) {
					name = p.getMetadata("name").get(0).asString();
				}
				int tier = Mobs.getMobTier(p);
				if (tier != 0 && (health < 1)) {
					GuildPlayer guildPlayer = GuildPlayers.getInstance().get(d.getUniqueId());
					if (WepTrak.isStatTrak(d.getInventory().getItemInMainHand())) {
						WepTrak.incrementStat(d.getInventory().getItemInMainHand(), "mk");
					}
					switch (tier) {
					case 1:
						guildPlayer.setT1Kills(guildPlayer.getT1Kills() + 1);
						break;
					case 2:
						guildPlayer.setT2Kills(guildPlayer.getT2Kills() + 1);
						break;
					case 3:
						guildPlayer.setT3Kills(guildPlayer.getT3Kills() + 1);
						break;
					case 4:
						guildPlayer.setT4Kills(guildPlayer.getT4Kills() + 1);
						break;
					case 5:
						guildPlayer.setT5Kills(guildPlayer.getT5Kills() + 1);
						break;
					case 6:
						guildPlayer.setT6Kills(guildPlayer.getT6Kills() + 1);
						break;
					}
				}
				if ((Toggles.getToggles(d.getUniqueId())).contains("Debug")) {
					d.sendMessage(ChatColor.RED + "            " + dmg + ChatColor.RED + ChatColor.BOLD + " DMG "
							+ ChatColor.RED + "-> " + ChatColor.RESET + name + " [" + health + "HP]");
				}
			}
		} catch (Exception es) {
			System.out.println("onDebug");
		}
	}

	private final long knockbackDelay = 500; // Delay in milliseconds
	private final float playerKnockbackMultiplier = 0.54f;
	private final float playerSpadeKnockbackMultiplier = 0.9f;
	private final float nonPlayerKnockbackMultiplier = 0.5f;
	private final double verticalKnockback = 0.25;
	private final double SpadeVerticalKnockback = 0.4;

	// You can remove this if you're not using the 'kb' map for anything else.
	// private final Map<UUID, Long> kb = new HashMap<>();

	@EventHandler(priority = EventPriority.HIGH)
	public void onKnockback(EntityDamageByEntityEvent event) {
		try {
			if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity) || !(event.getDamager() instanceof LivingEntity)) {
				return;
			}

			LivingEntity damagedEntity = (LivingEntity) event.getEntity();
			LivingEntity damagerEntity = (LivingEntity) event.getDamager();

			damagedEntity.setNoDamageTicks(0);

			if (!(damagedEntity instanceof Player)) {
				applyKnockback(damagedEntity, damagerEntity, nonPlayerKnockbackMultiplier, verticalKnockback);
				return;
			}

			Player damagedPlayer = (Player) damagedEntity;

			Vector knockbackVector = damagedPlayer.getLocation().toVector().subtract(damagerEntity.getLocation().toVector());
			if (knockbackVector.length() > 0.0) {
				knockbackVector.normalize();
			}

			if (damagerEntity instanceof Player) {
				Player damagerPlayer = (Player) damagerEntity;

				if (damagerPlayer.getInventory().getItemInMainHand() != null
						&& damagerPlayer.getInventory().getItemInMainHand().getType().name().contains("_SPADE")) {
					applyKnockback(damagedPlayer, knockbackVector, playerSpadeKnockbackMultiplier, SpadeVerticalKnockback);
				} else {
					applyKnockback(damagedPlayer, knockbackVector, playerKnockbackMultiplier, verticalKnockback);
				}
			} else {
				applyKnockback(damagedPlayer, knockbackVector, nonPlayerKnockbackMultiplier, verticalKnockback);
			}

			// If you're not using the 'kb' map for anything else, you can remove this.
			// kb.put(damagedPlayer.getUniqueId(), System.currentTimeMillis());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void applyKnockback(Player player, Vector knockbackVector, float horizontalMultiplier, double verticalMultiplier) {
		Vector velocity = knockbackVector.multiply(horizontalMultiplier);
		double vert = verticalMultiplier;

		if(!player.isOnGround()) vert /= 2;
		velocity.setY(vert);
		player.setVelocity(velocity);
	}

	private void applyKnockback(LivingEntity entity, LivingEntity damager, float horizontalMultiplier, double verticalMultiplier) {
		Vector knockbackVector = entity.getLocation().toVector().subtract(damager.getLocation().toVector());
		if (knockbackVector.length() > 0.0) {
			knockbackVector.normalize();
		}

		Vector velocity = knockbackVector.multiply(horizontalMultiplier);
		double vert = verticalMultiplier;
		if(!entity.isOnGround()) vert /= 2;
		velocity.setY(vert);
		entity.setVelocity(velocity);
	}


	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDeath(EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			LivingEntity s = (LivingEntity) e.getEntity();
			if (e.getDamage() >= s.getHealth() && this.kb.containsKey(s.getUniqueId())) {
				this.kb.remove(s.getUniqueId());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPolearmAOE(EntityDamageByEntityEvent e) {
		try {
			if (e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Player) {
				LivingEntity le = (LivingEntity) e.getEntity();
				Player p = (Player) e.getDamager();
				if (e.getDamage() <= 0.0) {
					return;
				}
				if (p.getInventory().getItemInMainHand() != null
						&& p.getInventory().getItemInMainHand().getType().name().contains("_SPADE")
						&& !this.p_arm.contains(p.getName())) {
					int amt = 5;
					Energy.removeEnergy(p, amt);
					for (Entity near : le.getNearbyEntities(1, 2, 1)) {
						if (!(near instanceof LivingEntity) || near == le || near == p)
							continue;
						LivingEntity n = (LivingEntity) near;
						le.setNoDamageTicks(0);
						n.setNoDamageTicks(0);
						if (Energy.nodamage.containsKey(p.getName())) {
							Energy.nodamage.remove(p.getName());
						}
						Energy.removeEnergy(p, 2);
						this.p_arm.add(p.getName());
						n.damage(1.0, p);
						this.p_arm.remove(p.getName());
					}
				}
			}
		} catch (Exception ex) {
			System.out.println("onPolearmAOE");
		}
	}

	@EventHandler
	public void onDamageSound(EntityDamageByEntityEvent e) {
		try {
			Player p;
			if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity) {
				if (e.getDamage() <= 0.0) {
					return;
				}
				p = (Player) e.getDamager();
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
				if (e.getEntity() instanceof Player) {
					e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON,
							1.0f, 1.6f);
				}
			}
			if (e.getEntity() instanceof Player && !(e.getDamager() instanceof Player)
					&& e.getDamager() instanceof LivingEntity) {
				p = (Player) e.getEntity();
				p.setWalkSpeed(0.165f);
				this.playerslow.put(p, System.currentTimeMillis());
			}
		} catch (Exception ex) {
			System.out.println("onDamageSound");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBypassArmor(EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			LivingEntity li = (LivingEntity) e.getEntity();
			if (e.getDamage() <= 0.0) {
				return;
			}
			int dmg = (int) e.getDamage();
			e.setDamage(0.0);
			e.setCancelled(true);
			li.playEffect(EntityEffect.HURT);
			li.setLastDamageCause(e);
			if (li.getHealth() - (double) dmg <= 0.0) {
				li.setHealth(0.0);
			} else {
				li.setHealth(li.getHealth() - (double) dmg);
			}
		}
	}
}
