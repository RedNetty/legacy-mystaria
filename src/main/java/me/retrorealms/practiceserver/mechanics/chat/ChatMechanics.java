/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  net.minecraft.server.v1_7_R4.ChatSerializer
 *  net.minecraft.server.v1_7_R4.EntityPlayer
 *  net.minecraft.server.v1_7_R4.IChatBaseComponent
 *  net.minecraft.server.v1_7_R4.Packet
 *  net.minecraft.server.v1_7_R4.PacketPlayOutChat
 *  net.minecraft.server.v1_7_R4.PlayerConnection
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.AsyncPlayerChatEvent
 *  org.bukkit.event.player.PlayerChatTabCompleteEvent
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 */
package me.retrorealms.practiceserver.mechanics.chat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.VanishCommand;
import me.retrorealms.practiceserver.enums.chat.ChatTag;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.Banks;
import me.retrorealms.practiceserver.mechanics.player.Buddies;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.useless.task.AsyncTask;
import me.retrorealms.practiceserver.mechanics.vendors.GemGambling;
import me.retrorealms.practiceserver.mechanics.vendors.OrbGambling;
import me.retrorealms.practiceserver.mechanics.vendors.OreMerchant;
import me.retrorealms.practiceserver.mechanics.world.MinigameState;
import me.retrorealms.practiceserver.utils.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChatMechanics implements Listener {

    public static HashMap<Player, Integer> chatCooldown = new HashMap<>();

    public static HashMap<Player, Player> reply = new HashMap<>();
    private static final HashMap<UUID, ChatTag> playerTags = Maps.newHashMap();
    private static final HashMap<UUID, List<ChatTag>> unlockedPlayerTags = Maps.newHashMap();
    public static HashMap<Player, Integer> muted = new HashMap<>();

    public static HashMap<UUID, ChatTag> getPlayerTags() {
        return playerTags;
    }

    public static HashMap<UUID, List<ChatTag>> getUnlockedPlayerTags() {
        return unlockedPlayerTags;
    }

    public static boolean hasTagUnlocked(Player player, ChatTag chatTag) {
        if (!unlockedPlayerTags.containsKey(player.getUniqueId())) {
            unlockedPlayerTags.put(player.getUniqueId(), Lists.newArrayList());
        }

        return unlockedPlayerTags.get(player.getUniqueId()).contains(chatTag);
    }

    public static void unlockTag(Player player, ChatTag chatTag) {
        if (!unlockedPlayerTags.containsKey(player.getUniqueId())) {
            unlockedPlayerTags.put(player.getUniqueId(), Lists.newArrayList());
        }

        unlockedPlayerTags.get(player.getUniqueId()).add(chatTag);
    }

    public static List<String> bad_words;

    static {
        bad_words = new ArrayList<String>(Arrays.asList("nigger"));  //FUCK THIS STUPID FILTER "shit", "fuck", "cunt", "bitch", "whore", "slut", "wank", "asshole", "cock", "dick", "clit", "homo", "fag", "queer", "nigger", "dike", "dyke", "retard", "motherfucker", "vagina", "boob", "pussy", "rape", "gay", "penis", "cunt", "titty", "anus", "faggot", "xFinity", "destiny"));

    }

    public void onEnable() {
        PracticeServer.log.info("[ChatMechanics] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);

        File file = new File(PracticeServer.plugin.getDataFolder(), "muted.yml");
        YamlConfiguration config = new YamlConfiguration();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            config.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (config.getConfigurationSection("muted") != null) {
            for (String key : config.getConfigurationSection("muted").getKeys(false)) {
                int time = config.getConfigurationSection("muted").getInt(key);
                muted.put(Bukkit.getPlayer(UUID.fromString(key)), time);
            }
        }

        new AsyncTask(() -> {
            for (Player p : muted.keySet()) {
                if (muted.get(p) < 60) {
                    muted.remove(p);
                } else {
                    if(muted.get(p) > 0){
                        muted.put(p, muted.get(p) - 1);
                    }
                }
            }
        }).setUseSharedPool(true).setInterval(1).scheduleRepeatingTask();

    }

    public void onDisable() {
        PracticeServer.log.info("[ChatMechanics] has been disabled.");
        if(PracticeServer.DATABASE) return;
        File file = new File(PracticeServer.plugin.getDataFolder(), "muted.yml");
        YamlConfiguration config = new YamlConfiguration();
        for (Player s2 : muted.keySet()) {
            config.set("muted." + s2.getUniqueId().toString(), muted.get(s2));
        }
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTag(Player p) {
        String tag = Optional.ofNullable(playerTags.get(p.getUniqueId()))
                .filter(t -> t != ChatTag.DEFAULT)
                .map(ChatTag::getTag)
                .map(t -> t + " ")
                .orElse("");

        String rank = Optional.ofNullable(ModerationMechanics.getRank(p))
                .filter(r -> r != RankEnum.DEFAULT)
                .map(r -> r.tag + " ")
                .orElse("");

        return ChatColor.translateAlternateColorCodes('&', tag + rank);
    }

    public static String censorMessage(String msg) {
        if (msg == null) {
            return "";
        }
        for (String bad : bad_words) {
            msg = msg.replaceAll("(?i)" + bad, "*");
        }
        return msg.trim();
    }

    public static String getDisplayNameFor(Player p, Player sendee) {
        String nameColor;
        if (!Alignments.chaotic.containsKey(p.getName()) && !Alignments.neutral.containsKey(p.getName())) {
            nameColor = Buddies.getBuddies(sendee.getName()).contains(p.getName().toLowerCase()) ? ChatColor.GREEN + "" : ChatColor.GRAY + "";
        } else {
            nameColor = Alignments.neutral.containsKey(p.getName()) ? ChatColor.YELLOW + "" : ChatColor.RED + "";
        }
        String endColor = (ModerationMechanics.isStaff(p) || ModerationMechanics.isDonator(p)) ? ChatColor.WHITE + "" : ChatColor.GRAY + "";
        return nameColor + p.getName() + endColor;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = extractCommand(event.getMessage());

        if (isRestrictedCommand(command)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.WHITE + "Unknown command. View your Character Journal's Index for a list of commands.");
            return;
        }

        if (ModerationMechanics.isStaff(player)) {
            updateStaffPermissions(player);
        }

        if (!player.isOp() && !ModerationMechanics.isStaff(player)) {
            if (!isAllowedCommand(command, player)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.WHITE + "Unknown command. View your Character Journal's Index for a list of commands.");
            }
        }
    }

    private String extractCommand(String message) {
        String command = message.toLowerCase();
        if (command.startsWith("/")) {
            command = command.replaceFirst("/", "");
        }
        if (command.contains(" ")) {
            command = command.split(" ")[0];
        }
        return command;
    }

    private boolean isRestrictedCommand(String command) {
        List<String> restrictedCommands = Arrays.asList("save-all", "stack", "stop", "restart", "reload", "tpall", "kill", "mute");
        return restrictedCommands.contains(command);
    }

    private void updateStaffPermissions(Player player) {
        if (ModerationMechanics.getPerms().containsKey(player.getUniqueId())) {
            ModerationMechanics.getPerms().remove(player.getUniqueId());
        }
        PermissionAttachment attachment = player.addAttachment(PracticeServer.getInstance());
        ModerationMechanics.getPerms().put(player.getUniqueId(), attachment);
        PermissionAttachment playerPerms = ModerationMechanics.getPerms().get(player.getUniqueId());
        for (String perm : ModerationMechanics.getPermsForRank(ModerationMechanics.getRank(player))) {
            playerPerms.setPermission(perm, true);
        }
    }

    private boolean isAllowedCommand(String command, Player player) {
        List<String> allowedCommands = new ArrayList<>(Arrays.asList("leaderboard", "guildshow", "ginfo", "gwho", "guildpromote", "gpromote", "gdemote", "guilddemote", "orbs", "guildwho", "guildinfo", "gshow", "guildquit", "gquit", "guildcreate", "gaccept", "guildaccept", "gcreate", "ginvite", "guildinvite", "gkick", "guildkick", "guilddecline", "gdecline", "guild", "g"));

        if (!player.isOp() && PracticeServer.getRaceMinigame().getGameState() != MinigameState.NONE) {
            allowedCommands.addAll(Arrays.asList("patch", "pet", "shard", "mount", "tags", "mount", "roll", "toggletrading", "gl", "toggle", "toggles", "togglepvp", "togglechaos", "toggledebug", "Debug", "toggleff", "add", "del", "delete", "message", "msg", "m", "whisper", "w", "tell", "t", "reply", "r", "logout", "sync", "reboot", "pinvite", "paccept", "pquit", "pkick", "pdecline", "p", "g", "guilds", "guilds", "abandon", "create", "guildcreate", "gcreate", "join", "gi", "report", "editreport"));
            return allowedCommands.contains(command);
        }

        if (ModerationMechanics.isDonator(player)) {
            allowedCommands.addAll(Arrays.asList("togglegems", "lootbuff", "pet", "shard", "market", "patch", "mount", "tags", "mount", "roll", "toggletrading", "gl", "toggle", "toggles", "togglepvp", "togglechaos", "toggledebug", "Debug", "toggleff", "add", "del", "delete", "message", "msg", "m", "whisper", "w", "tell", "t", "reply", "r", "logout", "sync", "reboot", "pinvite", "paccept", "pquit", "pkick", "pdecline", "p", "toggletrail", "g", "guilds", "guilds", "abandon", "create", "join", "gi", "report", "editreport"));
        } else {
            allowedCommands.addAll(Arrays.asList("patch", "pet", "shard", "mount", "tags", "mount", "roll", "toggletrading", "gl", "toggle", "toggles", "togglepvp", "togglechaos", "toggledebug", "Debug", "toggleff", "add", "del", "delete", "message", "msg", "m", "whisper", "w", "tell", "t", "reply", "r", "logout", "sync", "reboot", "pinvite", "paccept", "pquit", "pkick", "pdecline", "p", "g", "guilds", "guilds", "abandon", "create", "guildcreate", "gcreate", "join", "gi", "report", "editreport"));
        }

        return allowedCommands.contains(command);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatTabComplete(PlayerChatTabCompleteEvent event) {
        Player player = event.getPlayer();
        String chatMessage = event.getChatMessage();
        if (chatMessage != null && !chatMessage.isEmpty()) {
            player.closeInventory();
            player.performCommand("gl " + chatMessage);
            PracticeServer.log.info("<G> " + player.getName() + ": " + chatMessage);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            event.setCancelled(true);
            if (isPlayerMutedAndNotInPlay(player)) {
                sendMuteMessage(player);
                return;
            }
            String message = event.getMessage();
            if (isValidMessageAndItem(message, player.getInventory().getItemInMainHand())) {
                processChatWithItem(player, message);
            } else {
                processChatWithoutItem(player, message);
            }
        }
    }

    private boolean isPlayerMutedAndNotInPlay(Player player) {
        return muted.containsKey(player) && !GemGambling.inPlayGem.contains(player.getName()) && !OrbGambling.chatHandling.contains(player.getUniqueId()) && !OreMerchant.chatInteractive.containsKey(player.getUniqueId()) && !Banks.WITHDRAW_PROMPT.contains(player.getName());
    }

    private void sendMuteMessage(Player player) {
        player.sendMessage(ChatColor.RED + "You are currently muted");
        if (ChatMechanics.muted.get(player) > 0) {
            Integer minutes = ChatMechanics.muted.get(player) / 60;
            player.sendMessage(ChatColor.RED + "Your tmute expires in " + minutes.toString() + " minutes.");
        } else {
            player.sendMessage(ChatColor.RED + "Your mute WILL NOT expire.");
        }
    }

    private void processChatWithItem(Player player, String message) {
        this.sendShowString(player, player.getInventory().getItemInMainHand(), fullDisplayName(player), message, player);
        List<Player> recipients = getNearbyPlayers(player);
        if (recipients.isEmpty()) {
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "No one heard you.");
        } else {
            for (Player recipient : recipients) {
                this.sendShowString(player, player.getInventory().getItemInMainHand(), fullDisplayName(player), message, recipient);
            }
        }
        sendToVanishedOps(player, ChatMechanics.fullDisplayName(player), message);
        PracticeServer.log.info(player.getDisplayName() + ": " + ChatColor.WHITE + message);
    }

    public static String fullDisplayName(Player player) {
        String playerPrefix = ChatMechanics.getDisplayNameFor(player, player);
        String name = "";
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
        if (guildPlayer.isInGuild()) {
            name = ChatColor.WHITE + "[" + GuildManager.getInstance().get(guildPlayer.getGuildName()).getTag() + "] " + String.valueOf(ChatMechanics.getTag(player) + playerPrefix);
        } else {
            name = String.valueOf(ChatMechanics.getTag(player) + playerPrefix);
        }
        return name;
    }

    private void processChatWithoutItem(Player player, String message) {
        String playerMessage = getPlayerMessage(player, player, message);
        player.sendMessage(playerMessage);
        List<Player> recipients = getNearbyPlayers(player);
        if (recipients.isEmpty()) {
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "No one heard you.");
        } else {
            for (Player recipient : recipients) {
                recipient.sendMessage(getPlayerMessage(player, recipient, message));
            }
        }
        sendToVanishedOps(player, ChatMechanics.fullDisplayName(player), message);
        PracticeServer.log.info(player.getDisplayName() + ": " + ChatColor.WHITE + message);
    }

    private List<Player> getNearbyPlayers(Player player) {
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player otherPlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (isPlayerNearby(player, otherPlayer)) {
                nearbyPlayers.add(otherPlayer);
            }
        }
        return nearbyPlayers;
    }

    private boolean isPlayerNearby(Player player, Player otherPlayer) {
        return otherPlayer.getLocation().getWorld() == player.getLocation().getWorld() && !VanishCommand.vanished.contains(otherPlayer.getName().toLowerCase()) && otherPlayer != player && otherPlayer.getLocation().distance(player.getLocation()) < 50.0;
    }

    private void sendToVanishedOps(Player player, String prefix, String message) {
        for (Player op : Bukkit.getServer().getOnlinePlayers()) {
            if (op.isOp() && VanishCommand.vanished.contains(op.getName().toLowerCase()) && op != player) {
                this.sendShowString(player, player.getInventory().getItemInMainHand(), prefix, message, op);
            }
        }
    }

    private String getPlayerMessage(Player sender, Player recipient, String message) {
        String playerPrefix = getDisplayNameFor(sender, recipient);
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(sender.getUniqueId());
        if (guildPlayer != null && guildPlayer.isInGuild()) {
            return ChatColor.WHITE + "[" + GuildManager.getInstance().get(guildPlayer.getGuildName()).getTag() + "] " + getTag(sender) + playerPrefix + ": " + ChatColor.WHITE + message;
        } else {
            return getTag(sender) + playerPrefix + ": " + ChatColor.WHITE + message;
        }
    }


    public static void sendShowString(Player sender, ItemStack itemStack, String prefix, String message, Player recipient) {
        if (isValidMessageAndItem(message, itemStack)) {
            String[] splitMessage = message.split("@i@");
            String before = splitMessage.length > 0 ? splitMessage[0] : "";
            String after = splitMessage.length > 1 ? splitMessage[1] : "";

            List<String> hoveredChat = getHoveredChat(itemStack);

            JSONMessage jsonMessage = new JSONMessage(prefix);
            jsonMessage.addText(": " + ChatColor.WHITE + before);
            jsonMessage.addHoverText(hoveredChat, getHoverText(itemStack));
            jsonMessage.addText(after);
            jsonMessage.sendToPlayer(recipient);
        }
    }

    private static boolean isValidMessageAndItem(String message, ItemStack itemStack) {
        return message.contains("@i@") && itemStack != null && itemStack.getType() != Material.AIR;
    }

    private static List<String> getHoveredChat(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> hoveredChat = new ArrayList<>();
        hoveredChat.add(meta.hasDisplayName() ? meta.getDisplayName() : itemStack.getType().name());
        if (meta.hasLore()) {
            hoveredChat.addAll(meta.getLore());
        }
        return hoveredChat;
    }

    private static String getHoverText(ItemStack itemStack) {
        return ChatColor.getLastColors(itemStack.getItemMeta().getDisplayName()) + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW";
    }

    private static String getPlayerName(Player sender, Player recipient) {
        String playerPrefix = ChatMechanics.getDisplayNameFor(sender, recipient);
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(sender.getUniqueId());
        if (guildPlayer.isInGuild()) {
            return ChatColor.WHITE + "[" + GuildManager.getInstance().get(guildPlayer.getGuildName()).getTag() + "] " + ChatMechanics.getTag(sender) + playerPrefix;
        } else {
            return ChatMechanics.getTag(sender) + playerPrefix;
        }
    }
}



