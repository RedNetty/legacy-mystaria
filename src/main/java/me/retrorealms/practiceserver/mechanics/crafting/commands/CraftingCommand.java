package me.retrorealms.practiceserver.mechanics.crafting.commands;

import me.retrorealms.practiceserver.mechanics.crafting.CraftingHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CraftingCommand implements CommandExecutor {
    private final CraftingHandler craftingHandler;

    public CraftingCommand(CraftingHandler craftingHandler) {
        this.craftingHandler = craftingHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("yourgame.craft")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        // This will open the crafting menu or start the crafting process
        craftingHandler.openCraftingMenu(player);

        return true;
    }
}