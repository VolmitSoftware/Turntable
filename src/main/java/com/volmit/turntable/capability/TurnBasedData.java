package com.volmit.turntable.capability;

import com.volmit.turntable.api.TBState;

public class TurnBasedData implements TurnBased {
    private TBState state;
    private float actionPoints;
    private boolean damageable;
    private int initiative;

    public TurnBasedData() {
        state = TBState.FREE;
        actionPoints = 0;
        damageable = false;
        initiative = 0;
    }

    @Override
    public TBState getTBState() {
        return state;
    }

    @Override
    public void setTBState(TBState state) {
        this.state = state;
    }

    @Override
    public float getActionPoints() {
        return actionPoints;
    }

    @Override
    public void setActionPoints(float actionPoints) {
        this.actionPoints = actionPoints;
    }

    @Override
    public boolean isDamageable() {
        return damageable;
    }

    @Override
    public void setDamageable(boolean damageable) {
        this.damageable = damageable;
    }

    @Override
    public int getInitiative() {
        return initiative;
    }

    @Override
    public void setInitiative(int initiative) {
        this.initiative = initiative;
    }
}
