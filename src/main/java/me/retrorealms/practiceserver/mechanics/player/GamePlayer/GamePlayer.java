package me.retrorealms.practiceserver.mechanics.player.GamePlayer;

import com.google.common.collect.Lists;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.chat.ChatTag;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.chat.ChatMechanics;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.Economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by jaxon on 3/23/2017.
 */
public class GamePlayer implements Listener {


    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        if (PracticeServer.DATABASE) return;
        StaticConfig.setup();
        nonStaticConfig.setup();
    }


    public boolean nonStaticPlayerExist(Player p) {
        return nonStaticConfig.get().contains(p.getUniqueId().toString());
    }

    public boolean StaticPlayerExist(Player p) {
        return StaticConfig.get().contains(p.getUniqueId().toString() + ".Main.Rank");
    }

    @EventHandler
    public void onJoinCheck(PlayerJoinEvent e) {
        if (PracticeServer.DATABASE) return;
        if (!StaticPlayerExist(e.getPlayer())) {
            Player p = e.getPlayer();
            ModerationMechanics.rankHashMap.put(p.getUniqueId(), RankEnum.DEFAULT);
            ChatMechanics.getPlayerTags().put(p.getUniqueId(), ChatTag.DEFAULT);
            StaticConfig.get().set(e.getPlayer().getUniqueId() + ".Main.Rank", "DeFaUlT");
            StaticConfig.get().set(p.getUniqueId() + ".Info.Username", p.getName());
            StaticConfig.get().set(p.getUniqueId() + ".Info.IP Address", p.getAddress().toString());
            StaticConfig.get().set(p.getUniqueId() + ".Main.Banned", 0);
            StaticConfig.get().set(p.getUniqueId() + ".Main.Muted", 0);
            StaticConfig.get().set(p.getUniqueId() + ".Main.ChatTag", "DEFAULT");
            StaticConfig.get().set(p.getUniqueId() + ".Main.UnlockedChatTags", Lists.newArrayList());
            StaticConfig.save();

        }
        if (!nonStaticPlayerExist(e.getPlayer())) {
            PracticeServer.plugin.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin, new Runnable() {
                @Override
                public void run() {
                    Player p = e.getPlayer();

                    Economy.currentBalance.put(p.getUniqueId(), 0);
                    nonStaticConfig.get().set(p.getUniqueId() + ".Info.Horse Tier", 0);
                    nonStaticConfig.get().set(p.getUniqueId() + ".Economy.Money Balance", 0);
                    nonStaticConfig.get().set(p.getUniqueId() + ".Economy.Elite Shards", 0);
                    nonStaticConfig.get().set(p.getUniqueId() + ".Stats.Monster Kills", 0);
                    nonStaticConfig.get().set(p.getUniqueId() + ".Stats.Player Kills", 0);
                    nonStaticConfig.save();
                }
            }, 50);

        }
    }

}
