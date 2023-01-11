package me.retrorealms.practiceserver.manager;

import me.retrorealms.practiceserver.mechanics.market.GlobalMarket;
import me.retrorealms.practiceserver.mechanics.pets.handlers.PetManager;
import me.retrorealms.practiceserver.mechanics.player.GamePlayer.PlayerRegistery;
import me.retrorealms.practiceserver.mechanics.vendors.Halloween;

import java.util.stream.Stream;

/**
 * Created by Khalid on 8/3/2017.
 */
public class ManagerHandler {

    private SampleManager sampleManager;
    private SampleManager2 sampleManager2;
    private GlobalMarket globalMarket;
    private PlayerRegistery playerRegistery;
    private Halloween halloween; // like is there an error?dfq it wasnt added on floobits then or it glitched not for me.
    private PetManager petManager;

    public void onEnable() {
        Stream.of(
                globalMarket = new GlobalMarket(),
                playerRegistery = new PlayerRegistery(),
                sampleManager = new SampleManager(),
                sampleManager2 = new SampleManager2(),
                halloween = new Halloween(),
                petManager = new PetManager()
        ).forEach(manager -> {
            manager.onEnable();
        });
    }

    public void onDisable() {
        Stream.of(
                globalMarket,
                playerRegistery,
                sampleManager,
                halloween,
                sampleManager2
//                petManager
        ).forEach(manager -> {
            manager.onDisable();
        });
    }

    // you can refrence managers by using getters


    public PetManager getPetManager() {
        return petManager;
    }

    public Halloween getHalloween() {
        return halloween;
    }

    public SampleManager getSampleManager() {
        return sampleManager;
    }

    public SampleManager2 getSampleManager2() {
        return sampleManager2;
    }

    public GlobalMarket getGlobalMarket() {
        return globalMarket;
    }

    public PlayerRegistery getPlayerRegistery() {
        return playerRegistery;
    }
}
