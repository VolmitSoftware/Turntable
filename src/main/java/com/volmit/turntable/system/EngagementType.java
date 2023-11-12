package com.volmit.turntable.system;

public enum EngagementType {
    /**
     * Passive engagement means that it started with a player and passive entity. The engagement ends when all passives/hostiles
     * have been killed. 2+ players can be involved in a passive engagement and still end it.
     */
    PASSIVE,

    /**
     * Hostile engagement means that it started with a player and hostile entity. The engagement ends when all passives/hostiles
     */
    HOSTILE,

    /**
     * Player engagement means that it started with a player and another player. The engagement ends when all but one player
     * havs been killed.
     */
    PLAYER
}
