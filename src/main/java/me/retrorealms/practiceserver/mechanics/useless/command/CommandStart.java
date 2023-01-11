package me.retrorealms.practiceserver.mechanics.useless.command;

import com.google.common.collect.Lists;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.useless.skeleton.SkeletonDungeon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Created by Giovanni on 13-5-2017.
 */
public class CommandStart implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;

            if (!player.getName().equalsIgnoreCase("vawkenetty")) return false;

            if (Parties.isInParty(player)) {
                List<Player> players = Parties.getEntirePartyOf(player);

                List<Player> nearbyPlayers = Lists.newArrayList();

                player.getNearbyEntities(50, 50, 50).forEach(nearby -> {

                    if (nearby instanceof Player) {

                        Player near = (Player) nearby;
                        if (!players.contains(near)) return;

                        nearbyPlayers.add(near);
                    }
                });

                nearbyPlayers.add(player);

                SkeletonDungeon skeletonDungeon = new SkeletonDungeon(nearbyPlayers);

                return false;
            }

            SkeletonDungeon skeletonDungeon = new SkeletonDungeon(Collections.singletonList(player));

        }

        return false;
    }
}
