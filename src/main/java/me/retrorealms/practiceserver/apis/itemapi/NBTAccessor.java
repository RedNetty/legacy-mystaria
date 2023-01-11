package me.retrorealms.practiceserver.apis.itemapi;

import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Giovanni on 11-4-2017.
 */
public class NBTAccessor {

    private net.minecraft.server.v1_9_R2.ItemStack itemStack;

    public NBTAccessor(ItemStack itemStack) {
        this.itemStack = CraftItemStack.asNMSCopy(itemStack);
    }

    /**
     * Check if the itemstack has an NBTTagCompound and creates a new one if not.
     * <p>
     * Must be called before usage!
     *
     * @return The future NBTAccesor.
     */
    public NBTAccessor check() {
        if (this.itemStack.hasTag()) return this;

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        this.itemStack.setTag(nbtTagCompound);

        return this;

    }

    public void remove(String key) {
        this.itemStack.getTag().remove(key);
    }

    /**
     * Check if the itemstack has an NBTTagCompound.
     *
     * @return Does it?
     */
    public boolean hasTag() {
        return this.itemStack.hasTag();
    }

    /**
     * Check whether the NBTTagCompound of the itemstack has a specific key.
     *
     * @param key The key to check.
     * @return Probably I guess?
     */
    public boolean hasKey(String key) {
        return this.itemStack.getTag().hasKey(key);
    }

    /**
     * Check whether the NBTTagCompound of the itemstack has a specific value at a specific key.
     *
     * @param key The key to check.
     * @return Probably I guess?
     */
    public boolean hasValue(String key, Object value) {
        if (!this.hasKey(key)) return false;

        return this.itemStack.getTag().get(key) == value;
    }

    /**
     * Set an NBT String at a key.
     *
     * @param key   The key.
     * @param value The value.
     * @return New NBTAccessor.
     */
    public NBTAccessor setString(String key, String value) {
        this.itemStack.getTag().setString(key, value);

        return this;
    }

    /**
     * Set an NBT Integer at a key.
     *
     * @param key   The key.
     * @param value The value.
     * @return New NBTAccessor.
     */
    public NBTAccessor setInt(String key, int value) {
        this.itemStack.getTag().setInt(key, value);

        return this;
    }

    /**
     * Set an NBT Double at a key.
     *
     * @param key   The key.
     * @param value The value.
     * @return New NBTAccessor.
     */
    public NBTAccessor setDouble(String key, double value) {
        this.itemStack.getTag().setDouble(key, value);

        return this;
    }

    /**
     * Get a String value from a key.
     *
     * @param key The key.
     * @return The value.
     */
    public String getString(String key) {
        return this.itemStack.getTag().getString(key);
    }

    /**
     * Get an Integer value from a key.
     *
     * @param key The key.
     * @return The value.
     */
    public int getInteger(String key) {
        return this.itemStack.getTag().getInt(key);
    }

    /**
     * Updates the itemstack and applies all changes.
     *
     * @return The new itemstack.
     */
    public ItemStack update() {
        return CraftItemStack.asBukkitCopy(this.itemStack);
    }
}
