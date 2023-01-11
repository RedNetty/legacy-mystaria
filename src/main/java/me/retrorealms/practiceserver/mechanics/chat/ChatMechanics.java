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
        String tag = "";
        String rank = "";
        if (playerTags.containsKey(p.getUniqueId()) && playerTags.get(p.getUniqueId()) != ChatTag.DEFAULT) {
            tag = playerTags.get(p.getUniqueId()).getTag() + " ";
        }
        if (ModerationMechanics.getRank(p) != RankEnum.DEFAULT) {
            rank = ModerationMechanics.getRank(p).tag + " ";
        }
        return ChatColor.translateAlternateColorCodes('&', tag + rank);
    }


    public static String censorMessage(String msg) {
        String personal_msg = "";
        if (msg == null) {
            return "";
        }
        if (!msg.contains(" ")) {
            msg = String.valueOf(msg) + " ";
        }
        String[] split;
        for (int length = (split = msg.split(" ")).length, i = 0; i < length; ++i) {
            String s = split[i];
            for (final String bad : bad_words) {
                if (s.toLowerCase().contains(bad.toLowerCase())) {
                    int letters = bad.length();
                    String replace_char = "";
                    while (letters > 0) {
                        replace_char = String.valueOf(replace_char) + "*";
                        --letters;
                    }
                    int censor_start = 0;
                    int censor_end = 1;
                    censor_start = s.toLowerCase().indexOf(bad);
                    censor_end = censor_start + bad.length();
                    final String real_bad_word = s.substring(censor_start, censor_end);
                    s = s.replaceAll(real_bad_word, replace_char);
                }
            }
            personal_msg = String.valueOf(personal_msg) + s + " ";
        }
        if (personal_msg.endsWith(" ")) {
            personal_msg = personal_msg.substring(0, personal_msg.lastIndexOf(" "));
        }
        return personal_msg;
    }

    public static String getDisplayNameFor(Player p, Player sendee) {
        String nameColor = ChatColor.GRAY + "";
        if (!Alignments.chaotic.containsKey(p.getName()) && (!Alignments.neutral.containsKey(p.getName()))) {

            if (Buddies.getBuddies(sendee.getName()).contains(p.getName().toLowerCase())) {
                nameColor = ChatColor.GREEN + "";
            } else {
                nameColor = ChatColor.GRAY + "";
            }
            String endColor;
            if (ModerationMechanics.isStaff(p) || (ModerationMechanics.isDonator(p))) {
                endColor = ChatColor.WHITE + "";
            } else {
                endColor = ChatColor.GRAY + "";
            }
            return nameColor + p.getName() + endColor;
        } else {
            if (Alignments.neutral.containsKey(p.getName())) {
                nameColor = ChatColor.YELLOW + "";
            }
            if (Alignments.chaotic.containsKey(p.getName())) {
                nameColor = ChatColor.RED + "";
            }
            String endColor;
            if (ModerationMechanics.isStaff(p) || (ModerationMechanics.isDonator(p))) {
                endColor = ChatColor.WHITE + "";
            } else {
                endColor = ChatColor.GRAY + "";
            }
            return nameColor + p.getName() + endColor;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String s = e.getMessage().toLowerCase();
        if (s.startsWith("/")) {
            s = s.replace("/", "");
        }
        if (s.contains(" ")) {
            s = s.split(" ")[0];
        }
        if (s.equals("save-all") || s.equalsIgnoreCase("stack") || s.equals("stop") || s.equals("restart") || s.equals("reload") || s.equals("tpall") || s.equals("kill") || s.equals("mute")) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.WHITE + "Unknown command. View your Character Journal's Index for a list of commands.");
            return;
        }
        if (ModerationMechanics.isStaff(p)) {
            if (ModerationMechanics.getPerms().containsKey(p.getUniqueId())) {
                ModerationMechanics.getPerms().remove(p.getUniqueId());
            }
            PermissionAttachment attachment = p.addAttachment(PracticeServer.getInstance());
            ModerationMechanics.getPerms().put(p.getUniqueId(), attachment);
            PermissionAttachment playerPerms = ModerationMechanics.getPerms().get(p.getUniqueId());
            for (String perm : ModerationMechanics.getPermsForRank(ModerationMechanics.getRank(p))) {
                playerPerms.setPermission(perm, true);
            }
        }
        if (false) {//!p.isOp() && !isStaff(p)) {
            RankEnum rankEnum = ModerationMechanics.getRank(p);
            List<String> allowedCommands = Arrays.asList("guildshow",
                    "ginfo",
                    "gwho",
                    "guildpromote",
                    "gpromote",
                    "gdemote",
                    "guilddemote",
                    "orbs",
                    "guildwho",
                    "guildinfo",
                    "gshow",
                    "guildquit",
                    "gquit",
                    "guildcreate",
                    "gaccept",
                    "guildaccept",
                    "gcreate",
                    "ginvite",
                    "guildinvite",
                    "gkick",
                    "guildkick",
                    "guilddecline",
                    "gdecline",
                    "guild",
                    "g");
            //if (rankEnum == RankEnum.PMOD) {
            //  if (!(s.equals("ban") || s.equals("tempban") || s.equals("history") || s.equals("ipmute") ||  s.equals("staffhistory") || s.equals("alts") || s.equals("pet") || s.equals("unban") || s.equals("unmute") || s.equals("tempmute") || s.equals("kick") || s.equals("patch") || s.equals("mount") || s.equalsIgnoreCase("shard") || s.equals("tags") || s.equals("mount") || s.equals("roll") || s.equals("toggletrading") || s.equals("sc") || s.equals("gl") || s.equals("toggle") || s.equals("toggles") || s.equals("togglepvp") || s.equals("togglechaos") || s.equals("toggledebug") || s.equals("Debug") || s.equals("toggleff") || s.equals("add") || s.equals("del") || s.equals("delete") || s.equals("message") || s.equals("msg") || s.equals("m") || s.equals("whisper") || s.equals("w") || s.equals("tell") || (allowedCommands.contains(s.toLowerCase())) || s.equals("t") || s.equals("reply") || s.equals("r") || s.equals("logout") || s.equals("sync") || s.equals("reboot") || s.equals("pinvite") || s.equals("paccept") || s.equals("pquit") || s.equals("pkick") || s.equals("pdecline") || s.equals("p") || s.equals("psban") || s.equals("psunban") || s.equals("psmute") || s.equals("psunmute") || s.equals("report") || s.equals("editreport"))) {
            //    e.setCancelled(true);
            //       p.sendMessage(ChatColor.WHITE + "Unknown command. View your Character Journal's Index for a list of commands.");
            // }
            if (ModerationMechanics.isDonator(p)) {
                if (!(s.equals("togglegems") || s.equals("lootbuff") || s.equals("pet") || s.equalsIgnoreCase("shard") || s.equals("market") || s.equals("patch") || s.equals("mount") || s.equals("tags") || s.equals("mount") || s.equals("roll") || s.equals("toggletrading") || s.equals("gl") || s.equals("toggle") || s.equals("toggles") || s.equals("togglepvp") || s.equals("togglechaos") || s.equals("toggledebug") || s.equals("Debug") || s.equals("toggleff") || s.equals("add") || s.equals("del") || s.equals("delete") || s.equals("message") || s.equals("msg") || s.equals("m") || s.equals("whisper") || s.equals("w") || s.equals("tell") || s.equals("t") || (allowedCommands.contains(s.toLowerCase())) || s.equals("reply") || s.equals("r") || s.equals("logout") || s.equals("sync") || s.equals("reboot") || s.equals("pinvite") || s.equals("paccept") || s.equals("pquit") || s.equals("pkick") || s.equals("pdecline") || s.equals("p") || s.equals("toggletrail") || s.equals("g") || s.equals("guilds") || s.equals("guilds") || s.equals("abandon") || s.equals("create") || s.equals("join") || s.equals("gi") || s.equals("report") || s.equals("editreport"))) {
                    e.setCancelled(true);
                    p.sendMessage(ChatColor.WHITE + "Unknown command. View your Character Journal's Index for a list of commands.");
                }
            } else if (!(s.equals("patch") || s.equals("pet") || s.equalsIgnoreCase("shard") || s.equals("mount") || s.equals("tags") || s.equals("mount") || s.equals("roll") || s.equals("toggletrading") || s.equals("gl") || s.equals("toggle") || s.equals("toggles") || s.equals("togglepvp") || s.equals("togglechaos") || s.equals("toggledebug") || s.equals("Debug") || s.equals("toggleff") || s.equals("add") || s.equals("del") || s.equals("delete") || s.equals("message") || s.equals("msg") || s.equals("m") || s.equals("whisper") || s.equals("w") || s.equals("tell") || s.equals("t") || (allowedCommands.contains(s.toLowerCase())) || s.equals("reply") || s.equals("r") || s.equals("logout") || s.equals("sync") || s.equals("reboot") || s.equals("pinvite") || s.equals("paccept") || s.equals("pquit") || s.equals("pkick") || s.equals("pdecline") || s.equals("p") || s.equals("g") || s.equals("guilds") || s.equals("guilds") || s.equals("abandon") || s.equals("create") || s.equals("guildcreate") || s.equals("gcreate") || s.equals("join") || s.equals("gi") || s.equals("report") || s.equals("editreport"))) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.WHITE + "Unknown command. View your Character Journal's Index for a list of commands.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatTabComplete(PlayerChatTabCompleteEvent e) {
        Player p = e.getPlayer();
        if (e.getChatMessage() != null && e.getChatMessage().length() > 0) {
            p.closeInventory();
            p.performCommand("gl " + e.getChatMessage());
            PracticeServer.log.info("<G> " + e.getPlayer().getName() + ": " + e.getChatMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (!e.isCancelled()) {
            Player p = e.getPlayer();
            e.setCancelled(true);
            if(muted.containsKey(p)){
                if(!GemGambling.inPlayGem.contains(p.getName()) && !OrbGambling.chatHandling.contains(p.getUniqueId()) && !OreMerchant.chatInteractive.containsKey(p.getUniqueId()) && !Banks.withdraw.contains(p.getName())){
                    p.sendMessage(ChatColor.RED + "You are currently muted");
                    if(ChatMechanics.muted.get(p) > 0) {
                        Integer minutes = ChatMechanics.muted.get(p) / 60;
                        p.sendMessage(ChatColor.RED + "Your tmute expires in " + minutes.toString() + " minutes.");
                    }else{
                        p.sendMessage(ChatColor.RED + "Your mute WILL NOT expire.");
                    }
                    return;
                }
            }
            String message = e.getMessage();
            if (message.contains("@i@") && p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType() != Material.AIR) {
                this.sendShowString(p, p.getInventory().getItemInMainHand(), getTag(p), message, p);
                ArrayList<Player> to_send = new ArrayList<Player>();
                for (Player pl2 : Bukkit.getServer().getOnlinePlayers()) {

                    if (pl2.getLocation().getWorld() != p.getLocation().getWorld()) {
                        continue;
                    }

                    if (VanishCommand.vanished.contains(pl2.getName().toLowerCase()) || pl2 == null || pl2 == p || pl2.getLocation().distance(p.getLocation()) >= 50.0)
                        continue;
                    to_send.add(pl2);
                }
                if (to_send.size() <= 0) {
                    p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "No one heard you.");
                } else {
                    for (Player pl2 : to_send) {
                        this.sendShowString(p, p.getInventory().getItemInMainHand(), getTag(p), message, pl2);
                    }
                }
                for (Player op : Bukkit.getServer().getOnlinePlayers()) {
                    if (!op.isOp() || !VanishCommand.vanished.contains(op.getName().toLowerCase()) || op == p)
                        continue;
                    this.sendShowString(p, p.getInventory().getItemInMainHand(), getTag(p) + p.getDisplayName(), message, op);
                }
                PracticeServer.log.info(String.valueOf(p.getDisplayName()) + ": " + ChatColor.WHITE + message);
            } else {

                //      p.sendMessage(String.valueOf(getTag(p) + p.getDisplayName()) + ": " + ChatColor.WHITE + message);
                String playerPrefix = getDisplayNameFor(p, p);

                GuildPlayer guildPlayer = GuildPlayers.getInstance().get(p.getUniqueId());
                if (guildPlayer.isInGuild()) {
                    p.sendMessage(ChatColor.WHITE + "[" + GuildManager.getInstance().get(guildPlayer.getGuildName()).getTag() + "] " + String.valueOf(getTag(p) + playerPrefix) + ": " + ChatColor.WHITE + message);
                } else {
                    p.sendMessage(String.valueOf(getTag(p) + playerPrefix) + ": " + ChatColor.WHITE + message);

                }
                ArrayList<Player> to_send = new ArrayList<Player>();
                for (Player pl3 : Bukkit.getServer().getOnlinePlayers()) {
                    if (pl3.getLocation().getWorld() != p.getLocation().getWorld()) {
                        continue;
                    }

                    if (VanishCommand.vanished.contains(pl3.getName().toLowerCase()) || pl3 == null || pl3 == p || pl3.getLocation().distance(p.getLocation()) >= 50.0)
                        continue;
                    to_send.add(pl3);
                }
                if (to_send.size() <= 0) {
                    p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "No one heard you.");
                } else {
                    for (Player pl3 : to_send) {
                        String playerPrefix1 = getDisplayNameFor(p, pl3);


                        if (guildPlayer.isInGuild()) {
                            pl3.sendMessage(ChatColor.WHITE + "[" + GuildManager.getInstance().get(guildPlayer.getGuildName()).getTag() + "] " + String.valueOf(getTag(p) + playerPrefix1) + ": " + ChatColor.WHITE + message);
                        } else {
                            pl3.sendMessage(String.valueOf(getTag(p) + playerPrefix1) + ": " + ChatColor.WHITE + message);

                        }
                    }
                }
                for (Player op : Bukkit.getServer().getOnlinePlayers()) {
                    if (!op.isOp() || !VanishCommand.vanished.contains(op.getName().toLowerCase()) || op == p)
                        continue;
                    op.sendMessage(String.valueOf(getTag(p) + p.getDisplayName()) + ": " + ChatColor.WHITE + message);
                }
                PracticeServer.log.info(String.valueOf(p.getDisplayName()) + ": " + ChatColor.WHITE + message);
            }
        }

    }

    public static void sendShowStringGlobal(Player sender, ItemStack is, String prefix, String message, Player p) {
        if (message.contains("@i@") && is != null && is.getType() != Material.AIR) {
            String[] split = message.split("@i@");
            String after = "";
            String before = "";
            if (split.length > 0)
                before = split[0];
            if (split.length > 1)
                after = split[1];

            ItemStack stack = is;

            List<String> hoveredChat = new ArrayList<>();
            ItemMeta meta = stack.getItemMeta();
            hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
            if (meta.hasLore())
                hoveredChat.addAll(meta.getLore());
            JSONMessage normal = new JSONMessage(prefix + ": ");
            before = ChatColor.WHITE + before;
            normal.addText(before);
            normal.addHoverText(hoveredChat, ChatColor.getLastColors(is.getItemMeta().getDisplayName()) + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
            normal.addText(after);
            normal.sendToPlayer(p);
        }
    }

    public static void sendShowString(Player sender, ItemStack is, String prefix, String message, Player p) {
        if (message.contains("@i@") && is != null && is.getType() != Material.AIR) {
            String name;
            String playerPrefix = ChatMechanics.getDisplayNameFor(sender, p);
            GuildPlayer guildPlayer = GuildPlayers.getInstance().get(sender.getUniqueId());
            if (guildPlayer.isInGuild()) {
                name = ChatColor.WHITE + "[" + GuildManager.getInstance().get(guildPlayer.getGuildName()).getTag() + "] " + String.valueOf(ChatMechanics.getTag(sender) + playerPrefix);
            } else {
                name = String.valueOf(ChatMechanics.getTag(sender) + playerPrefix);
            }
            String[] split = message.split("@i@");
            String after = "";
            String before = "";
            if (split.length > 0)
                before = split[0];
            if (split.length > 1)
                after = split[1];

            ItemStack stack = is;

            List<String> hoveredChat = new ArrayList<>();
            ItemMeta meta = stack.getItemMeta();
            hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
            if (meta.hasLore())
                hoveredChat.addAll(meta.getLore());
            JSONMessage normal = new JSONMessage(name);

            before = ": " + ChatColor.WHITE + before;
            normal.addText(before + "");
            normal.addHoverText(hoveredChat, ChatColor.getLastColors(is.getItemMeta().getDisplayName()) + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
            normal.addText(after);
            normal.sendToPlayer(p);
        }
    }
}



