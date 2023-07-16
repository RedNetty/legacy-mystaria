package me.retrorealms.practiceserver.mechanics.loot.contract.types;

import me.retrorealms.practiceserver.mechanics.loot.contract.Contract;
import me.retrorealms.practiceserver.mechanics.loot.contract.ContractRewardType;
import org.bukkit.ChatColor;

public class MobContract extends Contract {
    private final int requiredKills;
    private int currentKills;
    public MobContract(int requiredKills, int rarity, int tier,  ContractRewardType type) {
        super(tier, rarity, type);
        this.requiredKills = requiredKills;
    }

    public int getCurrentKills() {
        return currentKills;
    }

    public int getRequiredKills() {
        return requiredKills;
    }

    public void addKill() {
        this.currentKills += 1;
    }
    @Override
    public String getConditions() {
        return "Kill " + requiredKills + " T" + getTier() + " monsters.";
    }

    @Override
    public String getTypeString() {
        return "Mob Hunter " + ChatColor.GRAY + "(" + currentKills + "/"  + requiredKills + ")";
    }

}
