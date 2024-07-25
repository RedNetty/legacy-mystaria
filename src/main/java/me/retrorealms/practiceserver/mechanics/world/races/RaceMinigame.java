package me.retrorealms.practiceserver.mechanics.world.races;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import lombok.Setter;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.actionbar.ActionBar;
import me.retrorealms.practiceserver.mechanics.donations.Crates.CratesMain;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.loot.LootChests;
import me.retrorealms.practiceserver.mechanics.money.Banks;
import me.retrorealms.practiceserver.mechanics.money.Economy.Economy;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import me.retrorealms.practiceserver.mechanics.vendors.NewMerchant;
import me.retrorealms.practiceserver.mechanics.world.MinigameState;
import me.retrorealms.practiceserver.mechanics.world.races.worldevents.ArtifactHunt;
import me.retrorealms.practiceserver.mechanics.world.races.worldevents.DynamicWorldEvents;
import me.retrorealms.practiceserver.mechanics.world.races.worldevents.ElementalStorm;
import me.retrorealms.practiceserver.mechanics.world.region.RegionHandler;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
@Getter
@Setter
public class RaceMinigame implements Listener {
    // made this sleep-deprived at 2am but with love
    // Map to store blocks and their original materials for restoration
    private final Map<Block, Material> blocksToReplace = new ConcurrentHashMap<>();
    private final List<UUID> playersTotal = new CopyOnWriteArrayList<>();
    private final List<UUID> playersLeft = new CopyOnWriteArrayList<>();
    private WorldBorder worldBorder;
    private Random random;
    private int preparationTime;
    private int lobbyTime;
    private int maxTeamSize;
    private int shrinkTime;
    private List<String> regionNames;
    private MinigameState gameState;
    private boolean incursionMode = false;
    private double incursionSpawnRate = 0.01;
    private double incursionHealthMultiplier = 15; // Default to 15x health
    private boolean elementalStormsEnabled = false;
    private boolean artifactHuntEnabled = false;
    private boolean dynamicEventsEnabled = false;
    private int eventInterval = 300; // 5 minutes
    /**
     * Called when the plugin is enabled.
     */
    public void onEnable() {
        random = new Random();
        preparationTime = 15; // Example: 15 minutes
        lobbyTime = 10;
        shrinkTime = 120; // Example: 120 seconds
        maxTeamSize = 3;

        regionNames = new ArrayList<>(); // Add your defined region names here
        gameState = MinigameState.NONE;

        regionNames.add("race");
        regionNames.add("race1");
        regionNames.add("race2");
        regionNames.add("race3");

        World world = Bukkit.getWorld("jew");
        worldBorder = world.getWorldBorder();

        resetBorder();

        // Damage players outside the border periodically
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    int zoneDamage = getZoneDamage(player);
                    if (isOutOfBounds(player)) {
                        if (gameState == MinigameState.LOBBY)
                            player.teleport(TeleportBooks.DeadPeaks);
                        player.damage(zoneDamage);
                        StringUtil.sendCenteredMessage(player, "&c-" + zoneDamage + "&cHP &7<- Zone Damage &a[" + player.getHealth() + "&a&lHP&a]");
                    }
                });
            }
        }.runTaskTimer(PracticeServer.getInstance(), 20L, 20L);

        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.getInstance());
    }


    /**
     * Initializes the player for the race mini-game.
     *
     * @param player The player to initialize.
     */
    public void initializePlayer(Player player) {
        if (player instanceof NPC || player.getUniqueId() == null)
            return;

        if (!playersTotal.contains(player.getUniqueId())) {
            player.closeInventory();

            if (!GuildPlayers.guildPlayerMap.containsKey(player.getUniqueId()))
                GuildPlayers.guildPlayerMap.put(player.getUniqueId(), new GuildPlayer(player.getUniqueId(), player.getName(), ""));

            // Updates the player's data before pausing
            SQLMain.updatePlayerStats(player);
            SQLMain.updatePersistentStats(player);

            // Deals with clearing normal wipe items temporarily
            if (!Horses.horseTier.containsKey(player)) Horses.horseTier.put(player, 3);
            Alignments.setLawful(player);

            player.getInventory().clear();

            Listeners.Kit(player);

            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, .0004F);
            player.teleport(TeleportBooks.DeadPeaks);

            playersTotal.add(player.getUniqueId());
            if (gameState != MinigameState.SHRINK)
                addPlayer(player);
        }
    }
    public double getIncursionHealthMultiplier() {
        return incursionHealthMultiplier;
    }

    public void setIncursionHealthMultiplier(double multiplier) {
        this.incursionHealthMultiplier = multiplier;
    }
    public boolean isIncursionMode() {
        return incursionMode;
    }

    public void setIncursionMode(boolean enabled) {
        this.incursionMode = enabled;
        // Do not broadcast any messages from here
    }


    /**
     * Eliminates the player from the race mini-game.
     *
     * @param player The player to eliminate.
     */
    public void eliminatePlayer(Player player) {
        synchronized (playersLeft) {
            if (!playersLeft.contains(player.getUniqueId()))
                return;

            playersLeft.removeIf(uuid -> Bukkit.getPlayer(uuid) == null || Bukkit.getPlayer(uuid).getGameMode() == GameMode.SPECTATOR || Bukkit.getPlayer(uuid).isDead() || !Bukkit.getPlayer(uuid).isOnline());
            Alignments.setLawful(player);
            player.getWorld().strikeLightningEffect(player.getLocation());
            removePlayer(player);
            if (player.getKiller() != null) {
                StringUtil.broadcastCentered("&c>>> " + player.getName() + " has been eliminated by " + player.getKiller().getName() + ".");
            } else {
                StringUtil.broadcastCentered("&c>>> " + player.getName() + " has been eliminated.");
            }
            StringUtil.broadcastCentered("&7" + playersLeft.size() + " player(s) remaining.");

            checkWinner();
        }
    }


    /**
     * Gets the team of a player. (Just uses parties, may change in future)
     *
     * @param uuid The UUID of the player.
     * @return The team of the player.
     */
    public List<Player> getTeam(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return Parties.getEntirePartyOf(player);
    }


    /**
     * Starts the lobby phase of the race mini-game.
     */
    public void startLobby() {
        Parties.clearParties();
        Bukkit.getServer().getWorld("jew").getEntities().forEach(entity -> {
            if (entity instanceof Item)
                entity.remove();
        });

        for (Player player : Bukkit.getOnlinePlayers()) {
            initializePlayer(player);
        }

        worldBorder.setWarningDistance(0);
        worldBorder.setCenter(TeleportBooks.DeadPeaks);
        worldBorder.setSize(45); // Setup Lobby Border Size
        gameState = MinigameState.LOBBY;
        phaseMessage();

        // Start the countdown timer for preparation
        resetPlayerData();

        new BukkitRunnable() {
            int secondsLeft = lobbyTime;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    if (gameState != MinigameState.LOBBY) {
                        cancel();
                    }

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        ActionBar.sendActionBar(onlinePlayer, "&7>> &eRACE LOBBY &c(" + secondsLeft + "s) &7<<", 1);
                    }

                    secondsLeft--;
                } else {
                    startPrep();
                    cancel(); // Stop the countdown timer
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L); // Run the timer every second (20 ticks)
    }

    public void cancelGame() {
        StringUtil.broadcastCentered("&e&l>> RACE HAS BEEN CANCELLED <<");
        endRace();
    }

    public void resetPlayerData() {
        LootChests.clearChestInventories();
        NewMerchant.clearTradeMap();
        Economy.clearEconomy();
        Parties.clearParties();
        Horses.clearHorses();

        Bukkit.getOnlinePlayers().forEach(player -> {
            Horses.horseTier.put(player, 3);
            player.getInventory().clear();

            Listeners.Kit(player);
        });
    }


    private void startPrep() {

        resetBorder(); // Restore Border To Open Game World
        Parties.giveEveryoneParty(); //Set up default parties
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, .0008F);
        });

        gameState = MinigameState.PREP;
        phaseMessage();
        resetPlayerData();
        // Get a random region name from the defined list
        String randomRegionName = regionNames.get(ThreadLocalRandom.current().nextInt(regionNames.size()));

        // Get the world and region associated with the random region name
        World world = Bukkit.getWorld("jew");
        RegionManager regionManager = WorldGuardPlugin.inst().getRegionManager(world);
        if (regionManager != null && regionManager.getRegion(randomRegionName) != null) {
            worldBorder.setCenter(getRandomLocation(world, Objects.requireNonNull(regionManager.getRegion(randomRegionName))));
        }
        new BukkitRunnable() {
            int secondsLeft = preparationTime;

            @Override
            public void run() {
                if (gameState != MinigameState.PREP) {
                    cancel();
                }
                if (secondsLeft > 0) {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        ActionBar.sendActionBar(onlinePlayer, "&7>> &eRACE PREP &c(" + secondsLeft + "s left) &7<<", 1);
                    }
                    secondsLeft--;
                } else {
                    startShrinking();
                    cancel(); // Stop the countdown timer
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L);
    }
    private Location getRandomLocationWithinBorder() {
        World world = Bukkit.getWorld("jew"); // Make sure this is the correct world name
        WorldGuardPlugin worldGuard = getWorldGuard();
        if (worldGuard == null) {
            PracticeServer.log.warning("WorldGuard not found, cannot get random location within region.");
            return world.getSpawnLocation(); // Fallback to spawn location
        }

        RegionManager regionManager = worldGuard.getRegionManager(world);
        List<ProtectedRegion> validRegions = new ArrayList<>();

        for (String regionName : regionNames) {
            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region != null) {
                validRegions.add(region);
            }
        }

        if (validRegions.isEmpty()) {
            PracticeServer.log.warning("No valid regions found for random location selection.");
            return world.getSpawnLocation(); // Fallback to spawn location
        }

        // Randomly select one of the valid regions
        ProtectedRegion selectedRegion = validRegions.get(new Random().nextInt(validRegions.size()));

        int attempts = 0;
        while (attempts < 50) { // Limit attempts to prevent infinite loop
            double x = selectedRegion.getMinimumPoint().getX() + Math.random() * (selectedRegion.getMaximumPoint().getX() - selectedRegion.getMinimumPoint().getX());
            double z = selectedRegion.getMinimumPoint().getZ() + Math.random() * (selectedRegion.getMaximumPoint().getZ() - selectedRegion.getMinimumPoint().getZ());
            int y = world.getHighestBlockYAt((int) x, (int) z);

            Location location = new Location(world, x, y, z);
            if (selectedRegion.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()) && isWithinWorldBorder(location)) {
                return location;
            }
            attempts++;
        }

        // If we couldn't find a suitable location, return the center of the selected region
        return new Location(world,
                (selectedRegion.getMaximumPoint().getX() + selectedRegion.getMinimumPoint().getX()) / 2,
                world.getHighestBlockYAt((int) ((selectedRegion.getMaximumPoint().getX() + selectedRegion.getMinimumPoint().getX()) / 2),
                        (int) ((selectedRegion.getMaximumPoint().getZ() + selectedRegion.getMinimumPoint().getZ()) / 2)),
                (selectedRegion.getMaximumPoint().getZ() + selectedRegion.getMinimumPoint().getZ()) / 2);
    }

    private boolean isWithinWorldBorder(Location location) {
        WorldBorder border = location.getWorld().getWorldBorder();
        double size = border.getSize() / 2;
        Location center = border.getCenter();
        double x = location.getX() - center.getX(), z = location.getZ() - center.getZ();
        return Math.abs(x) <= size && Math.abs(z) <= size;
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }
    public void startShrinking() {
        final int x = worldBorder.getCenter().getBlockX();
        final int z = worldBorder.getCenter().getBlockZ();

        Bukkit.getOnlinePlayers().forEach(player -> {

            if (Toggles.isToggled(player, "Anti PVP")) Toggles.changeToggle(player, "Anti PVP");
            Alignments.setChaotic(player, shrinkTime * 40);

            addPlayer(player);

            if (Parties.getParty(player) == null) Parties.createParty(player);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, .064F);
        });

        World world = Bukkit.getWorld("jew"); // Replace with your world name
        RegionHandler.switchPvPFlagForRegions(world);

        gameState = MinigameState.SHRINK;
        phaseMessage();
        createLocationBeacon(worldBorder.getCenter());
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    ActionBar.sendActionBar(onlinePlayer, "&7>> &eZONE SHRINKING &c(Center - X: " + x + " Z: " + z + ") &7<<", 1);
                }
                if (gameState != MinigameState.SHRINK) {
                    cancel();
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L);

        if (elementalStormsEnabled) {
            scheduleElementalStorms();
        }

        if (artifactHuntEnabled) {
            scheduleArtifactSpawns();
        }

        if (dynamicEventsEnabled) {
            scheduleDynamicEvents();
        }

        worldBorder.setDamageAmount(0);
        worldBorder.setSize(85, shrinkTime); // Shrink to the initial size in the specified time
        worldBorder.setWarningDistance(150);
    }
    private void scheduleElementalStorms() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != MinigameState.SHRINK) {
                    this.cancel();
                    return;
                }
                Location stormCenter = getRandomLocationWithinBorder();
                ElementalStorm.StormType type = ElementalStorm.StormType.values()[new Random().nextInt(ElementalStorm.StormType.values().length)];
                ElementalStorm.triggerElementalStorm(stormCenter, type);
            }
        }.runTaskTimer(PracticeServer.getInstance(), eventInterval * 20L, eventInterval * 20L);
    }
    private void scheduleArtifactSpawns() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != MinigameState.SHRINK) {
                    this.cancel();
                    return;
                }
                Location artifactLocation = getRandomLocationWithinBorder();
                ArtifactHunt.ArtifactType type = ArtifactHunt.ArtifactType.values()[new Random().nextInt(ArtifactHunt.ArtifactType.values().length)];
                ArtifactHunt.spawnArtifact(artifactLocation, type);
            }
        }.runTaskTimer(PracticeServer.getInstance(), eventInterval * 20L, eventInterval * 20L);
    }

    private void scheduleDynamicEvents() {

                DynamicWorldEvents.startEventScheduler();
    }
    public void clearItems() {
        Bukkit.getScheduler().runTaskAsynchronously(PracticeServer.getInstance(), () -> {
            World world = Bukkit.getWorld("jew");
            if (world != null) {
                world.getEntities().forEach(entity -> {
                    if (entity instanceof Item)
                        entity.remove();
                });
            }
        });
    }

    public void clearFlags(Player player) {
        if(Alignments.chaotic.containsKey(player.getName())) Alignments.setLawful(player);
        Alignments.tagged.remove(player.getName());
        Listeners.combat.remove(player.getName());
    }

    /**
     * Ends the race minigame.
     */
    public void endRace() {
        gameState = MinigameState.NONE;
        restoreBeaconBlocks();
        resetBorder();
        RegionHandler.restoreRegionFlags();
        clearItems();
        playersTotal.clear();
        playersLeft.clear();
        Bukkit.getOnlinePlayers().forEach(player -> {
            clearFlags(player);
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            Economy.clearEconomy();
            new BukkitRunnable() {
                @Override
                public void run() {
                    PracticeServer.getSQL().loadData(player);
                    Listeners.hpCheck(player);
                }
            }.runTaskLater(PracticeServer.getInstance(), 20L);
        });

        SQLMain.loadGems();
        NewMerchant.clearTradeMap();
        Banks.resetTempBanks();

    }

    /**
     * Sends a message indicating the current phase of the race minigame.
     */
    public void phaseMessage() {
        StringUtil.clearChat(null);
        switch (gameState) {
            case NONE:
            case LOBBY:
                StringUtil.broadcastCentered("&c&lA RACE HAS BEEN INITIALIZED");
                StringUtil.broadcastCentered("");
                StringUtil.broadcastCentered("");
                StringUtil.broadcastCentered("&e- THE MAX PARTY SIZE IS " + maxTeamSize + " -");
                break;
            case PREP:
                StringUtil.broadcastCentered("&c&lTHE PREP PHASE HAS STARTED");
                StringUtil.broadcastCentered("");
                StringUtil.broadcastCentered("&7Farm for gear, get as strong as you can");
                StringUtil.broadcastCentered("&7Once the time is up, the zone will shrink..");
                StringUtil.broadcastCentered("");
                StringUtil.broadcastCentered("&e- YOU HAVE " + (preparationTime / 60) + " MINUTE(s) TO FARM -");
                break;
            case SHRINK:
                StringUtil.broadcastCentered("&c&lTHE ZONE IS NOW SHRINKING");
                StringUtil.broadcastCentered("");
                StringUtil.broadcastCentered("&7Make your way to the center.");
                StringUtil.broadcastCentered("&7Leaving/Dying/PQuitting will result in Elimination.");
                StringUtil.broadcastCentered("");
                StringUtil.broadcastCentered("&e- THE CENTER IS LOCATED AT  (X: " + worldBorder.getCenter().getX() + " Z: " + worldBorder.getCenter().getZ() + ") -");
                break;

        }
    }


    public boolean isOutOfBounds(Player player) {
        WorldBorder worldBorder = player.getWorld().getWorldBorder();
        Location playerLocation = player.getLocation();
        Location centerLocation = worldBorder.getCenter();
        double radius = worldBorder.getSize() / 2.0;

        double deltaX = Math.abs(centerLocation.getX() - playerLocation.getX());
        double deltaZ = Math.abs(centerLocation.getZ() - playerLocation.getZ());

        return deltaX > radius || deltaZ > radius;
    }


    public void createLocationBeacon(Location beaconLocation) {
        if (beaconLocation == null || beaconLocation.getWorld() == null) {
            return;
        }
        // Create the beacon block
        Block beaconBlock = Bukkit.getWorld("jew").getBlockAt(getGroundLocation(beaconLocation));
        blocksToReplace.put(beaconBlock, beaconBlock.getType());
        beaconBlock.setType(Material.BEACON);

        System.out.println(beaconBlock.getX() + beaconBlock.getY() + beaconBlock.getZ());
        // Create the 3x3 structure of gold blocks below the beacon
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                Location blockLocation = beaconBlock.getLocation().clone().add(xOffset, -1, zOffset);
                Block currentBlock = blockLocation.getWorld().getBlockAt(blockLocation);

                blocksToReplace.put(currentBlock, currentBlock.getType());
                blockLocation.getWorld().getBlockAt(blockLocation).setType(Material.GOLD_BLOCK);
            }
        }

    }

    private void winnerEffects(Player player) {
        CratesMain.doFirework(player);

        player.getLocation().getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 2.5, 0), 25, .13, .13, .13, -0.0025);
        this.setIncursionMode(false);
        clearItems();

    }
    private void temporarySpeedBoost() {
        Bukkit.broadcastMessage(ChatColor.AQUA + "Everyone has received a temporary speed boost!");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 1));
        }
    }
    public void checkWinner() {
        synchronized (playersLeft) {
            if (playersLeft.size() <= maxTeamSize) {
                List<List<Player>> teams = new ArrayList<>();
                for (UUID uuid : playersLeft) {
                    Player teamPlayer = Bukkit.getPlayer(uuid);
                    if (teamPlayer != null) {
                        List<Player> team = getTeam(uuid);
                        boolean teamAlreadyAdded = teams.stream().anyMatch(existingTeam -> new HashSet<>(existingTeam).containsAll(team) && new HashSet<>(team).containsAll(existingTeam));
                        if (!teamAlreadyAdded) {
                            teams.add(team);
                        }
                    }
                }
                if (teams.size() == 1) {
                    // The remaining players are all on the same team, they are the winners
                    List<Player> winningTeam = teams.get(0);
                    String teamNames = winningTeam.stream().map(Player::getName).collect(Collectors.joining(", "));
                    StringUtil.broadcastCentered("&7>> &e&lRACE &7- The team " + teamNames + " has won the race!");
                    new BukkitRunnable() {
                        int timesToRun = 10;

                        @Override
                        public void run() {
                            for (Player player : winningTeam) {
                                clearFlags(player);
                                if (timesToRun >= 1) {
                                    winnerEffects(player);
                                    timesToRun--;
                                }
                                if (timesToRun <= 0) {
                                    cancel();
                                }
                            }
                        }
                    }.runTaskTimer(PracticeServer.getInstance(), 0, 5L);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), this::endRace, 120L);
                }
            }
        }
    }

    private Location getRandomLocation(World world, ProtectedRegion region) {
        // Generate random X and Z coordinates within the region bounds
        int randomX = random.nextInt(region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX()) + region.getMinimumPoint().getBlockX();
        int randomZ = random.nextInt(region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ()) + region.getMinimumPoint().getBlockZ();

        // Create a new location with the random coordinates
        return new Location(world, randomX, 0, randomZ);
    }

    /**
     * Resets the border to its initial state.
     */
    public void resetBorder() {
        worldBorder.setDamageAmount(0);
        worldBorder.setWarningDistance(155);
        worldBorder.setSize(3500);
    }

    public int getZoneDamage(Player player) {
        int dmg = (int) (player.getMaxHealth() * 0.01);
        if (dmg <= 1) dmg = 3;
        return dmg;
    }

    public void addPlayer(Player player) {
        if (isPlaying(player)) return;
        playersLeft.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        if (!isPlaying(player)) return;
        playersLeft.remove(player.getUniqueId());
    }

    public void restoreBeaconBlocks() {
        for (Block block : blocksToReplace.keySet()) {
            block.setType(blocksToReplace.get(block));
        }
        blocksToReplace.clear();
    }

    public boolean isPlaying(Player player) {
        return playersLeft.contains(player.getUniqueId());
    }

    public Location getGroundLocation(Location location) {
        return location.getWorld().getHighestBlockAt(location).getLocation().add(0, -1, 0);
    }

    public void setLobbyTime(int lobbyTime) {
        this.lobbyTime = lobbyTime;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }


    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public void setShrinkTime(int shrinkTime) {
        this.shrinkTime = shrinkTime;
    }

    public MinigameState getGameState() {
        return gameState;
    }

    @EventHandler //events at the bottom because I said so
    public void onDeath(PlayerDeathEvent event) {
        if (gameState == MinigameState.SHRINK) {
            Alignments.setLawful(event.getEntity());
            eliminatePlayer(event.getEntity());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!event.getPlayer().getInventory().contains(Material.SADDLE) && gameState != MinigameState.NONE)
            event.getPlayer().getInventory().addItem(Horses.createMount(3, false));
        if (gameState == MinigameState.SHRINK) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (gameState == MinigameState.SHRINK && playersLeft.contains(event.getPlayer().getUniqueId())) {
            Alignments.setLawful(event.getPlayer());
            eliminatePlayer(event.getPlayer());
        }
    }

    @EventHandler()
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (gameState != MinigameState.NONE && gameState != MinigameState.SHRINK && !playersTotal.contains(event.getPlayer().getUniqueId())) {
            initializePlayer(player);
        }
        if (player.getGameMode().equals(GameMode.SPECTATOR) && gameState != MinigameState.SHRINK)
            player.setGameMode(GameMode.SURVIVAL);

        if (gameState == MinigameState.SHRINK) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (gameState == MinigameState.SHRINK) {
                        Alignments.setLawful(player);
                        removePlayer(player);
                        player.setGameMode(GameMode.SPECTATOR);
                    }
                }
            }.runTaskLater(PracticeServer.getInstance(), 40L);
        }
    }
    public void toggleIncursionMode(boolean enabled, double healthMultiplier) {
        this.incursionMode = enabled;
        this.incursionHealthMultiplier = healthMultiplier;
        if (enabled) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Incursion mode has been enabled! Lightning mobs will spawn with a max " + healthMultiplier + "x Multipliers!");
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Incursion mode has been disabled.");
        }
    }

}

