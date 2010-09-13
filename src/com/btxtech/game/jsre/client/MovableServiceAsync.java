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

import com.btxtech.game.jsre.client.common.UserMessage;
import com.btxtech.game.jsre.common.EventTrackingItem;
import com.btxtech.game.jsre.common.EventTrackingStart;
import com.btxtech.game.jsre.common.SelectionTrackingItem;
import com.btxtech.game.jsre.common.SimpleBase;
import com.btxtech.game.jsre.common.gameengine.syncObjects.Id;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.BaseCommand;
import com.btxtech.game.jsre.common.tutorial.TutorialConfig;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * The async counterpart of <code>MovableService</code>.
 */
public interface MovableServiceAsync {
    void getGameInfo(AsyncCallback async);

    void log(String message, Date date, AsyncCallback async);

    void sendCommands(List<BaseCommand> baseCommands, AsyncCallback async);

    void getSyncInfo(SimpleBase simpleBase, AsyncCallback async);

    void getAllSyncInfo(AsyncCallback async);

    void register(String userName, String password, String confirmPassword, String email, AsyncCallback<Void> asyncCallback);

    void sendUserMessage(UserMessage userMessage, AsyncCallback<Void> asyncCallback);

    void surrenderBase(AsyncCallback<Void> asyncCallback);

    void closeConnection(AsyncCallback<Void> async);

    void getMissionTarget(AsyncCallback<String> asyncCallback);

    void tutorialTerminated(AsyncCallback<Void> async);

    void startUpTaskFinished(StartupTask state, Date clientTimeStamp, long duration, AsyncCallback<Void> asyncCallback);

    void startUpTaskFailed(StartupTask state, Date clientTimeStamp, long duration, String failureText, AsyncCallback<Void> asyncCallback);

    void sendTutorialProgress(TutorialConfig.TYPE type, String name, String parent, long duration, long clientTimeStamp, AsyncCallback<Void> asyncCallback);

    void sendEventTrackingStart(EventTrackingStart eventTrackingStart, AsyncCallback<Void> asyncCallback);

    void sendEventTrackerItems(Collection<EventTrackingItem> eventTrackingItems, Collection<BaseCommand> baseCommands, Collection<SelectionTrackingItem> selectionTrackingItems, AsyncCallback<Void> asyncCallback);

    void sendTotalStartupTime(long totalStartupTime, long clientTimeStamp, AsyncCallback<Void> asyncCallback);

    void sendCloseWindow(long totalRunningTime, long clientTimeStamp, AsyncCallback<Void> asyncCallback);

    void getFreeColors(int index, int count, AsyncCallback<List<String>> asyncCallback);

    void setBaseColor(String color, AsyncCallback<Void> asyncCallback);

    void sellItem(Id id, AsyncCallback<Void> asyncCallback);
}
