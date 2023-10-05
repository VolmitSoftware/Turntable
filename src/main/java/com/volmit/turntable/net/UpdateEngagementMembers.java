package com.volmit.turntable.net;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.proxy.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.reflect.internal.Trees;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class UpdateEngagementMembers implements IMessageHandler<UpdateEngagementMembers.Packet, IMessage> {
    @Override
    public IMessage onMessage(Packet message, MessageContext ctx) {
        ((ClientProxy) Turntable.proxy).onEngagementUpdate(message.turnOrder);

        return null;
    }

    public static class Packet implements IMessage {
        public List<Entity> turnOrder;

        public Packet() { }  // Empty constructor for Forge

        @Override
        public void fromBytes(ByteBuf buf) {
            int size = buf.readShort();
            turnOrder = new ArrayList<>();
            for(int i = 0; i < size; i++) {
              try
              {
                  turnOrder.add(Minecraft.getMinecraft().world.getEntityByID(buf.readInt()));
              }

              catch(Throwable e)
              {
                  e.printStackTrace();
              }
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeShort(turnOrder.size());
            for(Entity i : turnOrder) {
                buf.writeInt(i.getEntityId());
            }
        }
    }

}
