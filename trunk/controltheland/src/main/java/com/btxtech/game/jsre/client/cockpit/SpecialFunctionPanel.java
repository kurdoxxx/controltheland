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

import com.btxtech.game.jsre.client.ClientServices;
import com.btxtech.game.jsre.client.ExtendedCustomButton;
import com.btxtech.game.jsre.client.action.ActionHandler;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncBaseItem;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItemContainer;
import com.btxtech.game.jsre.common.tutorial.CockpitSpeechBubbleHintConfig;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: beat
 * Date: 18.11.2010
 * Time: 10:51:09
 */
public class SpecialFunctionPanel extends VerticalPanel implements HintWidgetProvider {
    private static final String TOOL_TIP_UPGRADE = "Upgrade this structure or unit";
    private static final String TOOL_TIP_UNLOAD = "Unload containing units";
    private static final String TOOL_TIP_LAUNCH = "Launch the missile";

    private static final int WIDTH = 92;
    private static final int HEIGHT = 76;
    private Widget unload;
    private Widget upgrade;
    private Widget launch;

    public SpecialFunctionPanel() {
        setPixelSize(WIDTH, HEIGHT);
    }

    public void display(SyncBaseItem syncBaseItem) {
        clear();
        if (syncBaseItem.isUpgradeable()) {
            addUpgradeable(syncBaseItem);
        }

        if (syncBaseItem.hasSyncItemContainer()) {
            addSyncItemContainer(syncBaseItem.getSyncItemContainer());
        }

        if (syncBaseItem.hasSyncLauncher()) {
            addSyncLauncher();
        }

    }

    private void addUpgradeable(final SyncBaseItem upgradeable) {
        ExtendedCustomButton button = new ExtendedCustomButton("/images/cockpit/upgradeButton-up.png", "/images/cockpit/upgradeButton-down.png", false, TOOL_TIP_UPGRADE, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ActionHandler.getInstance().upgrade(upgradeable);
            }
        });
        button.getUpDisabledFace().setImage(new Image("/images/cockpit/upgradeButton-disabled-up.png"));
        button.setEnabled(ClientServices.getInstance().getItemTypeAccess().isAllowed(upgradeable.getBaseItemType().getUpgradeable()));
        upgrade = button;
        add(button);
    }

    private void addSyncItemContainer(SyncItemContainer syncItemContainer) {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        ExtendedCustomButton button = new ExtendedCustomButton("/images/cockpit/unloadButton-up.png", "/images/cockpit/unloadButton-down.png", false, TOOL_TIP_UNLOAD, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Cockpit.getInstance().getCockpitMode().setUnloadMode();
            }
        });
        horizontalPanel.add(button);
        horizontalPanel.add(new HTML("&nbsp;Items: " + syncItemContainer.getContainedItems().size()));
        unload = button;
        add(horizontalPanel);
    }

    private void addSyncLauncher() {
        ExtendedCustomButton button = new ExtendedCustomButton("/images/cockpit/launchButton-up.png", "/images/cockpit/launchButton-down.png", false, TOOL_TIP_LAUNCH, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Cockpit.getInstance().getCockpitMode().setUnloadMode();
            }
        });
        launch = button;
        add(button);
    }

    @Override
    public Widget getHintWidgetAndEnsureVisible(CockpitSpeechBubbleHintConfig config) throws HintWidgetException {
        switch (config.getCockpitWidgetEnum()) {
            case UNLOAD:
                if (unload == null) {
                    throw new HintWidgetException("Unload button is not initialised", config);
                }
                return unload;
            case LAUNCH:
                if (upgrade == null) {
                    throw new HintWidgetException("Upgrade button is not initialised", config);
                }
                return upgrade;
            case UPGRADE:
                if (launch == null) {
                    throw new HintWidgetException("Launch button is not initialised", config);
                }
                return launch;
        }
        return null;
    }
}
