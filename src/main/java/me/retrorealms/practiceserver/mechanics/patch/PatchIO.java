package me.retrorealms.practiceserver.mechanics.patch;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.enchants.Enchants;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Giovanni on 7-7-2017.
 */
public class PatchIO {

    private Inventory inventory;

    public static void main(String[] args) {
        NotesFile notesFile = new NotesFile()

                .setNotes(Arrays.asList("- nigger", "- fuck you"))
                .setVersionId("1.0.0");

        System.out.println(new Gson().toJson(notesFile));
    }

    public PatchIO() {

        try {
            InputStream inputStream = PracticeServer.getInstance().getResource("patchnotes.inst");
            String jsonText = IOUtils.toString(inputStream, "UTF-8");

            if (jsonText == null) return;

            NotesFile notesFile = new Gson().fromJson(jsonText, NotesFile.class);

            String title = ChatColor.translateAlternateColorCodes('&', notesFile.getVersionId());
            this.inventory = Bukkit.createInventory(null, 54, "Patch notes for: " + title);

            ItemStack itemStack = new ItemStack(Material.BOOK);
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.setDisplayName(ChatColor.GRAY + "Patch: " + title);

            List<String> fixedLore = Lists.newArrayList();
            notesFile.getNotes().forEach(note -> {
                fixedLore.add(ChatColor.translateAlternateColorCodes('&', note));
            });

            itemMeta.setLore(fixedLore);

            itemStack.setItemMeta(itemMeta);
            itemStack.addEnchantment(Enchants.glow, 1);

            this.inventory.setItem(0, itemStack);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Inventory getInventory() {
        return inventory;
    }
}
