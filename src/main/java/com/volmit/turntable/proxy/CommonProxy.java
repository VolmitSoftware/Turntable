package com.volmit.turntable.proxy;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.net.EndTurnHandler;
import com.volmit.turntable.net.EndTurnPacket;
import com.volmit.turntable.old.net.SyncTurnBasedCapabilityHandler;
import com.volmit.turntable.old.net.SyncTurnBasedCapabilityPacket;
import com.volmit.turntable.system.TurnBasedHost;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CommonProxy {
    public static SimpleNetworkWrapper network;
    public TurnBasedHost host;
    protected Logger logger;

    public void preInit(){
        logger = Turntable.logger;;
        logger.info("CommonProxy preInit");
        host = new TurnBasedHost();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void init(){
        logger.info("CommonProxy init");
        registerPackets();
    }

    public void postInit() {
        logger.info("CommonProxy postInit");
    }

    public void registerPackets(){
        logger.info("CommonProxy Register Packets");
        int packetId = 0;
        network = NetworkRegistry.INSTANCE.newSimpleChannel(Turntable.MODID);
        network.registerMessage(new EndTurnHandler(), EndTurnPacket.class, packetId++, Side.SERVER);
    }

    @SubscribeEvent
    public void onEntityDamage(LivingAttackEvent event) {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return;
        }

        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();

            if(player.getEntityId() != event.getEntity().getEntityId()) {
                host.createEngagement(player, event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            host.onTick(event.world);
        }
    }
}
