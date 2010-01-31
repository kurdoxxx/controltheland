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

package com.btxtech.game.jsre.client.terrain;

import com.btxtech.game.jsre.client.ClientSyncItemView;
import com.btxtech.game.jsre.client.ExtendedCanvas;
import com.btxtech.game.jsre.client.GwtCommon;
import com.btxtech.game.jsre.client.ClientSyncBaseItemView;
import com.btxtech.game.jsre.client.utg.missions.ScrollMission;
import com.btxtech.game.jsre.client.common.Constants;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.item.ItemContainer;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User: beat
 * Date: May 22, 2009
 * Time: 12:51:09 PM
 */
public class TerrainView implements MouseDownHandler, MouseOutHandler, MouseUpHandler, TerrainListener {
    public static final int AUTO_SCROLL_DETECTION_WIDTH = 40;
    public static final int AUTO_SCROLL_MOVE_DISTANCE = 10;
    private static final TerrainView INSTANCE = new TerrainView();
    private int viewOriginLeft = 0;
    private int viewOriginTop = 0;
    private int viewWidth = 1;
    private int viewHeight = 1;
    private TerrainMouseButtonListener terrainMouseButtonListener;
    private ArrayList<TerrainScrollListener> terrainScrollListeners = new ArrayList<TerrainScrollListener>();
    private ExtendedCanvas canvas = new ExtendedCanvas();
    private AbsolutePanel parent;
    private TerrainHandler terrainHandler = new TerrainHandler();

    /**
     * Singleton
     */
    private TerrainView() {
        canvas.addMouseDownHandler(this);
        canvas.addMouseOutHandler(this);
        canvas.addMouseUpHandler(this);
        canvas.sinkEvents(Event.ONMOUSEMOVE);
    }

    public void setupTerrain(int[][] terrainField, Collection<Integer> passableTerrainTileIds) {
        if (terrainField == null || terrainField.length == 0 || terrainField[0] == null || terrainField[0].length == 0) {
            GwtCommon.sendLogToServer("Invalid terrain data received");
            return;
        }
        terrainHandler.addTerrainListener(this);
        terrainHandler.setupTerrain(terrainField, passableTerrainTileIds);
    }

    public static TerrainView getInstance() {
        return INSTANCE;
    }

    private void drawMap() {
        if (!terrainHandler.isLoaded()) {
            return;
        }
        int tileXStart = viewOriginLeft / Constants.TILE_WIDTH;
        int tileXEnd = (viewOriginLeft + viewWidth) / Constants.TILE_WIDTH;
        int tileYStart = viewOriginTop / Constants.TILE_HEIGHT;
        int tileYEnd = (viewOriginTop + viewHeight) / Constants.TILE_HEIGHT;

        int tileLeftOffset = viewOriginLeft % Constants.TILE_WIDTH;
        int tileRightOffset = (viewOriginLeft + viewWidth) % Constants.TILE_WIDTH;
        int tileTopOffset = viewOriginTop % Constants.TILE_HEIGHT;
        int tileBottomOffset = (viewOriginTop + viewHeight) % Constants.TILE_HEIGHT;

        canvas.clear();
        int posX = 0;
        int posY;
        for (int x = tileXStart; x <= tileXEnd; x++) {
            posY = 0;
            int srcXStart;
            int srcXWidth;
            if (x == tileXStart) {
                // first column
                srcXStart = tileLeftOffset;
                srcXWidth = Constants.TILE_WIDTH - tileLeftOffset;
            } else if (x == tileXEnd) {
                // last column
                srcXStart = 0;
                srcXWidth = tileRightOffset;
            } else {
                // middle
                srcXStart = 0;
                srcXWidth = Constants.TILE_WIDTH;
            }
            if (srcXWidth == 0) {
                // Sould never happen but happens in opera
                continue;
            }
            for (int y = tileYStart; y <= tileYEnd; y++) {
                int srcYStart;
                int srcYWidth;
                if (y == tileYStart) {
                    // first row
                    srcYStart = tileTopOffset;
                    srcYWidth = Constants.TILE_HEIGHT - tileTopOffset;
                } else if (y == tileYEnd) {
                    // last row
                    srcYStart = 0;
                    srcYWidth = tileBottomOffset;
                } else {
                    // middle
                    srcYStart = 0;
                    srcYWidth = Constants.TILE_HEIGHT;
                }
                if (srcYWidth == 0) {
                    // Sould never happen but happens in opera
                    continue;
                }

                int tileId = terrainHandler.getTileId(x, y);
                ImageElement imageElement = terrainHandler.getTileImageElement(tileId);
                if (imageElement != null && imageElement.getHeight() == Constants.TILE_HEIGHT && imageElement.getWidth() == Constants.TILE_WIDTH) {
                    try {
                        canvas.drawImage(imageElement, srcXStart, srcYStart, srcXWidth, srcYWidth, posX, posY, srcXWidth, srcYWidth);
                    } catch (Throwable t) {
                        GwtCommon.handleException(t);

                        StringBuilder builder = new StringBuilder();
                        builder.append("imageElement: ");
                        builder.append(imageElement);
                        builder.append("\n");

                        builder.append("srcXStart: ");
                        builder.append(srcXStart);
                        builder.append("\n");

                        builder.append("srcYStart: ");
                        builder.append(srcYStart);
                        builder.append("\n");

                        builder.append("srcXWidth: ");
                        builder.append(srcXWidth);
                        builder.append("\n");

                        builder.append("srcYWidth: ");
                        builder.append(srcYWidth);
                        builder.append("\n");

                        builder.append("posX: ");
                        builder.append(posX);
                        builder.append("\n");

                        builder.append("posY: ");
                        builder.append(posY);
                        builder.append("\n");

                        builder.append("srcXWidth: ");
                        builder.append(srcXWidth);
                        builder.append("\n");

                        builder.append("srcYWidth: ");
                        builder.append(srcYWidth);
                        builder.append("\n");

                        GwtCommon.sendLogToServer(builder.toString());
                    }
                }
                posY += srcYWidth;
            }
            posX += srcXWidth;
        }

    }

    public void move(int left, int top) {
        if (viewWidth == 0 && viewHeight == 0) {
            return;
        }

        int orgViewOriginLeft = viewOriginLeft;
        int orgViewOriginTop = viewOriginTop;

        int tmpViewOriginLeft = viewOriginLeft + left;
        int tmpViewOriginTop = viewOriginTop + top;


        if (tmpViewOriginLeft < 0) {
            left = left - tmpViewOriginLeft;
        } else if (tmpViewOriginLeft > terrainHandler.getTerrainWidth() - viewWidth - 1) {
            left = left - (tmpViewOriginLeft - (terrainHandler.getTerrainWidth() - viewWidth)) - 1;
        }
        if (viewWidth > terrainHandler.getTerrainWidth()) {
            left = -viewOriginLeft;
            viewOriginLeft = 0;
        } else {
            viewOriginLeft += left;
        }

        if (tmpViewOriginTop < 0) {
            top = top - tmpViewOriginTop;
        } else if (tmpViewOriginTop > terrainHandler.getTerrainHeight() - viewHeight - 1) {
            top = top - (tmpViewOriginTop - (terrainHandler.getTerrainHeight() - viewHeight)) - 1;
        }
        if (viewHeight > terrainHandler.getTerrainHeight()) {
            top = 0;
            viewOriginTop = 0;
        } else {
            viewOriginTop += top;
        }

        if (orgViewOriginLeft == viewOriginLeft && orgViewOriginTop == viewOriginTop) {
            // No move
            return;
        }

        drawMap();
        fireScrollEvent(left, top);
    }

    public int getViewOriginLeft() {
        return viewOriginLeft;
    }

    public int getViewOriginTop() {
        return viewOriginTop;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public void moveToMiddle(ClientSyncItemView clientSyncItemView) {
        int left = clientSyncItemView.getSyncItem().getPosition().getX() - parent.getOffsetWidth() / 2 - viewOriginLeft;
        int top = clientSyncItemView.getSyncItem().getPosition().getY() - parent.getOffsetHeight() / 2 - viewOriginTop;
        move(left, top);
    }

    public void moveToMiddle(Index startPoint) {
        int left = startPoint.getX() - parent.getOffsetWidth() / 2 - viewOriginLeft;
        int top = startPoint.getY() - parent.getOffsetHeight() / 2 - viewOriginTop;
        move(left, top);
    }

    public GWTCanvas getCanvas() {
        return canvas;
    }

    public void moveToHome() {
        ClientSyncBaseItemView scrollTo = null;
        for (ClientSyncBaseItemView itemView : ItemContainer.getInstance().getOwnItems()) {
            if (itemView.getSyncBaseItem().hasSyncFactory()) {
                scrollTo = itemView;
                break;
            }
            if (itemView.getSyncBaseItem().hasSyncBuilder()) {
                scrollTo = itemView;
                break;
            }
            scrollTo = itemView;
        }
        if (scrollTo != null) {
            moveToMiddle(scrollTo);
        }
    }

    public void addToParent(final AbsolutePanel parent) {
        this.parent = parent;
        parent.add(canvas);
        viewWidth = parent.getOffsetWidth();
        viewHeight = parent.getOffsetHeight();
        canvas.resize(viewWidth, viewHeight);
        drawMap();
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent resizeEvent) {
                viewWidth = parent.getOffsetWidth();
                viewHeight = parent.getOffsetHeight();
                canvas.resize(viewWidth, viewHeight);
                drawMap();
                fireScrollEvent(0, 0);
            }
        });
    }

    private void fireScrollEvent(int deltaLeft, int deltaTop) {
        for (TerrainScrollListener terrainScrollListener : terrainScrollListeners) {
            terrainScrollListener.onScroll(viewOriginLeft, viewOriginTop, viewWidth, viewHeight, deltaLeft, deltaTop);
        }
    }

    public void setTerrainMouseButtonListener(TerrainMouseButtonListener terrainMouseButtonListener) {
        this.terrainMouseButtonListener = terrainMouseButtonListener;
    }

    public void addTerrainScrollListener(TerrainScrollListener terrainScrollListener) {
        terrainScrollListeners.add(terrainScrollListener);
    }

    public void removeTerrainScrollListener(ScrollMission terrainScrollListener) {
        terrainScrollListeners.remove(terrainScrollListener);
    }

    @Override
    public void onMouseDown(MouseDownEvent mouseDownEvent) {
        int x = mouseDownEvent.getRelativeX(canvas.getElement()) + viewOriginLeft;
        int y = mouseDownEvent.getRelativeY(canvas.getElement()) + viewOriginTop;
        if (terrainMouseButtonListener != null) {
            terrainMouseButtonListener.onMouseDown(x, y, mouseDownEvent);
        }
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        // Ignore
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        int x = event.getRelativeX(canvas.getElement()) + viewOriginLeft;
        int y = event.getRelativeY(canvas.getElement()) + viewOriginTop;
        if (terrainMouseButtonListener != null) {
            terrainMouseButtonListener.onMouseUp(x, y, event);
        }
    }

    public TerrainHandler getTerrainHandler() {
        return terrainHandler;
    }

    @Override
    public void onTerrainChanged() {
        drawMap();
    }

}
