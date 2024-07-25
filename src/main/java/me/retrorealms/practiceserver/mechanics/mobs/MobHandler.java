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
        return type.equalsIgnoreCase("plaguebearer")
                || type.equalsIgnoreCase("bonereaver")
                || type.equalsIgnoreCase("soulreaper")
                || type.equalsIgnoreCase("doomherald")
                || type.equalsIgnoreCase("nethermancer")
                || type.equalsIgnoreCase("voidlord")
                || type.equalsIgnoreCase("mitsuki")
                || type.equalsIgnoreCase("copjak")
                || type.equalsIgnoreCase("impa")
                || type.equalsIgnoreCase("skeletonking")
                || type.equalsIgnoreCase("kingofgreed")
                || type.equalsIgnoreCase("blayshan")
                || type.equalsIgnoreCase("bloodbutcher")
                || type.equalsIgnoreCase("jayden")
                || type.equalsIgnoreCase("kilatan")
                || type.equalsIgnoreCase("bossSkeletonDungeon")
                || type.equalsIgnoreCase("spiderQueen")
                || type.equalsIgnoreCase("frostKing")
                || type.equalsIgnoreCase("watchMaster")
                || type.equalsIgnoreCase("duranor")
                || type.equalsIgnoreCase("frozenGolem")
                || type.equalsIgnoreCase("weakSkeletonEntity")
                || type.equalsIgnoreCase("frozenElite")
                || type.equalsIgnoreCase("frozenBoss")
                || type.equalsIgnoreCase("spectralKnight")
                || type.equalsIgnoreCase("krampus")
                || type.equalsIgnoreCase("risk_Elite")
                || type.equalsIgnoreCase("warden")
                || type.equalsIgnoreCase("grandWizard")
                || type.equalsIgnoreCase("orcKing");
    }

    public static boolean isWorldBoss(final Entity e) {
        if (!(e instanceof LivingEntity)) return false;
        LivingEntity l = (LivingEntity) e;
        if(!l.hasMetadata("type")) return false;
        String eliteName = l.getMetadata("type").get(0).asString();
        if (WorldBossHandler.getActiveBoss() != null && WorldBossHandler.getActiveBoss().getLivingEntity().getName().equals(l.getName())) {
            return true;
        }
        return eliteName.equalsIgnoreCase("frostwing") || eliteName.equalsIgnoreCase("chronos");
    }


    public static boolean isCustomNamedElite(final LivingEntity l) {
        if (l.hasMetadata("type")) {
            final String type = l.getMetadata("type").get(0).asString();
            return isCustomNamedElite(type);
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
