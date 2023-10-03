package com.volmit.turntable.api;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TBServer {
    public List<Encounter> activeEncounters;

    public Encounter getEncounter(Entity entity) {
        for(Encounter i : activeEncounters) {
            for(Member j : i.members) {
                if(j.entity == entity) {
                    return i;
                }
            }
        }

        return null;
    }

    public boolean tryCreateEncounter(List<Entity> entities) {
        World world = null;
        Set<Entity> adds = new HashSet<>();

        for(Entity i : entities) {
            if(world == null) {
                world = i.world;
                continue;
            }

            if(!world.equals(i.world)) {
                System.out.println("World Mismatch");
                return false;
            }

            adds.add(i);
        }

        entities = EntityUtil.explode(entities, Turntable.ENCOUNTER_RADIUS);

        if(entities.size() > 1) {
            createEncounter(entities);
            return true;
        }

        System.out.println("Not enough entities");
        return false;
    }

    private void createEncounter(List<Entity> initials){
        Encounter e = new Encounter(EncounterType.COMBAT);
        activeEncounters.add(e);
        e.begin(initials);
        System.out.println("Created an Encounter with " + initials.size() + " entities.");
    }
}