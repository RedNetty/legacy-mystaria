package me.retrorealms.practiceserver.mechanics.loot.contract;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;

public class ContractHandler implements Listener {
    private static HashMap<Player, List<Contract>> contractMap = new HashMap<>();

    public void onStart() {

    }

    public void onStop() {

    }

    public static HashMap<Player, List<Contract>> getContractMap() {
        return contractMap;
    }
}
