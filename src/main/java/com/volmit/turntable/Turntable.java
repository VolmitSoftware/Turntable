package com.volmit.turntable;

import com.volmit.turntable.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Turntable.MODID, name = Turntable.NAME, version = Turntable.VERSION)
public class Turntable {
    public static final double ENCOUNTER_ADD_RADIUS = 4;
    public static final double ENCOUNTER_FIELD_RADIUS = 24;
    public static final float AP_COST_MOVEMENT = 0.01f;
    public static final float AP_COST_BLOCK_DESTROY = 0.25f;
    public static final float AP_COST_BLOCK_BREAK = 0.5f;
    public static final float AP_COST_BLOCK_PLACE = 0.5f;
    public static final float AP_COST_ENDER_TELEPORT = 1f;
    public static final float AP_COST_CONSUME = 0.5f;
    public static final float AP_COST_MOUNT = 0.5f;
    public static final float AP_COST_PICKUP = 0.1f;
    public static final float AP_COST_INTERACT_ENTITY = 0.5f;
    public static final float AP_COST_OPEN_CONTAINER = 0.25f;
    public static final float AP_COST_RIGHT_CLICK_BLOCK = 0.25f;
    public static final float AP_COST_RIGHT_CLICK_ITEM = 0.1f;
    public static final float AP_COST_CLOSE_CONTAINER = 0.25f;
    public static final float AP_COST_HEAL = 0.05f;
    public static final float AP_COST_ATTACK = 1f;
    public static final float AP_COST_JUMP = 0.08f;
    public static final float AP_PER_TURN = 5f;
    public static final int TURN_TIME = 100;
    public static final int FROZEN_AUTO_ADVANCE_TIME = 50;
    public static final int MAX_TURN_TIME = 1200;
    public static final float CAMERA_TRACKING_SPEED = 0.03f;
    public static final int MOUSE_BREAK_OUT_THRESHOLD = 10;
    public static final int MOUSE_BREAK_OUT_TICKS = 50;
    @SidedProxy(clientSide = "com.volmit.turntable.proxy.ClientProxy", serverSide = "com.volmit.turntable.proxy.CommonProxy")
    public static CommonProxy proxy;
    public static final String MODID = "turntable";
    public static final String NAME = "Turntable";
    public static final String VERSION = "1.0";
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit();
        logger.info("Turntable preInit");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        logger.info("Turntable init");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
        logger.info("Turntable postInit");
    }
}
