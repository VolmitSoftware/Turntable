package com.volmit.turntable.proxy;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.api.TBServer;
import com.volmit.turntable.capability.TurnBased;
import com.volmit.turntable.capability.TurnBasedData;
import com.volmit.turntable.capability.TurnBasedProvider;
import com.volmit.turntable.capability.TurnBasedStorage;
import com.volmit.turntable.net.SyncTurnBasedCapabilityHandler;
import com.volmit.turntable.net.SyncTurnBasedCapabilityPacket;
import com.volmit.turntable.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CommonProxy {
    public static SimpleNetworkWrapper network;

    public void preInit(){
        System.out.println("CommonProxy preInit");
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void init(){
        System.out.println("CommonProxy init");
        CapabilityManager.INSTANCE.register(TurnBased.class, new TurnBasedStorage(), TurnBasedData.class);
        registerPackets();
    }

    public void registerPackets(){
        int packetId = 0;
        network = NetworkRegistry.INSTANCE.newSimpleChannel(Turntable.MODID);
        network.registerMessage(new SyncTurnBasedCapabilityHandler(), SyncTurnBasedCapabilityPacket.class, packetId++, Side.CLIENT);
    }

    public void postInit() {
        System.out.println("CommonProxy postInit");
    }

    public void update(Entity entity) {
        TurnBased tb = entity.getCapability(TurnBasedProvider.TBC, null);

        if(tb == null) {
            return;
        }

        if(tb.isFrozen()) {
            // Prevent Motion
            entity.motionX = 0;
            entity.motionY = 0;
            entity.motionZ = 0;

            // Prevent potions from expiring
            if(entity instanceof EntityLiving) {
                EntityLiving l = (EntityLiving) entity;
                l.tasks.taskEntries.clear();
                l.targetTasks.taskEntries.clear();

                for(PotionEffect i : l.getActivePotionEffects()) {
                    EntityUtil.setPotionEffectDuration(i, EntityUtil.getPotionEffectDuration(i) + 1);
                }
            }

            // Prevent fire from going out
            if(EntityUtil.getFireTicks(entity) > 0) {
                EntityUtil.setFireTicks(entity, EntityUtil.getFireTicks(entity) + 1);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingAttackEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        TurnBased tb = entity.getCapability(TurnBasedProvider.TBC, null);

        if(tb == null) {
            return;
        }

        if(tb.isFrozen() && !tb.isDamageable()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            for (Entity entity : event.world.loadedEntityList) {
                update(entity);
            }
        }
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(supportsTurnBased(event.getObject())) {
            event.addCapability(new ResourceLocation(Turntable.MODID, "tb"), new TurnBasedProvider());
        }
    }

    private boolean supportsTurnBased(Entity e){
        return !(e instanceof EntityItem || e instanceof EntityXPOrb|| e instanceof EntityFireball || e instanceof IProjectile);
    }
}
