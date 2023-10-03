package com.volmit.turntable.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.potion.PotionEffect;

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
        all.addAll(e);
        return new ArrayList<>(all);
    }

    public static int getPotionEffectDuration(PotionEffect i)
    {
        try {
            Field d = i.getClass().getDeclaredField("duration");
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
            Field d = i.getClass().getDeclaredField("duration");
            d.setAccessible(true);
            d.setInt(i, ticks);
        }

        catch(Throwable e) {
            e.printStackTrace();
        }
    }

    public static int getFireTicks(Entity entity) {
        try {
            Field f = entity.getClass().getDeclaredField("fire");
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
            Field f = entity.getClass().getDeclaredField("fire");
            f.setAccessible(true);
            f.setInt(entity, ticks);
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
}
