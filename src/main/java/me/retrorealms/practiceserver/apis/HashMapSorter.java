package me.retrorealms.practiceserver.apis;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HashMapSorter {
    public static ArrayList<Map.Entry<Player, Integer>> sortTopPlayers(HashMap<UUID, Integer> map) {
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<UUID, Integer>>() {
            @Override
            public int compare(Map.Entry<UUID, Integer> o1, Map.Entry<UUID, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        ArrayList<Map.Entry<Player, Integer>> topPlayers = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (Bukkit.getPlayer(list.get(i).getKey()) != null) {
                Player player = Bukkit.getPlayer(list.get(i).getKey());
                Map.Entry<Player, Integer> entry = new AbstractMap.SimpleEntry<>(player, list.get(i).getValue());
                topPlayers.add(entry);
            }
        }

        return topPlayers;
    }
}