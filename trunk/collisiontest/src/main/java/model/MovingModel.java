package model;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.gameengine.services.collision.CollisionService;
import com.btxtech.game.jsre.common.gameengine.services.collision.impl.NoBetterPathFoundException;
import com.btxtech.game.jsre.common.gameengine.services.terrain.Terrain;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;
import scenario.Scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MovingModel {
    private static final int FRAMES_PER_SECOND = 60;
    private static final long TIMER_DELAY = 1000 / FRAMES_PER_SECOND;
    private long lastTick;
    final private List<SyncItem> syncItems = new ArrayList<SyncItem>();
    private CollisionService collisionService;
    private Scenario scenario;
    private Timer timer;
    private Terrain terrain;

    public void init() {
        startTimer();
    }

    private void startTimer() {
        stopTimer();
        timer = new Timer("ItemTimerTask", true);
        timer.schedule(new ItemTimerTask(), 0, TIMER_DELAY);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void restart() throws NoBetterPathFoundException {
        synchronized (syncItems) {
            syncItems.clear();
        }
        if (scenario != null) {
            scenario.init(this, terrain);
            scenario.start();
        }
    }

    public void setCollisionService(CollisionService collisionService) {
        this.collisionService = collisionService;
    }

    public void setScenario(Scenario scenario) throws NoBetterPathFoundException {
        if (this.scenario != null) {
            this.scenario.stop();
        }
        this.scenario = scenario;
        restart();
    }

    public void pause(boolean pause) {
        if (pause) {
            stopTimer();
        } else {
            lastTick = 0;
            startTimer();
        }
    }

    public void step(double factor) {
        stopTimer();
        tick(factor);
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public SyncItem createSyncItem(int radius, Index position, String debugName) {
        SyncItem syncItem = new SyncItem(radius, position);
        // collisionService.addSyncItem(syncItem);
        synchronized (syncItems) {
            syncItems.add(syncItem);
        }
        return syncItem;
    }

    public void deleteSyncItem(SyncItem syncItem) {
        // collisionService.removeSyncItem(syncItem);
        synchronized (syncItems) {
            syncItems.remove(syncItem);
        }
    }

    public CollisionService getCollisionService() {
        return collisionService;
    }

    class ItemTimerTask extends TimerTask {
        public void run() {
            if (lastTick == 0) {
                lastTick = System.currentTimeMillis();
                return;
            }
            double delta = System.currentTimeMillis() - lastTick;
            double factor = delta / 1000f;
            lastTick = System.currentTimeMillis();

            tick(factor);
        }
    }

    private void tick(double factor) {
        SyncItem debugSyncItem = null;
        try {
            if (scenario != null) {
                synchronized (syncItems) {
                    scenario.tick();
                }
            }

            synchronized (syncItems) {
                for (SyncItem syncItem : syncItems) {
                    debugSyncItem = syncItem;
                    if (syncItem.isMoving()) {
                        collisionService.moveItem(syncItem, factor);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("tick failed: " + debugSyncItem);
            e.printStackTrace();
        }
    }

    public List<SyncItem> getSyncItems() {
        return syncItems;
    }

    public double calculateDensityOfItems(Index middle, int radius) {
        double wholeArea = Math.PI * Math.pow(radius, 2);
        double itemArea = 0.0;
        for (SyncItem syncItem : syncItems) {
            if (syncItem.isMoving()) {
                continue;
            }
            if (middle.getDistance(syncItem.getPosition()) > radius) {
                continue;
            }
            itemArea += syncItem.calculateArea();
        }
        return itemArea / wholeArea;
    }

}
