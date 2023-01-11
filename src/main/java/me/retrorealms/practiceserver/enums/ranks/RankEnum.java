package me.retrorealms.practiceserver.enums.ranks;

import lombok.Getter;

/**
 * Created by Jaxon on 8/13/2017.
 */
public enum RankEnum {
    DEFAULT(""),
    SUB("&a&lS"),
    SUB1("&6&lS+"),
    SUB2("&3&lS++"),
    SUB3("&3&lS++"),
    SUPPORTER("&d&lSUPPORTER"),
    QUALITY("&5&lQA"),
    BUILDER("&b&lBUILDER"),
    YOUTUBER("&c&lYT"),
    PMOD("&f&lPMOD"),
    GM("&b&lGM"),
    MANAGER("&e&lMANAGER"),
    DEV("&c&lDEV");

    @Getter
    public String tag;

    private RankEnum(String tag) {
        this.tag = tag;
    }


    public static String enumToString(RankEnum rankEnum) {
        switch (rankEnum) {
            case DEFAULT:
                return "default";
            case SUB:
                return "sub";
            case SUB1:
                return "sub+";
            case SUB2:
                return "sub++";
            case SUB3:
                return "sub+++";
            case SUPPORTER:
                return "supporter";
            case QUALITY:
                return "quality";
            case BUILDER:
                return "builder";
            case YOUTUBER:
                return "youtuber";
            case PMOD:
                return "pmod";
            case GM:
                return "gm";
            case MANAGER:
                return "manager";
            case DEV:
                return "dev";
            default:
                return "default";
        }
    }

    public static RankEnum fromString(String rank) {
        switch (rank.toLowerCase()) {
            case "default":
                return DEFAULT;
            case "sub":
                return SUB;
            case "sub+":
                return SUB1;
            case "sub++":
                return SUB2;
            case "sub+++":
                return SUB3;
            case "supporter":
                return SUPPORTER;
            case "youtuber":
                return YOUTUBER;
            case "quality":
                return QUALITY;
            case "builder":
                return BUILDER;
            case "pmod":
                return PMOD;
            case "gm":
                return GM;
            case "manager":
                return MANAGER;
            case "dev":
                return DEV;
            default:
                return DEFAULT;
        }
    }


}
