package me.retrorealms.practiceserver.mechanics.loot.contract.types;

import me.retrorealms.practiceserver.mechanics.loot.contract.Contract;
import me.retrorealms.practiceserver.mechanics.loot.contract.ContractRewardType;
import org.bukkit.ChatColor;

public class LootContract extends Contract {

    private final int chestRequired;
    private int currentChestsOpened;
    public LootContract(int chestRequired, int rarity, int tier, ContractRewardType type) {
        super(tier,rarity, type);
        this.chestRequired = chestRequired;
    }

    public int getChestRequired() {
        return chestRequired;
    }

    public int getCurrentChestsOpened() {
        return currentChestsOpened;
    }

    public void addChest() {
        currentChestsOpened += 1;
    }

    @Override
    public String getConditions() {
        return "Open " + chestRequired + " T" + getTier() + " loot chests.";
    }

    @Override
    public String getTypeString() {
        return "Chest Hunter " + ChatColor.GRAY + "(" + currentChestsOpened + "/" + chestRequired + ")";
    }
}
