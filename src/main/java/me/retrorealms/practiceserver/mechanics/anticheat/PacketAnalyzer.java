package me.retrorealms.practiceserver.mechanics.anticheat;

import io.netty.channel.*;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class PacketAnalyzer {
    private final AdvancedAntiCheat antiCheat;

    public PacketAnalyzer(AdvancedAntiCheat antiCheat) {
        this.antiCheat = antiCheat;
    }

    public void injectPlayer(Player player) {
///*        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
//            @Override
//            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                analyzeIncomingPacket(player, msg);
//                super.channelRead(ctx, msg);
//            }
//
//            @Override
//            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//                analyzeOutgoingPacket(player, msg);
//                super.write(ctx, msg, promise);
//            }
//        };
//        try{
//        ChannelPipeline pipeline = ((io.netty.channel.Channel) getPlayerConnection(player).getClass().getField("networkManager").get(getPlayerConnection(player))).pipeline();
//        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);*/
//    }catch (Exception e) {
//        }
        }

    private void analyzeIncomingPacket(Player player, Object packet) {
        String packetName = packet.getClass().getSimpleName();
        ACPlayerData data = antiCheat.getPlayerData(player);

        switch (packetName) {
            case "PacketPlayInFlying":
                // Analyze player movement packets
                break;
            case "PacketPlayInUseEntity":
                // Analyze combat-related packets
                break;
            // Add more cases for other relevant packets
        }
    }

    private void analyzeOutgoingPacket(Player player, Object packet) {
        // Analyze outgoing packets if needed
    }

    private Object getPlayerConnection(Player player) {
        try {
            return player.getClass().getMethod("getHandle").invoke(player).getClass().getField("playerConnection").get(player.getClass().getMethod("getHandle").invoke(player));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void uninjectPlayer(Player player) {
//        try {
//            Channel channel = ((io.netty.channel.Channel) getPlayerConnection(player).getClass().getField("networkManager").get(getPlayerConnection(player)));
//            channel.eventLoop().submit(() -> {
//                channel.pipeline().remove(player.getName());
//                return null;
//            });
//        }catch (Exception e) {e.printStackTrace();}
    }
}
