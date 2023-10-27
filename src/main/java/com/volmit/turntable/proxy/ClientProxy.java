package com.volmit.turntable.proxy;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.config.ConfigHandler;
import com.volmit.turntable.net.EndTurn;
import com.volmit.turntable.system.Member;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;
import scala.reflect.internal.Trees;

import java.awt.event.KeyEvent;
import java.util.List;

@Mod.EventBusSubscriber
public class ClientProxy extends CommonProxy {
    private static List<Entity> turnOrder = null;
    private float ap = -1;
    public static Float targetYaw;
    public static Float targetPitch;
    public static Entity targetEntity;
    public static float lastTickYaw;
    public static float lastTickPitch;
    public static int cameraInterruptionTicks = 0;
    public static int combat = 20;
    public static float currentZoomMultiplier = 1f;
    public static float targetZoomMultiplier = 1f;

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

    public static void lookAt(Entity entity){
        lookAt(entity.getPositionEyes(1f));
    }

    @SubscribeEvent
    public static void onFOVModifier(EntityViewRenderEvent.FOVModifier event) {
        event.setFOV(Minecraft.getMinecraft().gameSettings.fovSetting / currentZoomMultiplier);
    }

    public static void zoom(float multiplier) {
        targetZoomMultiplier = (float) Math.min(10, Math.max(1, multiplier));
    }

    public static void lookAt(Vec3d target) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        if (player == null) {
            return;
        }

        if(turnOrder == null || turnOrder.isEmpty()){
            return;
        }

        look(target.subtract(player.getPositionEyes(1f)));
        double distance = player.getPositionEyes(1f).distanceTo(target)+1;
        zoom(distance > 30 ? 10 : (float) (30d/(30-distance)));
    }

    public static void look(Vec3d direction) {
        direction = direction.normalize();
        double dX = direction.x;
        double dY = direction.y;
        double dZ = direction.z;
        double distanceXZ = Math.sqrt(dX * dX + dZ * dZ);
        float yaw = (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(dY, distanceXZ) * 180.0 / Math.PI);

        look(yaw, pitch);
    }

    public static void look(float yaw, float pitch){
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        if(player != null) {
            targetYaw = yaw;
            targetPitch = pitch;
        }
    }

    public void onEngagementClosed() {
        turnOrder = null;
        targetEntity = null;
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

    // lerp function
    public static float flerp(float a, float b, float f){
        return a + f * (b - a);
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        cameraInterruptionTicks = ConfigHandler.K_MOUSE_BREAK_OUT_TICKS;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        float partialTicks = event.renderTickTime;

        if(cameraInterruptionTicks > 0){
            return;
        }

        if(targetZoomMultiplier != currentZoomMultiplier){
            currentZoomMultiplier = flerp(currentZoomMultiplier, targetZoomMultiplier, ConfigHandler.K_CAMERA_TRACKING_SPEED);
        }

        if (targetEntity != null && event.phase == TickEvent.Phase.START && mc.world != null && mc.player != null) {
            if (targetYaw != null) {
                float interpolatedYaw = lastTickYaw + (mc.player.rotationYaw - lastTickYaw) * partialTicks;
                mc.player.rotationYaw = interpolateAngle(interpolatedYaw, targetYaw, ConfigHandler.K_CAMERA_TRACKING_SPEED * currentZoomMultiplier);

                if (Math.abs(angleDifference(targetYaw, mc.player.rotationYaw)) < 0.01f) {
                    targetYaw = null;
                }
            }

            if (targetPitch != null) {
                float interpolatedPitch = lastTickPitch + (mc.player.rotationPitch - lastTickPitch) * partialTicks;
                mc.player.rotationPitch = interpolateAngle(interpolatedPitch, targetPitch, ConfigHandler.K_CAMERA_TRACKING_SPEED * currentZoomMultiplier);

                if (Math.abs(angleDifference(targetPitch, mc.player.rotationPitch)) < 0.01f) {
                    targetPitch = null;
                }
            }
        }

        if(targetEntity == null || turnOrder == null){
            zoom(1f);
        }
    }

    private float angleDifference(float a, float b) {
        float diff = (b - a + 180) % 360 - 180;
        if (diff < -180) diff += 360;
        return diff;
    }

    private float interpolateAngle(float startAngle, float targetAngle, float alpha) {
        float diff = angleDifference(startAngle, targetAngle);
        return startAngle + diff * alpha;
    }



    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if(cameraInterruptionTicks > 0){
            cameraInterruptionTicks--;
        }

        if (targetEntity != null && event.phase == TickEvent.Phase.START && mc.world != null && mc.player != null) {
            lastTickYaw = mc.player.rotationYaw;
            lastTickPitch = mc.player.rotationPitch;
            lookAt(targetEntity);
        }

        if(combat++ > 20 && Turntable.proxy instanceof ClientProxy){
            combat = 0;

            try
            {
                Class.forName("org.cyberpwn.resonance.Resonance").getDeclaredMethod("combat", boolean.class).invoke(null, turnOrder != null);
            }

            catch(Throwable e){

            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        turnOrder = null;
        targetEntity = null;
        targetYaw = null;
        targetPitch = null;
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
            int barHeight = 3;  // Bar height in pixels
            int x = 0;
            int y = 0;
            int fillColor = 0xFFFFFFFF;
            Gui.drawRect(x, y, (int) (x + (w * (ap / ConfigHandler.AP_PER_TURN))), barHeight, fillColor);
        }

        if(turnOrder != null){
            int turnX = 10;
            int turnY = 10;
            int paddX = 10;
            for(Entity i : turnOrder){
                boolean you = i instanceof EntityPlayer && i.getUniqueID().equals(Minecraft.getMinecraft().player.getUniqueID());
                turnX += paddX + renderTurnCard(turnX, turnY, i, you?0xFFFFFFFF : 0xBBFFFFFF);
            }
        }
    }

    public int renderTurnCard(int x, int y, Entity entity, int color) {
        int tw = Minecraft.getMinecraft().fontRenderer.getStringWidth(Member.getName(entity));
        int tsw = Minecraft.getMinecraft().fontRenderer.getStringWidth(Member.getTypeName(entity));

        tw = Math.max(tw, tsw);

        if(tw > 20){
            Gui.drawRect(x+((tw-20)/2), y, x+10+((tw-20)/2), y+20, color);
            Minecraft.getMinecraft().fontRenderer.drawString(Member.getName(entity), x, y+30, color);
            Minecraft.getMinecraft().fontRenderer.drawString(Member.getTypeName(entity), x, y+40, color);
        }

        else {
            Gui.drawRect(x, y, x+10, y+20, color);
            Minecraft.getMinecraft().fontRenderer.drawString(Member.getName(entity), x+((20-tw)/2), y+30, color);
            Minecraft.getMinecraft().fontRenderer.drawString(Member.getTypeName(entity), x+((20-tsw)/2), y+30, color);
        }

        return Math.max(20, tw);
    }

    public void registerRenderers() {

    }
}
