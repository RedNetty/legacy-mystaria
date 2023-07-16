package me.retrorealms.practiceserver.mechanics.loot.contract;

import me.retrorealms.practiceserver.mechanics.donations.Nametags.Nametag;
import me.retrorealms.practiceserver.mechanics.loot.contract.types.LootContract;
import me.retrorealms.practiceserver.mechanics.loot.contract.types.MobContract;
import me.retrorealms.practiceserver.mechanics.loot.contract.types.PlayerContract;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


import javax.naming.Name;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContractMenu {

    private static final int MENU_SIZE = 9;
    private static final String MENU_TITLE = "Contracts";

    public static void openContractMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, ChatColor.translateAlternateColorCodes('&', MENU_TITLE));

        List<Contract> contracts = getCurrentContracts(player);
        contracts.add(new PlayerContract(1, 1, 5, ContractRewardType.MONEY));
        contracts.add(new MobContract(40, 3, 2, ContractRewardType.GEAR));
        contracts.add(new LootContract(25, 4, 4, ContractRewardType.ORBS));
        contracts.add(new PlayerContract(2, 2, 5,ContractRewardType.MONEY));

        for (int i = 0; i < contracts.size(); i++) {
            Contract contract = contracts.get(i);
            ItemStack contractItem = createContractItem(contract);
            inventory.setItem(i, contractItem);
        }

        player.openInventory(inventory);
    }

    private static List<Contract> getCurrentContracts(Player player) {
        // Replace with your logic to retrieve the player's contracts from the contractMap
        HashMap<Player, List<Contract>> contractMap = ContractHandler.getContractMap();
        if(!contractMap.containsKey(player)) contractMap.put(player, new ArrayList<>());
        return contractMap.get(player);
    }
    private static ItemStack createContractItem(Contract contract) {
        Material material = Material.PAPER;

        String displayName = Nametag.getRarityString(contract.getRarity()) + ChatColor.RESET + ChatColor.BOLD + " Contract";
        String contractType = ChatColor.BLUE + "Contract Type: " + ChatColor.GREEN + contract.getTypeString();
        String contractTier = ChatColor.DARK_AQUA + "Contract Tier: " + ChatColor.GRAY + "T" + contract.getTier();
        String conditions = ChatColor.DARK_AQUA + "Completion Conditions: " + ChatColor.GRAY + contract.getConditions();

        String rewardTypeInfo = ChatColor.DARK_AQUA + "Reward Type: " + ChatColor.GRAY + contract.getRewardType().name();
        String acceptedStatus = ChatColor.DARK_AQUA + "Accepted: " + (contract.isAccepted() ? ChatColor.GREEN + "✔" : ChatColor.RED + "✘");

        String completed = contract.isComplete() ? ChatColor.GREEN + "✔ Click for rewards." : ChatColor.RED + "Not Completed.";
        String completeStatus = ChatColor.DARK_AQUA + "Complete: " + completed;


        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(contractType);
        lore.add("");
        lore.add(contractTier);
        lore.add(rewardTypeInfo);
        lore.add(conditions);
        lore.add("");
        lore.add(acceptedStatus);
        lore.add(completeStatus);

        itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
