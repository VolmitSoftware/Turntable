package com.volmit.turntable.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;

public class TurnBasedProvider implements ICapabilitySerializable<NBTTagCompound> {
    @CapabilityInject(TurnBased.class)
    public static final Capability<TurnBased> TBC = null;
    private TurnBased instance = TBC.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == TBC;
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return capability == TBC ? TBC.cast(instance) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) TBC.getStorage().writeNBT(TBC, instance, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        TBC.getStorage().readNBT(TBC, instance, null, nbt);
    }
}
