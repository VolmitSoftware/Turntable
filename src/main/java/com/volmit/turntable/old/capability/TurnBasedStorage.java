package com.volmit.turntable.old.capability;

import com.volmit.turntable.old.api.TBState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TurnBasedStorage implements Capability.IStorage<TurnBased> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<TurnBased> capability, TurnBased instance, EnumFacing side) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("tbs", instance.getTBState().name());
        compound.setFloat("ap", instance.getActionPoints());
        return compound;
    }

    @Override
    public void readNBT(Capability<TurnBased> capability, TurnBased instance, EnumFacing side, NBTBase nbt) {
        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            instance.setTBState(TBState.valueOf(compound.getString("tbs")));
            instance.setActionPoints(compound.getFloat("ap"));
        }
    }
}
