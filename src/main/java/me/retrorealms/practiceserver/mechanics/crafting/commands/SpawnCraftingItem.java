package me.retrorealms.practiceserver.mechanics.crafting.commands;
import me.retrorealms.practiceserver.mechanics.crafting.items.CustomItemHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class SpawnCraftingItem implements CommandExecutor {


        private final CustomItemHandler itemSystem;

        public SpawnCraftingItem(CustomItemHandler itemSystem) {
            this.itemSystem = itemSystem;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.isOp()) {
                player.sendMessage("You don't have permission to use this command.");
                return true;
            }

            if (args.length != 1) {
                player.sendMessage("Usage: /spawnitem <itemId>");
                return true;
            }

            String itemId = args[0];
            ItemStack item = itemSystem.createCustomItem(itemId);

            if (item == null) {
                player.sendMessage("Invalid item ID.");
                return true;
            }

            player.getInventory().addItem(item);
            player.sendMessage("You have received the item: " + item.getItemMeta().getDisplayName());

            return true;
        }
    }
