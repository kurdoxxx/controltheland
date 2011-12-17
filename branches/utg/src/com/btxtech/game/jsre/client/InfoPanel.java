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

import com.btxtech.game.jsre.client.common.GameInfo;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.ProgressBar;

/**
 * User: beat
 * Date: Aug 2, 2009
 * Time: 11:26:24 AM
 */
public class InfoPanel extends TopMapPanel {
    private static final InfoPanel INSTANCE = new InfoPanel();

    public HorizontalPanel userIdentification;
    private Label money;
    private Label xp;
    private Label name;
    private SimplePanel marker;
    private Label cursorPos;
    private ProgressBar energyBar;
    private int generating;
    private int consuming;

    /**
     * Simgleton
     */
    private InfoPanel() {

    }

    @Override
    protected Widget createBody() {
        FlexTable layout = new FlexTable();
        layout.setStyleName("topMapPanelText");
        layout.setCellSpacing(6);

        // Name
        layout.setHTML(0, 1, "Name");
        name = new Label("???");
        layout.setWidget(0, 2, name);

        // Color
        layout.setHTML(1, 1, "Color");
        marker = new SimplePanel();
        marker.setPixelSize(30, 15);
        layout.setWidget(1, 2, marker);

        // Money
        layout.setHTML(2, 1, "Money");
        money = new Label("???");
        layout.setWidget(2, 2, money);

        // Xp
        layout.setHTML(3, 1, "Xp");
        xp = new Label("???");
        layout.setWidget(3, 2, xp);

        // Energy
        layout.setHTML(4, 1, "Energy");
        energyBar = new ProgressBar(0, 0) {
            @Override
            protected String generateText(double curProgress) {
                return Integer.toString(consuming) + "/" + Integer.toString(generating);
            }
        };

        energyBar.setStyleName("gwt-EnergyBar-shell");
        energyBar.getElement().getStyle().setHeight(15, Style.Unit.PX);
        energyBar.getElement().getStyle().setColor("#000000");

        layout.getFlexCellFormatter().setColSpan(4, 1, 2);
        layout.setWidget(5, 1, energyBar);
        layout.getFlexCellFormatter().setColSpan(5, 1, 2);

        if (Game.isDebug()) {
            // Cursor
            layout.setHTML(6, 1, "Cursor");
            cursorPos = new Label("???");
            layout.setWidget(6, 2, cursorPos);
        }

        return layout;
    }

    public static InfoPanel getInstance() {
        return INSTANCE;
    }

    public void setGameInfo(GameInfo gameInfo) {
        name.setText(gameInfo.getBase().getName());
        marker.getElement().getStyle().setBackgroundColor(gameInfo.getBase().getHtmlColor());
        money.setText("$" + Integer.toString(gameInfo.getAccountBalance()));
        xp.setText(Integer.toString(gameInfo.getXp()));
        updateEnergy(gameInfo.getEnergyGenerating(), gameInfo.getEnergyConsuming());
    }

    public void updateMoney() {
        if (money != null) {
            money.setText(Integer.toString(ClientBase.getInstance().getAccountBalance()));
        }
    }

    public void updateXp(int amount) {
        if (xp != null) {
            xp.setText(Integer.toString(amount));
        }
    }


    public void setAbsoluteCureserPos(int x, int y) {
        if (cursorPos != null) {
            cursorPos.setText(x + ":" + y);
        }
    }

    public void updateEnergy(int generating, int consuming) {
        this.generating = generating;
        this.consuming = consuming;
        if (generating == 0) {
            energyBar.setMaxProgress(consuming);
        } else {
            energyBar.setMaxProgress(generating);
        }
        energyBar.setProgress(consuming);
    }
}