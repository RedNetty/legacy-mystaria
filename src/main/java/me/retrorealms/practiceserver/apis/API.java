package me.retrorealms.practiceserver.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.market.GlobalMarket;
import me.retrorealms.practiceserver.mechanics.pets.handlers.RainbowSheepTask;
import me.retrorealms.practiceserver.mechanics.player.GamePlayer.PlayerRegistery;

/**
 * Created by Khalid on 8/3/2017.
 */
public class API {

    private static final PlayerRegistery playerRegistry = PracticeServer.getManagerHandler().getPlayerRegistery();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final GlobalMarket globalMarket = PracticeServer.getManagerHandler().getGlobalMarket();
    private static final RainbowSheepTask rainbowSheepTask = RainbowSheepTask.getInstance();

    public static PlayerRegistery getPlayerRegistry() {
        return playerRegistry;
    }

    public static Gson getGson() {
        return gson;
    }

    public static GlobalMarket getGlobalMarket() {
        return globalMarket;
    }

    public static RainbowSheepTask getRainbowSheepTask() {
        return rainbowSheepTask;
    }
}
