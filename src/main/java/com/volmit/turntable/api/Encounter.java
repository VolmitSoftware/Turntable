package com.volmit.turntable.api;

import com.volmit.turntable.Turntable;
import com.volmit.turntable.capability.TurnBased;
import com.volmit.turntable.util.EntityUtil;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class Encounter {
    public EncounterType type;
    public List<Member> members;
    public int activeMember;
    public int turn;
    public boolean closed;

    public Encounter(EncounterType type) {
        this.type = type;
        members = new ArrayList<>();
        activeMember = 0;
        turn = 0;
        closed = false;
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
        for(Member i : new ArrayList<>(members)) {
            removeMember(i);
        }
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
        for(Entity i : EntityUtil.explode(getMembersAsEntities(), Turntable.ENCOUNTER_RADIUS)) {
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
    }

    public boolean next(){
        if(closed) {
            return false;
        }

        if(members.size() < 2 && type == EncounterType.COMBAT){
            close();
            return false;
        }

        if(members.isEmpty()) {
            close();
            return false;
        }

        boolean hasPlayer = false;

        for(Member i : members){
            if(i.entity instanceof net.minecraft.entity.player.EntityPlayer){
                hasPlayer = true;
            }
        }

        if(!hasPlayer) {
            close();
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
        return members.get(activeMember);
    }

    public int getMemberIndex(Member member) {
        return members.indexOf(member);
    }

    public void addMember(Entity entity) {
        if(isInEncounter(entity)) {
            return;
        }

        Member a = getActiveMember();
        Member member = new Member(entity);
        member.getTB().setTBState(TBState.FROZEN);
        member.getTB().setDamageable(false);
        member.getTB().updateTrackers(member.entity);
        members.add(member);
        sortByInitiative();
        activeMember = getMemberIndex(a);
        member.onEnter(this);
    }

    public void sortByInitiative(){
        members.sort((o1, o2) -> o2.getTB().getInitiative() - o1.getTB().getInitiative());
    }
}
