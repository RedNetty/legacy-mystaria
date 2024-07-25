package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WipeCommand implements CommandExecutor {
    private static final Logger LOGGER = Logger.getLogger(WipeCommand.class.getName());

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("retrorealms.admin.wipe")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        sender.sendMessage("§eInitiating server wipe...");

        try {
            // Step 1: Kick all players
            kickAllPlayers();

            // Step 2: Clear all dropped items
            clearDroppedItems();

            // Step 3: Enable whitelist
            Bukkit.setWhitelist(true);

            // Step 4: Delete player data
            deletePlayerData();

            // Step 5: Disable whitelist
            Bukkit.setWhitelist(false);

            sender.sendMessage("§aServer wipe completed successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during server wipe", e);
            sender.sendMessage("§cAn error occurred during the wipe process. Check server logs for details.");
            Bukkit.setWhitelist(false);  // Ensure whitelist is disabled in case of error
        }

        return true;
    }

    private void kickAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§cServer is being wiped. Please rejoin in a few minutes.");
        }
        LOGGER.info("All players have been kicked for server wipe.");
    }

    private void clearDroppedItems() {
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    entity.remove();
                    count++;
                }
            }
        }
        LOGGER.info("Cleared " + count + " dropped items from all worlds.");
    }

    private void deletePlayerData() {
        try (Connection conn = SQLMain.getConnection()) {
            conn.setAutoCommit(false);  // Start transaction

            try {
                // Delete data from PlayerData table
                executeDelete(conn, "DELETE FROM PlayerData");

                // Delete data from all Banks tables and their backups
                for (int i = 1; i <= 5; i++) {
                    String tableName = i == 1 ? "banks" : "banks" + i;
                    executeDelete(conn, "DELETE FROM " + tableName);
                    executeDelete(conn, "DELETE FROM " + tableName + "_backup");
                }

                // Delete data from GuildBanks table
                executeDelete(conn, "DELETE FROM GuildBanks");

                // Delete data from Guilds table
                executeDelete(conn, "DELETE FROM Guilds");

                // Note: We're not deleting from PersistentData as per the requirement

                conn.commit();  // Commit the transaction
                LOGGER.info("All player data (except persistent data) has been wiped successfully.");
            } catch (SQLException e) {
                conn.rollback();  // Rollback in case of any error
                throw e;  // Re-throw the exception to be caught in the outer catch block
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting player data", e);
            throw new RuntimeException("Failed to delete player data", e);
        }
    }

    private void executeDelete(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Executed: " + sql + " - Rows affected: " + rowsAffected);
        }
    }
}