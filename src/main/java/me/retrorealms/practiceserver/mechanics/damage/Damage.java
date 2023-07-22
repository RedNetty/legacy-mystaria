
package me.retrorealms.practiceserver.mechanics.damage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import ac.grim.grimac.GrimAbstractAPI;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.core.api.v2.V2HologramsAPIProvider;
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
import me.retrorealms.practiceserver.utils.StringUtil;
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
import org.bukkit.inventory.meta.ItemMeta;
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
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					updateBossBar(player);
				}
			}
		}.runTaskTimerAsynchronously(PracticeServer.plugin, 0, 1);

		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					if (playerslow.containsKey(player)) {
						if (System.currentTimeMillis() - playerslow.get(player) <= 3000) {
							continue;
						}
						syncSpeed(player, 0.2f);
					} else if (player.getWalkSpeed() != 0.2f) {
						syncSpeed(player, 0.2f);
					}
				}
			}
		}.runTaskTimerAsynchronously(PracticeServer.plugin, 20, 20);
	}

	public void syncSpeed(Player player, float speed) {
		Bukkit.getScheduler().runTask(PracticeServer.plugin, () -> player.setWalkSpeed(speed));
	}

	public void onDisable() {
		// Perform any necessary cleanup or actions when the plugin is disabled.
	}

	public void updateBossBar(Player player) {
		double maxHealth = player.getMaxHealth();
		double currentHealth = player.getHealth();
		double healthPercentage = currentHealth / maxHealth;
		String safeZone = Alignments.isSafeZone(player.getLocation()) ? ChatColor.GRAY + " - " + ChatColor.GREEN.toString() + ChatColor.BOLD + "SAFE-ZONE" : "";
		if (healthPercentage > 1.0) {
			healthPercentage = 1.0;
		}
		float progress = (float) healthPercentage;

		BarColor barColor = getBarColor(player);
		ChatColor titleColor = barTitleColor(player);
		BossBar bossBar = Alignments.playerBossBars.get(player);

		if (bossBar == null) {
			bossBar = Bukkit.createBossBar(
					titleColor + "" + ChatColor.BOLD + "HP " + titleColor
							+ (int) currentHealth + titleColor + ChatColor.BOLD + " / "
							+ titleColor + (int) maxHealth + safeZone,
					barColor, BarStyle.SOLID);
			bossBar.addPlayer(player);
			Alignments.playerBossBars.put(player, bossBar);
		}

		bossBar.setColor(barColor);
		bossBar.setTitle(titleColor + "" + ChatColor.BOLD + "HP "
				+ titleColor + (int) currentHealth + titleColor
				+ ChatColor.BOLD + " / " + titleColor + (int) maxHealth + safeZone);
		bossBar.setProgress(progress);
	}

	public static BarColor getBarColor(Player player) {
		double maxHealth = player.getMaxHealth();
		double currentHealth = player.getHealth();
		double healthPercentage = currentHealth / maxHealth;

		if (healthPercentage > 0.5) {
			return BarColor.GREEN;
		} else if (healthPercentage > 0.25) {
			return BarColor.YELLOW;
		} else {
			return BarColor.RED;
		}
	}

	public static ChatColor barTitleColor(Player player) {
		double maxHealth = player.getMaxHealth();
		double currentHealth = player.getHealth();
		double healthPercentage = currentHealth / maxHealth;

		if (healthPercentage > 0.5) {
			return ChatColor.GREEN;
		} else if (healthPercentage > 0.25) {
			return ChatColor.YELLOW;
		} else {
			return ChatColor.RED;
		}
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
	public static int getElem(ItemStack itemStack, String type) {
		if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.hasItemMeta()) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta.hasLore()) {
				List<String> lore = itemMeta.getLore();
				for (String line : lore) {
					if (line.contains(type)) {
						try {
							return Integer.parseInt(line.split(": +")[1]);
						} catch (Exception e) {
							return 0;
						}
					}
				}
			}
		}
		return 0;
	}

	public static List<Integer> getDamageRange(ItemStack itemStack) {
		List<Integer> damageRange = new ArrayList<>(Arrays.asList(1, 1));
		if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.hasItemMeta()) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta.hasLore()) {
				List<String> lore = itemMeta.getLore();
				if (lore.size() > 0 && lore.get(0).contains("DMG")) {
					try {
						String[] dmgValues = lore.get(0).split("DMG: ")[1].split(" - ");
						int min = Integer.parseInt(dmgValues[0]);
						int max = Integer.parseInt(dmgValues[1]);
						damageRange.set(0, min);
						damageRange.set(1, max);
					} catch (Exception e) {
						damageRange.set(0, 1);
						damageRange.set(1, 1);
					}
				}
			}
		}
		return damageRange;
	}

	public static int getCrit(Player player) {
		int crit = 0;
		ItemStack weapon = player.getInventory().getItemInMainHand();
		if (Staffs.getStaff().containsKey(player)) {
			weapon = Staffs.getStaff().get(player);
		}
		if (weapon != null && weapon.getType() != Material.AIR && weapon.hasItemMeta()) {
			ItemMeta weaponMeta = weapon.getItemMeta();
			if (weaponMeta.hasLore()) {
				List<String> lore = weaponMeta.getLore();
				for (String line : lore) {
					if (line.contains("CRITICAL HIT")) {
						crit = getPercent(weapon, "CRITICAL HIT");
					}
				}
				if (weapon.getType().name().contains("_AXE")) {
					crit += 10;
				}
				int intel = 0;
				ItemStack[] armorContents = player.getInventory().getArmorContents();
				for (ItemStack armor : armorContents) {
					if (armor != null && armor.getType() != Material.AIR && armor.hasItemMeta()) {
						int addInt = getElem(armor, "INT");
						intel += addInt;
					}
				}
				if (intel > 0) {
					crit += Math.round(intel * 0.015);
				}
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

	private static final int HOLOGRAM_DELAY = 20; // Delay in ticks before hologram disappears

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.getDamage() <= 0) {
			return;
		}
		if (event.getDamager().getType() == EntityType.FIREWORK || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
			event.setDamage(0);
			event.setCancelled(true);
		}

		try {
			if (event.getEntity() instanceof LivingEntity && event.getDamager() instanceof Player) {
				if (event.getDamage() > 0 && !event.isCancelled()) {
					Player damager = (Player) event.getDamager();
					LivingEntity entity = (LivingEntity) event.getEntity();
					int damage = (int) event.getDamage();
					callHologramDamage(damager, entity, "dmg", damage);
				}
			}
		} catch (Exception ignored) {
		}
	}

	private void callHologramDamage(Player player, LivingEntity entity, String type, int damage) {
		ArrayList<String> toggles = Toggles.getToggles(player.getUniqueId());

		if (toggles.contains("Hologram Damage")) {
			Random random = new Random();
			float x = random.nextFloat();
			float y = random.nextFloat();
			float z = random.nextFloat();

			Hologram hologram = HolographicDisplaysAPI.get(PracticeServer.getInstance()).createHologram(entity.getLocation().clone().add(x, 0.5 + y, z));
			if (type.equalsIgnoreCase("dmg")) {
				hologram.getLines().appendText(ChatColor.RED + "-" + damage + "❤");
			}
			if (type.equalsIgnoreCase("dodge")) {
				hologram.getLines().appendText(ChatColor.RED + "*DODGE*");
			}
			if (type.equalsIgnoreCase("block")) {
				hologram.getLines().appendText(ChatColor.RED + "*BLOCK*");
			}

			new BukkitRunnable() {
				@Override
				public void run() {
					hologram.delete();
				}
			}.runTaskLater(PracticeServer.plugin, HOLOGRAM_DELAY);
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
				ItemStack weapon = player.getInventory().getItemInMainHand();

				if (weapon != null && weapon.getType() != Material.AIR && weapon.getItemMeta().hasLore()) {
					int minDamage = Damage.getDamageRange(weapon).get(0);
					int maxDamage = Damage.getDamageRange(weapon).get(1);
					int damage = ThreadLocalRandom.current().nextInt(minDamage, maxDamage);

					for (String line : weapon.getItemMeta().getLore()) {
						int elementalDamage;
						if (line.contains("ICE DMG")) {
							elementalDamage = Damage.getElem(weapon, "ICE DMG");
							damage += elementalDamage;
						}
						if (line.contains("POISON DMG")) {
							elementalDamage = Damage.getElem(weapon, "POISON DMG");
							damage += elementalDamage;
						}
						if (line.contains("FIRE DMG")) {
							elementalDamage = Damage.getElem(weapon, "FIRE DMG");
							damage += elementalDamage;
						}
						if (!line.contains("PURE DMG")) {
							elementalDamage = Damage.getElem(weapon, "PURE DMG");
							damage += elementalDamage;
						}
					}

					double dps = 0.0;
					double vitality = 0.0;
					double dexterity = 0.0;
					double intellect = 0.0;
					double strength = 0.0;

					ItemStack[] armorContents = player.getInventory().getArmorContents();

					for (ItemStack armor : armorContents) {
						if (armor != null && armor.getType() != Material.AIR && armor.hasItemMeta()
								&& armor.getItemMeta().hasLore()) {
							int addDps = Damage.getDps(armor);
							dps += addDps;
							int addVitality = Damage.getElem(armor, "VIT");
							vitality += addVitality;
							int addDexterity = Damage.getElem(armor, "DEX");
							dexterity += addDexterity;
							int addIntellect = Damage.getElem(armor, "INT");
							intellect += addIntellect;
							int addStrength = Damage.getElem(armor, "STR");
							strength += addStrength;
						}
					}

					if (vitality > 0.0 && weapon.getType().name().contains("_SWORD")) {
						double divide = vitality / 5000.0;
						double pre = (double) damage * divide;
						damage = (int) ((double) damage + pre);
					}
					if (strength > 0.0 && weapon.getType().name().contains("_AXE")) {
						double divide = strength / 5000.0;
						double pre = (double) damage * divide;
						damage = (int) ((double) damage + pre);
					}
					if (intellect > 0.0 && weapon.getType().name().contains("_HOE")) {
						double divide = intellect / 100.0;
						double pre = (double) damage * divide;
						damage = (int) ((double) damage + pre);
					}
					if (dps > 0.0) {
						double divide = dps / 100.0;
						double pre = (double) damage * divide;
						damage = (int) ((double) damage + pre);
					}

					event.setCancelled(true);

					StringUtil.sendCenteredMessage(player, ChatColor.RED + "" + damage + ChatColor.RED + ChatColor.BOLD + " DMG "
							+ ChatColor.RED + "➜ " + ChatColor.RESET + "DPS DUMMY" + " [" + 99999999 + "HP]");
				}
			}
		}
	}


	@EventHandler(priority = EventPriority.LOW)
	public void onBlodge(EntityDamageByEntityEvent e) {
		// Check if patchlockdown is enabled
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
			// Handle damage between players
			if (e.getDamager() instanceof Player) {
				Player d = (Player) e.getDamager();
				Player x = (Player) e.getEntity();

				// Check if both players are duelers and on the same team
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

			// Calculate block and dodge percentages based on equipped armor
			for (ItemStack is : i.getArmorContents()) {
				if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
					int addedblock = Damage.getPercent(is, "BLOCK");
					block += addedblock;
					int addeddodge = Damage.getPercent(is, "DODGE");
					dodge += addeddodge;
				}
			}

			int str = 0;
			int dex = 0;

			// Calculate strength and dexterity values based on equipped armor
			for (ItemStack is : p.getInventory().getArmorContents()) {
				if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
					int addstr = Damage.getElem(is, "STR");
					str += addstr;
					int adddex = Damage.getElem(is, "DEX");
					dex += adddex;
				}
			}

			// Adjust block and dodge values based on strength and dexterity
			if (str > 0) {
				block += Math.round((double) str * 0.015);
			}
			if (dex > 0) {
				dodge += Math.round((double) dex * 0.015);
			}

			int dodger = random.nextInt(110) + 1;
			int blockr = random.nextInt(110) + 1;

			if (drop < crit && ((Player) e.getDamager()).getInventory().getItemInMainHand().getType().name().contains("_AXE")) {
				// Reset block and dodge if critical hit with an axe
				block = 0;
				dodge = 0;
			}

			if (e.getDamager() instanceof Player) {
				Player d = (Player) e.getDamager();
				ItemStack wep = d.getInventory().getItemInMainHand();

				if (Staffs.getStaff().containsKey(d)) {
					wep = Staffs.getStaff().get(d);
				}

				int accuracy = Damage.getPercent(wep, "ACCURACY");

				// Reduce block and dodge based on weapon accuracy
				block -= accuracy;
				dodge -= accuracy;

				if (blockr <= block) {
					// Blocked damage
					e.setDamage(0.0);
					e.setCancelled(true);
					p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
					callHologramDamage(d, p, "block", 0);
					if (Toggles.getToggleStatus(d, "Debug")) {
						StringUtil.sendCenteredMessage(d, ChatColor.RED.toString() + ChatColor.BOLD + "*OPPONENT BLOCKED* ("
								+ (PracticeServer.FFA ? "Anonymous" : p.getName()) + ")");
					}
					if (Toggles.getToggleStatus(p, "Debug")) {
						StringUtil.sendCenteredMessage(p, ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "*BLOCK* ("
								+ (PracticeServer.FFA ? "Anonymous" : d.getName()) + ")");
					}
				} else if (dodger <= dodge) {
					// Dodged damage
					e.setDamage(0.0);
					e.setCancelled(true);
					p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);
					callHologramDamage(d, p, "dodge", 0);
					if (Toggles.getToggleStatus(d, "Debug")) {
						StringUtil.sendCenteredMessage(d, ChatColor.RED.toString() + ChatColor.BOLD + "*OPPONENT DODGED* ("
								+ (PracticeServer.FFA ? "Anonymous" : p.getName()) + ")");
					}
					if (Toggles.getToggleStatus(p, "Debug")) {
						StringUtil.sendCenteredMessage(p, ChatColor.GREEN.toString() + ChatColor.BOLD + "*DODGE* ("
								+ (PracticeServer.FFA ? "Anonymous" : d.getName()) + ")");
					}
				} else if (blockr <= 80 && p.isBlocking()) {
					// Partially blocked damage while blocking
					e.setDamage((double) ((int) e.getDamage() / 2));
					callHologramDamage(d, p, "block", 0);
					p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
					if (Toggles.getToggleStatus(d, "Debug")) {
						StringUtil.sendCenteredMessage(d, ChatColor.RED.toString() + ChatColor.BOLD + "*OPPONENT BLOCKED* ("
								+ (PracticeServer.FFA ? "Anonymous" : p.getName()) + ")");
					}
					if (Toggles.getToggleStatus(p, "Debug")) {
						StringUtil.sendCenteredMessage(p,  ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "*BLOCK* ("
								+ (PracticeServer.FFA ? "Anonymous" : d.getName()) + ")");
					}
				}
			} else if (e.getDamager() instanceof LivingEntity) {
				LivingEntity li = (LivingEntity) e.getDamager();

				if (Mobs.isFrozenBoss(li) || Mobs.isGolemBoss(li)) {
					// Reduce block and dodge against frozen and golem bosses
					block -= 25;
					dodge -= 25;
				}

				String mname = "";
				if (li.hasMetadata("name")) {
					mname = li.getMetadata("name").get(0).asString();
				}

				if (blockr <= block) {
					// Blocked damage from a living entity
					e.setDamage(0.0);
					e.setCancelled(true);
					callHologramDamage(p, li, "block", 0);
					p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
					if (Toggles.getToggleStatus(p, "Debug")) {
						StringUtil.sendCenteredMessage(p, ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "*BLOCK* (" + mname
								+ ChatColor.DARK_GREEN + ")");
					}
				} else if (dodger <= dodge) {
					// Dodged damage from a living entity
					e.setDamage(0.0);
					e.setCancelled(true);
					callHologramDamage(p, li, "dodge", 0);
					p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);
					if (Toggles.getToggleStatus(p, "Debug")) {
						StringUtil.sendCenteredMessage(p, ChatColor.GREEN.toString() + ChatColor.BOLD + "*DODGE* (" + mname
								+ ChatColor.GREEN + ")");
					}
				} else if (blockr <= 80 && p.isBlocking()) {
					// Partially blocked damage while blocking against a living entity
					e.setDamage((double) ((int) e.getDamage() / 2));
					callHologramDamage(p, li, "block", 0);
					p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
					if (Toggles.getToggleStatus(p, "Debug")) {
						StringUtil.sendCenteredMessage(p, ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "*BLOCK* (" + mname
								+ ChatColor.DARK_GREEN + ")");
					}
				}
			}
		}

		if (e.getDamage() <= 0.0) {
			return;
		}

		if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity) {
			if (!(e.getEntity() instanceof Player)) {
				p = (Player) e.getDamager();
				GrimAbstractAPI api = PracticeServer.getGrim();
				// Continue processing the event with the appropriate variables
			}

			p = (Player) e.getDamager();
			LivingEntity li = (LivingEntity) e.getEntity();
			ItemStack wep = p.getInventory().getItemInMainHand();

			if (Staffs.getStaff().containsKey(p)) {
				wep = Staffs.getStaff().get(p);
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
						// Apply ice damage effect
						DyeColor blockColor = DyeColor.LIGHT_BLUE;
						MaterialData data = new MaterialData(Material.STAINED_GLASS_PANE, (byte) 3);
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
						// Apply poison damage effect
						boolean color = ThreadLocalRandom.current().nextBoolean();
						byte bytes = color ? (byte) 5 : (byte) 13;
						MaterialData data = new MaterialData(Material.STAINED_GLASS, bytes);
						li.getWorld().spawnParticle(Particle.BLOCK_CRACK, li.getLocation().clone().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.01, data);
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
						// Apply fire damage effect
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
						// Apply pure damage effect
						eldmg = Damage.getElem(wep, "PURE DMG");
						int elemult = Math.round(eldmg * (1 + Math.round(Damage.getElem(wep, "DEX") / 3000)));
						dmg += elemult;
					}

					if (li instanceof Player && line.contains("VS PLAYERS")) {
						// Apply damage against players effect
						int addedDMG = dmg * getPercent(wep, "VS PLAYERS") / 100;
						dmg += addedDMG;
					} else if (!(li instanceof Player) && line.contains("VS MONSTERS")) {
						// Apply damage against monsters effect
						int addedDMG = dmg * getPercent(wep, "VS MONSTERS") / 100;
						dmg += addedDMG;
					}
				}

				if (drop <= crit) {
					// Apply critical hit effect
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

						if (toggles.contains("Debug")) {
							StringUtil.sendCenteredMessage(p, ChatColor.GREEN.toString() + ChatColor.BOLD + "+" + ChatColor.GREEN
									+ life + ChatColor.GREEN + ChatColor.BOLD + " HP " + ChatColor.GRAY + "["
									+ (int) p.getHealth() + "/" + (int) p.getMaxHealth() + "HP]");
						}
					}

					if (p.getHealth() < p.getMaxHealth() - (double) life)
						continue;

					p.setHealth(p.getMaxHealth());
					toggles = Toggles.getToggles(p.getUniqueId());

					if (toggles.contains("Debug")) {
						StringUtil.sendCenteredMessage(p, ChatColor.GREEN.toString() + ChatColor.BOLD + " " + "+" + ChatColor.GREEN
								+ life + ChatColor.GREEN + ChatColor.BOLD + " HP " + ChatColor.GRAY + "["
								+ (int) p.getMaxHealth() + "/" + (int) p.getMaxHealth() + "HP]");
					}
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
		if(e.getDamager() instanceof Projectile && e.getDamager().getType() == EntityType.FIREWORK){
			e.setDamage(0.0);
			e.setCancelled(true);
		}
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
					StringUtil.sendCenteredMessage(p, ChatColor.RED + "-" + cleaned + ChatColor.RED + ChatColor.BOLD + "HP "
							+ ChatColor.GRAY + "[-" + (int) arm + "%A ➜ -" + (int) pre + ChatColor.BOLD + "DMG"
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
					StringUtil.sendCenteredMessage(p, ChatColor.RED + "-" + (int) dmg + ChatColor.RED + ChatColor.BOLD + "HP "
							+ ChatColor.GRAY + "[-0%A ➜ -0" + ChatColor.BOLD + "DMG" + ChatColor.GRAY + "] "
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
					StringUtil.sendCenteredMessage(d, ChatColor.RED.toString() + dmg + ChatColor.RED + ChatColor.BOLD + " DMG "
							+ ChatColor.RED + "➜ " + (PracticeServer.FFA
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
					StringUtil.sendCenteredMessage(d, ChatColor.RED.toString() + dmg + ChatColor.RED + ChatColor.BOLD + " DMG "
							+ ChatColor.RED + "➜ " + ChatColor.RESET + name + " [" + health + "HP]");
				}
			}
		} catch (Exception ignored) {
		}
	}

	private final float playerKnockbackMultiplier = 0.32f;
	private final float playerSpadeKnockbackMultiplier = 0.7f;
	private final float spadeKnockbackMultiplier = 0.85f;
	private final float nonPlayerKnockbackMultiplier = 0.30f;
	private final double verticalKnockback = 0.15;
	private final double spadeVerticalKnockback = 0.3;
	private final long accelerationDurationMillis = 200;

	@EventHandler(priority = EventPriority.HIGH)
	public void onDamage(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity damagedEntity = event.getEntity();
		if (!(damagedEntity instanceof LivingEntity)) {
			return;
		}

		Entity damagerEntity = event.getDamager();
		if (!(damagerEntity instanceof LivingEntity)) {
			return;
		}

		LivingEntity damagedLivingEntity = (LivingEntity) damagedEntity;
		LivingEntity damagerLivingEntity = (LivingEntity) damagerEntity;

		applyKnockback(damagedLivingEntity, damagerLivingEntity);
	}

	private void applyKnockback(LivingEntity damagedEntity, LivingEntity damagerEntity) {
		Vector knockbackVector = damagedEntity.getLocation().toVector().subtract(damagerEntity.getLocation().toVector());
		if (knockbackVector.lengthSquared() > 0.0) {
			knockbackVector.normalize();
		}

		double horizontalMultiplier;
		double verticalMultiplier;
		boolean polearm = false;

		if (damagerEntity instanceof Player) {
			Player damagerPlayer = (Player) damagerEntity;
			if (damagerPlayer.getInventory().getItemInMainHand().getType().name().contains("_SPADE")) {
				if(!(damagedEntity instanceof Player)) polearm = true;
				horizontalMultiplier = playerSpadeKnockbackMultiplier;
				verticalMultiplier = spadeVerticalKnockback;
			} else {
				horizontalMultiplier = playerKnockbackMultiplier;
				verticalMultiplier = verticalKnockback;
			}
		}else{
			horizontalMultiplier = nonPlayerKnockbackMultiplier;
			verticalMultiplier = verticalKnockback;
		}

		applyKnockback(damagedEntity, knockbackVector, polearm, horizontalMultiplier, verticalMultiplier);
	}

	private void applyKnockback(LivingEntity entity, Vector knockbackVector, boolean polearm, double horizontalMultiplier, double verticalMultiplier) {
		double speed = Math.sqrt(entity.getVelocity().getX() * entity.getVelocity().getX() + entity.getVelocity().getZ() * entity.getVelocity().getZ());
		double maxSpeed = polearm ? 1 : 0.45; // Adjust this value as needed

		horizontalMultiplier *= (1.0 + speed * 0.2);
		verticalMultiplier *= !entity.isOnGround() ? (1.0 + speed * 0.145) : (1.0 + speed * 0.2);
		Vector velocity = knockbackVector.multiply(horizontalMultiplier).setY(verticalMultiplier);
		if (velocity.lengthSquared() > maxSpeed * maxSpeed) {
			velocity.normalize().multiply(maxSpeed);
		}



		// Apply smooth acceleration
		Vector currentVelocity = entity.getVelocity();
		Vector targetVelocity = velocity;
		//Vector interpolatedVelocity = interpolateVelocity(currentVelocity, targetVelocity, accelerationDurationMillis);
		entity.setVelocity(targetVelocity);
	}

	private Vector interpolateVelocity(Vector currentVelocity, Vector targetVelocity, long durationMillis) {
		double t = Math.min(1.0, (double) durationMillis / 1000.0); // Convert duration to seconds
		double x = currentVelocity.getX() + (targetVelocity.getX() - currentVelocity.getX()) * t;
		double y = currentVelocity.getY() + (targetVelocity.getY() - currentVelocity.getY()) * t;
		double z = currentVelocity.getZ() + (targetVelocity.getZ() - currentVelocity.getZ()) * t;
		return new Vector(x, y, z);
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
						if (Energy.noDamage.containsKey(p.getName())) {
							Energy.noDamage.remove(p.getName());
						}
						Energy.removeEnergy(p, 2);
						this.p_arm.add(p.getName());
						n.damage(1.0, p);
						this.p_arm.remove(p.getName());
					}
				}
			}
		} catch (Exception ignored) {
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
