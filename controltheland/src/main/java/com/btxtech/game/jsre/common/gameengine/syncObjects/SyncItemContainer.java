/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.game.jsre.common.gameengine.syncObjects;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.CommonJava;
import com.btxtech.game.jsre.common.gameengine.ItemDoesNotExistException;
import com.btxtech.game.jsre.common.gameengine.itemType.ItemContainerType;
import com.btxtech.game.jsre.common.gameengine.services.items.NoSuchItemTypeException;
import com.btxtech.game.jsre.common.gameengine.services.terrain.SurfaceType;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.UnloadContainerCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.syncInfos.SyncItemInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * User: beat
 * Date: 01.05.2010
 * Time: 11:38:49
 */
public class SyncItemContainer extends SyncBaseAbility {
    private ItemContainerType itemContainerType;
    private List<Id> containedItems = new ArrayList<Id>();
    private Index unloadPos;

    public SyncItemContainer(ItemContainerType itemContainerType, SyncBaseItem syncBaseItem) {
        super(syncBaseItem);
        this.itemContainerType = itemContainerType;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) throws NoSuchItemTypeException, ItemDoesNotExistException {
        unloadPos = syncItemInfo.getUnloadPos();
        containedItems = syncItemInfo.getContainedItems();
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        syncItemInfo.setUnloadPos(Index.saveCopy(unloadPos));
        syncItemInfo.setContainedItems(CommonJava.saveArrayListCopy(containedItems));
    }

    public void load(SyncBaseItem syncBaseItem) {
        isAbleToContainThrow(syncBaseItem);

        containedItems.add(syncBaseItem.getId());
        syncBaseItem.setContained(getSyncBaseItem().getId());
        getSyncBaseItem().fireItemChanged(SyncItemListener.Change.CONTAINER_COUNT_CHANGED);
    }

    private void isAbleToContainThrow(SyncBaseItem syncBaseItem) {
        if (getSyncBaseItem().equals(syncBaseItem)) {
            throw new IllegalArgumentException("Can not contain oneself: " + this);
        }

        if (!itemContainerType.isAbleToContain(syncBaseItem.getBaseItemType().getId())) {
            throw new IllegalArgumentException("Container " + getSyncBaseItem() + " is not able to contain: " + syncBaseItem);
        }

        if (!getServices().getTerritoryService().isAllowed(getSyncBaseItem().getSyncItemArea().getPosition(), getSyncBaseItem())) {
            throw new IllegalArgumentException(this + " Container not allowed to load on territory: " + getSyncBaseItem().getSyncItemArea().getPosition() + "  " + getSyncBaseItem());
        }

        if (containedItems.size() >= itemContainerType.getMaxCount()) {
            throw new IllegalArgumentException(this + " Container is full: " + containedItems.size());
        }
    }

    public boolean isAbleToContain(SyncBaseItem syncBaseItem) {
        try {
            isAbleToContainThrow(syncBaseItem);
            return true;
        } catch (IllegalArgumentException ignore) {
            return false;
        }
    }

    public void executeCommand(UnloadContainerCommand unloadContainerCommand) {
        if (containedItems.isEmpty()) {
            throw new IllegalStateException("No items in item container: " + getSyncBaseItem());
        }

        if (!getServices().getTerritoryService().isAllowed(unloadContainerCommand.getUnloadPos(), getSyncBaseItem())) {
            throw new IllegalArgumentException(this + " Container not allowed to unload on territory: " + unloadContainerCommand.getUnloadPos() + "  " + getSyncBaseItem());
        }
        setPathToDestinationIfSyncMovable(unloadContainerCommand.getPathToDestination());
        unloadPos = unloadContainerCommand.getUnloadPos();
    }

    public boolean tick(double factor) throws ItemDoesNotExistException {
        if (!getSyncBaseItem().isAlive()) {
            return false;
        }
        if (!isActive()) {
            return false;
        }

        if (getSyncBaseItem().getSyncMovable().tickMove(factor, null)) {
            return true;
        }

        if (!isInRange(unloadPos)) {
            if (isNewPathRecalculationAllowed()) {
                // Destination place was may be taken. Calculate a new one.
                recalculateNewPath(itemContainerType.getRange(), getSyncItemArea().getBoundingBox().createSyntheticSyncItemArea(unloadPos));
                getServices().getConnectionService().sendSyncInfo(getSyncBaseItem());
                return true;
            } else {
                return false;
            }
        }

        getSyncItemArea().turnTo(unloadPos);
        unload();
        stop();
        return false;
    }

    private boolean isInRange(Index unloadPos) {
        return getSyncItemArea().isInRange(itemContainerType.getRange(), getSyncItemArea().getBoundingBox().createSyntheticSyncItemArea(unloadPos));
    }

    private void unload() throws ItemDoesNotExistException {
        SurfaceType surfaceType = getServices().getTerrainService().getSurfaceTypeAbsolute(unloadPos);
        for (Iterator<Id> iterator = containedItems.iterator(); iterator.hasNext();) {
            Id containedItem = iterator.next();
            if (allowedUnload(surfaceType, containedItem)) {
                SyncBaseItem syncItem = (SyncBaseItem) getServices().getItemService().getItem(containedItem);
                syncItem.clearContained(unloadPos);
                getServices().getConnectionService().sendSyncInfo(syncItem);
                iterator.remove();
            }
        }
        getSyncBaseItem().fireItemChanged(SyncItemListener.Change.CONTAINER_COUNT_CHANGED);
    }

    public void stop() {
        unloadPos = null;
        if (getSyncBaseItem().hasSyncMovable()) {
            getSyncBaseItem().getSyncMovable().stop();
        }
    }

    public Index getUnloadPos() {
        return unloadPos;
    }

    public void setUnloadPos(Index unloadPos) {
        this.unloadPos = unloadPos;
    }

    public boolean isActive() {
        return unloadPos != null;
    }

    public int getRange() {
        return itemContainerType.getRange();
    }

    public ItemContainerType getItemContainerType() {
        return itemContainerType;
    }

    public List<Id> getContainedItems() {
        return containedItems;
    }

    public void setContainedItems(List<Id> containedItems) {
        this.containedItems = containedItems;
    }

    public boolean isAbleToContainAtLeastOne(Collection<SyncBaseItem> syncBaseItems) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (isAbleToContain(syncBaseItem)) {
                return true;
            }
        }
        return false;
    }

    public boolean atLeastOneAllowedToUnload(Index position) throws ItemDoesNotExistException {
        SurfaceType surfaceType = getServices().getTerrainService().getSurfaceTypeAbsolute(position);
        for (Id containedItem : containedItems) {
            if (allowedUnload(surfaceType, containedItem)) {
                return true;
            }
        }
        return false;
    }

    private boolean allowedUnload(SurfaceType surfaceType, Id containedItem) throws ItemDoesNotExistException {
        return getServices().getItemService().getItem(containedItem).getTerrainType().allowSurfaceType(surfaceType);
    }

    public boolean isUnloadPosReachable(Index position) {
        try {
            getServices().getTerrainService().getNearestPoint(getSyncBaseItem().getTerrainType(), position, getItemContainerType().getRange());
            return true;
        } catch (IllegalArgumentException ignore) {
            return false;
        }
    }
}