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

package com.btxtech.game.jsre.mapeditor;

import com.btxtech.game.jsre.client.ImageHandler;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.game.jsre.client.terrain.MapWindow;
import com.btxtech.game.jsre.client.terrain.TerrainView;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainImage;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainImagePosition;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseEvent;

import java.util.List;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 10.01.2010
 * Time: 19:37:39
 */
public class PlaceablePreviewTerrainImagePoition extends PlaceablePreviewWidget {
    private TerrainData terrainData;
    private TerrainImage terrainImage;
    private TerrainImagePosition terrainImagePosition;
    private TerrainImagePosition.ZIndex zIndex;
    private Logger log = Logger.getLogger(PlaceablePreviewTerrainImagePoition.class.getName());

    public PlaceablePreviewTerrainImagePoition(TerrainData terrainData, TerrainImagePosition terrainImagePosition, MouseEvent mouseEvent) {
        super(ImageHandler.getTerrainImage(terrainImagePosition.getImageId()), mouseEvent);
        this.terrainData = terrainData;
        this.terrainImagePosition = terrainImagePosition;
        zIndex = terrainImagePosition.getzIndex();
    }

    public PlaceablePreviewTerrainImagePoition(TerrainData terrainData, TerrainImage terrainImage, TerrainImagePosition.ZIndex zIndex, MouseDownEvent mouseDownEvent) {
        super(ImageHandler.getTerrainImage(terrainImage.getId()), mouseDownEvent);
        this.terrainData = terrainData;
        this.terrainImage = terrainImage;
        this.zIndex = zIndex;
    }

    @Override
    protected void execute(MouseEvent event) {
        int relX = event.getRelativeX(MapWindow.getAbsolutePanel().getElement());
        int relY = event.getRelativeY(MapWindow.getAbsolutePanel().getElement());
        if (relX < 0 || relY < 0) {
            return;
        }
        if (terrainImagePosition != null) {
            terrainData.moveTerrainImagePosition(relX, relY, terrainImagePosition);
        } else {
            terrainData.addNewTerrainImagePosition(relX, relY, terrainImage, zIndex);
        }
    }

    @Override
    protected int specialMoveX(int x) {
        int offset = TerrainView.getInstance().getViewOriginLeft();
        int tileX = TerrainView.getInstance().getTerrainHandler().getTerrainTileIndexForAbsXPosition(x + offset);
        return TerrainView.getInstance().getTerrainHandler().getAbsolutXForTerrainTile(tileX) - offset;
    }

    @Override
    protected int specialMoveY(int y) {
        int offset = TerrainView.getInstance().getViewOriginTop();
        int tileY = TerrainView.getInstance().getTerrainHandler().getTerrainTileIndexForAbsYPosition(y + offset);
        return TerrainView.getInstance().getTerrainHandler().getAbsolutYForTerrainTile(tileY) - offset;
    }

    @Override
    protected boolean allowedToPlace(int relX, int relY) {
        int tileX = TerrainView.getInstance().getTerrainHandler().getTerrainTileIndexForAbsXPosition(relX + TerrainView.getInstance().getViewOriginLeft());
        int tileY = TerrainView.getInstance().getTerrainHandler().getTerrainTileIndexForAbsYPosition(relY + TerrainView.getInstance().getViewOriginTop());
        TerrainImage tmpTerrainImage;
        if (terrainImage != null) {
            tmpTerrainImage = terrainImage;
        } else {
            tmpTerrainImage = TerrainView.getInstance().getTerrainHandler().getTerrainImage(terrainImagePosition.getImageId());
        }
        if (tileX < 0 || tileY < 0) {
            return false;
        }
        Rectangle rectangle = new Rectangle(tileX, tileY, tmpTerrainImage.getTileWidth(), tmpTerrainImage.getTileHeight());
        rectangle = TerrainView.getInstance().getTerrainHandler().convertToAbsolutePosition(rectangle);
        List<TerrainImagePosition> terrainImagePositions = terrainData.getTerrainImagesInRegion(rectangle);
        if (terrainImagePositions.isEmpty()) {
            return true;
        }
        if (terrainImagePositions.size() == 1 && terrainImagePositions.get(0).equals(this.terrainImagePosition)) {
            return true;
        }
        for (TerrainImagePosition imagePosition : terrainImagePositions) {
            if (imagePosition.getzIndex() == zIndex) {
                return false;
            }
        }
        return true;
    }

}
