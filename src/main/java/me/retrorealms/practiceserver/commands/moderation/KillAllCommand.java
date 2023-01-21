package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class KillAllCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            if (p.isOp()) {
                if (cmd.getName().equalsIgnoreCase("killall")) {
                    for (final Entity e : Bukkit.getWorlds().get(0).getEntities()) {
                        if (e instanceof LivingEntity && !(e instanceof Player)) {
                            if(!MobHandler.isWorldBoss(e)) e.remove();
                        }
                    }
                    Spawners.mobs.clear();
                    Spawners.respawntimer.clear();
                }
            }
        }
        return false;
    }
}
