package me.retrorealms.practiceserver.mechanics.pvp;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.item.Journal;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.teleport.Hearthstone;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
            final Random random = new Random();
            final int wepdrop = random.nextInt(2) + 1;
            final int armor = random.nextInt(4) + 1;
            final List<ItemStack> newInventory = new ArrayList<>();

            p.getInventory().all(Material.INK_SACK).values().stream()
                    .filter(Objects::nonNull)
                    .filter(itemStack -> itemStack.getType() == Material.INK_SACK)
                    .filter(itemStack -> itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
                    .filter(itemStack -> ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()).contains("Insane Gem Container"))
                    .forEach(itemStack -> {
                        newInventory.add(itemStack);
                        e.getDrops().remove(itemStack);
                    });

            if (!Alignments.neutral.containsKey(p.getName()) && !Alignments.chaotic.containsKey(p.getName())) {
                Stream.of(p.getInventory().getArmorContents())
                        .filter(Objects::nonNull)
                        .filter(itemStack -> itemStack.getType() != Material.AIR)
                        .forEach(newInventory::add);

                ItemStack is = p.getInventory().getItem(0);
                if (is != null && !is.getType().name().contains("_PICKAXE") && !is.getType().name().contains("FISHING") && is.getType().name().matches(".*(_SWORD|_AXE|_SPADE|_HOE|_HELMET|_CHESTPLATE|_BOOTS)")) {
                    newInventory.add(is);
                }

                Stream.of(p.getInventory().getContents())
                        .filter(Objects::nonNull)
                        .filter(itemStack -> itemStack.getType() != Material.AIR)
                        .filter(itemStack -> itemStack.getType().name().matches(".*(_PICKAXE|FISHING)"))
                        .forEach(newInventory::add);

                if(Alignments.chaotic.containsKey(p.getName())) Alignments.setNeutral(p);
            } else if (Alignments.neutral.containsKey(p.getName()) && !Alignments.chaotic.containsKey(p.getName())) {
                final List<ItemStack> arm = Stream.of(p.getInventory().getArmorContents())
                        .filter(Objects::nonNull)
                        .filter(itemStack -> itemStack.getType() != Material.AIR)
                        .collect(Collectors.toList());

                if (armor == 1 && !arm.isEmpty()) {
                    arm.remove(random.nextInt(arm.size()));
                }

                newInventory.addAll(arm);

                ItemStack is = p.getInventory().getItem(0);
                if (wepdrop == 1 && is != null && !is.getType().name().contains("_PICKAXE") && !is.getType().name().contains("FISHING") && is.getType().name().matches(".*(_SWORD|_AXE|_SPADE|_HOE|_HELMET|_CHESTPLATE|_BOOTS)")) {
                    newInventory.add(is);
                }

                Stream.of(p.getInventory().getContents())
                        .filter(Objects::nonNull)
                        .filter(itemStack -> itemStack.getType() != Material.AIR)
                        .filter(itemStack -> itemStack.getType().name().matches(".*(_PICKAXE|FISHING)"))
                        .forEach(newInventory::add);
            }

            Stream.of(p.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .filter(itemStack -> itemStack.getType() != Material.AIR)
                    .filter(itemStack -> itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().getLore().contains(ChatColor.GRAY + "Permenant Untradeable"))
                    .filter(itemStack -> !newInventory.contains(itemStack))
                    .forEach(newInventory::add);
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
                        GlowAPI.setGlowing(item, groupOf(item.getItemStack()), Bukkit.getOnlinePlayers());
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
                    e.getPlayer().getInventory().addItem(config.getItemStack(new StringBuilder().append(i).toString()));
                }
            }
        }
        Listeners.Kit(p);
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
