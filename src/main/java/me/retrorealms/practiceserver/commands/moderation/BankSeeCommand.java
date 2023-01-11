package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.money.Banks;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;

public class BankSeeCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (p.isOp()) {
            if (args.length == 1 || args.length == 2) {
                if(PracticeServer.DATABASE){
                    Inventory inv = SQLMain.getBank(Bukkit.getOfflinePlayer(args[0]).getUniqueId(), 1);
                    Banks.banksee.put(p, Bukkit.getOfflinePlayer(args[0]).getUniqueId());
                    p.openInventory(inv);
                }else {
                    File file = new File(PracticeServer.plugin.getDataFolder() + "/banks", String.valueOf(args[0]) + ".yml");
                    if (file.exists()) {
                        int page = 1;
                        if(args.length == 2){
                            page = Integer.valueOf(args[1]);
                        }
                        Inventory inv = Banks.getBank(p, page);
                        Banks.banksee.put(p, Bukkit.getOfflinePlayer(args[0]).getUniqueId());
                        if (inv == null) {
                            inv = Bukkit.createInventory(null, 63, "Bank Chest (1/1)");
                        }
                        inv.setItem(58, Banks.getGemBankItem(p));
                        p.openInventory(inv);

                        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                    } else {
                        p.sendMessage(ChatColor.RED + args[0] + " does not have a bank!");
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "/banksee <player>");
            }
        }
        return false;
    }
}