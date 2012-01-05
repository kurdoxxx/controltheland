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

package com.btxtech.game.jsre.common.gameengine.services.terrain;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.terrain.TerrainListener;
import com.btxtech.game.jsre.client.item.ItemContainer;
import com.btxtech.game.jsre.common.gameengine.itemType.ItemType;
import com.google.gwt.user.client.Random;

/**
 * User: beat
 * Date: 05.02.2010
 * Time: 22:28:01
 */
public class AbstractTerrainServiceImpl implements AbstractTerrainService {
    private Collection<TerrainImagePosition> terrainImagePositions = new ArrayList<TerrainImagePosition>();
    private Map<Integer, TerrainImage> terrainImages = new HashMap<Integer, TerrainImage>();
    private ArrayList<TerrainListener> terrainListeners = new ArrayList<TerrainListener>();
    private TerrainSettings terrainSettings;

    @Override
    public Collection<TerrainImagePosition> getTerrainImagePositions() {
        return terrainImagePositions;
    }

    public void setTerrainImagePositions(Collection<TerrainImagePosition> terrainImagePositions) {
        this.terrainImagePositions = terrainImagePositions;
    }

    protected void addTerrainImagePosition(TerrainImagePosition terrainImagePosition) {
        terrainImagePositions.add(terrainImagePosition);
    }

    protected void removeTerrainImagePosition(TerrainImagePosition terrainImagePosition) {
        terrainImagePositions.remove(terrainImagePosition);
    }

    public void setTerrainSettings(TerrainSettings terrainSettings) {
        this.terrainSettings = terrainSettings;
    }

    @Override
    public TerrainSettings getTerrainSettings() {
        return terrainSettings;
    }

    @Override
    public void addTerrainListener(TerrainListener terrainListener) {
        terrainListeners.add(terrainListener);
    }

    protected void fireTerrainChanged() {
        for (TerrainListener terrainListener : terrainListeners) {
            terrainListener.onTerrainChanged();
        }
    }

    protected void setupTerrainImages(Collection<TerrainImage> terrainImages) {
        clearTerrainImages();
        for (TerrainImage terrainImage : terrainImages) {
            putTerrainImage(terrainImage);
        }
    }

    protected void clearTerrainImages() {
        terrainImages.clear();
    }

    protected void putTerrainImage(TerrainImage terrainImage) {
        terrainImages.put(terrainImage.getId(), terrainImage);
    }

    @Override
    public Collection<TerrainImage> getTerrainImages() {
        return new ArrayList<TerrainImage>(terrainImages.values());
    }

    @Override
    public List<TerrainImagePosition> getTerrainImagesInRegion(Rectangle absolutePxRectangle) {
        ArrayList<TerrainImagePosition> result = new ArrayList<TerrainImagePosition>();
        if (terrainSettings == null || terrainImagePositions == null) {
            return result;
        }
        Rectangle tileRect = convertToTilePosition(absolutePxRectangle);
        for (TerrainImagePosition terrainImagePosition : terrainImagePositions) {
            if (tileRect.adjoinsEclusive(getTerrainImagePositionRectangle(terrainImagePosition))) {
                result.add(terrainImagePosition);
            }
        }
        return result;
    }

    @Override
    public TerrainImagePosition getTerrainImagePosition(int absoluteX, int absoluteY) {
        if (terrainSettings == null || terrainImagePositions == null) {
            return null;
        }
        Index tileIndex = getTerrainTileIndexForAbsPosition(absoluteX, absoluteY);
        for (TerrainImagePosition terrainImagePosition : terrainImagePositions) {
            if (getTerrainImagePositionRectangle(terrainImagePosition).containsExclusive(tileIndex)) {
                return terrainImagePosition;
            }
        }
        return null;
    }

    @Override
    public Rectangle getTerrainImagePositionRectangle(TerrainImagePosition terrainImagePosition) {
        TerrainImage terrainImage = getTerrainImage(terrainImagePosition);
        return new Rectangle(terrainImagePosition.getTileIndex().getX(),
                terrainImagePosition.getTileIndex().getY(),
                terrainImage.getTileWidth(),
                terrainImage.getTileHeight());
    }

    @Override
    public TerrainImage getTerrainImage(TerrainImagePosition terrainImagePosition) {
        TerrainImage terrainImage = terrainImages.get(terrainImagePosition.getImageId());
        if (terrainImage == null) {
            throw new IllegalArgumentException(this + " getTerrainImagePosRect(): image id does not exit: " + terrainImagePosition.getImageId());
        }
        return terrainImage;
    }

    @Override
    public Index getTerrainTileIndexForAbsPosition(int x, int y) {
        return new Index(x / terrainSettings.getTileWidth(), y / terrainSettings.getTileHeight());
    }

    @Override
    public int getTerrainTileIndexForAbsXPosition(int x) {
        return x / terrainSettings.getTileWidth();
    }

    @Override
    public int getTerrainTileIndexForAbsYPosition(int y) {
        return y / terrainSettings.getTileHeight();
    }

    @Override
    public Index getTerrainTileIndexForAbsPosition(Index absolutePos) {
        return new Index(absolutePos.getX() / terrainSettings.getTileWidth(), absolutePos.getY() / terrainSettings.getTileHeight());
    }

    @Override
    public Index getAbsolutIndexForTerrainTileIndex(Index tileIndex) {
        return new Index(tileIndex.getX() * terrainSettings.getTileWidth(), tileIndex.getY() * terrainSettings.getTileHeight());
    }

    @Override
    public Index getAbsolutIndexForTerrainTileIndex(int xTile, int yTile) {
        return new Index(xTile * terrainSettings.getTileWidth(), yTile * terrainSettings.getTileHeight());
    }

    @Override
    public int getAbsolutXForTerrainTile(int xTile) {
        return xTile * terrainSettings.getTileWidth();
    }

    @Override
    public int getAbsolutYForTerrainTile(int yTile) {
        return yTile * terrainSettings.getTileHeight();
    }

    @Override
    public Rectangle convertToTilePosition(Rectangle rectangle) {
        Index start = getTerrainTileIndexForAbsPosition(rectangle.getStart());
        Index end = getTerrainTileIndexForAbsPosition(rectangle.getEnd());
        return new Rectangle(start, end);
    }

    @Override
    public Rectangle convertToAbsolutePosition(Rectangle rectangle) {
        Index start = getAbsolutIndexForTerrainTileIndex(rectangle.getStart());
        Index end = getAbsolutIndexForTerrainTileIndex(rectangle.getEnd());
        return new Rectangle(start, end);
    }

    @Override
    public List<Index> setupPathToDestination(Index start, Index destionation, int range) {
        Index destination = start.getPointWithDistance(range, destionation);
        ArrayList<Index> path = new ArrayList<Index>();
        path.add(destination);
        return path;
    }

    @Override
    public List<Index> setupPathToDestination(Index start, Index destination) {
        ArrayList<Index> path = new ArrayList<Index>();
        path.add(destination);
        return path;
    }

    @Override
    public boolean isFree(Index posititon, ItemType itemType) {
        Rectangle rectangle = new Rectangle(posititon.getX() - itemType.getWidth() / 2,
                posititon.getY() - itemType.getHeight() / 2,
                itemType.getWidth(),
                itemType.getHeight());
        return getTerrainImagesInRegion(rectangle).isEmpty();
    }

    @Override
    public boolean isTerrainPassable(Index posititon) {
        return posititon != null && !(posititon.getX() >= terrainSettings.getPlayFieldXSize() || posititon.getY() >= terrainSettings.getPlayFieldYSize())
                && getTerrainImagePosition(posititon.getX(), posititon.getY()) == null;

    }

    public Index getAbsoluteFreeTerrainInRegion(Index absolutePos, int targetMinRange, int targetMaxRange) {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            int x;
            int y;
            if (Random.nextBoolean()) {
                x = absolutePos.getX() + targetMinRange + Random.nextInt(targetMaxRange - targetMinRange);
            } else {
                x = absolutePos.getX() - targetMinRange - Random.nextInt(targetMaxRange - targetMinRange);
            }
            if (Random.nextBoolean()) {
                y = absolutePos.getY() + targetMinRange + Random.nextInt(targetMaxRange - targetMinRange);
            } else {
                y = absolutePos.getY() - targetMinRange - Random.nextInt(targetMaxRange - targetMinRange);
            }
            if (x < 0 || y < 0) {
                continue;
            }
            if (x > terrainSettings.getPlayFieldXSize() || y > terrainSettings.getPlayFieldYSize()) {
                continue;
            }

            Index point = new Index(x, y);
            if (!isTerrainPassable(point)) {
                continue;
            }
            Rectangle itemRectangle = new Rectangle(x - 50, y - 50, 100, 100);
            if (!ItemContainer.getInstance().getItemsInRect(itemRectangle, false).isEmpty()) {
                continue;
            }
            return point;
        }
        throw new IllegalStateException(this + " getAbsoluteFreeTerrainInRegion: Can not find free position");
    }

}