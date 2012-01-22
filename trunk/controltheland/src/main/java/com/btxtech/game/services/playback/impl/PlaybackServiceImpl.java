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

package com.btxtech.game.services.playback.impl;

import com.btxtech.game.jsre.common.gameengine.syncObjects.syncInfos.SyncItemInfo;
import com.btxtech.game.jsre.common.utg.tracking.BrowserWindowTracking;
import com.btxtech.game.jsre.common.utg.tracking.DialogTracking;
import com.btxtech.game.jsre.common.utg.tracking.EventTrackingItem;
import com.btxtech.game.jsre.common.utg.tracking.SelectionTrackingItem;
import com.btxtech.game.jsre.common.utg.tracking.TerrainScrollTracking;
import com.btxtech.game.jsre.playback.PlaybackInfo;
import com.btxtech.game.services.gwt.MovableServiceImpl;
import com.btxtech.game.services.item.ItemService;
import com.btxtech.game.services.mgmt.MgmtService;
import com.btxtech.game.services.playback.PlaybackService;
import com.btxtech.game.services.terrain.TerrainService;
import com.btxtech.game.services.tutorial.TutorialService;
import com.btxtech.game.services.user.UserService;
import com.btxtech.game.services.utg.DbLevel;
import com.btxtech.game.services.utg.LifecycleTrackingInfo;
import com.btxtech.game.services.utg.UserGuidanceService;
import com.btxtech.game.services.utg.UserTrackingService;
import com.btxtech.game.services.utg.tracker.DbBrowserWindowTracking;
import com.btxtech.game.services.utg.tracker.DbDialogTracking;
import com.btxtech.game.services.utg.tracker.DbSyncItemInfo;
import com.btxtech.game.services.utg.tracker.DbEventTrackingItem;
import com.btxtech.game.services.utg.tracker.DbScrollTrackingItem;
import com.btxtech.game.services.utg.tracker.DbSelectionTrackingItem;
import com.btxtech.game.wicket.uiservices.cms.CmsUiService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * User: beat
 * Date: 04.08.2010
 * Time: 11:11:41
 */
@Component("playbackService")
public class PlaybackServiceImpl implements PlaybackService {
    @Autowired
    private UserTrackingService userTrackingService;
    @Autowired
    private UserGuidanceService userGuidanceService;
    @Autowired
    private TerrainService terrainService;
    @Autowired
    private TutorialService tutorialService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private MgmtService mgmtService;
    @Autowired
    private UserService userService;
    @Autowired
    private CmsUiService cmsUiService;
    private Log log = LogFactory.getLog(PlaybackServiceImpl.class);

    @Override
    public PlaybackInfo getPlaybackInfo(String sessionId, long startTime, String levelName) {
        try {
            PlaybackInfo playbackInfo = new PlaybackInfo();

            // Tutorial
            MovableServiceImpl.setCommonInfo(playbackInfo, userService, itemService, mgmtService, cmsUiService);
            // TODO DbLevel dbLevel = userGuidanceService.getDbLevel(levelName);
            // TODO playbackInfo.setTutorialConfig(tutorialService.getTutorialConfig((DbSimulationLevel) dbLevel));
            // TODO playbackInfo.setLevelScope(dbLevel.getLevel());
            // TODO terrainService.setupTerrain(playbackInfo, dbLevel);

            // Start and and time


            LifecycleTrackingInfo lifecycleTrackingInfo = userTrackingService.getLifecycleTrackingInfo(sessionId, startTime);
            long startClient = lifecycleTrackingInfo.getStartClient();
            Long endClient = lifecycleTrackingInfo.getNextStartClient();

            playbackInfo.setEventTrackingStart(userTrackingService.getDbEventTrackingStart(sessionId, startClient, endClient).createEventTrackingStart());

            // Mouse tracking
            ArrayList<EventTrackingItem> eventTrackingItems = new ArrayList<EventTrackingItem>();
            for (DbEventTrackingItem dbEventTrackingItem : userTrackingService.getDbEventTrackingItem(sessionId, startClient, endClient)) {
                eventTrackingItems.add(dbEventTrackingItem.createEventTrackingItem());
            }
            playbackInfo.setEventTrackingItems(eventTrackingItems);

            // Selections
            ArrayList<SelectionTrackingItem> selectionTrackingItems = new ArrayList<SelectionTrackingItem>();
            for (DbSelectionTrackingItem dbSelectionTrackingItem : userTrackingService.getDbSelectionTrackingItems(sessionId, startClient, endClient)) {
                selectionTrackingItems.add(dbSelectionTrackingItem.createSelectionTrackingItem());
            }
            playbackInfo.setSelectionTrackingItems(selectionTrackingItems);

            // Commands
            ArrayList<SyncItemInfo> syncItemInfos = new ArrayList<SyncItemInfo>();
            for (DbSyncItemInfo dbSyncItemInfo : userTrackingService.getDbSyncItemInfos(sessionId, startClient, endClient)) {
                syncItemInfos.add(dbSyncItemInfo.getSyncItemInfo());
            }
            playbackInfo.setSyncItemInfos(syncItemInfos);

            // Scrolling
            ArrayList<TerrainScrollTracking> terrainScrollTrackings = new ArrayList<TerrainScrollTracking>();
            for (DbScrollTrackingItem dbScrollTrackingItem : userTrackingService.getDbScrollTrackingItems(sessionId, startClient, endClient)) {
                terrainScrollTrackings.add(dbScrollTrackingItem.createScrollTrackingItem());
            }
            playbackInfo.setScrollTrackingItems(terrainScrollTrackings);

            // Browser window tracking
            ArrayList<BrowserWindowTracking> browserWindowTrackings = new ArrayList<BrowserWindowTracking>();
            for (DbBrowserWindowTracking dbBrowserWindowTracking : userTrackingService.getDbBrowserWindowTrackings(sessionId, startClient, endClient)) {
                browserWindowTrackings.add(dbBrowserWindowTracking.createBrowserWindowTracking());
            }
            playbackInfo.setBrowserWindowTrackings(browserWindowTrackings);

            // Dialogs
            ArrayList<DialogTracking> dialogTrackings = new ArrayList<DialogTracking>();
            for (DbDialogTracking dbDialogTracking : userTrackingService.getDbDialogTrackings(sessionId, startClient, endClient)) {
                dialogTrackings.add(dbDialogTracking.createDialogTracking());
            }
            playbackInfo.setDialogTrackings(dialogTrackings);

            return playbackInfo;
        } catch (Throwable t) {
            log.error("", t);
            return null;
        }
    }
}
