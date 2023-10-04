package com.volmit.turntable.net;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.system.Engagement;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EndTurnHandler implements IMessageHandler<EndTurnPacket, IMessage> {
    @Override
    public IMessage onMessage(EndTurnPacket message, MessageContext ctx) {
        Turntable.logger.info("EndTurnHandler onMessage");
        Engagement e = Turntable.proxy.host.getEngagement(ctx.getServerHandler().player);

        if(e != null && e.getActiveMember().entity.getUniqueID().equals(ctx.getServerHandler().player.getUniqueID())) {
            e.nextTurn();
        }
        else {
            Turntable.logger.error("Invalid end turn packet");
        }
        return null;
    }
}
