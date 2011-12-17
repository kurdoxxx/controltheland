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

import java.util.Date;

/**
 * User: beat
 * Date: 16.01.2010
 * Time: 12:24:41
 */
public class SessionOverviewDto {
    public static final int MAX_REFERER_LENGTH = 30;
    private Date date;
    private String sessionId;
    private int pageHits;
    private int enterGameHits;
    private int successfulStarts;
    private boolean startupFailure;
    private int commands;
    private int levelPromotions;
    private String referer;

    public SessionOverviewDto(Date date,
                       String sessionId,
                       int pageHits,
                       int enterGameHits,
                       int successfulStarts,
                       boolean startupFailure, int commands,
                       int levelPromotions,
                       String referer) {
        this.date = date;
        this.sessionId = sessionId;
        this.pageHits = pageHits;
        this.enterGameHits = enterGameHits;
        this.successfulStarts = successfulStarts;
        this.startupFailure = startupFailure;
        this.commands = commands;
        this.levelPromotions = levelPromotions;
        if (referer != null && referer.length() > MAX_REFERER_LENGTH) {
            this.referer = referer.substring(0, MAX_REFERER_LENGTH);
        } else {
            this.referer = referer;
        }
    }

    public Date getDate() {
        return date;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getPageHits() {
        return pageHits;
    }

    public int getEnterGameHits() {
        return enterGameHits;
    }

    public int getCommands() {
        return commands;
    }

    public String getReferer() {
        return referer;
    }

    public int getLevelPromotions() {
        return levelPromotions;
    }

    public int getSuccessfulStarts() {
        return successfulStarts;
    }

    public boolean isStartupFailure() {
        return startupFailure;
    }
}