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

package com.btxtech.game.jsre.client.simulation;

import com.btxtech.game.jsre.client.ClientBase;
import com.btxtech.game.jsre.client.Connection;
import com.btxtech.game.jsre.client.GameCommon;
import com.btxtech.game.jsre.client.GwtCommon;
import com.btxtech.game.jsre.client.ParametrisedRunnable;
import com.btxtech.game.jsre.client.cockpit.Cockpit;
import com.btxtech.game.jsre.client.cockpit.SelectionHandler;
import com.btxtech.game.jsre.client.common.Level;
import com.btxtech.game.jsre.client.common.info.SimulationInfo;
import com.btxtech.game.jsre.client.item.ItemContainer;
import com.btxtech.game.jsre.client.terrain.MapWindow;
import com.btxtech.game.jsre.client.terrain.TerrainView;
import com.btxtech.game.jsre.client.utg.ClientLevelHandler;
import com.btxtech.game.jsre.client.utg.ClientUserTracker;
import com.btxtech.game.jsre.common.gameengine.services.items.NoSuchItemTypeException;
import com.btxtech.game.jsre.common.utg.ConditionServiceListener;
import com.btxtech.game.jsre.common.utg.config.CockpitWidgetEnum;
import com.btxtech.game.jsre.common.tutorial.ItemTypeAndPosition;
import com.btxtech.game.jsre.common.tutorial.TaskConfig;
import com.btxtech.game.jsre.common.tutorial.TutorialConfig;
import com.google.gwt.user.client.Timer;
import java.util.List;

/**
 * User: beat
 * Date: 17.07.2010
 * Time: 17:21:24
 */
public class Simulation implements ConditionServiceListener<Object> {
    private static final Simulation SIMULATION = new Simulation();
    private SimulationInfo simulationInfo;
    private Task activeTask;
    private TutorialGui tutorialGui;
    private long taskTime;
    private long tutorialTime;

    /**
     * Singleton
     */
    private Simulation() {
    }

    public static Simulation getInstance() {
        return SIMULATION;
    }

    public void start() {
        simulationInfo = (SimulationInfo) Connection.getInstance().getGameInfo();
        TutorialConfig tutorialConfig = simulationInfo.getTutorialConfig();
        if (tutorialConfig == null) {
            return;
        }
        if (tutorialGui == null) {
            SelectionHandler.getInstance().addSelectionListener(SimulationConditionServiceImpl.getInstance());
            TerrainView.getInstance().addTerrainScrollListener(SimulationConditionServiceImpl.getInstance());
            tutorialGui = new TutorialGui();
        }
        SimulationConditionServiceImpl.getInstance().setConditionServiceListener(this);
        ClientBase.getInstance().setBase(tutorialConfig.getOwnBase());
        Cockpit.getInstance().updateBase();
        tutorialTime = System.currentTimeMillis();
        MapWindow.getAbsolutePanel().getElement().getStyle().setProperty("minWidth", tutorialConfig.getWidth() + "px");
        MapWindow.getAbsolutePanel().getElement().getStyle().setProperty("minHeight", tutorialConfig.getHeight() + "px");
        if (tutorialConfig.isEventTracking()) {
            ClientUserTracker.getInstance().startEventTracking();
        }
        runNextTask(activeTask);
    }

    private void processPreparation(TaskConfig taskConfig) {
        if (taskConfig.isClearGame()) {
            GameCommon.clearGame();
        }

        Cockpit.getInstance().setVisibleRadar(taskConfig.isScrollingAllowed());
        MapWindow.getInstance().setScrollingAllowed(taskConfig.isScrollingAllowed());
        Cockpit.getInstance().enableFocusWidget(CockpitWidgetEnum.SCROLL_HOME_BUTTON, taskConfig.isScrollingAllowed());
        Cockpit.getInstance().enableFocusWidget(CockpitWidgetEnum.OPTION_BUTTON, taskConfig.isOptionAllowed());
        Cockpit.getInstance().enableFocusWidget(CockpitWidgetEnum.SELL_BUTTON, taskConfig.isSellingAllowed());
        ClientLevelHandler.getInstance().getLevel().setHouseSpace(taskConfig.getHouseCount());
        Cockpit.getInstance().updateItemLimit();

        for (ItemTypeAndPosition itemTypeAndPosition : taskConfig.getOwnItems()) {
            try {
                ItemContainer.getInstance().createSimulationSyncObject(itemTypeAndPosition);
            } catch (NoSuchItemTypeException e) {
                GwtCommon.handleException(e);
            }
        }

        if (taskConfig.getScroll() != null) {
            TerrainView.getInstance().moveAbsolute(taskConfig.getScroll());
        }
    }

    private void runNextTask(Task closedTask) {
        TaskConfig taskConfig;
        List<TaskConfig> tasks = simulationInfo.getTutorialConfig().getTasks();
        if (tasks.isEmpty()) {
            tutorialFinished();
            return;
        }
        if (closedTask != null) {
            int index = tasks.indexOf(closedTask.getTaskConfig());
            index++;
            if (tasks.size() > index) {
                taskConfig = tasks.get(index);
            } else {
                tutorialFinished();
                return;
            }
        } else {
            taskConfig = tasks.get(0);
        }
        processPreparation(taskConfig);
        taskTime = System.currentTimeMillis();
        activeTask = new Task(taskConfig);
        tutorialGui.setTaskText(taskConfig.getTaskText());
    }

    private void tutorialFinished() {
        activeTask = null;
        long time = System.currentTimeMillis();
        ClientUserTracker.getInstance().onTutorialFinished(time - tutorialTime, time, new ParametrisedRunnable<Level>() {
            @Override
            public void run(Level level) {
                ClientLevelHandler.getInstance().onLevelChanged(level);
            }
        });
    }

    /*
    TODO checkForTutorialFailed
    private void checkForTutorialFailed() {
        // TODO mission failed startup sequence + send to server
        if (simulationInfo.getTutorialConfig().isFailOnOwnItemsLost() && ItemContainer.getInstance().getOwnItemCount() == 0) {
            throw new RuntimeException("Not implemented yet");
        } else if (simulationInfo.getTutorialConfig().isFailOnMoneyBelowAndNoAttackUnits() != null
                && ClientBase.getInstance().getAccountBalance() < simulationInfo.getTutorialConfig().isFailOnMoneyBelowAndNoAttackUnits()
                && !ItemContainer.getInstance().hasOwnAttackingMovable()) {
            throw new RuntimeException("Not implemented yet");
        }
    }*/

    public void cleanup() {
        if (tutorialGui != null) {
            tutorialGui.cleanup();
            tutorialGui = null;
        }
        SelectionHandler.getInstance().removeSelectionListener(SimulationConditionServiceImpl.getInstance());
        TerrainView.getInstance().removeTerrainScrollListener(SimulationConditionServiceImpl.getInstance());
        SimulationConditionServiceImpl.getInstance().setConditionServiceListener(null);
    }

    public void conditionPassed(Object ignore) {
        activeTask.runNextStep();
        if (activeTask.isFulfilled()) {
            long time = System.currentTimeMillis();
            activeTask.cleanup();
            ClientUserTracker.getInstance().onTaskFinished(activeTask, time - taskTime, time);
            if (activeTask.getTaskConfig().getFinishImageDuration() > 0 && activeTask.getTaskConfig().getFinishImageId() != null) {
                tutorialGui.showFinishImage(activeTask.getTaskConfig().getFinishImageId(), activeTask.getTaskConfig().getFinishImageDuration());
                final Task closedTask = activeTask;
                activeTask = null;
                Timer timer = new Timer() {
                    @Override
                    public void run() {
                        runNextTask(closedTask);
                    }
                };
                timer.schedule(closedTask.getTaskConfig().getFinishImageDuration());
            } else {
                runNextTask(activeTask);
            }
        }
    }
}
