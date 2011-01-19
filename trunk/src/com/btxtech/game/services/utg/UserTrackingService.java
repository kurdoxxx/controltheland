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

package com.btxtech.game.services.utg;

import com.btxtech.game.jsre.client.common.Level;
import com.btxtech.game.jsre.client.common.UserMessage;
import com.btxtech.game.jsre.common.EventTrackingItem;
import com.btxtech.game.jsre.common.EventTrackingStart;
import com.btxtech.game.jsre.common.ScrollTrackingItem;
import com.btxtech.game.jsre.common.SelectionTrackingItem;
import com.btxtech.game.jsre.common.StartupTaskInfo;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.BaseCommand;
import com.btxtech.game.jsre.common.tutorial.TutorialConfig;
import com.btxtech.game.services.base.Base;
import com.btxtech.game.services.user.User;
import com.btxtech.game.services.user.UserState;
import com.btxtech.game.wicket.pages.basepage.BasePage;
import java.util.Collection;
import java.util.List;

/**
 * User: beat
 * Date: 12.01.2010
 * Time: 22:04:31
 */
public interface UserTrackingService {
    void saveBrowserDetails(BrowserDetails browserDetails);

    void pageAccess(BasePage basePage);

    void pageAccess(Class theClass);

    List<VisitorInfo> getVisitorInfos(UserTrackingFilter filter);

    VisitorDetailInfo getVisitorDetails(String sessionId);

    void saveUserCommand(BaseCommand baseCommand);

    void onUserCreated(User user);

    void onUserLoggedIn(User user, Base base);

    void onUserLoggedOut(UserState user);

    void onBaseCreated(User user, Base base);

    void onBaseDefeated(User user, Base base);

    void onBaseSurrender(User user, Base base);

    void onUserEnterGame(User user);

    void onUserLeftGame(User user);

    void trackUserMessage(UserMessage userMessage);

    void levelPromotion(User user, DbAbstractLevel oldAbstractLevel);

    void onJavaScriptDetected();

    boolean isJavaScriptDetected();

    Level onTutorialProgressChanged(TutorialConfig.TYPE type, String name, String parent, long duration, long clientTimeStamp);

    void onEventTrackingStart(EventTrackingStart eventTrackingStart);

    void onEventTrackerItems(Collection<EventTrackingItem> eventTrackingItems, Collection<BaseCommand> baseCommands, Collection<SelectionTrackingItem> selectionTrackingItems, List<ScrollTrackingItem> scrollTrackingItems);

    List<DbEventTrackingStart> getDbEventTrackingStart(String sessionId);

    List<DbEventTrackingItem> getDbEventTrackingItem(DbEventTrackingStart begin, DbEventTrackingStart end);

    void startUpTaskFinished(Collection<StartupTaskInfo> infos, long totalTime);

    void onCloseWindow(long totalRunningTime, long clientTimeStamp);

    GameTrackingInfo getGameTracking(LifecycleTrackingInfo lifecycleTrackingInfo);

    TutorialTrackingInfo getTutorialTrackingInfo(LifecycleTrackingInfo lifecycleTrackingInfo);

    List<DbSelectionTrackingItem> getDbSelectionTrackingItems(String sessionId, long startTime, Long endTime);

    List<DbCommand> getDbCommands(String sessionId, long startTime, Long endTime);

    List<DbScrollTrackingItem> getDbScrollTrackingItems(final String sessionId, final long startTime, final Long endTime);
}
