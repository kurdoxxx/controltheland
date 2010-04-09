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

package com.btxtech.game.jsre.client.cockpit.radar;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.terrain.TerrainScrollListener;
import com.btxtech.game.jsre.client.terrain.TerrainView;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainSettings;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.widgetideas.graphics.client.Color;

/**
 * User: beat
 * Date: 22.12.2009
 * Time: 21:52:27
 */
public class RadarFrameView extends MiniMap implements TerrainScrollListener, MouseDownHandler {
    public RadarFrameView(int width, int height) {
        super(width, height);
        TerrainView.getInstance().addTerrainScrollListener(this);
        addMouseDownHandler(this);
    }

    @Override
    public void onScroll(int left, int top, int width, int height, int deltaLeft, int deltaTop) {
        clear();
        beginPath();
        rect(left, top, width, height);
        stroke();
    }

    @Override
    public void onTerrainSettings(TerrainSettings terrainSettings) {
        super.onTerrainSettings(terrainSettings);
        setLineWidth(1.0 / getScaleX());
        setStrokeStyle(Color.LIGHTGREY);
        onScroll(TerrainView.getInstance().getViewOriginLeft(),
                TerrainView.getInstance().getViewOriginTop(),
                TerrainView.getInstance().getViewWidth(),
                TerrainView.getInstance().getViewHeight(),
                0,
                0);
    }

    @Override
    public void onMouseDown(MouseDownEvent mouseDownEvent) {
        int x = (int) ((double) mouseDownEvent.getRelativeX(this.getElement()) / getScaleX());
        int y = (int) ((double) mouseDownEvent.getRelativeY(this.getElement()) / getScaleY());
        TerrainView.getInstance().moveToMiddle(new Index(x, y));
    }
}
