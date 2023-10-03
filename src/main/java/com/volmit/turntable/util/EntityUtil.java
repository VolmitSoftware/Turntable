package com.volmit.turntable.util;

import com.volmit.turntable.capability.TurnBasedProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityUtil {
    public static List<Entity> explode(List<Entity> e, double range) {
        Set<Entity> all = new HashSet<>();
        for(Entity i : e) {
            all.addAll(i.world.getEntitiesWithinAABB(Entity.class, i.getEntityBoundingBox().grow(range)));
        }

        all.removeIf((i) -> e instanceof EntityAnimal);
        all.removeIf((i) -> i.getCapability(TurnBasedProvider.TBC, null) == null);
        all.addAll(e);
        return new ArrayList<>(all);
    }

    public static int getPotionEffectDuration(PotionEffect i)
    {
        try {
            Field d = ObfuscationReflectionHelper.findField(PotionEffect.class, "duration");
            d.setAccessible(true);
            return d.getInt(i);
        }

        catch(Throwable e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void setPotionEffectDuration(PotionEffect i, int ticks)
    {
        try {
            Field d = ObfuscationReflectionHelper.findField(PotionEffect.class, "duration");
            d.setAccessible(true);
            d.setInt(i, ticks);
        }

        catch(Throwable e) {
            e.printStackTrace();
        }
    }

    public static int getFireTicks(Entity entity) {
        try {
            Field f = ObfuscationReflectionHelper.findField(Entity.class, "fire");
            f.setAccessible(true);
            return f.getInt(entity);
        } catch(Throwable e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void setFireTicks(Entity entity, int ticks)
    {
        try {
            Field f = ObfuscationReflectionHelper.findField(Entity.class, "fire");
            f.setAccessible(true);
            f.setInt(entity, ticks);
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
}
