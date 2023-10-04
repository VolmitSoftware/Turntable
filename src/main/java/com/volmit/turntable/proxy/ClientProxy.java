package com.volmit.turntable.proxy;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.net.EndTurnPacket;
import com.volmit.turntable.system.Member;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.awt.event.KeyEvent;

@Mod.EventBusSubscriber
public class ClientProxy extends CommonProxy {
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

    @Override
    public void postInit() {
        super.postInit();
        Turntable.logger.info("ClientProxy postInit");
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (K_END_TURN.isPressed()) {
            endTurnPressed = true;
        }

        else {
            if(endTurnPressed){
                CommonProxy.network.sendToServer(new EndTurnPacket());
            }
            endTurnPressed = false;
        }
    }

    @SubscribeEvent
    public void onFOVUpdate(FOVUpdateEvent event) {
        if (event.getEntity().isLiving()) {
            if (event.getEntity().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(Member.SPEED_MODIFIER)) {
            }
        }
    }

    public void registerRenderers() {

    }
}
