package com.volmit.turntable.system;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.net.EngagementClosed;
import com.volmit.turntable.net.UpdateAP;
import com.volmit.turntable.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

import java.util.UUID;
import java.util.logging.Logger;

public class Member {
    public static final UUID SPEED_MODIFIER = UUID.nameUUIDFromBytes("turntable.speed".getBytes());
    public static final UUID ATTACK_SPEED_MODIFIER = UUID.nameUUIDFromBytes("turntable.attackspeed".getBytes());
    public static final UUID ATTACK_MODIFIER = UUID.nameUUIDFromBytes("turntable.attack".getBytes());
    public static final UUID FLY_SPEED_MODIFIER = UUID.nameUUIDFromBytes("turntable.flyspeed".getBytes());

    public double lx;
    public double ly;
    public double lz;
    public final Engagement engagement;
    public Entity entity;
    public int initiative;
    public float actionPoints;
    public boolean active = false;

    public Member(Engagement engagement, Entity entity, int initiative){
        this.engagement = engagement;
        this.entity = entity;
        this.initiative = initiative;
        this.actionPoints = Turntable.ACTION_POINTS;
        this.lx = entity.posX;
        this.ly = entity.posY;
        this.lz = entity.posZ;
    }

    public boolean consume(float ap) {
        if(!active){
            return false;
        }

        ap = Math.abs(ap);
        if(actionPoints >= ap) {
            actionPoints -= ap;

            if(actionPoints < 0.1f) {
                actionPoints = 0;
            }

            uap();
            return true;
        }

        return false;
    }

    public boolean isPlayer(){
        return entity instanceof EntityPlayer;
    }

    public boolean isAlive(){
        return !entity.isDead;
    }

    public void onJoin(){
        if(isPlayer()){
            entity.sendMessage(new TextComponentString("Engagement started!"));
        }

        freeze();
    }

    public void onLeave(){
        if(isPlayer()){
            entity.sendMessage(new TextComponentString("Engagement ended!"));
            CommonProxy.network.sendTo(new EngagementClosed.Packet(), (EntityPlayerMP) entity);
        }
        unfreeze();
    }

    public void onEndTurn() {
        if(entity instanceof EntityLiving)
        {
            EntityLiving l = (EntityLiving) entity;
            l.setRevengeTarget(null);
            l.tasks.taskEntries.clear();
            l.targetTasks.taskEntries.clear();
        }

        active = false;
        if(isPlayer()){
            entity.sendMessage(new TextComponentString("Turn ended!"));
        }

        actionPoints = 0;
        uap();
        freeze();
    }

    public void onOtherBeginTurn(Member member) {
        if(isPlayer()){
            entity.sendMessage(new TextComponentString("It's "+member.entity.getName()+"'s Turn!"));
        }
    }

    public void onBeginTurn() {
        actionPoints = Turntable.ACTION_POINTS;
        uap();
        if(isPlayer()){
            entity.sendMessage(new TextComponentString("It's Your Turn!"));
        }

        unfreeze();
        active = true;

        if(entity instanceof EntityAnimal){
            EntityAnimal a = (EntityAnimal) entity;
            a.setRevengeTarget(a);
            a.tasks.addTask(1, new EntityAIPanic(a, 3d));
        }
    }

    private void uap(){
        if(isPlayer()){
            UpdateAP.Packet a = new UpdateAP.Packet();
            a.ap = actionPoints;
            CommonProxy.network.sendTo(a, (EntityPlayerMP) entity);
        }
    }

    public boolean isLiving(){
        return entity instanceof EntityLivingBase;
    }

    public EntityLivingBase living(){
        return (EntityLivingBase) entity;
    }

    public void onOutOfAP(){
        freeze();
    }

    public void onTurnTick(int ticks) {
        double px = lx;
        double py = ly;
        double pz = lz;
        lx = entity.posX;
        ly = entity.posY;
        lz = entity.posZ;
        double distance = Math.sqrt(Math.pow(px - lx, 2) + Math.pow(py - ly, 2) + Math.pow(pz - lz, 2));

        if(distance > 0.01) {
            if(!consume((float)Turntable.MOVEMENT_COST)) {
                onOutOfAP();
            }
        }
    }

    public double distanceFromEngagement(){
        double[] d = engagement.avgPos();
        double dist = Math.pow(d[0] - entity.posX, 2) + Math.pow(d[1] - entity.posY, 2) + Math.pow(d[2] - entity.posZ, 2);

        for(Member i : engagement.members) {
            if(i.entity.getEntityId() == entity.getEntityId()) {
                continue;
            }

            d[0] = i.entity.posX;
            d[1] = i.entity.posY;
            d[2] = i.entity.posZ;
            double dx = Math.pow(d[0] - entity.posX, 2) + Math.pow(d[1] - entity.posY, 2) + Math.pow(d[2] - entity.posZ, 2);

            if(dx < dist){
                dist = dx;
            }
        }

        return dist;
    }

    public void onTick(int ticks) {

    }

    public void unfreeze(){
        if(isLiving()) {
            unfreeze(living());
        }
    }

    public static void unfreeze(EntityLivingBase living) {
            try
            {
                living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER);
            }

            catch(Throwable e)
            {

            }
            try
            {
                living.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).removeModifier(FLY_SPEED_MODIFIER);}

            catch(Throwable e)
            {

            }
            try
            {
                living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER);}

            catch(Throwable e)
            {

            }
            try
            {
                living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).removeModifier(ATTACK_MODIFIER);}

            catch(Throwable e)
            {

            }
    }

    public static AttributeModifier speedModifier(){
        return new AttributeModifier(SPEED_MODIFIER, "Turntable Speed", -1024D, 0);
    }

    public static AttributeModifier flySpeedModifier(){
        return new AttributeModifier(FLY_SPEED_MODIFIER, "Turntable Fly Speed", -1024D, 0);
    }

    public static AttributeModifier attackSpeedModifier(){
        return new AttributeModifier(ATTACK_SPEED_MODIFIER, "Turntable Attack Speed", -1024D, 0);
    }

    public static AttributeModifier attackModifier(){
        return new AttributeModifier(ATTACK_MODIFIER, "Turntable Attack Damage", -2048D, 0);
    }

    public void freeze() {
        if(entity instanceof EntityLiving)
        {
            EntityLiving l = (EntityLiving) entity;
            l.tasks.taskEntries.clear();
            l.targetTasks.taskEntries.clear();
        }

        if(isLiving())
        { try
            {
            if(!living().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(speedModifier())) {
                living().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                    .applyModifier(speedModifier());
            }
        }

        catch(Throwable e)
        {

        }

           try
           {
               if(!living().getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).hasModifier(flySpeedModifier())) {
                   living().getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED)
                       .applyModifier(flySpeedModifier());
               }
           }

           catch(Throwable e)
           {

           }
            try
            {
            if(!living().getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).hasModifier(attackSpeedModifier())) {
                living().getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED)
                    .applyModifier(attackSpeedModifier());
            } }

           catch(Throwable e)
        {

        }
        try
        {
            if(!living().getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).hasModifier(attackModifier())) {
                living().getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                    .applyModifier(attackModifier());
            } }

           catch(Throwable e)
    {

    }
        }
    }
}
