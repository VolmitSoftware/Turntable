package com.volmit.turntable.proxy;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.net.EndTurn;
import com.volmit.turntable.net.EngagementClosed;
import com.volmit.turntable.net.UpdateAP;
import com.volmit.turntable.net.UpdateEngagementMembers;
import com.volmit.turntable.system.Engagement;
import com.volmit.turntable.system.Member;
import com.volmit.turntable.system.TurnBasedHost;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

public class CommonProxy {
    public static SimpleNetworkWrapper network;
    public TurnBasedHost host;
    protected Logger logger;

    public void preInit() {
        logger = Turntable.logger;
        logger.info("CommonProxy preInit");
        host = new TurnBasedHost();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void init() {
        logger.info("CommonProxy init");
        registerPackets();
    }

    public void postInit() {
        logger.info("CommonProxy postInit");
    }

    public void registerPackets() {
        logger.info("CommonProxy Register Packets");
        int packetId = 0;
        network = NetworkRegistry.INSTANCE.newSimpleChannel(Turntable.MODID);
        network.registerMessage(new EndTurn(), EndTurn.Packet.class, packetId++, Side.SERVER);
        network.registerMessage(new UpdateEngagementMembers(), UpdateEngagementMembers.Packet.class, packetId++, Side.CLIENT);
        network.registerMessage(new UpdateAP(), UpdateAP.Packet.class, packetId++, Side.CLIENT);
        network.registerMessage(new EngagementClosed(), EngagementClosed.Packet.class, packetId++, Side.CLIENT);
    }

    @SubscribeEvent
    public void on(LivingHealEvent event) {
        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onHeal(event);
        }
    }

    @SubscribeEvent
    public void on(LivingEvent.LivingUpdateEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        if (event.getEntity() instanceof EntityLivingBase && ((EntityLivingBase) event.getEntity()).getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(Member.speedModifier()) && host.getMember(event.getEntity()) == null) {
            Member.unfreeze((EntityLivingBase) event.getEntity());
        }
    }

    @SubscribeEvent
    public void on(LivingEvent.LivingJumpEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onJump();
        }
    }

    @SubscribeEvent
    public void on(LivingAttackEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        if (event.getSource().getTrueSource() != null && !host.consume(event.getSource().getTrueSource(), Turntable.AP_COST_ATTACK)) {
            event.setCanceled(true);
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            if (!m.shouldTakeDamage(event)) {
                event.setCanceled(true);
                return;
            }
        }

        if (event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();

            if (player.getEntityId() != event.getEntity().getEntityId()) {
                host.createEngagement(player, event.getEntity());
            }
        } else if (event.getEntity() instanceof EntityPlayer && event.getSource().getTrueSource() != null) {
            if (event.getSource().getTrueSource() != null && event.getSource().getTrueSource().getEntityId() != event.getEntity().getEntityId()) {
                host.createEngagement(event.getSource().getTrueSource(), event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public void on(LivingDestroyBlockEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onDestroyBlock(event);
        }
    }

    @SubscribeEvent
    public void on(BlockEvent.EntityPlaceEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onPlaceBlock(event);
        }
    }

    @SubscribeEvent
    public void on(BlockEvent.EntityMultiPlaceEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onMultiPlace(event);
        }
    }

    @SubscribeEvent
    public void on(BlockEvent.BreakEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getPlayer());

        if (m != null) {
            m.onBreakBlock(event);
        }
    }

    @SubscribeEvent
    public void on(EntityStruckByLightningEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void on(BabyEntitySpawnEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        if (host.getMember(event.getParentA()) != null || host.getMember(event.getParentB()) != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void on(BlockEvent.CropGrowEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Engagement e = host.getInField(event.getPos());

        if (e != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void on(LivingEntityUseItemEvent.Finish event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onConsumed(event);
        }
    }

    @SubscribeEvent
    public void on(LivingEntityUseItemEvent.Start event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onConsumeStarted(event);
        }
    }

    @SubscribeEvent
    public void on(LivingEntityUseItemEvent.Tick event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onConsumeTick(event);
        }
    }

    @SubscribeEvent
    public void on(BlockEvent.FluidPlaceBlockEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Engagement e = host.getInField(event.getPos());

        if (e != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void on(BlockEvent.CreateFluidSourceEvent event) {
        Engagement e = host.getInField(event.getPos());

        if (e != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void on(LivingSpawnEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Engagement e = host.getEngagement(Turntable.ENCOUNTER_FIELD_RADIUS, event.getX(), event.getY(), event.getZ());

        if (e != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void on(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            host.onTick(event.world);
        }
    }

    @SubscribeEvent
    public void on(EntityMountEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onMount(event);
        }
    }


    @SubscribeEvent
    public void on(EntityItemPickupEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onPickup(event);
        }
    }


    @SubscribeEvent
    public void on(PlayerContainerEvent.Open event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onOpenContainer(event);
        }
    }


    @SubscribeEvent
    public void on(PlayerContainerEvent.Close event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onCloseContainer(event);
        }
    }


    @SubscribeEvent
    public void on(PlayerInteractEvent.EntityInteract event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onInteractEntity(event);
        }
    }

    @SubscribeEvent
    public void on(PlayerInteractEvent.RightClickBlock event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onRightClickBlock(event);
        }
    }

    @SubscribeEvent
    public void on(PlayerInteractEvent.RightClickItem event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onRightClickItem(event);
        }
    }

    @SubscribeEvent
    public void on(PlayerInteractEvent.LeftClickBlock event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onLeftClickBlock(event);
        }
    }

    @SubscribeEvent
    public void on(PlayerSleepInBedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            event.setResult(EntityPlayer.SleepResult.NOT_SAFE);
        }
    }

    @SubscribeEvent
    public void on(EnderTeleportEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        Member m = host.getMember(event.getEntity());

        if (m != null) {
            m.onEnderTeleport(event);
        }
    }
}
