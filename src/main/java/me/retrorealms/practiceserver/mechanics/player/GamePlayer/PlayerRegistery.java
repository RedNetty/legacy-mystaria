package me.retrorealms.practiceserver.mechanics.player.GamePlayer;

import com.google.gson.reflect.TypeToken;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.API;
import me.retrorealms.practiceserver.manager.Manager;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Khalid on 8/3/2017.
 */
public class PlayerRegistery extends Manager {

    private List<PlayerEntity> players = new ArrayList<>();
    private CopyOnWriteArrayList<String> serializedPlayers = new CopyOnWriteArrayList<>();

    @Override
    public void onEnable() {
        players = loadPlayers();
        if (players == null)
            players = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        serialize();
    }

    public CopyOnWriteArrayList<PlayerEntity> loadPlayers() {
        if (!PracticeServer.getMarketData().getFile().exists()) return new CopyOnWriteArrayList<>();

        CopyOnWriteArrayList<PlayerEntity> list = new CopyOnWriteArrayList<>();
        try {
            FileReader reader = new FileReader(PracticeServer.getPlayerData().getFile());
            BufferedReader bufferedReader = new BufferedReader(reader);
            List<String> strings = API.getGson().fromJson(bufferedReader, new TypeToken<List<String>>() {
            }.getType());
            if (strings != null) {
                strings.forEach(s -> list.add(API.getGson().fromJson(s, PlayerEntity.class)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void serialize() {
        players.forEach(b -> serializedPlayers.add(b.serialize()));
        String s = API.getGson().toJson(serializedPlayers);
        FileWriter writer;
        try {
            writer = new FileWriter(PracticeServer.getPlayerData().getFile());
            writer.write(s);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerEntity request(Player p) {
        Optional<PlayerEntity> o = players.stream().filter(playerEntity -> playerEntity.getUuid().equals(p.getUniqueId().toString())).findAny();
        if (o.isPresent())
            return o.get();
        return null;
    }

    public void addPlayer(PlayerEntity p) {
        players.add(p);
    }

}
