package com.volmit.turntable.capability;

import com.volmit.turntable.api.TBState;
import com.volmit.turntable.net.SyncTurnBasedCapabilityHandler;
import com.volmit.turntable.net.SyncTurnBasedCapabilityPacket;
import com.volmit.turntable.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraftforge.fml.common.network.PacketLoggingHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface TurnBased {
    TBState getTBState();
    void setTBState(TBState state);

    float getActionPoints();
    void setActionPoints(float actionPoints);

    boolean isDamageable();
    void setDamageable(boolean damageable);

    int getInitiative();
    void setInitiative(int initiative);

    default int calculateInitiative(Entity entity) {
        int base = 100;

        if(entity instanceof EntityLivingBase){
            base -= (int) (((EntityLivingBase)entity).getMaxHealth() / 10);
        }

        if(entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHealth() <  (((EntityLivingBase)entity).getHealth())){
            base -= 10;
        }

        if(entity instanceof EntityAnimal){
            base += 100;
        }

        if(entity instanceof EntityMob) {
            base -= 25;
        }

        if(entity instanceof EntityWither || entity instanceof EntityDragon) {
            base += 1000;
        }

        return base;
    }

    default void updateTrackers(Entity entity) {
        if(entity != null && entity.world != null && !entity.world.isRemote) {
            SyncTurnBasedCapabilityPacket p = new SyncTurnBasedCapabilityPacket();
            p.entityId = entity.getEntityId();
            p.data = this;
            CommonProxy.network.sendToAllTracking(p, entity);
            System.out.println("Update TB State: " + getTBState().name() + " AP: " + getActionPoints() + " D: " + isDamageable() + " I: " + getInitiative() + "" + " for " + entity.getClass().getSimpleName());
        }
        else {
            System.out.println("No update!" + entity);
        }
    }

    default boolean isFrozen(){
        return getTBState() == TBState.FROZEN;
    }
}
