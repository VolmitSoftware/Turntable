package com.volmit.turntable.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ConfigHandler {
    public static Configuration config;


    /// Defaults
    public static final double D_ENCOUNTER_ADD_RADIUS = 16;
    public static final double D_ENCOUNTER_FIELD_RADIUS = 24;
    public static final float D_BONUS_AP_PER_ENEMY = 0.5f;
    public static final float D_AP_COST_MOVEMENT = 0.006f;
    public static final float D_AP_COST_BLOCK_DESTROY = 0.25f;
    public static final float D_AP_COST_BLOCK_BREAK = 0.5f;
    public static final float D_AP_COST_BLOCK_PLACE = 0.5f;
    public static final float D_AP_COST_ENDER_TELEPORT = 1f;
    public static final float D_AP_COST_CONSUME = 0.5f;
    public static final float D_AP_COST_MOUNT = 0.5f;
    public static final float D_AP_COST_PICKUP = 0.1f;
    public static final float D_AP_COST_INTERACT_ENTITY = 0.5f;
    public static final float D_AP_COST_OPEN_CONTAINER = 0.25f;
    public static final float D_AP_COST_RIGHT_CLICK_BLOCK = 0.25f;
    public static final float D_AP_COST_RIGHT_CLICK_ITEM = 0.1f;
    public static final float D_AP_COST_CLOSE_CONTAINER = 0.25f;
    public static final float D_AP_COST_HEAL = 0.01f;
    public static final float D_AP_COST_ATTACK = 1f;
    public static final float D_AP_COST_JUMP = 0.05f;
    public static final float D_AP_PER_TURN = 3f;
    public static final int D_TURN_TIME = 100;
    public static final int D_FROZEN_AUTO_ADVANCE_TIME = 5;
    public static final int D_MAX_TURN_TIME = 1200;
    public static final float D_K_CAMERA_TRACKING_SPEED = 0.1f;
    public static final float D_POTION_MODIFIER_ADD = 0.5f;
    public static final int D_K_MOUSE_BREAK_OUT_THRESHOLD = 1;
    public static final int D_K_MOUSE_BREAK_OUT_TICKS = 50;
    public static final boolean D_FORCE_TICK_ENTITIES = true;

    /// Configuration values
    public static double ENCOUNTER_ADD_RADIUS = D_ENCOUNTER_ADD_RADIUS;
    public static double ENCOUNTER_FIELD_RADIUS = D_ENCOUNTER_FIELD_RADIUS;
    public static float AP_COST_MOVEMENT = D_AP_COST_MOVEMENT;
    public static float AP_COST_BLOCK_DESTROY = D_AP_COST_BLOCK_DESTROY;
    public static float AP_COST_BLOCK_BREAK = D_AP_COST_BLOCK_BREAK;
    public static float AP_COST_BLOCK_PLACE =   D_AP_COST_BLOCK_PLACE;
    public static float AP_COST_ENDER_TELEPORT = D_AP_COST_ENDER_TELEPORT;
    public static float AP_COST_CONSUME = D_AP_COST_CONSUME;
    public static float AP_COST_MOUNT = D_AP_COST_MOUNT;
    public static float AP_COST_PICKUP = D_AP_COST_PICKUP;
    public static float AP_COST_INTERACT_ENTITY = D_AP_COST_INTERACT_ENTITY;
    public static float AP_COST_OPEN_CONTAINER = D_AP_COST_OPEN_CONTAINER;
    public static float AP_COST_RIGHT_CLICK_BLOCK = D_AP_COST_RIGHT_CLICK_BLOCK;
    public static float AP_COST_RIGHT_CLICK_ITEM = D_AP_COST_RIGHT_CLICK_ITEM;
    public static float AP_COST_CLOSE_CONTAINER = D_AP_COST_CLOSE_CONTAINER;
    public static float AP_COST_HEAL = D_AP_COST_HEAL;
    public static float AP_COST_ATTACK = D_AP_COST_ATTACK;
    public static float AP_COST_JUMP = D_AP_COST_JUMP;
    public static float AP_PER_TURN = D_AP_PER_TURN;
    public static int TURN_TIME = D_TURN_TIME;
    public static int FROZEN_AUTO_ADVANCE_TIME = D_FROZEN_AUTO_ADVANCE_TIME;
    public static int MAX_TURN_TIME = D_MAX_TURN_TIME;
    public static float K_CAMERA_TRACKING_SPEED = D_K_CAMERA_TRACKING_SPEED;
    public static float POTION_MODIFIER_ADD = D_POTION_MODIFIER_ADD;
    public static int K_MOUSE_BREAK_OUT_THRESHOLD = D_K_MOUSE_BREAK_OUT_THRESHOLD;
    public static int K_MOUSE_BREAK_OUT_TICKS = D_K_MOUSE_BREAK_OUT_TICKS;
    public static boolean FORCE_TICK_ENTITIES = D_FORCE_TICK_ENTITIES;
    public static float BONUS_AP_PER_ENEMY = D_BONUS_AP_PER_ENEMY;


    public static void init(File file) {
        if (config == null) {
            config = new Configuration(file);
            loadConfig();
        }
    }

    private static void loadConfig() {
        //exampleBoolean = config.getBoolean("exampleBoolean", Configuration.CATEGORY_GENERAL, true, "Example description");

        Map<String, Class<?>> keys = new HashMap<>();

        for(Field i : ConfigHandler.class.getDeclaredFields()){
            if(Modifier.isFinal(i.getModifiers()) && Modifier.isPublic(i.getModifiers()) && Modifier.isStatic(i.getModifiers()) && i.getName().startsWith("D_")) {
                keys.put(i.getName().substring(2), i.getType());
            }
        }

        for(String i : keys.keySet()){
            try
            {
                Field fieldDefault = ConfigHandler.class.getDeclaredField("D_" + i);
                Field field = ConfigHandler.class.getDeclaredField(i);
                field.setAccessible(true);
                fieldDefault.setAccessible(true);
                String n = i.replaceAll("\\Q_K\\E", "")
                        .replaceAll("\\Q_\\E", " ")
                        .toLowerCase();

                if(field.getType() == int.class){
                    field.set(null, config.getInt(n, i.startsWith("K_") ? "Client" : "Engine",
                            fieldDefault.getInt(null), Integer.MIN_VALUE, Integer.MAX_VALUE, ""));
                } else if(field.getType() == boolean.class){
                    field.set(null, config.getBoolean(n, i.startsWith("K_") ? "Client" : "Engine",
                            fieldDefault.getBoolean(null), ""));
                } else if(field.getType() == double.class){
                    field.set(null, config.getFloat(n, i.startsWith("K_") ? "Client" : "Engine",
                            (float) fieldDefault.getDouble(null), Float.MIN_VALUE, Float.MAX_VALUE, ""));
                } else if(field.getType() == float.class){
                    field.set(null, config.getFloat(n, i.startsWith("K_") ? "Client" : "Engine",
                            fieldDefault.getFloat(null), Float.MIN_VALUE, Float.MAX_VALUE, ""));
                } else if(field.getType() == String.class){
                    field.set(null, config.getString(n, i.startsWith("K_") ? "Client" : "Engine",
                            (String) fieldDefault.get(null), ""));
                }
            }

            catch(Throwable e){
                e.printStackTrace();
            }
        }

        if (config.hasChanged()) {
            config.save();
        }
    }
}
