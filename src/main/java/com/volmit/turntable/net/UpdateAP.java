package com.volmit.turntable.net;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.proxy.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateAP implements IMessageHandler<UpdateAP.Packet, IMessage> {
    @Override
    public IMessage onMessage(Packet message, MessageContext ctx) {
        ((ClientProxy) Turntable.proxy).onAPUpdate(message.ap);

        return null;
    }

    public static class Packet implements IMessage {
        public float ap;

        public Packet() {
        }  // Empty constructor for Forge

        @Override
        public void fromBytes(ByteBuf buf) {
            ap = buf.readFloat();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeFloat(ap);
        }
    }
}
