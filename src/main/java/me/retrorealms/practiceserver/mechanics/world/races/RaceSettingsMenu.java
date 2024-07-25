package me.retrorealms.practiceserver.mechanics.world.races;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.world.MinigameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RaceSettingsMenu implements Listener {
    private static final String MENU_TITLE = ChatColor.GOLD + "Race Settings";
    private static final RaceMinigame raceMinigame = PracticeServer.getRaceMinigame();
    private static final Map<Player, SettingInput> playerInputMap = new HashMap<>();

    public RaceSettingsMenu() {
    }

    public void register(){        Bukkit.getPluginManager().registerEvents(this, PracticeServer.getInstance());}

    public static void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 45, MENU_TITLE);

        for (MenuItem item : MenuItem.values()) {
            inventory.setItem(item.getSlot(), item.createItem(raceMinigame));
        }

        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        MenuItem clickedItem = MenuItem.getBySlot(slot);
        if (clickedItem != null) {
            player.closeInventory();  // Close the inventory first
            Bukkit.getScheduler().runTaskLater(PracticeServer.getInstance(), () -> {
                clickedItem.getAction().accept(player);
            }, 1L);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;
        Player player = (Player) event.getPlayer();
        if (!playerInputMap.containsKey(player)) {
            playerInputMap.remove(player);
        }
    }


    private enum MenuItem {
        LOBBY_TIME(10, Material.COMPASS, "&e&lLobby Time",
                (player) -> promptForInput(player, "lobbyTime", "Enter new lobby time in seconds:")),
        PREP_TIME(11, Material.IRON_SWORD, "&e&lPreparation Time",
                (player) -> promptForInput(player, "prepTime", "Enter new preparation time in seconds:")),
        SHRINK_TIME(12, Material.BARRIER, "&e&lShrink Time",
                (player) -> promptForInput(player, "shrinkTime", "Enter new shrink time in seconds:")),
        TEAM_SIZE(13, Material.SKULL_ITEM, "&e&lMax Team Size",
                (player) -> promptForInput(player, "teamSize", "Enter new max team size:")),
        INCURSION_MODE(19, Material.BLAZE_POWDER, "&e&lIncursion Mode",
                (player) -> {
                    boolean newState = !raceMinigame.isIncursionMode();
                    raceMinigame.setIncursionMode(newState);
                    player.sendMessage(ChatColor.GREEN + "Incursion mode has been " + (newState ? "enabled" : "disabled"));
                    if (newState) {
                        player.sendMessage(ChatColor.YELLOW + "Lightning mobs will spawn with a max " +
                                raceMinigame.getIncursionHealthMultiplier() + "x Multipliers!");
                    }
                    player.closeInventory();
                    Bukkit.getScheduler().runTaskLater(PracticeServer.getInstance(), () -> openMenu(player), 1L);
                }),
        INCURSION_SPAWN_RATE(20, Material.DIAMOND, "&e&lIncursion Spawn Rate",
                (player) -> promptForInput(player, "incursionSpawnRate", "Enter new incursion spawn rate (0.0 - 1.0):")),
        INCURSION_HEALTH_MULTIPLIER(21, Material.GOLDEN_APPLE, "&e&lIncursion Health Multiplier",
                (player) -> promptForInput(player, "incursionHealthMultiplier", "Enter new incursion health multiplier:")),
        START_RACE(40, Material.EMERALD_BLOCK, "&a&lStart Race",
                (player) -> {
                    if (raceMinigame.getGameState() == MinigameState.NONE) {
                        raceMinigame.startLobby();
                        player.sendMessage(ChatColor.GREEN + "Race has been started!");
                        player.closeInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "A race is already in progress!");
                    }
                }),
        CANCEL_RACE(41, Material.REDSTONE_BLOCK, "&c&lCancel Race",
                (player) -> {
                    if (raceMinigame.getGameState() != MinigameState.NONE) {
                        raceMinigame.cancelGame();
                        player.sendMessage(ChatColor.GREEN + "Race has been cancelled!");
                        player.closeInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "There is no race in progress!");
                    }
                }),
        CLOSE(44, Material.BARRIER, "&c&lClose", Player::closeInventory);

        private final int slot;
        private final Material material;
        private final String name;
        private final Consumer<Player> action;

        MenuItem(int slot, Material material, String name, Consumer<Player> action) {
            this.slot = slot;
            this.material = material;
            this.name = name;
            this.action = action;
        }

        public int getSlot() {
            return slot;
        }

        public Consumer<Player> getAction() {
            return action;
        }

        public boolean requiresRefresh() {
            return this != CLOSE && this != START_RACE && this != CANCEL_RACE && this != INCURSION_MODE;
        }

        public ItemStack createItem(RaceMinigame raceMinigame) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            String value = "";
            switch (this) {
                case LOBBY_TIME: value = raceMinigame.getLobbyTime() + "s"; break;
                case PREP_TIME: value = raceMinigame.getPreparationTime() + "s"; break;
                case SHRINK_TIME: value = raceMinigame.getShrinkTime() + "s"; break;
                case TEAM_SIZE: value = String.valueOf(raceMinigame.getMaxTeamSize()); break;
                case INCURSION_MODE: value = raceMinigame.isIncursionMode() ? "Enabled" : "Disabled"; break;
                case INCURSION_SPAWN_RATE: value = String.valueOf(raceMinigame.getIncursionSpawnRate()); break;
                case INCURSION_HEALTH_MULTIPLIER: value = String.valueOf(raceMinigame.getIncursionHealthMultiplier()); break;
            }

            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Current: " + ChatColor.WHITE + value,
                    ChatColor.YELLOW + "Click to change"
            ));

            item.setItemMeta(meta);
            return item;
        }

        public static MenuItem getBySlot(int slot) {
            for (MenuItem item : values()) {
                if (item.getSlot() == slot) {
                    return item;
                }
            }
            return null;
        }
    }

    private static void promptForInput(Player player, String setting, String prompt) {
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(PracticeServer.getInstance(), () -> {
            player.sendMessage(ChatColor.YELLOW + prompt);
            playerInputMap.put(player, new SettingInput(setting, value -> {
                updateSetting(setting, value);
                player.sendMessage(ChatColor.GREEN + "Value updated. Opening menu...");
                Bukkit.getScheduler().runTaskLater(PracticeServer.getInstance(), () -> openMenu(player), 10L);
            }));
            PracticeServer.getInstance().getLogger().info("Prompting player " + player.getName() + " for input: " + setting);
        }, 1L);
    }

    static void updateSetting(String setting, double value) {
        PracticeServer.getInstance().getLogger().info("Updating setting: " + setting + " to value: " + value);
        switch(setting) {
            case "lobbyTime": raceMinigame.setLobbyTime((int)value); break;
            case "prepTime": raceMinigame.setPreparationTime((int)value); break;
            case "shrinkTime": raceMinigame.setShrinkTime((int)value); break;
            case "teamSize": raceMinigame.setMaxTeamSize((int)value); break;
            case "incursionSpawnRate": raceMinigame.setIncursionSpawnRate(value); break;
            case "incursionHealthMultiplier": raceMinigame.setIncursionHealthMultiplier(value); break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        SettingInput input = playerInputMap.get(player);
        if (input != null) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(PracticeServer.getInstance(), () -> {
                try {
                    double value = Double.parseDouble(event.getMessage());
                    input.getCallback().accept(value);
                    PracticeServer.getInstance().getLogger().info("Player " + player.getName() + " input value: " + value + " for setting: " + input.getSetting());
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid input. Please enter a number.");
                    PracticeServer.getInstance().getLogger().warning("Player " + player.getName() + " entered invalid input: " + event.getMessage());
                }
                playerInputMap.remove(player);
            });
        }
    }

    private static class SettingInput {
        private final String setting;
        private final Consumer<Double> callback;

        public SettingInput(String setting, Consumer<Double> callback) {
            this.setting = setting;
            this.callback = callback;
        }

        public String getSetting() {
            return setting;
        }

        public Consumer<Double> getCallback() {
            return callback;
        }
    }
}