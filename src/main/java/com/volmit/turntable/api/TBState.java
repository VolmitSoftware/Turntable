package com.volmit.turntable.api;

public enum TBState {
    /**
     * They are frozen, no actions can be taken even if they have action points.
     */
    FROZEN,

    /**
     * They are free to take actions without using action points. They are not in tb mode
     */
    FREE,

    /**
     * They are in tb mode and can take actions using action points
     */
    TURN;
}
