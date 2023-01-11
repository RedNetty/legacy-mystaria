package me.retrorealms.practiceserver.utils.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Created by Jaxson on 8-7-2017.
 */
public class ItemGenerator {


    public List<String> lore;
    public String name;
    public Material material;
    public ItemStack item = null;
    public int amount = 1;
    public short durability;
    public boolean shiny = false;


    /*Allows you to create a new ItemStack*/
    public ItemGenerator(Material material) {
        this.material = material;
    }

    /*Allows you to edit a already existing ItemStack*/
    public ItemGenerator(ItemStack itemStack) {
        this.item = itemStack;
    }

    /* Allows you to set a list as the lore*/
    public ItemGenerator setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }


    /*Sets the Item of the Name*/
    public ItemGenerator setName(String name) {
        this.name = name;
        return this;
    }


    /*Adds a fake enchant to the item to make it shiny*/
    public ItemGenerator setShiny() {
        this.shiny = true;
        return this;
    }


    /*Allows you to change the amount that the ItemStack Returns*/
    public ItemGenerator setAmount(int amount) {
        this.amount = amount;
        return this;
    }


    /*Allows a player to set the durability of the item*/
    public ItemGenerator setDurability(short durability) {
        this.durability = durability;
        return this;
    }

    /*
    * Builds the Item and adds everything that is chosen.
    * THIS IS NEEDED TO RETURN AS ITEMSTACK
    * */
    public ItemStack build() {
        ItemStack itemStack = item != null ? item : new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        if (shiny) {
            itemStack.addUnsafeEnchantment(Glowing.enchant, 10);
        }
        itemStack.setAmount(amount);
        itemStack.setDurability(durability);
        return itemStack;
    }
}