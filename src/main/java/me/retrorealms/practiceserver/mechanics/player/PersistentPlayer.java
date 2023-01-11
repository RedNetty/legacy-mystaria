package me.retrorealms.practiceserver.mechanics.player;

public class PersistentPlayer {

    public int tokens; //currency
    public int bankpages; //this is for later
    public int mount; // starting mount tier
    public int pickaxe; // pickaxes you buy start at x level
    public int farmer; // damage to mobs increased by x%
    public int laststand; // while under 30% hp, deal x% more damage
    public int orbrolls; // adds stat rolls to legendary orbs
    public int luck; // increases success rate of enchants and altars
    public int reaper;
    public int kitweapon;
    public int kithelm;
    public int kitchest;
    public int kitlegs;
    public int kitboots;

    public PersistentPlayer(int tokens, int mount, int pickaxe, int farmer, int laststand,
                            int bankpages, int orbrolls, int luck, int reaper, int kitweapon, int kithelm, int kitchest, int kitlegs, int kitboots){
        this.tokens = tokens;
        this.mount = mount;
        this.pickaxe = pickaxe;
        this.farmer = farmer;
        this.laststand = laststand;
        this.bankpages = bankpages;
        this.orbrolls = orbrolls;
        this.luck = luck;
        this.reaper = reaper;
        this.kitweapon = kitweapon;
        this.kithelm = kithelm;
        this.kitchest = kitchest;
        this.kitlegs = kitlegs;
        this.kitboots = kitboots;
    }

    public int getLevel(String perk){
        switch(perk){
            case "Reset":
                return 1;
            case "Mount":
                return mount;
            case "BankPages":
                return bankpages;
            case "Pickaxe":
                return pickaxe;
            case "Farmer":
                return farmer;
            case "LastStand":
                return laststand;
            case "OrbRolls":
                return orbrolls;
            case "Luck":
                return luck;
            case "Reaper":
                return reaper;
            case "KitWeapon":
                return kitweapon;
            case "KitHelm":
                return kithelm;
            case "KitChest":
                return kitchest;
            case "KitLegs":
                return kitlegs;
            case "KitBoots":
                return kitboots;
            default: return 0;
        }
    }

    public void setLevel(String perk, int level){
        switch(perk){
            case "Mount":
                mount = level;
                break;
            case "BankPages":
                bankpages = level;
                break;
            case "Pickaxe":
                pickaxe = level;
                break;
            case "Farmer":
                farmer = level;
                break;
            case "LastStand":
                laststand = level;
                break;
            case "OrbRolls":
                orbrolls = level;
                break;
            case "Luck":
                luck = level;
                break;
            case "Reaper":
                reaper = level;
                break;
            case "KitWeapon":
                kitweapon = level;
                break;
            case "KitHelm":
                kithelm = level;
                break;
            case "KitChest":
                kitchest = level;
                break;
            case "KitLegs":
                kitlegs = level;
                break;
            case "KitBoots":
                kitboots = level;
                break;
            default: break;
        }
    }
}