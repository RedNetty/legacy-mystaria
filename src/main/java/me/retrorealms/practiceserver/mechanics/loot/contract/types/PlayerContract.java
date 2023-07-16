package me.retrorealms.practiceserver.mechanics.loot.contract.types;

import me.retrorealms.practiceserver.mechanics.loot.contract.Contract;
import me.retrorealms.practiceserver.mechanics.loot.contract.ContractRewardType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerContract extends Contract {

    private Player target;
    private int killsRequired;
    private int currentKills;
    public PlayerContract(int killsRequired, int rarity, int tier, ContractRewardType type) {
        super(tier, rarity, type);
        this.killsRequired = killsRequired;
    }

    public PlayerContract(Player target, int rarity, int tier, ContractRewardType type) {
        super(tier,rarity, type);
        this.target = target;
    }

    public int getCurrentKills() {
        return currentKills;
    }

    public Player getTarget() {
        return target;
    }

    public int getKillsRequired() {
        return killsRequired;
    }

    public void addKill() {currentKills += 1;}

    @Override
    public String getConditions() {
        return target != null ? "Killing the player " + target.getName() : "Kill " + killsRequired + " total players." ;
    }

    @Override
    public String getTypeString() {
        return "Player Hunter " + ChatColor.GRAY + "(" +currentKills + "/" + killsRequired + ")";
    }
}
