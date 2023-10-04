package com.volmit.turntable.old.net;

import com.volmit.turntable.old.api.TBState;
import com.volmit.turntable.old.capability.TurnBased;
import com.volmit.turntable.old.capability.TurnBasedData;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SyncTurnBasedCapabilityPacket implements IMessage {
    public int entityId;
    public TurnBased data;

    public SyncTurnBasedCapabilityPacket() { }  // Empty constructor for Forge

    public SyncTurnBasedCapabilityPacket(int entityId, TurnBased data) {
        this.entityId = entityId;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        data = new TurnBasedData();
        data.setTBState(TBState.values()[buf.readShort()]);
        data.setActionPoints(buf.readFloat());
        data.setDamageable(buf.readBoolean());
        data.setInitiative(buf.readShort());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeShort(data.getTBState().ordinal());
        buf.writeFloat(data.getActionPoints());
        buf.writeBoolean(data.isDamageable());
        buf.writeShort(data.getInitiative());
    }
}
