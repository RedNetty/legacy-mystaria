package me.retrorealms.practiceserver.mechanics.pets.utils;

import me.retrorealms.practiceserver.apis.nbt.NBTAccessor;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.pets.base.PetType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;

/**
 * Created by Khalid on 8/8/2017.
 */
public class PetGUI {

    public static void openGUI(Player p) {
        Inventory inventory = Bukkit.createInventory(null, 18, ChatColor.GOLD + "Pets");
        int counter = 0;
        for (PetType petType : PetType.values()) {
            if (ModerationMechanics.getRank(p) == RankEnum.SUB && counter >= 1) {
                break;
            } else if (ModerationMechanics.getRank(p) == RankEnum.SUB1 && counter >= 3) {
                break;
            } else if ((ModerationMechanics.getRank(p) == RankEnum.SUB2 || ModerationMechanics.getRank(p) == RankEnum.PMOD) && counter >= 6) {
                break;
            }

            SpawnEgg spawnEgg = new SpawnEgg();
            spawnEgg.setSpawnedType(petType.getType());


            ItemStack item = new ItemStack(Material.MONSTER_EGG, 1);
            net.minecraft.server.v1_9_R2.ItemStack stack = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tagCompound = stack.getTag();
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
            }
            NBTTagCompound id = new NBTTagCompound();
            id.setString("id", petType.getType().getName());
            tagCompound.set("EntityTag", id);
            stack.setTag(tagCompound);

            ItemStack egg = CraftItemStack.asBukkitCopy(stack);

            ItemMeta meta = egg.getItemMeta();
            String s = petType.toString();
            if (petType == PetType.BSHEEP) {
                s = "Baby Sheep";
            } else if (petType == PetType.PIG_ZOMBIE) {
                s = "Baby Pig Zombie";
            } else if (petType == PetType.ZOMBIE) {
                s = "Baby Zombie";
            } else if (petType == PetType.RSHEEP) {
                s = "Rainbow Sheep";
            } else {
                s = s.toLowerCase();
                s = s.replaceAll("_", " ");
                s = s.substring(0, 1).toUpperCase() + s.substring(1);
            }
            meta.setDisplayName(ChatColor.GOLD + s + " Pet");
            egg.setItemMeta(meta);

            NBTAccessor accessor = new NBTAccessor(egg).check();
            accessor.setString("pet", petType.getType().toString());

            inventory.addItem(accessor.update());
            counter++;
        }
        ItemStack delete = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData());
        ItemMeta meta = delete.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Remove Pet");
        delete.setItemMeta(meta);

        inventory.setItem(17, delete);
        p.openInventory(inventory);
    }


}
