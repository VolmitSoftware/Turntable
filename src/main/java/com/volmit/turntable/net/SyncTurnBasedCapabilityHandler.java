package com.volmit.turntable.net;

import com.volmit.turntable.capability.TurnBased;
import com.volmit.turntable.capability.TurnBasedProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncTurnBasedCapabilityHandler implements IMessageHandler<SyncTurnBasedCapabilityPacket, IMessage> {
    @Override
    public IMessage onMessage(SyncTurnBasedCapabilityPacket message, MessageContext ctx) {
       Minecraft.getMinecraft().addScheduledTask(() -> {
           System.out.println("ON MESSAGE");
           World world = Minecraft.getMinecraft().world;
           Entity entity = world.getEntityByID(message.entityId);

           if (entity != null) {
               TurnBased tbServer = message.data;
               TurnBased tbClient = entity.getCapability(TurnBasedProvider.TBC, null);

               if(tbClient != null) {
                   tbClient.setTBState(tbServer.getTBState());
                   tbClient.setActionPoints(tbServer.getActionPoints());
                   tbClient.setDamageable(tbServer.isDamageable());
                   tbClient.setInitiative(tbServer.getInitiative());

                   System.out.println("UTB " + entity.getClass().getSimpleName());
                   if(entity instanceof EntityPlayer) {
                       if(entity.getUniqueID().equals(Minecraft.getMinecraft().player.getUniqueID())) {
                           System.out.println("Update TB State: " + tbServer.getTBState().name() + " AP: " + tbServer.getActionPoints() + " D: " + tbServer.isDamageable() + " I: " + tbServer.getInitiative() + "");
                       }
                   }
               }
               else {
                   System.out.println("TB IS NULL for " + entity.getClass().getSimpleName() + " " + entity.getUniqueID());
               }
           }
       });
        return null;
    }
}
