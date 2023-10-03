package com.volmit.turntable.proxy;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.capability.TurnBased;
import com.volmit.turntable.capability.TurnBasedProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class ClientProxy extends CommonProxy {
    public void preInit() {
        super.preInit();
        Turntable.logger.info("ClientProxy preInit");
    }

    @Override
    public void init() {
        super.init();
        Turntable.logger.info("ClientProxy init");
        registerRenderers();
    }


    @Override
    public void postInit() {
        super.postInit();
        Turntable.logger.info("ClientProxy postInit");
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        if (!event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            return;
        }

        
    }

    public void registerRenderers() {

    }
}
