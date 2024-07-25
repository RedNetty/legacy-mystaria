package me.retrorealms.practiceserver.mechanics.player;

public class PersistentPlayer {
    public int tokens;
    public int mount;
    public int pickaxe;
    public int farmer;
    public int laststand;
    public int bankpages;
    public int orbrolls;
    public int luck;
    public int reaper;
    public int kitweapon;
    public int kithelm;
    public int kitchest;
    public int kitlegs;
    public int kitboots;
    public String currentQuest;
    public int dailyQuestsCompleted;

    public PersistentPlayer(int tokens, int mount, int pickaxe, int farmer, int laststand, int bankpages, int orbrolls, int luck, int reaper, int kitweapon, int kithelm, int kitchest, int kitlegs, int kitboots, int dailyQuestsCompleted, String currentQuest) {
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
        this.dailyQuestsCompleted = dailyQuestsCompleted;
        this.currentQuest = currentQuest;
    }

    public int getLevel(String perk) {
        switch (perk) {
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
            default:
                return 0;
        }
    }

    public void setLevel(String perk, int level) {
        switch (perk) {
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
        }
    }
}