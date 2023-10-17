package com.volmit.turntable;

import com.volmit.turntable.config.ConfigHandler;
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
    @Mod.Instance
    public static Turntable instance;
    @SidedProxy(clientSide = "com.volmit.turntable.proxy.ClientProxy", serverSide = "com.volmit.turntable.proxy.CommonProxy")
    public static CommonProxy proxy;
    public static final String MODID = "turntable";
    public static final String NAME = "Turntable";
    public static final String VERSION = "1.0";
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        ConfigHandler.init(event.getSuggestedConfigurationFile());
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
