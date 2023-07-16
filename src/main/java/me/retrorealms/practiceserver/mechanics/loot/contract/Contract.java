package me.retrorealms.practiceserver.mechanics.loot.contract;

import org.bukkit.entity.Player;

public class Contract {

    private boolean accepted;
    private boolean complete;
    private final ContractRewardType rewardType;
    private final int tier;
    private final int rarity;
    public Contract(int tier, int rarity, ContractRewardType type) {
        complete = false;
        accepted = false;
        this.rarity = rarity;
        this.tier = tier;
        this.rewardType = type;
    }

    public int getTier() {
        return tier;
    }

    public int getRarity() {
        return rarity;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted() {
        this.accepted = true;
    }


    public ContractRewardType getRewardType() {
        return rewardType;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isComplete() {
        return complete;
    }
    public String getTypeString() {
        return "Default";
    }
    public String getConditions() {
        return "L Contract";
    }
}
