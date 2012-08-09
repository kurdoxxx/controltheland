package com.btxtech.game.jsre.itemtypeeditor._OLD_;

import com.btxtech.game.jsre.client.action.ActionHandler;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.item.ItemContainer;
import com.btxtech.game.jsre.common.gameengine.itemType.ItemType;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncBaseItem;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncBuilder;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItemListener;
import com.google.gwt.core.client.Scheduler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 16.08.2011
 * Time: 12:28:30
 */
public class ItemTypeSimulation {
    private static final int BUILDUP_DELAY = 100;
    private SyncItem syncItem;
    private Logger log = Logger.getLogger(ItemTypeSimulation.class.getName());
    private int canvasWidth;
    private int canvasHeight;
    private ItemType itemType;
    private boolean doMove = false;
    private boolean doBuildup = false;
    private int imageNr = 0;
    private Index destination;
    private MuzzleFlashControl muzzleFlashControl;
    private BuildupStepEditorPanel buildupStepEditorPanel;
    private Index middle;

    public ItemTypeSimulation(int canvasWidth, int canvasHeight, ItemType itemType, MuzzleFlashControl muzzleFlashControl) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.itemType = itemType;
        this.muzzleFlashControl = muzzleFlashControl;
        muzzleFlashControl.setItemTypeSimulation(this);
    }

    public void setBuilupEditorPanel(BuildupStepEditorPanel buildupStepEditorPanel) {
        this.buildupStepEditorPanel = buildupStepEditorPanel;
    }

    private void executeMoveCommand() {
        syncItem.getSyncItemArea().setPosition(new Index(canvasWidth / 2, canvasHeight / 2));
        // TODO double angel = syncItem.getSyncItemArea().createBoundingBox().imageNumberToAngel(imageNr);
        Index middle = new Index(canvasWidth / 2, canvasHeight / 2);
        // TODO destination = middle.getPointFromAngelToNord(angel, 200);
        if (destination.getX() > canvasWidth - 1) {
            destination.setX(canvasWidth - 1);
        }
        if (destination.getY() > canvasHeight - 1) {
            destination.setY(canvasHeight - 1);
        }
        destination = Index.createSaveIndex(destination);
        ActionHandler.getInstance().move((SyncBaseItem) syncItem, destination);
    }

    public void createSyncItem() {
        try {
            middle = new Index(canvasWidth / 2, canvasHeight / 2);
            syncItem = ItemContainer.getInstance().createItemTypeEditorSyncObject(ItemTypeEditorPanel.MY_BASE, itemType.getId(), middle);
            syncItem.addSyncItemListener(new SyncItemListener() {
                @Override
                public void onItemChanged(Change change, SyncItem syncItem) {
                    if (doMove && change == Change.POSITION && destination != null && destination.equals(syncItem.getSyncItemArea().getPosition())) {
                        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                            @Override
                            public void execute() {
                                executeMoveCommand();
                            }
                        });
                    }
                }
            });
            if (syncItem instanceof SyncBaseItem) {
                SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
                muzzleFlashControl.initSyncItem(syncBaseItem);
                if (syncBaseItem.hasSyncConsumer()) {
                    syncBaseItem.getSyncConsumer().setOperationState(true);
                }
            }


        } catch (Exception e) {
            log.log(Level.SEVERE, "", e);
        }
    }

    public void onImageChanged(int imageNr) {
        this.imageNr = imageNr;
        if (doMove) {
            executeMoveCommand();
        }
    }

    public void doMove(boolean value) {
        doMove = value && (syncItem instanceof SyncBaseItem && ((SyncBaseItem) syncItem).hasSyncMovable());
        if (doMove) {
            executeMoveCommand();
        } else {
            muzzleFlashControl.stopAttack();
            if (syncItem instanceof SyncBaseItem && middle != null) {
                ((SyncBaseItem) syncItem).stop();
                syncItem.getSyncItemArea().setPosition(middle);
            }
        }
    }

    public void doBuildup(boolean value) {
        if (!(syncItem instanceof SyncBaseItem)) {
            doBuildup = false;
            return;
        }
        if (value) {
            doBuildup = true;
            Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
                @Override
                public boolean execute() {
                    SyncBaseItem syncBaseItem = ((SyncBaseItem) syncItem);
                    if (!doBuildup) {
                        ((SyncBaseItem) syncItem).setBuildup(1.0);
                        buildupStepEditorPanel.onBuildupProgress(((SyncBaseItem) syncItem).getBuildup());
                        return false;
                    }
                    if (syncBaseItem.isReady()) {
                        syncBaseItem.setBuildup(0.0);
                    } else {
                        double delta = SyncBuilder.setupBuildFactor((double) BUILDUP_DELAY / 1000.0, buildupStepEditorPanel.getProgress(), syncBaseItem.getBaseItemType(), syncBaseItem);
                        syncBaseItem.addBuildup(delta);
                    }
                    buildupStepEditorPanel.onBuildupProgress(syncBaseItem.getBuildup());
                    return true;
                }
            }, BUILDUP_DELAY);
        } else {
            doBuildup = false;
        }
    }


    public SyncItem getSyncItem() {
        return syncItem;
    }
}