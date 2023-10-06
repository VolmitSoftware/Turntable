package com.volmit.turntable.old.api;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.old.capability.TurnBased;
import com.volmit.turntable.old.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Encounter {
    public EncounterType type;
    public List<Member> members;
    public int activeMember;
    public int turn;
    public int ticks = 0;
    public boolean closed;
    public World world;
    public TBServer server;

    public Encounter(TBServer server, World world, EncounterType type) {
        this.type = type;
        this.server = server;
        this.world = world;
        members = new ArrayList<>();
        activeMember = 0;
        turn = 0;
        closed = false;
    }

    public void onTick(){
        if(closed){
            return;
        }

        ticks++;
        clean();
        validate();

        if(closed){
            return;
        }

        getActiveMember().onTick();

        if(ticks > Turntable.TURN_TIME){
            ticks = 0;

            if(!(getActiveMember().entity instanceof EntityPlayer)) {
                next();
            }
        }
    }

    public void begin(List<Entity> entities){
        for(Entity i : entities) {
            addMember(i);
        }

        activeMember = 0;
        onStart();
        onStartTurn(getActiveMember());
    }

    public boolean isInEncounter(Entity entity) {
        for(Member i : members) {
            if(i.entity.getEntityId() == entity.getEntityId()) {
                return true;
            }
        }

        return false;
    }

    public void onClose() {

    }

    public void onStart() {

    }

    public void onNextRound(){

    }

    public void onEndTurn(Member member) {
        TurnBased tb = member.getTB();
        tb.setTBState(TBState.FROZEN);
        tb.updateTrackers(member.entity);
        addNearbyMembers();
        member.entity.setGlowing(false);
        member.onEndTurn(this);
    }

    public List<Entity> getMembersAsEntities(){
        List<Entity> entities = new ArrayList<>();

        for(Member i : members){
            entities.add(i.entity);
        }

        return entities;
    }

    private void addNearbyMembers() {
        for(Entity i : EntityUtil.explode(getMembersAsEntities(), Turntable.ENCOUNTER_ADD_RADIUS)) {
            if(server.getEncounter(i) != null) {
                // TODO: MERGE ENCOUNTERS
                continue;
            }

            addMember(i);
        }
    }

    public void onStartTurn(Member member) {
        TurnBased tb = member.getTB();
        tb.setTBState(TBState.TURN);
        tb.setActionPoints(3f);
        tb.setDamageable(true);
        tb.setDamageable(false);
        tb.updateTrackers(member.entity);
        member.onStartTurn(this);
        member.entity.setGlowing(true);
        System.out.println("Turn: " + member.entity.getName());
    }

    public void clean(){
        for(Member i : new ArrayList<>(members)){
            if(i.entity.isDead){
                removeMember(i);
            }
        }

        sortByInitiative();
    }

    public void close(){
        onClose();
        for(Member i : new ArrayList<>(members)) {
            removeMember(i);
        }
        closed = true;
    }

    public void validate(){
        if(closed) {
            return;
        }
        if(members.size() < 2 && type == EncounterType.COMBAT){
            close();
            return;
        }

        if(members.isEmpty()) {
            close();
            return;
        }

        boolean hasPlayer = false;

        for(Member i : members){
            if(i.entity instanceof net.minecraft.entity.player.EntityPlayer){
                hasPlayer = true;
            }
        }

        if(!hasPlayer) {
            close();
        }
    }

    public boolean next(){
        validate();
        if(closed) {
            return false;
        }

        onEndTurn(getActiveMember());
        clean();

        activeMember++;
        if(activeMember >= members.size()){
            activeMember = 0;
            turn++;
            onNextRound();
        }

        onStartTurn(getActiveMember());

        return true;
    }

    public void removeMember(Member member) {
        members.remove(member);
        TurnBased tb =  member.getTB();
        tb.setTBState(TBState.FREE);
        tb.setDamageable(true);
        tb.updateTrackers(member.entity);
        member.onLeave(this);
    }

    public Member getActiveMember() {
        if(members.isEmpty()) {
            return null;
        }

        if(members.size() <= activeMember) {
            activeMember = members.size()-1;
        }

        return members.get(activeMember);
    }

    public int getMemberIndex(Member member) {
        return members.indexOf(member);
    }

    public void addMember(Entity entity) {
        if(isInEncounter(entity)) {
            return;
        }

        Member member = new Member(entity);
        member.getTB().setTBState(TBState.FROZEN);
        member.getTB().setDamageable(false);
        member.getTB().updateTrackers(member.entity);
        member.getTB().setInitiative(member.getTB().calculateInitiative(entity));
        members.add(member);
        Member a = getActiveMember();
        sortByInitiative();
        activeMember = getMemberIndex(a);
        member.onEnter(this);
    }

    public void sortByInitiative(){
        members.sort((o1, o2) -> o2.getTB().getInitiative() - o1.getTB().getInitiative());
    }
}
