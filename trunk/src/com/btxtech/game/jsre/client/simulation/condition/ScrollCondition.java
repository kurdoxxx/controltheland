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

package com.btxtech.game.jsre.client.simulation.condition;

import com.btxtech.game.jsre.client.ClientBase;
import com.btxtech.game.jsre.common.tutorial.condition.HarvestConditionConfig;
import com.btxtech.game.jsre.common.tutorial.condition.ScrollConditionConfig;

/**
 * User: beat
 * Date: 01.12.2010
 * Time: 21:05:57
 */
public class ScrollCondition extends AbstractCondition {

    @Override
    public boolean isFulfilledScroll() {
        return true;
    }
}