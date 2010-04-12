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

import java.io.Serializable;
import java.util.Collection;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 23:18:42
 */
public class FactoryType implements Serializable {
    private int progress;
    private Collection<Integer> ableToBuild;

    /**
     * Used by GWT
     */
    FactoryType() {
    }

    public FactoryType(int progress, Collection<Integer> ableToBuild) {
        this.progress = progress;
        this.ableToBuild = ableToBuild;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isAbleToBuild(int itemTypeId) {
        return ableToBuild.contains(itemTypeId);
    }

    public Collection<Integer> getAbleToBuild() {
        return ableToBuild;
    }

    public void changeTo(FactoryType factoryType) {
        progress = factoryType.progress;
        ableToBuild = factoryType.ableToBuild;
    }
}
