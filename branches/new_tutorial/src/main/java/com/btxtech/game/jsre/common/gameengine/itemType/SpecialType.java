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

package com.btxtech.game.jsre.common.gameengine.itemType;

import com.btxtech.game.jsre.client.common.RadarMode;

import java.io.Serializable;

/**
 * User: beat
 * Date: 23.12.2009
 * Time: 12:44:52
 */
public class SpecialType implements Serializable {
    private RadarMode radarMode;

    /**
     * Used by GWT
     */
    SpecialType() {
    }

    public SpecialType(RadarMode radarMode) {
        this.radarMode = radarMode;
    }

    public RadarMode getRadarMode() {
        return radarMode;
    }

    public void changeTo(SpecialType specialType) {
        radarMode = specialType.radarMode;
    }
}