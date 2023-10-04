package com.volmit.turntable.system;

import com.volmit.turntable.Turntable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

import java.util.UUID;

public class Member {
    public static final UUID SPEED_MODIFIER = UUID.nameUUIDFromBytes("turntable.speed".getBytes());

    public final Engagement engagement;
    public Entity entity;
    public int initiative;
    public float actionPoints;

    public Member(Engagement engagement, Entity entity, int initiative){
        this.engagement = engagement;
        this.entity = entity;
        this.initiative = initiative;
        this.actionPoints = Turntable.ACTION_POINTS;
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
        }
        unfreeze();
    }

    public void onEndTurn() {
        if(isPlayer()){
            entity.sendMessage(new TextComponentString("Turn ended!"));
        }

        entity.setGlowing(false);
        freeze();
    }

    public void onOtherBeginTurn(Member member) {
        if(isPlayer()){
            entity.sendMessage(new TextComponentString("It's "+member.entity.toString()+"'s Turn!"));
        }
    }

    public void onBeginTurn() {
        if(isPlayer()){
            entity.sendMessage(new TextComponentString("It's Your Turn!"));
        }

        entity.setGlowing(true);
        unfreeze();
    }

    public boolean isLiving(){
        return entity instanceof EntityLivingBase;
    }

    public EntityLivingBase living(){
        return (EntityLivingBase) entity;
    }

    public void onTurnTick(int ticks) {

    }

    public void onTick(int ticks) {

    }

    public void unfreeze(){
        if(isLiving()) {
            living().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER);
        }
    }

    public void freeze() {
        if(isLiving()) {
            AttributeModifier mod = new AttributeModifier(SPEED_MODIFIER, "Turntable Speed", -1024D, 0);
            if(!living().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(mod)) {
                living().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                    .applyModifier(mod);
            }
        }
        else {
            Turntable.logger.info("Cannot freeze non-living entity: "+entity);
        }
    }
}
