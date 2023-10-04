package com.volmit.turntable.old.proxy;

import com.volmit.turntable.Turntable;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
