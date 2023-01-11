package me.retrorealms.practiceserver.mechanics.useless;

import me.retrorealms.practiceserver.mechanics.useless.abyss.EntityDaemonLord;
import me.retrorealms.practiceserver.mechanics.useless.skeleton.SkeletonBoss;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Giovanni on 2-5-2017.
 */
public enum EntityRegistry {

    SKELETON_BOSS("SkeletonBoss_EntityLiving", 51, EntityType.SKELETON, EntitySkeleton.class, SkeletonBoss.class),
    DAEMON_LORD("DaemonLord_EntityLiving", 64, EntityType.WITHER, EntityWither.class, EntityDaemonLord.class);

    private String name;
    private int id;
    private EntityType entityType;
    private Class<?> nmsClass;
    private Class<?> customClass;

    private MinecraftKey key;
    private MinecraftKey oldKey;

    @SuppressWarnings("unchecked")
    EntityRegistry(String name, int id, EntityType entityType, Class<?> nmsClass, Class<?> customClass) {
        this.name = name;
        this.id = id;
        this.entityType = entityType;
        this.nmsClass = nmsClass;
        this.customClass = customClass;
        this.key = new MinecraftKey(name);
    }


    public static void registerEntities() {
        for (EntityRegistry entity : values())
            entity.registerEntity(entity.getName(), entity.getID(), entity.getNmsClass(), entity.getCustomClass());
    }

    public Class<?> getNmsClass() {
        return nmsClass;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Class<?> getCustomClass() {
        return customClass;
    }

    private void registerEntity(String name, int id, Class<?> nmsClass, Class<?> customClass) {
        try {

            List<Map<?, ?>> dataMaps = new ArrayList<>();
            for (Field f : EntityTypes.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())) {
                    f.setAccessible(true);
                    dataMaps.add((Map<?, ?>) f.get(null));
                }
            }

            if (dataMaps.get(2).containsKey(id)) {
                dataMaps.get(0).remove(name);
                dataMaps.get(2).remove(id);
            }

            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null, customClass, name, id);
            for (Field f : BiomeBase.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals(BiomeBase.class.getSimpleName())) {
                    if (f.get(null) != null) {

                        for (Field list : BiomeBase.class.getDeclaredFields()) {
                            if (list.getType().getSimpleName().equals(List.class.getSimpleName())) {
                                list.setAccessible(true);
                                @SuppressWarnings("unchecked")
                                List<BiomeBase.BiomeMeta> metaList = (List<BiomeBase.BiomeMeta>) list.get(f.get(null));

                                for (BiomeBase.BiomeMeta meta : metaList) {
                                    Field clazz = BiomeBase.BiomeMeta.class.getDeclaredFields()[0];
                                    if (clazz.get(meta).equals(nmsClass)) {
                                        clazz.set(meta, customClass);
                                    }
                                }
                            }
                        }

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
