package me.retrorealms.practiceserver.mechanics.world.region;

import lombok.Getter;

/**
 * Created by Giovanni on 20-5-2017.
 */
public enum Region {

    MALTAI_CATHEDRAL("maltai_cathedral");

    @Getter
    private String regionId;

    Region(String id) {
        regionId = id;
    }
}
