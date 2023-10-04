package com.volmit.turntable.net;

import com.volmit.turntable.old.api.TBState;
import com.volmit.turntable.old.capability.TurnBased;
import com.volmit.turntable.old.capability.TurnBasedData;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class EndTurnPacket implements IMessage {
    public EndTurnPacket() { }  // Empty constructor for Forge

    public EndTurnPacket(int entityId, TurnBased data) {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }
}
