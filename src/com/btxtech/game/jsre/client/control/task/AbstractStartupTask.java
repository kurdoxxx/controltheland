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

import com.btxtech.game.jsre.client.control.StartupTaskEnum;
import com.btxtech.game.jsre.common.StartupTaskInfo;

/**
 * User: beat
 * Date: 04.12.2010
 * Time: 11:29:57
 */
public abstract class AbstractStartupTask {
    private long startTime;
    private long duration;
    private StartupTaskEnum taskEnum;
    private boolean isBackground = false;

    protected AbstractStartupTask(StartupTaskEnum taskEnum) {
        this.taskEnum = taskEnum;
    }

    protected abstract void privateStart(DeferredStartup deferredStartup);

    public void start(DeferredStartup deferredStartup) {
        startTime = calculateStartTime();
        try {
            privateStart(deferredStartup);
            isBackground = deferredStartup.isBackground();
        } finally {
            duration = System.currentTimeMillis() - startTime;
        }
    }

    protected long calculateStartTime() {
        return System.currentTimeMillis();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public StartupTaskEnum getTaskEnum() {
        return taskEnum;
    }

    public StartupTaskInfo createStartupTaskInfo() {
        return new StartupTaskInfo(taskEnum, startTime, duration);
    }

    public void correctDeferredDuration() {
        duration = System.currentTimeMillis() - startTime;
    }

    public boolean isBackground() {
        return isBackground;
    }
}
