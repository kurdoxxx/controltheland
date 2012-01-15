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

package com.btxtech.game.jsre.common.utg.tracking;

import java.io.Serializable;

/**
 * User: beat
 * Date: 03.08.2010
 * Time: 22:12:06
 */
public class EventTrackingItem implements Serializable {
    private int xPos;
    private int yPos;
    private int eventType;
    private long clientTimeStamp;

    /**
     * Used by GWT
     */
    public EventTrackingItem() {
    }

    public EventTrackingItem(int xPos, int yPos, int eventType) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.eventType = eventType;
        clientTimeStamp = System.currentTimeMillis();
    }

    public EventTrackingItem(int xPos, int yPos, int eventType, long clientTimeStamp) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.eventType = eventType;
        this.clientTimeStamp = clientTimeStamp;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public int getEventType() {
        return eventType;
    }

    public long getClientTimeStamp() {
        return clientTimeStamp;
    }
}