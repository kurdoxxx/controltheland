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

package com.btxtech.game.jsre.client.control.task;

import com.btxtech.game.jsre.client.ClientBase;
import com.btxtech.game.jsre.client.Connection;
import com.btxtech.game.jsre.client.cockpit.Cockpit;
import com.btxtech.game.jsre.client.cockpit.radar.RadarPanel;
import com.btxtech.game.jsre.client.common.info.RealityInfo;
import com.btxtech.game.jsre.client.control.StartupTaskEnum;
import com.btxtech.game.jsre.client.item.ClientItemTypeAccess;
import com.btxtech.game.jsre.client.territory.ClientTerritoryService;
import com.btxtech.game.jsre.client.utg.MissionTarget;

/**
 * User: beat
 * Date: 04.12.2010
 * Time: 13:13:08
 */
public class GameStartupTask extends GameEngineStartupTask {

    public GameStartupTask(StartupTaskEnum taskEnum) {
        super(taskEnum);
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        RealityInfo realityInfo = (RealityInfo) Connection.getInstance().getGameInfo();
        setupGameStructure(realityInfo);
        ClientBase.getInstance().setAllBaseAttributes(realityInfo.getAllBase());
        ClientBase.getInstance().setBase(realityInfo.getBase());
        ClientBase.getInstance().setAccountBalance(realityInfo.getAccountBalance());
        Cockpit.getInstance().setGameInfo(realityInfo);
        ClientItemTypeAccess.getInstance().setAllowedItemTypes(realityInfo.getAllowedItemTypes());
        RadarPanel.getInstance().updateEnergy(realityInfo.getEnergyGenerating(), realityInfo.getEnergyConsuming());
        MissionTarget.getInstance().setLevel(realityInfo.getLevel());
        ClientTerritoryService.getInstance().setTerritories(realityInfo.getTerritories());
        ClientBase.getInstance().setItemLimit(realityInfo.getItemLimit());
        ClientBase.getInstance().setHouseSpace(realityInfo.getHouseSpace());
        Cockpit.getInstance().enableOnlinePanel(true);
    }

}
