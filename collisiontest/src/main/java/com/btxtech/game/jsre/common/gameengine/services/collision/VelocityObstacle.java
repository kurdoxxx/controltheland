package com.btxtech.game.jsre.common.gameengine.services.collision;

import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;

/**
 * Created by beat
 * on 09.06.2014.
 */
public class VelocityObstacle {
    private double start;
    private double end;

    public VelocityObstacle(SyncItem protagonist, SyncItem other) {
        double distance = protagonist.getDecimalPosition().getDistance(other.getDecimalPosition());
        double radius = protagonist.getRadius() + other.getRadius();
        double angel = Math.asin(radius / distance);
        double otherAngel = MathHelper.negateAngel(protagonist.getDecimalPosition().getAngleToNord(other.getDecimalPosition()));
        start = MathHelper.normaliseAngel(otherAngel - angel);
        end = MathHelper.normaliseAngel(otherAngel + angel);
    }

    public double getStartAngel() {
        return start;
    }

    public boolean isInside(double angel) {
        angel = MathHelper.normaliseAngel(angel);
        return !MathHelper.compareWithPrecision(angel, start) && !MathHelper.compareWithPrecision(angel, end) && MathHelper.isInSection(angel, start, end - start);
    }

    public double getEndAngel() {
        return end;
    }
}
