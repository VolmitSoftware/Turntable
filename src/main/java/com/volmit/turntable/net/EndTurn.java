package com.volmit.turntable.net;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.system.Engagement;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EndTurn implements IMessageHandler<EndTurn.Packet, IMessage> {
    @Override
    public IMessage onMessage(Packet message, MessageContext ctx) {
        Engagement e = Turntable.proxy.host.getEngagement(ctx.getServerHandler().player);

        if(e != null && e.getActiveMember().entity.getUniqueID().equals(ctx.getServerHandler().player.getUniqueID())) {
            e.nextTurn();
        }

        return null;
    }

    public static class Packet implements IMessage {
        public Packet() { }  // Empty constructor for Forge

        @Override
        public void fromBytes(ByteBuf buf) {

        }

        @Override
        public void toBytes(ByteBuf buf) {

        }
    }

}
