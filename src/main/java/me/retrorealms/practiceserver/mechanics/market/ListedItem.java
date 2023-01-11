package me.retrorealms.practiceserver.mechanics.market;

import me.retrorealms.practiceserver.apis.API;
import me.retrorealms.practiceserver.apis.itemapi.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Khalid on 7/12/2017.
 */
public class ListedItem {

    private UUID itemId;
    private transient ItemStack itemStack;
    private int price;
    private UUID owner;

    private String itemData;

    public ListedItem init() {
        itemStack = ItemSerializer.itemStackFromBase64(itemData);
        return this;
    }
    public UUID getOwner(){
        return owner;
    }

    public UUID getItemId(){ return itemId; }

    public int getPrice(){
        return price;
    }
    public ListedItem(UUID itemId, ItemStack itemStack, int price, UUID owner, String itemData){
        this.itemId = itemId;
        this.itemStack = itemStack;
        this.price = price;
        this.owner = owner;
        this.itemData = itemData;
    }

    public ItemStack getItemStack() {
        itemStack = ItemSerializer.itemStackFromBase64(itemData);
        return itemStack;
    }

    public String serializeItem() {
        return API.getGson().toJson(this);
    }

}
