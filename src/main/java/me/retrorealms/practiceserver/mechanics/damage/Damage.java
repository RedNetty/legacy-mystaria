package me.retrorealms.practiceserver.mechanics.damage;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.commands.moderation.ToggleGMCommand;
import me.retrorealms.practiceserver.mechanics.crafting.items.celestialbeacon.CelestialAlly;
import me.retrorealms.practiceserver.mechanics.crafting.items.celestialbeacon.CelestialAllyDamageEvent;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.WepTrak;
import me.retrorealms.practiceserver.mechanics.drops.Mobdrops;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.mobs.Mobs;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBossHandler;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses.Frostwing;
import me.retrorealms.practiceserver.mechanics.player.Energy;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Damage implements Listener {
    private static final int HOLOGRAM_DELAY = 20; // Delay in ticks before hologram disappears
    public static HashMap<Player, Long> lasthit = new HashMap<Player, Long>();
    public static HashMap<Player, Player> lastphit = new HashMap<Player, Player>();
    private final int MAX_BLOCK = 45;
    private final int MAX_DODGE = 45;
    HashMap<Player, Long> playerslow = new HashMap<Player, Long>();
    ConcurrentHashMap<UUID, Long> kb = new ConcurrentHashMap<UUID, Long>();
    ArrayList<String> p_arm = new ArrayList<String>();

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
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore() && (lore = is.getItemMeta().getLore()).size() > 1 && lore.get(1).contains("HP")) {
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
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore() && (lore = is.getItemMeta().getLore()).size() > 0 && lore.get(0).contains("ARMOR")) {
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
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore() && (lore = is.getItemMeta().getLore()).size() > 0 && lore.get(0).contains("DPS")) {
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
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore() && (lore = is.getItemMeta().getLore()).size() > 2 && lore.get(2).contains("ENERGY REGEN")) {
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
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore() && (lore = is.getItemMeta().getLore()).size() > 2 && lore.get(2).contains("HP REGEN")) {
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
                if (!s.contains(type)) continue;
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
        String safeZone = Alignments.isSafeZone(player.getLocation()) ? ChatColor.GRAY + " - " + ChatColor.GREEN + ChatColor.BOLD + "SAFE-ZONE" : "";
        if (healthPercentage > 1.0) {
            healthPercentage = 1.0;
        }
        float progress = (float) healthPercentage;

        BarColor barColor = getBarColor(player);
        ChatColor titleColor = barTitleColor(player);
        BossBar bossBar = Alignments.playerBossBars.get(player);

        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(titleColor + "" + ChatColor.BOLD + "HP " + titleColor + (int) currentHealth + titleColor + ChatColor.BOLD + " / " + titleColor + (int) maxHealth + safeZone, barColor, BarStyle.SOLID);
            bossBar.addPlayer(player);
            Alignments.playerBossBars.put(player, bossBar);
        }

        bossBar.setColor(barColor);
        bossBar.setTitle(titleColor + "" + ChatColor.BOLD + "HP " + titleColor + (int) currentHealth + titleColor + ChatColor.BOLD + " / " + titleColor + (int) maxHealth + safeZone);
        bossBar.setProgress(progress);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCelestialAllyDamage(CelestialAllyDamageEvent event) {
        if (event.getDamage() <= 0.0) {
            return;
        }

        Entity targetEntity = event.getTarget();

        if (targetEntity instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) targetEntity;
            double damage = event.getDamage();

            // Apply any relevant modifiers (e.g., armor penetration)
            double armorPenetration = 20.0; // 20% armor penetration

            // Calculate armor reduction
            double armor = 0.0;
            if (target instanceof Player) {
                Player playerTarget = (Player) target;
                PlayerInventory inventory = playerTarget.getInventory();
                for (ItemStack armorPiece : inventory.getArmorContents()) {
                    if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                        armor += getArmor(armorPiece);
                    }
                }
            }

            // Apply armor penetration
            armor = Math.max(0, armor - armorPenetration);

            // Calculate damage reduction
            double damageReduction = Math.min(80, armor) / 100.0;
            double finalDamage = damage * (1 - damageReduction);

            // Set the final damage
            event.setDamage(finalDamage);

            // Check if the damage would kill the target
            if (target.getHealth() <= finalDamage && MobHandler.isMobOnly(target)) {
                // Trigger MobDrop mechanic
                EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(event.getSource().getBukkitEntity(), target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, finalDamage);
                Bukkit.getPluginManager().callEvent(damageEvent);

                if (!damageEvent.isCancelled()) {
                    // Call handleMobDeath method from Mobdrops class
                    Mobdrops mobdrops = new Mobdrops();
                    mobdrops.handleMobDeath(target, Bukkit.getPlayer(event.getSource().getOwnerUUID()), damageEvent);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof CelestialAlly) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNpcDamage(EntityDamageEvent e) {
        if (e.getEntity().hasMetadata("pet")) e.setCancelled(true);

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

        if (Toggles.isToggled(player, "Hologram Damage")) {
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

    private double calculateWeaponTypeBonus(double baseDamage, double statValue, double divisor) {
        return baseDamage * (1 + (statValue / divisor));
    }

    @EventHandler
    public void onDummyUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {

            Block block = event.getClickedBlock();

            if (block == null) return;

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
                        if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                            int adddps = Damage.getDps(is);
                            dps += adddps;
                            int addvit = Damage.getElem(is, "VIT");
                            vit += addvit;
                            int adddex = Damage.getElem(is, "DEX");
                            dex += adddex;
                            int addint = Damage.getElem(is, "INT");
                            intel += addint;
                            int addstr = Damage.getElem(is, "STR");
                            str += addstr;
                        }
                        ++n4;
                    }
                    if (vit > 0.0 && wep.getType().name().contains("_SWORD")) {
                        damage = (int) calculateWeaponTypeBonus(damage, vit, 5000.0);
                    }
                    if (str > 0.0 && wep.getType().name().contains("_AXE")) {
                        damage = (int) calculateWeaponTypeBonus(damage, str, 4500.0);
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

                    player.sendMessage(ChatColor.RED + "            " + damage + ChatColor.RED + ChatColor.BOLD + " DMG " + ChatColor.RED + "-> " + ChatColor.RESET + "DPS DUMMY" + " [" + 99999999 + "HP]");
                }
            }
        }
    }

    private boolean rollForSuccess(int chance) {
        return new Random().nextInt(100) < chance;
    }

    private void handleBlock(EntityDamageByEntityEvent e, Player attacker, Player defender) {
        // Cancel the event and set damage to 0
        e.setDamage(0.0);
        e.setCancelled(true);
        e.setDamage(0.0);

        // Play block sound for both attacker and defender
        defender.playSound(defender.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);

        callHologramDamage(attacker, defender, "block", 0);

        if (Toggles.isToggled(attacker, "Debug")) {
            attacker.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "*OPPONENT BLOCKED* (" + (PracticeServer.FFA ? "Anonymous" : defender.getName()) + ")");
        }
        if (Toggles.isToggled(defender, "Debug")) {
            defender.sendMessage(ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + "*BLOCK* (" + (PracticeServer.FFA ? "Anonymous" : attacker.getName()) + ")");
        }
    }

    private void handleDodge(EntityDamageByEntityEvent e, Player attacker, Player defender) {
        // Cancel the event and set damage to 0
        e.setDamage(0.0);
        e.setCancelled(true);
        e.setDamage(0.0);

        // Play dodge sound for both attacker and defender
        defender.playSound(defender.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);

        // Visual effect for dodge
        defender.getWorld().spawnParticle(Particle.CLOUD, defender.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);

        callHologramDamage(attacker, defender, "dodge", 0);

        if (Toggles.isToggled(attacker, "Debug")) {
            attacker.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "*OPPONENT DODGED* (" + (PracticeServer.FFA ? "Anonymous" : defender.getName()) + ")");
        }
        if (Toggles.isToggled(defender, "Debug")) {
            defender.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "*DODGE* (" + (PracticeServer.FFA ? "Anonymous" : attacker.getName()) + ")");
        }
    }

    private int calculateBlock(Player player) {
        int block = 0;
        int str = 0;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR && armor.hasItemMeta() && armor.getItemMeta().hasLore()) {
                block += getPercent(armor, "BLOCK");
                str += getElem(armor, "STR");
            }
        }
        block += Math.round(str * 0.015);
        return Math.min(block, 60); // Assuming max block is 80%
    }

    private int calculateDodge(Player player) {
        int dodge = 0;
        int dex = 0;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR && armor.hasItemMeta() && armor.getItemMeta().hasLore()) {
                dodge += getPercent(armor, "DODGE");
                dex += getElem(armor, "DEX");
            }
        }
        dodge += Math.round(dex * 0.015);
        return Math.min(dodge, 60); // Assuming max dodge is 80%
    }

    private int getAccuracy(Player attacker) {
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        return getPercent(weapon, "ACCURACY");
    }

    private double calculateThornsDamage(double incomingDamage, int thornsPercentage) {
        return (incomingDamage * thornsPercentage / 100.0);
    }

    private int calculateLifeSteal(double damageDealt, double lifeStealPercentage) {
        return Math.max(1, (int) (damageDealt * (lifeStealPercentage / 125.0)));
    }

    private double[] calculateStats(Player p) {
        double dps = 0.0;
        double vit = 0.0;
        double str = 0.0;

        for (ItemStack is : p.getInventory().getArmorContents()) {
            if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                dps += Damage.getDps(is);
                vit += Damage.getElem(is, "VIT");
                str += Damage.getElem(is, "STR");
                dps += Damage.getElem(is, "DEX") * 0.012; // DEX contribution to DPS
            }
        }

        return new double[]{dps, vit, str};
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
                        return;
                    }
                }
            }
            if (e.getDamage() <= 0.0) {
                return;
            }
            p = (Player) e.getEntity();
            PlayerInventory i = p.getInventory();
            p.setNoDamageTicks(0);
            int block = calculateBlock(p);
            int dodge = calculateDodge(p);
            int thorns = 0;
            if (p.getHealth() > 0.0) {
                ItemStack[] arritemStack = i.getArmorContents();
                int n = arritemStack.length;
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
                int thornsRandom = random.nextInt(110) + 1;
                if (e.getDamager() instanceof Player) {
                    Player d = (Player) e.getDamager();
                    int accuracy = getAccuracy(d);

                    // Polynomial function parameters for diminishing returns
                    double scale = 300;
                    double nS = 1.35;

                    // Calculate effective block and dodge with diminishing returns
                    double effectiveBlockDiminishingFactor = 1.0 / (1.0 + Math.pow(block / scale, nS));
                    double effectiveBlock = block * effectiveBlockDiminishingFactor;

                    double effectiveDodgeDiminishingFactor = 1.0 / (1.0 + Math.pow(dodge / scale, nS));
                    double effectiveDodge = dodge * effectiveDodgeDiminishingFactor;

                    // Calculate reductions based on accuracy
                    int blockReduction = (int)(effectiveBlock * (accuracy / 100.0));
                    int dodgeReduction = (int)(effectiveDodge * (accuracy / 100.0));

                    // Apply reductions to block and dodge values
                    block = (int) Math.max(0, effectiveBlock - blockReduction);
                    dodge = (int) Math.max(0, effectiveDodge - dodgeReduction);
                    block = block > 40 ? block - (int) (accuracy * (.05 * ((double) block / 10))) : block;
                    dodge = dodge > 40 ? dodge - (int) (accuracy * (.05 * ((double) dodge / 10))) : dodge;
                    boolean blocked = rollForSuccess(block);
                    boolean dodged = rollForSuccess(dodge);
                    if (blocked) {
                        e.setCancelled(true);
                        e.setDamage(0);
                        handleBlock(e, d, p);
                        return;
                    } else if (dodged) {
                        e.setCancelled(true);
                        e.setDamage(0);
                        handleDodge(e, d, p);
                        return;

                    } else if (blockr <= 80 && p.isBlocking()) {
                        e.setDamage((double) ((int) e.getDamage() / 2));
                        callHologramDamage(d, p, "block", 0);
                        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
                        if (Toggles.isToggled(d, "Debug"))
                            d.sendMessage("          " + ChatColor.RED + ChatColor.BOLD + "*OPPONENT BLOCKED* ("
                                    + (PracticeServer.FFA ? "Anonymous" : p.getName()) + ")");
                        if (Toggles.isToggled(p, "Debug"))
                            p.sendMessage("          " + ChatColor.DARK_GREEN + ChatColor.BOLD + "*BLOCK* ("
                                    + (PracticeServer.FFA ? "Anonymous" : d.getName()) + ")");
                        return;
                    }
                } else if (e.getDamager() instanceof LivingEntity && (!(e.getDamager() instanceof Player))) {
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
                        callHologramDamage(p, li, "block", 0);
                        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
                        if (Toggles.isToggled(p, "Debug"))
                            p.sendMessage("          " + ChatColor.DARK_GREEN + ChatColor.BOLD + "*BLOCK* (" + mname
                                    + ChatColor.DARK_GREEN + ")");
                        return;
                    } else if (dodger <= dodge) {
                        e.setDamage(0.0);
                        e.setCancelled(true);
                        callHologramDamage(p, li, "dodge", 0);
                        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);
                        if (Toggles.isToggled(p, "Debug"))
                            p.sendMessage("          " + ChatColor.GREEN + ChatColor.BOLD + "*DODGE* (" + mname
                                    + ChatColor.GREEN + ")");
                        return;
                    } else if (blockr <= 80 && p.isBlocking()) {
                        e.setDamage((double) ((int) e.getDamage() / 2));
                        callHologramDamage(p, li, "block", 0);
                        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
                        if (Toggles.isToggled(p, "Debug"))
                            p.sendMessage("          " + ChatColor.DARK_GREEN + ChatColor.BOLD + "*BLOCK* (" + mname
                                    + ChatColor.DARK_GREEN + ")");
                        return;
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
            if (Staffs.isRecentStaffShot(p)) {
                ItemStack staffWep = Staffs.getLastUsedStaff(p);
                if (staffWep != null) {
                    wep = staffWep;
                }
                // Clear the staff shot data after use
                Staffs.clearStaffShot(p);
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
                    if (line.contains("ICE DMG")&& !e.isCancelled()) {
                        li.getWorld().playEffect(li.getLocation().add(0.0, 1.3, 0.0), Effect.POTION_BREAK, 8194);
                        eldmg = Damage.getElem(wep, "ICE DMG");
                        int elemult = Math.round(eldmg * (1 + Math.round(Damage.getElem(wep, "DEX") / 3000)));
                        dmg += elemult;

                        if (tier == 1) {
                            li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                        } else if (tier == 2) {
                            li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                        } else if (tier == 3) {
                            li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                        } else if (tier == 4) {
                            li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                        } else if (tier >= 5) {
                            li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));
                        }
                    }
                    if (line.contains("POISON DMG")&& !e.isCancelled()) {
                        li.getWorld().playEffect(li.getLocation().add(0.0, 1.3, 0.0), Effect.POTION_BREAK, 8196);
                        eldmg = Damage.getElem(wep, "POISON DMG");
                        int elemult = Math.round(eldmg * (1 + Math.round(Damage.getElem(wep, "DEX") / 3000)));
                        dmg += elemult;

                        if (tier == 1) {
                            li.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 15, 0));
                        } else if (tier == 2) {
                            li.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 25, 0));
                        } else if (tier == 3) {
                            li.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 1));
                        } else if (tier == 4) {
                            li.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 35, 1));
                        } else if (tier >= 5) {
                            li.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                        }
                    }
                    if (line.contains("FIRE DMG") && !e.isCancelled()) {
                        eldmg = Damage.getElem(wep, "FIRE DMG");
                        int elemult = Math.round(eldmg * (1 + Math.round((float) Damage.getElem(wep, "DEX") / 3000)));
                        dmg += elemult;
                        if (tier == 1) {
                            li.setFireTicks(15);
                        } else if (tier == 2) {
                            li.setFireTicks(25);
                        } else if (tier == 3) {
                            li.setFireTicks(30);
                        } else if (tier == 4) {
                            li.setFireTicks(35);
                        } else if (tier >= 5) {
                            li.setFireTicks(40);
                        }
                    }
                    if (line.contains("PURE DMG")) {
                        eldmg = Damage.getElem(wep, "PURE DMG");
                        int elemult = Math.round(eldmg * (1 + Math.round(Damage.getElem(wep, "DEX") / 3000)));
                        dmg += elemult;
                    }
                }

                if (li instanceof Player && wep.getItemMeta().getLore().stream().anyMatch(loreLine -> loreLine.contains("VS PLAYERS"))) {
                    dmg *= (1 + getPercent(wep, "VS PLAYERS") / 100.0);
                } else if (!(li instanceof Player) && wep.getItemMeta().getLore().stream().anyMatch(loreLine -> loreLine.contains("VS MONSTERS"))) {
                    dmg *= (1 + getPercent(wep, "VS MONSTERS") / 100.0);
                }

                if (drop <= crit) {
                    e.setCancelled(false);
                    dmg *= 2;
                    p.playSound(p.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.5f, 0.5f);
                    Particles.CRIT_MAGIC.display(0.0f, 0.0f, 0.0f, 1.0f, 50, li.getLocation(), 20.0);
                }
                int thorns = 0;
                ItemStack[] arritemStack = li.getEquipment().getArmorContents();
                int n = arritemStack.length;
                int n2 = 0;
                while (n2 < n) {
                    ItemStack is = arritemStack[n2];
                    if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                        int addedThorns = Damage.getPercent(is, "THORNS");
                        thorns += addedThorns;
                    }
                    ++n2;
                }
                double[] stats = calculateStats(p);
                double dps = stats[0];
                double vit = stats[1];
                double str = stats[2];

                String weaponType = wep.getType().name();
                if (weaponType.contains("_SWORD")) {
                    dmg *= (1 + vit / 5000.0);
                } else if (weaponType.contains("_AXE")) {
                    dmg *= (1 + str / 4500.0);
                }

                dmg *= (1 + dps / 100.0);

                for (String line2 : lore) {
                    if (!line2.contains("LIFE STEAL")) continue;
                    if (e.getEntityType().equals(EntityType.ARMOR_STAND)) continue;

                    li.getWorld().playEffect(li.getEyeLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
                    double lifeStealPercentage = Damage.getPercent(wep, "LIFE STEAL");
                    int lifeStolen = calculateLifeSteal(dmg, lifeStealPercentage);

                    if (p.getHealth() < p.getMaxHealth() - (double) lifeStolen) {
                        p.setHealth(p.getHealth() + (double) lifeStolen);
                        if (!Toggles.isToggled(p, "Debug"))
                            continue;
                        p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "            +" + ChatColor.GREEN
                                + lifeStolen + ChatColor.GREEN + ChatColor.BOLD + " HP " + ChatColor.GRAY + "["
                                + (int) p.getHealth() + "/" + (int) p.getMaxHealth() + "HP]");
                        continue;
                    }
                    if (p.getHealth() < p.getMaxHealth() - (double) lifeStolen)
                        continue;
                    p.setHealth(p.getMaxHealth());
                    if (!Toggles.isToggled(p, "Debug"))
                        continue;
                    p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + " " + "           +" + ChatColor.GREEN
                            + lifeStolen + ChatColor.GREEN + ChatColor.BOLD + " HP " + ChatColor.GRAY + "["
                            + (int) p.getMaxHealth() + "/" + (int) p.getMaxHealth() + "HP]");
                }
                e.setDamage(dmg);
                if (thorns > 1 && random.nextBoolean()) {
                    int damageReturned = (int) (e.getDamage() * ((double) (thorns * .5) / 100)) + 1;

                    if (e.getDamager() instanceof LivingEntity && li instanceof Player) {
                        e.getEntity().getLocation().getWorld().spawnParticle(Particle.BLOCK_CRACK, e.getEntity().getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.01, new MaterialData(Material.LEAVES));
                        p.setHealth(p.getHealth() - damageReturned);
                    }
                }
                return;
            } else {
                e.setDamage(1.0);
            }
        }
    }


    private boolean areSame(Player shooter, Player entity) {
        return shooter.getUniqueId().equals(entity.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onArmor(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) || e.getDamage() <= 0.0) {
            return;
        }
        Player defender = (Player) e.getEntity();
        PlayerInventory defenderInv = defender.getInventory();
        double dmg = e.getDamage();
        double defenderArmor = 0.0;

        // Calculate defender's armor
        for (ItemStack is : defenderInv.getArmorContents()) {
            if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                int addarm = Damage.getArmor(is);
                int str = Damage.getElem(is, "STR");
                defenderArmor += addarm;
                // Reduced STR impact on armor (0.1% per point)
                defenderArmor += str * 0.001;
            }
        }

        // Polynomial function parameters for diminishing returns
        double scale = 400;
        double n = 1.8;

        // Apply diminishing returns to defender's armor using the polynomial function
        double effectiveArmor = defenderArmor / (1.0 + Math.pow(defenderArmor / scale, n));

        // Calculate attacker's armor penetration percentage
        double armorPenPercentage = 0.0;
        if (e.getDamager() instanceof Player) {
            Player attacker = (Player) e.getDamager();
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            armorPenPercentage = Damage.getElem(weapon, "ARMOR PEN") / 100.0;

            int totalDex = 0;
            for (ItemStack is : attacker.getInventory().getArmorContents()) {
                if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                    int dex = Damage.getElem(is, "DEX");
                    totalDex += dex;
                }
            }
            // DEX impact on armor penetration (0.035% per point)
            armorPenPercentage += totalDex * 0.00035;

        } else if (e.getDamager() instanceof LivingEntity) {
            LivingEntity mobAttacker = (LivingEntity) e.getDamager();
            if (Mobs.isFrozenBoss(mobAttacker) || Mobs.isGolemBoss(mobAttacker)) {
                armorPenPercentage = 0.2; // 20% armor penetration for these bosses
            }
        }

        // Ensure armor penetration percentage is within valid range (0% to 100%)
        armorPenPercentage = Math.max(0, Math.min(1, armorPenPercentage));

        // Calculate the remaining armor after applying armor penetration
        double remainingArmor = effectiveArmor * (1 - armorPenPercentage);


        // Calculate damage reduction directly from remaining armor
        // Assuming remaining armor directly reduces damage in a linear fashion
        double damageReduction = remainingArmor / 100.0;


        // Calculate the final reduced damage
        double reducedDamage = dmg * (1 - damageReduction);

        int finalDamage = (int) Math.round(reducedDamage);

        if (Toggles.isToggled(defender, "Debug")) {
            int health = Math.max(0, (int) (defender.getHealth() - finalDamage));
            double effectiveReduction = ((dmg - finalDamage) / dmg) * 100;
            defender.sendMessage(ChatColor.RED + "            -" + finalDamage + ChatColor.RED + ChatColor.BOLD + "HP " + ChatColor.GRAY + "[" + String.format("%.2f", effectiveReduction) + "%A -> -" + (int) (dmg - finalDamage) + ChatColor.BOLD + "DMG" + ChatColor.GRAY + "] " + ChatColor.GREEN + "[" + health + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
        }

        e.setDamage(finalDamage);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDebug(EntityDamageByEntityEvent e) {
        try {
            if (e.getDamage() <= 0.0 || e.getCause() == EntityDamageEvent.DamageCause.FIRE) {
                return;
            }

            if (e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Player) {
                LivingEntity entity = (LivingEntity) e.getEntity();
                Player damager = (Player) e.getDamager();

                int damage = (int) e.getDamage();
                int remainingHealth = Math.max(0, (int)(entity.getHealth() - damage));

                String name = entity.hasMetadata("name") ?
                        entity.getMetadata("name").get(0).asString() :
                        (entity instanceof Player ? ((Player) entity).getName() : "Unknown");

                if (Toggles.getToggles(damager.getUniqueId()).contains("Debug")) {
                    String message = String.format("%s%d%s DMG %s-> %s%s [%dHP]",
                            ChatColor.RED, damage, ChatColor.RED.toString() + ChatColor.BOLD,
                            ChatColor.RED, ChatColor.RESET, name, remainingHealth);
                    damager.sendMessage(message);
                }

                // Handle mob kills and other logic
                if (!(entity instanceof Player) && remainingHealth <= 0) {
                    int tier = Mobs.getMobTier(entity);
                    if (tier >= 1 && tier <= 6) {
                        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(damager.getUniqueId());
                        if (guildPlayer != null) {
                            // Update kills based on tier
                            switch (tier) {
                                case 1: guildPlayer.setT1Kills(guildPlayer.getT1Kills() + 1); break;
                                case 2: guildPlayer.setT2Kills(guildPlayer.getT2Kills() + 1); break;
                                case 3: guildPlayer.setT3Kills(guildPlayer.getT3Kills() + 1); break;
                                case 4: guildPlayer.setT4Kills(guildPlayer.getT4Kills() + 1); break;
                                case 5: guildPlayer.setT5Kills(guildPlayer.getT5Kills() + 1); break;
                                case 6: guildPlayer.setT6Kills(guildPlayer.getT6Kills() + 1); break;
                            }
                        }

                        if (WepTrak.isStatTrak(damager.getInventory().getItemInMainHand())) {
                            WepTrak.incrementStat(damager.getInventory().getItemInMainHand(), "mk");
                        }
                    }
                }

                // Update last hit information
                if (entity instanceof Player) {
                    lastphit.put((Player) entity, damager);
                    lasthit.put((Player) entity, System.currentTimeMillis());
                }
            }
        } catch (Exception ex) {
            PracticeServer.log.warning("Error in onDebug event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKnockback(EntityDamageByEntityEvent e) {
        try {
            if (e.getEntity() instanceof LivingEntity && e.getDamager() instanceof LivingEntity) {
                LivingEntity entity = (LivingEntity) e.getEntity();
                LivingEntity damager = (LivingEntity) e.getDamager();
                entity.setNoDamageTicks(0);

                if (e.getDamage() <= 0.0) {
                    return;
                }

                double knockbackForce = 0.5;
                double verticalKnockback = 0.35;

                if (entity instanceof Player) {
                    knockbackForce = 0.24;
                    verticalKnockback = 0.0;
                } else if (damager instanceof Player) {
                    Player player = (Player) damager;
                    if (player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType().name().contains("_SPADE")) {
                        knockbackForce = 0.7;
                        verticalKnockback = 0.3;
                    } else {
                        knockbackForce = 0.3;
                        verticalKnockback = 0.1;
                    }
                }

                Vector knockbackDirection = entity.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();
                Vector knockbackVelocity = knockbackDirection.multiply(knockbackForce).setY(verticalKnockback);

                // Apply knockback velocity to the entity
                entity.setVelocity(entity.getVelocity().add(knockbackVelocity));
            }
        } catch (Exception ex) {
            System.out.println("onKnockback");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDamageEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity s = (LivingEntity) e.getEntity();
            if (e.getDamage() >= s.getHealth()) {
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
                if (p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType().name().contains("_SPADE") && !this.p_arm.contains(p.getName())) {
                    int amt = 5;
                    Energy.removeEnergy(p, amt);
                    for (Entity near : le.getNearbyEntities(1, 2, 1)) {
                        if (!(near instanceof LivingEntity) || near == le || near == p) continue;
                        LivingEntity n = (LivingEntity) near;
                        le.setNoDamageTicks(0);
                        n.setNoDamageTicks(0);
                        Energy.noDamage.remove(p.getName());
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
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.6f);
                }
            }
            if (e.getEntity() instanceof Player && !(e.getDamager() instanceof Player) && e.getDamager() instanceof LivingEntity) {
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