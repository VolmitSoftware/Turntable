package com.volmit.turntable.system;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.config.ConfigHandler;
import com.volmit.turntable.net.UpdateEngagementMembers;
import com.volmit.turntable.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Engagement {
    public World world;
    public TurnBasedHost host;
    public List<Member> members;
    public boolean closed;
    public Member lastMember;
    public int ticks;

    public Engagement(TurnBasedHost host) {
        ticks = 0;
        this.host = host;
        members = new ArrayList<>();
        closed = false;
    }

    public void init(List<Entity> add) {
        world = add.get(0).world;

        for (Entity i : add) {
            addMember(new Member(this, i, host.getInitiative(i)));
        }

        addNearby();
        sortByInitiative();
        Turntable.logger.info("An engagement has started between " + members.size() + " entities.");
        updateTurnOrder();
    }

    public void addNearby() {
        for (Member i : new ArrayList<>(members)) {
            for (Entity j : i.entity.world.getEntitiesWithinAABB(Entity.class, i.entity.getEntityBoundingBox().grow(ConfigHandler.ENCOUNTER_ADD_RADIUS))) {
                if (canEngage(j) && host.getEngagement(j) == null && Member.hasLOS(i.entity, j, (int)ConfigHandler.ENCOUNTER_ADD_RADIUS)) {
                    if(!(j instanceof EntityAnimal)){
                        addMember(new Member(this, j, host.getInitiative(j)));
                    }
                }
            }
        }
    }

    public void sortByInitiative() {
        members.sort((o1, o2) -> o2.initiative - o1.initiative);
    }

    public void updateTurnOrder() {
        List<Entity> t = new ArrayList<>();
        for (Member i : members) {
            t.add(i.entity);
        }

        UpdateEngagementMembers.Packet p = new UpdateEngagementMembers.Packet();
        p.turnOrder = t;

        for (Member i : members) {
            if (i.isPlayer()) {
                CommonProxy.network.sendTo(p, (EntityPlayerMP) i.entity);
            }
        }
    }

    public double[] avgPos() {
        double[] d = new double[]{0, 0, 0};

        for (Member i : members) {
            d[0] += i.entity.posX;
            d[1] += i.entity.posY;
            d[2] += i.entity.posZ;
        }

        d[0] /= members.size();
        d[1] /= members.size();
        d[2] /= members.size();

        return d;
    }

    public double distanceFromEngagement(double x, double y, double z) {
        double[] d = avgPos();
        double dist = Math.pow(d[0] - x, 2) + Math.pow(d[1] - y, 2) + Math.pow(d[2] - z, 2);

        for (Member i : members) {
            d[0] = i.entity.posX;
            d[1] = i.entity.posY;
            d[2] = i.entity.posZ;
            double dx = Math.pow(d[0] - x, 2) + Math.pow(d[1] - y, 2) + Math.pow(d[2] - z, 2);

            if (dx < dist) {
                dist = dx;
            }
        }

        return dist;
    }

    public void onTick() {
        if (closed) {
            Turntable.logger.info("Ticked a closed engagement!");
            return;
        }

        ticks++;

        List<Member> rem = new ArrayList<>();

        for (int i = members.size() - 1; i >= 0; i--) {
            if (!members.get(i).isAlive() || members.get(i).entity.world != world || members.get(i).distanceFromEngagement() > ConfigHandler.ENCOUNTER_FIELD_RADIUS * ConfigHandler.ENCOUNTER_FIELD_RADIUS) {
                rem.add(members.get(i));
                Turntable.logger.info("Removed a dead/escaped entity " + members.get(i).entity + " from engagement!");
            }
        }

        for (Member i : rem) {
            removeMember(i);
        }

        if (members.size() < 2) {
            Turntable.logger.info("Engagement closing due to lack of members! (<2)");
            close();
            return;
        }

        if (lastMember == null) {
            lastMember = getActiveMember();
            Turntable.logger.info("First turn: " + lastMember.entity);
            lastMember.onBeginTurn();

            for (Member i : members) {
                if (i != lastMember) {
                    i.onOtherBeginTurn(lastMember);
                }
            }
        }

        if (ticks > ConfigHandler.TURN_TIME) {
            if (!getActiveMember().isPlayer()) {
                nextTurn();
            }
        }

        getActiveMember().onTurnTick(ticks);

        if (lastMember != getActiveMember()) {
            lastMember.onEndTurn();
            lastMember = getActiveMember();
            lastMember.onBeginTurn();

            for (Member i : members) {
                if (i != lastMember) {
                    i.onOtherBeginTurn(lastMember);
                }
            }
        }

        for (Member i : members) {
            i.onTick(ticks);
        }

        if(ticks % 20 == 0){
            addNearby();
        }
    }

    public void close() {
        closed = true;

        for (Member i : new ArrayList<>(members)) {
            removeMember(i);
        }

        Turntable.logger.info("An engagement has closed.");
    }

    public Member getActiveMember() {
        return members.get(0);
    }

    public void removeMember(Member member) {
        members.remove(member);
        member.onLeave();

        for(Member i : members){
            i.onOtherLeft(member);
        }
    }

    public void addMember(Member member) {
        for (Member i : members) {
            if (i.entity.getUniqueID().equals(member.entity.getUniqueID())) {
                return;
            }
        }

        members.add(member);
        member.onJoin();
        updateTurnOrder();
    }

    public void nextTurn() {
        members.add(members.remove(0));
        ticks = 0;
        Turntable.logger.info("Engagement Turn: " + getActiveMember().entity);
        updateTurnOrder();

        String s = "";

        for (Member i : members) {
            s += i.entity.getEntityId() + " ";
        }

        Turntable.logger.info("Engagement Turn Order: " + s);
    }

    public static boolean canEngage(Entity e) {
        return !(e instanceof EntityItem || e instanceof EntityXPOrb || e instanceof EntityFireball || e instanceof IProjectile || e instanceof EntityAreaEffectCloud);
    }
}
