package me.retrorealms.practiceserver.mechanics.moderation;

import com.google.common.collect.Lists;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.chat.ChatTag;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.chat.ChatMechanics;
import me.retrorealms.practiceserver.mechanics.player.GamePlayer.StaticConfig;
import me.retrorealms.practiceserver.mechanics.world.MinigameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jaxon on 8/13/2017.
 */
public class ModerationMechanics implements Listener {

    public static HashMap<UUID, RankEnum> rankHashMap = new HashMap<>();

    public static HashMap<UUID, PermissionAttachment> getPerms() {
        return perms;
    }

    private static HashMap<UUID, PermissionAttachment> perms = new HashMap<UUID, PermissionAttachment>();

    public static RankEnum getRank(Player player) {
        if(PracticeServer.getRaceMinigame().getGameState() != MinigameState.NONE && player.isOp()) return RankEnum.GM;
        if(PracticeServer.getRaceMinigame().getGameState() != MinigameState.NONE) return RankEnum.DEFAULT;
        return rankHashMap.get(player.getUniqueId());
    }

    public static List<String> ImportantStaff() {
        List<String> staffList = Arrays.asList("Red");
        return staffList;
    }

    public static boolean isStaff(Player player) {
        if(player.isOp()) return true;
        if(PracticeServer.getRaceMinigame().getGameState() != MinigameState.NONE) return false;
        RankEnum rankEnum = getRank(player);
        switch (rankEnum) {
            case QUALITY:
            case BUILDER:
            case PMOD:
            case GM:
            case MANAGER:
            case DEV:
                return true;
            default:
                return false;
        }
    }

    public static boolean isDonator(Player player) {
        if(PracticeServer.getRaceMinigame().getGameState() != MinigameState.NONE) return false;
        RankEnum rankEnum = getRank(player);
        switch (rankEnum) {
            case SUB:
            case SUB1:
            case SUB2:
            case SUB3:
            case SUPPORTER:
            case YOUTUBER:
            case QUALITY:
            case PMOD:
            case GM:
            case DEV:
            case MANAGER:
                return true;
            default:
                return false;
        }
    }

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.getInstance());
        loadModeration();
    }

    public void onDisable() {
        saveModeration();
    }

    /*Add new perms for player here*/
    public static List<String> getPermsForRank(RankEnum rankEnum) {
        List<String> permList = new ArrayList<>();

        if (rankEnum == RankEnum.SUB || rankEnum == RankEnum.SUB1 || rankEnum == RankEnum.SUB2 || rankEnum == RankEnum.BUILDER || rankEnum == RankEnum.PMOD) {
            permList.add("ultracosmetics.particleeffects.greensparks");
            permList.add("ultracosmetics.particleeffects.frozenwalk");
            permList.add("ultracosmetics.particleeffects.inferno");
            permList.add("ultracosmetics.mounts.hypecart");

        }
        if (rankEnum == RankEnum.SUB1 || rankEnum == RankEnum.SUB2 || rankEnum == RankEnum.BUILDER || rankEnum == RankEnum.PMOD) {
            permList.add("ultracosmetics.particleeffects.raincloud");
            permList.add("ultracosmetics.particleeffects.snowcloud");
            permList.add("ultracosmetics.particleeffects.frostlord");
            permList.add("ultracosmetics.particleeffects.flamerings");
            permList.add("ultracosmetics.particleeffects.inlove");
            permList.add("ultracosmetics.mounts.infernalhorror");
            permList.add("ultracosmetics.mounts.glacialsteed");
            permList.add("ultracosmetics.mounts.rudolph");

        }
        if (rankEnum == RankEnum.SUB2 || rankEnum == RankEnum.BUILDER || rankEnum == RankEnum.PMOD) {
            permList.add("ultracosmetics.particleeffects.music");
            permList.add("ultracosmetics.particleeffects.enchanted");
            permList.add("ultracosmetics.particleeffects.crushedcandycane");
            permList.add("ultracosmetics.mounts.mountoffire");
            permList.add("ultracosmetics.mounts.mountofwater");
            permList.add("ultracosmetics.mounts.slime");

        }
        if (rankEnum == RankEnum.SUPPORTER) {
            permList.add("ultracosmetics.mounts.*");
            permList.add("ultracosmetics.particleeffects.*");
        }

        if (rankEnum == RankEnum.PMOD || rankEnum == RankEnum.GM) {
            permList.add("litebans.kick");
            permList.add("litebans.ban");
            permList.add("litebans.tempban");
            permList.add("litebans.tempmute");
            permList.add("litebans.unmute");
            permList.add("litebans.history");
            permList.add("litebans.staffhistory");
            permList.add("litebans.ipmute");

        }
        if (rankEnum == RankEnum.GM) {
            permList.add("ultracosmetics.command.*");
            permList.add("ultracosmetics.gadgets.*");
            permList.add("litebans.togglechat");
            permList.add("litebans.unban");
            permList.add("litebans.dupeip");
            permList.add("litebans.notify.dupeip_join");
            permList.add("litebans.warn");
            permList.add("litebans.unwarn");
            permList.add("litebans.mute");
            permList.add("litebans.iphistory");
        }
        return permList;
    }

    private void loadModeration() {
        if (PracticeServer.DATABASE) return;
        for (String UUIDString : StaticConfig.get().getKeys(false)) {
            UUID playerUUID = UUID.fromString(UUIDString);
            /*RANKS*/
            RankEnum rankEnum = RankEnum.fromString(StaticConfig.get().getString(UUIDString + ".Main.Rank"));
            rankHashMap.put(UUID.fromString(UUIDString), rankEnum);

            /* CHAT TAG*/
            List<String> unlockedTags = StaticConfig.get().getStringList(UUIDString + ".Main.UnlockedChatTags");
            String tag = StaticConfig.get().getString(UUIDString + ".Main.ChatTag");

            if (tag == null) {
                tag = ChatTag.DEFAULT.name();
                if (StaticConfig.get().contains(UUIDString + ".Main.ChatTag")) {
                    StaticConfig.get().set(UUIDString + ".Main.ChatTag", "DEFAULT");
                    StaticConfig.save();
                }

            }

            if (unlockedTags != null && !unlockedTags.isEmpty()) {
                List<ChatTag> unlocked = Lists.newArrayList();

                unlockedTags.forEach(stringTag -> {
                    unlocked.add(ChatTag.valueOf(stringTag));
                });

                ChatMechanics.getUnlockedPlayerTags().put(java.util.UUID.fromString(UUIDString), unlocked);

            } else if (unlockedTags == null) {
                if (isDonator(Bukkit.getPlayer(playerUUID))) {
                    StaticConfig.get().set(UUIDString + ".Main.UnlockedChatTags", ChatTag.stream().collect(Collectors.toList()));
                    ChatMechanics.getUnlockedPlayerTags().put(playerUUID, ChatTag.stream().collect(Collectors.toList()));
                } else {
                    StaticConfig.get().set(UUIDString + ".Main.UnlockedChatTags", ChatMechanics.getUnlockedPlayerTags().get(playerUUID));
                    ChatMechanics.getUnlockedPlayerTags().put(playerUUID, Lists.newArrayList());
                }
                StaticConfig.save();
            }

            ChatTag chatTag = ChatTag.valueOf(tag.toUpperCase());

            ChatMechanics.getPlayerTags().put(playerUUID, chatTag);
        }

    }

    private void saveModeration() {
        if (PracticeServer.DATABASE) return;
        for (UUID playerUUID : rankHashMap.keySet()) {
            String UUIDString = playerUUID.toString();
            /*Rank*/
            StaticConfig.get().set(UUIDString + ".Main.Rank", RankEnum.enumToString(rankHashMap.get(playerUUID)));
            /*Tags*/
            StaticConfig.get().set(UUIDString + ".Main.ChatTag", ChatMechanics.getPlayerTags().get(playerUUID).name());

            if (Bukkit.getPlayer(playerUUID) == null) return;
            if (isDonator(Bukkit.getPlayer(playerUUID))) {
                StaticConfig.get().set(UUIDString + ".Main.UnlockedChatTags", ChatTag.stream().collect(Collectors.toList()));
            } else {
                StaticConfig.get().set(UUIDString + ".Main.UnlockedChatTags", ChatMechanics.getUnlockedPlayerTags().get(playerUUID));
            }
            StaticConfig.save();
        }

    }


}
