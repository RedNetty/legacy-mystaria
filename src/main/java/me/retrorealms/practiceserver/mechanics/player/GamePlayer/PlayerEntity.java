package me.retrorealms.practiceserver.mechanics.player.GamePlayer;

import me.retrorealms.practiceserver.apis.API;
import me.retrorealms.practiceserver.mechanics.market.ListedItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Khalid on 8/3/2017.
 */
public class PlayerEntity {

    private String uuid;
    private List<ListedItem> listedItems;

    public PlayerEntity(UUID uuid) {
        this.uuid = uuid.toString();
        listedItems = new ArrayList<>();
        API.getPlayerRegistry().addPlayer(this);
    }

    public String serialize() {
        return API.getGson().toJson(this);
    }

    public Player bukkitPlayer() {
        return Bukkit.getPlayer(UUID.fromString(uuid));
    }

    public String getUuid() {
        return uuid;
    }

    public List<ListedItem> getListedItems() {
        return listedItems;
    }
}
