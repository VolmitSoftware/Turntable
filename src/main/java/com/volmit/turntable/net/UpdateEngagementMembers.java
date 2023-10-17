package com.volmit.turntable.net;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.proxy.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

        public Packet() {
        }  // Empty constructor for Forge

        @Override
        public void fromBytes(ByteBuf buf) {
           try
           {
               int size = buf.readShort();
               turnOrder = new ArrayList<>();
               for (int i = 0; i < size; i++) {
                   try {
                       turnOrder.add(Minecraft.getMinecraft().world.getEntityByID(buf.readInt()));
                   } catch (Throwable e) {
                       e.printStackTrace();
                   }
               }

               if(ClientProxy.targetEntity != null) {
                   ClientProxy.targetEntity.setGlowing(false);
               }

               if(turnOrder != null && !turnOrder.isEmpty()){
                   if(!Minecraft.getMinecraft().player.getUniqueID().equals(turnOrder.get(0).getUniqueID())){
                       ClientProxy.targetEntity = turnOrder.get(0);

                       if(ClientProxy.targetEntity != null && !ClientProxy.targetEntity.isDead) {
                           ClientProxy.targetEntity.setGlowing(true);
                       }
                   }
                   else{
                       ClientProxy.targetEntity = null;
                   }
               }else {
                   ClientProxy.targetEntity = null;
               }
           }

           catch(Throwable e){

           }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeShort(turnOrder.size());
            for (Entity i : turnOrder) {
                buf.writeInt(i.getEntityId());
            }
        }
    }

}
