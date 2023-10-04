package com.volmit.turntable.old.api;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.old.capability.TurnBased;
import com.volmit.turntable.old.capability.TurnBasedProvider;
import com.volmit.turntable.old.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;

public class Member {
    public final Entity entity;

    public Member(Entity entity) {
        this.entity = entity;
    }

    public void onEnter(Encounter encounter){
        if(entity instanceof EntityPlayer) {
            entity.sendMessage(ITextComponent.Serializer.jsonToComponent("{\"text\":\"COMBAT!\",\"color\":\"dark_red\"}"));
        }
    }

    public void onEndTurn(Encounter encounter){
        if(entity instanceof EntityPlayer) {
            entity.sendMessage(ITextComponent.Serializer.jsonToComponent("{\"text\":\"It is not your turn.\",\"color\":\"red\"}"));
        }

        if(entity instanceof EntityLiving){
            EntityLiving l = (EntityLiving) entity;
            l.setRevengeTarget(null);
            l.tasks.taskEntries.clear();
            l.targetTasks.taskEntries.clear();
        }
    }

    public void onStartTurn(Encounter encounter){
        applyFire();
        applyPotions();

        if(entity instanceof EntityPlayer) {
            entity.sendMessage(ITextComponent.Serializer.jsonToComponent("{\"text\":\"It is your turn.\",\"color\":\"green\"}"));
        }
    }

    public void onLeave(Encounter encounter){
        if(entity instanceof EntityPlayer) {
            entity.sendMessage(ITextComponent.Serializer.jsonToComponent("{\"text\":\"Encounter Ended\",\"color\":\"aqua\"}"));
        }
    }

    public TurnBased getTB(){
        TurnBased tb = entity.getCapability(TurnBasedProvider.TBC, null);

        if(tb == null)
        {
            System.out.println("NO TB FOR " + entity);
        }

        return tb;
    }

    public void applyFire(){
        int fire = EntityUtil.getFireTicks(entity);

        if(fire > 0) {
            EntityUtil.setFireTicks(entity, Math.max(0, fire - Turntable.TURN_TIME));
        }
    }

    public void applyPotions(){
        if(entity instanceof EntityLiving) {
            EntityLiving l = (EntityLiving) entity;

            for(PotionEffect i : new ArrayList<>(l.getActivePotionEffects())) {
                int duration = EntityUtil.getPotionEffectDuration(i) - Turntable.TURN_TIME;

                if(duration <= 0) {
                    l.removePotionEffect(i.getPotion());
                }

                i.getPotion().performEffect(l, i.getAmplifier());
            }
        }
    }

    public void onTick() {
        if(entity instanceof EntityAnimal){
            EntityAnimal a = (EntityAnimal) entity;
            a.setRevengeTarget(a);
            a.tasks.addTask(1, new EntityAIPanic(a, 3d));

        }
    }
}
