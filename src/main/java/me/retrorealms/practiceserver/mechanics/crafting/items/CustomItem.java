package me.retrorealms.practiceserver.mechanics.crafting.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
@Getter
@Setter
public abstract class CustomItem {
    private final String id;
    private final String name;
    private final Material material;
    private List<String> lore;
    private ActivationType activationType;
    private double rarity = 0.5;

    public enum ActivationType {
        RIGHT_CLICK,
        INVENTORY,
        NONE
    }

    public CustomItem(String id, String name, Material material, double rarity, ActivationType activationType) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.activationType = activationType;
        this.rarity = rarity;
    }

    public CustomItem(String id, String name, Material material, double rarity, List<String> lore, ActivationType activationType) {
        this(id, name, material, rarity, activationType);
        this.lore = lore;
    }

    public abstract void applyEffects(Player player);

    public void onRightClick(PlayerInteractEvent event) {
        if (activationType == ActivationType.RIGHT_CLICK &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            applyEffects(event.getPlayer());
        }
    }

    public void onInventoryTick(Player player) {
        if (activationType == ActivationType.INVENTORY) {
            applyEffects(player);
        }
    }
}