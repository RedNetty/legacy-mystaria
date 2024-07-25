package me.retrorealms.practiceserver.mechanics.drops;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.mechanics.donations.Crates.CratesMain;
import me.retrorealms.practiceserver.mechanics.drops.buff.BuffHandler;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.mobs.Mobs;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBossHandler;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import me.retrorealms.practiceserver.utils.GlowAPI;
import me.retrorealms.practiceserver.utils.JSONMessage;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Mobdrops implements Listener {

    private static int T1RATES = 85;
    private static int T2RATES = 55;
    private static int T3RATES = 40;
    private static int T4RATES = 24;
    private static int T5RATES = 18;
    private final ArrayList<LivingEntity> bugFix = new ArrayList<>();
    PlayerCommandPreprocessEvent event;

    public static void setRATES(int tier, int rates) {
        switch (tier) {
            case 2:
                T2RATES = rates;
                break;
            case 3:
                T3RATES = rates;
                break;
            case 4:
                T4RATES = rates;
                break;
            case 5:
                T5RATES = rates;
                break;
            default:
                T1RATES = rates;
                break;
        }
        StringUtil.broadcastCentered("&e&lDROP RATES &7- &bTier " + tier + " drop rates have been changed to " + rates + "%");
    }

    public static int getT1RATES() {
        return T1RATES;
    }

    public static int getT2RATES() {
        return T2RATES;
    }

    public static int getT3RATES() {
        return T3RATES;
    }

    public static int getT4RATES() {
        return T4RATES;
    }

    public static int getT5RATES() {
        return T5RATES;
    }

    public static boolean setDropRate(LivingEntity s, boolean elite, int dropRate, int cratedrop, int targetDropRate, int targetCrateDrop, int eliteRate, int remainer) {

        if (elite && !MobHandler.isCustomNamedElite(s)) {
            if (dropRate < targetDropRate) {
                return true;
            }
        } else if (elite && MobHandler.isCustomNamedElite(s)) {
            if (dropRate < eliteRate) {
                return true;
            }
        } else if (dropRate < targetDropRate) {
            return true;
        }
        if (cratedrop < targetCrateDrop) {
            DropPriority.dropItem(s, s.getLocation(), CratesMain.createCrate(MobHandler.getTier(s), false));
        }
        if (PracticeServer.buffHandler().isActive()) {
            int buffAmount = PracticeServer.buffHandler().getActiveBuff().getUpdate();
            if ((dropRate - (dropRate * buffAmount) / 100) < targetDropRate) {
                BuffHandler lootBuff = PracticeServer.buffHandler();
                if (lootBuff.isActive()) lootBuff.updateImprovedDrops();
                return true;
            }
        }
        return false;
    }

    public static int gemDrop(int tier) {
        int gems = 3;
        int totalGems = 1;
        for (int i = 0; i < tier; i++) {
            totalGems = totalGems * gems;
        }
        if (totalGems > 64) {
            totalGems = 64;
        }
        return totalGems;
    }

    public static void dropShowString(Player killer, ItemStack is, LivingEntity livingEntity) {
        String name = "";
        if (livingEntity != null && livingEntity.hasMetadata("name")) {
            name = livingEntity.getMetadata("name").get(0).asString();
        }
        if (is.getType() == Material.BOOK) return;
        String message = ChatColor.getLastColors(name) + "➤   " + ChatColor.RED + name + ChatColor.YELLOW + " has dropped " + "@i@";
        if (livingEntity == null) {
            message = StringUtil.getCenteredMessage(ChatColor.RED + "➤" + ChatColor.YELLOW + " You have received " + "@i@" + ChatColor.YELLOW + " from the World-Boss");
        }
        String[] split = message.split("@i@");
        String after = "";
        String before = "";
        if (split.length > 0) before = split[0];
        if (split.length > 1) after = split[1];

        ItemStack stack = is;

        List<String> hoveredChat = new ArrayList<>();
        ItemMeta meta = stack.getItemMeta();
        hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
        if (meta.hasLore()) hoveredChat.addAll(meta.getLore());
        JSONMessage normal = new JSONMessage("");

        String centered = StringUtil.getCentered(before + " SHOW.");
        normal.addText(ChatColor.RESET + centered + before);
        normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
        normal.addText(ChatColor.RED + ".");
        normal.sendToPlayer(killer);
    }

    private static void enhanceLightningItem(ItemStack item, int multiplier) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (item.getType().toString().contains("HOE"))
                multiplier = (int) (multiplier * 0.35); // Reduce multiplier for hoes, but ensure it's at least 1

            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                try {
                    if (line.contains("DMG:") && line.contains("-")) {
                        String[] parts = line.split(":");
                        if (parts[1].contains("-")) {
                            String[] range = parts[1].trim().split("-");
                            int min = Integer.parseInt(range[0].trim());
                            int max = Integer.parseInt(range[1].trim());
                            min *= (int) (multiplier * .75);
                            max *= (int) (multiplier * .75);
                            lore.set(i, parts[0] + ": " + min + " - " + max);
                        } else {
                            int value = Integer.parseInt(parts[1].trim().split(" ")[0]);
                            value *= multiplier;
                            lore.set(i, parts[0] + ": " + value);
                        }
                    } else if (line.contains("HP:")) {
                        String[] parts = line.split(":");
                        int value = Integer.parseInt(parts[1].trim().split(" ")[0]);
                        value *= multiplier;
                        lore.set(i, parts[0] + ": +" + value + (line.contains("/s") ? "/s" : ""));
                    }
                    if (line.contains("HP REGEN:")) {

                        String[] parts = line.split(":");
                        int value = Integer.parseInt(parts[1].trim().split(" ")[0]);
                        value *= (multiplier * 4);
                        lore.set(i, parts[0] + ": +" + value + "/s");
                    }
                    // Add more stat types here as needed
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Error enhancing stat: " + line);
                    // If parsing fails, leave the line unchanged
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        System.out.println("Enhanced item: " + item.getItemMeta().getDisplayName() + " with multiplier: " + multiplier);
    }

    public void onEnable() {
        PracticeServer.log.info("[MobDrops] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[MobDrops] has been disabled.");
    }

    @EventHandler
    public void onMobDeath(final EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            e.getDrops().clear();
        }
        e.setDroppedExp(0);
    }


    @EventHandler
    public void onMobDeathNat(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            return; // Ignore player deaths
        }

        LivingEntity entity = event.getEntity();
        if (!MobHandler.isMobOnly(entity)) {
            return; // Ignore non-custom mobs
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!bugFix.contains(entity)) handleSecondaryDrop(entity);
            }
        }.runTaskLater(PracticeServer.getInstance(), 2L);
    }
    private void handleSecondaryDrop(LivingEntity entity) {
        if (bugFix.contains(entity)) {
            return;
        }
        System.out.println("Natural Death Drops.");
        bugFix.add(entity);
        boolean elite = MobHandler.isElite(entity);
        boolean dodrop = shouldDrop(entity, elite);

        if (dodrop) {
            if (MobHandler.isWorldBoss(entity)) {
                if (WorldBossHandler.getActiveBoss() != null) {
                    WorldBossHandler.getActiveBoss().explodeDrops(entity);
                    return;
                } else {
                    PracticeServer.log.warning("Attempted to explode drops for a world boss, but no active boss was found.");
                }
            }

            if (!MobHandler.isCustomNamedElite(entity) && elite) {
                handleEliteDrops(entity);
            } else if (!MobHandler.isCustomNamedElite(entity) && !elite) {
                handleNormalDrops(entity);
            } else if (entity.hasMetadata("type")) { // Named Elite Drops
                final String type = entity.getMetadata("type").get(0).asString();
                handleCustomEliteDrops(entity, type);
            }
        }
    }

    @EventHandler
    public void onMobDeath(final EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity entity = e.getEntity();

        if (MobHandler.isMobOnly(entity)) {
            handleMobDeath((LivingEntity) entity, damager, e);
        }
    }

    private void handleLightningDrops(LivingEntity entity) {
        // Generate high-tier armor
        Item drop = null;
        int multiplier = 2;
        if (entity.hasMetadata("LightningMultiplier")) {
            try {
                multiplier = entity.getMetadata("LightningMultiplier").get(0).asInt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int gearDrop = ThreadLocalRandom.current().nextInt(0, 2);
        switch (gearDrop) {
            case 0:
                ItemStack armor = Drops.createDrop(MobHandler.getTier(entity), ThreadLocalRandom.current().nextInt(5, 9));
                enhanceLightningItem(armor, multiplier);
                drop = DropPriority.dropItem(entity, entity.getLocation(), armor);

                break;
            case 1:
            case 2:
                // Generate high-tier weapon
                ItemStack weapon = Drops.createDrop(MobHandler.getTier(entity), ThreadLocalRandom.current().nextInt(1, 5));
                enhanceLightningItem(weapon, multiplier);
                drop = DropPriority.dropItem(entity, entity.getLocation(), weapon);
                break;
        }
        assert drop != null;
        GlowAPI.setGlowing(drop, ChatColor.LIGHT_PURPLE);
        // Visual and sound effects for special drop
        entity.getWorld().strikeLightningEffect(entity.getLocation());
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_LIGHTNING_IMPACT, 1.0f, 1.0f);
    }

    public boolean shouldDrop(LivingEntity entity, boolean elite) {
        boolean dodrop = false;
        int randomEliteDrop = ThreadLocalRandom.current().nextInt(80);
        int dropRate = ThreadLocalRandom.current().nextInt(100);
        int cratedrop = ThreadLocalRandom.current().nextInt(50);
        switch (MobHandler.getTier(entity)) {
            case 1:
                dodrop = setDropRate(entity, elite, dropRate, cratedrop, T1RATES, 10, T1RATES - 5, 150);
                break;
            case 2:
                dodrop = setDropRate(entity, elite, dropRate, cratedrop, T2RATES, 7, T2RATES - 5, 100);
                break;
            case 3:
                dodrop = setDropRate(entity, elite, dropRate, cratedrop, T3RATES, 7, T3RATES - 2, 75);
                break;
            case 4:
                dodrop = setDropRate(entity, elite, dropRate, cratedrop, T4RATES, 3, T4RATES - 2, 50);
                break;
            case 5:
                dodrop = setDropRate(entity, elite, dropRate, cratedrop, T5RATES, 1, T5RATES - 2, 30);
                if (Mobs.isGolemBoss(entity) && dropRate < 50) {
                    dodrop = true;
                }
                break;
            case 6:
                dodrop = setDropRate(entity, elite, dropRate, cratedrop, 40, 1, 20, 8);
                if (Mobs.isGolemBoss(entity) && dropRate < 30) {
                    dodrop = true;
                }
                break;
        }
        return dodrop;
    }

    public void handleMobDeath(LivingEntity entity, Entity damager, EntityDamageByEntityEvent e) {
        if (bugFix.contains(entity)) {
            return;
        }

        if (MobHandler.mobsHandIsWeapon(entity, e.getDamage())) {
            entity.playEffect(EntityEffect.ENTITY_POOF);
            entity.remove();
            bugFix.add(entity);

            Random random = new Random();
            int gems = random.nextInt(2) + 1;
            entity.setHealth(0);
            entity.remove();

            boolean elite = MobHandler.isElite(entity);
            boolean dodrop = shouldDrop(entity, elite);
            int randomEliteDrop = ThreadLocalRandom.current().nextInt(80);
            int dropRate = ThreadLocalRandom.current().nextInt(100);
            int cratedrop = random.nextInt(50);

            if (entity.getEquipment().getItemInMainHand().getItemMeta().hasEnchants()) {
                elite = true;
            }

            // TODO: Loot Buff improvement
            if (MobHandler.getTier(entity) == 5) {
                WorldBossHandler.addKill();
            }

            switch (MobHandler.getTier(entity)) {
                case 1:
                    dodrop = setDropRate(entity, elite, dropRate, cratedrop, T1RATES, 10, T1RATES - 5, 150);
                    break;
                case 2:
                    dodrop = setDropRate(entity, elite, dropRate, cratedrop, T2RATES, 7, T2RATES - 5, 100);
                    break;
                case 3:
                    dodrop = setDropRate(entity, elite, dropRate, cratedrop, T3RATES, 7, T3RATES - 2, 75);
                    break;
                case 4:
                    dodrop = setDropRate(entity, elite, dropRate, cratedrop, T4RATES, 3, T4RATES - 2, 50);
                    break;
                case 5:
                    dodrop = setDropRate(entity, elite, dropRate, cratedrop, T5RATES, 1, T5RATES - 2, 30);
                    if (Mobs.isGolemBoss(entity) && dropRate < 50) {
                        dodrop = true;
                    }
                    break;
                case 6:
                    dodrop = setDropRate(entity, elite, dropRate, cratedrop, 40, 1, 20, 8);
                    if (Mobs.isGolemBoss(entity) && dropRate < 30) {
                        dodrop = true;
                    }
                    break;
            }

            int scrollChance = ThreadLocalRandom.current().nextInt(4);
            if (scrollChance == 1) {
                DropPriority.dropItem(entity, entity.getLocation().clone().add(0, 1, 0), getBookDrop(MobHandler.getTier(entity)));
            }

            // Gem Drops
            if (gems == 1) {
                ItemStack itemStack = Money.makeGems(1);
                itemStack.setAmount(gemDrop(MobHandler.getTier(entity)));
                DropPriority.dropItem(entity, entity.getLocation().clone().add(0, 1, 0), itemStack);
                DropPriority.dropItem(entity, entity.getLocation().clone().add(0, 1, 0), itemStack);
                DropPriority.dropItem(entity, entity.getLocation().clone().add(0, 1, 0), itemStack);
            }
            if (!Spawners.isElite(entity) && entity.hasMetadata("LightningMob")) {
                handleLightningDrops(entity);
                return;
            }
            // Elite Random Drops
            if (elite && MobHandler.isCustomNamedElite(entity)) {
                if (randomEliteDrop <= 20) {
                    ItemStack protScroll = ItemAPI.getScrollGenerator().next(MobHandler.getTier(entity) - 1).clone();
                    DropPriority.dropItem(entity, entity.getLocation().clone().add(0, 1, 0), protScroll);
                }
            }

            if (MobHandler.isWorldBoss(entity)) {
                dodrop = true;
            }

            // Normal Mob Drops
            if (dodrop) {
                if (damager instanceof Player && PersistentPlayers.getCurrentQuest((Player) damager).startsWith("Kill")) {
                    Player player = (Player) damager;
                    PersistentPlayers.updateQuestProgress(player, 1);
                }
                if (MobHandler.isWorldBoss(entity)) {
                    if (WorldBossHandler.getActiveBoss() != null) {
                        WorldBossHandler.getActiveBoss().explodeDrops(entity);
                        return;
                    } else {
                        PracticeServer.log.warning("Attempted to explode drops for a world boss, but no active boss was found.");
                    }
                }

                if (!MobHandler.isCustomNamedElite(entity) && elite) {
                    handleEliteDrops(entity);
                } else if (!MobHandler.isCustomNamedElite(entity) && !elite) {
                    handleNormalDrops(entity);
                } else if (entity.hasMetadata("type")) { // Named Elite Drops
                    final String type = entity.getMetadata("type").get(0).asString();
                    handleCustomEliteDrops(entity, type);
                }
            }
        }
    }

    private void handleEliteDrops(LivingEntity entity) {
        Random random = ThreadLocalRandom.current();
        final ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        if (entity instanceof Skeleton) {
            if (entity.getEquipment().getHelmet() == null || entity.getEquipment().getHelmet().getType() == Material.SKULL_ITEM) {
                drops.add(Drops.createDrop(Mobs.getMobTier(entity), 5));
            }
        }

        ItemStack[] armorContents;
        for (int j = 0; j < 2; j++) {
            drops.add(entity.getEquipment().getItemInMainHand());
        }

        for (int length = (armorContents = entity.getEquipment().getArmorContents()).length, i = 0; i < length; ++i) {
            final ItemStack is = armorContents[i];
            if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                drops.add(is);
            }
        }

        int piece = 0;
        if (drops.size() > 1) piece = random.nextInt(drops.size());
        final ItemStack is2 = drops.get(piece);
        if (is2.getItemMeta().hasEnchants() && is2.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_MOBS)) {
            is2.removeEnchantment(Enchantment.LOOT_BONUS_MOBS);
        }
        short dura = (short) -1;
        if (dura == 0) {
            dura = 1;
        }
        if (dura == is2.getType().getMaxDurability()) {
            dura = (short) -1;
        }
        is2.setDurability((short) 0);
        if (is2.getType() == Material.JACK_O_LANTERN) return;

        Item item = DropPriority.dropItem(entity, entity.getLocation(), is2);
        ChatColor color = Listeners.groupOf(is2);
        if (color == ChatColor.AQUA) {
            entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ITEM_TOTEM_USE, 1F, 1F);
        }
        if (color == ChatColor.YELLOW) {
            entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1F, 1F);
        }
        GlowAPI.setGlowing(item, Listeners.groupOf(item.getItemStack()));
    }

    private void handleNormalDrops(LivingEntity entity) {
        Random random = ThreadLocalRandom.current();
        final ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        ItemStack[] armorContents;
        if (entity instanceof Skeleton) {
            if (entity.hasMetadata("type") && entity.getMetadata("type").get(0).asString().equals("witherskeleton")) {
                drops.add(Drops.createDrop(Mobs.getMobTier(entity), 5));
            }
        }

        for (int j = 0; j < 2; j++) {
            drops.add(entity.getEquipment().getItemInMainHand());
        }

        for (int length = (armorContents = entity.getEquipment().getArmorContents()).length, i = 0; i < length; ++i) {
            final ItemStack is = armorContents[i];
            if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                drops.add(is);
            }
        }

        int piece = 0;
        if (drops.size() > 1) piece = random.nextInt(drops.size());
        final ItemStack is2 = drops.get(piece);
        if (is2.getItemMeta().hasEnchants() && is2.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_MOBS)) {
            is2.removeEnchantment(Enchantment.LOOT_BONUS_MOBS);
        }
        short dura = (short) -1;
        if (dura == 0) {
            dura = 1;
        }
        if (dura == is2.getType().getMaxDurability()) {
            dura = (short) -1;
        }
        is2.setDurability((short) 0);
        if (is2.getType() == Material.JACK_O_LANTERN) return;

        Item item = DropPriority.dropItem(entity, entity.getLocation(), is2);
        ChatColor color = Listeners.groupOf(is2);
        if (color == ChatColor.AQUA) {
            entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ITEM_TOTEM_USE, 1F, 1F);
        }
        if (color == ChatColor.YELLOW) {
            entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1F, 1F);
        }
        GlowAPI.setGlowing(item, Listeners.groupOf(item.getItemStack()));
    }

    private void handleCustomEliteDrops(LivingEntity entity, String type) {
        ItemStack is;
        if (type.equalsIgnoreCase("krampus") || type.equalsIgnoreCase("warden") || type.equalsIgnoreCase("weakSkeletonEntity") || type.equalsIgnoreCase("bossSkeletonDungeon")) {
            is = EliteDrops.createCustomDungeonDrop(type, ThreadLocalRandom.current().nextInt(8) + 1);
        } else {
            is = EliteDrops.createCustomEliteDrop(type);
        }
        if (is == null || is.getType() == Material.JACK_O_LANTERN) {
            return;
        }

        Item itemDrop = DropPriority.dropItem(entity, entity.getLocation(), is);
        if (itemDrop != null) {
            ChatColor color = Listeners.groupOf(is);
            if (color == ChatColor.AQUA) {
                entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_TOTEM_USE, 1F, 1F);
            }
            if (color == ChatColor.YELLOW) {
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1F, 1F);
            }
            GlowAPI.setGlowing(itemDrop, Listeners.groupOf(itemDrop.getItemStack()));
        }
    }

    private ItemStack getBookDrop(int tier) {
        int scrolltype;
        if (tier == 1) {
            scrolltype = ThreadLocalRandom.current().nextInt(2);
            if (scrolltype == 0) {
                return TeleportBooks.deadpeaks_book(false);
            }
            if (scrolltype == 1) {
                return TeleportBooks.tripoli_book(false);
            }
        }
        if (tier == 2) {
            scrolltype = ThreadLocalRandom.current().nextInt(2);
            if (scrolltype == 0) {
                return TeleportBooks.deadpeaks_book(false);
            }
            if (scrolltype == 1) {
                return TeleportBooks.tripoli_book(false);
            }
        }
        if (tier == 3) {
            scrolltype = ThreadLocalRandom.current().nextInt(2);
            if (scrolltype == 0) {
                return TeleportBooks.deadpeaks_book(false);
            }
            if (scrolltype == 1) {
                return TeleportBooks.tripoli_book(false);
            }
        }
        if (tier == 4) {
            scrolltype = ThreadLocalRandom.current().nextInt(2);
            if (scrolltype == 0) {
                return TeleportBooks.deadpeaks_book(false);
            }
            if (scrolltype == 1) {
                return TeleportBooks.avalonBook(false);
            }
        }
        if (tier == 5) {
            scrolltype = ThreadLocalRandom.current().nextInt(2);
            if (scrolltype == 0) {
                return TeleportBooks.avalonBook(false);
            }
            if (scrolltype == 1) {
                return TeleportBooks.tripoli_book(false);
            }
        }
        return TeleportBooks.deadpeaks_book(false);
    }
}
