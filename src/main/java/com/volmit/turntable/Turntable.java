package com.volmit.turntable;

import com.volmit.turntable.proxy.CommonProxy;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.awt.event.KeyEvent;

@Mod(modid = Turntable.MODID, name = Turntable.NAME, version = Turntable.VERSION)
public class Turntable {
    public static final double ENCOUNTER_RADIUS = 8;
    public static final double ENCOUNTER_ESCAPE_RADIUS = 32;
    public static final double MOVEMENT_COST = 0.01;
    public static final int TURN_TIME = 100;
    public static final float ACTION_POINTS = 3;
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
