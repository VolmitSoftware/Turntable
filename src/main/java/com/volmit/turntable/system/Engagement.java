package com.volmit.turntable.system;

import com.volmit.turntable.Turntable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
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

    public Engagement(TurnBasedHost host){
        ticks = 0;
        this.host = host;
        members = new ArrayList<>();
        closed = false;
    }

    public void init(List<Entity> add){
        world = add.get(0).world;

        for(Entity i : add) {
            addMember(new Member(this, i, host.getInitiative(i)));
        }

        addNearby();
        sortByInitiative();

        Turntable.logger.info("An engagement has started between " + members.size() + " entities.");
    }

    public void addNearby(){
        for(Member i : new ArrayList<>(members)) {
            for(Entity j : i.entity.world.getEntitiesWithinAABB(Entity.class, i.entity.getEntityBoundingBox().grow(Turntable.ENCOUNTER_RADIUS))) {
                if(canEngage(j) && host.getEngagement(j) == null) {
                    addMember(new Member(this, j, host.getInitiative(j)));
                }
            }
        }
    }

    public void sortByInitiative(){
        members.sort((o1, o2) -> o2.initiative - o1.initiative);
    }

    public void onTick(){
        if(closed) {
            Turntable.logger.info("Ticked a closed engagement!");
            return;
        }

        ticks++;

        for(int i = members.size()-1; i >= 0; i--) {
            if(!members.get(i).isAlive() || members.get(i).entity.world != world) {
                Member m = members.remove(i);
                Turntable.logger.info("Removed a dead/teleported entity "+m.entity+" from engagement!");
            }
        }

        if(members.size() < 2) {
            Turntable.logger.info("Engagement closing due to lack of members! (<2)");
            close();
            return;
        }

        if(lastMember == null) {
            lastMember = getActiveMember();
            Turntable.logger.info("First turn: "+lastMember.entity);
            lastMember.onBeginTurn();

            for(Member i : members) {
                if(i != lastMember) {
                    i.onOtherBeginTurn(lastMember);
                }
            }
        }

        if(ticks > Turntable.TURN_TIME) {
            if(!getActiveMember().isPlayer()){
                nextTurn();
            }
        }

        getActiveMember().onTurnTick(ticks);

        if(lastMember != getActiveMember()) {
            lastMember.onEndTurn();
            lastMember = getActiveMember();
            lastMember.onBeginTurn();

            for(Member i : members) {
                if(i != lastMember) {
                    i.onOtherBeginTurn(lastMember);
                }
            }
        }

        for(Member i : members) {
            i.onTick(ticks);
        }

        addNearby();
    }

    public void close(){
        closed = true;

        for(Member i : new ArrayList<>(members)) {
            removeMember(i);
        }

        Turntable.logger.info("An engagement has closed.");
    }

    public Member getActiveMember(){
        return members.get(0);
    }

    public void removeMember(Member member){
        members.remove(member);
        member.onLeave();
    }

    public void addMember(Member member){
        members.add(member);
        member.onJoin();
    }

    public void nextTurn(){
        members.add(members.remove(0));
        ticks = 0;
        Turntable.logger.info("Engagement Turn: "+getActiveMember().entity);
    }

    public static boolean canEngage(Entity e){
        return !(e instanceof EntityItem || e instanceof EntityXPOrb || e instanceof EntityFireball || e instanceof IProjectile);
    }
}
