package me.retrorealms.practiceserver.enums.chat;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by Giovanni on 3-5-2017.
 */
public enum ChatTag {

    DEFAULT("$none", false),
    SHOOTER("&c&lSHOOTER&r", true),
    NUTS("&b&lNUTS&r", true),
    PIMP("&b&lPIMP&r", true),
    MEMER("&9&lM&9E&9&lM&9E&9&lR&r", true),
    BASED("&eBASED&r", true),
    THINKING("&6&l:THINKING:", true),
    SILVER("&7&lSILVER", true),
    GAY("&d&lG&e&lA&a&lY", true),
    THOT("&d&lTHOT", true),
    CODER("&3&lCODER", true),
    DOG("&3&lDOG-WATER", true),
    HACKER("&9&lHACKER", true),
    DADDY("&b&lD&ba&b&lD&bd&b&lY", true),
    SHERIFF("&6✪", true),
    GREASY("&e&lG&6&lR&e&lE&6&lA&e&lS&6&lY", true),
    BOZO("&a&lB&e&lO&3&lZ&a&lO", true),
    YikesYouDied("&d&lYikesYouDied", true),
    MOD("&cM&9O&eD", true);

    private String tag;
    private boolean storeInstance;

    ChatTag(String tag, boolean storeInstance) {
        this.tag = tag;
        this.storeInstance = storeInstance;
    }

    public String getTag() {
        return tag;
    }

    public boolean isStoreInstance() {
        return storeInstance;
    }

    public static Stream<ChatTag> stream() {
        return Arrays.stream(values());
    }
}
