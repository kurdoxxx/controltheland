package com.btxtech.game.jsre.common.gameengine.services.collision;

import com.btxtech.game.jsre.client.common.Constants;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.gameengine.services.terrain.Terrain;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;
import model.MovingModel;

import javax.xml.ws.Holder;

/**
 * User: beat
 * Date: 21.03.13
 * Time: 02:00
 */
public class CollisionService {
    private final MovingModel movingModel;
    public static final double DENSITY_OF_ITEM = 0.5;
    public static final double MAX_DISTANCE = 50;

    public CollisionService(MovingModel movingModel) {
        this.movingModel = movingModel;
    }

    public void init(Terrain terrain) {
        int xTiles = (int) Math.ceil(terrain.getXCount() * Constants.TERRAIN_TILE_WIDTH / Constants.COLLISION_TILE_WIDTH);
        int yTiles = (int) Math.ceil(terrain.getYCount() * Constants.TERRAIN_TILE_HEIGHT / Constants.COLLISION_TILE_HEIGHT);
    }

    public void moveItem(Terrain terrain, MovingModel movingModel, final SyncItem syncItem, double factor) {
        if (syncItem.getPosition().equals(syncItem.getTargetPosition().getPosition())) {
            syncItem.stop();
        } else {
            if (syncItem.getStatus() == SyncItem.Status.GAVE_UP) {
                moveItemGaveUp(movingModel, syncItem, factor);
            } else {
                moveItem(movingModel, syncItem, factor);
            }
        }

    }

    private void moveItemGaveUp(MovingModel movingModel, final SyncItem syncItem, double factor) {
        syncItem.setSpeed(SyncItem.SPEED);
        syncItem.setAimAngel(syncItem.getTargetAngel());
        final DecimalPosition positionProposal = syncItem.calculateExecuteMove(factor);
        final Holder<Boolean> crash = new Holder<>(false);
        movingModel.iterateOverSyncItems(new MovingModel.SyncItemCallback() {
            @Override
            public void onSyncItem(SyncItem other) {
                if (other == syncItem) {
                    return;
                }
                if (positionProposal.getDistance(other.getDecimalPosition()) - syncItem.getRadius() - other.getRadius() < 0) {
                    crash.value = true;
                }
            }
        });

        if (crash.value) {
            syncItem.stop();
        } else {
            syncItem.executeMove(factor);
        }
    }

    private void moveItem(MovingModel movingModel, SyncItem syncItem, double factor) {
        final VelocityObstacleManager velocityObstacleManager = new VelocityObstacleManager(syncItem);
        movingModel.iterateOverSyncItems(new MovingModel.SyncItemCallback() {
            @Override
            public void onSyncItem(SyncItem other) {
                velocityObstacleManager.inspect(other);
            }
        });

        Double bestAngel = velocityObstacleManager.getBestAngel();
        if (bestAngel != null) {
            syncItem.setSpeed(SyncItem.SPEED);
            syncItem.setAimAngel(bestAngel);
        } else {
            syncItem.setSpeed(0);
        }
        syncItem.executeMove(factor);
        syncItem.handleGiveUpTimer(factor);

        // if (!isBetterPositionAvailable(syncItem)) {
        //     syncItem.stop();
        // }
    }

    private boolean isBetterPositionAvailable(SyncItem syncItem) {
        return !syncItem.getPosition().equals(syncItem.getTargetPosition().getPosition())
                && movingModel.calculateDensityOfItems(syncItem.getTargetPosition().getPosition(), syncItem.getPosition().getDistance(syncItem.getTargetPosition().getPosition())) < DENSITY_OF_ITEM;
    }

    public void findPath(SyncItem syncItem, Index targetPosition) {
        syncItem.moveTo(targetPosition);
    }

}
