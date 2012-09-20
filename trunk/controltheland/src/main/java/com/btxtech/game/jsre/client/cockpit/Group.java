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

package com.btxtech.game.jsre.client.cockpit;

import com.btxtech.game.jsre.client.GwtCommon;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.gameengine.itemType.BaseItemType;
import com.btxtech.game.jsre.common.gameengine.services.items.NoSuchItemTypeException;
import com.btxtech.game.jsre.common.gameengine.services.terrain.SurfaceType;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncBaseItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * User: beat
 * Date: 09.11.2009
 * Time: 23:05:45
 */
public class Group {
    private Collection<SyncBaseItem> syncBaseItems = new ArrayList<SyncBaseItem>();

    public Group() {
    }

    public Group(Collection<SyncBaseItem> selectedItems) {
        syncBaseItems = new ArrayList<SyncBaseItem>(selectedItems);
    }

    public void addItem(SyncBaseItem syncBaseItem) {
        syncBaseItems.add(syncBaseItem);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Collection<SyncBaseItem> otherSyncBaseItems = ((Group) o).syncBaseItems;

        if (syncBaseItems == null) {
            return otherSyncBaseItems == null;
        } else if (otherSyncBaseItems == null) {
            return false;
        }
        if (syncBaseItems.isEmpty() && otherSyncBaseItems.isEmpty()) {
            return true;
        }
        if (syncBaseItems.size() != otherSyncBaseItems.size()) {
            return false;
        }
        for (SyncBaseItem item1 : syncBaseItems) {
            boolean found = false;
            for (SyncBaseItem item2 : otherSyncBaseItems) {
                if (item1.equals(item2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }


    @Override
    public int hashCode() {
        return syncBaseItems != null ? syncBaseItems.hashCode() : 0;
    }

    public boolean canAttack() {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (syncBaseItem.hasSyncWeapon()) {
                return true;
            }
        }
        return false;
    }

    public boolean canCollect() {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (syncBaseItem.hasSyncHarvester()) {
                return true;
            }
        }
        return false;
    }

    public boolean canMove() {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (syncBaseItem.hasSyncMovable()) {
                return true;
            }
        }
        return false;
    }

    public boolean canLaunch() {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (syncBaseItem.hasSyncLauncher() && !syncBaseItem.getSyncLauncher().isActive()) {
                return true;
            }
        }
        return false;
    }

    public boolean canFinalizeBuild() {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (syncBaseItem.hasSyncBuilder()) {
                return true;
            }
        }
        return false;
    }

    public boolean onlyFactories() {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (!syncBaseItem.hasSyncFactory()) {
                return false;
            }
        }
        return true;
    }

    public boolean onlyConstructionVehicle() {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (!syncBaseItem.hasSyncBuilder()) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(SyncBaseItem syncBaseItem) {
        return syncBaseItems.contains(syncBaseItem);
    }

    public void remove(SyncBaseItem syncBaseItem) {
        syncBaseItems.remove(syncBaseItem);
    }

    public boolean isEmpty() {
        return syncBaseItems.isEmpty();
    }

    public int getCount() {
        return syncBaseItems.size();
    }

    public Collection<SyncBaseItem> getItems() {
        return syncBaseItems;
    }

    public Collection<SyncBaseItem> getSyncBaseItems() {
        return syncBaseItems;
    }

    public SyncBaseItem getFirst() {
        return syncBaseItems.iterator().next();
    }

    public int count() {
        return syncBaseItems.size();
    }

    public Map<BaseItemType, Collection<SyncBaseItem>> getGroupedItems() {
        HashMap<BaseItemType, Collection<SyncBaseItem>> map = new HashMap<BaseItemType, Collection<SyncBaseItem>>();
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            Collection<SyncBaseItem> collection = map.get(syncBaseItem.getBaseItemType());
            if (collection == null) {
                collection = new ArrayList<SyncBaseItem>();
                map.put(syncBaseItem.getBaseItemType(), collection);
            }
            collection.add(syncBaseItem);
        }
        return map;
    }

    public Collection<SurfaceType> getAllowedSurfaceTypes() {
        HashSet<SurfaceType> result = new HashSet<SurfaceType>();
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            result.addAll(syncBaseItem.getTerrainType().getSurfaceTypes());
        }
        return result;
    }


    public boolean atLeastOneItemTypeAllowed2Attack(SyncBaseItem syncBaseItem) {
        for (SyncBaseItem selectedSyncBaseItem : syncBaseItems) {
            if (selectedSyncBaseItem.hasSyncWeapon() && selectedSyncBaseItem.getSyncWeapon().isItemTypeAllowed(syncBaseItem)) {
                return true;
            }
        }
        return false;
    }

    public boolean atLeastOneItemTypeAllowed2FinalizeBuild(SyncBaseItem tobeFinalized) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (syncBaseItem.hasSyncBuilder() && syncBaseItem.getSyncBuilder().getBuilderType().isAbleToBuild(tobeFinalized.getItemType().getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean atLeastOneAllowedToLaunch(Index position) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (syncBaseItem.hasSyncLauncher()) {
                try {
                    int range = syncBaseItem.getSyncLauncher().getRange();
                    if (syncBaseItem.getSyncItemArea().getPosition().getDistance(position) <= range) {
                        return true;
                    }
                } catch (NoSuchItemTypeException e) {
                    GwtCommon.handleException(e);
                }
            }
        }
        return false;
    }


    public void keepOnlyOwnOfType(BaseItemType baseItemType) {
        for (Iterator<SyncBaseItem> iterator = syncBaseItems.iterator(); iterator.hasNext(); ) {
            BaseItemType currentBaseItemType = iterator.next().getBaseItemType();
            if (!(baseItemType.equals(currentBaseItemType))) {
                iterator.remove();
            }
        }
    }

}
