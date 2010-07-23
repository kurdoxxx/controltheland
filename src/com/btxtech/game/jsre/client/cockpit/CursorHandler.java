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

import com.btxtech.game.jsre.client.ClientSyncBaseItemView;
import com.btxtech.game.jsre.client.ClientSyncItemView;
import com.btxtech.game.jsre.client.GwtCommon;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.terrain.MapWindow;
import com.btxtech.game.jsre.client.terrain.TerrainMouseMoveListener;
import com.btxtech.game.jsre.client.terrain.TerrainView;
import com.btxtech.game.jsre.client.territory.ClientTerritoryService;
import com.btxtech.game.jsre.common.gameengine.services.terrain.SurfaceType;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncBaseItem;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItemContainer;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;
import java.util.Collection;

/**
 * User: beat
 * Date: Jun 27, 2009
 * Time: 9:43:48 AM
 */
public class CursorHandler implements TerrainMouseMoveListener {
    private static CursorHandler INSTANCE = new CursorHandler();
    private CursorState cursorState;

    /**
     * Singleton
     */
    private CursorHandler() {
        MapWindow.getInstance().setTerrainMouseMoveListener(this);
    }

    public static CursorHandler getInstance() {
        return INSTANCE;
    }

    public void setUnloadContainer() {
        cursorState.setCanUnload(true);
    }

    public void clearUnloadContainer() {
        cursorState.setCanUnload(false);
    }

    public void onOwnSelectionChanged(Group selection) {
        CursorState cursorState = new CursorState();
        if (selection.canMove()) {
            cursorState.setCanMove();
            cursorState.setCanLoad();
        }

        if (selection.canAttack()) {
            cursorState.setCanAttack();
        }

        if (selection.canCollect()) {
            cursorState.setCanCollect();
        }

        this.cursorState = cursorState;
    }

    public void onSelectionCleared() {
        cursorState = null;
        setTerrainCursor(null, false);
    }

    @Override
    public void onMove(int absoluteLeft, int absoluteTop, int relativeLeft, int relativeTop) {
        if (cursorState == null) {
            setTerrainCursor(null, false);
            return;
        }
        Index position = new Index(absoluteLeft, absoluteTop);

        if (cursorState.isCanUnload()) {
            setTerrainCursor(CursorType.UNLOAD, SelectionHandler.getInstance().atLeastOneAllowedOnTerrain4Selection(position) && atLeastOnAllowedForUnload(position));
        } else if (cursorState.isCanMove()) {
            Collection<SurfaceType> allowedSurfaceTypes = SelectionHandler.getInstance().getOwnSelectionSurfaceTypes();
            SurfaceType surfaceType = TerrainView.getInstance().getTerrainHandler().getSurfaceTypeAbsolute(position);
            boolean tmpIsMoveAllowed = allowedSurfaceTypes.contains(surfaceType);
            setTerrainCursor(CursorType.GO, tmpIsMoveAllowed);
        }
    }

    private boolean atLeastOnAllowedForUnload(Index position) {
        for (SyncBaseItem syncBaseItem : SelectionHandler.getInstance().getOwnSelection().getSyncBaseItems()) {
            if (syncBaseItem.hasSyncItemContainer()) {
                SyncItemContainer syncItemContainer = syncBaseItem.getSyncItemContainer();
                if (syncItemContainer.atLeastOneAllowedToUnload(position)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setItemCursor(ClientSyncItemView clientSyncItemView, CursorItemState cursorItemState) {
        if (cursorState == null) {
            setCursor(clientSyncItemView, null, false);
            return;
        }
        Index position = clientSyncItemView.getSyncItem().getPosition();
        if (cursorState.isCanAttack() && cursorItemState.isAttackTarget()) {
            setCursor(clientSyncItemView, CursorType.ATTACK,
                    SelectionHandler.getInstance().atLeastOneAllowedOnTerrain4Selection()
                            && SelectionHandler.getInstance().atLeastOneAllowedOnTerrain4Selection(position)
                            && SelectionHandler.getInstance().atLeastOneItemTypeAllowed2Attack4Selection(((ClientSyncBaseItemView) clientSyncItemView).getSyncBaseItem()));
        } else if (cursorState.isCanCollect() && cursorItemState.isCollectTarget()) {
            setCursor(clientSyncItemView, CursorType.COLLECT, SelectionHandler.getInstance().atLeastOneAllowedOnTerrain4Selection(position));
        } else if (cursorState.isCanLoad() && cursorItemState.isLoadTarget() && isNotMyself(clientSyncItemView)) {
            SyncItemContainer syncItemContainer = ((ClientSyncBaseItemView) clientSyncItemView).getSyncBaseItem().getSyncItemContainer();
            boolean allowed = ClientTerritoryService.getInstance().isAllowed(position, ((ClientSyncBaseItemView) clientSyncItemView).getSyncBaseItem())
                    && syncItemContainer.isAbleToContainAtLeastOne(SelectionHandler.getInstance().getOwnSelection().getSyncBaseItems())
                    && atLeastOneLoadPosReachable(SelectionHandler.getInstance().getOwnSelection().getSyncBaseItems(), syncItemContainer);
            setCursor(clientSyncItemView, CursorType.LOAD, allowed);
        }
    }

    private boolean isNotMyself(ClientSyncItemView clientSyncItemView) {
        SyncBaseItem my = ((ClientSyncBaseItemView) clientSyncItemView).getSyncBaseItem();
        for (SyncBaseItem syncBaseItem : SelectionHandler.getInstance().getOwnSelection().getSyncBaseItems()) {
            if (syncBaseItem.equals(my)) {
                return false;
            }
        }
        return true;
    }

    private boolean atLeastOneLoadPosReachable(Collection<SyncBaseItem> syncBaseItems, SyncItemContainer syncItemContainer) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (syncBaseItem.hasSyncMovable() && syncBaseItem.getSyncMovable().isLoadPosReachable(syncItemContainer)) {
                return true;
            }
        }
        return false;
    }

    private void setTerrainCursor(CursorType cursorType, boolean allowed) {
        setCursor(MapWindow.getAbsolutePanel(), cursorType, allowed);
    }

    private void setCursor(Widget widget, CursorType cursorType, boolean allowed) {
        if (cursorType == null) {
            widget.getElement().getStyle().setCursor(Style.Cursor.POINTER);
            return;
        }

        String url;
        Style.Cursor alternativeDefault;
        if (allowed) {
            url = cursorType.getUrl();
            alternativeDefault = cursorType.getAlternativeDefault();
        } else {
            url = cursorType.getNoUrl();
            alternativeDefault = cursorType.getNoAlternativeDefault();
        }

        if (GwtCommon.isOpera() || url == null) {
            widget.getElement().getStyle().setCursor(alternativeDefault);
        } else {
            widget.getElement().getStyle().setProperty("cursor", "url(" + url + "), " + alternativeDefault.getCssName());
        }

    }

}