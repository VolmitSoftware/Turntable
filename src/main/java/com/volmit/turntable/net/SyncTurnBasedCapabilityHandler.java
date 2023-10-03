package com.volmit.turntable.net;

import com.volmit.turntable.capability.TurnBased;
import com.volmit.turntable.capability.TurnBasedProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncTurnBasedCapabilityHandler implements IMessageHandler<SyncTurnBasedCapabilityPacket, IMessage> {
    @Override
    public IMessage onMessage(SyncTurnBasedCapabilityPacket message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            World world = Minecraft.getMinecraft().world;
            Entity entity = world.getEntityByID(message.entityId);
            if (entity != null) {
                TurnBased tbServer = message.data;
                TurnBased tbClient = entity.getCapability(TurnBasedProvider.TBC, null);

                if(tbClient != null) {
                    tbClient.setTBState(tbServer.getTBState());
                    tbClient.setActionPoints(tbServer.getActionPoints());
                }
            }
        });
        return null;
    }
}
