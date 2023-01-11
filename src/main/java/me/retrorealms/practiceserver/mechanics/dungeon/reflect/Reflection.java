package me.retrorealms.practiceserver.mechanics.dungeon.reflect;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Giovanni on 17-1-2017.
 */
public class Reflection {

    /**
     * Create a field to be used for injection
     *
     * @param field    The field to modify
     * @param instance The instance of the field
     * @param value    The value to set the field to
     */
    public static void setField(Field field, Object instance, Object value) {
        if (field != null) {
            field.setAccessible(true);
            try {
                field.set(instance, value);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else throw new NullPointerException();
    }

    /**
     * Get an object by clause in a specific class & field
     *
     * @param fieldName The field name
     * @param object    The object clause
     * @param clazz     The class
     * @return The object
     */
    public static Object getPrivateField(String fieldName, Class clazz, Object object) {
        Field field;
        Object toReturn = null;

        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            toReturn = field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    /**
     * Get a field in a class by name
     *
     * @param clazz The class
     * @param name  Field name
     * @return Field
     */

    public static Field getField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Object getPrivateStaticField(final Class<?> clazz, final String f) {
        try {
            Field field = clazz.getDeclaredField(f);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Access the profile properties cache
     *
     * @author Lightlord323dev
     */
    public static final LoadingCache<UUID, GameProfile> properties;

    static {
        properties = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<UUID, GameProfile>() {
                    @Override
                    public GameProfile load(UUID uuid) throws Exception {
                        //?return MinecraftServer.getServer().ay().fillProfileProperties(new GameProfile(uuid, null), true);
                        return null;
                    }
                });
    }
}
