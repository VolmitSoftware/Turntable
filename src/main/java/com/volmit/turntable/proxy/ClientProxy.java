package com.volmit.turntable.proxy;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.net.EndTurn;
import com.volmit.turntable.system.Member;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.awt.event.KeyEvent;
import java.util.List;

@Mod.EventBusSubscriber
public class ClientProxy extends CommonProxy {
    private List<Entity> turnOrder = null;
    private float ap = -1;

    public static final KeyBinding K_END_TURN = new KeyBinding("key.turntable.description.endturn", KeyEvent.VK_N, "key.categories.turntable");
    private boolean endTurnPressed = false;

    public void preInit() {
        super.preInit();
        Turntable.logger.info("ClientProxy preInit");
    }

    @Override
    public void init() {
        super.init();
        Turntable.logger.info("ClientProxy init");
        ClientRegistry.registerKeyBinding(K_END_TURN);
        registerRenderers();
    }

    public void onEngagementClosed() {
        turnOrder = null;
        ap = -1;
    }

    public void onAPUpdate(float ap) {
        this.ap = ap;
    }

    public void onEngagementUpdate(List<Entity> entities) {
        turnOrder = entities;
    }

    @Override
    public void postInit() {
        super.postInit();
        Turntable.logger.info("ClientProxy postInit");
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (K_END_TURN.isPressed()) {
            endTurnPressed = true;
        } else {
            if (endTurnPressed) {
                CommonProxy.network.sendToServer(new EndTurn.Packet());
            }
            endTurnPressed = false;
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (turnOrder != null && ap > 0 && mc.currentScreen == null) {
            int w = event.getResolution().getScaledWidth();
            int h = event.getResolution().getScaledHeight();
            int barWidth = w;  // Bar width in pixels
            int barHeight = 5;  // Bar height in pixels
            int x = 0;
            int y = 0;
            int fillColor = 0xFFFFFFFF;
            Gui.drawRect(x, y, (int) (x + (w * (ap / Turntable.AP_PER_TURN))), barHeight, fillColor);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFOVUpdate(FOVUpdateEvent event) {
        if (event.getEntity().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(Member.speedModifier())) {
            float originalFOV = event.getFov();
            float affectedFOV = event.getNewfov();
            float difference = originalFOV - affectedFOV;
            float myDesiredFOV = Minecraft.getMinecraft().gameSettings.fovSetting;
            event.setNewfov(myDesiredFOV + difference);
        }
    }

    public void registerRenderers() {

    }
}
