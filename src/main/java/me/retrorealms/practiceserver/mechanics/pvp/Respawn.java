package me.retrorealms.practiceserver.mechanics.pvp;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.item.Journal;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.teleport.Hearthstone;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.glow.GlowAPI;

import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

public class Respawn implements Listener {
    List<Player> dead;
    private final HashMap<Player, Integer> horseMap = new HashMap<>();

    public Respawn() {
        this.dead = new ArrayList<Player>();
    }

    public void onEnable() {
        PracticeServer.log.info("[Respawn] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents((Listener) this, PracticeServer.plugin);
        if(PracticeServer.DATABASE) return;
        final File file = new File(PracticeServer.plugin.getDataFolder(), "respawndata");
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public void onDisable() {
        PracticeServer.log.info("[Respawn] has been disabled.");
        if(PracticeServer.DATABASE) return;
        final File file = new File(PracticeServer.plugin.getDataFolder(), "respawndata");
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(final PlayerDeathEvent e) {
        final Player p = e.getEntity();
        if (!this.dead.contains(p)) {
            this.dead.add(p);
            Location loc = p.getLocation();
            final Random random = new Random();
            final int wepdrop = random.nextInt(2) + 1;
            final int armor = random.nextInt(4) + 1;
            final List<ItemStack> newInventory = new ArrayList<ItemStack>();

            HashMap<Integer, ? extends ItemStack> itemMap = p.getInventory().all(Material.INK_SACK);

            for (int i : itemMap.keySet()) {
                if (i == 0) continue;

                ItemStack itemStack = itemMap.get(i);
                if (itemStack != null && itemStack.getType() != Material.AIR) {

                    if (itemStack.getType() != Material.INK_SACK) continue;

                    if (!itemStack.hasItemMeta()) continue;
                    if (!itemStack.getItemMeta().hasDisplayName()) continue;

                    if (ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()).contains("Insane Gem Container")) {
                        newInventory.add(itemStack);

                        e.getDrops().remove(itemStack);
                    }
                }
            }

            if (!Alignments.neutral.containsKey(p.getName()) && !Alignments.chaotic.containsKey(p.getName())) {
                ItemStack[] armorContents;
                for (int length = (armorContents = p.getInventory().getArmorContents()).length, j = 0; j < length; ++j) {
                    final ItemStack is = armorContents[j];
                    if (is != null && is.getType() != Material.AIR) {
                        newInventory.add(is);
                    }
                }
                if (p.getInventory().getItem(0) != null && !p.getInventory().getItem(0).getType().name().contains("_PICKAXE") && !p.getInventory().getItem(0).getType().name().contains("FISHING")) {
                    final ItemStack is = p.getInventory().getItem(0);
                    if (is.getType().name().contains("_SWORD") || is.getType().name().contains("_AXE") || is.getType().name().contains("_SPADE") || is.getType().name().contains("_HOE") || is.getType().name().contains("_HELMET") || is.getType().name().contains("_CHESTPLATE") || is.getType().name().contains("_BOOTS")) {
                        newInventory.add(is);
                    }
                }
                ItemStack[] contents;
                for (int length2 = (contents = p.getInventory().getContents()).length, k = 0; k < length2; ++k) {
                    final ItemStack is = contents[k];
                    if (is != null && is.getType() != Material.AIR) {
                        if (is.getType().name().contains("_PICKAXE") || is.getType().name().contains("FISHING")) {
                            newInventory.add(is);
                        }
                    }
                }
                if(Alignments.chaotic.containsKey(p.getName())) Alignments.setNeutral(p);
            } else if (Alignments.neutral.containsKey(p.getName()) && !Alignments.chaotic.containsKey(p.getName())) {
                final List<ItemStack> arm = new ArrayList<ItemStack>();
                ItemStack[] armorContents2;
                for (int length3 = (armorContents2 = p.getInventory().getArmorContents()).length, l = 0; l < length3; ++l) {
                    final ItemStack is2 = armorContents2[l];
                    if (is2 != null && is2.getType() != Material.AIR) {
                        arm.add(is2);
                    }
                }
                if (armor == 1 && arm.size() > 0) {
                    arm.remove(arm.get(random.nextInt(arm.size())));
                }
                if (arm.size() > 0) {
                    for (final ItemStack is2 : arm) {
                        newInventory.add(is2);
                    }
                }
                if (wepdrop == 1 && p.getInventory().getItem(0) != null && !p.getInventory().getItem(0).getType().name().contains("_PICKAXE") && !p.getInventory().getItem(0).getType().name().contains("FISHING")) {
                    final ItemStack is2 = p.getInventory().getItem(0);
                    if (is2.getType().name().contains("_SWORD") || is2.getType().name().contains("_AXE") || is2.getType().name().contains("_SPADE") || is2.getType().name().contains("_HOE") || is2.getType().name().contains("_HELMET") || is2.getType().name().contains("_CHESTPLATE") || is2.getType().name().contains("_BOOTS")) {
                        newInventory.add(is2);
                    }
                }
                ItemStack[] contents2;
                for (int length4 = (contents2 = p.getInventory().getContents()).length, n = 0; n < length4; ++n) {
                    final ItemStack is2 = contents2[n];
                    if (is2 != null && is2.getType() != Material.AIR) {
                        if (is2.getType().name().contains("_PICKAXE") || is2.getType().name().contains("FISHING")) {
                            newInventory.add(is2);
                        }
                    }
                }
            }
            ItemStack[] contents3;
            for (int length5 = (contents3 = p.getInventory().getContents()).length, n2 = 0; n2 < length5; ++n2) {
                final ItemStack is = contents3[n2];
                if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore() && is.getItemMeta().getLore().contains(ChatColor.GRAY + "Permenant Untradeable") && !newInventory.contains(is)) {
                    newInventory.add(is);
                }
            }
            if(PracticeServer.DATABASE){
                SQLMain.updateRespawnData(p, newInventory);
            }else {
                final File file = new File(PracticeServer.plugin.getDataFolder() + "/respawndata", String.valueOf(p.getName()) + ".yml");
                final YamlConfiguration config = new YamlConfiguration();
                for (int i = 0; i < newInventory.size(); ++i) {
                    config.set(new StringBuilder().append(i).toString(), (Object) newInventory.get(i));
                }
                try {
                    config.save(file);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            for (final ItemStack is3 : newInventory) {
                if (is3 != null) {
                    final ItemMeta meta = is3.getItemMeta();
                    meta.setLore((List<String>) Arrays.asList("notarealitem"));
                    is3.setItemMeta(meta);
                }
            }
            Bukkit.getOnlinePlayers().stream().filter(entity -> entity instanceof Item).map(entity -> (Item) entity).forEach(item -> {
                ItemStack itemStack = item.getItemStack();
                if (itemStack != null && (itemStack.getType() != Material.AIR) && (itemStack.hasItemMeta()) && (itemStack.getItemMeta().hasLore())) {
                    GlowAPI.Color color = groupOf(itemStack);
                    if (color != null) {
                        List<Player> playerList = new ArrayList<>();
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (Toggles.hasGlowOnDrops(player)) {
                                playerList.add(player);
                            }
                        }
                        me.retrorealms.practiceserver.utils.GlowAPI.setGlowing(item, groupOf(item.getItemStack()));
                    }
                }
            });
        }
    }
    private GlowAPI.Color groupOf(ItemStack itemStack) {
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

        return GlowAPI.Color.WHITE;
    }

    @EventHandler
    public void onRespawn(final PlayerRespawnEvent e) {
        final Player p = e.getPlayer();
        if (this.dead.contains(p)) {
            this.dead.remove(p);
        }
        if(PracticeServer.DATABASE){
            SQLMain.loadRespawnData(p);
        }else {
            final File file = new File(PracticeServer.plugin.getDataFolder() + "/respawndata", String.valueOf(p.getName()) + ".yml");
            final YamlConfiguration config = new YamlConfiguration();
            try {
                config.load(file);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            for (int i = 0; i < p.getInventory().getSize(); ++i) {
                if (config.contains(new StringBuilder().append(i).toString())) {
                    e.getPlayer().getInventory().addItem(new ItemStack[]{config.getItemStack(new StringBuilder().append(i).toString())});
                }
            }
        }
        Listeners.Kit(p);
        if (!p.getInventory().contains(Material.QUARTZ)) {
            p.getInventory().addItem(new ItemStack[]{Hearthstone.hearthstone()});
        }
        if (!p.getInventory().contains(Material.WRITTEN_BOOK)) {
            p.getInventory().addItem(new ItemStack[]{Journal.journal()});
        }
        e.getPlayer().setMaxHealth(50.0);
        e.getPlayer().setHealth(50.0);
        p.setLevel(100);
        p.setExp(1.0f);
        p.getInventory().setHeldItemSlot(0);
        new BukkitRunnable() {
            public void run() {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
            }
        }.runTaskLater(PracticeServer.plugin, 1L);
    }
}
