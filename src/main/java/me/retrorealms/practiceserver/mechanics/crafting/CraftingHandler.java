package me.retrorealms.practiceserver.mechanics.crafting;

import me.retrorealms.practiceserver.mechanics.crafting.items.CustomItemHandler;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;

public class CraftingHandler implements Listener {
    private final JavaPlugin plugin;
    private final Map<String, Recipe> recipes;
    private final Map<Player, CraftingSession> activeSessions;
    private final CustomItemHandler itemSystem;
    private final CraftingMenu craftingMenu;

    public CraftingHandler(JavaPlugin plugin, CustomItemHandler itemSystem) {
        this.plugin = plugin;
        this.recipes = new HashMap<>();
        this.activeSessions = new HashMap<>();
        this.itemSystem = itemSystem;
        this.craftingMenu = new CraftingMenu(this, itemSystem);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void registerRecipes() {
        elementalHarmony();
        celestialBeacon();
        // Register other recipes here
    }

    private void elementalHarmony() {
        ItemStack essenceOfFrost = itemSystem.createCustomItem("essence_of_frost");
        ItemStack infernalCore = itemSystem.createCustomItem("infernal_core");
        ItemStack windsongFeather = itemSystem.createCustomItem("windsong_feather");
        ItemStack elementalHarmony = itemSystem.createCustomItem("elemental_harmony");

        Recipe elementalHarmonyRecipe = new Recipe(
                "ElementalHarmony",
                elementalHarmony,
                Arrays.asList(essenceOfFrost, infernalCore, windsongFeather)
        );

        registerRecipe(elementalHarmonyRecipe);
    }

    private void celestialBeacon() {
        ItemStack starlightEssence = itemSystem.createCustomItem("starlight_essence");
        ItemStack angelicFeather = itemSystem.createCustomItem("angelic_feather");
        ItemStack divineClay = itemSystem.createCustomItem("divine_clay");
        ItemStack celestialBeacon = itemSystem.createCustomItem("celestial_beacon");

        Recipe celestialBeaconRecipe = new Recipe(
                "CelestialBeacon",
                celestialBeacon,
                Arrays.asList(starlightEssence, angelicFeather, divineClay)
        );

        registerRecipe(celestialBeaconRecipe);
    }

    public void registerRecipe(Recipe recipe) {
        recipes.put(recipe.getResultName(), recipe);
    }

    public Map<String, Recipe> getRecipes() {
        return recipes;
    }

    public void openCraftingMenu(Player player) {
        craftingMenu.openMenu(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Crafting Menu")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (activeSessions.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "You are already crafting!");
            return;
        }

        for (Recipe recipe : recipes.values()) {
            if (matchRecipe(recipe, clickedItem)) {
                if (hasIngredients(player, recipe.getIngredients())) {
                    removeIngredients(player, recipe.getIngredients());
                    startCraftingAnimation(player, recipe);
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the required ingredients!");
                }
                break;
            }
        }
    }

    private boolean matchRecipe(Recipe recipe, ItemStack clickedItem) {
        ItemStack recipeResult = recipe.getResult();
        return recipeResult.getType() == clickedItem.getType() &&
                recipeResult.getItemMeta().getDisplayName().equals(clickedItem.getItemMeta().getDisplayName());
    }

    private boolean hasIngredients(Player player, List<ItemStack> ingredients) {
        for (ItemStack ingredient : ingredients) {
            if (!hasMatchingItem(player, ingredient)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasMatchingItem(Player player, ItemStack ingredient) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && itemSystem.isCustomItem(item, itemSystem.getCustomItemId(ingredient))) {
                return true;
            }
        }
        return false;
    }

    private void removeIngredients(Player player, List<ItemStack> ingredients) {
        for (ItemStack ingredient : ingredients) {
            removeMatchingItem(player, ingredient);
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                event.getClickedBlock() != null &&
                event.getClickedBlock().getType() == Material.ENDER_PORTAL_FRAME) {

            event.setCancelled(true);
            Player player = event.getPlayer();
            openCraftingMenu(player);
        }
    }
    private void removeMatchingItem(Player player, ItemStack ingredient) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && itemSystem.isCustomItem(item, itemSystem.getCustomItemId(ingredient))) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().remove(item);
                }
                break;
            }
        }
    }

    private void startCraftingAnimation(Player player, Recipe recipe) {
        CraftingSession session = new CraftingSession(player, recipe);
        activeSessions.put(player, session);
        session.start();
    }

    private class CraftingSession {
        private final Player player;
        private final Recipe recipe;
        private final List<ArmorStand> ingredientStands = new ArrayList<>();
        private ArmorStand resultStand;
        private BukkitRunnable animationTask;
        private int tick = 0;

        public CraftingSession(Player player, Recipe recipe) {
            this.player = player;
            this.recipe = recipe;
        }

        public void start() {
            Location centerLoc = player.getLocation().add(0, 1, 0);
            createIngredientStands(centerLoc);
            createResultStand(centerLoc.clone().add(0, 2, 0));
            startAnimation();
        }

        private void createIngredientStands(Location center) {
            double angleIncrement = 360.0 / recipe.getIngredients().size();
            double currentAngle = 0;

            for (ItemStack ingredient : recipe.getIngredients()) {
                double radians = Math.toRadians(currentAngle);
                double x = Math.cos(radians) * 3;
                double z = Math.sin(radians) * 3;

                Location standLocation = center.clone().add(x, 0, z);
                ArmorStand stand = createStand(standLocation, ingredient);
                ingredientStands.add(stand);

                currentAngle += angleIncrement;
            }
        }

        private ArmorStand createStand(Location location, ItemStack item) {
            ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setHelmet(item);
            return stand;
        }

        private void createResultStand(Location location) {
            resultStand = createStand(location, new ItemStack(Material.AIR));
        }

        private void startAnimation() {
            animationTask = new BukkitRunnable() {
                double angle = 0;

                @Override
                public void run() {
                    if (tick >= 240) {
                        finishCrafting();
                        this.cancel();
                        return;
                    }

                    animateIngredients(angle);
                    animateResult();
                    playCraftingEffects();

                    if (tick >= 160) {
                        mergeIngredients();
                    }

                    angle += Math.PI / 30;
                    tick++;
                }
            };
            animationTask.runTaskTimer(plugin, 0L, 1L);
        }

        private void animateIngredients(double angle) {
            Location center = player.getLocation().add(0, 1, 0);
            World world = center.getWorld();

            for (int i = 0; i < ingredientStands.size(); i++) {
                double radius = 3 - (tick * 0.02);
                double y = Math.sin(angle + (2 * Math.PI * i / ingredientStands.size())) * 0.5;

                double individualAngle = angle + (2 * Math.PI * i / ingredientStands.size());
                double x = Math.cos(individualAngle) * radius;
                double z = Math.sin(individualAngle) * radius;

                Location newLoc = center.clone().add(x, y, z);
                ingredientStands.get(i).teleport(newLoc);

                world.spawnParticle(Particle.DRAGON_BREATH, newLoc.clone().add(0, -3, 0), 2, 0.1, 3, 0.1, 0);
            }
        }

        private void animateResult() {
            Location center = player.getLocation().add(0, 1, 0);
            double height = 2 + Math.sin(tick * 0.05) * 0.3;
            double rotation = tick * 3 * (Math.PI / 180);

            Location newLoc = center.clone().add(0, height, 0);
            resultStand.teleport(newLoc);
            resultStand.setHeadPose(new EulerAngle(0, rotation, 0));

            for (int i = 0; i < 10; i++) {
                double spiralAngle = tick * 0.1 + (i * 0.6);
                double x = Math.cos(spiralAngle) * (0.5 + i * 0.1);
                double y = i * 0.2;
                double z = Math.sin(spiralAngle) * (0.5 + i * 0.1);
                Location particleLoc = newLoc.clone().add(x, y, z);
                center.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, particleLoc, 1, 0, 0, 0, 0);
            }
        }

        private void playCraftingEffects() {
            Location center = player.getLocation().add(0, 1, 0);
            World world = center.getWorld();

            for (int i = 0; i < 36; i++) {
                double ringAngle = Math.PI * 2 * i / 36;
                double radius = tick * 0.05;
                double x = Math.cos(ringAngle) * radius;
                double z = Math.sin(ringAngle) * radius;
                Location ringLoc = center.clone().add(x, 0, z);
                world.spawnParticle(Particle.SPELL_WITCH, ringLoc, 1, 0, 0, 0, 0);
            }

            if (tick % 20 == 0) {
                world.playSound(center, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
            }

            if (tick % 60 == 0) {
                world.strikeLightningEffect(center);
                world.playSound(center, Sound.ENTITY_LIGHTNING_THUNDER, 1.0f, 1.0f);
                player.setGlowing(true);
            }
        }

        private void mergeIngredients() {
            Location center = player.getLocation().add(0, 1, 0);

            for (ArmorStand stand : ingredientStands) {
                Location standLoc = stand.getLocation();
                Vector direction = center.toVector().subtract(standLoc.toVector()).normalize();
                stand.setVelocity(direction.multiply(0.3));
            }

            if (tick == 200) {
                for (ArmorStand stand : ingredientStands) {
                    stand.remove();
                }
                ingredientStands.clear();

                resultStand.setHelmet(recipe.getResult());
                resultStand.getWorld().playSound(resultStand.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);

                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI * 2 * i / 8;
                    double x = Math.cos(angle) * 2;
                    double z = Math.sin(angle) * 2;
                    Location armorStandLoc = center.clone().add(x, 0, z);
                    ArmorStand armorStand = createStand(armorStandLoc, recipe.getResult());
                    armorStand.setHeadPose(new EulerAngle(0, angle, 0));
                    ingredientStands.add(armorStand);
                }

                player.setVelocity(new Vector(0, 0.5, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 25, 1));
            }
        }

        private void finishCrafting() {
            try {
                World world = player.getWorld();
                Location center = player.getLocation().add(0, 1, 0);

                ingredientStands.forEach(ArmorStand::remove);
                resultStand.remove();

                player.getInventory().addItem(recipe.getResult());

                world.spawnParticle(Particle.EXPLOSION_LARGE, center, 1);
                world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

                launchFireworks(center);

                player.sendMessage(ChatColor.GREEN + "You have crafted " + recipe.getResult().getItemMeta().getDisplayName() + "!");

                activeSessions.remove(player);
                player.setGlowing(false);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error finishing crafting for player: " + player.getName(), e);
                player.sendMessage(ChatColor.RED + "An error occurred while crafting. Please contact an administrator.");
            }
        }

        private void launchFireworks(Location location) {
            try {
                Firework fw = location.getWorld().spawn(location, Firework.class);
                FireworkMeta fwm = fw.getFireworkMeta();

                fwm.addEffect(FireworkEffect.builder()
                        .withColor(Color.RED, Color.BLUE, Color.WHITE)
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .withFlicker()
                        .build());

                fwm.setPower(0);
                fw.setFireworkMeta(fwm);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error launching fireworks at location: " + location, e);
            }
        }
    }
}

class Recipe {
    private final String resultName;
    private final ItemStack result;
    private final List<ItemStack> ingredients;

    public Recipe(String resultName, ItemStack result, List<ItemStack> ingredients) {
        this.resultName = resultName;
        this.result = result;
        this.ingredients = ingredients;
    }

    public String getResultName() {
        return resultName;
    }

    public ItemStack getResult() {
        return result.clone();
    }

    public List<ItemStack> getIngredients() {
        return new ArrayList<>(ingredients);
    }
}