package com.volmit.turntable.proxy;

import com.volmit.turntable.api.TBServer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class ServerProxy extends CommonProxy {
    private TBServer server;

    public void preInit() {
        System.out.println("ServerProxy postInit");
        super.preInit();
        server = new TBServer();
    }

    @Override
    public void init() {
        System.out.println("ServerProxy init");
        super.init();
    }

    @SubscribeEvent
    public void onEntityDamage(LivingAttackEvent event) {
        System.out.println("Server catch attack");
        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            System.out.println("Server catch attacker is player");
            EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();

            if(player.getEntityId() != event.getEntity().getEntityId()) {
                System.out.println("Server catch attack non player by player");
                List<Entity> ee = new ArrayList<>();
                ee.add(player);
                ee.add(event.getEntity());
                server.tryCreateEncounter(ee);
            }
        }
    }

    @SubscribeEvent
    public void onEntityHurtByArrow(LivingHurtEvent event) {
        System.out.println("Server catch arr");
        if (event.getSource().getImmediateSource() instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow) event.getSource().getImmediateSource();

            if (arrow.shootingEntity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) arrow.shootingEntity;
                System.out.println(player.getName() + " hit an entity with an arrow!");

                if(player.getEntityId() != event.getEntity().getEntityId()) {
                    List<Entity> ee = new ArrayList<>();
                    ee.add(player);
                    ee.add(event.getEntity());
                    server.tryCreateEncounter(ee);
                }
            }
        }
    }

    @Override
    public void postInit() {
        System.out.println("ServerProxy postInit");
        super.postInit();
    }
}
