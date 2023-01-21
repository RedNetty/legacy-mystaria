package me.retrorealms.practiceserver.mechanics.mobs;

import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBossHandler;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;

public class MobHandler {

    public static int getTier(LivingEntity s) {
        String mobsHand = s.getEquipment().getItemInMainHand().getType().name();
        boolean t6 = Items.isBlueLeather(s.getEquipment().getArmorContents()[0]);
        if (mobsHand.contains("WOOD_")) {
            return 1;
        } else if (mobsHand.contains("STONE_")) {
            return 2;
        } else if (mobsHand.contains("IRON_")) {
            return 3;
        } else if (mobsHand.contains("DIAMOND_") && !t6) {
            return 4;
        } else if (mobsHand.contains("GOLD_")) {
            return 5;
        } else if (mobsHand.contains("DIAMOND_") && t6) {
            return 6;
        }
        return 11;
    }

    public static boolean isCustomNamedElite(String type) {
        if (type.equalsIgnoreCase("mitsuki") || type.equalsIgnoreCase("copjak") || type.equalsIgnoreCase("impa")
                || type.equalsIgnoreCase("skeletonking") || type.equalsIgnoreCase("kingofgreed")
                || type.equalsIgnoreCase("blayshan") || type.equalsIgnoreCase("bloodbutcher")
                || type.equalsIgnoreCase("jayden") || type.equalsIgnoreCase("kilatan")
                || type.equalsIgnoreCase("bossSkeletonDungeon") || type.equalsIgnoreCase("spiderQueen")
                || type.equalsIgnoreCase("frostKing") || type.equalsIgnoreCase("watchMaster")
                || type.equalsIgnoreCase("duranor") || type.equalsIgnoreCase("frozenGolem")
                || type.equalsIgnoreCase("weakSkeletonEntity") || type.equalsIgnoreCase("frozenElite")
                || type.equalsIgnoreCase("frozenBoss") || type.equalsIgnoreCase("spectralKnight")
                || type.equalsIgnoreCase("krampus") || type.equalsIgnoreCase("risk_Elite")
                || type.equalsIgnoreCase("warden") || type.equalsIgnoreCase("orcKing")) {
            return true;
        }
        return false;
    }

    public static boolean isWorldBoss(final Entity e) {
        if (!(e instanceof LivingEntity)) return false;
        LivingEntity l = (LivingEntity) e;
        if (WorldBossHandler.getActiveBoss() != null && WorldBossHandler.getActiveBoss().getLivingEntity().getName().equals(l.getName())) {
            return true;
        }
        if (l.hasMetadata("type") && l.getMetadata("type").get(0).asString().equalsIgnoreCase("frostwing")) {
            return true;
        }
        return false;
    }

    public static boolean isCustomNamedElite(final LivingEntity l) {
        if (l.hasMetadata("type")) {
            final String type = l.getMetadata("type").get(0).asString();
            if (type.equalsIgnoreCase("mitsuki") || type.equalsIgnoreCase("copjak") || type.equalsIgnoreCase("impa")
                    || type.equalsIgnoreCase("skeletonking") || type.equalsIgnoreCase("kingofgreed")
                    || type.equalsIgnoreCase("blayshan") || type.equalsIgnoreCase("bloodbutcher")
                    || type.equalsIgnoreCase("jayden") || type.equalsIgnoreCase("kilatan")
                    || type.equalsIgnoreCase("bossSkeletonDungeon") || type.equalsIgnoreCase("spiderQueen")
                    || type.equalsIgnoreCase("frostKing") || type.equalsIgnoreCase("watchMaster")
                    || type.equalsIgnoreCase("duranor") || type.equalsIgnoreCase("frozenGolem")
                    || type.equalsIgnoreCase("weakSkeletonEntity") || type.equalsIgnoreCase("frozenElite")
                    || type.equalsIgnoreCase("frozenBoss") || type.equalsIgnoreCase("spectralKnight")
                    || type.equalsIgnoreCase("krampus") || type.equalsIgnoreCase("risk_Elite")
                    || type.equalsIgnoreCase("warden") || type.equalsIgnoreCase("orcKing")) {
                return true;
            }
        }
        return false;
    }

    public static boolean t6DungeonDefeat(Skeleton s) {
        if (s.hasMetadata("type") && s.getMetadata("type").get(0).asString().equals("bossSkeleteonDungeonDAEMON")) {
            return true;
        }
        return false;
    }

    public static boolean isMobOnly(Entity e) {
        if (e instanceof LivingEntity && !(e instanceof Player)) {
            return true;
        }
        return false;
    }

    public static boolean isDaemon(LivingEntity s) {
        if (s.hasMetadata("type") && s.getMetadata("type").get(0).asString().equals("bossSkeleteonDungeonDAEMON")) {
            return true;
        }
        return false;
    }

    public static boolean isDeathlord(LivingEntity s) {
        if (s.hasMetadata("type") && s.getMetadata("type").get(0).asString().equals("bossSkeletonDungeon")) {
            return true;
        }
        return false;
    }

    public static boolean mobsHandIsWeapon(LivingEntity s, double damage) {
        if (damage >= s.getHealth() && s.getEquipment().getItemInMainHand() != null && s.getEquipment().getItemInMainHand().getType() != Material.AIR) {
            return true;
        }
        return false;
    }

    public static boolean isElite(LivingEntity s) {
        if (s.getEquipment().getItemInMainHand().getItemMeta().hasEnchants()) {
            return true;
        }
        return false;
    }
}
