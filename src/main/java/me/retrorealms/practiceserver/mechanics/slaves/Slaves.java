package me.retrorealms.practiceserver.mechanics.slaves;

import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLCreate;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.Arrays;

public class Slaves implements Listener {



    @EventHandler
    public void onSlaveManagerClick(PlayerInteractEntityEvent e) {
        HumanEntity npc = (HumanEntity) e.getPlayer();
        Player p = e.getPlayer();
        if(npc.getName().contains("Slave Manager")){
            openSlaveManager(p);
        }
    }

    public void openSlaveManager(Player p){
        Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, "Slave List");

        int oreAmt = 1;

        ItemStack t5Miner = new ItemStack(Material.GOLD_PICKAXE);
        ItemMeta t5MinerMeta = t5Miner.getItemMeta();
        t5MinerMeta.setDisplayName(ChatColor.YELLOW + "Tier 5 Miner");
        t5MinerMeta.setLore(
                Arrays.asList(new String[] {
                        ChatColor.GRAY + "Mines " + oreAmt + "x gold ore per minute",
                        ChatColor.WHITE + "Price: " + ChatColor.GREEN + "25000g" }));
        t5Miner.setItemMeta(t5MinerMeta);
        inv.setItem(3, t5Miner);
        p.openInventory(inv);
    }

    @EventHandler
    public void buySlave(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory().getTitle().equals("Slave List")) {
            e.setCancelled(true);
            if(e.getCurrentItem().getType() ==  Material.GOLD_PICKAXE) {
                verifySlave(p);
            }
        }
    }

    public void verifySlave(Player p){
        try {
            FileWriter writer = new FileWriter("MyFile.txt", true);
            writer.write("Hello World");
            writer.write("\r\n");   // write new line
            writer.write("Good Bye!");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter writer = new FileWriter("MyFile.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            bufferedWriter.write("Hello World");
            bufferedWriter.newLine();
            bufferedWriter.write("See You Again!");

            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileReader reader = new FileReader("MyFile.txt");
            int character;

            while ((character = reader.read()) != -1) {
                System.out.print((char) character);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onEnable(){
    }

    public void onDisable(){
    }

}
