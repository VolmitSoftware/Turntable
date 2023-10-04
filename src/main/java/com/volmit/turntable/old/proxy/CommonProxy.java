package com.volmit.turntable.old.proxy;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.old.api.Encounter;
import com.volmit.turntable.old.api.TBServer;
import com.volmit.turntable.old.capability.TurnBased;
import com.volmit.turntable.old.capability.TurnBasedData;
import com.volmit.turntable.old.capability.TurnBasedProvider;
import com.volmit.turntable.old.capability.TurnBasedStorage;
import com.volmit.turntable.old.net.SyncTurnBasedCapabilityHandler;
import com.volmit.turntable.old.net.SyncTurnBasedCapabilityPacket;
import com.volmit.turntable.old.util.EntityUtil;
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
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

public class CommonProxy {
    public static SimpleNetworkWrapper network;
    private TBServer server;

    public void preInit(){
        Turntable.logger.info("CommonProxy preInit");
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void init(){
        Turntable.logger.info("CommonProxy init");
        CapabilityManager.INSTANCE.register(TurnBased.class, new TurnBasedStorage(), TurnBasedData.class);
        registerPackets();
    }

    public void postInit() {
        Turntable.logger.info("CommonProxy postInit");
    }

    public void registerPackets(){
        Turntable.logger.info("CommonProxy Register Packets");
        int packetId = 0;
        network = NetworkRegistry.INSTANCE.newSimpleChannel(Turntable.MODID);
        network.registerMessage(new SyncTurnBasedCapabilityHandler(), SyncTurnBasedCapabilityPacket.class, packetId++, Side.CLIENT);
    }

    public void update(Entity entity) {
        System.out.println("Updating " + entity);

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
                l.setRevengeTarget(null);
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
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && server == null) {
            server = new TBServer();
        }
    }

    @SubscribeEvent
    public void onEntityDamage(LivingAttackEvent event) {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();

            if(player.getEntityId() != event.getEntity().getEntityId()) {
                List<Entity> ee = new ArrayList<>();
                ee.add(player);
                ee.add(event.getEntity());
                server.tryCreateEncounter(ee);
            }
        }
    }

    @SubscribeEvent
    public void onEntityHurtByArrow(LivingHurtEvent event) {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        if (event.getSource().getImmediateSource() instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow) event.getSource().getImmediateSource();

            if (arrow.shootingEntity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) arrow.shootingEntity;

                if(player.getEntityId() != event.getEntity().getEntityId()) {
                    List<Entity> ee = new ArrayList<>();
                    ee.add(player);
                    ee.add(event.getEntity());
                    server.tryCreateEncounter(ee);
                }
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
           // event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if(server != null) {
                for(Encounter i : server.activeEncounters) {
                    if(i.world == event.world) {
                        i.onTick();
                    }
                }
            }


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
