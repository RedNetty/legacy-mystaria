package me.retrorealms.practiceserver.utils;

import com.google.common.collect.Maps;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Created by Giovanni on 12-5-2017.
 */
public class GlowAPI {

    public static void setGlowing(Entity entity, org.inventivetalent.glow.GlowAPI.Color color) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if (Toggles.hasGlowOnDrops(player)) {
                org.inventivetalent.glow.GlowAPI.setGlowing(entity, color, player);
            }
        }
    }

    /* DEPRECATED */
    private static final HashMap<GlowGroup, Team> scoreboardMap = Maps.newHashMap();

    public static void init() {
        if (!scoreboardMap.isEmpty()) return;


        GlowGroup.stream().forEach(glowGroup -> {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(glowGroup.name());

            team.setPrefix(glowGroup.getGlowColour().getColor().toString());
            scoreboardMap.put(glowGroup, team);
        });
    }

    public static void end() {
        scoreboardMap.values().forEach(Team::unregister);
    }

    public static void setGlowing(GlowGroup glowGroup, Entity entity) {
        scoreboardMap.get(glowGroup).addEntry(entity.getUniqueId().toString());
        entity.setGlowing(true);
    }

    public static void removeGlow(Entity entity) {
        scoreboardMap.values().forEach(team -> {
            if (team.hasEntry(entity.getUniqueId().toString())) team.removeEntry(entity.getUniqueId().toString());
        });

        if (!entity.isDead()) entity.setGlowing(false);
    }

    public enum GlowGroup {

        ONE(GlowColour.WHITE),
        TWO(GlowColour.GREEN),
        THREE(GlowColour.AQUA),
        FOUR(GlowColour.YELLOW);

        private GlowColour glowColour;

        GlowGroup(GlowColour glowColour) {
            this.glowColour = glowColour;
        }

        static Stream<GlowGroup> stream() {
            return Arrays.stream(values());
        }

        public GlowColour getGlowColour() {
            return glowColour;
        }
    }

    public enum GlowColour {

        GREEN('a'),
        BLUE('1'),
        RED('c'),
        ORANGE('6'),
        YELLOW('e'),
        WHITE('f'),
        AQUA('b');

        private char colorCode;

        GlowColour(char colorCode) {
            this.colorCode = colorCode;
        }

        public ChatColor getColor() {
            return ChatColor.getByChar(colorCode);
        }
    }
}