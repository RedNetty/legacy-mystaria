package me.retrorealms.practiceserver.mechanics.drops;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.mechanics.donations.Crates.CratesMain;
import me.retrorealms.practiceserver.mechanics.drops.buff.BuffHandler;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.mobs.Mobs;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBossHandler;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import me.retrorealms.practiceserver.utils.JSONMessage;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.glow.GlowAPI;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Mobdrops implements Listener {

    PlayerCommandPreprocessEvent event;

    private static int T1RATES = 85;
    private static int T2RATES = 55;
    private static int T3RATES = 40;
    private static int T4RATES = 24;
    private static int T5RATES = 18;

    public static void setRATES(int tier, int rates) {
        switch(tier) {
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
        StringUtil.broadcastCentered("&e&lDROP RATES &7- &bTier " + tier + " drop rates have been changed to " + rates + "%" );
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

    private final ArrayList<LivingEntity> bugFix = new ArrayList<>();

    public static boolean setDropRate(Entity player, LivingEntity s, boolean elite, int dropRate, int cratedrop, int targetDropRate, int targetCrateDrop, int eliteRate, int remainer) {

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
            DropPriority.DropItem(player, s, s.getLocation(), CratesMain.createCrate(MobHandler.getTier(s), false));
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
        if(is.getType() == Material.BOOK) return;
        String message = ChatColor.getLastColors(name) + "➤   " + ChatColor.RED + name + ChatColor.YELLOW + " has dropped " + "@i@";
        if (livingEntity == null) {
            message = StringUtil.getCenteredMessage(ChatColor.RED + "                      ➤" + ChatColor.YELLOW + " You have received " + "@i@" + ChatColor.YELLOW + " from the World-Boss");
        }
        String[] split = message.split("@i@");
        String after = "";
        String before = "";
        if (split.length > 0)
            before = split[0];
        if (split.length > 1)
            after = split[1];

        ItemStack stack = is;

        List<String> hoveredChat = new ArrayList<>();
        ItemMeta meta = stack.getItemMeta();
        hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
        if (meta.hasLore())
            hoveredChat.addAll(meta.getLore());
        JSONMessage normal = new JSONMessage("");

        String centered = StringUtil.getCentered(before + " SHOW.");
        normal.addText(ChatColor.RESET + centered + before + "");
        normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
        normal.addText(ChatColor.RED + ".");
        normal.sendToPlayer(killer);
    }

    public static GlowAPI.Color groupOf(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().getDisplayName().contains("Loot Crate"))
            return GlowAPI.Color.RED;
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
    public void onMobDeath(final EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity entity = e.getEntity();

        if (MobHandler.isMobOnly(entity)) {
            handleMobDeath((LivingEntity) entity, damager, e);
        }
    }

    private void handleMobDeath(LivingEntity entity, Entity damager, EntityDamageByEntityEvent e) {
        if (bugFix.contains(entity)) {
            return;
        }

        if (MobHandler.mobsHandIsWeapon(entity, e.getDamage())) {
            entity.playEffect(EntityEffect.DEATH);
            entity.remove();
            bugFix.add(entity);

            Random random = new Random();
            int gems = random.nextInt(2) + 1;
            boolean dodrop = false;
            boolean elite = false;
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
                    dodrop = setDropRate(damager, entity, elite, dropRate, cratedrop,
                            T1RATES, 10, T1RATES - 5, 150);
                    break;
                case 2:
                    dodrop = setDropRate(damager, entity, elite, dropRate, cratedrop,
                            T2RATES, 7, T2RATES - 5, 100);
                    break;
                case 3:
                    dodrop = setDropRate(damager, entity, elite, dropRate, cratedrop,
                            T3RATES, 7, T3RATES - 2, 75);
                    break;
                case 4:
                    dodrop = setDropRate(damager, entity, elite, dropRate, cratedrop,
                            T4RATES, 3, T4RATES - 2, 50);
                    break;
                case 5:
                    dodrop = setDropRate(damager, entity, elite, dropRate, cratedrop,
                            T5RATES, 1, T5RATES - 2, 30);
                    if (Mobs.isGolemBoss(entity) && dropRate < 50) {
                        dodrop = true;
                    }
                    break;
                case 6:
                    dodrop = setDropRate(damager, entity, elite, dropRate, cratedrop,
                            40, 1, 20, 8);
                    if (Mobs.isGolemBoss(entity) && dropRate < 30) {
                        dodrop = true;
                    }
                    break;
            }

            int scrollChance = ThreadLocalRandom.current().nextInt(4);
            if (scrollChance == 1) {
                DropPriority.DropItem(damager, entity, entity.getLocation().clone().add(0, 1, 0), getBookDrop(MobHandler.getTier(entity)));
            }

            // Gem Drops
            if (gems == 1) {
                ItemStack itemStack = Money.makeGems(1);
                itemStack.setAmount(gemDrop(MobHandler.getTier(entity)));
                DropPriority.DropItem(damager, entity, entity.getLocation().clone().add(0, 1, 0), itemStack);
                DropPriority.DropItem(damager, entity, entity.getLocation().clone().add(0, 1, 0), itemStack);
                DropPriority.DropItem(damager, entity, entity.getLocation().clone().add(0, 1, 0), itemStack);
            }

            // Elite Random Drops
            if (elite && MobHandler.isCustomNamedElite(entity)) {
                if (randomEliteDrop <= 20) {
                    ItemStack protScroll = ItemAPI.getScrollGenerator().next(MobHandler.getTier(entity) - 1).clone();
                    DropPriority.DropItem(damager, entity, entity.getLocation().clone().add(0, 1, 0), protScroll);
                }
            }

            if (MobHandler.isWorldBoss(entity)) {
                dodrop = true;
            }

            // Normal Mob Drops
            if (dodrop) {
                if (MobHandler.isWorldBoss(entity)) {
                    WorldBossHandler.getActiveBoss().explodeDrops(entity);
                    return;
                }

                if (!MobHandler.isCustomNamedElite(entity) && elite) {
                    handleEliteDrops(damager, entity);
                } else if (!MobHandler.isCustomNamedElite(entity) && !elite) {
                    handleNormalDrops(damager, entity);
                } else if (entity.hasMetadata("type")) { // Named Elite Drops
                    final String type = entity.getMetadata("type").get(0).asString();
                    handleCustomEliteDrops(damager, entity, type);
                }
            }
        }
    }

    private void handleEliteDrops(Entity damager, LivingEntity entity) {
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
        if (drops.size() > 1)
            piece = random.nextInt(drops.size());
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
        if (is2.getType() == Material.JACK_O_LANTERN)
            return;

        Item item = DropPriority.DropItem(damager, entity, entity.getLocation(), is2);
        GlowAPI.Color color = groupOf(is2);
        if (color == GlowAPI.Color.AQUA) {
            entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ITEM_TOTEM_USE, 1F, 1F);
        }
        if (color == GlowAPI.Color.YELLOW) {
            entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1F, 1F);
        }
        //GlowAPI.setGlowing(item, groupOf(item.getItemStack()), Bukkit.getOnlinePlayers());
    }

    private void handleNormalDrops(Entity damager, LivingEntity entity) {
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
        if (drops.size() > 1)
            piece = random.nextInt(drops.size());
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
        if (is2.getType() == Material.JACK_O_LANTERN)
            return;

        Item item = DropPriority.DropItem(damager, entity, entity.getLocation(), is2);
        GlowAPI.Color color = groupOf(is2);
        if (color == GlowAPI.Color.AQUA) {
            entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ITEM_TOTEM_USE, 1F, 1F);
        }
        if (color == GlowAPI.Color.YELLOW) {
            entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1F, 1F);
        }
        GlowAPI.setGlowing(item, groupOf(item.getItemStack()), Bukkit.getOnlinePlayers());
    }

    private void handleCustomEliteDrops(Entity damager, LivingEntity entity, String type) {
        ItemStack is;
        if (type.equalsIgnoreCase("krampus") || type.equalsIgnoreCase("warden")
                || type.equalsIgnoreCase("weakSkeletonEntity") || type.equalsIgnoreCase("bossSkeletonDungeon")) {
            is = EliteDrops.createCustomDungeonDrop(type, new Random().nextInt(8) + 1);
        } else {
            is = EliteDrops.createCustomEliteDrop(type);
        }
        if (is.getType() == Material.JACK_O_LANTERN)
            return;

        Item itemDrop = DropPriority.DropItem(damager, entity, entity.getLocation(), is);
        GlowAPI.Color color = groupOf(is);
        if (color == GlowAPI.Color.AQUA) {
            entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ITEM_TOTEM_USE, 1F, 1F);
        }
        if (color == GlowAPI.Color.YELLOW) {
            entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1F, 1F);
        }
        GlowAPI.setGlowing(itemDrop, groupOf(itemDrop.getItemStack()), Bukkit.getOnlinePlayers());
    }

    private ItemStack getBookDrop(int tier) {
        int scrolltype;
        if (tier == 1) {
            scrolltype = ThreadLocalRandom.current().nextInt(2);
            if (scrolltype == 0) {
                return TeleportBooks.deadpeaksBook(false);
            }
            if (scrolltype == 1) {
                return TeleportBooks.tripoliBook(false);
            }
        }
        if (tier == 2) {
            scrolltype = ThreadLocalRandom.current().nextInt(2);
            if (scrolltype == 0) {
                return TeleportBooks.deadpeaksBook(false);
            }
            if (scrolltype == 1) {
                return TeleportBooks.tripoliBook(false);
            }
        }
        if (tier == 3) {
            scrolltype = ThreadLocalRandom.current().nextInt(2);
            if (scrolltype == 0) {
                return TeleportBooks.deadpeaksBook(false);
            }
            if (scrolltype == 1) {
                return TeleportBooks.tripoliBook(false);
            }
        }
        if (tier == 4) {
            scrolltype = ThreadLocalRandom.current().nextInt(2);
            if (scrolltype == 0) {
                return TeleportBooks.deadpeaksBook(false);
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
                return TeleportBooks.tripoliBook(false);
            }
        }
        return TeleportBooks.deadpeaksBook(false);
    }
}
