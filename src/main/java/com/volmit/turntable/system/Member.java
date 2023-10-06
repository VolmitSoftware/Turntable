package com.volmit.turntable.system;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.net.EngagementClosed;
import com.volmit.turntable.net.UpdateAP;
import com.volmit.turntable.proxy.CommonProxy;
import com.volmit.turntable.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Iterator;
import java.util.UUID;

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
    public boolean environmentDamageGate = false;
    public float jumpFactor = 0.2f;

    public Member(Engagement engagement, Entity entity, int initiative) {
        this.engagement = engagement;
        this.entity = entity;
        this.initiative = initiative;
        this.actionPoints = Turntable.AP_PER_TURN;
        this.lx = entity.posX;
        this.ly = entity.posY;
        this.lz = entity.posZ;
    }

    public boolean consume(float ap) {
        if (!active) {
            return false;
        }

        ap = Math.abs(ap);
        if (ap > 0) {
            actionPoints -= ap;

            if (actionPoints < 0.1f) {
                actionPoints = 0;
            }

            uap();
            return true;
        }

        return false;
    }

    public boolean isPlayer() {
        return entity instanceof EntityPlayer;
    }

    public boolean isAlive() {
        return !entity.isDead;
    }

    public void onJoin() {
        if (isPlayer()) {
            entity.sendMessage(new TextComponentString("Engagement started!"));
        }

        freeze();
    }

    public void onLeave() {
        if (isPlayer()) {
            entity.sendMessage(new TextComponentString("Engagement ended!"));
            CommonProxy.network.sendTo(new EngagementClosed.Packet(), (EntityPlayerMP) entity);
        }
        unfreeze();
        environmentDamageGate = false;
        active = false;
    }

    public void onEndTurn() {
        if (entity instanceof EntityLiving) {
            EntityLiving l = (EntityLiving) entity;
            l.setRevengeTarget(null);
            l.tasks.taskEntries.clear();
            l.targetTasks.taskEntries.clear();
        }

        active = false;
        if (isPlayer()) {
            entity.sendMessage(new TextComponentString("Turn ended!"));
        }

        actionPoints = 0;
        uap();
        freeze();
        lx = entity.posX;
        ly = entity.posY;
        lz = entity.posZ;
    }

    public void onOtherBeginTurn(Member member) {
        if (isPlayer()) {
            entity.sendMessage(new TextComponentString("It's " + member.entity.getName() + "'s Turn!"));
        }
    }

    public void onBeginTurn() {
        actionPoints = Turntable.AP_PER_TURN;
        uap();
        if (isPlayer()) {
            entity.sendMessage(new TextComponentString("It's Your Turn!"));
        }

        lx = entity.posX;
        ly = entity.posY;
        lz = entity.posZ;
        unfreeze();
        active = true;

        if (entity instanceof EntityAnimal) {
            EntityAnimal a = (EntityAnimal) entity;
            a.setRevengeTarget(a);
            a.tasks.addTask(1, new EntityAIPanic(a, 3d));
        }

        triggerEffects();
        environmentDamageGate = true;
    }

    private void uap() {
        if (isPlayer()) {
            UpdateAP.Packet a = new UpdateAP.Packet();
            a.ap = actionPoints;
            CommonProxy.network.sendTo(a, (EntityPlayerMP) entity);
        }
    }

    public boolean isLiving() {
        return entity instanceof EntityLivingBase;
    }

    public EntityLivingBase living() {
        return (EntityLivingBase) entity;
    }

    public void onOutOfAP() {
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

        if (distance > 0.01) {
            if (!consume(Turntable.AP_COST_MOVEMENT)) {
                onOutOfAP();
            }
        }

        if (actionPoints <= 0 && active) {
            freeze();
        }
    }

    public double distanceFromEngagement() {
        double[] d = engagement.avgPos();
        double dist = Math.pow(d[0] - entity.posX, 2) + Math.pow(d[1] - entity.posY, 2) + Math.pow(d[2] - entity.posZ, 2);

        for (Member i : engagement.members) {
            if (i.entity.getEntityId() == entity.getEntityId()) {
                continue;
            }

            d[0] = i.entity.posX;
            d[1] = i.entity.posY;
            d[2] = i.entity.posZ;
            double dx = Math.pow(d[0] - entity.posX, 2) + Math.pow(d[1] - entity.posY, 2) + Math.pow(d[2] - entity.posZ, 2);

            if (dx < dist) {
                dist = dx;
            }
        }

        return dist;
    }

    public void onTick(int ticks) {
        if (!active) {
            onInactiveTick(ticks);
        }

        pausePotionEffects();
        pauseFire();
    }

    public void triggerEffects() {
        triggerFire();
        triggerPotionEffects();
    }

    public boolean shouldTakeDamage(LivingAttackEvent src) {
        if (src.getSource().getTrueSource() == null) {
            if (environmentDamageGate) {
                environmentDamageGate = false;
                return true;
            }

            return false;
        }

        return true;
    }

    public void triggerFire() {
        EntityUtil.setFireTicks(entity, Math.max(EntityUtil.getFireTicks(entity) - Turntable.TURN_TIME, 0));

        if (isLiving() && EntityUtil.getFireTicks(entity) > 0) {
            environmentDamageGate = true;
            living().attackEntityFrom(DamageSource.IN_FIRE, 1f);
        }
    }

    public void triggerPotionEffects() {
        if (isLiving()) {
            Iterator<PotionEffect> it = living().getActivePotionEffects().iterator();
            while (it.hasNext()) {
                PotionEffect i = it.next();
                int duration = i.getDuration() - Turntable.TURN_TIME;

                if (duration <= 0) {
                    it.remove();
                    continue;
                }

                EntityUtil.setPotionEffectDuration(i, duration);
                environmentDamageGate = true;
                i.performEffect(living());
                environmentDamageGate = false;
            }
        }
    }

    public void pausePotionEffects() {
        if (isLiving()) {
            for (PotionEffect i : living().getActivePotionEffects()) {
                EntityUtil.setPotionEffectDuration(i, i.getDuration() + 1);
            }
        }
    }

    public void pauseFire() {
        if (isLiving()) {
            int fire = EntityUtil.getFireTicks(entity);

            if (fire > 0) {
                EntityUtil.setFireTicks(entity, fire + 1);
            }
        }
    }

    public void onInactiveTick(int ticks) {
        if (entity instanceof EntityLiving) {
            EntityLiving l = (EntityLiving) entity;
            l.targetTasks.taskEntries.clear();
            l.tasks.taskEntries.clear();
        }

        if (isLiving()) {
            living().setJumping(false);
        }
    }

    public void unfreeze() {
        if (isLiving()) {
            unfreeze(living());
            living().jumpMovementFactor = jumpFactor;
        }
    }

    public static void unfreeze(EntityLivingBase living) {
        try {
            living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER);
        } catch (Throwable e) {

        }
        try {
            living.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).removeModifier(FLY_SPEED_MODIFIER);
        } catch (Throwable e) {

        }
        try {
            living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER);
        } catch (Throwable e) {

        }
        try {
            living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).removeModifier(ATTACK_MODIFIER);
        } catch (Throwable e) {

        }
    }

    public static AttributeModifier speedModifier() {
        return new AttributeModifier(SPEED_MODIFIER, "Turntable Speed", -1024D, 0);
    }

    public static AttributeModifier flySpeedModifier() {
        return new AttributeModifier(FLY_SPEED_MODIFIER, "Turntable Fly Speed", -1024D, 0);
    }

    public static AttributeModifier attackSpeedModifier() {
        return new AttributeModifier(ATTACK_SPEED_MODIFIER, "Turntable Attack Speed", -1024D, 0);
    }

    public static AttributeModifier attackModifier() {
        return new AttributeModifier(ATTACK_MODIFIER, "Turntable Attack Damage", -2048D, 0);
    }

    public void freeze() {
        if (entity instanceof EntityLiving) {
            EntityLiving l = (EntityLiving) entity;
            l.tasks.taskEntries.clear();
            l.targetTasks.taskEntries.clear();
        }

        if (isLiving()) {
            jumpFactor = living().jumpMovementFactor;
            living().jumpMovementFactor = 0f;
            try {
                if (!living().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(speedModifier())) {
                    living().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                            .applyModifier(speedModifier());
                }
            } catch (Throwable e) {

            }

            try {
                if (!living().getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).hasModifier(flySpeedModifier())) {
                    living().getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED)
                            .applyModifier(flySpeedModifier());
                }
            } catch (Throwable e) {

            }
            try {
                if (!living().getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).hasModifier(attackSpeedModifier())) {
                    living().getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED)
                            .applyModifier(attackSpeedModifier());
                }
            } catch (Throwable e) {

            }
            try {
                if (!living().getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).hasModifier(attackModifier())) {
                    living().getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                            .applyModifier(attackModifier());
                }
            } catch (Throwable e) {

            }
        }
    }

    public void onHeal(LivingHealEvent event) {
        if (!active || !consume(Turntable.AP_COST_HEAL * event.getAmount())) {
            event.setCanceled(true);
        }
    }

    public void onJump() {
        if (active) {
            consume(Turntable.AP_COST_JUMP);
        }
    }

    public void onDestroyBlock(LivingDestroyBlockEvent event) {
        if (!consume(Turntable.AP_COST_BLOCK_DESTROY)) {
            event.setCanceled(true);
        }
    }

    public void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if (!consume(Turntable.AP_COST_BLOCK_PLACE)) {
            event.setCanceled(true);
        }
    }

    public void onMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        if (!consume(Turntable.AP_COST_BLOCK_PLACE * 2)) {
            event.setCanceled(true);
        }
    }

    public void onBreakBlock(BlockEvent.BreakEvent event) {
        if (!consume(Turntable.AP_COST_BLOCK_BREAK)) {
            event.setCanceled(true);
        }
    }

    public void onConsumed(LivingEntityUseItemEvent.Finish event) {
        consume(Turntable.AP_COST_CONSUME);
    }

    public void onConsumeStarted(LivingEntityUseItemEvent.Start event) {
        if (!active || actionPoints < Turntable.AP_COST_CONSUME) {
            event.setCanceled(true);
        }
    }

    public void onConsumeTick(LivingEntityUseItemEvent.Tick event) {
        if (!active || actionPoints <= Turntable.AP_COST_CONSUME) {
            event.setCanceled(true);
        }
    }

    public void onMount(EntityMountEvent event) {
        if (!active || !consume(Turntable.AP_COST_MOUNT)) {
            event.setCanceled(true);
        }
    }

    public void onPickup(EntityItemPickupEvent event) {
        if (!active || !consume(Turntable.AP_COST_PICKUP)) {
            event.setCanceled(true);
        }
    }

    public void onCloseContainer(PlayerContainerEvent.Close event) {
        if (!consume(Turntable.AP_COST_CLOSE_CONTAINER)) {
            event.setCanceled(true);
        }
    }

    public void onOpenContainer(PlayerContainerEvent.Open event) {
        if (!consume(Turntable.AP_COST_OPEN_CONTAINER)) {
            event.setCanceled(true);
        }
    }

    public void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (!consume(Turntable.AP_COST_INTERACT_ENTITY)) {
            event.setCanceled(true);
        }
    }

    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!consume(Turntable.AP_COST_RIGHT_CLICK_BLOCK)) {
            event.setCanceled(true);
        }
    }

    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!consume(Turntable.AP_COST_RIGHT_CLICK_ITEM)) {
            event.setCanceled(true);
        }
    }

    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!active || actionPoints < Turntable.AP_COST_BLOCK_BREAK) {
            event.setCanceled(true);
        }
    }

    public void onEnderTeleport(EnderTeleportEvent event) {
        if (!consume(Turntable.AP_COST_ENDER_TELEPORT)) {
            event.setCanceled(true);
        }
    }
}
