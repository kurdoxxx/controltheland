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

package com.btxtech.game.jsre.common.gameengine.services.items;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.NotYourBaseException;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.game.jsre.common.SimpleBase;
import com.btxtech.game.jsre.common.gameengine.ItemDoesNotExistException;
import com.btxtech.game.jsre.common.gameengine.itemType.BaseItemType;
import com.btxtech.game.jsre.common.gameengine.itemType.BoundingBox;
import com.btxtech.game.jsre.common.gameengine.itemType.ItemType;
import com.btxtech.game.jsre.common.gameengine.services.Services;
import com.btxtech.game.jsre.common.gameengine.services.base.HouseSpaceExceededException;
import com.btxtech.game.jsre.common.gameengine.services.base.ItemLimitExceededException;
import com.btxtech.game.jsre.common.gameengine.syncObjects.Id;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncBaseItem;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;

import java.util.Collection;
import java.util.List;

/**
 * User: beat
 * Date: 18.11.2009
 * Time: 22:01:19
 */
public interface ItemService {
    SyncItem getItem(Id id) throws ItemDoesNotExistException;

    List<SyncBaseItem> getBaseItems(List<Id> baseItemsIds) throws ItemDoesNotExistException;

    List<Id> getBaseItemIds(List<SyncBaseItem> baseItems);

    void killSyncItem(SyncItem killedItem, SimpleBase actor, boolean force, boolean explode);

    SyncItem createSyncObject(ItemType toBeBuilt, Index position, SyncBaseItem creator, SimpleBase base, int createdChildCount) throws NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException;

    boolean baseObjectExists(SyncItem currentBuildup);

    ItemType getItemType(int itemTypeId) throws NoSuchItemTypeException;

    ItemType getItemType(String name) throws NoSuchItemTypeException;

    List<ItemType> getItemTypes();

    boolean areItemTypesLoaded();

    SyncItem newSyncItem(Id id, Index position, int itemTypeId, SimpleBase base, Services services) throws NoSuchItemTypeException;

    List<BaseItemType> ableToBuild(BaseItemType toBeBuilt);

    Collection<SyncBaseItem> getItems4Base(SimpleBase simpleBase);

    Collection<? extends SyncItem> getItems(ItemType itemType, SimpleBase simpleBase);

    Collection<SyncBaseItem> getEnemyItems(SimpleBase base, Rectangle region, boolean ignoreBot);

    boolean hasStandingItemsInRect(Rectangle rectangle, SyncItem exceptThat);

    boolean isSyncItemOverlapping(SyncItem syncItem);

    boolean isSyncItemOverlapping(SyncItem syncItem, Index positionToCheck, Double angelToCheck, Collection<SyncItem> exceptionThem);

    boolean isUnmovableSyncItemOverlapping(BoundingBox boundingBox, Index positionToCheck);

    void checkBuildingsInRect(BaseItemType toBeBuiltType, Index toBeBuildPosition);

    Collection<SyncBaseItem> getBaseItemsInRadius(Index position, int radius, SimpleBase simpleBase, Collection<BaseItemType> baseItemTypeFilter);

    Collection<SyncBaseItem> getBaseItemsInRectangle(Rectangle rectangle, SimpleBase simpleBase, Collection<BaseItemType> baseItemTypeFilter);

    boolean hasItemsInRectangle(Rectangle rectangle);

    void killSyncItems(Collection<SyncItem> syncItems);

    SyncBaseItem getFirstEnemyItemInRange(SyncBaseItem baseSyncItem, boolean ignoreBot);

    void sellItem(Id id) throws ItemDoesNotExistException, NotYourBaseException;
}