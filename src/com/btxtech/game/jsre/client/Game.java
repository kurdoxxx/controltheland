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

package com.btxtech.game.jsre.client;

import com.btxtech.game.jsre.client.cockpit.CockpitPanel;
import com.btxtech.game.jsre.client.cockpit.TerrainMouseHandler;
import com.btxtech.game.jsre.client.common.Constants;
import com.btxtech.game.jsre.client.terrain.MapWindow;
import com.btxtech.game.jsre.client.terrain.TerrainView;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class Game implements EntryPoint {
    static private boolean isDebug = false;
    static public CockpitPanel cockpitPanel;

    public void onModuleLoad() {
        try {
            GwtCommon.setUncaughtExceptionHandler();

            StartupProbe.getInstance().init();
            StartupProbe.getInstance().taskSwitch(StartupTask.getFirstTask(), StartupTask.INIT_GUI);

            isDebug = Boolean.parseBoolean(Window.Location.getParameter("debug"));

            init();
            Connection.INSTANCE.start();
        } catch (Throwable t) {
            GwtCommon.handleException(t);
        }
    }

    public void init() {
        GwtCommon.disableBrowserContextMenuJSNI();
        //////////////////////////////////////////////////////
        CockpitNew.getInstance().addToParent(MapWindow.getAbsolutePanel());
        //////////////////////////////////////////////////////
        RootPanel.get().add(MapWindow.getAbsolutePanel(), 0, 0);

        //RadarPanel.getInstance().addToParent(MapWindow.getAbsolutePanel(), TopMapPanel.Direction.RIGHT_TOP, 30);

        cockpitPanel = new CockpitPanel();
        cockpitPanel.addToParent(MapWindow.getAbsolutePanel(), TopMapPanel.Direction.LEFT_BOTTOM, 30);

        InfoPanel.getInstance().addToParent(MapWindow.getAbsolutePanel(), TopMapPanel.Direction.LEFT_TOP, 30);

        OnlineBasePanel.getInstance().addToParent(MapWindow.getAbsolutePanel(), TopMapPanel.Direction.RIGHT_BOTTOM, 30);

        TerrainView.getInstance().addToParent(MapWindow.getAbsolutePanel());
        TerrainView.getInstance().getCanvas().getElement().getStyle().setZIndex(Constants.Z_INDEX_TERRAIN);
        TerrainView.getInstance().addTerrainScrollListener(MapWindow.getInstance());

        TerrainMouseHandler.getInstance(); // Just for activation
    }

    public static boolean isDebug() {
        return isDebug;
    }
}
