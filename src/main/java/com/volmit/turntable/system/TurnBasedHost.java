package com.volmit.turntable.system;

import com.volmit.turntable.Turntable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TurnBasedHost {
    public List<Engagement> engagements;

    public TurnBasedHost() {
        engagements = new ArrayList<>();
    }

    public Engagement getInField(double x, double y, double z) {
        return getEngagement(Turntable.ENCOUNTER_FIELD_RADIUS, x, y, z);
    }

    public Engagement getInField(BlockPos p) {
        return getEngagement(Turntable.ENCOUNTER_FIELD_RADIUS, p.getX(), p.getY(), p.getZ());
    }

    public Engagement getEngagement(double threshold, BlockPos pos) {
        return getEngagement(threshold, pos.getX(), pos.getY(), pos.getZ());
    }

    public Engagement getEngagement(double threshold, double x, double y, double z) {
        for (Engagement i : engagements) {
            if (i.distanceFromEngagement(x, y, z) <= threshold * threshold) {
                return i;
            }
        }

        return null;
    }

    public int getInitiative(Entity entity) {
        if (entity instanceof EntityAnimal) {
            return 1000;
        }

        return 100;
    }

    public boolean consume(Entity entity, float ap) {
        Member m = getMember(entity);

        if (m != null) {
            return m.consume(ap);
        }

        return true;
    }

    public Member getMember(Entity entity) {
        for (Engagement i : engagements) {
            for (Member j : i.members) {
                if (j.entity == entity) {
                    return j;
                }
            }
        }

        return null;
    }

    public void onTick(World world) {
        for (int i = engagements.size() - 1; i >= 0; i--) {
            Engagement e = engagements.get(i);

            if (e.world != world) {
                continue;
            }

            if (e.closed) {
                engagements.remove(i);
                continue;
            }

            e.onTick();
        }
    }

    public void createEngagement(Entity... entities) {
        List<Entity> e = new ArrayList<>(Arrays.asList(entities));
        e.removeIf((i) -> getEngagement(i) != null || !Engagement.canEngage(i));

        if (e.size() < 2) {
            return;
        }
        boolean hasPlayer = false;
        for (Entity i : e) {
            if (i instanceof EntityPlayer) {
                hasPlayer = true;
                break;
            }
        }

        if (!hasPlayer) {
            return;
        }

        Engagement en = new Engagement(this);
        engagements.add(en);
        en.init(e);
    }

    public Engagement getEngagement(Entity entity) {
        for (Engagement i : engagements) {
            for (Member j : i.members) {
                if (j.entity == entity) {
                    return i;
                }
            }
        }

        return null;
    }
}
