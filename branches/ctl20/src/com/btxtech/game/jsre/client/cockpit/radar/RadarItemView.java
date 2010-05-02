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

import com.btxtech.game.jsre.client.ClientSyncBaseItemView;
import com.btxtech.game.jsre.client.ClientSyncItemView;
import com.btxtech.game.jsre.client.ClientSyncResourceItemView;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.item.ItemContainer;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainSettings;
import com.google.gwt.user.client.Timer;
import com.google.gwt.widgetideas.graphics.client.Color;

/**
 * User: beat
 * Date: 06.04.2010
 * Time: 21:23:04
 */
public class RadarItemView extends MiniMap {
    public static final int BASE_ITEM_SIZE = 20;
    public static final int RESOURCE_ITEM_SIZE = 60;
    public static final double WHOLE_RADIUS = 2 * Math.PI;

    public RadarItemView(int width, int height) {
        super(width, height);
    }

    @Override
    public void onTerrainSettings(TerrainSettings terrainSettings) {
        super.onTerrainSettings(terrainSettings);
        setLineWidth(2.0 / getScaleX());
        Timer timer = new Timer() {

            @Override
            public void run() {
                refreshItems();
            }
        };
        timer.scheduleRepeating(1000);
    }

    private void refreshItems() {
        clear();
        for (ClientSyncItemView syncItemView : ItemContainer.getInstance().getItems()) {
            if (syncItemView instanceof ClientSyncBaseItemView) {
                ClientSyncBaseItemView baseItemView = (ClientSyncBaseItemView) syncItemView;
                Index pos = baseItemView.getSyncBaseItem().getPosition();
                if(pos == null) {
                    continue;
                }
                setStrokeStyle(new Color(baseItemView.getSyncBaseItem().getBase().getHtmlColor()));
                strokeRect(pos.getX(), pos.getY(), BASE_ITEM_SIZE, BASE_ITEM_SIZE);
            } else if (syncItemView instanceof ClientSyncResourceItemView) {
                Index pos = syncItemView.getSyncItem().getPosition();
                setStrokeStyle(Color.WHITE);                
                strokeRect(pos.getX(), pos.getY(), RESOURCE_ITEM_SIZE, RESOURCE_ITEM_SIZE);
            }
        }
    }
}
