package me.retrorealms.practiceserver.mechanics.mobs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Created by Giovanni on 15-5-2017.
 */
public enum SkullTextures {

    CHERRY("d525707696bcd15a173056fa39296e80ff41168bb0add552f4523e2558a3119"),
    FROST("ab2344a2aace0350158750ce137ae6d337edbcaee2e894aa69eaac0bf9b5869c"),
    APPLE("cbb311f3ba1c07c3d1147cd210d81fe11fd8ae9e3db212a0fa748946c3633"),
    WITHER_KING("68c0165e9b2dbd78dac91277e97d9a02648f3059e126a5941a84d05429ce"),
    DEMON("d2975b67c19f9ba 2344f8eee956c5015ad63d9e88ad4882ae79369374fb3975"),
    DEMON_KILATAN("1464eb8e99e2878f343803a742ef57ceafacc2283e67b88edec16821316f9f"),
    DEMON_JAYDEN("444772dc4def22219ee6d889ccdc2f9232ee23d356dd9e4adcea5f72cc0c689"),
    VOID("c84bcd9ab0b02a9eaeaae2648282d3d5e90d24d483979eaffb19390243eca301");

    private String b64String;

    SkullTextures(String base64String) {
        this.b64String = base64String;
    }

    public String getURL() {
        return "http://textures.minecraft.net/texture/" + this.b64String;
    }

    public ItemStack getSkullByURL() {
        String url = getURL();
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        if (url == null || url.isEmpty()) {
            return skull;
        }
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        assert profileField != null;
        profileField.setAccessible(true);
        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        skull.setItemMeta(skullMeta);
        return skull;
    }

    public ItemStack getFrostSkull() {
        try {
            ItemStack head = new ItemStack(Material.SKULL, 1, (short) 3);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwner("87d0088a-09c2-4765-8f9c-9a2f5db7acbc");
            head.setItemMeta(meta);

            return head;
        }catch (Exception e) {
            return CHERRY.getSkullByURL();
        }
    }
    public ItemStack getVoidSkull() {
        try {


            return VOID.getSkullByURL();
        }catch (Exception e) {
            return CHERRY.getSkullByURL();
        }
    }
    /**
     * Create a textured skull via a mojang texture URL.
     *
     * @param par1 The URL of the texture.
     * @return Textured skull.
     */
    public static ItemStack createSkullFromURL(String par1) {
        String url = "http://textures.minecraft.net/texture/" + par1;
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        if (url.isEmpty()) {
            return skull;
        }
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        assert profileField != null;
        profileField.setAccessible(true);
        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        skull.setItemMeta(skullMeta);
        return skull;
    }
}
