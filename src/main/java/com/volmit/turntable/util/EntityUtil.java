package com.volmit.turntable.util;

import net.minecraft.entity.Entity;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public class EntityUtil {
    public static int getPotionEffectDuration(PotionEffect i) {
        try {
            Field d = ObfuscationReflectionHelper.findField(PotionEffect.class, "duration");
            d.setAccessible(true);
            return d.getInt(i);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void setPotionEffectDuration(PotionEffect i, int ticks) {
        try {
            Field d = ObfuscationReflectionHelper.findField(PotionEffect.class, "duration");
            d.setAccessible(true);
            d.setInt(i, ticks);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static int getFireTicks(Entity entity) {
        try {
            Field f = ObfuscationReflectionHelper.findField(Entity.class, "fire");
            f.setAccessible(true);
            return f.getInt(entity);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void setFireTicks(Entity entity, int ticks) {
        try {
            Field f = ObfuscationReflectionHelper.findField(Entity.class, "fire");
            f.setAccessible(true);
            f.setInt(entity, ticks);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
