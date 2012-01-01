/*
 * Copyright (c) 2011.
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

package com.btxtech.game.jsre.common.utg.condition;

import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;

/**
 * User: beat
 * Date: 18.07.2010
 * Time: 21:06:41
 */
public class SyncItemIdPositionComparison extends AbstractSyncItemComparison {
    private int syncObjectId;
    private Rectangle region;
    private boolean isFulfilled = false;

    public SyncItemIdPositionComparison(Integer excludedTerritoryId, int syncObjectId, Rectangle region) {
        super(excludedTerritoryId);
        this.syncObjectId = syncObjectId;
        this.region = region;
    }

    @Override
    protected void privateOnSyncItem(SyncItem syncItem) {
        if (syncItem.getId().getId() == syncObjectId) {
            isFulfilled = syncItem.getSyncItemArea().contains(region);
        }
    }

    @Override
    public boolean isFulfilled() {
        return isFulfilled;
    }

}