package com.btxtech.game.jsre.common.gameengine.syncObjects;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Vector;
import com.btxtech.game.jsre.common.MathHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: beat
 * Date: 20.03.13
 * Time: 21:36
 */
public class SyncItem {
    public static final double MAX_TURN_SPEED = MathHelper.gradToRad(360 / 4);
    public static final double MAX_FORCE = 5.4;
    public static final double SLOWING_DOWN_RADIUS = 20;
    public static final double MAX_AVOID_AHEAD = 50;
    public static final double AVOID_FORCE = 20;
    public static final double MASS = 10;
    public static final double DELTA_ANGEL = MathHelper.ONE_RADIANT / 24;
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();
    public static final double SPEED = 50;
    public static final double REMAINING_GIVE_UP_TIME = 20;
    public static final double MAX_GIVE_UP_DISTANCE = 400;

    // General
    private int id;
    // Item type
    private int radius;
    // SyncItemArea
    private DecimalPosition decimalPosition;
    private DecimalPosition target;
    private double speed;
    private double angel;

    public SyncItem(int radius, Index position) {
        this.radius = radius;
        decimalPosition = new DecimalPosition(position);
        id = ID_GENERATOR.incrementAndGet();
    }

    public int getRadius() {
        return radius;
    }

    public Index getPosition() {
        return getDecimalPosition().getPosition();
    }

    public DecimalPosition getDecimalPosition() {
        return decimalPosition;
    }

    public int getDiameter() {
        return radius * 2;
    }

    public void moveTo(Index destination) {
        target = new DecimalPosition(destination);
    }

    public void moveTo(List<Index> wayPoint) {
    }

    public double getTargetAngel() {
        return decimalPosition.getAngleToNord(target);
    }

    public DecimalPosition getTargetPosition() {
        return target;
    }

    public void stop() {
        target = null;
    }

    public double calculateArea() {
        return Math.PI * Math.pow(radius, 2);
    }

    public int getId() {
        return id;
    }

    public boolean isMoving() {
        return target != null;
    }

    public DecimalPosition getVelocity(double factor) {
        return DecimalPosition.NULL.getPointFromAngelToNord(angel, speed * factor);
    }

    public void setVelocity(DecimalPosition velocity, double factor) {
        angel = DecimalPosition.NULL.getAngleToNord(velocity);
        speed =  DecimalPosition.NULL.getDistance(velocity) / factor;
    }

    public DecimalPosition getPreferredVelocity(double factor) {
        return DecimalPosition.NULL.getPointFromAngelToNord(getTargetAngel(), SPEED * factor);
    }

    public void executeMove(double factor) {
        decimalPosition = decimalPosition.getPointFromAngelToNord(angel, speed * factor);
    }

    @Override
    public String toString() {
        return "SyncItem{id=" + id + " Position: " + decimalPosition;
    }
}