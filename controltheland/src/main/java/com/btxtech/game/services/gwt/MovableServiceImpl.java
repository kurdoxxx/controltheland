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

package com.btxtech.game.services.gwt;


import com.btxtech.game.jsre.client.MovableService;
import com.btxtech.game.jsre.client.common.Level;
import com.btxtech.game.jsre.client.common.UserMessage;
import com.btxtech.game.jsre.client.common.info.GameInfo;
import com.btxtech.game.jsre.client.common.info.RealityInfo;
import com.btxtech.game.jsre.client.common.info.SimulationInfo;
import com.btxtech.game.jsre.common.NoConnectionException;
import com.btxtech.game.jsre.common.Packet;
import com.btxtech.game.jsre.common.StartupTaskInfo;
import com.btxtech.game.jsre.common.gameengine.services.user.PasswordNotMatchException;
import com.btxtech.game.jsre.common.gameengine.services.user.UserAlreadyExistsException;
import com.btxtech.game.jsre.common.gameengine.syncObjects.Id;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncBaseItem;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.BaseCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.syncInfos.SyncItemInfo;
import com.btxtech.game.jsre.common.tutorial.TutorialConfig;
import com.btxtech.game.jsre.common.utg.tracking.BrowserWindowTracking;
import com.btxtech.game.jsre.common.utg.tracking.EventTrackingItem;
import com.btxtech.game.jsre.common.utg.tracking.EventTrackingStart;
import com.btxtech.game.jsre.common.utg.tracking.SelectionTrackingItem;
import com.btxtech.game.jsre.common.utg.tracking.TerrainScrollTracking;
import com.btxtech.game.services.action.ActionService;
import com.btxtech.game.services.base.BaseService;
import com.btxtech.game.services.connection.ConnectionService;
import com.btxtech.game.services.connection.Session;
import com.btxtech.game.services.energy.ServerEnergyService;
import com.btxtech.game.services.item.ItemService;
import com.btxtech.game.services.market.ServerMarketService;
import com.btxtech.game.services.mgmt.MgmtService;
import com.btxtech.game.services.mgmt.StartupData;
import com.btxtech.game.services.terrain.TerrainService;
import com.btxtech.game.services.territory.TerritoryService;
import com.btxtech.game.services.tutorial.TutorialService;
import com.btxtech.game.services.user.UserService;
import com.btxtech.game.services.utg.DbAbstractLevel;
import com.btxtech.game.services.utg.DbRealGameLevel;
import com.btxtech.game.services.utg.DbSimulationLevel;
import com.btxtech.game.services.utg.UserGuidanceService;
import com.btxtech.game.services.utg.UserTrackingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component("movableService")
public class MovableServiceImpl implements MovableService {
    @Autowired
    private TerrainService terrainService;
    @Autowired
    private ActionService actionService;
    @Autowired
    private BaseService baseService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ConnectionService connectionService;
    @Autowired
    private ServerMarketService serverMarketService;
    @Autowired
    private ServerEnergyService serverEnergyService;
    @Autowired
    private UserTrackingService userTrackingService;
    @Autowired
    private UserGuidanceService userGuidanceService;
    @Autowired
    private UserService userService;
    @Autowired
    private MgmtService mgmtService;
    @Autowired
    private TerritoryService territoryService;
    @Autowired
    private TutorialService tutorialService;
    @Autowired
    private Session session;

    private Log log = LogFactory.getLog(MovableServiceImpl.class);

    @Override
    public void sendCommands(List<BaseCommand> baseCommands) {
        try {
            actionService.executeCommands(baseCommands);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public Collection<Packet> getSyncInfo() throws NoConnectionException {
        try {
            return connectionService.getConnection().getAndRemovePendingPackets();
        } catch (NoConnectionException e) {
            throw e;
        } catch (Throwable t) {
            log.error("", t);
            return null;
        }
    }

    @Override
    public Collection<SyncItemInfo> getAllSyncInfo() {
        try {
            return itemService.getSyncInfo();
        } catch (Throwable t) {
            log.error("", t);
            return null;
        }
    }

    @Override
    public void sendStartupInfo(List<StartupTaskInfo> infos, long totalTime) {
        try {
            userTrackingService.startUpTaskFinished(infos, totalTime);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public GameInfo getGameInfo() {
        try {
            DbAbstractLevel dbAbstractLevel = userGuidanceService.getDbAbstractLevel();
            if (dbAbstractLevel instanceof DbRealGameLevel) {
                return createRealInfo((DbRealGameLevel) dbAbstractLevel);
            } else if (dbAbstractLevel instanceof DbSimulationLevel) {
                return createSimulationInfo((DbSimulationLevel) dbAbstractLevel);
            } else {
                throw new IllegalArgumentException("Unknown DbAbstractLevel " + dbAbstractLevel);
            }
        } catch (Throwable t) {
            log.error("", t);
        }
        return null;
    }

    private GameInfo createRealInfo(DbRealGameLevel dbRealGameLevel) {
        try {
            baseService.continueBase();
            RealityInfo realityInfo = new RealityInfo();
            setCommonInfo(realityInfo, userService, itemService, mgmtService);
            realityInfo.setBase(baseService.getBase().getSimpleBase());
            realityInfo.setAccountBalance(baseService.getBase().getAccountBalance());
            realityInfo.setAllowedItemTypes(serverMarketService.getAllowedItemTypes());
            realityInfo.setXp(serverMarketService.getXp());
            realityInfo.setEnergyConsuming(serverEnergyService.getConsuming());
            realityInfo.setEnergyGenerating(serverEnergyService.getGenerating());
            terrainService.setupTerrain(realityInfo, dbRealGameLevel);
            realityInfo.setLevel(userGuidanceService.getDbLevel().getLevel());
            realityInfo.setTerritories(territoryService.getTerritories());
            realityInfo.setAllBases(baseService.getAllBaseAttributes());
            realityInfo.setHouseSpace(baseService.getBase().getHouseSpace());
            return realityInfo;
        } catch (com.btxtech.game.services.connection.NoConnectionException t) {
            log.error(t.getMessage() + ", SessionId: " + t.getSessionId());
        } catch (Throwable t) {
            log.error("", t);
        }
        return null;
    }

    private SimulationInfo createSimulationInfo(DbSimulationLevel dbSimulationLevel) {
        try {
            SimulationInfo simulationInfo = new SimulationInfo();
            simulationInfo.setLevel(dbSimulationLevel.getLevel());
            // Common
            setCommonInfo(simulationInfo, userService, itemService, mgmtService);
            simulationInfo.setTutorialConfig(tutorialService.getTutorialConfig(dbSimulationLevel));
            // Terrain
            terrainService.setupTerrain(simulationInfo, dbSimulationLevel);
            return simulationInfo;
        } catch (Throwable t) {
            log.error("", t);
        }
        return null;
    }

    public static void setCommonInfo(GameInfo gameInfo, UserService userService, ItemService itemService, MgmtService mgmtService) {
        gameInfo.setRegistered(userService.isRegistered());
        gameInfo.setItemTypes(itemService.getItemTypes());
        StartupData startupData = mgmtService.getStartupData();
        gameInfo.setRegisterDialogDelay(startupData.getRegisterDialogDelay());
    }

    @Override
    public void log(String message, Date date) {
        try {
            connectionService.clientLog(message, date);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void register(String userName, String password, String confirmPassword, String email) throws UserAlreadyExistsException, PasswordNotMatchException {
        try {
            userService.createUser(userName, password, confirmPassword, email);
            Object o = session.getRequest().getSession().getAttribute("wicket:wicket:" + org.apache.wicket.Session.SESSION_ATTRIBUTE_NAME);
            if (o == null) {
                throw new Exception("Wicket session not found");
            }
            ((AuthenticatedWebSession) o).signIn(userName, password);
        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (PasswordNotMatchException e) {
            throw e;
        } catch (Throwable t) {
            log.error("", t);
        }

    }

    @Override
    public void sendUserMessage(UserMessage userMessage) {
        try {
            connectionService.sendUserMessage(userMessage);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void surrenderBase() {
        try {
            baseService.surrenderBase(baseService.getBase());
            connectionService.closeConnection();
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void closeConnection() {
        try {
            connectionService.closeConnection();
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public Level sendTutorialProgress(TutorialConfig.TYPE type, String name, String parent, long duration, long clientTimeStamp) {
        try {
            return userTrackingService.onTutorialProgressChanged(type, name, parent, duration, clientTimeStamp);
        } catch (Throwable t) {
            log.error("", t);
            return null;
        }
    }

    @Override
    public void sendEventTrackingStart(EventTrackingStart eventTrackingStart) {
        try {
            userTrackingService.onEventTrackingStart(eventTrackingStart);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void sendEventTrackerItems(Collection<EventTrackingItem> eventTrackingItems, Collection<BaseCommand> baseCommands, Collection<SelectionTrackingItem> selectionTrackingItems, Collection<TerrainScrollTracking> terrainScrollTrackings, Collection<BrowserWindowTracking> browserWindowTrackings) {
        try {
            userTrackingService.onEventTrackerItems(eventTrackingItems, baseCommands, selectionTrackingItems, terrainScrollTrackings, browserWindowTrackings);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void sellItem(Id id) {
        try {
            itemService.sellItem(id);
        } catch (Throwable t) {
            log.error("", t);
        }
    }
}
