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
    public static final double ENCOUNTER_RADIUS = 32;
    public static final int TURN_TIME = 100;
    @SidedProxy(clientSide = "com.volmit.turntable.proxy.ClientProxy", serverSide = "com.volmit.turntable.proxy.ServerProxy")
    public static CommonProxy proxy;
    public static final String MODID = "turntable";
    public static final String NAME = "Turntable";
    public static final String VERSION = "1.0";
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit();
        System.out.println("Turntable preInit");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        System.out.println("Turntable init");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
        System.out.println("Turntable postInit");
    }
}
