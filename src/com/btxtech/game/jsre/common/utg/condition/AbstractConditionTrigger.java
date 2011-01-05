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

package com.btxtech.game.jsre.common.utg.condition;

import com.btxtech.game.jsre.common.utg.config.ConditionTrigger;

/**
 * User: beat
 * Date: 27.12.2010
 * Time: 23:33:37
 */
public class AbstractConditionTrigger<T> {
    private ConditionTrigger conditionTrigger;
    private AbstractComparison abstractComparison;
    private boolean fulfilled = false;
    private T userObject;

    public AbstractConditionTrigger(ConditionTrigger conditionTrigger, AbstractComparison abstractComparison, T userObject) {
        this.conditionTrigger = conditionTrigger;
        this.abstractComparison = abstractComparison;
        this.userObject = userObject;
    }

    public ConditionTrigger getConditionTrigger() {
        return conditionTrigger;
    }

    public AbstractComparison getAbstractComparison() {
        return abstractComparison;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    protected void setFulfilled() {
        fulfilled = true;
    }

    public T getUserObject() {
        return userObject;
    }
}
